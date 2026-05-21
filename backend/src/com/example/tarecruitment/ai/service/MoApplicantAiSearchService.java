package com.example.tarecruitment.ai.service;

import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.ai.client.DeepSeekApplicantSearchClient;
import com.example.tarecruitment.ai.client.DeepSeekApplicantSearchClient.SearchRecommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MO applicant AI recommendation service.
 *
 * Current frontend entry is the applicant recommendation search in MO dashboard.
 * Only sends whitelist fields and sanitized candidate context to DeepSeek.
 * When there is no key or service is unavailable, it directly returns “AI search temporarily unavailable”,
 * without local fake recommendations.
 */
public class MoApplicantAiSearchService {

    public static final String DEFAULT_QUERY = "Recommend the most suitable applicants for the current position";
    public static final String OUT_OF_SCOPE_MESSAGE =
            "I cannot process your question. Based on the current applicants for this position, I can help you recommend candidates, compare applicants, or explain the recommendation reasons.";
    public static final String UNAVAILABLE_MESSAGE = "AI search is temporarily unavailable. Please try again later.";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?\\d[\\d\\s().-]{6,}\\d)(?!\\d)");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("(?<!\\d)(?:20\\d{8}|\\d{8,10})(?!\\d)");
    private static final int MAX_TEXT_LENGTH = 280;

    private final DeepSeekApplicantSearchClient client;

    public MoApplicantAiSearchService(DeepSeekApplicantSearchClient client) {
        this.client = client;
    }

    public SearchResult search(Job job,
                               List<Application> applications,
                               Map<String, Applicant> applicantsByUserId,
                               String rawQuery) {
        if (job == null) {
            throw new IllegalArgumentException("Job is required.");
        }
        if (client == null || !client.isConfigured()) {
            return SearchResult.unavailable(UNAVAILABLE_MESSAGE);
        }

        List<CandidateContext> candidates = buildCandidates(applications, applicantsByUserId);
        if (candidates.isEmpty()) {
            return SearchResult.recommend("No applicants available for recommendation at this time.", Collections.emptyList());
        }

        String query = normalizeQuery(rawQuery);
        DeepSeekApplicantSearchClient.SearchAttempt attempt = client == null
                ? DeepSeekApplicantSearchClient.SearchAttempt.failure("DeepSeek client is unavailable.")
                : client.search(buildSystemPrompt(), buildUserPrompt(job, candidates, query));

        if (!attempt.hasResult()) {
            return SearchResult.unavailable(UNAVAILABLE_MESSAGE);
        }

        DeepSeekApplicantSearchClient.SearchPayload payload = attempt.getPayload();
        if ("out_of_scope".equals(payload.getAction())) {
            return SearchResult.outOfScope(OUT_OF_SCOPE_MESSAGE);
        }
        if (!"recommend".equals(payload.getAction())) {
            return SearchResult.unavailable(UNAVAILABLE_MESSAGE);
        }

        Map<String, CandidateContext> byRef = new LinkedHashMap<>();
        for (CandidateContext candidate : candidates) {
            byRef.put(candidate.candidateRef, candidate);
        }

        List<RecommendedApplication> recommendations = new ArrayList<>();
        Set<String> seenApplicationIds = new LinkedHashSet<>();
        for (SearchRecommendation recommendation : payload.getRecommendations()) {
            CandidateContext candidate = byRef.get(recommendation.getCandidateRef());
            if (candidate == null || !seenApplicationIds.add(candidate.application.getApplicationId())) {
                continue;
            }
            // candidateRef is only an internal reference in the prompt; frontend does not display C1/C2;
// replace with applicant display name before returning.
            recommendations.add(new RecommendedApplication(
                    candidate.application,
                    replaceCandidateRefs(recommendation.getRecommendation(), byRef)
            ));
        }

        String message = payload.getMessage().isEmpty()
                ? "AI recommendation results have been generated."
                : replaceCandidateRefs(payload.getMessage(), byRef);
        return SearchResult.recommend(message, recommendations);
    }

    private String buildSystemPrompt() {
        return "You are an AI search assistant in the TA recruitment system that helps MO review applicants."
                + "You can only handle current job applicant recommendations, applicant comparisons, and recommendation reason explanations."
                + "If the user question is outside this scope, you must return JSON:"
                + "{\"action\":\"out_of_scope\",\"message\":\"" + OUT_OF_SCOPE_MESSAGE + "\",\"results\":[]}."
                + "If the user question is within the scope, you must return a JSON object:"
                + "{\"action\":\"recommend\",\"message\":\"brief explanation\",\"results\":[{\"candidateRef\":\"C1\",\"recommendation\":\"recommendation reason\"}]}."
                + "You can only use the candidateRef provided in the input, cannot fabricate candidates, and cannot output Markdown or additional text."
                + "Recommendation reasons should be specific, concise, and based on the candidate's skills, experience, motivation, cover letter, and job requirements.";
    }

    private String buildUserPrompt(Job job, List<CandidateContext> candidates, String query) {
        StringBuilder prompt = new StringBuilder(1600);
        prompt.append("User question: ").append(query).append("\n\n");
        prompt.append("Current job:\n");
        prompt.append("- title: ").append(safe(job.getTitle())).append("\n");
        prompt.append("- courseCode: ").append(safe(job.getCourseCode())).append("\n");
        prompt.append("- courseName: ").append(safe(job.getCourseName())).append("\n");
        prompt.append("- workload: ").append(safe(job.getWorkload())).append("\n");
        prompt.append("- description: ").append(sanitizeFreeText(job.getDescription())).append("\n");
        prompt.append("- requiredSkills: ").append(join(normalizeSkills(job.getRequiredSkills()))).append("\n\n");
        prompt.append("Candidate list (sanitized; do not output names, emails, phones, student IDs, addresses, or file paths):\n");
        for (CandidateContext candidate : candidates) {
            prompt.append(candidate.candidateRef).append(":\n");
            prompt.append("- applicationStatus: ").append(safe(candidate.application.getStatus() != null
                    ? candidate.application.getStatus().name()
                    : "PENDING")).append("\n");
            prompt.append("- department: ").append(candidate.department).append("\n");
            prompt.append("- program: ").append(candidate.program).append("\n");
            prompt.append("- gpa: ").append(candidate.gpa).append("\n");
            prompt.append("- skills: ").append(join(candidate.skills)).append("\n");
            prompt.append("- experience: ").append(candidate.experience).append("\n");
            prompt.append("- motivation: ").append(candidate.motivation).append("\n");
            prompt.append("- coverLetter: ").append(candidate.coverLetter).append("\n");
        }
        return prompt.toString();
    }

    private List<CandidateContext> buildCandidates(List<Application> applications,
                                                   Map<String, Applicant> applicantsByUserId) {
        if (applications == null || applications.isEmpty()) {
            return Collections.emptyList();
        }

        List<CandidateContext> candidates = new ArrayList<>();
        for (Application application : applications) {
            if (application == null || isBlank(application.getApplicationId())) {
                continue;
            }
            Applicant applicant = applicantsByUserId == null ? null : applicantsByUserId.get(application.getApplicantId());
            if (applicant == null) {
                continue;
            }
            // candidateRef is a temporary reference number for AI ranking and referencing,
// to avoid putting real name/email into the prompt.
            String candidateRef = "C" + (candidates.size() + 1);
            candidates.add(new CandidateContext(
                    candidateRef,
                    application,
                    sanitizeFreeText(applicant.getDepartment()),
                    sanitizeFreeText(applicant.getProgram()),
                    sanitizeFreeText(applicant.getGpa()),
                    normalizeSkills(applicant.getSkills()),
                    sanitizeFreeText(applicant.getExperience()),
                    sanitizeFreeText(applicant.getMotivation()),
                    sanitizeFreeText(application.getCoverLetter())
            ));
        }
        return candidates;
    }

    private String normalizeQuery(String rawQuery) {
        String query = sanitizeFreeText(rawQuery);
        return query.isEmpty() ? DEFAULT_QUERY : query;
    }

    private String sanitizeFreeText(String text) {
        if (isBlank(text)) {
            return "";
        }
        String sanitized = text.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[sanitized email]");
        sanitized = STUDENT_ID_PATTERN.matcher(sanitized).replaceAll("[sanitized student ID]");
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[sanitized phone]");
        sanitized = sanitized.replaceAll("\\s{2,}", " ").trim();
        if (sanitized.length() > MAX_TEXT_LENGTH) {
            sanitized = sanitized.substring(0, MAX_TEXT_LENGTH).trim();
        }
        return sanitized;
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> deduplicated = new LinkedHashSet<>();
        for (String skill : skills) {
            String normalized = sanitizeFreeText(skill);
            if (!normalized.isEmpty()) {
                deduplicated.add(normalized);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(deduplicated));
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(", ", values);
    }

    private String replaceCandidateRefs(String text, Map<String, CandidateContext> candidatesByRef) {
        String result = safe(text);
        if (result.isEmpty() || candidatesByRef == null || candidatesByRef.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, CandidateContext> entry : candidatesByRef.entrySet()) {
            String candidateRef = entry.getKey();
            CandidateContext candidate = entry.getValue();
            if (candidate == null || candidate.application == null) {
                continue;
            }
            String displayName = safe(candidate.application.getApplicantName());
            if (candidateRef == null || candidateRef.isEmpty() || displayName.isEmpty()) {
                continue;
            }
            result = result.replaceAll(
                    "(?<![A-Za-z0-9_])" + Pattern.quote(candidateRef) + "(?![A-Za-z0-9_])",
                    Matcher.quoteReplacement(displayName)
            );
        }
        return result;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class CandidateContext {
        private final String candidateRef;
        private final Application application;
        private final String department;
        private final String program;
        private final String gpa;
        private final List<String> skills;
        private final String experience;
        private final String motivation;
        private final String coverLetter;

        private CandidateContext(String candidateRef,
                                 Application application,
                                 String department,
                                 String program,
                                 String gpa,
                                 List<String> skills,
                                 String experience,
                                 String motivation,
                                 String coverLetter) {
            this.candidateRef = candidateRef;
            this.application = application;
            this.department = department;
            this.program = program;
            this.gpa = gpa;
            this.skills = skills;
            this.experience = experience;
            this.motivation = motivation;
            this.coverLetter = coverLetter;
        }
    }

    public static final class SearchResult {
        private final String action;
        private final boolean success;
        private final String message;
        private final List<RecommendedApplication> recommendations;

        private SearchResult(String action,
                             boolean success,
                             String message,
                             List<RecommendedApplication> recommendations) {
            this.action = action;
            this.success = success;
            this.message = safe(message);
            this.recommendations = recommendations == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(recommendations));
        }

        public static SearchResult recommend(String message, List<RecommendedApplication> recommendations) {
            return new SearchResult("recommend", true, message, recommendations);
        }

        public static SearchResult outOfScope(String message) {
            return new SearchResult("out_of_scope", true, message, Collections.emptyList());
        }

        public static SearchResult unavailable(String message) {
            return new SearchResult("unavailable", false, message, Collections.emptyList());
        }

        public String getAction() {
            return action;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<RecommendedApplication> getRecommendations() {
            return recommendations;
        }
    }

    public static final class RecommendedApplication {
        private final Application application;
        private final String recommendation;

        public RecommendedApplication(Application application, String recommendation) {
            this.application = application;
            this.recommendation = safe(recommendation);
        }

        public Application getApplication() {
            return application;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }
}

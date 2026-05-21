package com.example.tarecruitment.ai.service;

import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.ai.client.DeepSeekTaJobSearchClient;
import com.example.tarecruitment.ai.client.DeepSeekTaJobSearchClient.SearchRecommendation;

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
 * TA job list AI recommendation service.
 *
 * Current frontend entry is the AI search mode in TA job list.
 * Only sends TA profile whitelist fields and currently open jobs to DeepSeek.
 * When there is no key or service is unavailable, it directly returns “AI recommendation temporarily unavailable”,
 * without local fake recommendations.
 */
public class TaJobAiSearchService {

    public static final String DEFAULT_QUERY = "Recommend the most suitable open TA positions for me";
    public static final String OUT_OF_SCOPE_MESSAGE =
            "I cannot process your question. Based on your personal profile and currently open positions, I can help you recommend positions, compare positions, or explain recommendation reasons.";
    public static final String UNAVAILABLE_MESSAGE = "AI recommendation is temporarily unavailable. Please try again later.";
    public static final String PROFILE_REQUIRED_MESSAGE = "Please complete your profile before using AI recommendation.";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?\\d[\\d\\s().-]{6,}\\d)(?!\\d)");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("(?<!\\d)(?:20\\d{8}|\\d{8,10})(?!\\d)");
    private static final int MAX_TEXT_LENGTH = 280;

    private final DeepSeekTaJobSearchClient client;

    public TaJobAiSearchService(DeepSeekTaJobSearchClient client) {
        this.client = client;
    }

    public SearchResult search(Applicant applicant, List<Job> jobs, String rawQuery) {
        if (applicant == null) {
            return SearchResult.profileRequired(PROFILE_REQUIRED_MESSAGE);
        }
        if (client == null || !client.isConfigured()) {
            return SearchResult.unavailable(UNAVAILABLE_MESSAGE);
        }

        List<JobContext> jobContexts = buildJobs(jobs);
        if (jobContexts.isEmpty()) {
            return SearchResult.recommend("Currently no open positions available for recommendation.", Collections.emptyList());
        }

        String query = normalizeQuery(rawQuery);
        DeepSeekTaJobSearchClient.SearchAttempt attempt = client.search(
                buildSystemPrompt(),
                buildUserPrompt(applicant, jobContexts, query)
        );

        if (!attempt.hasResult()) {
            return SearchResult.unavailable(UNAVAILABLE_MESSAGE);
        }

        DeepSeekTaJobSearchClient.SearchPayload payload = attempt.getPayload();
        if ("out_of_scope".equals(payload.getAction())) {
            return SearchResult.outOfScope(OUT_OF_SCOPE_MESSAGE);
        }
        if (!"recommend".equals(payload.getAction())) {
            return SearchResult.unavailable(UNAVAILABLE_MESSAGE);
        }

        Map<String, JobContext> byRef = new LinkedHashMap<>();
        for (JobContext jobContext : jobContexts) {
            byRef.put(jobContext.jobRef, jobContext);
        }

        List<RecommendedJob> recommendations = new ArrayList<>();
        Set<String> seenJobIds = new LinkedHashSet<>();
        for (SearchRecommendation recommendation : payload.getRecommendations()) {
            JobContext jobContext = byRef.get(recommendation.getJobRef());
            if (jobContext == null || jobContext.job == null || !seenJobIds.add(jobContext.job.getJobId())) {
                continue;
            }
            // jobRef is only an internal reference in the prompt; frontend does not display J1/J2;
// replace with job title before returning.
            recommendations.add(new RecommendedJob(
                    jobContext.job,
                    replaceJobRefs(recommendation.getRecommendation(), byRef)
            ));
        }

        String message = payload.getMessage().isEmpty()
                ? "AI recommended positions have been generated."
                : replaceJobRefs(payload.getMessage(), byRef);
        return SearchResult.recommend(message, recommendations);
    }

    private String buildSystemPrompt() {
        return "You are an AI recommendation assistant in the TA recruitment system that helps TA choose suitable positions from open positions."
                + "You can only handle current open position recommendations, position comparisons, and recommendation reason explanations."
                + "If the user question is outside this scope, you must return JSON:"
                + "{\"action\":\"out_of_scope\",\"message\":\"" + OUT_OF_SCOPE_MESSAGE + "\",\"results\":[]}."
                + "If the user question is within the scope, you must return a JSON object:"
                + "{\"action\":\"recommend\",\"message\":\"brief explanation\",\"results\":[{\"jobRef\":\"J1\",\"recommendation\":\"recommendation reason\"}]}."
                + "You can only use the jobRef provided in the input, cannot fabricate positions, and cannot output Markdown or additional text."
                + "Recommendation reasons should be specific, concise, and based on the TA's skills, experience, motivation, GPA, and job requirements.";
    }

    private String buildUserPrompt(Applicant applicant, List<JobContext> jobContexts, String query) {
        StringBuilder prompt = new StringBuilder(1800);
        prompt.append("User question: ").append(query).append("\n\n");
        prompt.append("TA personal profile (sanitized; do not output names, emails, phones, student IDs, addresses, or file paths):\n");
        prompt.append("- department: ").append(sanitizeFreeText(applicant.getDepartment())).append("\n");
        prompt.append("- program: ").append(sanitizeFreeText(applicant.getProgram())).append("\n");
        prompt.append("- gpa: ").append(sanitizeFreeText(applicant.getGpa())).append("\n");
        prompt.append("- skills: ").append(join(normalizeSkills(applicant.getSkills()))).append("\n");
        prompt.append("- experience: ").append(sanitizeFreeText(applicant.getExperience())).append("\n");
        prompt.append("- motivation: ").append(sanitizeFreeText(applicant.getMotivation())).append("\n\n");
        prompt.append("Open position list (only these jobRefs are allowed for recommendation):\n");
        for (JobContext jobContext : jobContexts) {
            Job job = jobContext.job;
            prompt.append(jobContext.jobRef).append(":\n");
            prompt.append("- title: ").append(safe(job.getTitle())).append("\n");
            prompt.append("- courseCode: ").append(safe(job.getCourseCode())).append("\n");
            prompt.append("- courseName: ").append(safe(job.getCourseName())).append("\n");
            prompt.append("- positions: ").append(job.getPositions()).append("\n");
            prompt.append("- workload: ").append(safe(job.getWorkload())).append("\n");
            prompt.append("- salary: ").append(safe(job.getSalary())).append("\n");
            prompt.append("- deadline: ").append(job.getDeadline() != null ? job.getDeadline().toString() : "").append("\n");
            prompt.append("- description: ").append(sanitizeFreeText(job.getDescription())).append("\n");
            prompt.append("- requiredSkills: ").append(join(normalizeSkills(job.getRequiredSkills()))).append("\n");
        }
        return prompt.toString();
    }

    private List<JobContext> buildJobs(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return Collections.emptyList();
        }

        List<JobContext> contexts = new ArrayList<>();
        for (Job job : jobs) {
            if (job == null || isBlank(job.getJobId())) {
                continue;
            }
            // Use temporary jobRef to avoid AI directly touching internal jobId,
// and also so the model can only recommend jobs from the input list.
            contexts.add(new JobContext("J" + (contexts.size() + 1), job));
        }
        return contexts;
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

    private String replaceJobRefs(String text, Map<String, JobContext> jobsByRef) {
        String result = safe(text);
        if (result.isEmpty() || jobsByRef == null || jobsByRef.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, JobContext> entry : jobsByRef.entrySet()) {
            String jobRef = entry.getKey();
            JobContext jobContext = entry.getValue();
            if (jobContext == null || jobContext.job == null) {
                continue;
            }
            String displayTitle = safe(jobContext.job.getTitle());
            if (jobRef == null || jobRef.isEmpty() || displayTitle.isEmpty()) {
                continue;
            }
            result = result.replaceAll(
                    "(?<![A-Za-z0-9_])" + Pattern.quote(jobRef) + "(?![A-Za-z0-9_])",
                    Matcher.quoteReplacement(displayTitle)
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

    private static final class JobContext {
        private final String jobRef;
        private final Job job;

        private JobContext(String jobRef, Job job) {
            this.jobRef = jobRef;
            this.job = job;
        }
    }

    public static final class SearchResult {
        private final String action;
        private final boolean success;
        private final String message;
        private final List<RecommendedJob> recommendations;

        private SearchResult(String action,
                             boolean success,
                             String message,
                             List<RecommendedJob> recommendations) {
            this.action = safe(action);
            this.success = success;
            this.message = safe(message);
            this.recommendations = recommendations == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(recommendations));
        }

        public static SearchResult recommend(String message, List<RecommendedJob> recommendations) {
            return new SearchResult("recommend", true, message, recommendations);
        }

        public static SearchResult outOfScope(String message) {
            return new SearchResult("out_of_scope", true, message, Collections.emptyList());
        }

        public static SearchResult unavailable(String message) {
            return new SearchResult("unavailable", false, message, Collections.emptyList());
        }

        public static SearchResult profileRequired(String message) {
            return new SearchResult("profile_required", false, message, Collections.emptyList());
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

        public List<RecommendedJob> getRecommendations() {
            return recommendations;
        }
    }

    public static final class RecommendedJob {
        private final Job job;
        private final String recommendation;

        public RecommendedJob(Job job, String recommendation) {
            this.job = job;
            this.recommendation = safe(recommendation);
        }

        public Job getJob() {
            return job;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }
}

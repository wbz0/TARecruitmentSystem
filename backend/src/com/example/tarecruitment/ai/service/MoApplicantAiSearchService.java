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
 * MO 申请人 AI 推荐服务。
 *
 * 当前前端入口是 MO dashboard 的申请人推荐搜索。
 * 只把白名单字段和脱敏后的候选人上下文发给 DeepSeek。
 * 没有 key 或服务不可用时，直接返回“AI 搜索暂不可用”，不做本地假推荐。
 */
public class MoApplicantAiSearchService {

    public static final String DEFAULT_QUERY = "推荐当前职位最适合的申请人";
    public static final String OUT_OF_SCOPE_MESSAGE =
            "我无法处理您的问题。我可以根据当前职位的申请人信息，帮你推荐候选人、比较申请人或解释推荐理由。";
    public static final String UNAVAILABLE_MESSAGE = "AI 搜索暂不可用，请稍后再试。";

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
            return SearchResult.recommend("当前职位暂无可推荐申请人。", Collections.emptyList());
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
            // candidateRef 只是 prompt 内部引用，前端不展示 C1/C2；返回前替换成申请人显示名。
            recommendations.add(new RecommendedApplication(
                    candidate.application,
                    replaceCandidateRefs(recommendation.getRecommendation(), byRef)
            ));
        }

        String message = payload.getMessage().isEmpty()
                ? "已生成 AI 推荐结果。"
                : replaceCandidateRefs(payload.getMessage(), byRef);
        return SearchResult.recommend(message, recommendations);
    }

    private String buildSystemPrompt() {
        return "你是 TA 招聘系统中帮助 MO 审核申请人的 AI 搜索助手。"
                + "你只能处理当前职位申请人推荐、申请人比较、推荐理由解释。"
                + "如果用户问题超出这个范围，必须返回 JSON："
                + "{\"action\":\"out_of_scope\",\"message\":\"" + OUT_OF_SCOPE_MESSAGE + "\",\"results\":[]}。"
                + "如果用户问题属于范围，必须返回 JSON 对象："
                + "{\"action\":\"recommend\",\"message\":\"简短说明\",\"results\":[{\"candidateRef\":\"C1\",\"recommendation\":\"推荐理由\"}]}。"
                + "只能使用输入中提供的 candidateRef，不能编造候选人，不能输出 Markdown 或额外文本。"
                + "推荐理由要具体、简洁，并基于候选人的技能、经历、动机、求职信与岗位要求。";
    }

    private String buildUserPrompt(Job job, List<CandidateContext> candidates, String query) {
        StringBuilder prompt = new StringBuilder(1600);
        prompt.append("用户问题：").append(query).append("\n\n");
        prompt.append("当前职位：\n");
        prompt.append("- title: ").append(safe(job.getTitle())).append("\n");
        prompt.append("- courseCode: ").append(safe(job.getCourseCode())).append("\n");
        prompt.append("- courseName: ").append(safe(job.getCourseName())).append("\n");
        prompt.append("- workload: ").append(safe(job.getWorkload())).append("\n");
        prompt.append("- description: ").append(sanitizeFreeText(job.getDescription())).append("\n");
        prompt.append("- requiredSkills: ").append(join(normalizeSkills(job.getRequiredSkills()))).append("\n\n");
        prompt.append("候选人列表（已脱敏；不要输出姓名、邮箱、电话、学号、地址或文件路径）：\n");
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
            // candidateRef 是给 AI 排序和引用用的临时编号，避免把真实姓名/邮箱放进 prompt。
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
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[已脱敏邮箱]");
        sanitized = STUDENT_ID_PATTERN.matcher(sanitized).replaceAll("[已脱敏学号]");
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[已脱敏电话]");
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

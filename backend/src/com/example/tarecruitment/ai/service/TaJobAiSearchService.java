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
 * TA 职位列表 AI 推荐服务。
 *
 * 当前前端入口是 TA job list 的 AI 搜索模式。
 * 只把 TA 档案的白名单字段和当前开放职位发给 DeepSeek。
 * 没有 key 或服务不可用时，直接返回“AI 推荐暂不可用”，不做本地假推荐。
 */
public class TaJobAiSearchService {

    public static final String DEFAULT_QUERY = "推荐最适合我的开放 TA 职位";
    public static final String OUT_OF_SCOPE_MESSAGE =
            "我无法处理您的问题。我可以根据你的个人档案和当前开放职位，帮你推荐职位、比较职位或解释推荐理由。";
    public static final String UNAVAILABLE_MESSAGE = "AI 推荐暂不可用，请稍后再试。";
    public static final String PROFILE_REQUIRED_MESSAGE = "请先完善个人档案后再使用 AI 推荐。";

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
            return SearchResult.recommend("当前暂无可推荐的开放职位。", Collections.emptyList());
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
            // jobRef 只是 prompt 内部引用，前端不展示 J1/J2；返回前替换成职位标题。
            recommendations.add(new RecommendedJob(
                    jobContext.job,
                    replaceJobRefs(recommendation.getRecommendation(), byRef)
            ));
        }

        String message = payload.getMessage().isEmpty()
                ? "已生成 AI 推荐职位。"
                : replaceJobRefs(payload.getMessage(), byRef);
        return SearchResult.recommend(message, recommendations);
    }

    private String buildSystemPrompt() {
        return "你是 TA 招聘系统中帮助 TA 从开放职位中选择合适岗位的 AI 推荐助手。"
                + "你只能处理当前开放职位推荐、职位比较、推荐理由解释。"
                + "如果用户问题超出这个范围，必须返回 JSON："
                + "{\"action\":\"out_of_scope\",\"message\":\"" + OUT_OF_SCOPE_MESSAGE + "\",\"results\":[]}。"
                + "如果用户问题属于范围，必须返回 JSON 对象："
                + "{\"action\":\"recommend\",\"message\":\"简短说明\",\"results\":[{\"jobRef\":\"J1\",\"recommendation\":\"推荐理由\"}]}。"
                + "只能使用输入中提供的 jobRef，不能编造职位，不能输出 Markdown 或额外文本。"
                + "推荐理由要具体、简洁，并基于 TA 的技能、经历、动机、GPA 与职位要求。";
    }

    private String buildUserPrompt(Applicant applicant, List<JobContext> jobContexts, String query) {
        StringBuilder prompt = new StringBuilder(1800);
        prompt.append("用户问题：").append(query).append("\n\n");
        prompt.append("TA 个人档案（已脱敏；不要输出姓名、邮箱、电话、学号、地址或文件路径）：\n");
        prompt.append("- department: ").append(sanitizeFreeText(applicant.getDepartment())).append("\n");
        prompt.append("- program: ").append(sanitizeFreeText(applicant.getProgram())).append("\n");
        prompt.append("- gpa: ").append(sanitizeFreeText(applicant.getGpa())).append("\n");
        prompt.append("- skills: ").append(join(normalizeSkills(applicant.getSkills()))).append("\n");
        prompt.append("- experience: ").append(sanitizeFreeText(applicant.getExperience())).append("\n");
        prompt.append("- motivation: ").append(sanitizeFreeText(applicant.getMotivation())).append("\n\n");
        prompt.append("开放职位列表（只允许推荐这些 jobRef）：\n");
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
            // 用临时 jobRef 避免 AI 直接接触内部 jobId，也让模型只能推荐输入列表里的职位。
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

package com.example.tarecruitment.ai.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * TA 职位推荐使用的 DeepSeek 客户端。
 *
 * 只负责调用 DeepSeek 并解析 JSON；开放职位过滤、已申请职位排除、
 * 以及脱敏后的 prompt 组装都在 service/servlet 层完成。
 */
public class DeepSeekTaJobSearchClient {

    private final HttpClient httpClient;
    private final DeepSeekAiConfig config;

    public DeepSeekTaJobSearchClient(DeepSeekAiConfig config) {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build(), config);
    }

    DeepSeekTaJobSearchClient(HttpClient httpClient, DeepSeekAiConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    /**
     * 推荐类 AI 没有本地假推荐兜底；未配置 key 时直接返回 unavailable。
     */
    public boolean isConfigured() {
        return config != null && config.isApiKeyConfigured();
    }

    /**
     * 调用 DeepSeek 生成 TA 职位推荐。
     *
     * prompt 中只包含 service 层允许的职位和 TA 档案摘要；客户端不接触完整页面状态。
     */
    public SearchAttempt search(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            return SearchAttempt.failure("deepseek.api.key is missing or placeholder.");
        }
        if (isBlank(systemPrompt) || isBlank(userPrompt)) {
            return SearchAttempt.failure("Prompt content is empty.");
        }

        String endpoint = config.getBaseUrl() + "/chat/completions";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(config.getTimeoutMillis()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(
                        DeepSeekChatClient.buildJsonRequestBody(config, systemPrompt, userPrompt)
                ))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return SearchAttempt.failure("DeepSeek endpoint returned status " + response.statusCode() + ".");
            }

            Optional<SearchPayload> payload = parseResponse(response.body());
            if (payload.isEmpty()) {
                return SearchAttempt.failure("DeepSeek response format is invalid.");
            }
            return SearchAttempt.success(payload.get());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return SearchAttempt.failure("DeepSeek request interrupted.");
        } catch (IOException ex) {
            return SearchAttempt.failure("DeepSeek request IO error: " + ex.getMessage());
        } catch (RuntimeException ex) {
            return SearchAttempt.failure("DeepSeek request failed: " + ex.getMessage());
        }
    }

    /**
     * 解析 DeepSeek assistant content。
     *
     * action 只接受 recommend / out_of_scope，未知结构会被视为格式错误。
     */
    private Optional<SearchPayload> parseResponse(String body) {
        if (isBlank(body)) {
            return Optional.empty();
        }
        String json = DeepSeekChatClient.extractAssistantJson(body);
        if (isBlank(json)) {
            return Optional.empty();
        }

        String action = DeepSeekChatClient.extractStringField(json, "action").toLowerCase(Locale.ROOT);
        if (!"recommend".equals(action) && !"out_of_scope".equals(action)) {
            return Optional.empty();
        }

        String message = safe(DeepSeekChatClient.extractStringField(json, "message"));
        List<SearchRecommendation> results = "recommend".equals(action)
                ? extractRecommendations(json)
                : Collections.emptyList();

        return Optional.of(new SearchPayload(action, message, results));
    }

    /**
     * 解析职位推荐数组。
     *
     * jobRef 是 prompt 内部引用，service 会映射回真实 jobId 并保持只展示开放职位。
     */
    private List<SearchRecommendation> extractRecommendations(String json) {
        String arrayBody = DeepSeekChatClient.extractArrayBody(json, "results");
        if (isBlank(arrayBody)) {
            return Collections.emptyList();
        }
        List<String> objects = DeepSeekChatClient.extractObjectBlocks(arrayBody);
        List<SearchRecommendation> recommendations = new ArrayList<>();
        for (String object : objects) {
            String jobRef = DeepSeekChatClient.extractStringField(object, "jobRef");
            String recommendation = DeepSeekChatClient.extractStringField(object, "recommendation");
            if (!isBlank(jobRef) && !isBlank(recommendation)) {
                recommendations.add(new SearchRecommendation(jobRef, recommendation));
            }
        }
        return Collections.unmodifiableList(recommendations);
    }

    private static boolean isBlank(String value) {
        return DeepSeekChatClient.isBlank(value);
    }

    private static String safe(String value) {
        return DeepSeekChatClient.safe(value);
    }

    /**
     * DeepSeek 调用结果包装，避免 service 层处理 HTTP/IO/解析异常细节。
     */
    public static final class SearchAttempt {
        private final SearchPayload payload;
        private final String failureReason;

        private SearchAttempt(SearchPayload payload, String failureReason) {
            this.payload = payload;
            this.failureReason = safe(failureReason);
        }

        public static SearchAttempt success(SearchPayload payload) {
            return new SearchAttempt(payload, "");
        }

        public static SearchAttempt failure(String reason) {
            return new SearchAttempt(null, reason);
        }

        public boolean hasResult() {
            return payload != null;
        }

        public SearchPayload getPayload() {
            return payload;
        }

        public String getFailureReason() {
            return failureReason;
        }
    }

    /**
     * 客户端解析后的职位推荐响应。
     */
    public static final class SearchPayload {
        private final String action;
        private final String message;
        private final List<SearchRecommendation> recommendations;

        public SearchPayload(String action, String message, List<SearchRecommendation> recommendations) {
            this.action = safe(action).toLowerCase(Locale.ROOT);
            this.message = safe(message);
            this.recommendations = recommendations == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(recommendations));
        }

        public String getAction() {
            return action;
        }

        public String getMessage() {
            return message;
        }

        public List<SearchRecommendation> getRecommendations() {
            return recommendations;
        }
    }

    /**
     * 单条职位推荐。
     */
    public static final class SearchRecommendation {
        private final String jobRef;
        private final String recommendation;

        public SearchRecommendation(String jobRef, String recommendation) {
            this.jobRef = safe(jobRef);
            this.recommendation = safe(recommendation);
        }

        public String getJobRef() {
            return jobRef;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }
}

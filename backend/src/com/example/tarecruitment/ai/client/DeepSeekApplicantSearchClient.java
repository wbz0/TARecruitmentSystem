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
 * DeepSeek client for MO applicant recommendations.
 *
 * Only handles external interface communication and JSON parsing;
 * which applicants can be recommended and which fields can be sent to AI are controlled by MoApplicantAiSearchService.
 */
public class DeepSeekApplicantSearchClient {

    private final HttpClient httpClient;
    private final DeepSeekAiConfig config;

    public DeepSeekApplicantSearchClient(DeepSeekAiConfig config) {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(3000))
                .build(), config);
    }

    DeepSeekApplicantSearchClient(HttpClient httpClient, DeepSeekAiConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    /**
     * Recommendation AI has no local fake recommendation fallback; when key is not configured, it directly returns unavailable.
     */
    public boolean isConfigured() {
        return config != null && config.isApiKeyConfigured();
    }

    /**
     * Call DeepSeek to generate MO applicant recommendations.
     *
     * Prompt is constructed and sanitized by the service layer; client only handles sending requests and parsing stable JSON.
     */
    public SearchAttempt search(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            return SearchAttempt.failure("deepseek.api.key is missing or placeholder.");
        }
        if (isBlank(systemPrompt) || isBlank(userPrompt)) {
            return SearchAttempt.failure("Prompt content is empty.");
        }

        String endpoint = config.getBaseUrl() + "/chat/completions";
        String requestBody = DeepSeekChatClient.buildJsonRequestBody(config, systemPrompt, userPrompt);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(config.getTimeoutMillis()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
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
     * Parse DeepSeek assistant content.
     *
     * Only accepts recommend / out_of_scope actions to prevent model from returning free text that frontend might misuse.
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
     * Parse recommendation results array.
     *
     * candidateRef is an internal reference provided by the service layer to AI;
     * backend will map it back to real applicationId/name.
     */
    private List<SearchRecommendation> extractRecommendations(String json) {
        String arrayBody = DeepSeekChatClient.extractArrayBody(json, "results");
        if (isBlank(arrayBody)) {
            return Collections.emptyList();
        }
        List<String> objects = DeepSeekChatClient.extractObjectBlocks(arrayBody);
        List<SearchRecommendation> recommendations = new ArrayList<>();
        for (String object : objects) {
            String candidateRef = DeepSeekChatClient.extractStringField(object, "candidateRef");
            String recommendation = DeepSeekChatClient.extractStringField(object, "recommendation");
            if (!isBlank(candidateRef) && !isBlank(recommendation)) {
                recommendations.add(new SearchRecommendation(candidateRef, recommendation));
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
     * DeepSeek call result wrapper, avoiding service layer from handling HTTP/IO/parsing exception details.
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
     * Client-parsed recommendation response.
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
     * Single applicant recommendation.
     */
    public static final class SearchRecommendation {
        private final String candidateRef;
        private final String recommendation;

        public SearchRecommendation(String candidateRef, String recommendation) {
            this.candidateRef = safe(candidateRef);
            this.recommendation = safe(recommendation);
        }

        public String getCandidateRef() {
            return candidateRef;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }
}

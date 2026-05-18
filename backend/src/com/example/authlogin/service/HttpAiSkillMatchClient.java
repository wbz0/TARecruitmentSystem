package com.example.authlogin.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 HTTP 的 AI 技能匹配客户端。
 * 通过环境变量配置：
 * - SKILL_MATCH_AI_ENDPOINT
 * - SKILL_MATCH_AI_API_KEY（可选，若服务无需鉴权可留空）
 * - SKILL_MATCH_AI_TIMEOUT_MS（可选，默认 2000）
 */
public class HttpAiSkillMatchClient implements AiSkillMatchClient {

    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");
    private static final Pattern REASON_PATTERN = Pattern.compile("\"reason\"\\s*:\\s*\"([^\"]*)\"");
    private static final long DEFAULT_TIMEOUT_MS = 2000L;

    private final HttpClient httpClient;
    private final String endpoint;
    private final String apiKey;
    private final Duration timeout;

    public HttpAiSkillMatchClient() {
        this(HttpClient.newHttpClient(),
                System.getenv("SKILL_MATCH_AI_ENDPOINT"),
                System.getenv("SKILL_MATCH_AI_API_KEY"),
                parseTimeout(System.getenv("SKILL_MATCH_AI_TIMEOUT_MS")));
    }

    public HttpAiSkillMatchClient(HttpClient httpClient, String endpoint, String apiKey, Duration timeout) {
        this.httpClient = httpClient;
        this.endpoint = endpoint != null ? endpoint.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.timeout = timeout != null ? timeout : Duration.ofMillis(DEFAULT_TIMEOUT_MS);
    }

    @Override
    public Optional<AiScoreResult> score(List<String> requiredKeywords, List<String> applicantKeywords) {
        if (endpoint.isEmpty()) {
            return Optional.empty();
        }

        List<String> safeRequired = requiredKeywords != null ? requiredKeywords : Collections.emptyList();
        List<String> safeApplicant = applicantKeywords != null ? applicantKeywords : Collections.emptyList();

        String payload = buildPayload(safeRequired, safeApplicant);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload));

        if (!apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }
            return parseResponse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private Optional<AiScoreResult> parseResponse(String body) {
        if (body == null || body.isEmpty()) {
            return Optional.empty();
        }

        Matcher scoreMatcher = SCORE_PATTERN.matcher(body);
        if (!scoreMatcher.find()) {
            return Optional.empty();
        }

        double score;
        try {
            score = Double.parseDouble(scoreMatcher.group(1));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        score = Math.max(0.0, Math.min(100.0, score));

        String reason = "";
        Matcher reasonMatcher = REASON_PATTERN.matcher(body);
        if (reasonMatcher.find()) {
            reason = unescapeJson(reasonMatcher.group(1));
        }

        return Optional.of(new AiScoreResult(score, reason));
    }

    private String buildPayload(List<String> requiredKeywords, List<String> applicantKeywords) {
        return "{"
                + "\"task\":\"skill_match\","
                + "\"requiredKeywords\":" + toJsonArray(requiredKeywords) + ","
                + "\"applicantKeywords\":" + toJsonArray(applicantKeywords)
                + "}";
    }

    private String toJsonArray(List<String> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < values.size(); i++) {
            sb.append("\"").append(escapeJson(values.get(i))).append("\"");
            if (i < values.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String text) {
        return text.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static Duration parseTimeout(String timeoutText) {
        if (timeoutText == null || timeoutText.trim().isEmpty()) {
            return Duration.ofMillis(DEFAULT_TIMEOUT_MS);
        }
        try {
            long timeoutMs = Long.parseLong(timeoutText.trim());
            if (timeoutMs <= 0) {
                return Duration.ofMillis(DEFAULT_TIMEOUT_MS);
            }
            return Duration.ofMillis(timeoutMs);
        } catch (NumberFormatException e) {
            return Duration.ofMillis(DEFAULT_TIMEOUT_MS);
        }
    }
}

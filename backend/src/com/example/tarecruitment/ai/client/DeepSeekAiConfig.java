package com.example.tarecruitment.ai.client;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * DeepSeek OpenAI-compatible API configuration.
 *
 * Read priority: local properties file > System Property > Environment Variable.
 * This configuration supports “recommendation” AI features; the configuration itself is not displayed on the frontend.
 * If the key is missing or still a placeholder, the service layer will return “AI temporarily unavailable”,
 * rather than generating local fake recommendations.
 */
public final class DeepSeekAiConfig {

    // Local secret file path; *.local.properties are not committed to the repository.
    private static final String LOCAL_CONFIG_PATH = "/WEB-INF/ai/deepseek.local.properties";
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_MODEL = "deepseek-v4-flash";
    private static final long DEFAULT_TIMEOUT_MS = 8000L;

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final long timeoutMillis;

    private DeepSeekAiConfig(String apiKey, String baseUrl, String model, long timeoutMillis) {
        this.apiKey = safe(apiKey);
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = isBlank(model) ? DEFAULT_MODEL : model.trim();
        this.timeoutMillis = timeoutMillis > 0 ? timeoutMillis : DEFAULT_TIMEOUT_MS;
    }

    /**
     * Load DeepSeek configuration.
     *
     * Local properties are suitable for course demos; System Property/Environment Variable are suitable for deployment.
     * Callers don't need to know where the configuration comes from.
     */
    public static DeepSeekAiConfig load(ServletContext servletContext) {
        Properties localProps = loadLocalProperties(servletContext);

        // Local file is suitable for classroom demos; System Property / Environment Variable is suitable for deployment.
        String apiKey = readConfig(localProps, "deepseek.api.key", "deepseek.api.key", "DEEPSEEK_API_KEY");
        String baseUrl = readConfig(localProps, "deepseek.base-url", "deepseek.base-url", "DEEPSEEK_BASE_URL");
        String model = readConfig(localProps, "deepseek.model", "deepseek.model", "DEEPSEEK_MODEL");
        String timeoutText = readConfig(localProps, "deepseek.timeout-ms", "deepseek.timeout-ms", "DEEPSEEK_TIMEOUT_MS");

        return new DeepSeekAiConfig(apiKey, baseUrl, model, parseTimeout(timeoutText));
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    /**
     * Check if API key is genuinely configured.
     *
     * Placeholders are treated as not configured; recommendation AI will return “temporarily unavailable”,
     * and will not use local fake recommendations.
     */
    public boolean isApiKeyConfigured() {
        if (isBlank(apiKey)) {
            return false;
        }
        String lower = apiKey.toLowerCase(Locale.ROOT);
        return !(lower.contains("replace")
                || lower.contains("placeholder")
                || lower.contains("your_api")
                || lower.contains("change_me")
                || lower.contains("changeme"));
    }

    /**
     * Read local configuration file under WEB-INF.
     */
    private static Properties loadLocalProperties(ServletContext servletContext) {
        Properties properties = new Properties();
        if (servletContext == null) {
            return properties;
        }
        try (InputStream inputStream = servletContext.getResourceAsStream(LOCAL_CONFIG_PATH)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            servletContext.log("Failed to load DeepSeek local config: " + LOCAL_CONFIG_PATH, e);
        }
        return properties;
    }

    /**
     * Read configuration in order: local file -> JVM argument -> environment variable.
     */
    private static String readConfig(Properties localProps, String localKey, String propertyName, String envName) {
        String localValue = localProps.getProperty(localKey);
        if (!isBlank(localValue)) {
            return localValue.trim();
        }

        String propertyValue = System.getProperty(propertyName);
        if (!isBlank(propertyValue)) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(envName);
        if (!isBlank(envValue)) {
            return envValue.trim();
        }
        return "";
    }

    /**
     * Parse timeout; invalid values fallback to default.
     */
    private static long parseTimeout(String text) {
        if (isBlank(text)) {
            return DEFAULT_TIMEOUT_MS;
        }
        try {
            long parsed = Long.parseLong(text.trim());
            return parsed > 0 ? parsed : DEFAULT_TIMEOUT_MS;
        } catch (NumberFormatException ex) {
            return DEFAULT_TIMEOUT_MS;
        }
    }

    /**
     * Normalize base URL, remove trailing slashes to avoid double slashes when appending /chat/completions.
     */
    private static String normalizeBaseUrl(String url) {
        String normalized = isBlank(url) ? DEFAULT_BASE_URL : url.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String safe(String text) {
        return text == null ? "" : text.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

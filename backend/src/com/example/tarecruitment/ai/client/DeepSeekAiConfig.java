package com.example.tarecruitment.ai.client;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * DeepSeek OpenAI-compatible API 配置。
 *
 * 读取优先级：本地 properties 文件 > System Property > Environment Variable。
 * 这份配置支撑“推荐类”AI 功能；配置本身不在前端展示。
 * 如果 key 缺失或还是占位符，服务层会返回 AI 暂不可用，而不是生成本地假推荐。
 */
public final class DeepSeekAiConfig {

    // 本地密钥文件路径；*.local.properties 不提交仓库。
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
     * 加载 DeepSeek 配置。
     *
     * 本地 properties 适合课程演示，System Property/Environment Variable 适合部署；
     * 调用方不需要知道配置来自哪里。
     */
    public static DeepSeekAiConfig load(ServletContext servletContext) {
        Properties localProps = loadLocalProperties(servletContext);

        // 本地文件适合课堂演示；System Property / Environment Variable 适合部署环境。
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
     * 判断 API key 是否真实配置。
     *
     * 占位符会被视为未配置，推荐类 AI 将返回“暂不可用”，不会走本地假推荐。
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
     * 读取 WEB-INF 下的本地配置文件。
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
     * 按本地文件 -> JVM 参数 -> 环境变量的顺序读取配置。
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
     * 解析超时时间，非法值回退默认值。
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
     * 规范化 base URL，去掉尾部斜杠，避免拼接 /chat/completions 时出现双斜杠。
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

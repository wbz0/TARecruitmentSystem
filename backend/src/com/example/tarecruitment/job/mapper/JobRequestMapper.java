package com.example.tarecruitment.job.mapper;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JobRequestMapper - 职位接口的请求转换工具。
 *
 * 只负责把 HTTP 路径/表单文本转换成 service 可用的普通参数，
 * 不做权限判断，也不直接读写 CSV。
 */
public final class JobRequestMapper {

    private JobRequestMapper() {
    }

    /**
     * 从 /api/jobs/{jobId} 的 pathInfo 中取出 jobId。
     *
     * Servlet 只关心第一段路径，后续如果出现 /extra 之类的子路径，
     * 也不会把它误当成职位 ID 的一部分。
     */
    public static String pathJobId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return "";
        }
        // /api/jobs/{jobId}/extra 只取第一段，当前前端没有额外子资源。
        String text = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int slash = text.indexOf('/');
        return slash >= 0 ? text.substring(0, slash) : text;
    }

    /**
     * 把可空文本统一压成空字符串，方便 service/validator 少写 null 判断。
     */
    public static String trimToEmpty(String value) {
        return value != null ? value.trim() : "";
    }

    /**
     * 把职位表单中的技能文本转换成列表。
     *
     * 前端提交的是逗号分隔文本，CSV 模型里保存的是技能列表；
     * 这里仅做格式转换，是否合法仍由 JobValidator 负责。
     */
    public static List<String> normalizeSkillsToList(String rawSkills) {
        if (rawSkills == null || rawSkills.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 当前表单只允许中英文逗号分隔；分号等格式在 JobValidator 中会被拒绝。
        return Arrays.stream(rawSkills.split("[,，]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 解析 weeklyHours 字段。
     *
     * 返回 null 表示用户没填或格式不合法，调用方会结合校验结果决定
     * 是使用默认值、保留旧值，还是返回 bad request。
     */
    public static Double parseWeeklyHours(String weeklyHoursText) {
        if (weeklyHoursText == null || weeklyHoursText.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(weeklyHoursText.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析浏览器 datetime-local 提交的截止时间。
     *
     * 不同浏览器可能提交秒级或分钟级精度，所以按 ISO、秒级、分钟级
     * 依次尝试，保证前端控件的小差异不会破坏创建/编辑职位。
     */
    public static LocalDateTime parseDeadline(String deadlineStr) {
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            return null;
        }

        String text = deadlineStr.trim();
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
            // 继续兼容浏览器 datetime-local 的不同精度。
        }

        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (Exception ignored) {
            // Try minute precision below.
        }

        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 手动解析 PUT 的 x-www-form-urlencoded 请求体。
     *
     * 兼容说明：POST 表单一般可以用 request.getParameter，
     * 但 PUT 请求在部分 Servlet 容器中不会自动填充 parameter map。
     */
    public static Map<String, String> formParameters(HttpServletRequest request) throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        if (request == null) {
            return values;
        }
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            return values;
        }

        // PUT 的 x-www-form-urlencoded body 在 Servlet API 中不一定会自动进 parameter map。
        String body = request.getReader().lines().collect(Collectors.joining("&"));
        if (body.trim().isEmpty()) {
            return values;
        }

        for (String pair : body.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int separator = pair.indexOf('=');
            String rawKey = separator >= 0 ? pair.substring(0, separator) : pair;
            String rawValue = separator >= 0 ? pair.substring(separator + 1) : "";
            String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
            String value = URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
            values.put(key, value);
        }
        return values;
    }

    /**
     * 按白名单读取前端提交字段。
     *
     * JobServlet 传入 JOB_FIELDS，确保未知参数不会绕过 mapper 直接进入
     * JobService 的创建流程。
     */
    public static Map<String, String> requestParameters(HttpServletRequest request, String... names) {
        Map<String, String> values = new LinkedHashMap<>();
        if (request == null || names == null) {
            return values;
        }
        for (String name : names) {
            values.put(name, request.getParameter(name));
        }
        return values;
    }
}

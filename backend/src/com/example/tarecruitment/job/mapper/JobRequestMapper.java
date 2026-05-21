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
 * JobRequestMapper - Job interface request conversion utility.
 *
 * Only responsible for converting HTTP path/form text to ordinary parameters usable by service,
 * does not do permission check, does not directly read/write CSV.
 */
public final class JobRequestMapper {

    private JobRequestMapper() {
    }

    /**
     * Extract jobId from pathInfo of /api/jobs/{jobId}.
     *
     * Servlet only cares about first path segment, if /extra-like sub-paths appear later,
     * it will not be mistaken as part of job ID.
     */
    public static String pathJobId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return "";
        }
        // /api/jobs/{jobId}/extra only takes first segment; current frontend has no extra sub-resources.
        String text = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int slash = text.indexOf('/');
        return slash >= 0 ? text.substring(0, slash) : text;
    }

    /**
     * Coerce nullable text to empty string uniformly, to reduce null checks in service/validator.
     */
    public static String trimToEmpty(String value) {
        return value != null ? value.trim() : "";
    }

    /**
     * Convert skills text in job form to list.
     *
     * Frontend submits comma-separated text, CSV model stores skills list;
     * here only does format conversion, whether legal is still JobValidator responsibility.
     */
    public static List<String> normalizeSkillsToList(String rawSkills) {
        if (rawSkills == null || rawSkills.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Current form only allows English or Chinese comma separator; semicolons and other formats will be rejected in JobValidator.
        return Arrays.stream(rawSkills.split("[,，]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Parse weeklyHours field.
     *
     * Return null means user did not fill or format invalid, caller will decide based on validation result
     * whether to use default value, keep old value, or return bad request.
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
     * Parse deadline submitted by browser datetime-local.
     *
     * Different browsers may submit second or minute precision, so try ISO, second, minute
     * in order to ensure small differences in frontend controls do not break create/edit job.
     */
    public static LocalDateTime parseDeadline(String deadlineStr) {
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            return null;
        }

        String text = deadlineStr.trim();
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
            // Continue to be compatible with different precision of browser datetime-local.
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
     * Manually parse PUT x-www-form-urlencoded request body.
     *
     * Compatibility note: POST forms generally can use request.getParameter,
     * but PUT requests in some Servlet containers do not automatically populate parameter map.
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

        // PUT x-www-form-urlencoded body may not automatically populate parameter map in Servlet API.
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
     * Read frontend submitted fields by whitelist.
     *
     * JobServlet passes JOB_FIELDS, ensuring unknown parameters do not bypass mapper to directly enter
     * JobService creation flow.
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

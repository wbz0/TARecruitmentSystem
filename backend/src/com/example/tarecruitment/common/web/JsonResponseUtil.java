package com.example.tarecruitment.common.web;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * JsonResponseUtil - Low-level JSON serialization helper.
 *
 * New Servlets prefer ApiResponses; this class is kept as a compatibility layer for low-level implementation and a few old calls.
 */
public final class JsonResponseUtil {

    private JsonResponseUtil() {
    }

    public static void writeJsonResponse(HttpServletResponse response,
                                         int status,
                                         boolean success,
                                         String message,
                                         Map<String, Object> data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        StringBuilder json = new StringBuilder(256);
        json.append("{");
        json.append("\"success\":").append(success);
        json.append(",\"message\":\"").append(escapeJson(message)).append("\"");
        if (data != null) {
            json.append(",\"data\":").append(toJsonValue(data));
        }
        json.append("}");

        PrintWriter out = response.getWriter();
        out.write(json.toString());
    }

    public static void write(HttpServletResponse response,
                             int status,
                             boolean success,
                             String message,
                             Map<String, Object> data) throws IOException {
        writeJsonResponse(response, status, success, message, data);
    }

    public static Map<String, Object> objectMap(Object... keyValues) {
        java.util.LinkedHashMap<String, Object> data = new java.util.LinkedHashMap<>();
        if (keyValues == null) {
            return data;
        }
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("objectMap() requires an even number of arguments");
        }
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = keyValues[i + 1];
            data.put(key, value);
        }
        return data;
    }

    public static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @SuppressWarnings("unchecked")
    public static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> mapValue) {
            if (mapValue.size() == 1 && mapValue.containsKey("__RAW_OBJECT__")) {
                return "{" + String.valueOf(mapValue.get("__RAW_OBJECT__")) + "}";
            }
            StringBuilder json = new StringBuilder();
            json.append("{");
            int index = 0;
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                if (index > 0) {
                    json.append(",");
                }
                json.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                json.append(toJsonValue(entry.getValue()));
                index++;
            }
            json.append("}");
            return json.toString();
        }
        if (value instanceof List<?> listValue) {
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < listValue.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append(toJsonValue(listValue.get(i)));
            }
            json.append("]");
            return json.toString();
        }
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append(toJsonValue(array[i]));
            }
            json.append("]");
            return json.toString();
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }
}

package com.example.tarecruitment.common.web;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Unified API response outlet.
 *
 * Servlet layer should prioritize using this class to output `{ success, message, data }`,
 * to avoid inconsistent field names, status codes or Chinese messages in different page interfaces.
 * Business service does not depend on this class; service only returns domain results or ServiceResult.
 */
public final class ApiResponses {

    private ApiResponses() {
    }

    /** 200 OK, query or normal update successful. */
    public static void ok(HttpServletResponse response, String message, Map<String, Object> data) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_OK, true, message, data);
    }

    /** 201 Created, resource created successfully. */
    public static void created(HttpServletResponse response, String message, Map<String, Object> data) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_CREATED, true, message, data);
    }

    /** 400 Bad Request, parameter or form validation failed. */
    public static void badRequest(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_BAD_REQUEST, false, message, null);
    }

    /** 401 Unauthorized, not logged in or session expired. */
    public static void unauthorized(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_UNAUTHORIZED, false, message, null);
    }

    /** 403 Forbidden, logged in but role or resource ownership not allowed. */
    public static void forbidden(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_FORBIDDEN, false, message, null);
    }

    /** 404 Not Found, resource does not exist. */
    public static void notFound(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_NOT_FOUND, false, message, null);
    }

    /** 405 Method Not Allowed, default method error message. */
    public static void methodNotAllowed(HttpServletResponse response) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, false, "Method not allowed", null);
    }

    /** 405 Method Not Allowed, custom method error message. */
    public static void methodNotAllowed(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, false, message, null);
    }

    /** 503 Service Unavailable, external AI and other dependencies temporarily unavailable. */
    public static void serviceUnavailable(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, false, message, null);
    }

    /** 500 Internal Server Error, unexpected server exception. */
    public static void serverError(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, message, null);
    }

    /**
     * General response writing, data can be Map/List/primitive type.
     */
    public static void write(HttpServletResponse response,
                             int status,
                             boolean success,
                             String message,
                             Object data) throws IOException {
        // This is the general writing method for new Servlets, data can be Map/List/primitive type.
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        StringBuilder json = new StringBuilder(256);
        json.append("{");
        json.append("\"success\":").append(success);
        json.append(",\"message\":\"").append(JsonResponseUtil.escapeJson(message)).append("\"");
        if (data != null) {
            json.append(",\"data\":").append(JsonResponseUtil.toJsonValue(data));
        }
        json.append("}");

        PrintWriter out = response.getWriter();
        out.write(json.toString());
    }

    /**
     * Convenient construct LinkedHashMap to keep response field order stable.
     */
    public static Map<String, Object> objectMap(Object... keyValues) {
        return JsonResponseUtil.objectMap(keyValues);
    }

    /**
     * Expose underlying JSON serialization capability for a small number of mappers to use.
     */
    public static String toJsonValue(Object value) {
        return JsonResponseUtil.toJsonValue(value);
    }
}

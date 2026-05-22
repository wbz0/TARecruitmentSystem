package com.example.tarecruitment.common.service;

/**
 * Lightweight result object returned from service layer to Servlet.
 *
 * It expresses the status code, success flag, message, and data ultimately needed by the HTTP endpoint,
 * but does not depend on common.web, keeping the service layer from directly writing response.
 */
public final class ServiceResult {

    private final int statusCode;
    private final boolean success;
    private final String message;
    private final Object data;

    private ServiceResult(int statusCode, boolean success, String message, Object data) {
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public static ServiceResult ok(String message, Object data) {
        return new ServiceResult(200, true, message, data);
    }

    public static ServiceResult of(int statusCode, boolean success, String message, Object data) {
        return new ServiceResult(statusCode, success, message, data);
    }

    public static ServiceResult created(String message, Object data) {
        return new ServiceResult(201, true, message, data);
    }

    public static ServiceResult badRequest(String message) {
        return new ServiceResult(400, false, message, null);
    }

    public static ServiceResult unauthorized(String message) {
        return new ServiceResult(401, false, message, null);
    }

    public static ServiceResult forbidden(String message) {
        return new ServiceResult(403, false, message, null);
    }

    public static ServiceResult notFound(String message) {
        return new ServiceResult(404, false, message, null);
    }

    public static ServiceResult methodNotAllowed(String message) {
        return new ServiceResult(405, false, message, null);
    }

    public static ServiceResult serverError(String message) {
        return new ServiceResult(500, false, message, null);
    }
}

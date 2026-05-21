package com.example.tarecruitment.common.web;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 统一的 API 响应出口。
 *
 * Servlet 层应该优先使用这个类输出 `{ success, message, data }`，
 * 避免各页面接口自己拼 JSON，导致字段名、状态码或中文提示不一致。
 * 业务 service 不依赖这个类；service 只返回领域结果或 ServiceResult。
 */
public final class ApiResponses {

    private ApiResponses() {
    }

    /** 200 OK，查询或普通更新成功。 */
    public static void ok(HttpServletResponse response, String message, Map<String, Object> data) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_OK, true, message, data);
    }

    /** 201 Created，创建资源成功。 */
    public static void created(HttpServletResponse response, String message, Map<String, Object> data) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_CREATED, true, message, data);
    }

    /** 400 Bad Request，参数或表单校验失败。 */
    public static void badRequest(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_BAD_REQUEST, false, message, null);
    }

    /** 401 Unauthorized，未登录或 session 失效。 */
    public static void unauthorized(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_UNAUTHORIZED, false, message, null);
    }

    /** 403 Forbidden，已登录但角色或资源归属不允许。 */
    public static void forbidden(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_FORBIDDEN, false, message, null);
    }

    /** 404 Not Found，资源不存在。 */
    public static void notFound(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_NOT_FOUND, false, message, null);
    }

    /** 405 Method Not Allowed，默认方法错误文案。 */
    public static void methodNotAllowed(HttpServletResponse response) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, false, "Method not allowed", null);
    }

    /** 405 Method Not Allowed，自定义方法错误文案。 */
    public static void methodNotAllowed(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, false, message, null);
    }

    /** 503 Service Unavailable，外部 AI 等依赖暂不可用。 */
    public static void serviceUnavailable(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, false, message, null);
    }

    /** 500 Internal Server Error，未预期服务端异常。 */
    public static void serverError(HttpServletResponse response, String message) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, message, null);
    }

    /**
     * 通用响应写法，data 可以是 Map/List/基础类型。
     */
    public static void write(HttpServletResponse response,
                             int status,
                             boolean success,
                             String message,
                             Object data) throws IOException {
        // 这是给新 Servlet 使用的通用写法，data 可以是 Map/List/基础类型。
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
     * 便捷构造 LinkedHashMap，保持响应字段顺序稳定。
     */
    public static Map<String, Object> objectMap(Object... keyValues) {
        return JsonResponseUtil.objectMap(keyValues);
    }

    /**
     * 暴露底层 JSON 序列化能力给少量 mapper 使用。
     */
    public static String toJsonValue(Object value) {
        return JsonResponseUtil.toJsonValue(value);
    }
}

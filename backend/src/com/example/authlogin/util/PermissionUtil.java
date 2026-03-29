package com.example.authlogin.util;

import com.example.authlogin.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * PermissionUtil - 权限检查工具类
 * 提供细粒度的权限检查方法
 */
public class PermissionUtil {

    /**
     * 检查用户是否为资源所有者
     * @param request HTTP请求对象
     * @param ownerId 资源所有者ID
     * @return true表示是所有者或是管理员
     */
    public static boolean isOwnerOrAdmin(HttpServletRequest request, String ownerId) {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            return false;
        }
        // ADMIN可以操作所有资源
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        // 检查是否为资源所有者
        String currentUserId = user.getUserId();
        return currentUserId != null && currentUserId.equals(ownerId);
    }

    /**
     * 检查用户是否可以访问指定资源
     * @param request HTTP请求对象
     * @param resourceOwnerId 资源所有者ID
     * @return true表示可以访问
     */
    public static boolean canAccessResource(HttpServletRequest request, String resourceOwnerId) {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            return false;
        }
        // ADMIN和MO可以访问所有资源
        if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.MO) {
            return true;
        }
        // TA只能访问自己的资源
        return user.getUserId().equals(resourceOwnerId);
    }

    /**
     * 验证AJAX请求的权限，如果无权访问则返回错误响应
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param resourceOwnerId 资源所有者ID
     * @return true表示有权限，false表示无权限（已发送错误响应）
     */
    public static boolean validateOwnerAccess(HttpServletRequest request,
                                               HttpServletResponse response,
                                               String resourceOwnerId) throws IOException {
        if (!canAccessResource(request, resourceOwnerId)) {
            if (isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Access denied\", \"message\": \"You don't have permission to access this resource\"}");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            }
            return false;
        }
        return true;
    }

    /**
     * 验证用户角色权限，如果无权访问则返回错误响应
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param requiredRoles 需要的角色数组
     * @return true表示有权限，false表示无权限（已发送错误响应）
     */
    public static boolean validateRoleAccess(HttpServletRequest request,
                                              HttpServletResponse response,
                                              User.Role... requiredRoles) throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            if (isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Please login first\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
            return false;
        }

        for (User.Role role : requiredRoles) {
            if (user.getRole() == role) {
                return true;
            }
        }

        // 无权访问
        if (isAjaxRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"You don't have permission to perform this action\"}");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        }
        return false;
    }

    /**
     * 检查是否为AJAX请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    /**
     * 返回未授权的JSON错误响应
     */
    public static void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }

    /**
     * 返回禁止访问的JSON错误响应
     */
    public static void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"" + message + "\"}");
    }

    /**
     * 返回成功响应的JSON
     */
    public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"success\": true, \"message\": \"" + message + "\"}");
    }

    /**
     * 返回错误响应的JSON
     */
    public static void sendError(HttpServletResponse response, String error, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": \"" + error + "\", \"message\": \"" + message + "\"}");
    }
}

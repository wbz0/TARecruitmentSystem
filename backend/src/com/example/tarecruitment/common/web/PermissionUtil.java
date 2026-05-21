package com.example.tarecruitment.common.web;

import com.example.tarecruitment.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * PermissionUtil - Permission check utility class.
 *
 * Mainly serves quick role/resource validation in old Servlets. New interfaces prefer putting route access rules in
 * AccessPolicy and each domain Servlet, keeping permission logic closer to specific APIs.
 */
public class PermissionUtil {

    /**
     * Check if user is resource owner
     * @param request HTTP request object
     * @param ownerId resource owner ID
     * @return true if owner or admin
     */
    public static boolean isOwnerOrAdmin(HttpServletRequest request, String ownerId) {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            return false;
        }
        // ADMIN can operate all resources
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        // Check if resource owner
        String currentUserId = user.getUserId();
        return currentUserId != null && currentUserId.equals(ownerId);
    }

    /**
     * Check if user can access specified resource
     * @param request HTTP request object
     * @param resourceOwnerId resource owner ID
     * @return true if accessible
     */
    public static boolean canAccessResource(HttpServletRequest request, String resourceOwnerId) {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            return false;
        }
        // ADMIN and MO can access all resources
        if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.MO) {
            return true;
        }
        // TA can only access own resources
        return user.getUserId().equals(resourceOwnerId);
    }

    /**
     * Validate AJAX request permissions, return error response if access denied
     * @param request HTTP request object
     * @param response HTTP response object
     * @param resourceOwnerId resource owner ID
     * @return true if authorized, false if unauthorized (error response already sent)
     */
    public static boolean validateOwnerAccess(HttpServletRequest request,
                                               HttpServletResponse response,
                                               String resourceOwnerId) throws IOException {
        // Legacy/pending removal: this method directly writes response. New Servlets should prefer ApiResponses for explicit returns.
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
     * Validate user role permissions, return error response if access denied
     * @param request HTTP request object
     * @param response HTTP response object
     * @param requiredRoles required role array
     * @return true if authorized, false if unauthorized (error response already sent)
     */
    public static boolean validateRoleAccess(HttpServletRequest request,
                                              HttpServletResponse response,
                                              User.Role... requiredRoles) throws IOException {
        // Legacy/pending removal: this method mixes permission check and response output. New interfaces should separate check and output.
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

        // No permission
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
     * Check if AJAX request
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return WebRequests.isAjax(request);
    }

    /**
     * Return unauthorized JSON error response
     */
    public static void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        ApiResponses.unauthorized(response, message);
    }

    /**
     * Return forbidden JSON error response
     */
    public static void sendForbidden(HttpServletResponse response, String message) throws IOException {
        ApiResponses.forbidden(response, message);
    }

    /**
     * Return success response JSON
     */
    public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
        ApiResponses.ok(response, message, null);
    }

    /**
     * Return error response JSON
     */
    public static void sendError(HttpServletResponse response, String error, String message) throws IOException {
        ApiResponses.write(response, HttpServletResponse.SC_BAD_REQUEST, false, message, ApiResponses.objectMap("error", error));
    }
}

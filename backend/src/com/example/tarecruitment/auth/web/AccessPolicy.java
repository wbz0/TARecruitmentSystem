package com.example.tarecruitment.auth.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;

import java.util.Locale;

/**
 * Unified access policy table.
 *
 * AuthFilter only calls here after getting path/method/user, to avoid scattered hardcoded paths in Servlets or JSP.
 * When adding new API routes, should simultaneously check role rules here.
 */
final class AccessPolicy {

    private AccessPolicy() {
    }

    /**
     * Static assets do not enter login/role validation.
     *
     * This covers css/js/image/font paths referenced by JSP pages to avoid losing page styles when not logged in.
     */
    static boolean isStaticAsset(String path) {
        if (path == null) {
            return false;
        }
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.endsWith(".css")
                || lower.endsWith(".js")
                || lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".ico")
                || lower.endsWith(".woff")
                || lower.endsWith(".woff2")
                || lower.endsWith(".ttf")
                || lower.endsWith(".svg")
                || path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    /**
     * Determine pages and APIs that can be accessed without login.
     *
     * Public scope only includes homepage, login/register, admin invitation registration and job browsing;
     * other /api/... require session by default.
     */
    static boolean isPublic(String method, String path) {
        // Public pages and public APIs only include login, register, availability check and job browsing.
        // Admin registration entry can be opened, but actually creating ADMIN still requires invite code validation.
        String verb = normalizeMethod(method);
        if ("/".equals(path)
                || "/index.jsp".equals(path)
                || "/login.jsp".equals(path)
                || "/register.jsp".equals(path)
                || "/admin-invite.jsp".equals(path)) {
            return true;
        }
        if ("POST".equals(verb)
                && (ApiRoutes.AUTH_LOGIN.equals(path)
                || ApiRoutes.AUTH_REGISTER.equals(path)
                || ApiRoutes.AUTH_LOGOUT.equals(path)
                || ApiRoutes.ADMIN_INVITATION_ACCEPTANCE.equals(path))) {
            return true;
        }
        if ("GET".equals(verb)
                && (ApiRoutes.AUTH_AVAILABILITY.equals(path)
                || ApiRoutes.JOBS.equals(path)
                || path.startsWith(ApiRoutes.JOBS + "/"))) {
            return true;
        }
        return false;
    }

    /**
     * Determine logged-in user's role permission.
     *
     * This only does route-level access control; specific business ownership validation is still done in each service,
     * for example MO can only modify jobs they posted.
     */
    static boolean canAccess(String method, String path, User.Role role) {
        if (role == null) {
            return false;
        }
        if (role == User.Role.ADMIN) {
            return true;
        }
        if (path.startsWith("/jsp/admin/") || path.startsWith("/api/admin/")) {
            return false;
        }
        if (role == User.Role.MO) {
            // MO can enter TA page to view job/application perspective, but admin page and admin API are prohibited.
            return path.startsWith("/jsp/mo/")
                    || path.startsWith("/jsp/ta/")
                    || path.startsWith("/api/mo/")
                    || path.startsWith("/api/ta/")
                    || path.startsWith(ApiRoutes.APPLICATIONS)
                    || path.startsWith(ApiRoutes.ME_ACCOUNT)
                    || path.startsWith(ApiRoutes.ME_AVATAR)
                    || path.startsWith(ApiRoutes.NOTIFICATIONS)
                    || isMoJobWrite(method, path);
        }
        if (role == User.Role.TA) {
            return path.startsWith("/jsp/ta/")
                    || path.startsWith("/api/ta/")
                    || path.startsWith(ApiRoutes.APPLICATIONS)
                    || path.startsWith("/api/me/")
                    || path.startsWith(ApiRoutes.NOTIFICATIONS);
        }
        return false;
    }

    /**
     * Job write operations only allowed for MO; Job GET is still handled by public browsing rules.
     */
    private static boolean isMoJobWrite(String method, String path) {
        // Job list GET is public browsing; create/modify/delete job only allowed for MO.
        String verb = normalizeMethod(method);
        return path.startsWith(ApiRoutes.JOBS)
                && ("POST".equals(verb) || "PUT".equals(verb) || "DELETE".equals(verb));
    }

    /**
     * HTTP method normalization to avoid case differences affecting policy judgment.
     */
    private static String normalizeMethod(String method) {
        return method == null ? "" : method.toUpperCase(Locale.ROOT);
    }
}

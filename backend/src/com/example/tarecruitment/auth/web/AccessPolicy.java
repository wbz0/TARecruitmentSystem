package com.example.tarecruitment.auth.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;

import java.util.Locale;

/**
 * 统一的访问策略表。
 *
 * AuthFilter 只负责拿到 path/method/user 后调用这里，避免各 Servlet 或 JSP
 * 维护散落的硬编码路径。新增 API 路由时应同步检查这里的角色规则。
 */
final class AccessPolicy {

    private AccessPolicy() {
    }

    /**
     * 静态资源不进入登录/角色校验。
     *
     * 这里覆盖 JSP 页面引用的 css/js/image/font 路径，避免未登录时页面样式丢失。
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
     * 判断无需登录即可访问的页面和 API。
     *
     * 公开范围只包含首页、登录注册、管理员邀请注册和职位浏览；
     * 其它 /api/... 默认都需要 session。
     */
    static boolean isPublic(String method, String path) {
        // 公开页面和公开 API 只包含登录、注册、可用性检查和职位浏览。
        // 管理员注册入口可打开，但真正创建 ADMIN 仍需要邀请码接口校验。
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
     * 判断已登录用户的角色权限。
     *
     * 这里只做路由级访问控制；具体业务归属校验仍在各 service 中完成，
     * 例如 MO 只能修改自己发布的职位。
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
            // MO 可以进入 TA 页面查看职位/申请视角，但 admin 页面和 admin API 禁止访问。
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
     * 职位写操作只允许 MO；职位 GET 仍由公开浏览规则处理。
     */
    private static boolean isMoJobWrite(String method, String path) {
        // 职位列表 GET 是公开浏览；创建/修改/删除职位只允许 MO。
        String verb = normalizeMethod(method);
        return path.startsWith(ApiRoutes.JOBS)
                && ("POST".equals(verb) || "PUT".equals(verb) || "DELETE".equals(verb));
    }

    /**
     * HTTP method 归一化，避免大小写差异影响策略判断。
     */
    private static String normalizeMethod(String method) {
        return method == null ? "" : method.toUpperCase(Locale.ROOT);
    }
}

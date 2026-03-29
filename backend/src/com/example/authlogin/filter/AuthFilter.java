package com.example.authlogin.filter;

import com.example.authlogin.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * AuthFilter - 权限验证过滤器
 * 用于保护需要登录才能访问的资源
 *
 * 过滤规则：
 * - /jsp/* - 需要登录
 * - /api/* - 需要登录
 * - /profile/* - 需要登录
 * - /job/* - 需要登录
 * - /application/* - 需要登录
 *
 * 公开路径（不需要登录）：
 * - /login - 登录页
 * - /register - 注册页
 * - /logout - 登出
 * - /HelloServlet - 测试用
 * - /static/* - 静态资源
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    // 公开路径，不需要登录验证
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
        "/",
        "/login",
        "/register",
        "/logout",
        "/HelloServlet",
        "/index.jsp",
        "/login.jsp",
        "/register.jsp"
    ));

    // 需要特定角色的路径
    private static final Set<String> MO_PATHS = new HashSet<>(Arrays.asList(
        "/jsp/mo/",
        "/api/mo/",
        "/job/create",
        "/job/delete",
        "/job/update"
    ));

    private static final Set<String> ADMIN_PATHS = new HashSet<>(Arrays.asList(
        "/jsp/admin/",
        "/api/admin/"
    ));

    // TA可访问的路径（除MO和ADMIN专属路径外）
    private static final Set<String> TA_PATHS = new HashSet<>(Arrays.asList(
        "/jsp/ta/",
        "/api/ta/",
        "/profile/",
        "/application/",
        "/job/list",
        "/job/view"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[AuthFilter] Initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());

        // 获取请求方法
        String method = httpRequest.getMethod();

        // 处理静态资源和CSS/JS
        if (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") ||
            path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif") ||
            path.endsWith(".ico") || path.endsWith(".woff") || path.endsWith(".woff2") ||
            path.endsWith(".ttf") || path.endsWith(".svg")) {
            chain.doFilter(request, response);
            return;
        }

        // 公开路径直接放行
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 获取session（不创建）
        HttpSession session = httpRequest.getSession(false);

        // 检查用户是否已登录
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
        }

        // 需要登录的路径
        if (user == null) {
            // AJAX请求返回JSON
            if (isAjaxRequest(httpRequest)) {
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Please login first\"}");
                return;
            }

            // 普通请求重定向到登录页
            httpResponse.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        // 验证角色权限
        if (!hasPermission(path, user.getRole())) {
            if (isAjaxRequest(httpRequest)) {
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"You don't have permission to access this resource\"}");
                return;
            }

            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        // 放行请求
        chain.doFilter(request, response);
    }

    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String path) {
        // 精确匹配
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        // 前缀匹配（静态资源目录）
        if (path.startsWith("/static/") || path.startsWith("/css/") ||
            path.startsWith("/js/") || path.startsWith("/images/")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为AJAX请求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    /**
     * 验证用户角色权限
     */
    private boolean hasPermission(String path, User.Role role) {
        // ADMIN可以访问所有路径
        if (role == User.Role.ADMIN) {
            return true;
        }

        // MO可以访问MO路径和TA路径
        if (role == User.Role.MO) {
            if (isPathMatch(path, MO_PATHS) || isPathMatch(path, TA_PATHS)) {
                return true;
            }
            // MO也可以访问自己的管理页面
            if (path.startsWith("/jsp/mo/") || path.startsWith("/api/mo/")) {
                return true;
            }
            return false;
        }

        // TA只能访问TA路径
        if (role == User.Role.TA) {
            if (isPathMatch(path, TA_PATHS)) {
                return true;
            }
            // TA可以访问自己的profile
            if (path.startsWith("/profile/") || path.startsWith("/application/")) {
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * 路径匹配检查
     */
    private boolean isPathMatch(String path, Set<String> patterns) {
        for (String pattern : patterns) {
            if (path.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        System.out.println("[AuthFilter] Destroyed");
    }
}

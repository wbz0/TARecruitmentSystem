package com.example.tarecruitment.auth.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.common.web.WebRequests;
import com.example.tarecruitment.common.util.Logger;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AuthFilter - Permission validation filter
 * Used to protect resources that require login to access
 *
 * Permission rules are centralized in AccessPolicy to avoid scattered modifications when Servlets and frontend routes migrate.
 * Returns JSON for AJAX requests, redirects or returns 403 for normal page requests.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Logger.i("AuthFilter", "Initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());

        if (AccessPolicy.isStaticAsset(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (AccessPolicy.isPublic(httpRequest.getMethod(), path)) {
            chain.doFilter(request, response);
            return;
        }

        // Get session (do not create), should not create new empty session when accessing protected pages without login.
        HttpSession session = httpRequest.getSession(false);

        // Check if user is logged in
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
        }

        // Paths requiring login: page requests redirect to login page, frontend fetch requests get 401 and handle themselves.
        if (user == null) {
            // AJAX request returns JSON
            if (WebRequests.isAjax(httpRequest)) {
                ApiResponses.unauthorized(httpResponse, "Please login first");
                return;
            }

            // Normal request redirects to login page
            httpResponse.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        // Verify role permission: specific path/method rules are centralized in AccessPolicy.
        if (!AccessPolicy.canAccess(httpRequest.getMethod(), path, user.getRole())) {
            if (WebRequests.isAjax(httpRequest)) {
                ApiResponses.forbidden(httpResponse, "You don't have permission to access this resource");
                return;
            }

            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        // Allow request to pass through
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Logger.i("AuthFilter", "Destroyed");
    }
}

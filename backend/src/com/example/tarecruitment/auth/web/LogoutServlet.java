package com.example.tarecruitment.auth.web;

import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.common.web.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LogoutServlet - Handle user logout.
 *
 * Current frontend entry: shared sidebar logout button, called via /api/auth/logout.
 * Does not restore the old root path /logout.
 * Access path: /api/auth/logout
 */
@WebServlet(ApiRoutes.AUTH_LOGOUT)
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get current username for logging
        String username = SessionUtil.getCurrentUsername(request);

        // Destroy session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Check if AJAX request: page fetch expects JSON, direct access redirects to login page.
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        if (isAjax) {
            // AJAX request returns JSON
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            response.getWriter().write("{\"success\": true, \"message\": \"Logout successful\"}");
        } else {
            // Regular request redirects to login page
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }

        Logger.i("LogoutServlet", "User logged out: " + username);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

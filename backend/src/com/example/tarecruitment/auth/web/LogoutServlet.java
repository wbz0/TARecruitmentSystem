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
 * LogoutServlet - 处理用户退出登录。
 *
 * 当前前端入口：共享侧边栏退出按钮，通过 /api/auth/logout 调用。
 * 不恢复旧根路径 /logout。
 * 访问路径: /api/auth/logout
 */
@WebServlet(ApiRoutes.AUTH_LOGOUT)
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取当前用户名用于日志
        String username = SessionUtil.getCurrentUsername(request);

        // 销毁会话
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 判断是否为 AJAX 请求：页面 fetch 要 JSON，直接访问则回登录页。
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        if (isAjax) {
            // AJAX请求返回JSON
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            response.getWriter().write("{\"success\": true, \"message\": \"Logout successful\"}");
        } else {
            // 普通请求跳转到登录页
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

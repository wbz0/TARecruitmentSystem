package com.example.authlogin;

import com.example.authlogin.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LogoutServlet - 处理用户退出登录
 * 访问路径: /logout
 */
@WebServlet("/logout")
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

        // 判断是否为AJAX请求
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

        System.out.println("[LogoutServlet] User logged out: " + username);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

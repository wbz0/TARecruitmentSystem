package com.example.authlogin;

import com.example.authlogin.dao.UserDao;
import com.example.authlogin.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * LoginServlet - 处理用户登录
 * 访问路径: /login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\": \"Use POST to login\", \"example\": {\"username\": \"test\", \"password\": \"123456\"}}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 输入验证
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\": \"Username and password are required\"}");
            return;
        }

        // 验证登录
        Optional<User> userOpt = userDao.verifyLogin(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 创建会话
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole().name());
            session.setMaxInactiveInterval(30 * 60); // 30分钟超时

            // 返回JSON响应
            response.setStatus(200);
            response.getWriter().write("{\"success\": true, \"message\": \"Login successful\", \"username\": \"" + user.getUsername() + "\", \"role\": \"" + user.getRole().name() + "\"}");
        } else {
            response.setStatus(401);
            response.getWriter().write("{\"error\": \"Invalid username or password\"}");
        }
    }

    /**
     * 根据用户角色确定跳转页面
     */
    private String determineRedirectPage(User.Role role) {
        switch (role) {
            case TA:
                return "/index.jsp";
            case MO:
                return "/jsp/mo/dashboard.jsp";
            case ADMIN:
                return "/jsp/admin/dashboard.jsp";
            default:
                return "/index.jsp";
        }
    }
}

package com.example.authlogin;

import com.example.authlogin.dao.UserDao;
import com.example.authlogin.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * RegisterServlet - 处理用户注册
 * 访问路径: /register
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserDao userDao;

    // 邮箱验证正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // 用户名验证正则 (字母开头，允许字母数字下划线，3-20字符)
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z][a-zA-Z0-9_]{2,19}$"
    );

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\": \"Use POST to register\", \"example\": {\"username\": \"test\", \"password\": \"123456\", \"email\": \"test@example.com\", \"role\": \"TA\"}}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String email = request.getParameter("email");
        String roleStr = request.getParameter("role");

        // 输入验证
        String error = validateInput(username, password, confirmPassword, email, roleStr);
        if (error != null) {
            response.setStatus(400);
            response.getWriter().write("{\"error\": \"" + error + "\"}");
            return;
        }

        try {
            // 解析角色
            User.Role role;
            try {
                role = User.Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.setStatus(400);
                response.getWriter().write("{\"error\": \"Invalid role selected\"}");
                return;
            }

            // 创建用户
            User user = new User(username, password, email, role);
            User savedUser = userDao.create(user);

            // 注册成功
            response.setStatus(201);
            response.getWriter().write("{\"success\": true, \"message\": \"Registration successful!\", \"userId\": \"" + savedUser.getUserId() + "\"}");

        } catch (IllegalArgumentException e) {
            // 用户名或邮箱已存在
            response.setStatus(409);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 验证输入
     * @return 错误信息，如果验证通过返回null
     */
    private String validateInput(String username, String password,
                                  String confirmPassword, String email, String role) {
        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "Username must be 3-20 characters, start with a letter, and contain only letters, numbers, and underscores";
        }

        // 验证密码
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }

        // 验证确认密码
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }

        // 验证邮箱
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email format";
        }

        // 验证角色
        if (role == null || role.trim().isEmpty()) {
            return "Please select a role";
        }

        return null;
    }
}

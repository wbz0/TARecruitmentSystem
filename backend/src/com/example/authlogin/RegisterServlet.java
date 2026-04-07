package com.example.authlogin;

import com.example.authlogin.dao.UserDao;
import com.example.authlogin.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * RegisterServlet - 处理用户注册
 * 访问路径: /register
 *
 * 优化内容:
 * - 添加日志记录
 * - 增强输入验证
 * - 统一JSON响应格式
 * - 添加异常处理
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

    // 简单的日志方法
    private void logInfo(String message) {
        System.out.println("[RegisterServlet] " + message);
    }

    private void logError(String message, Throwable t) {
        System.err.println("[RegisterServlet ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
        logInfo("RegisterServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        writeJsonResponse(response, 200, true, "Use POST to register", null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String email = request.getParameter("email");
            String roleStr = request.getParameter("role");

            // 输入验证
            String error = validateInput(username, password, confirmPassword, email, roleStr);
            if (error != null) {
                logInfo("Validation failed: " + error);
                writeJsonResponse(response, 400, false, error, null);
                return;
            }

            // 去除输入首尾空格
            username = username.trim();
            email = email.trim();
            roleStr = roleStr.trim();

            // 解析角色
            User.Role role;
            try {
                role = User.Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logInfo("Invalid role: " + roleStr);
                writeJsonResponse(response, 400, false, "Invalid role selected", null);
                return;
            }

            // 创建用户
            logInfo("Attempting to create user: " + username);
            User user = new User(username, password, email, role);
            User savedUser = userDao.create(user);

            logInfo("User registered successfully: " + username + ", role: " + role);

            // 注册成功
            writeJsonResponse(response, 201, true, "Registration successful!",
                "{\"userId\": \"" + savedUser.getUserId() + "\", \"username\": \"" + savedUser.getUsername() + "\"}");

        } catch (IllegalArgumentException e) {
            // 用户名或邮箱已存在
            logInfo("Registration failed: " + e.getMessage());
            writeJsonResponse(response, 409, false, e.getMessage(), null);
        } catch (Exception e) {
            logError("Unexpected error during registration", e);
            writeJsonResponse(response, 500, false, "An error occurred during registration. Please try again later.", null);
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
        if (password.length() > 100) {
            return "Password is too long";
        }

        // 验证确认密码
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Please confirm your password";
        }
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
        if (email.length() > 100) {
            return "Email is too long";
        }

        // 验证角色
        if (role == null || role.trim().isEmpty()) {
            return "Please select a role";
        }

        return null;
    }

    /**
     * 统一的JSON响应写入方法
     */
    private void writeJsonResponse(HttpServletResponse response, int status, boolean success, String message, String data)
            throws IOException {
        response.setStatus(status);
        PrintWriter out = response.getWriter();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(success).append(", ");
        json.append("\"message\": \"").append(escapeJson(message)).append("\"");

        if (data != null) {
            json.append(", ").append(data);
        }

        json.append("}");
        out.write(json.toString());
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

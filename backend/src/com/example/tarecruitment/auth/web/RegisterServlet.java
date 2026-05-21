package com.example.tarecruitment.auth.web;

import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.common.util.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * RegisterServlet - 处理公开用户注册。
 *
 * 当前前端入口：register.jsp / js/auth/register.js。
 * 访问路径: /api/auth/register
 *
 * 公开注册只允许 TA/MO。Admin 注册必须走邀请页和邀请码验收流程。
 */
@WebServlet(ApiRoutes.AUTH_REGISTER)
public class RegisterServlet extends HttpServlet {

    private UserDao userDao;

    // 邮箱验证正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // 用户名验证正则 (字母开头，允许字母数字下划线，3-20字符)
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z][a-zA-Z0-9_]{2,19}$"
    );
    private static final int USERNAME_MAX_LENGTH = 20;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 100;

    // 简单的日志方法
    private void logInfo(String message) {
        Logger.i("RegisterServlet", message);
    }

    private void logError(String message, Throwable t) {
        Logger.e("RegisterServlet", message, t);
    }

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
        logInfo("RegisterServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ApiResponses.write(response, 200, true, "Use POST to register", null);
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
                ApiResponses.write(response, 400, false, error, null);
                return;
            }

            // 去除首尾空格（密码保留原样）
            username = username.trim().toLowerCase();
            email = email.trim();
            roleStr = roleStr.trim();

            // 解析角色（公开注册仅允许 TA / MO），ADMIN 必须走 admin invite。
            User.Role role;
            try {
                role = parsePublicRole(roleStr);
            } catch (IllegalArgumentException e) {
                logInfo("Invalid role: " + roleStr);
                ApiResponses.write(response, 400, false, e.getMessage(), null);
                return;
            }

            // 创建用户
            logInfo("Attempting to create user: " + username);
            User user = new User(username, password, email, role);
            User savedUser = userDao.create(user);

            logInfo("User registered successfully: " + username + ", role: " + role);

            // 注册成功
            ApiResponses.write(
                    response,
                    201,
                    true,
                    "Registration successful!",
                    ApiResponses.objectMap(
                            "userId", savedUser.getUserId(),
                            "username", savedUser.getUsername()
                    )
            );

        } catch (IllegalArgumentException e) {
            // 用户名或邮箱已存在
            logInfo("Registration failed: " + e.getMessage());
            ApiResponses.write(response, 409, false, e.getMessage(), null);
        } catch (Exception e) {
            logError("Unexpected error during registration", e);
            ApiResponses.write(response, 500, false, "An error occurred during registration. Please try again later.", null);
        }
    }

    /**
     * 验证输入
     * @return 错误信息，如果验证通过返回null
     */
    private String validateInput(String username, String password,
                                  String confirmPassword, String email, String role) {
        String usernameText = username != null ? username.trim().toLowerCase() : "";
        String emailText = email != null ? email.trim() : "";
        String passwordText = password != null ? password : "";           // 密码不 trim，避免悄悄改变用户输入。
        String confirmPasswordText = confirmPassword != null ? confirmPassword : ""; // 确认密码同样不 trim。
        String roleText = role != null ? role.trim().toUpperCase() : "";

        // 验证用户名
        if (usernameText.isEmpty()) {
            return "Username is required";
        }
        if (usernameText.length() > USERNAME_MAX_LENGTH) {
            return "Username is too long";
        }
        if (hasControlChars(username) || containsDangerousMarkup(username)) {
            return "Username contains unsupported characters";
        }
        if (!USERNAME_PATTERN.matcher(usernameText).matches()) {
            return "Username must be 3-20 characters, start with a letter, and contain only letters, numbers, and underscores";
        }
        if (usernameText.contains("__")) {
            return "Username cannot contain consecutive underscores";
        }
        if (usernameText.charAt(usernameText.length() - 1) == '_') {
            return "Username cannot end with an underscore";
        }

        // 验证密码
        if (passwordText.isEmpty()) {
            return "Password is required";
        }
        if (passwordText.length() < PASSWORD_MIN_LENGTH) {
            return "Password must be at least 6 characters";
        }
        if (passwordText.length() > PASSWORD_MAX_LENGTH) {
            return "Password is too long";
        }
        if (hasControlChars(password)) {
            return "Password contains unsupported characters";
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            return "Password must contain at least one letter and one number";
        }

        // 验证确认密码
        if (confirmPasswordText.isEmpty()) {
            return "Please confirm your password";
        }
        if (!passwordText.equals(confirmPasswordText)) {
            return "Passwords do not match";
        }

        // 验证邮箱
        if (emailText.isEmpty()) {
            return "Email is required";
        }
        if (emailText.length() > EMAIL_MAX_LENGTH) {
            return "Email is too long";
        }
        if (hasControlChars(email) || containsDangerousMarkup(email)) {
            return "Email contains unsupported characters";
        }
        if (!isValidEmailAddress(emailText)) {
            return "Invalid email format";
        }

        // 验证角色
        if (roleText.isEmpty()) {
            return "Please select a role";
        }
        if ("ADMIN".equals(roleText)) {
            return "Admin registration is invitation-only";
        }
        if (!isSupportedPublicRole(roleText)) {
            return "Invalid role selected";
        }

        return null;
    }

    private boolean isValidEmailAddress(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }

        String[] parts = email.split("@", -1);
        if (parts.length != 2) {
            return false;
        }

        String local = parts[0];
        String domain = parts[1];
        if (local.isEmpty() || domain.isEmpty()) {
            return false;
        }

        if (local.startsWith(".") || local.endsWith(".") || local.contains("..")) {
            return false;
        }
        if (domain.startsWith(".") || domain.endsWith(".") || domain.contains("..")) {
            return false;
        }

        return true;
    }

    private boolean hasControlChars(String value) {
        return value != null && value.matches(".*[\\x00-\\x1F\\x7F].*");
    }

    private boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
            || text.contains("javascript:")
            || text.matches(".*on\\w+\\s*=.*");
    }

    private boolean isSupportedPublicRole(String role) {
        return "TA".equals(role) || "MO".equals(role);
    }

    private User.Role parsePublicRole(String roleText) {
        // Admin 的入口不在这里，避免公开注册接口绕过邀请码。
        if (roleText == null) {
            throw new IllegalArgumentException("Invalid role selected");
        }
        String normalized = roleText.trim().toUpperCase();
        if ("ADMIN".equals(normalized)) {
            throw new IllegalArgumentException("Admin registration is invitation-only");
        }
        if (!isSupportedPublicRole(normalized)) {
            throw new IllegalArgumentException("Invalid role selected");
        }
        return User.Role.valueOf(normalized);
    }

}

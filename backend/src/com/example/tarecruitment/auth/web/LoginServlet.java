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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * LoginServlet - 处理用户登录。
 *
 * 当前前端入口：login.jsp / js/auth/login.js。
 * 访问路径: /api/auth/login
 *
 * 该项目使用 CSV 存储，没有 SQL 查询；这里的防护重点是参数长度、格式、
 * 明显危险标记和角色匹配校验。
 */
@WebServlet(ApiRoutes.AUTH_LOGIN)
public class LoginServlet extends HttpServlet {

    private UserDao userDao;
    private static final String INVALID_ROLE = "__INVALID_ROLE__";
    private static final int LOGIN_IDENTIFIER_MAX_LENGTH = 100;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 100;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{2,19}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // 简单的日志方法
    private void logInfo(String message) {
        Logger.i("LoginServlet", message);
    }

    private void logError(String message, Throwable t) {
        Logger.e("LoginServlet", message, t);
    }

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
        logInfo("LoginServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ApiResponses.write(response, 200, true, "Use POST to login", null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取并验证输入
            String loginIdentifier = request.getParameter("username");
            String password = request.getParameter("password");
            String requestedRole = normalizeRequestedRole(request.getParameter("role"));
            boolean rememberMe = "1".equals(request.getParameter("rememberMe"));

            // 前端登录页会传入用户选择的角色，用来避免 TA/MO/Admin 误进错入口。
            if (INVALID_ROLE.equals(requestedRole)) {
                logInfo("Validation failed: Invalid role parameter");
                ApiResponses.write(response, 400, false, "Invalid role parameter", null);
                return;
            }

            // 输入验证
            String validationError = validateInput(loginIdentifier, password);
            if (validationError != null) {
                logInfo("Validation failed: " + validationError);
                ApiResponses.write(response, 400, false, validationError, null);
                return;
            }

            // 去除输入首尾空格
            loginIdentifier = loginIdentifier.trim();
            password = password.trim();

            // 验证登录
            logInfo("Attempting login for identifier: " + loginIdentifier +
                (requestedRole != null ? ", requestedRole: " + requestedRole : ""));
            Optional<User> userOpt = userDao.verifyLogin(loginIdentifier, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (requestedRole != null && !requestedRole.equals(user.getRole().name())) {
                    logInfo("Login failed for identifier: " + loginIdentifier + " - Role mismatch. accountRole="
                        + user.getRole().name() + ", requestedRole=" + requestedRole);
                    ApiResponses.write(response, 403, false, "Selected login role does not match account role", null);
                    return;
                }

                logInfo("Login successful for identifier: " + loginIdentifier + ", role: " + user.getRole());

                // 创建会话
                HttpSession session = request.getSession(true);
                // 同时保存 User 对象和基础字段：User 给后端权限判断，字段给 JSP 片段显示。
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole().name());

                if (rememberMe) {
                    int maxAge = 30 * 24 * 60 * 60; // 30 天
                    session.setMaxInactiveInterval(maxAge);
                    Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
                    sessionCookie.setMaxAge(maxAge);
                    sessionCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath() + "/");
                    sessionCookie.setHttpOnly(true);
                    response.addCookie(sessionCookie);
                } else {
                    session.setMaxInactiveInterval(30 * 60); // 30分钟超时
                }

                // 返回成功响应
                String redirectPage = determineRedirectPage(user.getRole());
                ApiResponses.write(response, 200, true, "Login successful",
                        ApiResponses.objectMap(
                                "username", user.getUsername(),
                                "role", user.getRole().name(),
                                "redirect", redirectPage
                        ));
            } else {
                logInfo("Login failed for identifier: " + loginIdentifier + " - Invalid credentials");
                ApiResponses.write(response, 401, false, "Invalid username/email or password", null);
            }
        } catch (Exception e) {
            logError("Unexpected error during login", e);
            ApiResponses.write(response, 500, false, "An error occurred during login. Please try again later.", null);
        }
    }

    /**
     * 验证用户输入
     * @return 错误信息，如果验证通过返回null
     */
    private String validateInput(String loginIdentifier, String password) {
        String identifierText = loginIdentifier != null ? loginIdentifier.trim() : "";
        String passwordText = password != null ? password.trim() : "";

        // 验证用户名或邮箱
        if (identifierText.isEmpty()) {
            return "Username or email is required";
        }
        if (identifierText.length() > LOGIN_IDENTIFIER_MAX_LENGTH) {
            return "Username or email is too long";
        }
        if (hasControlChars(loginIdentifier) || containsDangerousMarkup(loginIdentifier)) {
            return "Username or email contains unsupported characters";
        }
        if (identifierText.contains("@")) {
            if (!isValidEmailAddress(identifierText)) {
                return "Invalid email format";
            }
        } else if (!USERNAME_PATTERN.matcher(identifierText).matches()) {
            return "Invalid username format";
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

        return null;
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

    /**
     * 规范化并验证前端传入的登录角色
     * @return TA/MO/ADMIN/null/INVALID_ROLE
     */
    private String normalizeRequestedRole(String role) {
        // 空角色表示旧前端或直接 API 调用；非空但非法时明确拒绝。
        if (role == null) {
            return null;
        }

        String normalizedRole = role.trim().toUpperCase();
        if (normalizedRole.isEmpty()) {
            return null;
        }

        if ("TA".equals(normalizedRole) || "MO".equals(normalizedRole) || "ADMIN".equals(normalizedRole)) {
            return normalizedRole;
        }

        return INVALID_ROLE;
    }

    /**
     * 根据用户角色确定跳转页面
     */
    private String determineRedirectPage(User.Role role) {
        // 这里返回带 /groupproject 的历史路径，前端会按部署 context 做跳转兜底。
        switch (role) {
            case TA:
                return "/groupproject/jsp/ta/dashboard.jsp";
            case MO:
                return "/groupproject/jsp/mo/dashboard.jsp";
            case ADMIN:
                return "/groupproject/jsp/admin/dashboard.jsp";
            default:
                return "/groupproject/login.jsp";
        }
    }
}

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
import java.io.PrintWriter;
import java.util.Optional;

/**
 * LoginServlet - 处理用户登录
 * 访问路径: /login
 *
 * 优化内容:
 * - 添加日志记录
 * - 增强输入验证
 * - 统一JSON响应格式
 * - 添加异常处理
 * - 防止SQL注入（使用参数化查询）
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDao userDao;
    private static final String INVALID_ROLE = "__INVALID_ROLE__";

    // 简单的日志方法
    private void logInfo(String message) {
        System.out.println("[LoginServlet] " + message);
    }

    private void logError(String message, Throwable t) {
        System.err.println("[LoginServlet ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
        logInfo("LoginServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        writeJsonResponse(response, 200, true, "Use POST to login", null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取并验证输入
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String requestedRole = normalizeRequestedRole(request.getParameter("role"));

            if (INVALID_ROLE.equals(requestedRole)) {
                logInfo("Validation failed: Invalid role parameter");
                writeJsonResponse(response, 400, false, "Invalid role parameter", null);
                return;
            }

            // 输入验证
            String validationError = validateInput(username, password);
            if (validationError != null) {
                logInfo("Validation failed: " + validationError);
                writeJsonResponse(response, 400, false, validationError, null);
                return;
            }

            // 去除输入首尾空格
            username = username.trim();
            password = password.trim();

            // 验证登录
            logInfo("Attempting login for username: " + username +
                (requestedRole != null ? ", requestedRole: " + requestedRole : ""));
            Optional<User> userOpt = userDao.verifyLogin(username, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (requestedRole != null && !requestedRole.equals(user.getRole().name())) {
                    logInfo("Login failed for username: " + username + " - Role mismatch. accountRole="
                        + user.getRole().name() + ", requestedRole=" + requestedRole);
                    writeJsonResponse(response, 403, false, "Selected login role does not match account role", null);
                    return;
                }

                logInfo("Login successful for username: " + username + ", role: " + user.getRole());

                // 创建会话
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole().name());
                session.setMaxInactiveInterval(30 * 60); // 30分钟超时

                // 返回成功响应
                String redirectPage = determineRedirectPage(user.getRole());
                writeJsonResponse(response, 200, true, "Login successful",
                    "{\"username\": \"" + user.getUsername() + "\", \"role\": \"" + user.getRole().name() + "\", \"redirect\": \"" + redirectPage + "\"}");
            } else {
                logInfo("Login failed for username: " + username + " - Invalid credentials");
                writeJsonResponse(response, 401, false, "Invalid username or password", null);
            }
        } catch (Exception e) {
            logError("Unexpected error during login", e);
            writeJsonResponse(response, 500, false, "An error occurred during login. Please try again later.", null);
        }
    }

    /**
     * 验证用户输入
     * @return 错误信息，如果验证通过返回null
     */
    private String validateInput(String username, String password) {
        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (username.length() > 50) {
            return "Username is too long";
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

        return null;
    }

    /**
     * 规范化并验证前端传入的登录角色
     * @return TA/MO/ADMIN/null/INVALID_ROLE
     */
    private String normalizeRequestedRole(String role) {
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

    /**
     * 根据用户角色确定跳转页面
     */
    private String determineRedirectPage(User.Role role) {
        switch (role) {
            case TA:
                return "/groupproject/jsp/ta/dashboard.jsp";
            case MO:
                return "/groupproject/jsp/mo/dashboard.jsp";
            case ADMIN:
                return "/groupproject/jsp/admin/dashboard.jsp";
            default:
                return "/groupproject/index.jsp";
        }
    }
}

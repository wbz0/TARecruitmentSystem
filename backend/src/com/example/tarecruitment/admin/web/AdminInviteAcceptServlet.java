package com.example.tarecruitment.admin.web;

import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.admin.service.InviteCodeService;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * AdminInviteAcceptServlet - 接受邀请码，创建管理员账号。
 *
 * 对应 /admin-invite.jsp / js/auth/admin-invite.js。
 * 这条是当前可见管理员注册主流程：用户输入邮箱、用户名、密码和短邀请码即可创建 ADMIN。
 *
 * 邀请码由 InviteCodeService 生成的时间窗口码校验；
 * 不再依赖 CSV 存储的邀请记录。
 */
@WebServlet(ApiRoutes.ADMIN_INVITATION_ACCEPTANCE)
public class AdminInviteAcceptServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{2,19}$");
    private static final int USERNAME_MAX_LENGTH = 20;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 100;

    private InviteCodeService inviteCodeService;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        inviteCodeService = InviteCodeService.getInstance();
        userDao = UserDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 页面只会 POST 注册；GET 留作 API 提示，避免浏览器直接访问时返回容器默认错误页。
        ApiResponses.write(response, 200, true, "Use POST to accept invitation", null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String username = trimToEmpty(request.getParameter("username")).toLowerCase();
        String email = normalizeEmail(request.getParameter("email"));
        String password = emptyIfNull(request.getParameter("password"));         // no trim
        String confirmPassword = emptyIfNull(request.getParameter("confirmPassword")); // no trim
        String inviteCode = trimToEmpty(request.getParameter("inviteCode")).toUpperCase();

        String validationError = validateInput(username, email, password, confirmPassword, inviteCode);
        if (validationError != null) {
            ApiResponses.write(response, 400, false, validationError, null);
            return;
        }

        if (!inviteCodeService.isValidCode(inviteCode)) {
            ApiResponses.write(response, 403, false, "Invite code is invalid or expired", null);
            return;
        }

        try {
            User user = new User(username, password, email, User.Role.ADMIN);
            User saved = userDao.create(user);

            // 注册成功后前端跳回登录页，不在这里自动建立登录 session。
            ApiResponses.write(response, 201, true, "Admin account created successfully",
                    ApiResponses.objectMap(
                            "userId", saved.getUserId(),
                            "username", saved.getUsername(),
                            "role", saved.getRole().name(),
                            "redirect", request.getContextPath() + "/login.jsp"
                    ));
        } catch (IllegalArgumentException e) {
            ApiResponses.write(response, 409, false, e.getMessage(), null);
        } catch (Exception e) {
            ApiResponses.write(response, 500, false, "Failed to create admin account", null);
        }
    }

    private String validateInput(String username, String email,
                                 String password, String confirmPassword,
                                 String inviteCode) {
        // 这里只做注册表单安全和格式校验；邀请码是否有效交给 InviteCodeService。
        if (username.isEmpty()) return "Username is required";
        if (username.length() > USERNAME_MAX_LENGTH) return "Username is too long";
        if (hasControlChars(username) || containsDangerousMarkup(username))
            return "Username contains unsupported characters";
        if (!USERNAME_PATTERN.matcher(username).matches())
            return "Username must be 3-20 characters, start with a letter, and contain only letters, numbers, and underscores";
        if (username.contains("__")) return "Username cannot contain consecutive underscores";
        if (username.charAt(username.length() - 1) == '_') return "Username cannot end with an underscore";

        if (email.isEmpty()) return "Email is required";
        if (email.length() > EMAIL_MAX_LENGTH) return "Email is too long";
        if (hasControlChars(email) || containsDangerousMarkup(email) || !EMAIL_PATTERN.matcher(email).matches())
            return "Invalid email format";

        if (password.isEmpty()) return "Password is required";
        if (password.length() < PASSWORD_MIN_LENGTH) return "Password must be at least 8 characters";
        if (password.length() > PASSWORD_MAX_LENGTH) return "Password is too long";
        if (hasControlChars(password)) return "Password contains unsupported characters";
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*"))
            return "Password must contain at least one letter and one number";
        if (!password.equals(confirmPassword)) return "Passwords do not match";

        if (inviteCode.isEmpty()) return "Invite code is required";

        return null;
    }

    private String normalizeEmail(String value) {
        return trimToEmpty(value).toLowerCase();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private boolean hasControlChars(String value) {
        return value.matches(".*[\\x00-\\x1F\\x7F].*");
    }

    private boolean containsDangerousMarkup(String value) {
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
                || text.contains("javascript:")
                || text.matches(".*on\\w+\\s*=.*");
    }
}

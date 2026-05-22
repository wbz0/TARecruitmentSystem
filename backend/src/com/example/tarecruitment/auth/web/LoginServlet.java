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
 * LoginServlet - Handles user login.
 *
 * Current frontend entry: login.jsp / js/auth/login.js.
 * Access path: /api/auth/login
 *
 * This project uses CSV storage, no SQL queries; the focus here is parameter length, format,
 * obvious danger markers and role matching validation.
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

    // Simple logging method
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
            // Get and validate input
            String loginIdentifier = request.getParameter("username");
            String password = request.getParameter("password");
            String requestedRole = normalizeRequestedRole(request.getParameter("role"));
            boolean rememberMe = "1".equals(request.getParameter("rememberMe"));

            // Frontend login page passes user-selected role to avoid TA/MO/Admin entering wrong entry by mistake.
            if (INVALID_ROLE.equals(requestedRole)) {
                logInfo("Validation failed: Invalid role parameter");
                ApiResponses.write(response, 400, false, "Invalid role parameter", null);
                return;
            }

            // Input validation
            String validationError = validateInput(loginIdentifier, password);
            if (validationError != null) {
                logInfo("Validation failed: " + validationError);
                ApiResponses.write(response, 400, false, validationError, null);
                return;
            }

            // Trim input
            loginIdentifier = loginIdentifier.trim();
            password = password.trim();

            // Verify login
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

                // Create session
                HttpSession session = request.getSession(true);
                // Also save User object and basic fields: User for backend permission judgment, fields for JSP fragment display.
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole().name());

                if (rememberMe) {
                    int maxAge = 30 * 24 * 60 * 60; // 30 days
                    session.setMaxInactiveInterval(maxAge);
                    Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
                    sessionCookie.setMaxAge(maxAge);
                    sessionCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath() + "/");
                    sessionCookie.setHttpOnly(true);
                    response.addCookie(sessionCookie);
                } else {
                    session.setMaxInactiveInterval(30 * 60); // 30 minutes timeout
                }

                // Return success response
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
     * Validate user input
     * @return Error message, or null if validation passes
     */
    private String validateInput(String loginIdentifier, String password) {
        String identifierText = loginIdentifier != null ? loginIdentifier.trim() : "";
        String passwordText = password != null ? password.trim() : "";

        // Validate username or email
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

        // Validate password
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
     * Normalize and validate login role passed from frontend
     * @return TA/MO/ADMIN/null/INVALID_ROLE
     */
    private String normalizeRequestedRole(String role) {
        // Empty role means old frontend or direct API call; non-empty but invalid should be explicitly rejected.
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
     * Determine redirect page based on user role
     */
    private String determineRedirectPage(User.Role role) {
        // Returns path with /groupproject historical path here; frontend will do deployment context fallback.
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

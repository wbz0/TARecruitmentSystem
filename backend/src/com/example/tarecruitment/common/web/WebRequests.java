package com.example.tarecruitment.common.web;

import com.example.tarecruitment.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet request reading utility.
 *
 * Here are all small checks repeatedly used in web layer: current user, AJAX request, basic string cleanup,
 * dangerous markup check. service/dao layer should not depend on this class to avoid bringing HTTP details into business layer.
 */
public final class WebRequests {

    private WebRequests() {
    }

    /**
     * Read current logged-in user from session.
     *
     * Servlet layer uses this method to avoid repeating writing session.getAttribute("user") at each entry.
     */
    public static User currentUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }

    /**
     * Determine if frontend fetch/ajax request.
     */
    public static boolean isAjax(HttpServletRequest request) {
        return request != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * Uniformly trim request parameters, null becomes empty string.
     */
    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Control character check for basic input protection.
     */
    public static boolean containsControlChars(String value) {
        return value != null && value.chars().anyMatch(ch -> ch < 32 || ch == 127);
    }

    /**
     * Obvious HTML/JS markup check.
     *
     * This is web layer general fallback; more precise field rules are still in each domain validator.
     */
    public static boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // Only lightweight protection before entering business logic; specific field length/format still determined by each domain validator.
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
                || text.contains("javascript:")
                || text.matches(".*on\\w+\\s*=.*");
    }
}

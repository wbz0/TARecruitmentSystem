package com.example.tarecruitment.common.web;

import com.example.tarecruitment.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * SessionUtil - Session utility class.
 *
 * After login, session saves both `user` object and some basic fields.
 * New code preferentially reads `user` object; string fields are mainly for JSP fragments and legacy utility method compatibility.
 */
public class SessionUtil {

    /**
     * Get current logged-in user
     * @param request HTTP request object
     * @return user object, null if not logged in
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    /**
     * Get current logged-in user ID
     * @param request HTTP request object
     * @return user ID, null if not logged in
     */
    public static String getCurrentUserId(HttpServletRequest request) {
        // Legacy compatibility: old JSP/tools directly read userId. New Java logic preferentially reads from User object.
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute("userId");
            return userId != null ? userId.toString() : null;
        }
        return null;
    }

    /**
     * Get current logged-in username
     * @param request HTTP request object
     * @return username, null if not logged in
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        // Legacy compatibility: used for JSP page injection display name; account profile API still uses User object as source of truth.
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute("username");
            return username != null ? username.toString() : null;
        }
        return null;
    }

    /**
     * Get current logged-in user role
     * @param request HTTP request object
     * @return role name, null if not logged in
     */
    public static String getCurrentUserRole(HttpServletRequest request) {
        // Legacy compatibility: string role is convenient for JSP check; backend permission check preferentially uses User.Role.
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object role = session.getAttribute("role");
            return role != null ? role.toString() : null;
        }
        return null;
    }

    /**
     * Check if user is logged in
     * @param request HTTP request object
     * @return true if logged in
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    /**
     * Check if user is TA role
     * @param request HTTP request object
     * @return true if TA
     */
    public static boolean isTA(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "TA".equals(role);
    }

    /**
     * Check if user is MO role
     * @param request HTTP request object
     * @return true if MO
     */
    public static boolean isMO(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "MO".equals(role);
    }

    /**
     * Check if user is ADMIN role
     * @param request HTTP request object
     * @return true if ADMIN
     */
    public static boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "ADMIN".equals(role);
    }

    /**
     * Check if user has specific role
     * @param request HTTP request object
     * @param role role to check
     * @return true if has the role
     */
    public static boolean hasRole(HttpServletRequest request, User.Role role) {
        User user = getCurrentUser(request);
        return user != null && user.getRole() == role;
    }

    /**
     * Check if user has specific role (string form)
     * @param request HTTP request object
     * @param roleName role name to check
     * @return true if has the role
     */
    public static boolean hasRole(HttpServletRequest request, String roleName) {
        String currentRole = getCurrentUserRole(request);
        return roleName != null && roleName.equalsIgnoreCase(currentRole);
    }

    /**
     * Create or get Session
     * @param request HTTP request object
     * @return HttpSession object
     */
    public static HttpSession getSession(HttpServletRequest request) {
        return request.getSession(true);
    }

    /**
     * Destroy Session
     * @param request HTTP request object
     */
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Set Session attribute
     * @param request HTTP request object
     * @param name attribute name
     * @param value attribute value
     */
    public static void setAttribute(HttpServletRequest request, String name, Object value) {
        HttpSession session = request.getSession(true);
        session.setAttribute(name, value);
    }

    /**
     * Get Session attribute
     * @param request HTTP request object
     * @param name attribute name
     * @return attribute value, null if not exists
     */
    public static Object getAttribute(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getAttribute(name);
        }
        return null;
    }

    /**
     * Remove Session attribute
     * @param request HTTP request object
     * @param name attribute name
     */
    public static void removeAttribute(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    /**
     * Get Session creation time
     * @param request HTTP request object
     * @return creation time (milliseconds), -1 if no session
     */
    public static long getCreationTime(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getCreationTime();
        }
        return -1;
    }

    /**
     * Get Session last access time
     * @param request HTTP request object
     * @return last access time (milliseconds), -1 if no session
     */
    public static long getLastAccessTime(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getLastAccessedTime();
        }
        return -1;
    }

    /**
     * Get Session remaining active time (seconds)
     * @param request HTTP request object
     * @return remaining active time, -1 if no session
     */
    public static int getMaxInactiveInterval(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getMaxInactiveInterval();
        }
        return -1;
    }

    /**
     * Set Session max inactive interval (seconds)
     * @param request HTTP request object
     * @param interval max inactive interval (seconds)
     */
    public static void setMaxInactiveInterval(HttpServletRequest request, int interval) {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(interval);
    }
}

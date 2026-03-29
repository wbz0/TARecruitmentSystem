package com.example.authlogin.util;

import com.example.authlogin.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * SessionUtil - Session工具类
 * 提供便捷的Session操作方法，用于权限验证和用户信息获取
 */
public class SessionUtil {

    /**
     * 获取当前登录用户
     * @param request HTTP请求对象
     * @return 用户对象，如果未登录返回null
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    /**
     * 获取当前登录用户的ID
     * @param request HTTP请求对象
     * @return 用户ID，如果未登录返回null
     */
    public static String getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute("userId");
            return userId != null ? userId.toString() : null;
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户名
     * @param request HTTP请求对象
     * @return 用户名，如果未登录返回null
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute("username");
            return username != null ? username.toString() : null;
        }
        return null;
    }

    /**
     * 获取当前登录用户的角色
     * @param request HTTP请求对象
     * @return 角色名称，如果未登录返回null
     */
    public static String getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object role = session.getAttribute("role");
            return role != null ? role.toString() : null;
        }
        return null;
    }

    /**
     * 检查用户是否已登录
     * @param request HTTP请求对象
     * @return true表示已登录
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    /**
     * 检查用户是否为TA角色
     * @param request HTTP请求对象
     * @return true表示是TA
     */
    public static boolean isTA(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "TA".equals(role);
    }

    /**
     * 检查用户是否为MO角色
     * @param request HTTP请求对象
     * @return true表示是MO
     */
    public static boolean isMO(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "MO".equals(role);
    }

    /**
     * 检查用户是否为ADMIN角色
     * @param request HTTP请求对象
     * @return true表示是ADMIN
     */
    public static boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "ADMIN".equals(role);
    }

    /**
     * 检查用户是否具有特定角色
     * @param request HTTP请求对象
     * @param role 要检查的角色
     * @return true表示具有该角色
     */
    public static boolean hasRole(HttpServletRequest request, User.Role role) {
        User user = getCurrentUser(request);
        return user != null && user.getRole() == role;
    }

    /**
     * 检查用户是否具有特定角色（字符串形式）
     * @param request HTTP请求对象
     * @param roleName 要检查的角色名称
     * @return true表示具有该角色
     */
    public static boolean hasRole(HttpServletRequest request, String roleName) {
        String currentRole = getCurrentUserRole(request);
        return roleName != null && roleName.equalsIgnoreCase(currentRole);
    }

    /**
     * 创建或获取Session
     * @param request HTTP请求对象
     * @return HttpSession对象
     */
    public static HttpSession getSession(HttpServletRequest request) {
        return request.getSession(true);
    }

    /**
     * 销毁Session
     * @param request HTTP请求对象
     */
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * 设置Session属性
     * @param request HTTP请求对象
     * @param name 属性名
     * @param value 属性值
     */
    public static void setAttribute(HttpServletRequest request, String name, Object value) {
        HttpSession session = request.getSession(true);
        session.setAttribute(name, value);
    }

    /**
     * 获取Session属性
     * @param request HTTP请求对象
     * @param name 属性名
     * @return 属性值，如果不存在返回null
     */
    public static Object getAttribute(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getAttribute(name);
        }
        return null;
    }

    /**
     * 移除Session属性
     * @param request HTTP请求对象
     * @param name 属性名
     */
    public static void removeAttribute(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    /**
     * 获取Session创建时间
     * @param request HTTP请求对象
     * @return 创建时间（毫秒），如果无Session返回-1
     */
    public static long getCreationTime(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getCreationTime();
        }
        return -1;
    }

    /**
     * 获取Session最后访问时间
     * @param request HTTP请求对象
     * @return 最后访问时间（毫秒），如果无Session返回-1
     */
    public static long getLastAccessTime(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getLastAccessedTime();
        }
        return -1;
    }

    /**
     * 获取Session剩余存活时间（秒）
     * @param request HTTP请求对象
     * @return 剩余存活时间，如果无Session返回-1
     */
    public static int getMaxInactiveInterval(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getMaxInactiveInterval();
        }
        return -1;
    }

    /**
     * 设置Session最大不活跃时间（秒）
     * @param request HTTP请求对象
     * @param interval 最大不活跃时间（秒）
     */
    public static void setMaxInactiveInterval(HttpServletRequest request, int interval) {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(interval);
    }
}

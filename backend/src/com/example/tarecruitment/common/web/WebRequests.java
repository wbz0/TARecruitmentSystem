package com.example.tarecruitment.common.web;

import com.example.tarecruitment.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet 请求读取工具。
 *
 * 这里放所有 web 层重复用到的小判断：当前用户、AJAX 请求、基础字符串清理、
 * 危险标记检查。service/dao 层不要依赖这个类，避免把 HTTP 细节带进业务层。
 */
public final class WebRequests {

    private WebRequests() {
    }

    /**
     * 从 session 中读取当前登录用户。
     *
     * Servlet 层使用这个方法，避免每个入口重复写 session.getAttribute("user")。
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
     * 判断是否为前端 fetch/ajax 请求。
     */
    public static boolean isAjax(HttpServletRequest request) {
        return request != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * 请求参数统一 trim，null 归为空字符串。
     */
    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 控制字符检查，用于基础输入防护。
     */
    public static boolean containsControlChars(String value) {
        return value != null && value.chars().anyMatch(ch -> ch < 32 || ch == 127);
    }

    /**
     * 明显 HTML/JS 标记检查。
     *
     * 这是 web 层通用兜底；更精确的字段规则仍放在各 domain validator。
     */
    public static boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // 这里只做进入业务前的轻量防护；具体字段长度/格式仍由各 domain validator 判断。
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
                || text.contains("javascript:")
                || text.matches(".*on\\w+\\s*=.*");
    }
}

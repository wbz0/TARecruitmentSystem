package com.example.tarecruitment.notification.web;

import com.example.tarecruitment.notification.dao.NotificationDao;
import com.example.tarecruitment.notification.model.Notification;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationServlet - System notification API.
 *
 * GET    /api/notifications - All logged-in users can view.
 * POST   /api/notifications - Only ADMIN can publish, parameters title/content.
 * DELETE /api/notifications - Only ADMIN can delete, parameter notificationId.
 *
 * Corresponds to TA/MO/Admin three sets of notifications.jsp pages; notifications are site-wide shared, CSV not split by role.
 */
@WebServlet(ApiRoutes.NOTIFICATIONS)
public class NotificationServlet extends HttpServlet {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private NotificationDao notificationDao;

    @Override
    public void init() throws ServletException {
        notificationDao = NotificationDao.getInstance();
    }

    // GET - list all notifications

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if (user == null) {
            ApiResponses.write(response, 401, false, "Please login first", null);
            return;
        }

        List<Notification> notifications = notificationDao.findAll();
        List<Map<String, Object>> payload = new ArrayList<>();
        for (Notification n : notifications) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("notificationId", n.getNotificationId());
            item.put("title", n.getTitle() != null ? n.getTitle() : "");
            item.put("content", n.getContent() != null ? n.getContent() : "");
            item.put("publishedAt", n.getPublishedAt() != null ? n.getPublishedAt().format(FMT) : "");
            item.put("publishedByUsername", n.getPublishedByUsername() != null ? n.getPublishedByUsername() : "");
            payload.add(item);
        }

        ApiResponses.write(response, 200, true, "OK", payload);
    }

    // POST - publish a notification (admin only)

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if (user == null) {
            ApiResponses.write(response, 401, false, "Please login first", null);
            return;
        }
        if (user.getRole() != User.Role.ADMIN) {
            ApiResponses.write(response, 403, false, "Only admins can publish notifications", null);
            return;
        }

        String title   = safe(request.getParameter("title"));
        String content = safe(request.getParameter("content"));

        if (title.isEmpty()) {
            ApiResponses.write(response, 400, false, "Title is required", null);
            return;
        }
        if (content.isEmpty()) {
            ApiResponses.write(response, 400, false, "Content is required", null);
            return;
        }

        Notification n = new Notification();
        n.setTitle(title);
        n.setContent(content);
        // Save publisher snapshot; later account rename won't affect historical notification display.
        n.setPublishedByUserId(user.getUserId());
        n.setPublishedByUsername(user.getUsername());

        notificationDao.save(n);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("notificationId", n.getNotificationId());
        ApiResponses.write(response, 201, true, "Notification published", data);
    }

    // DELETE - remove a notification (admin only)

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if (user == null) {
            ApiResponses.write(response, 401, false, "Please login first", null);
            return;
        }
        if (user.getRole() != User.Role.ADMIN) {
            ApiResponses.write(response, 403, false, "Only admins can delete notifications", null);
            return;
        }

        String notificationId = safe(request.getParameter("notificationId"));
        if (notificationId.isEmpty()) {
            ApiResponses.write(response, 400, false, "notificationId is required", null);
            return;
        }

        boolean removed = notificationDao.deleteById(notificationId);
        if (removed) {
            ApiResponses.write(response, 200, true, "Notification deleted", null);
        } else {
            ApiResponses.write(response, 404, false, "Notification not found", null);
        }
    }

    // Helpers

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (User) session.getAttribute("user");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

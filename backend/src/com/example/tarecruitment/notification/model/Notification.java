package com.example.tarecruitment.notification.model;

import com.example.tarecruitment.common.storage.CsvCodec;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Notification entity class - System notification published by admin.
 *
 * Notifications are shared display for TA, MO, ADMIN notification pages; CSV only saves title, body, publish time and publisher snapshot.
 */
public class Notification {

    private String notificationId;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private String publishedByUserId;
    private String publishedByUsername;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Notification() {
        this.notificationId = UUID.randomUUID().toString();
        this.publishedAt = LocalDateTime.now();
    }

    // Getters / Setters

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public String getPublishedByUserId() { return publishedByUserId; }
    public void setPublishedByUserId(String publishedByUserId) { this.publishedByUserId = publishedByUserId; }

    public String getPublishedByUsername() { return publishedByUsername; }
    public void setPublishedByUsername(String publishedByUsername) { this.publishedByUsername = publishedByUsername; }

    // CSV serialisation

    /**
     * Field order must be consistent with NotificationDao.CSV_HEADER.
     */
    public String toCsv() {
        return String.join(",",
            escapeCsv(notificationId),
            escapeCsv(title != null ? title : ""),
            escapeCsv(content != null ? content : ""),
            publishedAt != null ? publishedAt.format(FMT) : "",
            escapeCsv(publishedByUserId != null ? publishedByUserId : ""),
            escapeCsv(publishedByUsername != null ? publishedByUsername : "")
        );
    }

    public static Notification fromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = CsvCodec.split(csvLine);
        if (parts.length < 6) return null;

        Notification n = new Notification();
        n.setNotificationId(unescapeCsv(parts[0]));
        n.setTitle(unescapeCsv(parts[1]));
        n.setContent(unescapeCsv(parts[2]));
        if (!parts[3].isEmpty()) {
            try { n.setPublishedAt(LocalDateTime.parse(parts[3], FMT)); }
            catch (Exception ignored) { /* Compatibility with bad timestamps: keep current time from constructor to avoid losing entire notification. */ }
        }
        n.setPublishedByUserId(unescapeCsv(parts[4]));
        n.setPublishedByUsername(unescapeCsv(parts[5]));
        return n;
    }

    private static String escapeCsv(String value) {
        return CsvCodec.escape(value);
    }

    private static String unescapeCsv(String value) {
        return CsvCodec.unescape(value);
    }

    @Override
    public String toString() {
        return "Notification{id='" + notificationId + "', title='" + title + "', publishedAt=" + publishedAt + '}';
    }
}

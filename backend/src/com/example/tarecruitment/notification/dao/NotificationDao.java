package com.example.tarecruitment.notification.dao;

import com.example.tarecruitment.notification.model.Notification;
import com.example.tarecruitment.common.storage.StoragePaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * NotificationDao - System notification CSV data access object.
 *
 * Storage path: {TA_HIRING_DATA_DIR}/notifications/notifications.csv.
 * Only responsible for notification read/write, permission and response format handled by NotificationServlet.
 */
public class NotificationDao {

    private static final String DIR  = StoragePaths.getNotificationsDir();
    private static final String FILE = DIR + File.separator + "notifications.csv";
    private static final String CSV_HEADER = "notificationId,title,content,publishedAt,publishedByUserId,publishedByUsername";

    private static NotificationDao instance;

    private NotificationDao() {
        initDir();
    }

    public static synchronized NotificationDao getInstance() {
        if (instance == null) {
            instance = new NotificationDao();
        }
        return instance;
    }

    private void initDir() {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void initFile() {
        File f = new File(FILE);
        if (!f.exists()) {
            try {
                f.getParentFile().mkdirs();
                f.createNewFile();
                try (FileWriter w = new FileWriter(FILE)) {
                    w.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create notifications file", e);
            }
        }
    }

    // Read

    private synchronized List<Notification> readAll() {
        initFile();
        List<Notification> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                if (line.trim().isEmpty()) continue;
                Notification n = Notification.fromCsv(line);
                if (n != null) list.add(n);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read notifications file", e);
        }
        // Frontend notification page displays newest announcements first.
        list.sort(Comparator.comparing(Notification::getPublishedAt).reversed());
        return list;
    }

    // Write

    private synchronized void writeAll(List<Notification> list) {
        Path target = Path.of(FILE);
        Path temp   = Path.of(FILE + ".tmp");
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(temp))) {
            w.println(CSV_HEADER);
            for (Notification n : list) {
                w.println(n.toCsv());
            }
            w.flush();
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            throw new RuntimeException("Failed to write notifications file", e);
        }
    }

    // Public API

    /** Returns all notifications, newest first. */
    public List<Notification> findAll() {
        return readAll();
    }

    /** Appends a new notification. */
    public synchronized Notification save(Notification n) {
        initFile();
        // Publishing notification is append write; only rewrite whole CSV when deleting.
        try (FileWriter fw = new FileWriter(FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(n.toCsv());
        } catch (IOException e) {
            throw new RuntimeException("Failed to append notification", e);
        }
        return n;
    }

    /** Removes the notification with the given id. Returns true if found and removed. */
    public synchronized boolean deleteById(String id) {
        if (id == null || id.isEmpty()) return false;
        List<Notification> all = readAll();
        boolean removed = all.removeIf(n -> id.equals(n.getNotificationId()));
        if (removed) {
            // Write back in old-to-new order; subsequent append still keeps natural file time order.
            all.sort(Comparator.comparing(Notification::getPublishedAt));
            writeAll(all);
        }
        return removed;
    }
}

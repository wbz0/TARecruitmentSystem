package com.example.tarecruitment.auth.model;

import com.example.tarecruitment.common.storage.CsvCodec;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * User entity - Login account.
 *
 * Supports three roles: TA, MO, ADMIN. This class also handles CSV serialization,
 * so new fields can only be appended at the end and cannot break old CSV column order.
 */
public class User {

    private String userId;
    private String username;
    private String password;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private String displayName;
    private String realName;
    private String professionalTitle;
    private String avatarPath;

    public enum Role {
        TA, MO, ADMIN
    }

    public User() {
        this.userId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String password, String email, Role role) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getProfessionalTitle() {
        return professionalTitle;
    }

    public void setProfessionalTitle(String professionalTitle) {
        this.professionalTitle = professionalTitle;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    /**
     * Convert to CSV format for storage
     */
    public String toCsv() {
        // displayName/realName/professionalTitle/avatarPath are new fields added after old fields for account profile page.
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.join(",",
            escapeCsv(userId),
            escapeCsv(username),
            escapeCsv(password),
            escapeCsv(email),
            role.name(),
            createdAt != null ? createdAt.format(formatter) : "",
            lastLoginAt != null ? lastLoginAt.format(formatter) : "",
            escapeCsv(displayName),
            escapeCsv(realName),
            escapeCsv(professionalTitle),
            escapeCsv(avatarPath)
        );
    }

    /**
     * Parse from CSV format
     */
    public static User fromCsv(String csvLine) {
        // Compatible with old CSV: early 10th column may be avatarPath directly, not professionalTitle.
        String[] parts = CsvCodec.split(csvLine);
        if (parts.length < 6) {
            return null;
        }

        User user = new User();
        user.setUserId(unescapeCsv(parts[0]));
        user.setUsername(unescapeCsv(parts[1]));
        user.setPassword(unescapeCsv(parts[2]));
        user.setEmail(unescapeCsv(parts[3]));
        user.setRole(Role.valueOf(parts[4].trim()));

        if (parts.length > 5 && !parts[5].isEmpty()) {
            user.setCreatedAt(LocalDateTime.parse(parts[5], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (parts.length > 6 && !parts[6].isEmpty()) {
            user.setLastLoginAt(LocalDateTime.parse(parts[6], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (parts.length > 7) {
            user.setDisplayName(unescapeCsv(parts[7]));
        }
        if (parts.length > 8) {
            user.setRealName(unescapeCsv(parts[8]));
        }
        if (parts.length > 9) {
            String ninthField = unescapeCsv(parts[9]);
            if (parts.length == 10 && looksLikeAvatarPath(ninthField)) {
                user.setAvatarPath(ninthField);
            } else {
                user.setProfessionalTitle(ninthField);
            }
        }
        if (parts.length > 10) {
            user.setAvatarPath(unescapeCsv(parts[10]));
        }

        return user;
    }

    private static boolean looksLikeAvatarPath(String value) {
        // Legacy compatibility: determine whether the old CSV's 10th column is avatar path or professional title.
        if (value == null || value.isEmpty()) {
            return false;
        }
        String lower = value.toLowerCase();
        return lower.startsWith("account-avatars/")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp");
    }

    private static String escapeCsv(String value) {
        return CsvCodec.escape(value);
    }

    private static String unescapeCsv(String value) {
        return CsvCodec.unescape(value);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId != null && userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}

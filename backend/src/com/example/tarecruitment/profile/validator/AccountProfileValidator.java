package com.example.tarecruitment.profile.validator;

import jakarta.servlet.http.Part;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * AccountProfileValidator - Account profile form validation.
 *
 * Used for /api/me/account display name, real name, MO professional title, and account avatar validation.
 * TA profile fullName stricter rules are also reused here to ensure consistency between account page and profile page.
 */
public final class AccountProfileValidator {

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final int USERNAME_MAX_LENGTH = 20;
    private static final int REAL_NAME_MAX_LENGTH = 100;
    private static final int PROFESSIONAL_TITLE_MAX_LENGTH = 40;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{2,19}$");
    private static final List<String> ALLOWED_AVATAR_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");
    private static final List<String> ALLOWED_AVATAR_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp");

    private AccountProfileValidator() {
    }

    public static String normalizeInput(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    public static String normalizeUsername(String value) {
        return value == null ? "" : value.trim();
    }

    public static String validateUsernameFormat(String username) {
        if (!isNotEmpty(username)) return "Username is required";
        if (username.length() > USERNAME_MAX_LENGTH) return "Username is too long";
        if (hasControlChars(username) || containsDangerousMarkup(username)) return "Username contains unsupported characters";
        if (!USERNAME_PATTERN.matcher(username).matches()) return "Username format is invalid";
        if (username.contains("__")) return "Username cannot contain consecutive underscores";
        if (username.charAt(username.length() - 1) == '_') return "Username cannot end with an underscore";
        return null;
    }

    public static String validateNames(String realName, String professionalTitle) {
        if (realName != null && realName.length() > REAL_NAME_MAX_LENGTH) return "Real name is too long";
        if (professionalTitle != null && professionalTitle.length() > PROFESSIONAL_TITLE_MAX_LENGTH) {
            return "Professional title is too long";
        }
        if (hasControlChars(realName) || hasControlChars(professionalTitle)
                || containsDangerousMarkup(realName)
                || containsDangerousMarkup(professionalTitle)) {
            return "Account profile contains unsupported characters";
        }
        return null;
    }

    public static String validateTaSharedRealName(String realName, boolean hasApplicantProfile) {
        if (!hasApplicantProfile) {
            return null;
        }
        // Once a TA has established a profile, the account real name must continue to comply with profile fullName rules.
        if (!isNotEmpty(realName)) {
            return "Full name is required.";
        }
        return validateApplicantFullName(realName);
    }

    public static String validateAvatar(Part avatarPart) {
        if (avatarPart.getSize() > MAX_AVATAR_SIZE) return "Avatar file is too large";

        String submittedFileName = avatarPart.getSubmittedFileName();
        String extension = extractExtension(submittedFileName, "");
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) return "Avatar must be JPG, PNG, or WEBP";

        String contentType = avatarPart.getContentType();
        if (contentType != null && !contentType.trim().isEmpty()
                && !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return "Avatar must be JPG, PNG, or WEBP";
        }
        return null;
    }

    public static boolean isUsableFilePart(Part part) {
        return part != null && part.getSize() > 0 && isNotEmpty(part.getSubmittedFileName());
    }

    public static String extractExtension(String fileName, String defaultExtension) {
        String fallback = isNotEmpty(defaultExtension) ? defaultExtension.toLowerCase() : "";
        if (fileName == null) {
            return fallback;
        }
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return fallback;
        }
        return fileName.substring(dotIndex).toLowerCase();
    }

    public static String sanitizeBaseName(String fileName, String fallbackName) {
        String safeFileName = fileName != null ? fileName.trim() : "";
        int slashIndex = Math.max(safeFileName.lastIndexOf('/'), safeFileName.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < safeFileName.length() - 1) {
            // Browser may submit file name with path; only keep the last file name part.
            safeFileName = safeFileName.substring(slashIndex + 1);
        }

        int dotIndex = safeFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? safeFileName.substring(0, dotIndex) : safeFileName;
        baseName = baseName.replaceAll("[^\\p{L}\\p{N}._-]+", "_");
        baseName = baseName.replaceAll("_+", "_");
        baseName = baseName.replaceAll("^[._-]+", "");
        baseName = baseName.replaceAll("[._-]+$", "");

        if (!isNotEmpty(baseName)) {
            return isNotEmpty(fallbackName) ? fallbackName : "avatar";
        }
        return baseName.length() > 60 ? baseName.substring(0, 60) : baseName;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String validateApplicantFullName(String value) {
        if (value.length() < 2) return "Full name must be at least 2 characters.";
        if (value.length() > REAL_NAME_MAX_LENGTH) return "Full name must be 100 characters or fewer.";
        if (!hasLetterOrCjk(value)) return "Full name must include at least one letter.";
        if (!value.matches("^[A-Za-z\\u00C0-\\u024F\\u4E00-\\u9FFF\\s.'-]+$")) {
            return "Full name contains unsupported characters.";
        }
        if (hasExcessiveRepeatedChars(value, 4)) return "Full name contains too many repeated characters.";
        return null;
    }

    private static boolean hasControlChars(String value) {
        return value != null && value.matches(".*[\\x00-\\x1F\\x7F].*");
    }

    private static boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // Account profile is displayed directly in sidebar/top bar; first reject obvious HTML/JS fragments.
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
                || text.contains("javascript:")
                || text.matches(".*on\\w+\\s*=.*");
    }

    private static boolean hasLetterOrCjk(String value) {
        return value != null && value.matches(".*[A-Za-z\\u00C0-\\u024F\\u4E00-\\u9FFF].*");
    }

    private static boolean hasExcessiveRepeatedChars(String value, int threshold) {
        if (value == null) {
            return false;
        }
        int safeThreshold = Math.max(1, threshold);
        return value.matches(".*(.)\\1{" + safeThreshold + ",}.*");
    }
}

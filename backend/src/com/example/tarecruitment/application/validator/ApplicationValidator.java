package com.example.tarecruitment.application.validator;

import java.util.Set;

/**
 * Input validation for application API.
 *
 * This only validates whether HTTP parameters are safe, length is reasonable, and action is in the allowed list;
 * business rules like permissions, job is open, duplicate application etc. are in ApplicationService.
 */
public final class ApplicationValidator {

    private static final int MAX_ID_LENGTH = 120;
    private static final int MAX_COVER_LETTER_LENGTH = 2000;
    private static final Set<String> TRANSITION_ACTIONS = Set.of("accept", "reject", "withdraw");

    private ApplicationValidator() {
    }

    public static String validateApplicationId(String applicationId) {
        return validateIdentifier(applicationId, "Application ID");
    }

    public static String validateJobId(String jobId) {
        return validateIdentifier(jobId, "Job ID");
    }

    public static String validateCoverLetter(String coverLetter) {
        String value = coverLetter != null ? coverLetter.trim() : "";
        if (value.length() > MAX_COVER_LETTER_LENGTH) {
            return "Cover letter must be 2000 characters or fewer";
        }
        if (hasControlChars(coverLetter) || containsDangerousMarkup(coverLetter)) {
            return "Cover letter contains unsupported characters";
        }
        return null;
    }

    public static String validateTransitionAction(String action) {
        String value = action != null ? action.trim().toLowerCase() : "";
        if (value.isEmpty()) {
            return "Action is required";
        }
        if (!TRANSITION_ACTIONS.contains(value)) {
            return "Invalid action. Use 'accept', 'reject', or 'withdraw'";
        }
        return null;
    }

    private static String validateIdentifier(String value, String label) {
        String text = value != null ? value.trim() : "";
        if (text.isEmpty()) {
            return label + " is required";
        }
        if (text.length() > MAX_ID_LENGTH || hasControlChars(text) || containsDangerousMarkup(text)) {
            return label + " contains unsupported characters";
        }
        return null;
    }

    private static boolean hasControlChars(String value) {
        return value != null && value.matches(".*[\\x00-\\x1F\\x7F].*");
    }

    private static boolean containsDangerousMarkup(String value) {
        // Prevent obvious HTML/JS fragments from being written into CSV and then rendered by page; frontend will still do escapeHtml.
        if (value == null || value.isEmpty()) {
            return false;
        }
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
                || text.contains("javascript:")
                || text.matches(".*on\\w+\\s*=.*");
    }
}

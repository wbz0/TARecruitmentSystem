package com.example.tarecruitment.profile.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ApplicantProfileValidator - TA profile field validation.
 *
 * Responsible for visible form field required, length, format, and obvious spam input checks.
 * Student ID uniqueness, file saving, and account/application synchronization are not handled here.
 */
public final class ApplicantProfileValidator {

    private static final List<String> ALLOWED_PROGRAMS = Arrays.asList("Undergraduate", "Master", "PhD");

    private ApplicantProfileValidator() {
    }

    public static String normalizeInput(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static List<String> parseSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // TA profile skills allow comma or semicolon, unified to semicolon when saving to CSV.
        return Arrays.stream(skills.split("[;,]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static boolean isTruthyFlag(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        return "true".equalsIgnoreCase(normalized) || "1".equals(normalized);
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String validatePartialInput(ApplicantProfileInput input) {
        // Partial update only validates fields present in this request; fields not passed retain their original values.
        if (input.getFullNameRaw() != null) {
            String fullName = normalizeInput(input.getFullNameRaw());
            if (fullName == null) return "Full name cannot be empty.";
            String error = validateFullName(fullName);
            if (error != null) return error;
        }

        if (input.getStudentIdRaw() != null) {
            String studentId = normalizeInput(input.getStudentIdRaw());
            if (studentId == null) return "Student ID cannot be empty.";
            String error = validateStudentId(studentId);
            if (error != null) return error;
        }

        if (input.getDepartmentRaw() != null) {
            String department = normalizeInput(input.getDepartmentRaw());
            if (department == null) return "Department cannot be empty.";
            String error = validateDepartment(department);
            if (error != null) return error;
        }

        if (input.getProgramRaw() != null) {
            String program = normalizeInput(input.getProgramRaw());
            if (program == null) return "Program cannot be empty.";
            String error = validateProgram(program);
            if (error != null) return error;
        }

        String gpa = normalizeInput(input.getGpaRaw());
        if (input.getGpaRaw() != null && gpa == null) return "GPA cannot be empty.";
        if (input.getGpaRaw() != null && gpa != null) {
            String error = validateGpa(gpa);
            if (error != null) return error;
        }

        String skills = normalizeInput(input.getSkillsRaw());
        if (input.getSkillsRaw() != null && skills == null) return "Skills cannot be empty.";
        if (input.getSkillsRaw() != null && skills != null) {
            String error = validateSkills(skills);
            if (error != null) return error;
        }

        String phone = normalizeInput(input.getPhoneRaw());
        if (input.getPhoneRaw() != null && phone == null) return "Phone number cannot be empty.";
        if (input.getPhoneRaw() != null && phone != null) {
            String error = validatePhone(phone);
            if (error != null) return error;
        }

        String address = normalizeInput(input.getAddressRaw());
        if (input.getAddressRaw() != null && address != null) {
            String error = validateAddress(address);
            if (error != null) return error;
        }

        String experience = normalizeInput(input.getExperienceRaw());
        if (input.getExperienceRaw() != null && experience == null) return "Related experience cannot be empty.";
        if (input.getExperienceRaw() != null && experience != null) {
            String error = validateLongTextField(experience, "Related experience");
            if (error != null) return error;
        }

        String motivation = normalizeInput(input.getMotivationRaw());
        if (input.getMotivationRaw() != null && motivation == null) return "Motivation cannot be empty.";
        if (input.getMotivationRaw() != null && motivation != null) {
            String error = validateLongTextField(motivation, "Motivation");
            if (error != null) return error;
        }
        return null;
    }

    public static String validateInput(ApplicantProfileInput input, boolean requireRequiredFields) {
        // Creating a profile requires all main form fields; the same validation rules can be reused during editing.
        if (requireRequiredFields && input.getFullName() == null) return "Full name is required.";
        if (input.getFullName() != null) {
            String error = validateFullName(input.getFullName());
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getStudentId() == null) return "Student ID is required.";
        if (input.getStudentId() != null) {
            String error = validateStudentId(input.getStudentId());
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getDepartment() == null) return "Department is required.";
        if (input.getDepartment() != null) {
            String error = validateDepartment(input.getDepartment());
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getProgram() == null) return "Program is required.";
        if (input.getProgram() != null) {
            String error = validateProgram(input.getProgram());
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getGpa() == null) return "GPA is required.";
        if (input.getGpa() != null) {
            String error = validateGpa(input.getGpa());
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getSkills() == null) return "Skills are required.";
        if (input.getSkills() != null) {
            String error = validateSkills(input.getSkills());
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getPhone() == null) return "Phone number is required.";
        if (input.getPhone() != null) {
            String error = validatePhone(input.getPhone());
            if (error != null) return error;
        }

        if (input.getAddress() != null) {
            String error = validateAddress(input.getAddress());
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getExperience() == null) return "Related experience is required.";
        if (input.getExperience() != null) {
            String error = validateLongTextField(input.getExperience(), "Related experience");
            if (error != null) return error;
        }

        if (requireRequiredFields && input.getMotivation() == null) return "Motivation is required.";
        if (input.getMotivation() != null) {
            String error = validateLongTextField(input.getMotivation(), "Motivation");
            if (error != null) return error;
        }
        return null;
    }

    private static String validateFullName(String value) {
        if (value.length() < 2) return "Full name must be at least 2 characters.";
        if (value.length() > 100) return "Full name must be 100 characters or fewer.";
        if (!hasLetterOrCjk(value)) return "Full name must include at least one letter.";
        if (!value.matches("^[A-Za-z\\u00C0-\\u024F\\u4E00-\\u9FFF\\s.'-]+$")) {
            return "Full name contains unsupported characters.";
        }
        if (hasExcessiveRepeatedChars(value, 4)) return "Full name contains too many repeated characters.";
        return null;
    }

    private static String validateStudentId(String value) {
        if (!value.matches("^\\d{10}$")) return "Student ID must be exactly 10 digits, for example 2023213039.";
        if (!value.matches("^20\\d{8}$")) return "Student ID should start with 20, for example 2023213051.";
        int year = Integer.parseInt(value.substring(0, 4));
        if (year < 2010 || year > 2099) return "Student ID year appears invalid. Please check the first 4 digits.";
        if (value.matches("^(\\d)\\1{9}$")) {
            return "Student ID appears invalid. Please check your official 10-digit student number.";
        }
        return null;
    }

    private static String validateDepartment(String value) {
        if (value.length() < 2) return "Department must be at least 2 characters.";
        if (value.length() > 100) return "Department must be 100 characters or fewer.";
        if (!hasLetterOrCjk(value)) return "Department should include letters.";
        if (!value.matches("^[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF\\s&(),./'-]+$")) {
            return "Department contains unsupported characters.";
        }
        if (hasExcessiveRepeatedChars(value, 6)) return "Department contains too many repeated characters.";
        return null;
    }

    private static String validateProgram(String value) {
        return ALLOWED_PROGRAMS.contains(value) ? null : "Please select a valid program option.";
    }

    private static String validateGpa(String value) {
        if (value.length() > 20) return "GPA must be 20 characters or fewer.";
        if (!value.matches("^[0-9.,/\\s]+$")) {
            return "GPA may only include digits, spaces, decimal separators, and '/'.";
        }

        String normalized = value.replaceAll("\\s+", "").replace(",", ".");
        String[] parts = normalized.split("/", -1);
        if (parts.length > 2) return "GPA format is invalid. Use one optional '/'.";
        if (!parts[0].matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
            return "GPA value supports up to 2 decimal places.";
        }

        double actual = Double.parseDouble(parts[0]);
        if (actual < 0) return "GPA cannot be negative.";

        if (parts.length == 2) {
            if (!parts[1].matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
                return "GPA scale supports up to 2 decimal places.";
            }
            double scale = Double.parseDouble(parts[1]);
            if (scale < 4 || scale > 100) return "GPA scale should be between 4 and 100.";
            if (actual > scale) return "GPA value cannot be greater than the GPA scale.";
        } else if (actual > 4.3) {
            return "For GPA above 4.3, please include scale (for example 85/100).";
        }
        return null;
    }

    private static String validateSkills(String value) {
        if (value.length() > 300) return "Skills must be 300 characters or fewer.";
        if (value.matches("(^[;,].*|.*[;,]\\s*[;,].*|.*[;,]\\s*$)")) {
            return "Please remove empty skill items between separators.";
        }

        List<String> items = parseSkills(value);
        if (items.size() > 12) return "Please list up to 12 skills.";

        Set<String> seen = new HashSet<>();
        for (String skill : items) {
            if (skill.length() < 2 || skill.length() > 40) return "Each skill should be 2 to 40 characters.";
            if (!hasLetterOrCjk(skill)) return "Each skill should include letters.";
            if (!skill.matches("^[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF+#&./\\-\\s]+$")) {
                return "Skills contain unsupported characters.";
            }
            if (hasExcessiveRepeatedChars(skill, 5)) return "A skill item has too many repeated characters.";
            String normalized = skill.toLowerCase().replaceAll("\\s+", " ").trim();
            if (!seen.add(normalized)) return "Duplicate skills found. Please keep each skill only once.";
        }
        return null;
    }

    private static String validatePhone(String value) {
        if (value.length() > 30) return "Phone number must be 30 characters or fewer.";
        if (!value.matches("^[\\d+\\-()./\\s]+$")) {
            return "Phone number may only include digits, spaces, and + - ( ) . /.";
        }
        if (countOccurrences(value, '+') > 1) return "Phone number can contain only one '+'.";
        if (value.indexOf('+') > 0) return "If used, '+' must be at the beginning.";
        if (!hasBalancedParentheses(value)) return "Phone number parentheses are not balanced.";

        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 8 || digits.length() > 15) return "Phone number should contain 8 to 15 digits.";
        if (digits.matches("^(\\d)\\1+$")) return "Phone number appears invalid. Please check repeated digits.";
        if (value.startsWith("+") && digits.length() < 10) {
            return "International format should usually contain at least 10 digits.";
        }
        return null;
    }

    private static String validateAddress(String value) {
        if (value.length() > 200) return "Address must be 200 characters or fewer.";
        if (value.length() < 5) return "Address should be at least 5 characters if provided.";
        if (!hasLetterOrCjk(value)) return "Address should include letters.";
        if (hasOnlyPunctuationAndSpace(value)) return "Address cannot contain only punctuation.";
        if (!value.matches("^[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF\\s#&(),./:'-]+$")) {
            return "Address contains unsupported characters.";
        }
        if (hasExcessiveRepeatedChars(value, 8)) return "Address contains too many repeated characters.";
        return null;
    }

    private static String validateLongTextField(String value, String label) {
        if (value.length() > 1200) return label + " must be 1200 characters or fewer.";
        if (value.length() < 20) return label + " should be at least 20 characters if provided.";
        // Supports both English word count and Chinese character count to avoid Chinese descriptions being mistakenly judged as "insufficient character count".
        if (getTextContentUnits(value) < 10) {
            return label + " should contain more detail (about 10 words/characters).";
        }
        if (hasExcessiveRepeatedChars(value, 8)) return label + " contains too many repeated characters.";
        return null;
    }

    private static boolean hasLetterOrCjk(String value) {
        return value != null && value.matches(".*[A-Za-z\\u00C0-\\u024F\\u4E00-\\u9FFF].*");
    }

    private static boolean hasOnlyPunctuationAndSpace(String value) {
        return value != null && !value.matches(".*[A-Za-z0-9\\u00C0-\\u024F\\u4E00-\\u9FFF].*");
    }

    private static boolean hasExcessiveRepeatedChars(String value, int threshold) {
        if (value == null) {
            return false;
        }
        int safeThreshold = Math.max(1, threshold);
        return value.matches(".*(.)\\1{" + safeThreshold + ",}.*");
    }

    private static boolean hasBalancedParentheses(String value) {
        int balance = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) return false;
            }
        }
        return balance == 0;
    }

    private static int countOccurrences(String value, char target) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    private static int getTextContentUnits(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        int cjkChars = value.replaceAll("[^\\u4E00-\\u9FFF]", "").length();
        String latinPart = value.replaceAll("[\\u4E00-\\u9FFF]", " ");
        String[] tokens = latinPart.split("[^A-Za-z0-9'-]+");

        int latinWords = 0;
        for (String token : tokens) {
            if (!token.isEmpty()) {
                latinWords++;
            }
        }
        return cjkChars + latinWords;
    }
}

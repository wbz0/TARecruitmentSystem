package com.example.tarecruitment.job.validator;

import com.example.tarecruitment.job.mapper.JobRequestMapper;
import com.example.tarecruitment.job.model.Job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JobValidator - Job form validation.
 *
 * Responsible for required, length, format, dangerous input and date relationships; permissions, MO ownership, CSV write and other business flows in JobService.
 * These error messages are directly displayed on MO publish/edit job form, so keep them plain and clear.
 */
public final class JobValidator {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_COURSE_CODE_LENGTH = 50;
    private static final int MAX_COURSE_NAME_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 4000;
    private static final int MAX_SKILLS_LENGTH = 500;
    private static final int MAX_SALARY_LENGTH = 120;
    private static final int MAX_POSITIONS = 200;
    private static final double MIN_WEEKLY_HOURS = 0.5;
    private static final double MAX_WEEKLY_HOURS = 40.0;

    private JobValidator() {
    }

    /**
     * Overall validation entry for creating job.
     *
     * After field-by-field validation, then validate overall order of deadline, start time and end time.
     */
    public static String validateCreate(String title,
                                        String courseCode,
                                        String courseName,
                                        String description,
                                        String skills,
                                        String positionsStr,
                                        String weeklyHoursStr,
                                        String workStartDateStr,
                                        String workEndDateStr,
                                        String salary,
                                        String deadlineStr) {
        String titleText = JobRequestMapper.trimToEmpty(title);
        String courseCodeText = JobRequestMapper.trimToEmpty(courseCode);
        String courseNameText = JobRequestMapper.trimToEmpty(courseName);
        String descriptionText = JobRequestMapper.trimToEmpty(description);
        String skillsText = JobRequestMapper.trimToEmpty(skills);
        String positionsText = JobRequestMapper.trimToEmpty(positionsStr);
        String weeklyHoursText = JobRequestMapper.trimToEmpty(weeklyHoursStr);
        String workStartDateText = JobRequestMapper.trimToEmpty(workStartDateStr);
        String workEndDateText = JobRequestMapper.trimToEmpty(workEndDateStr);
        String salaryText = JobRequestMapper.trimToEmpty(salary);
        String deadlineText = JobRequestMapper.trimToEmpty(deadlineStr);

        // Most fields required when creating job; positions defaults to 1 if empty in service.
        String error = validateTitle(titleText, true);
        if (error != null) return error;
        error = validateCourseCode(courseCodeText, true);
        if (error != null) return error;
        error = validateCourseName(courseNameText, true);
        if (error != null) return error;
        error = validateDescription(descriptionText, true);
        if (error != null) return error;
        error = validateSkills(skillsText, true);
        if (error != null) return error;
        error = validatePositions(positionsText, false);
        if (error != null) return error;
        error = validateWeeklyHours(weeklyHoursText, true);
        if (error != null) return error;
        error = validateWorkDate(workStartDateText, "Work start date", true);
        if (error != null) return error;
        error = validateWorkDate(workEndDateText, "Work end date", true);
        if (error != null) return error;
        error = validateDeadline(deadlineText, true);
        if (error != null) return error;
        error = validateWorkSchedule(JobRequestMapper.parseDeadline(deadlineText),
                LocalDate.parse(workStartDateText), LocalDate.parse(workEndDateText), true);
        if (error != null) return error;
        return validateSalary(salaryText, true);
    }

    /**
     * Job title displayed on list card and detail page, control characters and obvious HTML prohibited.
     */
    public static String validateTitle(String titleText, boolean required) {
        if (required && titleText.isEmpty()) return "Job title is required";
        if (titleText.isEmpty()) return null;
        if (titleText.length() > MAX_TITLE_LENGTH) return "Job title is too long";
        if (hasControlChars(titleText) || containsDangerousMarkup(titleText)) {
            return "Job title contains unsupported characters";
        }
        return null;
    }

    /**
     * Course code allows common alphanumeric, spaces and separators to support formats like SE601 / CS-101.
     */
    public static String validateCourseCode(String courseCodeText, boolean required) {
        if (required && courseCodeText.isEmpty()) return "Course code is required";
        if (courseCodeText.isEmpty()) return null;
        if (courseCodeText.length() > MAX_COURSE_CODE_LENGTH) return "Course code is too long";
        if (!courseCodeText.matches("^[A-Za-z0-9][A-Za-z0-9 _\\-/.]{0,49}$")) {
            return "Course code contains unsupported characters";
        }
        return null;
    }

    /**
     * Course name is display field, just limit length and dangerous markup.
     */
    public static String validateCourseName(String courseNameText, boolean required) {
        if (required && courseNameText.isEmpty()) return "Course name is required";
        if (courseNameText.isEmpty()) return null;
        if (courseNameText.length() > MAX_COURSE_NAME_LENGTH) return "Course name is too long";
        if (hasControlChars(courseNameText) || containsDangerousMarkup(courseNameText)) {
            return "Course name contains unsupported characters";
        }
        return null;
    }

    /**
     * Job description can be long, but still need to prevent HTML/JS snippets from echoing to page.
     */
    public static String validateDescription(String descriptionText, boolean required) {
        if (required && descriptionText.isEmpty()) return "Description is required";
        if (descriptionText.isEmpty()) return null;
        if (descriptionText.length() > MAX_DESCRIPTION_LENGTH) return "Description is too long";
        if (hasControlChars(descriptionText) || containsDangerousMarkup(descriptionText)) {
            return "Description contains unsupported characters";
        }
        return null;
    }

    /**
     * Skills field serves frontend chips, AI detail analysis and CSV storage simultaneously.
     *
     * Here only allows Chinese/English commas to avoid different modules having inconsistent understanding of separators.
     */
    public static String validateSkills(String skillsText, boolean required) {
        if (required && skillsText.isEmpty()) return "Required skills are required";
        if (skillsText.isEmpty()) return null;
        if (skillsText.length() > MAX_SKILLS_LENGTH) return "Required skills are too long";
        if (hasControlChars(skillsText) || containsDangerousMarkup(skillsText)) {
            return "Required skills contain unsupported characters";
        }
        if (skillsText.matches(".*[;；、|].*")) {
            // Keep one separator rule to avoid AI analysis and list display parsing different results.
            return "Please use English commas or Chinese commas to separate skills";
        }
        if (skillsText.matches("(^[,，]|.*[,，]\\s*[,，].*|.*[,，]\\s*$)")) {
            return "Please remove empty skill items";
        }

        List<String> normalizedSkills = JobRequestMapper.normalizeSkillsToList(skillsText);
        if (normalizedSkills.isEmpty()) return "Please remove empty skill items";
        if (normalizedSkills.size() > 20) return "Please list up to 20 skills";
        Set<String> seen = new HashSet<>();
        for (String skill : normalizedSkills) {
            String normalized = skill.toLowerCase().replaceAll("\\s+", " ");
            if (!seen.add(normalized)) {
                return "Duplicate skills found. Please keep each skill only once";
            }
        }
        return null;
    }

    /**
     * Position count must be positive integer, set upper limit to prevent accidentally filling very large values.
     */
    public static String validatePositions(String positionsText, boolean required) {
        if (required && positionsText.isEmpty()) return "Positions must be a whole number";
        if (positionsText.isEmpty()) return null;
        if (!positionsText.matches("^\\d+$")) return "Positions must be a whole number";
        try {
            int pos = Integer.parseInt(positionsText);
            if (pos < 1 || pos > MAX_POSITIONS) {
                return "Positions must be between 1 and " + MAX_POSITIONS;
            }
        } catch (NumberFormatException e) {
            return "Invalid positions number";
        }
        return null;
    }

    /**
     * Weekly hours allow one decimal place, range limited by TA regular workload.
     */
    public static String validateWeeklyHours(String weeklyHoursText, boolean required) {
        if (required && weeklyHoursText.isEmpty()) return "Weekly hours are required";
        if (weeklyHoursText.isEmpty()) return null;
        if (!weeklyHoursText.matches("^\\d+(?:\\.\\d)?$")) {
            return "Weekly hours must be a number with at most one decimal place";
        }
        Double weeklyHours = JobRequestMapper.parseWeeklyHours(weeklyHoursText);
        if (weeklyHours == null) return "Invalid weekly hours";
        if (weeklyHours < MIN_WEEKLY_HOURS || weeklyHours > MAX_WEEKLY_HOURS) {
            return "Weekly hours must be between 0.5 and 40";
        }
        return null;
    }

    /**
     * Work date only accepts yyyy-MM-dd, consistent with browser date input output.
     */
    public static String validateWorkDate(String dateText, String label, boolean required) {
        if (required && dateText.isEmpty()) return label + " is required";
        if (dateText.isEmpty()) return null;
        try {
            LocalDate.parse(dateText);
        } catch (Exception e) {
            return label + " must use yyyy-MM-dd";
        }
        return null;
    }

    /**
     * Validate recruitment timeline.
     *
     * Application deadline should be before or equal to start date, end date cannot be before start date.
     */
    public static String validateWorkSchedule(LocalDateTime deadline, LocalDate workStartDate, LocalDate workEndDate, boolean required) {
        if (required && workStartDate == null) return "Work start date is required";
        if (required && workEndDate == null) return "Work end date is required";
        if (required && deadline == null) return "Application deadline is required";
        if (deadline != null && workStartDate != null && workStartDate.isBefore(deadline.toLocalDate())) {
            // TA can only start work after application deadline; avoid timeline where work starts before deadline.
            return "Work start date cannot be before application deadline";
        }
        if (workStartDate != null && workEndDate != null && workEndDate.isBefore(workStartDate)) {
            return "Work end date cannot be before work start date";
        }
        return null;
    }

    /**
     * Salary field is display text, does not parse currency, but needs length limit and dangerous markup check.
     */
    public static String validateSalary(String salaryText, boolean required) {
        if (required && salaryText.isEmpty()) return "Salary is required";
        if (salaryText.isEmpty()) return null;
        if (salaryText.length() > MAX_SALARY_LENGTH) return "Salary is too long";
        if (hasControlChars(salaryText) || containsDangerousMarkup(salaryText)) {
            return "Salary contains unsupported characters";
        }
        return null;
    }

    /**
     * Application deadline uses datetime-local format, and cannot be significantly earlier than current time.
     */
    public static String validateDeadline(String deadlineText, boolean required) {
        if (required && deadlineText.isEmpty()) return "Application deadline is required";
        if (deadlineText.isEmpty()) return null;
        LocalDateTime deadline = JobRequestMapper.parseDeadline(deadlineText);
        if (deadline == null) return "Invalid deadline format";
        if (deadline.isBefore(LocalDateTime.now().minusMinutes(1))) {
            return "Deadline cannot be in the past";
        }
        return null;
    }

    /**
     * Status only accepts enum values defined in Job.Status.
     */
    public static String validateStatus(String statusText, boolean required) {
        if (required && statusText.isEmpty()) return "Status is required";
        if (statusText.isEmpty()) return null;
        try {
            Job.Status.valueOf(statusText.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid status value";
        }
        return null;
    }

    /**
     * Control characters usually come from copy-paste or abnormal input, not suitable for CSV and page display.
     */
    private static boolean hasControlChars(String value) {
        return value != null && value.matches(".*[\\x00-\\x1F\\x7F].*");
    }

    /**
     * Backend fallback blocks obvious HTML/JS injection; frontend still does escapeHtml.
     */
    private static boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // These fields will be echoed to JSP pages; block obvious HTML/JS injection at backend first.
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
                || text.contains("javascript:")
                || text.matches(".*on\\w+\\s*=.*");
    }
}

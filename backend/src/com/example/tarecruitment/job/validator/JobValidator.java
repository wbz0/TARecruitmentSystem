package com.example.tarecruitment.job.validator;

import com.example.tarecruitment.job.mapper.JobRequestMapper;
import com.example.tarecruitment.job.model.Job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JobValidator - 职位表单校验。
 *
 * 负责必填、长度、格式、危险输入和日期前后关系；权限、所属 MO、CSV 写入等业务流程在 JobService。
 * 这些错误文案会直接显示在 MO 发布/编辑职位表单上，所以保持通俗明确。
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
     * 创建职位的整体验证入口。
     *
     * 逐项字段校验之后，再校验截止时间、开工时间和结束时间的整体顺序。
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

        // 创建岗位时大多数字段必填；positions 为空时由 service 兜底为 1。
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
     * 职位标题会显示在列表卡片和详情页，禁止控制字符和明显 HTML。
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
     * 课程代码允许常见字母数字、空格和分隔符，便于支持 SE601 / CS-101 等格式。
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
     * 课程名是展示字段，限制长度和危险标记即可。
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
     * 职位描述可较长，但仍要阻止 HTML/JS 片段回显到页面。
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
     * 技能字段同时服务前端 chips、AI 详情分析和 CSV 存储。
     *
     * 这里只允许中英文逗号，避免不同模块对分隔符理解不一致。
     */
    public static String validateSkills(String skillsText, boolean required) {
        if (required && skillsText.isEmpty()) return "Required skills are required";
        if (skillsText.isEmpty()) return null;
        if (skillsText.length() > MAX_SKILLS_LENGTH) return "Required skills are too long";
        if (hasControlChars(skillsText) || containsDangerousMarkup(skillsText)) {
            return "Required skills contain unsupported characters";
        }
        if (skillsText.matches(".*[;；、|].*")) {
            // 保持一个分隔规则，避免后续 AI 分析和列表展示解析出不同结果。
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
     * 岗位名额必须是正整数，并设上限防止误填极大值。
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
     * 每周工时允许一位小数，范围按 TA 常规工作量限制。
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
     * 工作日期只接受 yyyy-MM-dd，和浏览器 date input 输出保持一致。
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
     * 校验招聘时间线。
     *
     * 申请截止日应早于或等于开工日期，结束日期不能早于开始日期。
     */
    public static String validateWorkSchedule(LocalDateTime deadline, LocalDate workStartDate, LocalDate workEndDate, boolean required) {
        if (required && workStartDate == null) return "Work start date is required";
        if (required && workEndDate == null) return "Work end date is required";
        if (required && deadline == null) return "Application deadline is required";
        if (deadline != null && workStartDate != null && workStartDate.isBefore(deadline.toLocalDate())) {
            // TA 申请截止后才开始工作，避免出现还没截止就已经开工的时间线。
            return "Work start date cannot be before application deadline";
        }
        if (workStartDate != null && workEndDate != null && workEndDate.isBefore(workStartDate)) {
            return "Work end date cannot be before work start date";
        }
        return null;
    }

    /**
     * 薪酬字段是展示文本，不解析币种，但要限制长度和危险标记。
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
     * 申请截止时间使用 datetime-local 格式，并且不能明显早于当前时间。
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
     * 状态只接受 Job.Status 中定义的枚举值。
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
     * 控制字符通常来自复制粘贴或异常输入，不适合进入 CSV 和页面展示。
     */
    private static boolean hasControlChars(String value) {
        return value != null && value.matches(".*[\\x00-\\x1F\\x7F].*");
    }

    /**
     * 后端兜底拦截明显 HTML/JS 注入；前端仍会做 escapeHtml。
     */
    private static boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // 这些字段会回显到 JSP 页面，先在后端挡掉明显 HTML/JS 注入。
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
                || text.contains("javascript:")
                || text.matches(".*on\\w+\\s*=.*");
    }
}

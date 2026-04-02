package com.example.authlogin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Job实体类 - 职位
 * 存储MO发布的职位信息
 */
public class Job {

    private String jobId;
    private String moId;                 // 发布职位的MO用户ID
    private String moName;               // MO姓名
    private String title;                // 职位标题
    private String courseCode;           // 课程代码
    private String courseName;           // 课程名称
    private String description;          // 职位描述
    private List<String> requiredSkills; // 必需技能列表
    private int positions;               // 职位数量
    private String workload;             // 工作量（如：10小时/周）
    private String salary;               // 薪资
    private LocalDateTime deadline;     // 申请截止日期
    private Status status;               // 职位状态
    private LocalDateTime createdAt;     // 创建时间
    private LocalDateTime updatedAt;    // 更新时间

    public enum Status {
        OPEN, CLOSED, FILLED
    }

    public Job() {
        this.jobId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.requiredSkills = new ArrayList<>();
        this.status = Status.OPEN;
    }

    public Job(String moId, String moName, String title, String courseCode) {
        this();
        this.moId = moId;
        this.moName = moName;
        this.title = title;
        this.courseCode = courseCode;
    }

    // Getters and Setters
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }

    public String getMoName() {
        return moName;
    }

    public void setMoName(String moName) {
        this.moName = moName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
    }

    public String getRequiredSkillsAsString() {
        return requiredSkills != null ? String.join(";", requiredSkills) : "";
    }

    public void setRequiredSkillsFromString(String skillsStr) {
        if (skillsStr != null && !skillsStr.isEmpty()) {
            this.requiredSkills = new ArrayList<>(Arrays.asList(skillsStr.split(";")));
        } else {
            this.requiredSkills = new ArrayList<>();
        }
    }

    public int getPositions() {
        return positions;
    }

    public void setPositions(int positions) {
        this.positions = positions;
    }

    public String getWorkload() {
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 转换为CSV格式存储
     */
    public String toCsv() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.join(",",
            escapeCsv(jobId),
            escapeCsv(moId),
            escapeCsv(moName),
            escapeCsv(title),
            escapeCsv(courseCode),
            escapeCsv(courseName != null ? courseName : ""),
            escapeCsv(description != null ? description : ""),
            escapeCsv(getRequiredSkillsAsString()),
            String.valueOf(positions),
            escapeCsv(workload != null ? workload : ""),
            escapeCsv(salary != null ? salary : ""),
            deadline != null ? deadline.format(formatter) : "",
            status != null ? status.name() : "OPEN",
            createdAt != null ? createdAt.format(formatter) : "",
            updatedAt != null ? updatedAt.format(formatter) : ""
        );
    }

    /**
     * 从CSV格式解析
     */
    public static Job fromCsv(String csvLine) {
        String[] parts = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 8) {
            return null;
        }

        Job job = new Job();
        job.setJobId(unescapeCsv(parts[0]));
        job.setMoId(unescapeCsv(parts[1]));
        job.setMoName(unescapeCsv(parts[2]));
        job.setTitle(unescapeCsv(parts[3]));
        job.setCourseCode(unescapeCsv(parts[4]));
        job.setCourseName(parts.length > 5 ? unescapeCsv(parts[5]) : "");
        job.setDescription(parts.length > 6 ? unescapeCsv(parts[6]) : "");
        job.setRequiredSkillsFromString(parts.length > 7 ? unescapeCsv(parts[7]) : "");

        if (parts.length > 8) {
            try {
                job.setPositions(Integer.parseInt(parts[8].trim()));
            } catch (NumberFormatException e) {
                job.setPositions(1);
            }
        }

        if (parts.length > 9) job.setWorkload(unescapeCsv(parts[9]));
        if (parts.length > 10) job.setSalary(unescapeCsv(parts[10]));

        if (parts.length > 11 && !parts[11].isEmpty()) {
            job.setDeadline(LocalDateTime.parse(parts[11], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        if (parts.length > 12 && !parts[12].isEmpty()) {
            job.setStatus(Status.valueOf(parts[12].trim()));
        }

        if (parts.length > 13 && !parts[13].isEmpty()) {
            job.setCreatedAt(LocalDateTime.parse(parts[13], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (parts.length > 14 && !parts[14].isEmpty()) {
            job.setUpdatedAt(LocalDateTime.parse(parts[14], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return job;
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String unescapeCsv(String value) {
        if (value == null) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            return value.replace("\"\"", "\"");
        }
        return value;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", moId='" + moId + '\'' +
                ", title='" + title + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", status=" + status +
                ", positions=" + positions +
                ", deadline=" + deadline +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return jobId != null && jobId.equals(job.jobId);
    }

    @Override
    public int hashCode() {
        return jobId != null ? jobId.hashCode() : 0;
    }
}

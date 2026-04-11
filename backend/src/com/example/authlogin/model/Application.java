package com.example.authlogin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Application实体类 - 职位申请
 * 存储TA申请人针对职位的申请信息
 */
public class Application {

    private String applicationId;        // 申请ID
    private String jobId;                // 申请的职位ID
    private String applicantId;          // 申请人ID
    private String applicantName;       // 申请人姓名
    private String applicantEmail;      // 申请人邮箱
    private String jobTitle;            // 职位标题（冗余存储，便于显示）
    private String courseCode;           // 课程代码（冗余存储）
    private String moId;                // 发布职位的MO ID
    private String moName;              // MO姓名
    private Status status;              // 申请状态
    private String coverLetter;         // 求职信
    private LocalDateTime appliedAt;    // 申请时间
    private LocalDateTime updatedAt;    // 更新时间
    private LocalDateTime reviewedAt;  // 审核时间

    public enum Status {
        PENDING,    // 待审核
        ACCEPTED,  // 已接受
        REJECTED,  // 已拒绝
        WITHDRAWN  // 已撤回
    }

    public Application() {
        this.applicationId = UUID.randomUUID().toString();
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public Application(String jobId, String applicantId, String applicantName, String applicantEmail) {
        this();
        this.jobId = jobId;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
    }

    // Getters and Setters
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    /**
     * 转换为CSV格式存储
     */
    public String toCsv() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.join(",",
            escapeCsv(applicationId),
            escapeCsv(jobId),
            escapeCsv(applicantId),
            escapeCsv(applicantName),
            escapeCsv(applicantEmail),
            escapeCsv(jobTitle != null ? jobTitle : ""),
            escapeCsv(courseCode != null ? courseCode : ""),
            escapeCsv(moId != null ? moId : ""),
            escapeCsv(moName != null ? moName : ""),
            status != null ? status.name() : "PENDING",
            escapeCsv(coverLetter != null ? coverLetter : ""),
            appliedAt != null ? appliedAt.format(formatter) : "",
            updatedAt != null ? updatedAt.format(formatter) : "",
            reviewedAt != null ? reviewedAt.format(formatter) : ""
        );
    }

    /**
     * 从CSV格式解析
     */
    public static Application fromCsv(String csvLine) {
        String[] parts = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 5) {
            return null;
        }

        Application app = new Application();
        app.setApplicationId(unescapeCsv(parts[0]));
        app.setJobId(unescapeCsv(parts[1]));
        app.setApplicantId(unescapeCsv(parts[2]));
        app.setApplicantName(unescapeCsv(parts[3]));
        app.setApplicantEmail(unescapeCsv(parts[4]));

        if (parts.length > 5) app.setJobTitle(unescapeCsv(parts[5]));
        if (parts.length > 6) app.setCourseCode(unescapeCsv(parts[6]));
        if (parts.length > 7) app.setMoId(unescapeCsv(parts[7]));
        if (parts.length > 8) app.setMoName(unescapeCsv(parts[8]));

        if (parts.length > 9 && !parts[9].isEmpty()) {
            try {
                app.setStatus(Status.valueOf(parts[9].trim()));
            } catch (IllegalArgumentException e) {
                app.setStatus(Status.PENDING);
            }
        }

        if (parts.length > 10) app.setCoverLetter(unescapeCsv(parts[10]));

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (parts.length > 11 && !parts[11].isEmpty()) {
            app.setAppliedAt(LocalDateTime.parse(parts[11], formatter));
        }
        if (parts.length > 12 && !parts[12].isEmpty()) {
            app.setUpdatedAt(LocalDateTime.parse(parts[12], formatter));
        }
        if (parts.length > 13 && !parts[13].isEmpty()) {
            app.setReviewedAt(LocalDateTime.parse(parts[13], formatter));
        }

        return app;
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
        return "Application{" +
                "applicationId='" + applicationId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", applicantId='" + applicantId + '\'' +
                ", applicantName='" + applicantName + '\'' +
                ", status=" + status +
                ", appliedAt=" + appliedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return applicationId != null && applicationId.equals(that.applicationId);
    }

    @Override
    public int hashCode() {
        return applicationId != null ? applicationId.hashCode() : 0;
    }
}

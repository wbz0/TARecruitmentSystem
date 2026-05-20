package com.example.tarecruitment.application.model;

import com.example.tarecruitment.common.storage.CsvCodec;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Application 实体 - TA 对某个职位的一次申请。
 *
 * 这个类同时承担 CSV 序列化/反序列化，因此字段顺序是持久化契约。
 * jobTitle/courseCode/moName 等字段是冗余显示字段，用来让历史申请列表不依赖实时职位表。
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
    /** 申请流程阶段，与 {@link #status} 解耦：status 表示最终结果汇总，阶段表示当前进度。 */
    private ProgressStage progressStage;
    private LocalDateTime reviewStartedAt;      // 材料审核开始
    private LocalDateTime interviewScheduledAt; // 面试已安排
    private LocalDateTime finalDecisionAt;    // 最终决定时间（录用/拒绝/撤回）

    public enum Status {
        PENDING,    // 待审核
        ACCEPTED,  // 已接受
        REJECTED,  // 已拒绝
        WITHDRAWN  // 已撤回
    }

    /**
     * 流程阶段：提交后即进入审核中，结束后为已完成。
     */
    public enum ProgressStage {
        UNDER_REVIEW,
        INTERVIEW_SCHEDULED,
        COMPLETED
    }

    public Application() {
        this.applicationId = UUID.randomUUID().toString();
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = Status.PENDING;
        this.progressStage = ProgressStage.UNDER_REVIEW;
        this.reviewStartedAt = this.appliedAt;
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

    public ProgressStage getProgressStage() {
        return progressStage;
    }

    public void setProgressStage(ProgressStage progressStage) {
        this.progressStage = progressStage;
    }

    public LocalDateTime getReviewStartedAt() {
        return reviewStartedAt;
    }

    public void setReviewStartedAt(LocalDateTime reviewStartedAt) {
        this.reviewStartedAt = reviewStartedAt;
    }

    public LocalDateTime getInterviewScheduledAt() {
        return interviewScheduledAt;
    }

    public void setInterviewScheduledAt(LocalDateTime interviewScheduledAt) {
        this.interviewScheduledAt = interviewScheduledAt;
    }

    public LocalDateTime getFinalDecisionAt() {
        return finalDecisionAt;
    }

    public void setFinalDecisionAt(LocalDateTime finalDecisionAt) {
        this.finalDecisionAt = finalDecisionAt;
    }

    /**
     * 转换为CSV格式存储
     *
     * 字段顺序就是持久化协议；新增字段只能追加在末尾。
     */
    public String toCsv() {
        // 新列只能追加在末尾，不能插入中间，否则旧 CSV 数据会被错列读取。
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
            reviewedAt != null ? reviewedAt.format(formatter) : "",
            progressStage != null ? progressStage.name() : ProgressStage.UNDER_REVIEW.name(),
            reviewStartedAt != null ? reviewStartedAt.format(formatter) : "",
            interviewScheduledAt != null ? interviewScheduledAt.format(formatter) : "",
            finalDecisionAt != null ? finalDecisionAt.format(formatter) : ""
        );
    }

    /**
     * 从CSV格式解析
     *
     * 解析时允许旧行缺少尾部字段，避免历史演示数据因为新增进度列而失效。
     */
    public static Application fromCsv(String csvLine) {
        // 兼容旧 CSV：早期文件只有前 14 列，没有 progressStage 和阶段时间。
        String[] parts = CsvCodec.split(csvLine);
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

        if (parts.length > 14 && !parts[14].isEmpty()) {
            try {
                app.setProgressStage(parseProgressStage(parts[14]));
            } catch (IllegalArgumentException e) {
                app.setProgressStage(null);
            }
        }
        if (parts.length > 15 && !parts[15].isEmpty()) {
            app.setReviewStartedAt(LocalDateTime.parse(parts[15], formatter));
        }
        if (parts.length > 16 && !parts[16].isEmpty()) {
            app.setInterviewScheduledAt(LocalDateTime.parse(parts[16], formatter));
        }
        if (parts.length > 17 && !parts[17].isEmpty()) {
            app.setFinalDecisionAt(LocalDateTime.parse(parts[17], formatter));
        }

        // 遗留兼容：旧 CSV 仅有 14 列（至 reviewedAt），需按 status 推断流程阶段。
        if (parts.length <= 14) {
            applyLegacyProgressInference(app);
        }
        normalizePendingProgress(app);

        return app;
    }

    /**
     * 旧版 CSV 无阶段列时，根据 status / reviewedAt 推断阶段与时间。
     */
    private static void applyLegacyProgressInference(Application app) {
        if (app.getStatus() != null && app.getStatus() != Status.PENDING) {
            app.setProgressStage(ProgressStage.COMPLETED);
            if (app.getFinalDecisionAt() == null && app.getReviewedAt() != null) {
                app.setFinalDecisionAt(app.getReviewedAt());
            }
        } else {
            app.setProgressStage(ProgressStage.UNDER_REVIEW);
            if (app.getReviewStartedAt() == null) {
                app.setReviewStartedAt(app.getAppliedAt());
            }
        }
    }

    /**
     * 统一待处理申请的进度阶段。
     */
    private static void normalizePendingProgress(Application app) {
        if (app == null || app.getStatus() != Status.PENDING) {
            return;
        }
        if (app.getProgressStage() == null) {
            app.setProgressStage(ProgressStage.UNDER_REVIEW);
        }
        if (app.getReviewStartedAt() == null) {
            app.setReviewStartedAt(app.getAppliedAt());
        }
    }

    private static ProgressStage parseProgressStage(String value) {
        String normalized = value != null ? value.trim() : "";
        if ("SUBMITTED".equals(normalized)) {
            return ProgressStage.UNDER_REVIEW;
        }
        return ProgressStage.valueOf(normalized);
    }

    private static String escapeCsv(String value) {
        return CsvCodec.escape(value);
    }

    private static String unescapeCsv(String value) {
        return CsvCodec.unescape(value);
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

package com.example.tarecruitment.application.model;

import com.example.tarecruitment.common.storage.CsvCodec;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Application entity - A TA's application for a specific job.
 *
 * This class also handles CSV serialization/deserialization, so field order is the persistence contract.
 * jobTitle/courseCode/moName etc. are redundant display fields, used to keep historical application list
 * independent of the real-time job table.
 */
public class Application {

    private String applicationId;        // Application ID
    private String jobId;                // Job ID applied for
    private String applicantId;          // Applicant ID
    private String applicantName;       // Applicant name
    private String applicantEmail;      // Applicant email
    private String jobTitle;            // Job title (redundant storage for display)
    private String courseCode;           // Course code (redundant storage)
    private String moId;                // MO ID who posted the job
    private String moName;              // MO name
    private Status status;              // Application status
    private String coverLetter;         // Cover letter
    private LocalDateTime appliedAt;    // Application time
    private LocalDateTime updatedAt;    // Update time
    private LocalDateTime reviewedAt;  // Review time
    /** Application process stage, decoupled from {@link #status}: status represents final result summary, stage represents current progress. */
    private ProgressStage progressStage;
    private LocalDateTime reviewStartedAt;      // Material review started
    private LocalDateTime interviewScheduledAt; // Interview scheduled
    private LocalDateTime finalDecisionAt;    // Final decision time (accept/reject/withdraw)

    public enum Status {
        PENDING,    // Pending review
        ACCEPTED,  // Accepted
        REJECTED,  // Rejected
        WITHDRAWN  // Withdrawn
    }

    /**
     * Process stage: enters review after submission, completed when finished.
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
     * Convert to CSV format for storage.
     *
     * Field order is the persistence contract; new fields can only be appended at the end.
     */
    public String toCsv() {
        // New columns can only be appended at the end, not inserted in the middle,
        // otherwise old CSV data will be misread by column offset.
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
     * Parse from CSV format.
     *
     * Parsing allows old rows to lack trailing fields to avoid historical demo data
     * becoming invalid due to new progress columns.
     */
    public static Application fromCsv(String csvLine) {
        // Compatible with old CSV: early files only have 14 columns, no progressStage and stage times.
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

        // Legacy compatibility: old CSV has only 14 columns (up to reviewedAt), need to infer progress stage from status.
        if (parts.length <= 14) {
            applyLegacyProgressInference(app);
        }
        normalizePendingProgress(app);

        return app;
    }

    /**
     * When old CSV has no stage column, infer stage and time from status / reviewedAt.
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
     * Normalize pending application's progress stage.
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

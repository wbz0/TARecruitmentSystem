package com.example.tarecruitment.job.model;

import com.example.tarecruitment.common.storage.CsvCodec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Job entity class - Position published by MO.
 *
 * It is both business object and jobs.csv serialization contract. New fields should be appended to CSV end,
 * so old data can still be read by fromCsv().
 */
public class Job {

    private String jobId;
    private String moId;                 // MO user ID who published the job
    private String moName;               // MO name
    private String title;                // Job title
    private String courseCode;           // Course code
    private String courseName;           // Course name
    private String description;          // Job description
    private List<String> requiredSkills; // Required skills list
    private int positions;               // Number of positions
    private String workload;             // Legacy/to be removed: old workload text, frontend now uses weeklyHours + workStartDate/workEndDate
    private Double weeklyHours;          // Weekly work hours
    private LocalDate workStartDate;     // Work start date
    private LocalDate workEndDate;       // Work end date
    private String salary;               // Salary
    private LocalDateTime deadline;     // Application deadline
    private Status status;               // Job status
    private LocalDateTime createdAt;     // Created time
    private LocalDateTime updatedAt;    // Updated time

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
        return requiredSkills != null ? String.join(", ", requiredSkills) : "";
    }

    public void setRequiredSkillsFromString(String skillsStr) {
        if (skillsStr != null && !skillsStr.isEmpty()) {
            this.requiredSkills = new ArrayList<>();
            Arrays.stream(skillsStr.split("[,，;；]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.requiredSkills::add);
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
        if (weeklyHours != null) {
            return formatWeeklyHours(weeklyHours) + " hours / week";
        }
        // Compatible with old CSV: if no structured weeklyHours, fall back to historical workload text display.
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    public Double getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(Double weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public LocalDate getWorkStartDate() {
        return workStartDate;
    }

    public void setWorkStartDate(LocalDate workStartDate) {
        this.workStartDate = workStartDate;
    }

    public LocalDate getWorkEndDate() {
        return workEndDate;
    }

    public void setWorkEndDate(LocalDate workEndDate) {
        this.workEndDate = workEndDate;
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

    /**
     * Return effective status for external display/filtering.
     * Rules:
     * 1. FILLED takes priority;
     * 2. Manual CLOSED takes priority;
     * 3. OPEN past deadline treated as CLOSED;
     * 4. Other cases treated as OPEN.
     */
    public Status getEffectiveStatus() {
        return getEffectiveStatus(LocalDateTime.now());
    }

    public Status getEffectiveStatus(LocalDateTime referenceTime) {
        if (status == Status.FILLED) {
            return Status.FILLED;
        }
        if (status == Status.CLOSED) {
            return Status.CLOSED;
        }
        LocalDateTime now = referenceTime != null ? referenceTime : LocalDateTime.now();
        if (deadline != null && deadline.isBefore(now)) {
            return Status.CLOSED;
        }
        return Status.OPEN;
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
     * Convert to CSV format for storage.
     *
     * Field order must align with JobDao.CSV_HEADER; subsequent new fields only appended, not inserted in middle.
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
            escapeCsv(getWorkload() != null ? getWorkload() : ""),
            escapeCsv(salary != null ? salary : ""),
            deadline != null ? deadline.format(formatter) : "",
            status != null ? status.name() : "OPEN",
            createdAt != null ? createdAt.format(formatter) : "",
            updatedAt != null ? updatedAt.format(formatter) : "",
            weeklyHours != null ? formatWeeklyHours(weeklyHours) : "",
            workStartDate != null ? workStartDate.toString() : "",
            workEndDate != null ? workEndDate.toString() : ""
        );
    }

    /**
     * Parse from CSV format.
     *
     * fromCsv allows missing trailing fields to be compatible with early jobs.csv that only saved workload text.
     */
    public static Job fromCsv(String csvLine) {
        String[] parts = CsvCodec.split(csvLine);
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
        if (parts.length > 15 && !parts[15].isEmpty()) {
            try {
                job.setWeeklyHours(Double.parseDouble(parts[15].trim()));
            } catch (NumberFormatException ignored) {
                job.setWeeklyHours(null);
            }
        }
        if (parts.length > 16 && !parts[16].isEmpty()) {
            try {
                job.setWorkStartDate(LocalDate.parse(parts[16].trim()));
            } catch (Exception ignored) {
                job.setWorkStartDate(null);
            }
        }
        if (parts.length > 17 && !parts[17].isEmpty()) {
            try {
                job.setWorkEndDate(LocalDate.parse(parts[17].trim()));
            } catch (Exception ignored) {
                job.setWorkEndDate(null);
            }
        }

        return job;
    }

    public static String formatWeeklyHours(Double value) {
        if (value == null) {
            return "";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static String escapeCsv(String value) {
        return CsvCodec.escape(value);
    }

    private static String unescapeCsv(String value) {
        return CsvCodec.unescape(value);
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
                ", weeklyHours=" + weeklyHours +
                ", workStartDate=" + workStartDate +
                ", workEndDate=" + workEndDate +
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

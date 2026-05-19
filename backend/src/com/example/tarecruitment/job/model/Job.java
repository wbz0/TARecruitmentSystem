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
 * Job 实体类 - MO 发布的职位。
 *
 * 它既是业务对象，也是 jobs.csv 的序列化契约。新增字段要追加到 CSV 末尾，
 * 这样旧数据仍能被 fromCsv() 读取。
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
    private String workload;             // 遗留/待移除：旧版工作量文本，当前前端改用 weeklyHours + workStartDate/workEndDate
    private Double weeklyHours;          // 每周工作小时数
    private LocalDate workStartDate;     // 工作开始日期
    private LocalDate workEndDate;       // 工作结束日期
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
        // 兼容旧 CSV：如果没有结构化 weeklyHours，就退回历史 workload 文本展示。
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
     * 返回对外展示/筛选时应采用的生效状态。
     * 规则：
     * 1. FILLED 优先保留；
     * 2. 手动 CLOSED 优先保留；
     * 3. 截止时间已过的 OPEN 视为 CLOSED；
     * 4. 其他情况视为 OPEN。
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
     * 转换为 CSV 格式存储。
     *
     * 字段顺序必须和 JobDao.CSV_HEADER 对齐；后续新增字段只追加，不插到中间。
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
     * 从 CSV 格式解析。
     *
     * fromCsv 允许缺少末尾字段，是为了兼容早期只保存 workload 文本的 jobs.csv。
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

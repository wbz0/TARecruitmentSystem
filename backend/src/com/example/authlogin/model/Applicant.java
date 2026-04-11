package com.example.authlogin.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Applicant实体类 - TA申请人档案
 * 存储TA申请人的详细信息
 */
public class Applicant {

    private String applicantId;
    private String userId;           // 关联的User ID
    private String fullName;         // 姓名
    private String studentId;        // 学号
    private String department;       // 院系
    private String program;          // 项目（本科/硕士/博士）
    private String gpa;              // GPA
    private List<String> skills;     // 技能列表（逗号分隔）
    private String resumePath;       // 简历文件路径
    private String phone;            // 电话
    private String address;          // 地址
    private String experience;       // 相关经验
    private String motivation;       // 申请动机
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    public Applicant() {
        this.applicantId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.skills = new ArrayList<>();
    }

    public Applicant(String userId, String fullName, String studentId) {
        this();
        this.userId = userId;
        this.fullName = fullName;
        this.studentId = studentId;
    }

    // Getters and Setters
    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getGpa() {
        return gpa;
    }

    public void setGpa(String gpa) {
        this.gpa = gpa;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public String getSkillsAsString() {
        return skills != null ? String.join(";", skills) : "";
    }

    public void setSkillsFromString(String skillsStr) {
        if (skillsStr != null && !skillsStr.isEmpty()) {
            this.skills = new ArrayList<>(Arrays.asList(skillsStr.split(";")));
        } else {
            this.skills = new ArrayList<>();
        }
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
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
            escapeCsv(applicantId),
            escapeCsv(userId),
            escapeCsv(fullName),
            escapeCsv(studentId),
            escapeCsv(department),
            escapeCsv(program),
            escapeCsv(gpa),
            escapeCsv(getSkillsAsString()),
            escapeCsv(resumePath != null ? resumePath : ""),
            escapeCsv(phone != null ? phone : ""),
            escapeCsv(address != null ? address : ""),
            escapeCsv(experience != null ? experience : ""),
            escapeCsv(motivation != null ? motivation : ""),
            createdAt != null ? createdAt.format(formatter) : "",
            updatedAt != null ? updatedAt.format(formatter) : ""
        );
    }

    /**
     * 从CSV格式解析
     */
    public static Applicant fromCsv(String csvLine) {
        String[] parts = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 8) {
            return null;
        }

        Applicant applicant = new Applicant();
        applicant.setApplicantId(unescapeCsv(parts[0]));
        applicant.setUserId(unescapeCsv(parts[1]));
        applicant.setFullName(unescapeCsv(parts[2]));
        applicant.setStudentId(unescapeCsv(parts[3]));
        applicant.setDepartment(unescapeCsv(parts[4]));
        applicant.setProgram(unescapeCsv(parts[5]));
        applicant.setGpa(unescapeCsv(parts[6]));
        applicant.setSkillsFromString(parts.length > 7 ? unescapeCsv(parts[7]) : "");

        if (parts.length > 8) applicant.setResumePath(unescapeCsv(parts[8]));
        if (parts.length > 9) applicant.setPhone(unescapeCsv(parts[9]));
        if (parts.length > 10) applicant.setAddress(unescapeCsv(parts[10]));
        if (parts.length > 11) applicant.setExperience(unescapeCsv(parts[11]));
        if (parts.length > 12) applicant.setMotivation(unescapeCsv(parts[12]));

        if (parts.length > 13 && !parts[13].isEmpty()) {
            applicant.setCreatedAt(LocalDateTime.parse(parts[13], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (parts.length > 14 && !parts[14].isEmpty()) {
            applicant.setUpdatedAt(LocalDateTime.parse(parts[14], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return applicant;
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
        return "Applicant{" +
                "applicantId='" + applicantId + '\'' +
                ", userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", studentId='" + studentId + '\'' +
                ", department='" + department + '\'' +
                ", program='" + program + '\'' +
                ", gpa='" + gpa + '\'' +
                ", skills=" + skills +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Applicant applicant = (Applicant) o;
        return applicantId != null && applicantId.equals(applicant.applicantId);
    }

    @Override
    public int hashCode() {
        return applicantId != null ? applicantId.hashCode() : 0;
    }
}

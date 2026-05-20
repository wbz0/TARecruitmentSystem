package com.example.tarecruitment.profile.validator;

/**
 * ApplicantProfileInput - TA 档案表单输入对象。
 *
 * raw 字段保留 request 原始状态，便于区分“字段没传”和“字段传了空值”；
 * getter 则返回 trim 后的业务值，供 validator/service 使用。
 */
public final class ApplicantProfileInput {

    private final String fullNameRaw;
    private final String studentIdRaw;
    private final String departmentRaw;
    private final String programRaw;
    private final String gpaRaw;
    private final String skillsRaw;
    private final String phoneRaw;
    private final String addressRaw;
    private final String experienceRaw;
    private final String motivationRaw;
    private final boolean removePhoto;

    public ApplicantProfileInput(String fullNameRaw,
                                 String studentIdRaw,
                                 String departmentRaw,
                                 String programRaw,
                                 String gpaRaw,
                                 String skillsRaw,
                                 String phoneRaw,
                                 String addressRaw,
                                 String experienceRaw,
                                 String motivationRaw,
                                 boolean removePhoto) {
        this.fullNameRaw = fullNameRaw;
        this.studentIdRaw = studentIdRaw;
        this.departmentRaw = departmentRaw;
        this.programRaw = programRaw;
        this.gpaRaw = gpaRaw;
        this.skillsRaw = skillsRaw;
        this.phoneRaw = phoneRaw;
        this.addressRaw = addressRaw;
        this.experienceRaw = experienceRaw;
        this.motivationRaw = motivationRaw;
        this.removePhoto = removePhoto;
    }

    public String getFullNameRaw() {
        return fullNameRaw;
    }

    public String getStudentIdRaw() {
        return studentIdRaw;
    }

    public String getDepartmentRaw() {
        return departmentRaw;
    }

    public String getProgramRaw() {
        return programRaw;
    }

    public String getGpaRaw() {
        return gpaRaw;
    }

    public String getSkillsRaw() {
        return skillsRaw;
    }

    public String getPhoneRaw() {
        return phoneRaw;
    }

    public String getAddressRaw() {
        return addressRaw;
    }

    public String getExperienceRaw() {
        return experienceRaw;
    }

    public String getMotivationRaw() {
        return motivationRaw;
    }

    public boolean isRemovePhoto() {
        return removePhoto;
    }

    public String getFullName() {
        return ApplicantProfileValidator.normalizeInput(fullNameRaw);
    }

    public String getStudentId() {
        return ApplicantProfileValidator.normalizeInput(studentIdRaw);
    }

    public String getDepartment() {
        return ApplicantProfileValidator.normalizeInput(departmentRaw);
    }

    public String getProgram() {
        return ApplicantProfileValidator.normalizeInput(programRaw);
    }

    public String getGpa() {
        return ApplicantProfileValidator.normalizeInput(gpaRaw);
    }

    public String getSkills() {
        return ApplicantProfileValidator.normalizeInput(skillsRaw);
    }

    public String getPhone() {
        return ApplicantProfileValidator.normalizeInput(phoneRaw);
    }

    public String getAddress() {
        return ApplicantProfileValidator.normalizeInput(addressRaw);
    }

    public String getExperience() {
        return ApplicantProfileValidator.normalizeInput(experienceRaw);
    }

    public String getMotivation() {
        return ApplicantProfileValidator.normalizeInput(motivationRaw);
    }
}

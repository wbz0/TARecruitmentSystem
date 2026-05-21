package com.example.tarecruitment.profile.mapper;

import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.profile.service.ProfileAssetService;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ApplicantProfileResponseMapper - TA profile response conversion utility.
 *
 * Uniformly output fields needed by frontend, resume/photo display info and profile completeness.
 * File body not returned in JSON, only path, filename, size and other metadata returned.
 */
public final class ApplicantProfileResponseMapper {

    private ApplicantProfileResponseMapper() {
    }

    public static Map<String, Object> toPayload(Applicant applicant,
                                                HttpSession session,
                                                ProfileAssetService assetService) {
        CompletenessResult completeness = calculateCompleteness(applicant);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicantId", applicant.getApplicantId());
        data.put("userId", applicant.getUserId());
        data.put("fullName", applicant.getFullName());
        data.put("studentId", applicant.getStudentId());
        data.put("department", applicant.getDepartment() != null ? applicant.getDepartment() : "");
        data.put("program", applicant.getProgram() != null ? applicant.getProgram() : "");
        data.put("gpa", applicant.getGpa() != null ? applicant.getGpa() : "");
        data.put("skills", applicant.getSkillsAsString());
        appendStoredResumePayload(data, applicant.getResumePath(), "", assetService);
        appendStoredPhotoPayload(data, applicant.getPhotoPath(), "", assetService);
        data.put("phone", applicant.getPhone() != null ? applicant.getPhone() : "");
        data.put("address", applicant.getAddress() != null ? applicant.getAddress() : "");
        data.put("experience", applicant.getExperience() != null ? applicant.getExperience() : "");
        data.put("motivation", applicant.getMotivation() != null ? applicant.getMotivation() : "");
        data.put("completeness", completeness.completeness);
        data.put("missingFields", completeness.missingFields);
        // Even if profile already exists, still include current session's pending resume draft for frontend.
        data.putAll(draftResumePayload(session, assetService));
        return data;
    }

    public static Map<String, Object> savedPayload(Applicant applicant,
                                                   String resumeFallbackName,
                                                   String photoFallbackName,
                                                   ProfileAssetService assetService) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicantId", applicant.getApplicantId());
        appendStoredResumePayload(data, applicant.getResumePath(), resumeFallbackName, assetService);
        appendStoredPhotoPayload(data, applicant.getPhotoPath(), photoFallbackName, assetService);
        return data;
    }

    public static Map<String, Object> draftResumePayload(HttpSession session, ProfileAssetService assetService) {
        Map<String, Object> data = new LinkedHashMap<>();
        String pendingResumePath = assetService.getDraftResumePath(session);
        // pendingResume* only indicates session draft, not yet written to applicant.resumePath.
        data.put("pendingResumePath", pendingResumePath);
        data.put("pendingResumeName", assetService.buildDisplayFileName(pendingResumePath, assetService.getDraftResumeName(session)));
        data.put("pendingResumeSize", assetService.getStoredFileSize(pendingResumePath));
        data.put("hasPendingResume", assetService.hasDraftResume(session));
        return data;
    }

    public static void appendStoredResumePayload(Map<String, Object> data,
                                                 String resumePath,
                                                 String fallbackName,
                                                 ProfileAssetService assetService) {
        String safeResumePath = resumePath != null ? resumePath : "";
        data.put("resumePath", safeResumePath);
        data.put("resumeName", assetService.buildDisplayFileName(safeResumePath, fallbackName));
        data.put("resumeSize", assetService.getStoredFileSize(safeResumePath));
    }

    public static void appendStoredPhotoPayload(Map<String, Object> data,
                                                String photoPath,
                                                String fallbackName,
                                                ProfileAssetService assetService) {
        String safePhotoPath = photoPath != null ? photoPath : "";
        data.put("photoPath", safePhotoPath);
        data.put("photoName", assetService.buildDisplayFileName(safePhotoPath, fallbackName));
        data.put("photoSize", assetService.getStoredFileSize(safePhotoPath));
    }

    private static CompletenessResult calculateCompleteness(Applicant applicant) {
        // Completeness is for TA dashboard hints only, not involved in backend permission or application eligibility checks.
        int totalFields = 12;
        int filledFields = 0;
        List<String> missingFields = new ArrayList<>();

        if (isNotEmpty(applicant.getFullName())) filledFields++;
        else missingFields.add("fullName");

        if (isNotEmpty(applicant.getStudentId())) filledFields++;
        else missingFields.add("studentId");

        if (isNotEmpty(applicant.getDepartment())) filledFields++;
        else missingFields.add("department");

        if (isNotEmpty(applicant.getProgram())) filledFields++;
        else missingFields.add("program");

        if (isNotEmpty(applicant.getGpa())) filledFields++;
        else missingFields.add("gpa");

        if (applicant.getSkills() != null && !applicant.getSkills().isEmpty()) filledFields++;
        else missingFields.add("skills");

        if (isNotEmpty(applicant.getResumePath())) filledFields++;
        else missingFields.add("resume");

        if (isNotEmpty(applicant.getPhone())) filledFields++;
        else missingFields.add("phone");

        if (isNotEmpty(applicant.getAddress())) filledFields++;
        else missingFields.add("address");

        if (isNotEmpty(applicant.getExperience())) filledFields++;
        else missingFields.add("experience");

        if (isNotEmpty(applicant.getMotivation())) filledFields++;
        else missingFields.add("motivation");

        int completeness = (int) Math.round((double) filledFields / totalFields * 100);
        return new CompletenessResult(completeness, missingFields);
    }

    private static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class CompletenessResult {
        private final int completeness;
        private final List<String> missingFields;

        private CompletenessResult(int completeness, List<String> missingFields) {
            this.completeness = completeness;
            this.missingFields = missingFields;
        }
    }
}

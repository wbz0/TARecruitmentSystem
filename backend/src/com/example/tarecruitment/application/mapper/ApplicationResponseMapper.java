package com.example.tarecruitment.application.mapper;

import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.search.FuzzySearchUtil;
import com.example.tarecruitment.common.web.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts Application domain object to JSON payload required by frontend page.
 *
 * Service layer is only responsible for business results; list fields, search hint fields and time strings
 * are uniformly organized here, so TA/MO/Admin pages won't each guess CSV field meanings.
 */
public final class ApplicationResponseMapper {

    private ApplicationResponseMapper() {
    }

    /**
     * Application list response.
     *
     * Three types of role pages share the applications array; search metadata only handles frontend hints
     * “whether keywords were used / whether there are only approximate results”.
     */
    public static Map<String, Object> toListPayload(List<Application> applications,
                                                    FuzzySearchUtil.SearchOutcome<Application> searchOutcome) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (applications != null) {
            for (Application application : applications) {
                items.add(toPayload(application));
            }
        }
        int total = applications == null ? 0 : applications.size();
        return ApiResponses.objectMap(
                "applications", items,
                "total", total,
                "keywordApplied", searchOutcome != null && searchOutcome.isKeywordApplied(),
                "approximateOnly", searchOutcome != null && searchOutcome.isApproximateOnly(),
                "hasMatches", searchOutcome != null && searchOutcome.hasMatches()
        );
    }

    /**
     * Determine which fields fuzzy search covers based on role.
     *
     * This way TA searches job-related information, MO searches candidate-related information, Admin searches all audit information.
     */
    public static List<String> searchFieldsForRole(Application application, User.Role role) {
        List<String> fields = new ArrayList<>();
        if (application == null || role == null) {
            return fields;
        }

        // Search fields are consistent with role perspective: TA searches jobs, MO searches candidates, Admin searches global information.
        if (role == User.Role.TA) {
            fields.add(application.getJobTitle());
            fields.add(application.getCourseCode());
            fields.add(application.getMoName());
            return fields;
        }

        if (role == User.Role.MO) {
            fields.add(application.getApplicantName());
            fields.add(application.getApplicantEmail());
            fields.add(application.getJobTitle());
            return fields;
        }

        fields.add(application.getApplicantName());
        fields.add(application.getApplicantEmail());
        fields.add(application.getJobTitle());
        fields.add(application.getCourseCode());
        fields.add(application.getMoName());
        return fields;
    }

    /**
     * Single application response.
     *
     * Here outputs page field names, not directly exposing Application CSV field order.
     */
    public static Map<String, Object> toPayload(Application app) {
        return ApiResponses.objectMap(
                "applicationId", safeText(app.getApplicationId()),
                "jobId", safeText(app.getJobId()),
                "applicantId", safeText(app.getApplicantId()),
                "applicantName", safeText(app.getApplicantName()),
                "applicantEmail", safeText(app.getApplicantEmail()),
                "jobTitle", safeText(app.getJobTitle()),
                "courseCode", safeText(app.getCourseCode()),
                "moId", safeText(app.getMoId()),
                "moName", safeText(app.getMoName()),
                "status", app.getStatus() != null ? app.getStatus().name() : "PENDING",
                "coverLetter", safeText(app.getCoverLetter()),
                "appliedAt", app.getAppliedAt() != null ? app.getAppliedAt().toString() : "",
                "updatedAt", app.getUpdatedAt() != null ? app.getUpdatedAt().toString() : "",
                "reviewedAt", app.getReviewedAt() != null ? app.getReviewedAt().toString() : "",
                "progressStage", app.getProgressStage() != null ? app.getProgressStage().name() : "UNDER_REVIEW",
                // Stage time is used for application progress timeline; some old CSVs don't have values, frontend will handle as empty string.
                "reviewStartedAt", app.getReviewStartedAt() != null ? app.getReviewStartedAt().toString() : "",
                "interviewScheduledAt", app.getInterviewScheduledAt() != null ? app.getInterviewScheduledAt().toString() : "",
                "finalDecisionAt", app.getFinalDecisionAt() != null ? app.getFinalDecisionAt().toString() : ""
        );
    }

    private static String safeText(String value) {
        return value != null ? value : "";
    }
}

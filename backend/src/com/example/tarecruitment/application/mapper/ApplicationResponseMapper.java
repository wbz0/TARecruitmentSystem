package com.example.tarecruitment.application.mapper;

import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.search.FuzzySearchUtil;
import com.example.tarecruitment.common.web.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把 Application 领域对象转换成前端页面需要的 JSON payload。
 *
 * service 层只负责业务结果；列表字段、搜索提示字段和时间字符串在这里统一整理，
 * 这样 TA/MO/Admin 页面不会各自猜 CSV 字段含义。
 */
public final class ApplicationResponseMapper {

    private ApplicationResponseMapper() {
    }

    /**
     * 申请列表响应。
     *
     * 三类角色页面共用 applications 数组；搜索元信息只负责前端提示
     * “是否用了关键词/是否只有近似结果”。
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
     * 按角色决定模糊搜索覆盖哪些字段。
     *
     * 这样 TA 搜岗位相关信息，MO 搜候选人相关信息，Admin 搜全量审计信息。
     */
    public static List<String> searchFieldsForRole(Application application, User.Role role) {
        List<String> fields = new ArrayList<>();
        if (application == null || role == null) {
            return fields;
        }

        // 搜索字段跟角色视角保持一致：TA 搜岗位，MO 搜候选人，Admin 搜全局信息。
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
     * 单条申请响应。
     *
     * 这里输出的是页面字段名，不直接暴露 Application CSV 字段顺序。
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
                // 阶段时间用于申请进度时间线；部分旧 CSV 没有值，前端会按空字符串处理。
                "reviewStartedAt", app.getReviewStartedAt() != null ? app.getReviewStartedAt().toString() : "",
                "interviewScheduledAt", app.getInterviewScheduledAt() != null ? app.getInterviewScheduledAt().toString() : "",
                "finalDecisionAt", app.getFinalDecisionAt() != null ? app.getFinalDecisionAt().toString() : ""
        );
    }

    private static String safeText(String value) {
        return value != null ? value : "";
    }
}

package com.example.tarecruitment.ai.web;

import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.ai.service.MoApplicantAiSearchService;
import com.example.tarecruitment.ai.service.MoApplicantAiSearchService.RecommendedApplication;
import com.example.tarecruitment.ai.service.MoApplicantAiSearchService.SearchResult;
import com.example.tarecruitment.ai.client.DeepSeekAiConfig;
import com.example.tarecruitment.ai.client.DeepSeekApplicantSearchClient;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MO dashboard applicant AI search endpoint.
 *
 * The current frontend entry is in mo-dashboard.js, not a standalone JSP.
 * No local fallback is provided here: when DeepSeek is unavailable, returns 503, and the frontend displays “AI temporarily unavailable”.
 * Access path: POST /api/mo/applicant-recommendations
 */
@WebServlet(ApiRoutes.MO_APPLICANT_RECOMMENDATIONS)
public class MoApplicantAiSearchServlet extends HttpServlet {

    private static final int MAX_QUERY_LENGTH = 500;

    private JobDao jobDao;
    private ApplicationDao applicationDao;
    private ApplicantDao applicantDao;
    private MoApplicantAiSearchService aiSearchService;

    @Override
    public void init() throws ServletException {
        jobDao = JobDao.getInstance();
        applicationDao = ApplicationDao.getInstance();
        applicantDao = ApplicantDao.getInstance();
        DeepSeekAiConfig config = DeepSeekAiConfig.load(getServletContext());
        aiSearchService = new MoApplicantAiSearchService(new DeepSeekApplicantSearchClient(config));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApiResponses.write(response, 405, false, "Method not allowed", null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            ApiResponses.write(response, 401, false, "Please login first", null);
            return;
        }
        if (currentUser.getRole() != User.Role.MO) {
            ApiResponses.write(response, 403, false, "Only MO can use applicant AI search", null);
            return;
        }

        String jobId = normalizeId(request.getParameter("jobId"));
        if (jobId.isEmpty()) {
            ApiResponses.write(response, 400, false, "jobId is required", null);
            return;
        }
        if (containsControlChars(jobId) || containsDangerousMarkup(jobId)) {
            ApiResponses.write(response, 400, false, "jobId contains invalid characters", null);
            return;
        }

        String query = request.getParameter("query");
        if (query != null && query.length() > MAX_QUERY_LENGTH) {
            ApiResponses.write(response, 400, false, "query is too long", null);
            return;
        }
        if (containsControlChars(query)) {
            ApiResponses.write(response, 400, false, "query contains invalid characters", null);
            return;
        }

        Optional<Job> jobOpt = jobDao.findById(jobId);
        if (jobOpt.isEmpty()) {
            ApiResponses.write(response, 404, false, "Job not found", null);
            return;
        }
        Job job = jobOpt.get();
        if (job.getMoId() == null || !job.getMoId().equals(currentUser.getUserId())) {
            ApiResponses.write(response, 403, false, "You can only search applicants for your own jobs", null);
            return;
        }

        try {
            List<Application> applications = applicationDao.findByJobId(job.getJobId());
            Map<String, Applicant> applicantsByUserId = loadApplicantProfiles(applications);
            SearchResult result = aiSearchService.search(job, applications, applicantsByUserId, query);

            if (!result.isSuccess()) {
                ApiResponses.write(
                        response,
                        503,
                        false,
                        result.getMessage(),
                        ApiResponses.objectMap("action", result.getAction())
                );
                return;
            }

            Map<String, Object> data = buildResponseData(result);
            ApiResponses.write(response, 200, true, result.getMessage(), data);
        } catch (IllegalArgumentException ex) {
            ApiResponses.write(response, 400, false, ex.getMessage(), null);
        } catch (Exception ex) {
            getServletContext().log("Failed to run MO applicant AI search", ex);
            ApiResponses.write(response, 503, false, MoApplicantAiSearchService.UNAVAILABLE_MESSAGE, null);
        }
    }

    private Map<String, Applicant> loadApplicantProfiles(List<Application> applications) {
        Map<String, Applicant> profiles = new LinkedHashMap<>();
        if (applications == null || applications.isEmpty()) {
            return profiles;
        }
        for (Application application : applications) {
            if (application == null || application.getApplicantId() == null || profiles.containsKey(application.getApplicantId())) {
                continue;
            }
            Optional<Applicant> applicantOpt = applicantDao.findByUserId(application.getApplicantId());
            applicantOpt.ifPresent(applicant -> profiles.put(application.getApplicantId(), applicant));
        }
        return profiles;
    }

    private Map<String, Object> buildResponseData(SearchResult result) {
        List<Map<String, Object>> applications = new ArrayList<>();
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Map<String, Object> recommendationsById = new LinkedHashMap<>();

        for (RecommendedApplication recommended : result.getRecommendations()) {
            Application application = recommended.getApplication();
            if (application == null) {
                continue;
            }
            // Frontend needs both complete application cards and applicationId -> recommendation reason fast lookup.
            applications.add(buildApplicationPayload(application));
            Map<String, Object> recommendation = ApiResponses.objectMap(
                    "applicationId", safeText(application.getApplicationId()),
                    "recommendation", recommended.getRecommendation()
            );
            recommendations.add(recommendation);
            recommendationsById.put(safeText(application.getApplicationId()), recommended.getRecommendation());
        }

        return ApiResponses.objectMap(
                "action", result.getAction(),
                "message", result.getMessage(),
                "applications", applications,
                "recommendations", recommendations,
                "recommendationsByApplicationId", recommendationsById,
                "total", applications.size()
        );
    }

    private Map<String, Object> buildApplicationPayload(Application app) {
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
                "reviewStartedAt", app.getReviewStartedAt() != null ? app.getReviewStartedAt().toString() : "",
                "interviewScheduledAt", app.getInterviewScheduledAt() != null ? app.getInterviewScheduledAt().toString() : "",
                "finalDecisionAt", app.getFinalDecisionAt() != null ? app.getFinalDecisionAt().toString() : ""
        );
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    private String normalizeId(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() > 128 ? "" : trimmed;
    }

    private boolean containsControlChars(String value) {
        return value != null && value.chars().anyMatch(ch -> ch < 32 || ch == 127);
    }

    private boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String lower = value.toLowerCase();
        return lower.contains("<") || lower.contains(">") || lower.contains("javascript:");
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }
}

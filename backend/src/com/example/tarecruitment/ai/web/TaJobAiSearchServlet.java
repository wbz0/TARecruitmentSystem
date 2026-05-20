package com.example.tarecruitment.ai.web;

import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.ai.service.TaJobAiSearchService;
import com.example.tarecruitment.ai.service.TaJobAiSearchService.RecommendedJob;
import com.example.tarecruitment.ai.service.TaJobAiSearchService.SearchResult;
import com.example.tarecruitment.ai.client.DeepSeekAiConfig;
import com.example.tarecruitment.ai.client.DeepSeekTaJobSearchClient;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TA job-list AI recommendation endpoint.
 *
 * 当前前端入口在 ta-job-list.js 的 AI 搜索模式。
 * 只推荐当前开放且当前 TA 尚未申请的职位；DeepSeek 不可用时返回 503，不做本地假推荐。
 * Access path: POST /api/ta/job-recommendations
 */
@WebServlet(ApiRoutes.TA_JOB_RECOMMENDATIONS)
public class TaJobAiSearchServlet extends HttpServlet {

    private static final int MAX_QUERY_LENGTH = 500;

    private JobDao jobDao;
    private ApplicationDao applicationDao;
    private ApplicantDao applicantDao;
    private TaJobAiSearchService aiSearchService;

    @Override
    public void init() throws ServletException {
        jobDao = JobDao.getInstance();
        applicationDao = ApplicationDao.getInstance();
        applicantDao = ApplicantDao.getInstance();
        DeepSeekAiConfig config = DeepSeekAiConfig.load(getServletContext());
        aiSearchService = new TaJobAiSearchService(new DeepSeekTaJobSearchClient(config));
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
        if (currentUser.getRole() != User.Role.TA) {
            ApiResponses.write(response, 403, false, "Only TA can use job AI search", null);
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

        try {
            Optional<Applicant> applicantOpt = applicantDao.findByUserId(currentUser.getUserId());
            if (applicantOpt.isEmpty()) {
                ApiResponses.write(
                        response,
                        400,
                        false,
                        TaJobAiSearchService.PROFILE_REQUIRED_MESSAGE,
                        ApiResponses.objectMap("action", "profile_required")
                );
                return;
            }

            List<Job> jobs = loadApplicableOpenJobs(currentUser.getUserId());
            SearchResult result = aiSearchService.search(applicantOpt.get(), jobs, query);

            if (!result.isSuccess()) {
                int status = "profile_required".equals(result.getAction()) ? 400 : 503;
                ApiResponses.write(
                        response,
                        status,
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
            getServletContext().log("Failed to run TA job AI search", ex);
            ApiResponses.write(response, 503, false, TaJobAiSearchService.UNAVAILABLE_MESSAGE, null);
        }
    }

    private List<Job> loadApplicableOpenJobs(String currentUserId) {
        LocalDateTime effectiveNow = LocalDateTime.now();
        // 前端只显示推荐结果；“排除已申请职位”和“只看开放职位”是后端保护逻辑。
        return jobDao.findAll().stream()
                .filter(job -> job != null && job.getEffectiveStatus(effectiveNow) == Job.Status.OPEN)
                .filter(job -> job.getJobId() != null && !job.getJobId().trim().isEmpty())
                .filter(job -> !applicationDao.hasApplied(job.getJobId(), currentUserId))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildResponseData(SearchResult result) {
        List<Map<String, Object>> jobs = new ArrayList<>();
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Map<String, Object> recommendationsById = new LinkedHashMap<>();

        for (RecommendedJob recommended : result.getRecommendations()) {
            Job job = recommended.getJob();
            if (job == null) {
                continue;
            }
            // 前端同时需要职位卡片数据和 jobId -> 推荐理由的快速索引。
            jobs.add(buildJobPayload(job));
            Map<String, Object> recommendation = ApiResponses.objectMap(
                    "jobId", safeText(job.getJobId()),
                    "recommendation", recommended.getRecommendation()
            );
            recommendations.add(recommendation);
            recommendationsById.put(safeText(job.getJobId()), recommended.getRecommendation());
        }

        return ApiResponses.objectMap(
                "action", result.getAction(),
                "message", result.getMessage(),
                "jobs", jobs,
                "recommendations", recommendations,
                "recommendationsByJobId", recommendationsById,
                "total", jobs.size()
        );
    }

    private Map<String, Object> buildJobPayload(Job job) {
        Job.Status effectiveStatus = job.getEffectiveStatus();
        return ApiResponses.objectMap(
                "jobId", safeText(job.getJobId()),
                "moId", safeText(job.getMoId()),
                "moName", safeText(job.getMoName()),
                "title", safeText(job.getTitle()),
                "courseCode", safeText(job.getCourseCode()),
                "courseName", safeText(job.getCourseName()),
                "description", safeText(job.getDescription()),
                "requiredSkills", safeText(job.getRequiredSkillsAsString()),
                "positions", job.getPositions(),
                "workload", safeText(job.getWorkload()),
                "salary", safeText(job.getSalary()),
                "deadline", job.getDeadline() != null ? job.getDeadline().toString() : "",
                "status", effectiveStatus != null ? effectiveStatus.name() : "OPEN"
        );
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    private boolean containsControlChars(String value) {
        return value != null && value.chars().anyMatch(ch -> ch < 32 || ch == 127);
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }
}

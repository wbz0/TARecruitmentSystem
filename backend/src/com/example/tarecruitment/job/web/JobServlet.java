package com.example.tarecruitment.job.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.job.mapper.JobRequestMapper;
import com.example.tarecruitment.job.service.JobService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * JobServlet - Job API entry.
 *
 * Paths:
 * - GET    /api/jobs: TA job list, MO dashboard list.
 * - GET    /api/jobs/{jobId}: TA/MO job detail.
 * - POST   /api/jobs: MO publishes job.
 * - PUT    /api/jobs/{jobId}: MO updates their own published job.
 * - DELETE /api/jobs/{jobId}: MO deletes their own published job.
 *
 * Servlet remains thin: only parses path/parameters, gets current user, calls JobService, writes unified JSON.
 */
@WebServlet(urlPatterns = {ApiRoutes.JOBS, ApiRoutes.JOBS + "/*"})
public class JobServlet extends HttpServlet {

    private static final String[] JOB_FIELDS = {
            // POST job creation only accepts these frontend form fields, avoiding irrelevant request parameters entering business object.
            "title",
            "courseCode",
            "courseName",
            "description",
            "requiredSkills",
            "positions",
            "weeklyHours",
            "workStartDate",
            "workEndDate",
            "salary",
            "deadline",
            "status"
    };

    private JobService jobService;

    @Override
    public void init() throws ServletException {
        jobService = JobService.getInstance();
        Logger.i("JobServlet", "JobServlet initialized");
    }

    /**
     * Query job list or single job detail.
     *
     * Frontend sources:
     * - TA job list page requests list and detail;
     * - MO dashboard requests list of their own published jobs.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String jobId = JobRequestMapper.pathJobId(request.getPathInfo());
            if (!jobId.isEmpty()) {
                write(response, jobService.detail(jobId));
                return;
            }

            // List filter parameters come from TA job-list.js and MO dashboard.js.
            write(response, jobService.list(
                    request.getParameter("courseCode"),
                    request.getParameter("status"),
                    request.getParameter("keyword"),
                    request.getParameter("moId")
            ));
        } catch (Exception e) {
            Logger.e("JobServlet", "Error retrieving jobs", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * Create job, only accepts POST to /api/jobs root path.
     *
     * Permission, field validation and CSV write are all handled in JobService, here only responsible for
     * collecting whitelist parameters and forwarding.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (!JobRequestMapper.pathJobId(request.getPathInfo()).isEmpty()) {
                ApiResponses.methodNotAllowed(response);
                return;
            }
            write(response, jobService.create(getCurrentUser(request), JobRequestMapper.requestParameters(request, JOB_FIELDS)));
        } catch (Exception e) {
            Logger.e("JobServlet", "Unexpected error during job creation", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * Update job detail.
     *
     * This API is called by MO dashboard edit dialog, PUT body needs mapper manual parsing,
     * to avoid inconsistent PUT form parameter support across different Servlet containers.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String jobId = JobRequestMapper.pathJobId(request.getPathInfo());
            // PUT request body is x-www-form-urlencoded; request.getParameter may not auto-parse in some containers.
            Map<String, String> parameters = JobRequestMapper.formParameters(request);
            write(response, jobService.update(getCurrentUser(request), jobId, parameters));
        } catch (Exception e) {
            Logger.e("JobServlet", "Unexpected error during job update", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * Delete job.
     *
     * Whether delete is allowed is determined by JobService based on current logged-in user and job publisher,
     * Servlet does not directly do business permission inference.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            write(response, jobService.delete(getCurrentUser(request), JobRequestMapper.pathJobId(request.getPathInfo())));
        } catch (Exception e) {
            Logger.e("JobServlet", "Unexpected error during job deletion", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * Convert service layer result to unified { success, message, data } JSON.
     */
    private void write(HttpServletResponse response, ServiceResult result) throws IOException {
        ApiResponses.write(response, result.getStatusCode(), result.isSuccess(), result.getMessage(), result.getData());
    }

    /**
     * Only get current user from session, do not duplicate login/role logic in Servlet.
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
}

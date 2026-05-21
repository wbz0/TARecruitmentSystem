package com.example.tarecruitment.admin.web;

import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.admin.service.WorkloadStatsService;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.model.Applicant;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WorkloadStatsServlet - 管理员 TA 录用工作量统计接口。
 *
 * 访问路径：GET /api/admin/workload-statistics
 * 对应页面：admin dashboard.jsp / js/admin/admin-dashboard.js。
 *
 * Servlet 只做登录、ADMIN 权限、日期参数和响应格式；统计口径放在 WorkloadStatsService。
 */
@WebServlet(ApiRoutes.ADMIN_WORKLOAD_STATISTICS)
public class WorkloadStatsServlet extends HttpServlet {

    private ApplicationDao applicationDao;
    private JobDao jobDao;
    private UserDao userDao;
    private ApplicantDao applicantDao;
    private WorkloadStatsService workloadStatsService;

    @Override
    public void init() throws ServletException {
        applicationDao = ApplicationDao.getInstance();
        jobDao = JobDao.getInstance();
        userDao = UserDao.getInstance();
        applicantDao = ApplicantDao.getInstance();
        workloadStatsService = new WorkloadStatsService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            ApiResponses.write(response, 401, false, "Please login first", null);
            return;
        }
        if (currentUser.getRole() != User.Role.ADMIN) {
            ApiResponses.write(response, 403, false, "Only ADMIN can access workload stats", null);
            return;
        }

        LocalDateTime start = parseDateTime(request.getParameter("start"), false);
        LocalDateTime end = parseDateTime(request.getParameter("end"), true);
        if (request.getParameter("start") != null && start == null) {
            ApiResponses.write(response, 400, false, "Invalid start datetime format", null);
            return;
        }
        if (request.getParameter("end") != null && end == null) {
            ApiResponses.write(response, 400, false, "Invalid end datetime format", null);
            return;
        }
        if (start != null && end != null && start.isAfter(end)) {
            ApiResponses.write(response, 400, false, "start cannot be after end", null);
            return;
        }

        String mode = request.getParameter("mode");
        if (mode != null && !mode.isBlank() && !"ta".equalsIgnoreCase(mode)) {
            // 遗留/待移除：曾预留过非 TA 工作量模式，但当前前端没有入口，也没有对应统计实现。
            ApiResponses.write(response, 400, false, "Only TA workload stats are supported", null);
            return;
        }

        WorkloadStatsService.WorkloadReport report = buildReport(start, end);
        if ("csv".equalsIgnoreCase(request.getParameter("export"))) {
            // 管理员页面的导出按钮使用同一统计结果，只是换成 text/csv 下载。
            String csv = workloadStatsService.exportTaWorkloadCsv(report);
            response.setStatus(200);
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"ta-workload-stats.csv\"");
            response.getWriter().write(csv);
            return;
        }

        ApiResponses.write(response, 200, true, "TA workload stats generated", reportToMap(report));
    }

    private WorkloadStatsService.WorkloadReport buildReport(LocalDateTime start, LocalDateTime end) {
        // 先把 CSV 数据整理成按 id 访问的 map，避免统计过程中反复扫描文件。
        Map<String, Job> jobsById = jobDao.findAll().stream()
                .collect(Collectors.toMap(
                        Job::getJobId,
                        job -> job,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        Map<String, User> usersById = userDao.findAll().stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        Map<String, String> taRealNamesByUserId = applicantDao.findAll().stream()
                .filter(applicant -> hasText(applicant.getUserId()) && hasText(applicant.getFullName()))
                .collect(Collectors.toMap(
                        Applicant::getUserId,
                        applicant -> applicant.getFullName().trim(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return workloadStatsService.calculateTaWorkloadReport(
                applicationDao.findAll(),
                jobsById,
                usersById,
                taRealNamesByUserId,
                start,
                end
        );
    }

    private Map<String, Object> reportToMap(WorkloadStatsService.WorkloadReport report) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taWorkloads", taWorkloadsToList(report.getTaWorkloads()));
        data.put("invalidJobs", invalidJobsToList(report.getInvalidJobs()));
        data.put("totalTaCount", report.getTotalTaCount());
        data.put("totalAcceptedJobs", report.getTotalAcceptedJobs());
        data.put("totalWorkWeeks", report.getTotalWorkWeeks());
        data.put("totalWorkHours", roundNumber(report.getTotalWorkHours()));
        return data;
    }

    private List<Map<String, Object>> taWorkloadsToList(List<WorkloadStatsService.TaWorkloadStats> workloads) {
        return workloads.stream().map(stats -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taId", stats.getTaId());
            item.put("taName", stats.getTaName());
            item.put("acceptedJobCount", stats.getAcceptedJobCount());
            item.put("totalWorkWeeks", stats.getTotalWorkWeeks());
            item.put("totalWorkHours", roundNumber(stats.getTotalWorkHours()));
            item.put("jobs", stats.getJobs().stream().map(job -> {
                Map<String, Object> jobItem = new LinkedHashMap<>();
                jobItem.put("jobId", job.getJobId());
                jobItem.put("jobTitle", job.getJobTitle());
                jobItem.put("courseCode", job.getCourseCode());
                jobItem.put("weeklyHours", roundNumber(job.getWeeklyHours()));
                jobItem.put("workStartDate", job.getWorkStartDate().toString());
                jobItem.put("workEndDate", job.getWorkEndDate().toString());
                jobItem.put("countedWeeks", job.getCountedWeeks());
                jobItem.put("countedHours", roundNumber(job.getCountedHours()));
                return jobItem;
            }).collect(Collectors.toList()));
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> invalidJobsToList(List<WorkloadStatsService.InvalidJob> invalidJobs) {
        return invalidJobs.stream().map(invalid -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("applicationId", invalid.getApplicationId());
            item.put("applicantId", invalid.getApplicantId());
            item.put("applicantName", invalid.getApplicantName());
            item.put("jobId", invalid.getJobId());
            item.put("jobTitle", invalid.getJobTitle());
            item.put("reason", invalid.getReason());
            return item;
        }).collect(Collectors.toList());
    }

    private double roundNumber(double value) {
        return Double.parseDouble(WorkloadStatsService.formatNumber(value));
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private LocalDateTime parseDateTime(String text, boolean isEnd) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String value = text.trim();
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
            // 继续兼容浏览器 datetime-local 的分钟精度。
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (Exception ignored) {
            // 继续兼容只有日期的筛选值。
        }
        try {
            LocalDate date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            return isEnd ? LocalDateTime.of(date, LocalTime.MAX) : LocalDateTime.of(date, LocalTime.MIN);
        } catch (Exception ignored) {
            return null;
        }
    }
}

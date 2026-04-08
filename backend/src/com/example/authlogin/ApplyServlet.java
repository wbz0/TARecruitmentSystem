package com.example.authlogin;

import com.example.authlogin.dao.ApplicationDao;
import com.example.authlogin.dao.JobDao;
import com.example.authlogin.model.Application;
import com.example.authlogin.model.Job;
import com.example.authlogin.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ApplyServlet - 处理职位申请相关操作
 * 访问路径: /apply
 *
 * 功能:
 * - POST /apply - 申请职位（仅TA可操作）
 * - GET /apply - 查看申请列表
 * - PUT /apply?id=xxx&action=accept/reject/withdraw - 审核/撤回申请
 */
@WebServlet("/apply")
public class ApplyServlet extends HttpServlet {

    private ApplicationDao applicationDao;
    private JobDao jobDao;
    private static final int MAX_COVER_LETTER_LENGTH = 2000;

    // 简单的日志方法
    private void logInfo(String message) {
        System.out.println("[ApplyServlet] " + message);
    }

    private void logError(String message, Throwable t) {
        System.err.println("[ApplyServlet ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    @Override
    public void init() throws ServletException {
        applicationDao = ApplicationDao.getInstance();
        jobDao = JobDao.getInstance();
        logInfo("ApplyServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                writeJsonResponse(response, 401, false, "Please login first", null);
                return;
            }

            // 获取查询参数
            String applicationId = request.getParameter("id");
            String applicantId = request.getParameter("applicantId");
            String jobId = request.getParameter("jobId");
            String moId = request.getParameter("moId");
            String status = request.getParameter("status");

            // 如果指定了applicationId，返回单个申请
            if (applicationId != null && !applicationId.trim().isEmpty()) {
                getApplicationById(request, response, applicationId, currentUser);
                return;
            }

            // 获取申请列表
            List<Application> applications;

            // TA查看自己的申请
            if (currentUser.getRole() == User.Role.TA) {
                applications = applicationDao.findByApplicantId(currentUser.getUserId());
            }
            // MO查看自己职位的申请
            else if (currentUser.getRole() == User.Role.MO) {
                applications = applicationDao.findByMoId(currentUser.getUserId());
            }
            // 管理员查看所有申请
            else if (currentUser.getRole() == User.Role.ADMIN) {
                applications = applicationDao.findAll();
            }
            else {
                writeJsonResponse(response, 403, false, "Unauthorized role", null);
                return;
            }

            // 应用筛选条件
            if (jobId != null && !jobId.trim().isEmpty()) {
                applications = applications.stream()
                        .filter(a -> a.getJobId().equals(jobId.trim()))
                        .collect(Collectors.toList());
            }

            if (applicantId != null && !applicantId.trim().isEmpty()) {
                // MO只能看自己职位的申请人
                if (currentUser.getRole() == User.Role.MO) {
                    applications = applications.stream()
                            .filter(a -> a.getApplicantId().equals(applicantId.trim()))
                            .collect(Collectors.toList());
                } else {
                    // 其他角色直接过滤
                    applications = applicationDao.findByApplicantId(applicantId.trim());
                }
            }

            if (moId != null && !moId.trim().isEmpty()) {
                applications = applications.stream()
                        .filter(a -> a.getMoId() != null && a.getMoId().equals(moId.trim()))
                        .collect(Collectors.toList());
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    Application.Status appStatus = Application.Status.valueOf(status.toUpperCase().trim());
                    applications = applications.stream()
                            .filter(a -> a.getStatus() == appStatus)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // 无效状态，返回空列表
                    applications = List.of();
                }
            }

            // 构建JSON响应
            String data = buildApplicationListJson(applications);
            writeJsonResponse(response, 200, true, "Applications retrieved successfully", data);

        } catch (Exception e) {
            logError("Error retrieving applications", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    /**
     * 获取单个申请详情
     */
    private void getApplicationById(HttpServletRequest request, HttpServletResponse response, String applicationId, User currentUser)
            throws IOException {
        Optional<Application> appOpt = applicationDao.findById(applicationId);

        if (appOpt.isEmpty()) {
            writeJsonResponse(response, 404, false, "Application not found", null);
            return;
        }

        Application app = appOpt.get();

        // 检查权限：申请人、MO、管理员可以查看
        boolean hasPermission = false;
        if (currentUser.getRole() == User.Role.ADMIN) {
            hasPermission = true;
        } else if (currentUser.getRole() == User.Role.TA && app.getApplicantId().equals(currentUser.getUserId())) {
            hasPermission = true;
        } else if (currentUser.getRole() == User.Role.MO && app.getMoId() != null && app.getMoId().equals(currentUser.getUserId())) {
            hasPermission = true;
        }

        if (!hasPermission) {
            writeJsonResponse(response, 403, false, "You don't have permission to view this application", null);
            return;
        }

        String data = buildApplicationJson(app);
        writeJsonResponse(response, 200, true, "Application retrieved successfully", data);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                writeJsonResponse(response, 401, false, "Please login first", null);
                return;
            }

            // 检查用户角色（只有TA可以申请职位）
            if (currentUser.getRole() != User.Role.TA) {
                writeJsonResponse(response, 403, false, "Only TA can apply for jobs", null);
                return;
            }

            // 获取请求参数
            String jobId = request.getParameter("jobId");
            String coverLetter = request.getParameter("coverLetter");
            String normalizedJobId = jobId != null ? jobId.trim() : "";
            String normalizedCoverLetter = coverLetter != null ? coverLetter.trim() : "";

            // 输入验证
            if (normalizedJobId.isEmpty()) {
                writeJsonResponse(response, 400, false, "Job ID is required", null);
                return;
            }
            if (hasControlChars(jobId) || containsDangerousMarkup(jobId)) {
                writeJsonResponse(response, 400, false, "Job ID contains unsupported characters", null);
                return;
            }
            if (normalizedCoverLetter.length() > MAX_COVER_LETTER_LENGTH) {
                writeJsonResponse(response, 400, false, "Cover letter must be 2000 characters or fewer", null);
                return;
            }
            if (hasControlChars(coverLetter) || containsDangerousMarkup(coverLetter)) {
                writeJsonResponse(response, 400, false, "Cover letter contains unsupported characters", null);
                return;
            }

            // 检查职位是否存在
            Optional<Job> jobOpt = jobDao.findById(normalizedJobId);
            if (jobOpt.isEmpty()) {
                writeJsonResponse(response, 404, false, "Job not found", null);
                return;
            }

            Job job = jobOpt.get();

            // 检查职位是否开放
            if (job.getStatus() != Job.Status.OPEN) {
                writeJsonResponse(response, 400, false, "This job is no longer accepting applications", null);
                return;
            }

            // 检查是否已申请
            if (applicationDao.hasApplied(normalizedJobId, currentUser.getUserId())) {
                writeJsonResponse(response, 400, false, "You have already applied for this job", null);
                return;
            }

            // 创建申请对象
            Application application = new Application();
            application.setJobId(normalizedJobId);
            application.setApplicantId(currentUser.getUserId());
            application.setApplicantName(currentUser.getUsername());
            application.setApplicantEmail(currentUser.getEmail());
            application.setJobTitle(job.getTitle());
            application.setCourseCode(job.getCourseCode());
            application.setMoId(job.getMoId());
            application.setMoName(job.getMoName());
            application.setCoverLetter(normalizedCoverLetter.isEmpty() ? null : normalizedCoverLetter);

            // 保存申请
            Application savedApp = applicationDao.create(application);
            logInfo("Application created successfully: " + savedApp.getApplicationId() + " by TA: " + currentUser.getUsername());

            String data = "{\"applicationId\": \"" + savedApp.getApplicationId() + "\"}";
            writeJsonResponse(response, 201, true, "Application submitted successfully!", data);

        } catch (IllegalArgumentException e) {
            logInfo("Application submission failed: " + e.getMessage());
            writeJsonResponse(response, 400, false, e.getMessage(), null);
        } catch (Exception e) {
            logError("Unexpected error during application submission", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                writeJsonResponse(response, 401, false, "Please login first", null);
                return;
            }

            // 获取参数
            String applicationId = request.getParameter("id");
            String action = request.getParameter("action");

            if (applicationId == null || applicationId.trim().isEmpty()) {
                writeJsonResponse(response, 400, false, "Application ID is required", null);
                return;
            }

            if (action == null || action.trim().isEmpty()) {
                writeJsonResponse(response, 400, false, "Action is required", null);
                return;
            }

            // 查找申请
            Optional<Application> appOpt = applicationDao.findById(applicationId.trim());
            if (appOpt.isEmpty()) {
                writeJsonResponse(response, 404, false, "Application not found", null);
                return;
            }

            Application application = appOpt.get();

            // 处理不同的操作
            switch (action.trim().toLowerCase()) {
                case "accept":
                    handleAccept(response, application, currentUser);
                    break;
                case "reject":
                    handleReject(response, application, currentUser);
                    break;
                case "withdraw":
                    handleWithdraw(response, application, currentUser);
                    break;
                default:
                    writeJsonResponse(response, 400, false, "Invalid action. Use 'accept', 'reject', or 'withdraw'", null);
            }

        } catch (Exception e) {
            logError("Unexpected error during application update", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    /**
     * 处理接受申请（MO操作）
     */
    private void handleAccept(HttpServletResponse response, Application application, User currentUser)
            throws IOException {
        // 只有MO可以接受申请
        if (currentUser.getRole() != User.Role.MO) {
            writeJsonResponse(response, 403, false, "Only MO can accept applications", null);
            return;
        }

        // MO只能操作自己职位的申请
        if (!application.getMoId().equals(currentUser.getUserId())) {
            writeJsonResponse(response, 403, false, "You can only review applications for your own jobs", null);
            return;
        }

        // 检查申请状态
        if (application.getStatus() != Application.Status.PENDING) {
            writeJsonResponse(response, 400, false, "This application has already been reviewed", null);
            return;
        }

        // 接受申请
        boolean updated = applicationDao.accept(application.getApplicationId());
        if (updated) {
            // 获取更新后的申请状态
            Optional<Application> updatedApp = applicationDao.findById(application.getApplicationId());
            if (updatedApp.isPresent()) {
                String data = "{" + buildApplicationJson(updatedApp.get()) + "}";
                logInfo("Application accepted: " + application.getApplicationId() + " by MO: " + currentUser.getUsername());
                writeJsonResponse(response, 200, true, "Application accepted successfully!", data);
            } else {
                writeJsonResponse(response, 500, false, "Failed to retrieve updated application", null);
            }
        } else {
            writeJsonResponse(response, 500, false, "Failed to accept application", null);
        }
    }

    /**
     * 处理拒绝申请（MO操作）
     */
    private void handleReject(HttpServletResponse response, Application application, User currentUser)
            throws IOException {
        // 只有MO可以拒绝申请
        if (currentUser.getRole() != User.Role.MO) {
            writeJsonResponse(response, 403, false, "Only MO can reject applications", null);
            return;
        }

        // MO只能操作自己职位的申请
        if (!application.getMoId().equals(currentUser.getUserId())) {
            writeJsonResponse(response, 403, false, "You can only review applications for your own jobs", null);
            return;
        }

        // 检查申请状态
        if (application.getStatus() != Application.Status.PENDING) {
            writeJsonResponse(response, 400, false, "This application has already been reviewed", null);
            return;
        }

        // 拒绝申请
        boolean updated = applicationDao.reject(application.getApplicationId());
        if (updated) {
            // 获取更新后的申请状态
            Optional<Application> updatedApp = applicationDao.findById(application.getApplicationId());
            if (updatedApp.isPresent()) {
                String data = "{" + buildApplicationJson(updatedApp.get()) + "}";
                logInfo("Application rejected: " + application.getApplicationId() + " by MO: " + currentUser.getUsername());
                writeJsonResponse(response, 200, true, "Application rejected successfully!", data);
            } else {
                writeJsonResponse(response, 500, false, "Failed to retrieve updated application", null);
            }
        } else {
            writeJsonResponse(response, 500, false, "Failed to reject application", null);
        }
    }

    /**
     * 处理撤回申请（申请人操作）
     */
    private void handleWithdraw(HttpServletResponse response, Application application, User currentUser)
            throws IOException {
        // TA可以撤回自己的申请，MO可以撤回自己职位的申请
        boolean canWithdraw = false;

        if (currentUser.getRole() == User.Role.TA && application.getApplicantId().equals(currentUser.getUserId())) {
            canWithdraw = true;
        } else if (currentUser.getRole() == User.Role.MO && application.getMoId().equals(currentUser.getUserId())) {
            canWithdraw = true;
        } else if (currentUser.getRole() == User.Role.ADMIN) {
            canWithdraw = true;
        }

        if (!canWithdraw) {
            writeJsonResponse(response, 403, false, "You don't have permission to withdraw this application", null);
            return;
        }

        // 检查申请状态
        if (application.getStatus() != Application.Status.PENDING) {
            writeJsonResponse(response, 400, false, "This application can no longer be withdrawn", null);
            return;
        }

        // 撤回申请
        boolean updated = applicationDao.withdraw(application.getApplicationId());
        if (updated) {
            // 获取更新后的申请状态
            Optional<Application> updatedApp = applicationDao.findById(application.getApplicationId());
            if (updatedApp.isPresent()) {
                String data = "{" + buildApplicationJson(updatedApp.get()) + "}";
                logInfo("Application withdrawn: " + application.getApplicationId());
                writeJsonResponse(response, 200, true, "Application withdrawn successfully!", data);
            } else {
                writeJsonResponse(response, 500, false, "Failed to retrieve updated application", null);
            }
        } else {
            writeJsonResponse(response, 500, false, "Failed to withdraw application", null);
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    private boolean hasControlChars(String value) {
        return value != null && value.matches(".*[\\x00-\\x1F\\x7F].*");
    }

    private boolean containsDangerousMarkup(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String text = value.toLowerCase();
        return text.matches(".*<[^>]*>.*")
            || text.contains("javascript:")
            || text.matches(".*on\\w+\\s*=.*");
    }

    /**
     * 构建单个申请JSON
     */
    private String buildApplicationJson(Application app) {
        StringBuilder json = new StringBuilder();
        json.append("\"applicationId\": \"").append(escapeJson(app.getApplicationId())).append("\", ");
        json.append("\"jobId\": \"").append(escapeJson(app.getJobId())).append("\", ");
        json.append("\"applicantId\": \"").append(escapeJson(app.getApplicantId())).append("\", ");
        json.append("\"applicantName\": \"").append(escapeJson(app.getApplicantName())).append("\", ");
        json.append("\"applicantEmail\": \"").append(escapeJson(app.getApplicantEmail())).append("\", ");
        json.append("\"jobTitle\": \"").append(escapeJson(app.getJobTitle() != null ? app.getJobTitle() : "")).append("\", ");
        json.append("\"courseCode\": \"").append(escapeJson(app.getCourseCode() != null ? app.getCourseCode() : "")).append("\", ");
        json.append("\"moId\": \"").append(escapeJson(app.getMoId() != null ? app.getMoId() : "")).append("\", ");
        json.append("\"moName\": \"").append(escapeJson(app.getMoName() != null ? app.getMoName() : "")).append("\", ");
        json.append("\"status\": \"").append(app.getStatus() != null ? app.getStatus().name() : "PENDING").append("\", ");
        json.append("\"coverLetter\": \"").append(escapeJson(app.getCoverLetter() != null ? app.getCoverLetter() : "")).append("\", ");
        json.append("\"appliedAt\": \"").append(app.getAppliedAt() != null ? app.getAppliedAt().toString() : "").append("\"");
        return json.toString();
    }

    /**
     * 构建申请列表JSON
     */
    private String buildApplicationListJson(List<Application> applications) {
        StringBuilder json = new StringBuilder();
        json.append("\"applications\": [");

        for (int i = 0; i < applications.size(); i++) {
            json.append("{");
            json.append(buildApplicationJson(applications.get(i)));
            json.append("}");
            if (i < applications.size() - 1) {
                json.append(", ");
            }
        }

        json.append("], ");
        json.append("\"total\": ").append(applications.size());
        return json.toString();
    }

    /**
     * 统一的JSON响应写入方法
     */
    private void writeJsonResponse(HttpServletResponse response, int status, boolean success, String message, String data)
            throws IOException {
        response.setStatus(status);
        PrintWriter out = response.getWriter();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(success).append(", ");
        json.append("\"message\": \"").append(escapeJson(message)).append("\"");

        if (data != null) {
            json.append(", ").append(data);
        }

        json.append("}");
        out.write(json.toString());
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

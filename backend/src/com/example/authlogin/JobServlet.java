package com.example.authlogin;

import com.example.authlogin.dao.JobDao;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JobServlet - 处理职位相关操作
 * 访问路径: /jobs
 *
 * 功能:
 * - POST /jobs - 创建新职位（仅MO可操作）
 * - GET /jobs - 获取职位列表（支持筛选）
 * - GET /jobs?id=xxx - 获取单个职位详情
 * - PUT /jobs?id=xxx - 更新职位（仅职位所属MO可操作）
 * - DELETE /jobs?id=xxx - 删除职位（仅职位所属MO可操作）
 */
@WebServlet("/jobs")
public class JobServlet extends HttpServlet {

    private JobDao jobDao;

    // 简单的日志方法
    private void logInfo(String message) {
        System.out.println("[JobServlet] " + message);
    }

    private void logError(String message, Throwable t) {
        System.err.println("[JobServlet ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    @Override
    public void init() throws ServletException {
        jobDao = JobDao.getInstance();
        logInfo("JobServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取查询参数
            String jobId = request.getParameter("id");
            String courseCode = request.getParameter("courseCode");
            String status = request.getParameter("status");
            String keyword = request.getParameter("keyword");
            String moId = request.getParameter("moId");

            // 如果指定了jobId，返回单个职位
            if (jobId != null && !jobId.trim().isEmpty()) {
                getJobById(request, response, jobId);
                return;
            }

            // 获取职位列表
            List<Job> jobs = jobDao.findAll();

            // 应用筛选条件
            if (courseCode != null && !courseCode.trim().isEmpty()) {
                jobs = jobs.stream()
                        .filter(j -> j.getCourseCode().equalsIgnoreCase(courseCode.trim()))
                        .collect(Collectors.toList());
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    Job.Status jobStatus = Job.Status.valueOf(status.toUpperCase().trim());
                    jobs = jobs.stream()
                            .filter(j -> j.getStatus() == jobStatus)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // 忽略无效状态
                }
            }

            if (moId != null && !moId.trim().isEmpty()) {
                jobs = jobs.stream()
                        .filter(j -> j.getMoId().equals(moId.trim()))
                        .collect(Collectors.toList());
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                jobs = jobDao.search(keyword.trim());
            }

            // 构建JSON响应
            String data = buildJobListJson(jobs);
            writeJsonResponse(response, 200, true, "Jobs retrieved successfully", data);

        } catch (Exception e) {
            logError("Error retrieving jobs", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    /**
     * 获取单个职位详情
     */
    private void getJobById(HttpServletRequest request, HttpServletResponse response, String jobId)
            throws IOException {
        Optional<Job> jobOpt = jobDao.findById(jobId);

        if (jobOpt.isEmpty()) {
            writeJsonResponse(response, 404, false, "Job not found", null);
            return;
        }

        Job job = jobOpt.get();
        String data = buildJobJson(job);
        writeJsonResponse(response, 200, true, "Job retrieved successfully", data);
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

            // 检查用户角色（只有MO可以发布职位）
            if (currentUser.getRole() != User.Role.MO) {
                writeJsonResponse(response, 403, false, "Only MO can post jobs", null);
                return;
            }

            // 获取请求参数
            String title = request.getParameter("title");
            String courseCode = request.getParameter("courseCode");
            String courseName = request.getParameter("courseName");
            String description = request.getParameter("description");
            String skills = request.getParameter("requiredSkills");
            String positionsStr = request.getParameter("positions");
            String workload = request.getParameter("workload");
            String salary = request.getParameter("salary");
            String deadlineStr = request.getParameter("deadline");

            // 输入验证
            String error = validateInput(title, courseCode, positionsStr);
            if (error != null) {
                logInfo("Validation failed: " + error);
                writeJsonResponse(response, 400, false, error, null);
                return;
            }

            // 创建职位对象
            Job job = new Job();
            job.setMoId(currentUser.getUserId());
            job.setMoName(currentUser.getUsername()); // 使用username作为MO姓名
            job.setTitle(title.trim());
            job.setCourseCode(courseCode.trim());
            job.setCourseName(courseName != null ? courseName.trim() : null);
            job.setDescription(description != null ? description.trim() : null);

            // 处理技能列表
            if (skills != null && !skills.trim().isEmpty()) {
                List<String> skillList = Arrays.stream(skills.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                job.setRequiredSkills(skillList);
            }

            // 处理职位数量
            try {
                int positions = Integer.parseInt(positionsStr.trim());
                job.setPositions(Math.max(1, positions));
            } catch (NumberFormatException e) {
                job.setPositions(1);
            }

            job.setWorkload(workload != null ? workload.trim() : null);
            job.setSalary(salary != null ? salary.trim() : null);

            // 处理截止日期
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime deadline = LocalDateTime.parse(deadlineStr.trim(), formatter);
                    job.setDeadline(deadline);
                } catch (Exception e) {
                    // 尝试其他日期格式
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        LocalDateTime deadline = LocalDateTime.parse(deadlineStr.trim(), formatter);
                        job.setDeadline(deadline);
                    } catch (Exception e2) {
                        logInfo("Invalid deadline format: " + deadlineStr);
                    }
                }
            }

            // 保存职位
            Job savedJob = jobDao.create(job);
            logInfo("Job created successfully: " + savedJob.getJobId() + " by MO: " + currentUser.getUsername());

            String data = "{\"jobId\": \"" + savedJob.getJobId() + "\"}";
            writeJsonResponse(response, 201, true, "Job created successfully!", data);

        } catch (IllegalArgumentException e) {
            logInfo("Job creation failed: " + e.getMessage());
            writeJsonResponse(response, 400, false, e.getMessage(), null);
        } catch (Exception e) {
            logError("Unexpected error during job creation", e);
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

            // 获取要更新的职位ID
            String jobId = request.getParameter("id");
            if (jobId == null || jobId.trim().isEmpty()) {
                writeJsonResponse(response, 400, false, "Job ID is required", null);
                return;
            }

            // 查找职位
            Optional<Job> jobOpt = jobDao.findById(jobId.trim());
            if (jobOpt.isEmpty()) {
                writeJsonResponse(response, 404, false, "Job not found", null);
                return;
            }

            Job job = jobOpt.get();

            // 检查权限（只有职位所属MO可以更新）
            if (!job.getMoId().equals(currentUser.getUserId())) {
                writeJsonResponse(response, 403, false, "You can only update your own jobs", null);
                return;
            }

            // 获取请求参数
            String title = request.getParameter("title");
            String courseCode = request.getParameter("courseCode");
            String courseName = request.getParameter("courseName");
            String description = request.getParameter("description");
            String skills = request.getParameter("requiredSkills");
            String positionsStr = request.getParameter("positions");
            String workload = request.getParameter("workload");
            String salary = request.getParameter("salary");
            String deadlineStr = request.getParameter("deadline");
            String statusStr = request.getParameter("status");

            // 更新字段
            if (title != null && !title.trim().isEmpty()) {
                job.setTitle(title.trim());
            }
            if (courseCode != null && !courseCode.trim().isEmpty()) {
                job.setCourseCode(courseCode.trim());
            }
            if (courseName != null) {
                job.setCourseName(courseName.trim().isEmpty() ? null : courseName.trim());
            }
            if (description != null) {
                job.setDescription(description.trim().isEmpty() ? null : description.trim());
            }

            // 处理技能列表
            if (skills != null) {
                List<String> skillList = Arrays.stream(skills.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                job.setRequiredSkills(skillList);
            }

            // 处理职位数量
            if (positionsStr != null && !positionsStr.trim().isEmpty()) {
                try {
                    int positions = Integer.parseInt(positionsStr.trim());
                    job.setPositions(Math.max(1, positions));
                } catch (NumberFormatException e) {
                    // 忽略无效输入
                }
            }

            if (workload != null) {
                job.setWorkload(workload.trim().isEmpty() ? null : workload.trim());
            }
            if (salary != null) {
                job.setSalary(salary.trim().isEmpty() ? null : salary.trim());
            }

            // 处理截止日期
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime deadline = LocalDateTime.parse(deadlineStr.trim(), formatter);
                    job.setDeadline(deadline);
                } catch (Exception e) {
                    // 尝试其他日期格式
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        LocalDateTime deadline = LocalDateTime.parse(deadlineStr.trim(), formatter);
                        job.setDeadline(deadline);
                    } catch (Exception e2) {
                        logInfo("Invalid deadline format: " + deadlineStr);
                    }
                }
            }

            // 处理状态更新
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    Job.Status newStatus = Job.Status.valueOf(statusStr.toUpperCase().trim());
                    job.setStatus(newStatus);
                } catch (IllegalArgumentException e) {
                    // 忽略无效状态
                }
            }

            // 保存更新
            Job updatedJob = jobDao.update(job);
            logInfo("Job updated successfully: " + updatedJob.getJobId());

            String data = "{\"jobId\": \"" + updatedJob.getJobId() + "\"}";
            writeJsonResponse(response, 200, true, "Job updated successfully!", data);

        } catch (IllegalArgumentException e) {
            logInfo("Job update failed: " + e.getMessage());
            writeJsonResponse(response, 400, false, e.getMessage(), null);
        } catch (Exception e) {
            logError("Unexpected error during job update", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                writeJsonResponse(response, 401, false, "Please login first", null);
                return;
            }

            // 获取要删除的职位ID
            String jobId = request.getParameter("id");
            if (jobId == null || jobId.trim().isEmpty()) {
                writeJsonResponse(response, 400, false, "Job ID is required", null);
                return;
            }

            // 查找职位
            Optional<Job> jobOpt = jobDao.findById(jobId.trim());
            if (jobOpt.isEmpty()) {
                writeJsonResponse(response, 404, false, "Job not found", null);
                return;
            }

            Job job = jobOpt.get();

            // 检查权限（只有职位所属MO可以删除）
            if (!job.getMoId().equals(currentUser.getUserId())) {
                writeJsonResponse(response, 403, false, "You can only delete your own jobs", null);
                return;
            }

            // 删除职位
            boolean deleted = jobDao.delete(jobId.trim());
            if (deleted) {
                logInfo("Job deleted successfully: " + jobId);
                writeJsonResponse(response, 200, true, "Job deleted successfully!", null);
            } else {
                writeJsonResponse(response, 500, false, "Failed to delete job", null);
            }

        } catch (Exception e) {
            logError("Unexpected error during job deletion", e);
            writeJsonResponse(response, 500, false, "An error occurred. Please try again later.", null);
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

    /**
     * 验证必填字段
     */
    private String validateInput(String title, String courseCode, String positions) {
        if (title == null || title.trim().isEmpty()) {
            return "Job title is required";
        }
        if (title.trim().length() > 200) {
            return "Job title is too long";
        }

        if (courseCode == null || courseCode.trim().isEmpty()) {
            return "Course code is required";
        }
        if (courseCode.trim().length() > 50) {
            return "Course code is too long";
        }

        if (positions != null && !positions.trim().isEmpty()) {
            try {
                int pos = Integer.parseInt(positions.trim());
                if (pos < 1) {
                    return "Positions must be at least 1";
                }
            } catch (NumberFormatException e) {
                return "Invalid positions number";
            }
        }

        return null;
    }

    /**
     * 构建单个职位JSON
     */
    private String buildJobJson(Job job) {
        StringBuilder json = new StringBuilder();
        json.append("\"jobId\": \"").append(escapeJson(job.getJobId())).append("\", ");
        json.append("\"moId\": \"").append(escapeJson(job.getMoId())).append("\", ");
        json.append("\"moName\": \"").append(escapeJson(job.getMoName())).append("\", ");
        json.append("\"title\": \"").append(escapeJson(job.getTitle())).append("\", ");
        json.append("\"courseCode\": \"").append(escapeJson(job.getCourseCode())).append("\", ");
        json.append("\"courseName\": \"").append(escapeJson(job.getCourseName() != null ? job.getCourseName() : "")).append("\", ");
        json.append("\"description\": \"").append(escapeJson(job.getDescription() != null ? job.getDescription() : "")).append("\", ");
        json.append("\"requiredSkills\": \"").append(escapeJson(job.getRequiredSkillsAsString())).append("\", ");
        json.append("\"positions\": ").append(job.getPositions()).append(", ");
        json.append("\"workload\": \"").append(escapeJson(job.getWorkload() != null ? job.getWorkload() : "")).append("\", ");
        json.append("\"salary\": \"").append(escapeJson(job.getSalary() != null ? job.getSalary() : "")).append("\", ");
        json.append("\"deadline\": \"").append(job.getDeadline() != null ? job.getDeadline().toString() : "").append("\", ");
        json.append("\"status\": \"").append(job.getStatus() != null ? job.getStatus().name() : "OPEN").append("\"");
        return json.toString();
    }

    /**
     * 构建职位列表JSON
     */
    private String buildJobListJson(List<Job> jobs) {
        StringBuilder json = new StringBuilder();
        json.append("\"jobs\": [");

        for (int i = 0; i < jobs.size(); i++) {
            json.append("{");
            json.append(buildJobJson(jobs.get(i)));
            json.append("}");
            if (i < jobs.size() - 1) {
                json.append(", ");
            }
        }

        json.append("], ");
        json.append("\"total\": ").append(jobs.size());
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

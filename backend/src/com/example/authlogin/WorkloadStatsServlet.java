package com.example.authlogin;

import com.example.authlogin.dao.ApplicationDao;
import com.example.authlogin.model.User;
import com.example.authlogin.service.WorkloadStatsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * WorkloadStatsServlet - 管理员工作量统计接口
 * 访问路径: /api/admin/workload
 */
@WebServlet("/api/admin/workload")
public class WorkloadStatsServlet extends HttpServlet {

    private ApplicationDao applicationDao;
    private WorkloadStatsService workloadStatsService;

    @Override
    public void init() throws ServletException {
        applicationDao = ApplicationDao.getInstance();
        workloadStatsService = new WorkloadStatsService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            writeJsonResponse(response, 401, false, "Please login first", null);
            return;
        }
        if (currentUser.getRole() != User.Role.ADMIN) {
            writeJsonResponse(response, 403, false, "Only ADMIN can access workload stats", null);
            return;
        }

        LocalDateTime start = parseDateTime(request.getParameter("start"), false);
        LocalDateTime end = parseDateTime(request.getParameter("end"), true);
        if (request.getParameter("start") != null && start == null) {
            writeJsonResponse(response, 400, false, "Invalid start datetime format", null);
            return;
        }
        if (request.getParameter("end") != null && end == null) {
            writeJsonResponse(response, 400, false, "Invalid end datetime format", null);
            return;
        }
        if (start != null && end != null && start.isAfter(end)) {
            writeJsonResponse(response, 400, false, "start cannot be after end", null);
            return;
        }

        String mode = request.getParameter("mode");
        if ("mo".equalsIgnoreCase(mode)) {
            if ("csv".equalsIgnoreCase(request.getParameter("export"))) {
                String csv = workloadStatsService.exportMoWorkloadCsv(applicationDao.findAll(), start, end);
                response.setStatus(200);
                response.setContentType("text/csv;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=\"mo-workload-stats.csv\"");
                response.getWriter().write(csv);
                return;
            }

            String data = "\"moWorkloads\": " + moWorkloadsToJson(
                    workloadStatsService.calculateMoWorkloadStats(applicationDao.findAll(), start, end)
            );
            writeJsonResponse(response, 200, true, "MO workload stats generated", data);
            return;
        }

        WorkloadStatsService.ApplicationCounts counts = workloadStatsService.calculateApplicationCounts(applicationDao.findAll(), start, end);
        String data = "\"total\": " + counts.getTotal()
                + ", \"pending\": " + counts.getPending()
                + ", \"accepted\": " + counts.getAccepted()
                + ", \"rejected\": " + counts.getRejected()
                + ", \"withdrawn\": " + counts.getWithdrawn();
        writeJsonResponse(response, 200, true, "Application count stats generated", data);
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

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

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String moWorkloadsToJson(java.util.List<WorkloadStatsService.MoWorkloadStats> workloads) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (workloads != null) {
            for (int i = 0; i < workloads.size(); i++) {
                WorkloadStatsService.MoWorkloadStats stats = workloads.get(i);
                json.append("{");
                json.append("\"moId\": \"").append(escapeJson(stats.getMoId())).append("\", ");
                json.append("\"moName\": \"").append(escapeJson(stats.getMoName())).append("\", ");
                json.append("\"totalApplications\": ").append(stats.getTotalApplications()).append(", ");
                json.append("\"pending\": ").append(stats.getPending()).append(", ");
                json.append("\"processed\": ").append(stats.getProcessed()).append(", ");
                json.append("\"accepted\": ").append(stats.getAccepted()).append(", ");
                json.append("\"rejected\": ").append(stats.getRejected()).append(", ");
                json.append("\"withdrawn\": ").append(stats.getWithdrawn());
                json.append("}");
                if (i < workloads.size() - 1) {
                    json.append(", ");
                }
            }
        }
        json.append("]");
        return json.toString();
    }

    private LocalDateTime parseDateTime(String text, boolean isEnd) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String value = text.trim();
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
            // fallback
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (Exception ignored) {
            // fallback
        }
        try {
            LocalDate date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            return isEnd ? LocalDateTime.of(date, LocalTime.MAX) : LocalDateTime.of(date, LocalTime.MIN);
        } catch (Exception ignored) {
            return null;
        }
    }
}

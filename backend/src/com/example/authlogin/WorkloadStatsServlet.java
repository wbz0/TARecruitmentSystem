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

        WorkloadStatsService.ApplicationCounts counts =
                workloadStatsService.calculateApplicationCounts(applicationDao.findAll());

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
}

package com.example.tarecruitment.application.web;

import com.example.tarecruitment.application.mapper.ApplicationRequestMapper;
import com.example.tarecruitment.application.service.ApplicationApplicantService;
import com.example.tarecruitment.application.service.ApplicationService;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.common.web.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;

/**
 * 申请相关 API 的 HTTP 入口。
 *
 * 路径：
 * - GET/POST `/api/applications`
 * - GET `/api/applications/{applicationId}`
 * - POST `/api/applications/{applicationId}/transition`
 * - GET `/api/applications/{applicationId}/applicant[/resume|/photo]`
 *
 * Servlet 保持薄：只解析 path/parameter，业务和权限交给 application service。
 */
@WebServlet(urlPatterns = {ApiRoutes.APPLICATIONS, ApiRoutes.APPLICATIONS + "/*"})
public class ApplicationServlet extends HttpServlet {

    private ApplicationService applicationService;
    private ApplicationApplicantService applicationApplicantService;

    @Override
    public void init() throws ServletException {
        applicationService = ApplicationService.getInstance();
        applicationApplicantService = ApplicationApplicantService.getInstance();
        Logger.i("ApplicationServlet", "ApplicationServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser(request);
        String pathInfo = request.getPathInfo();
        try {
            if (ApplicationRequestMapper.isCollection(pathInfo)) {
                // TA/MO/Admin 复用同一个列表接口，service 按角色收窄可见数据。
                write(response, applicationService.list(
                        currentUser,
                        request.getParameter("applicantId"),
                        request.getParameter("jobId"),
                        request.getParameter("moId"),
                        request.getParameter("status"),
                        request.getParameter("keyword")
                ));
                return;
            }

            String applicationId = ApplicationRequestMapper.applicationId(pathInfo);
            if (ApplicationRequestMapper.isDetail(pathInfo)) {
                write(response, applicationService.detail(currentUser, applicationId));
                return;
            }
            if (ApplicationRequestMapper.isApplicantDetail(pathInfo)) {
                write(response, applicationApplicantService.detail(currentUser, applicationId));
                return;
            }
            if (ApplicationRequestMapper.isApplicantResume(pathInfo)) {
                // 文件资源不包在 JSON 中，直接写二进制响应。
                writeFile(response, applicationApplicantService.resume(currentUser, applicationId));
                return;
            }
            if (ApplicationRequestMapper.isApplicantPhoto(pathInfo)) {
                // 照片同样走 applicationId 权限校验，避免用 applicantId 枚举资源。
                writeFile(response, applicationApplicantService.photo(currentUser, applicationId));
                return;
            }

            ApiResponses.notFound(response, "Application resource not found");
        } catch (Exception e) {
            Logger.e("ApplicationServlet", "Error retrieving application resource", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser(request);
        String pathInfo = request.getPathInfo();
        try {
            if (ApplicationRequestMapper.isCollection(pathInfo)) {
                write(response, applicationService.create(
                        currentUser,
                        request.getParameter("jobId"),
                        request.getParameter("coverLetter")
                ));
                return;
            }
            if (ApplicationRequestMapper.isTransition(pathInfo)) {
                write(response, applicationService.transition(
                        currentUser,
                        ApplicationRequestMapper.applicationId(pathInfo),
                        request.getParameter("action")
                ));
                return;
            }
            ApiResponses.methodNotAllowed(response, "Method not allowed");
        } catch (IllegalArgumentException e) {
            ApiResponses.badRequest(response, e.getMessage());
        } catch (Exception e) {
            Logger.e("ApplicationServlet", "Error updating application resource", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 状态流转统一走 POST transition，避免恢复旧的大入口/多动词接口。
        ApiResponses.methodNotAllowed(response, "Use POST /api/applications/{applicationId}/transition");
    }

    private void write(HttpServletResponse response, ServiceResult result) throws IOException {
        ApiResponses.write(response, result.getStatusCode(), result.isSuccess(), result.getMessage(), result.getData());
    }

    private void write(HttpServletResponse response, ApplicationApplicantService.DetailResult result) throws IOException {
        ApiResponses.write(response, result.getStatusCode(), result.isSuccess(), result.getMessage(), result.getData());
    }

    private void writeFile(HttpServletResponse response, ApplicationApplicantService.FileResult result) throws IOException {
        if (!result.isSuccess()) {
            ApiResponses.write(response, result.getStatusCode(), false, result.getMessage(), null);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        // 文件响应只设置内容类型、缓存和 disposition，不加入统一 JSON 包装。
        response.setContentType(result.getContentType());
        if (result.getContentDisposition() != null) {
            response.setHeader("Content-Disposition", result.getContentDisposition());
        }
        if (result.getCacheControl() != null) {
            response.setHeader("Cache-Control", result.getCacheControl());
        }
        response.setContentLengthLong(result.getFile().length());
        Files.copy(result.getFile().toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
}

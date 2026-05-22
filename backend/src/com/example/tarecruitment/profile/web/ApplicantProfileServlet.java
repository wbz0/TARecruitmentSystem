package com.example.tarecruitment.profile.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.profile.mapper.ApplicantProfileRequestMapper;
import com.example.tarecruitment.profile.service.ApplicantProfileService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * ApplicantProfileServlet - Current TA profile JSON API.
 *
 * Path: /api/me/applicant-profile
 * Corresponds to profile read and save in TA dashboard/job apply related JS.
 *
 * Avatar, formal resume, and resume draft files are not handled here directly; they are handled by ApplicantAssetServlet.
 */
@WebServlet(ApiRoutes.ME_APPLICANT_PROFILE)
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 20
)
public class ApplicantProfileServlet extends HttpServlet {

    private ApplicantProfileService applicantProfileService;

    @Override
    public void init() throws ServletException {
        applicantProfileService = ApplicantProfileService.getInstance();
        Logger.i("ApplicantProfileServlet", "ApplicantProfileServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            write(response, applicantProfileService.get(getCurrentUser(request), request.getSession(false)));
        } catch (Exception e) {
            Logger.e("ApplicantProfileServlet", "Error retrieving applicant profile", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        save(request, response, false);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        save(request, response, true);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Current frontend has no "delete entire TA profile" entry, only allows editing fields and removing avatar.
        ApiResponses.methodNotAllowed(response, "Delete is not supported for this endpoint.");
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean isUpdate)
            throws IOException, ServletException {
        try {
            User currentUser = getCurrentUser(request);
            HttpSession session = request.getSession(false);
            if (ApplicantProfileRequestMapper.isMultipart(request)) {
                // Requests with resume/photo go through multipart mapper; plain form save goes through normal parameter mapper.
                write(response, applicantProfileService.saveUpload(
                        currentUser,
                        session,
                        ApplicantProfileRequestMapper.upload(request)
                ));
                return;
            }

            write(response, applicantProfileService.saveForm(
                    currentUser,
                    session,
                    ApplicantProfileRequestMapper.input(request),
                    isUpdate
            ));
        } catch (ServletException e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("size")) {
                ApiResponses.write(response, 413, false,
                        "File size exceeds the maximum limit of 10MB. Please upload a smaller file.", null);
            } else {
                Logger.e("ApplicantProfileServlet", "Servlet error during profile save", e);
                ApiResponses.badRequest(response, "File upload failed. " + e.getMessage());
            }
        } catch (Exception e) {
            Logger.e("ApplicantProfileServlet", "Unexpected error during profile save", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    private void write(HttpServletResponse response, ServiceResult result) throws IOException {
        ApiResponses.write(response, result.getStatusCode(), result.isSuccess(), result.getMessage(), result.getData());
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
}

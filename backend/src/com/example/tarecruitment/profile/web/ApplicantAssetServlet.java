package com.example.tarecruitment.profile.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.mapper.ApplicantProfileResponseMapper;
import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.profile.service.ProfileAssetService;
import com.example.tarecruitment.profile.validator.ProfileAssetValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

/**
 * ApplicantAssetServlet - Current TA profile asset API.
 *
 * Paths:
 * - GET    /api/me/applicant-profile/photo: Returns profile photo.
 * - GET    /api/me/applicant-profile/resume: Returns formal resume.
 * - POST   /api/me/applicant-profile/resume-draft: First uploads pending resume draft to save.
 * - DELETE /api/me/applicant-profile/resume-draft: Discards pending resume draft.
 *
 * This handles binary files and draft state in session; profile field saving is still in ApplicantProfileServlet.
 */
@WebServlet(urlPatterns = {
        ApiRoutes.ME_APPLICANT_RESUME_DRAFT,
        ApiRoutes.ME_APPLICANT_PHOTO,
        ApiRoutes.ME_APPLICANT_RESUME
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 20
)
public class ApplicantAssetServlet extends HttpServlet {

    private ApplicantDao applicantDao;
    private ProfileAssetService assetService;

    @Override
    public void init() throws ServletException {
        applicantDao = ApplicantDao.getInstance();
        assetService = ProfileAssetService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Optional<Applicant> applicantOpt = applicantDao.findByUserId(currentUser.getUserId());
        if (applicantOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Optional<ProfileAssetService.FileResource> resource = Optional.empty();
        if (ApiRoutes.ME_APPLICANT_PHOTO.equals(request.getServletPath())) {
            resource = assetService.photoResource(applicantOpt.get());
        } else if (ApiRoutes.ME_APPLICANT_RESUME.equals(request.getServletPath())) {
            resource = assetService.resumeResource(applicantOpt.get());
        }

        if (resource.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeFile(response, resource.get());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!ApiRoutes.ME_APPLICANT_RESUME_DRAFT.equals(request.getServletPath())) {
            ApiResponses.methodNotAllowed(response);
            return;
        }

        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            ApiResponses.unauthorized(response, "Please login first");
            return;
        }

        try {
            Part filePart = request.getPart("resume");
            if (filePart == null || filePart.getSize() <= 0) {
                ApiResponses.badRequest(response, "Please choose a resume file first.");
                return;
            }

            String fileError = ProfileAssetValidator.validateResumeFile(filePart);
            if (fileError != null) {
                ApiResponses.badRequest(response, fileError);
                return;
            }

            HttpSession session = request.getSession();
            // Only one pending resume is kept per session; new draft replaces old draft.
            assetService.clearDraftResumeState(session, true);
            String originalFileName = ProfileAssetValidator.extractFileName(filePart);
            String draftResumePath = assetService.saveDraftFile(filePart, currentUser.getUserId());
            assetService.storeDraftResumeState(session, draftResumePath, originalFileName);

            ApiResponses.ok(response, "Resume draft uploaded successfully!",
                    ApplicantProfileResponseMapper.draftResumePayload(session, assetService));
        } catch (IllegalArgumentException e) {
            ApiResponses.badRequest(response, e.getMessage());
        } catch (ServletException e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("size")) {
                ApiResponses.write(response, 413, false,
                        "File size exceeds the maximum limit of 10MB. Please upload a smaller file.", null);
            } else {
                Logger.e("ApplicantAssetServlet", "Servlet error during draft resume upload", e);
                ApiResponses.badRequest(response, "File upload failed. " + e.getMessage());
            }
        } catch (Exception e) {
            Logger.e("ApplicantAssetServlet", "Unexpected error during draft resume upload", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!ApiRoutes.ME_APPLICANT_RESUME_DRAFT.equals(request.getServletPath())) {
            ApiResponses.methodNotAllowed(response);
            return;
        }

        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            ApiResponses.unauthorized(response, "Please login first");
            return;
        }

        HttpSession session = request.getSession(false);
        assetService.clearDraftResumeState(session, true);
        ApiResponses.ok(response, "Pending resume changes discarded.",
                ApplicantProfileResponseMapper.draftResumePayload(session, assetService));
    }

    private void writeFile(HttpServletResponse response, ProfileAssetService.FileResource resource) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(resource.getContentType());
        if (resource.getContentDisposition() != null) {
            response.setHeader("Content-Disposition", resource.getContentDisposition());
        }
        if (resource.getCacheControl() != null) {
            response.setHeader("Cache-Control", resource.getCacheControl());
        }
        response.setContentLengthLong(resource.getFile().length());
        Files.copy(resource.getFile().toPath(), response.getOutputStream());
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

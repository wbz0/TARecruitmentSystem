package com.example.tarecruitment.profile.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.profile.service.AccountProfileService;
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
 * AccountProfileServlet - Account profile and account avatar API entry point.
 *
 * Paths:
 * - GET/POST/PUT /api/me/account: Shared sidebar/top bar read and save account profile.
 * - GET          /api/me/avatar: Returns current account avatar file.
 *
 * Servlet only handles multipart, avatar binary response, and unified errors; business synchronization logic is in AccountProfileService.
 */
@WebServlet(urlPatterns = {ApiRoutes.ME_ACCOUNT, ApiRoutes.ME_AVATAR})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 8
)
public class AccountProfileServlet extends HttpServlet {

    private AccountProfileService accountProfileService;

    @Override
    public void init() throws ServletException {
        accountProfileService = AccountProfileService.getInstance();
        Logger.i("AccountProfileServlet", "AccountProfileServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = accountProfileService.currentUser(request.getSession(false));
        if (isAvatarRequest(request)) {
            // Avatar is image binary response, not wrapped in ApiResponses JSON shell.
            writeAvatar(response, currentUser);
            return;
        }
        write(response, accountProfileService.get(currentUser));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        update(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        update(request, response);
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            User currentUser = accountProfileService.currentUser(session);
            write(response, accountProfileService.update(
                    currentUser,
                    session,
                    request.getParameter("displayName"),
                    request.getParameter("realName"),
                    request.getParameter("professionalTitle"),
                    getOptionalPart(request, "avatar")
            ));
        } catch (ServletException e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("size")) {
                // Servlet container throws exception first when multipart limit is exceeded, service layer cannot get Part.
                ApiResponses.write(response, 413, false,
                        "File size exceeds the maximum limit of 5MB. Please upload a smaller file.", null);
            } else {
                Logger.e("AccountProfileServlet", "Servlet error during account profile update", e);
                ApiResponses.badRequest(response, "File upload failed. " + e.getMessage());
            }
        } catch (Exception e) {
            Logger.e("AccountProfileServlet", "Unexpected error during account profile update", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    private void writeAvatar(HttpServletResponse response, User currentUser) throws IOException {
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Optional<AccountProfileService.AvatarResource> resource = accountProfileService.avatar(currentUser);
        if (resource.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        AccountProfileService.AvatarResource avatar = resource.get();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(avatar.getContentType());
        response.setHeader("Cache-Control", avatar.getCacheControl());
        response.setContentLengthLong(avatar.getFile().length());
        Files.copy(avatar.getFile().toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    private Part getOptionalPart(HttpServletRequest request, String name) throws IOException, ServletException {
        try {
            return request.getPart(name);
        } catch (IllegalStateException e) {
            throw e;
        } catch (ServletException e) {
            String contentType = request.getContentType();
            if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
                // Normal form update account profile does not have avatar part, this is a valid request.
                return null;
            }
            throw e;
        }
    }

    private boolean isAvatarRequest(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String asset = request.getParameter("asset");
        return ApiRoutes.ME_AVATAR.equals(servletPath) || "avatar".equalsIgnoreCase(asset);
    }

    private void write(HttpServletResponse response, ServiceResult result) throws IOException {
        ApiResponses.write(response, result.getStatusCode(), result.isSuccess(), result.getMessage(), result.getData());
    }
}

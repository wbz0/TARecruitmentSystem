package com.example.tarecruitment.profile.mapper;

import com.example.tarecruitment.profile.validator.ApplicantProfileInput;
import com.example.tarecruitment.profile.validator.ApplicantProfileValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;

/**
 * ApplicantProfileRequestMapper - TA profile request parameter conversion utility.
 *
 * Responsible for taking form fields and multipart file parts from request, and generating ApplicantProfileInput usable by service layer.
 * Does not do business validation, does not save files.
 */
public final class ApplicantProfileRequestMapper {

    private ApplicantProfileRequestMapper() {
    }

    public static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("multipart/form-data");
    }

    public static ApplicantProfileInput input(HttpServletRequest request) {
        // Preserve raw values; validator/service needs to know whether field is “not passed” vs “passed empty string”.
        return new ApplicantProfileInput(
                request.getParameter("fullName"),
                request.getParameter("studentId"),
                request.getParameter("department"),
                request.getParameter("program"),
                request.getParameter("gpa"),
                request.getParameter("skills"),
                request.getParameter("phone"),
                request.getParameter("address"),
                request.getParameter("experience"),
                request.getParameter("motivation"),
                ApplicantProfileValidator.isTruthyFlag(request.getParameter("removePhoto"))
        );
    }

    public static ApplicantProfileUpload upload(HttpServletRequest request) throws ServletException, IOException {
        return new ApplicantProfileUpload(input(request), optionalPart(request, "resume"), optionalPart(request, "photo"));
    }

    private static Part optionalPart(HttpServletRequest request, String name) throws ServletException, IOException {
        Part part = request.getPart(name);
        // Empty file input generates a Part in some browsers; service should not treat it as an upload.
        return part != null && part.getSize() > 0 ? part : null;
    }

    public static final class ApplicantProfileUpload {
        private final ApplicantProfileInput input;
        private final Part resumePart;
        private final Part photoPart;

        private ApplicantProfileUpload(ApplicantProfileInput input, Part resumePart, Part photoPart) {
            this.input = input;
            this.resumePart = resumePart;
            this.photoPart = photoPart;
        }

        public ApplicantProfileInput getInput() {
            return input;
        }

        public Part getResumePart() {
            return resumePart;
        }

        public Part getPhotoPart() {
            return photoPart;
        }
    }
}

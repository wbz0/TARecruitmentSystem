package com.example.tarecruitment.profile.mapper;

import com.example.tarecruitment.profile.validator.ApplicantProfileInput;
import com.example.tarecruitment.profile.validator.ApplicantProfileValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;

/**
 * ApplicantProfileRequestMapper - TA 档案请求参数转换工具。
 *
 * 负责从 request 中取表单字段、multipart 文件 part，并生成 service 层可用的 ApplicantProfileInput。
 * 不做业务校验、不保存文件。
 */
public final class ApplicantProfileRequestMapper {

    private ApplicantProfileRequestMapper() {
    }

    public static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("multipart/form-data");
    }

    public static ApplicantProfileInput input(HttpServletRequest request) {
        // 保留 raw 值，validator/service 需要知道字段是“未传”还是“传了空字符串”。
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
        // 空文件输入框在部分浏览器里也会生成 Part，service 不应把它当作上传。
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

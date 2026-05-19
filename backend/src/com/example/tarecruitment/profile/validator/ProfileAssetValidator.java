package com.example.tarecruitment.profile.validator;

import jakarta.servlet.http.Part;

import java.util.Arrays;
import java.util.List;

/**
 * ProfileAssetValidator - TA 档案附件校验。
 *
 * 校验简历和档案照片的类型、扩展名、大小。实际保存路径由 ProfileAssetService 负责。
 */
public final class ProfileAssetValidator {

    private static final long MAX_RESUME_SIZE = 10 * 1024 * 1024;
    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_RESUME_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final List<String> ALLOWED_PHOTO_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final List<String> ALLOWED_RESUME_EXTENSIONS = Arrays.asList(".pdf", ".doc", ".docx");
    private static final List<String> ALLOWED_PHOTO_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp");

    private ProfileAssetValidator() {
    }

    public static String validateResumeFile(Part filePart) {
        return validateFile(filePart, ALLOWED_RESUME_CONTENT_TYPES, ALLOWED_RESUME_EXTENSIONS,
                MAX_RESUME_SIZE, "PDF, DOC, and DOCX", 10);
    }

    public static String validatePhotoFile(Part filePart) {
        return validateFile(filePart, ALLOWED_PHOTO_CONTENT_TYPES, ALLOWED_PHOTO_EXTENSIONS,
                MAX_PHOTO_SIZE, "JPG, JPEG, PNG, and WEBP", 5);
    }

    public static String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                token = token.trim();
                if (token.startsWith("filename=")) {
                    String fileName = token.substring(9);
                    if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                        fileName = fileName.substring(1, fileName.length() - 1);
                    }
                    return fileName;
                }
            }
        }
        String submittedFileName = part.getSubmittedFileName();
        return submittedFileName != null ? submittedFileName : "resume";
    }

    private static String validateFile(Part filePart,
                                       List<String> allowedContentTypes,
                                       List<String> allowedExtensions,
                                       long maxSize,
                                       String allowedDescription,
                                       int maxSizeMb) {
        if (filePart == null || filePart.getSize() <= 0) {
            return "Please choose a file first.";
        }

        String contentType = filePart.getContentType();
        String fileName = extractFileName(filePart);
        // 同时检查 Content-Type 和扩展名，降低伪装文件被当作简历/图片保存的风险。
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            return "Invalid file type. Only " + allowedDescription + " files are allowed.";
        }

        String lowerFileName = fileName.toLowerCase();
        boolean hasValidExtension = allowedExtensions.stream().anyMatch(lowerFileName::endsWith);
        if (!hasValidExtension) {
            return "Invalid file extension. Only " + allowedDescription + " files are allowed.";
        }

        if (filePart.getSize() > maxSize) {
            return "File size exceeds " + maxSizeMb + "MB limit.";
        }
        return null;
    }
}

package com.example.tarecruitment.application.service;

import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.storage.StoragePaths;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.model.Applicant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Read applicant details and controlled resources through application records.
 *
 * MO/TA application detail page cannot directly use any applicantId to access profile,
 * must first confirm current user is associated with this application through applicationId.
 */
public class ApplicationApplicantService {

    private static ApplicationApplicantService instance;

    private final ApplicantDao applicantDao;
    private final ApplicationDao applicationDao;

    private ApplicationApplicantService() {
        this.applicantDao = ApplicantDao.getInstance();
        this.applicationDao = ApplicationDao.getInstance();
    }

    public static synchronized ApplicationApplicantService getInstance() {
        if (instance == null) {
            instance = new ApplicationApplicantService();
        }
        return instance;
    }

    public DetailResult detail(User currentUser, String applicationId) {
        LookupResult lookup = loadApplicantForApplication(currentUser, applicationId);
        if (!lookup.isSuccess()) {
            return DetailResult.error(lookup.statusCode, lookup.message);
        }
        return DetailResult.ok("Applicant detail retrieved successfully",
                buildApplicantDetailPayload(lookup.applicant, lookup.application));
    }

    public FileResult resume(User currentUser, String applicationId) {
        LookupResult lookup = loadApplicantForApplication(currentUser, applicationId);
        if (!lookup.isSuccess()) {
            return FileResult.error(lookup.statusCode, lookup.message);
        }
        String resumePath = safeText(lookup.applicant.getResumePath());
        if (resumePath.isEmpty()) {
            return FileResult.error(404, "Resume not found for this applicant");
        }
        File file = new File(StoragePaths.getDataDir(), resumePath);
        if (!file.exists() || !file.isFile()) {
            return FileResult.error(404, "Resume file is unavailable");
        }
        // Resume is returned as inline preview; filename is only used for browser download/preview title, not for path construction.
        String contentType = probeContentType(file, "application/octet-stream");
        return FileResult.ok(file, contentType, "inline; filename=\"" + sanitizeFilename(file.getName()) + "\"", null);
    }

    public FileResult photo(User currentUser, String applicationId) {
        LookupResult lookup = loadApplicantForApplication(currentUser, applicationId);
        if (!lookup.isSuccess()) {
            return FileResult.error(lookup.statusCode, lookup.message);
        }
        String photoPath = safeText(lookup.applicant.getPhotoPath());
        if (photoPath.isEmpty()) {
            return FileResult.error(404, "Photo not found for this applicant");
        }
        File file = new File(StoragePaths.getDataDir(), photoPath);
        if (!file.exists() || !file.isFile()) {
            return FileResult.error(404, "Photo file is unavailable");
        }
        String contentType = probeContentType(file, "image/jpeg");
        if (!contentType.startsWith("image/")) {
            // When file exists but system cannot identify it as image, fallback to jpeg to avoid page avatar/photo preview directly failing.
            contentType = "image/jpeg";
        }
        return FileResult.ok(file, contentType, null, "private, max-age=300");
    }

    private LookupResult loadApplicantForApplication(User currentUser, String applicationId) {
        // Unified entry: detail, resume, photo all first go through same application ownership verification.
        if (currentUser == null) {
            return LookupResult.error(401, "Please login first");
        }
        String normalizedApplicationId = safeText(applicationId);
        if (normalizedApplicationId.isEmpty()) {
            return LookupResult.error(400, "Application ID is required");
        }

        Optional<Application> applicationOpt = applicationDao.findById(normalizedApplicationId);
        if (applicationOpt.isEmpty()) {
            return LookupResult.error(404, "Application not found");
        }

        Application application = applicationOpt.get();
        if (!canAccessApplication(currentUser, application)) {
            return LookupResult.error(403, "You don't have permission to access this applicant");
        }

        Optional<Applicant> applicantOpt = applicantDao.findByUserId(application.getApplicantId());
        if (applicantOpt.isEmpty()) {
            return LookupResult.error(404, "Applicant profile not found");
        }

        return LookupResult.ok(application, applicantOpt.get());
    }

    private boolean canAccessApplication(User currentUser, Application application) {
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (currentUser.getRole() == User.Role.MO) {
            return currentUser.getUserId().equals(application.getMoId());
        }
        return currentUser.getRole() == User.Role.TA && currentUser.getUserId().equals(application.getApplicantId());
    }

    private Map<String, Object> buildApplicantDetailPayload(Applicant applicant, Application application) {
        // Current MO/TA application detail page displays some fields; sensitive fields like phone and address are only returned in authorized details.
        // AI prompt does not directly read sensitive fields from this payload; AI service will separately do whitelist and sanitization.
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicationId", application.getApplicationId());
        data.put("applicantId", applicant.getApplicantId());
        data.put("userId", applicant.getUserId());
        data.put("fullName", applicant.getFullName());
        data.put("studentId", applicant.getStudentId());
        data.put("department", applicant.getDepartment());
        data.put("program", applicant.getProgram());
        data.put("gpa", applicant.getGpa());
        data.put("skills", applicant.getSkills());
        data.put("resumePath", applicant.getResumePath());
        data.put("resumeName", extractResumeName(applicant.getResumePath()));
        data.put("photoPath", applicant.getPhotoPath());
        data.put("phone", applicant.getPhone());
        data.put("address", applicant.getAddress());
        data.put("experience", applicant.getExperience());
        data.put("motivation", applicant.getMotivation());
        data.put("hasResume", !safeText(applicant.getResumePath()).isEmpty());
        data.put("hasPhoto", !safeText(applicant.getPhotoPath()).isEmpty());
        data.put("profileCreatedAt", formatDateTime(applicant.getCreatedAt()));
        data.put("profileUpdatedAt", formatDateTime(applicant.getUpdatedAt()));
        data.put("applicationStatus", application.getStatus() != null ? application.getStatus().name() : "PENDING");
        data.put("coverLetter", application.getCoverLetter());
        return data;
    }

    private String probeContentType(File file, String fallback) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            return contentType == null || contentType.trim().isEmpty() ? fallback : contentType;
        } catch (IOException e) {
            return fallback;
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private String extractResumeName(String resumePath) {
        String path = safeText(resumePath);
        if (path.isEmpty()) {
            return "";
        }
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private String sanitizeFilename(String filename) {
        return safeText(filename).replace("\"", "");
    }

    private static final class LookupResult {
        private final int statusCode;
        private final String message;
        private final Application application;
        private final Applicant applicant;

        private LookupResult(int statusCode, String message, Application application, Applicant applicant) {
            this.statusCode = statusCode;
            this.message = message;
            this.application = application;
            this.applicant = applicant;
        }

        private boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        private static LookupResult ok(Application application, Applicant applicant) {
            return new LookupResult(200, "", application, applicant);
        }

        private static LookupResult error(int statusCode, String message) {
            return new LookupResult(statusCode, message, null, null);
        }
    }

    public static final class DetailResult {
        private final int statusCode;
        private final boolean success;
        private final String message;
        private final Object data;

        private DetailResult(int statusCode, boolean success, String message, Object data) {
            this.statusCode = statusCode;
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }

        public static DetailResult ok(String message, Object data) {
            return new DetailResult(200, true, message, data);
        }

        public static DetailResult error(int statusCode, String message) {
            return new DetailResult(statusCode, false, message, null);
        }
    }

    public static final class FileResult {
        private final int statusCode;
        private final boolean success;
        private final String message;
        private final File file;
        private final String contentType;
        private final String contentDisposition;
        private final String cacheControl;

        private FileResult(int statusCode,
                           boolean success,
                           String message,
                           File file,
                           String contentType,
                           String contentDisposition,
                           String cacheControl) {
            this.statusCode = statusCode;
            this.success = success;
            this.message = message;
            this.file = file;
            this.contentType = contentType;
            this.contentDisposition = contentDisposition;
            this.cacheControl = cacheControl;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public File getFile() {
            return file;
        }

        public String getContentType() {
            return contentType;
        }

        public String getContentDisposition() {
            return contentDisposition;
        }

        public String getCacheControl() {
            return cacheControl;
        }

        public static FileResult ok(File file, String contentType, String contentDisposition, String cacheControl) {
            return new FileResult(200, true, "", file, contentType, contentDisposition, cacheControl);
        }

        public static FileResult error(int statusCode, String message) {
            return new FileResult(statusCode, false, message, null, null, null, null);
        }
    }
}

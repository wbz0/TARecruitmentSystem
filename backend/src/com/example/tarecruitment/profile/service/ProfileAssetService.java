package com.example.tarecruitment.profile.service;

import com.example.tarecruitment.common.storage.StoragePaths;
import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.profile.validator.ProfileAssetValidator;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * ProfileAssetService - TA profile attachment service.
 *
 * Responsible for saving, locating, deleting and assembling response information for resume, resume draft, and photo files.
 * Externally only returns relative paths (e.g., resumes/xxx.pdf), real root directory is determined by TA_HIRING_DATA_DIR.
 */
public class ProfileAssetService {

    public static final String SESSION_DRAFT_RESUME_PATH = "applicantDraftResumePath";
    public static final String SESSION_DRAFT_RESUME_NAME = "applicantDraftResumeName";

    private static ProfileAssetService instance;

    private ProfileAssetService() {
        ensureDirectories();
    }

    public static synchronized ProfileAssetService getInstance() {
        if (instance == null) {
            instance = new ProfileAssetService();
        }
        return instance;
    }

    /**
     * Ensures the three attachment directories exist.
     *
     * Auto-created when local script runs first time or TA_HIRING_DATA_DIR points to a new directory.
     */
    public void ensureDirectories() {
        ensureDirectoryExists(StoragePaths.getResumeDir());
        ensureDirectoryExists(StoragePaths.getResumeDraftDir());
        ensureDirectoryExists(StoragePaths.getPhotoDir());
    }

    /**
     * Save formal resume, return relative path to write to Applicant CSV.
     */
    public String saveResumeFile(Part filePart, String userId) throws IOException {
        String fileName = ProfileAssetValidator.extractFileName(filePart);
        String newFileName = buildStoredFileName(fileName, userId, "", ".pdf", "resume");
        ensureDirectoryExists(StoragePaths.getResumeDir());
        File file = new File(StoragePaths.getResumeDir(), newFileName);
        filePart.write(file.getAbsolutePath());
        return "resumes/" + newFileName;
    }

    /**
     * Save profile photo, return relative path to write to Applicant CSV.
     */
    public String savePhotoFile(Part filePart, String userId) throws IOException {
        String fileName = ProfileAssetValidator.extractFileName(filePart);
        String newFileName = buildStoredFileName(fileName, userId, "", ".jpg", "photo");
        ensureDirectoryExists(StoragePaths.getPhotoDir());
        File file = new File(StoragePaths.getPhotoDir(), newFileName);
        filePart.write(file.getAbsolutePath());
        return "photos/" + newFileName;
    }

    /**
     * Save resume draft.
     *
     * Draft is first attached to session, only copied to formal resume when user finally saves TA profile.
     */
    public String saveDraftFile(Part filePart, String userId) throws IOException {
        String fileName = ProfileAssetValidator.extractFileName(filePart);
        String newFileName = buildStoredFileName(fileName, userId, "draft_", ".pdf", "resume");
        ensureDirectoryExists(StoragePaths.getResumeDraftDir());
        File file = new File(StoragePaths.getResumeDraftDir(), newFileName);
        filePart.write(file.getAbsolutePath());
        return "resume-drafts/" + newFileName;
    }

    /**
     * Copy draft resume from session to formal resume directory.
     *
     * Copy instead of move, so that if save fails later, the uploaded draft can still be retained.
     */
    public String copyDraftResumeToFinal(String draftRelativePath, String userId, String originalFileName) throws IOException {
        File draftFile = resolveStoredFile(draftRelativePath);
        if (draftFile == null || !draftFile.exists() || !draftFile.isFile()) {
            throw new IllegalArgumentException("The pending resume draft is unavailable. Please choose the file again.");
        }

        String sourceFileName = isNotEmpty(originalFileName) ? originalFileName : buildDisplayFileName(draftRelativePath, draftFile.getName());
        String newFileName = buildStoredFileName(sourceFileName, userId, "", ".pdf", "resume");
        ensureDirectoryExists(StoragePaths.getResumeDir());

        File finalFile = new File(StoragePaths.getResumeDir(), newFileName);
        Files.copy(draftFile.toPath(), finalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return "resumes/" + newFileName;
    }

    /**
     * Construct photo response resource.
     *
     * Returning Optional.empty means frontend should use default avatar, not treat missing file as 500.
     */
    public Optional<FileResource> photoResource(Applicant applicant) throws IOException {
        if (applicant == null || !isNotEmpty(applicant.getPhotoPath())) {
            return Optional.empty();
        }
        File file = resolveStoredFile(applicant.getPhotoPath());
        if (!isUsableFile(file)) {
            return Optional.empty();
        }
        String contentType = Files.probeContentType(file.toPath());
        if (!isNotEmpty(contentType) || !contentType.startsWith("image/")) {
            // Local filesystem sometimes cannot recognize webp/jpg, fallback by extension to avoid avatar display failure.
            contentType = detectPhotoContentType(file.getName());
        }
        return Optional.of(new FileResource(file, contentType, null, "no-store"));
    }

    /**
     * Construct resume response resource.
     *
     * PDF tries inline preview, Word documents let browser download to avoid iframe preview failure.
     */
    public Optional<FileResource> resumeResource(Applicant applicant) throws IOException {
        if (applicant == null || !isNotEmpty(applicant.getResumePath())) {
            return Optional.empty();
        }
        File file = resolveStoredFile(applicant.getResumePath());
        if (!isUsableFile(file)) {
            return Optional.empty();
        }
        String contentType = Files.probeContentType(file.toPath());
        if (!isNotEmpty(contentType)) {
            contentType = detectResumeContentType(file.getName());
        }
        boolean isPdf = "application/pdf".equalsIgnoreCase(contentType);
        // PDF can be embedded preview; doc/docx let browser download.
        String disposition = (isPdf ? "inline" : "attachment") + "; filename=\"" + file.getName() + "\"";
        return Optional.of(new FileResource(file, contentType, disposition, "no-store"));
    }

    /**
     * Store draft resume info to current session.
     *
     * This type of state does not enter CSV, because user may only upload file but not yet saved profile form.
     */
    public void storeDraftResumeState(HttpSession session, String draftResumePath, String originalFileName) {
        if (session == null) {
            return;
        }
        session.setAttribute(SESSION_DRAFT_RESUME_PATH, draftResumePath);
        session.setAttribute(SESSION_DRAFT_RESUME_NAME, originalFileName != null ? originalFileName : "");
    }

    /**
     * Clear draft resume session state.
     *
     * deleteFile=true for cancel/save complete scenario, to avoid abandoned draft occupying data directory for long.
     */
    public void clearDraftResumeState(HttpSession session, boolean deleteFile) {
        if (session == null) {
            return;
        }
        String draftResumePath = getDraftResumePath(session);
        if (deleteFile && isNotEmpty(draftResumePath)) {
            // Draft only belongs to current session; should be cleared after user cancels or saves successfully, to avoid occupying data directory.
            deleteStoredFile(draftResumePath);
        }
        session.removeAttribute(SESSION_DRAFT_RESUME_PATH);
        session.removeAttribute(SESSION_DRAFT_RESUME_NAME);
    }

    public String getDraftResumePath(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object value = session.getAttribute(SESSION_DRAFT_RESUME_PATH);
        return value instanceof String ? ((String) value).trim() : "";
    }

    public String getDraftResumeName(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object value = session.getAttribute(SESSION_DRAFT_RESUME_NAME);
        return value instanceof String ? ((String) value).trim() : "";
    }

    public boolean hasDraftResume(HttpSession session) {
        return isNotEmpty(getDraftResumePath(session));
    }

    /**
     * Resolve relative path saved in CSV/session to data directory.
     *
     * Business layer only saves relative paths, to avoid exposing or hardening local absolute paths.
     */
    public File resolveStoredFile(String relativePath) {
        if (!isNotEmpty(relativePath)) {
            return null;
        }
        // Only concatenate relative paths under project data directory, does not accept external absolute paths as business file location.
        return new File(StoragePaths.getDataDir(), relativePath);
    }

    public long getStoredFileSize(String relativePath) {
        File file = resolveStoredFile(relativePath);
        return isUsableFile(file) ? file.length() : 0L;
    }

    public void deleteStoredFile(String relativePath) {
        File file = resolveStoredFile(relativePath);
        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    public void cleanupReplacedResume(String previousResumePath, String currentResumePath) {
        cleanupReplacedFile(previousResumePath, currentResumePath);
    }

    public void cleanupReplacedPhoto(String previousPhotoPath, String currentPhotoPath) {
        cleanupReplacedFile(previousPhotoPath, currentPhotoPath);
    }

    /**
     * Generate user-readable filename.
     *
     * Storage name contains userId and timestamp, when displaying on page try to strip these technical prefixes.
     */
    public String buildDisplayFileName(String relativePath, String fallbackName) {
        String safeFallbackName = fallbackName != null ? fallbackName.trim() : "";
        if (isNotEmpty(safeFallbackName)) {
            return safeFallbackName;
        }

        File file = resolveStoredFile(relativePath);
        String fileName = file != null ? file.getName() : "";
        if (!isNotEmpty(fileName) && isNotEmpty(relativePath)) {
            int slashIndex = Math.max(relativePath.lastIndexOf('/'), relativePath.lastIndexOf('\\'));
            fileName = slashIndex >= 0 ? relativePath.substring(slashIndex + 1) : relativePath;
        }
        if (!isNotEmpty(fileName)) {
            return "";
        }
        // Storage name has userId and timestamp; when showing to user, try to restore original filename.
        String normalizedName = fileName.replaceFirst("^(draft_)?[^_]+_\\d+_", "");
        return isNotEmpty(normalizedName) ? normalizedName : fileName;
    }

    /**
     * If new and old paths are different, delete the replaced old attachment.
     */
    private void cleanupReplacedFile(String previousPath, String currentPath) {
        if (!isNotEmpty(previousPath)) {
            return;
        }
        String safeCurrentPath = currentPath != null ? currentPath.trim() : "";
        if (!previousPath.equals(safeCurrentPath)) {
            deleteStoredFile(previousPath);
        }
    }

    /**
     * Generate actual stored filename.
     *
     * Format contains userId and timestamp, both for easy ownership tracing and avoiding same-name uploads overwriting each other.
     */
    private String buildStoredFileName(String originalFileName, String userId, String prefix, String defaultExtension, String fallbackBaseName) {
        // Filename preserves original extension and readable basename, while adding userId/timestamp to avoid overwriting.
        String extension = extractExtension(originalFileName, defaultExtension);
        String safeBaseName = sanitizeBaseName(originalFileName, fallbackBaseName);
        return prefix + userId + "_" + System.currentTimeMillis() + "_" + safeBaseName + extension;
    }

    /**
     * Preserve original extension; use caller's default extension when missing.
     */
    private String extractExtension(String fileName, String defaultExtension) {
        String safeDefaultExtension = isNotEmpty(defaultExtension) ? defaultExtension.toLowerCase() : ".bin";
        if (fileName == null) {
            return safeDefaultExtension;
        }
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return safeDefaultExtension;
        }
        return fileName.substring(dotIndex).toLowerCase();
    }

    /**
     * Clean filename body, prevent path separators or special characters from entering data directory filename.
     */
    private String sanitizeBaseName(String fileName, String fallbackBaseName) {
        String safeFileName = fileName != null ? fileName.trim() : "";
        int slashIndex = Math.max(safeFileName.lastIndexOf('/'), safeFileName.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < safeFileName.length() - 1) {
            safeFileName = safeFileName.substring(slashIndex + 1);
        }

        int dotIndex = safeFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? safeFileName.substring(0, dotIndex) : safeFileName;
        baseName = baseName.replaceAll("[^\\p{L}\\p{N}._-]+", "_");
        baseName = baseName.replaceAll("_+", "_");
        baseName = baseName.replaceAll("^[._-]+", "");
        baseName = baseName.replaceAll("[._-]+$", "");

        if (!isNotEmpty(baseName)) {
            return isNotEmpty(fallbackBaseName) ? fallbackBaseName : "file";
        }
        return baseName.length() > 60 ? baseName.substring(0, 60) : baseName;
    }

    /**
     * Avatar content type fallback.
     */
    private String detectPhotoContentType(String fileName) {
        String safeName = fileName != null ? fileName.toLowerCase() : "";
        if (safeName.endsWith(".png")) {
            return "image/png";
        }
        if (safeName.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    /**
     * Resume content type fallback.
     */
    private String detectResumeContentType(String fileName) {
        String safeName = fileName != null ? fileName.toLowerCase() : "";
        if (safeName.endsWith(".pdf")) return "application/pdf";
        if (safeName.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/msword";
    }

    /**
     * Does not immediately throw on single directory creation failure, later file write will return more specific IO error.
     */
    private void ensureDirectoryExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Attachment must exist and be a regular file; directory or missing file cannot be used as response resource.
     */
    private boolean isUsableFile(File file) {
        return file != null && file.exists() && file.isFile();
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Minimum resource description needed by Servlet to write file response.
     */
    public static final class FileResource {
        private final File file;
        private final String contentType;
        private final String contentDisposition;
        private final String cacheControl;

        private FileResource(File file, String contentType, String contentDisposition, String cacheControl) {
            this.file = file;
            this.contentType = contentType;
            this.contentDisposition = contentDisposition;
            this.cacheControl = cacheControl;
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
    }
}

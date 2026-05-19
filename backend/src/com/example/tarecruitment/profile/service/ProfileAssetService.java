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
 * ProfileAssetService - TA 档案附件服务。
 *
 * 负责简历、简历草稿、照片三类文件的保存、定位、删除和响应信息组装。
 * 对外只返回相对路径（例如 resumes/xxx.pdf），真实根目录由 TA_HIRING_DATA_DIR 决定。
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
     * 确保三类附件目录存在。
     *
     * 本地脚本首次运行或 TA_HIRING_DATA_DIR 指向新目录时，需要自动创建。
     */
    public void ensureDirectories() {
        ensureDirectoryExists(StoragePaths.getResumeDir());
        ensureDirectoryExists(StoragePaths.getResumeDraftDir());
        ensureDirectoryExists(StoragePaths.getPhotoDir());
    }

    /**
     * 保存正式简历，返回写入 Applicant CSV 的相对路径。
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
     * 保存头像照片，返回写入 Applicant CSV 的相对路径。
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
     * 保存简历草稿。
     *
     * 草稿先挂在 session 上，只有用户最终保存 TA 档案时才复制为正式简历。
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
     * 把会话中的草稿简历复制到正式简历目录。
     *
     * 复制而不是移动，是为了让后续保存失败时仍能保留本次上传草稿。
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
     * 构造头像响应资源。
     *
     * 返回 Optional.empty 表示前端应使用默认头像，不把缺文件当成 500。
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
            // 本地文件系统有时无法识别 webp/jpg，按扩展名做兜底，避免头像无法显示。
            contentType = detectPhotoContentType(file.getName());
        }
        return Optional.of(new FileResource(file, contentType, null, "no-store"));
    }

    /**
     * 构造简历响应资源。
     *
     * PDF 尽量内嵌预览，Word 文档交给浏览器下载，避免 iframe 预览失败。
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
        // PDF 可以内嵌预览；doc/docx 交给浏览器下载。
        String disposition = (isPdf ? "inline" : "attachment") + "; filename=\"" + file.getName() + "\"";
        return Optional.of(new FileResource(file, contentType, disposition, "no-store"));
    }

    /**
     * 把草稿简历信息存到当前会话。
     *
     * 这类状态不进入 CSV，因为用户可能只上传文件但尚未保存档案表单。
     */
    public void storeDraftResumeState(HttpSession session, String draftResumePath, String originalFileName) {
        if (session == null) {
            return;
        }
        session.setAttribute(SESSION_DRAFT_RESUME_PATH, draftResumePath);
        session.setAttribute(SESSION_DRAFT_RESUME_NAME, originalFileName != null ? originalFileName : "");
    }

    /**
     * 清理草稿简历会话状态。
     *
     * deleteFile=true 用于取消/保存完成场景，避免废弃草稿长期留在数据目录。
     */
    public void clearDraftResumeState(HttpSession session, boolean deleteFile) {
        if (session == null) {
            return;
        }
        String draftResumePath = getDraftResumePath(session);
        if (deleteFile && isNotEmpty(draftResumePath)) {
            // 草稿只属于当前会话；用户取消或保存成功后应清理，避免占用数据目录。
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
     * 把 CSV/session 中保存的相对路径解析到数据目录。
     *
     * 业务层只保存相对路径，避免暴露或固化本机绝对路径。
     */
    public File resolveStoredFile(String relativePath) {
        if (!isNotEmpty(relativePath)) {
            return null;
        }
        // 只拼接项目数据目录下的相对路径，不接受外部绝对路径作为业务文件位置。
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
     * 生成用户可读文件名。
     *
     * 存储名里含 userId 和时间戳，页面展示时尽量剥掉这些技术前缀。
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
        // 存储名带 userId 和时间戳；展示给用户时尽量还原原始文件名。
        String normalizedName = fileName.replaceFirst("^(draft_)?[^_]+_\\d+_", "");
        return isNotEmpty(normalizedName) ? normalizedName : fileName;
    }

    /**
     * 如果新旧路径不同，删除被替换的旧附件。
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
     * 生成实际存储文件名。
     *
     * 格式中包含 userId 和时间戳，既便于排查归属，也避免同名上传互相覆盖。
     */
    private String buildStoredFileName(String originalFileName, String userId, String prefix, String defaultExtension, String fallbackBaseName) {
        // 文件名保留原扩展名和可读 basename，同时加 userId/时间戳避免互相覆盖。
        String extension = extractExtension(originalFileName, defaultExtension);
        String safeBaseName = sanitizeBaseName(originalFileName, fallbackBaseName);
        return prefix + userId + "_" + System.currentTimeMillis() + "_" + safeBaseName + extension;
    }

    /**
     * 保留原扩展名；缺失时使用调用方传入的默认扩展名。
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
     * 清理文件名主体，防止路径分隔符或特殊字符进入数据目录文件名。
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
     * 头像 content type 兜底。
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
     * 简历 content type 兜底。
     */
    private String detectResumeContentType(String fileName) {
        String safeName = fileName != null ? fileName.toLowerCase() : "";
        if (safeName.endsWith(".pdf")) return "application/pdf";
        if (safeName.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/msword";
    }

    /**
     * 单目录创建失败不立即抛出，后续写文件时会返回更具体的 IO 错误。
     */
    private void ensureDirectoryExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 附件必须存在且是普通文件，目录或缺失文件都不能作为响应资源。
     */
    private boolean isUsableFile(File file) {
        return file != null && file.exists() && file.isFile();
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Servlet 写文件响应所需的最小资源描述。
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

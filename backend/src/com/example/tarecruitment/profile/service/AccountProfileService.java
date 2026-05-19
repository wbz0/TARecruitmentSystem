package com.example.tarecruitment.profile.service;

import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.common.storage.StoragePaths;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.mapper.AccountProfileResponseMapper;
import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.profile.validator.AccountProfileValidator;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * AccountProfileService - 账号资料业务服务。
 *
 * 被 AccountProfileServlet 调用，对应 /api/me/account 和 /api/me/avatar。
 * 负责显示名、实名、MO 职称、账号头像，并同步 TA 档案姓名、MO 已发布岗位展示名等跨页面字段。
 */
public class AccountProfileService {

    private static final String AVATAR_DIR_NAME = "account-avatars";
    private static AccountProfileService instance;

    private final UserDao userDao;
    private final JobDao jobDao;
    private final ApplicantDao applicantDao;
    private final ApplicationDao applicationDao;

    private AccountProfileService() {
        this.userDao = UserDao.getInstance();
        this.jobDao = JobDao.getInstance();
        this.applicantDao = ApplicantDao.getInstance();
        this.applicationDao = ApplicationDao.getInstance();
        ensureDirectoryExists(StoragePaths.getAccountAvatarDir());
    }

    public static synchronized AccountProfileService getInstance() {
        if (instance == null) {
            instance = new AccountProfileService();
        }
        return instance;
    }

    /**
     * 从 session 取当前用户，并尽量回查 CSV 最新资料。
     */
    public User currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object userObject = session.getAttribute("user");
        if (userObject instanceof User) {
            User sessionUser = (User) userObject;
            // 先回查 CSV，保证侧边栏/顶栏拿到的是最新账号资料；失败再用 session 快照。
            return userDao.findById(sessionUser.getUserId()).orElse(sessionUser);
        }

        Object userIdObject = session.getAttribute("userId");
        String userId = userIdObject != null ? String.valueOf(userIdObject) : "";
        if (!AccountProfileValidator.isNotEmpty(userId)) {
            return null;
        }
        return userDao.findById(userId).orElse(null);
    }

    /**
     * 读取当前账号资料，供共享侧边栏弹窗使用。
     */
    public ServiceResult get(User currentUser) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }
        return ServiceResult.ok(
                "Account profile retrieved successfully",
                AccountProfileResponseMapper.toPayload(currentUser, buildSharedRealName(currentUser), hasAccountAvatar(currentUser))
        );
    }

    /**
     * 更新账号展示资料和账号头像。
     *
     * TA 的实名会同步到 Applicant 档案和历史申请快照；
     * MO 的展示名会同步到已发布职位快照，保证列表不显示旧名字。
     */
    public ServiceResult update(User currentUser,
                                HttpSession session,
                                String displayName,
                                String realName,
                                String professionalTitle,
                                Part avatarPart) {
        String newAvatarPath = null;
        boolean persisted = false;
        try {
            if (currentUser == null) {
                return ServiceResult.unauthorized("Please login first");
            }
            if (currentUser.getRole() != User.Role.TA && currentUser.getRole() != User.Role.MO) {
                return ServiceResult.forbidden("Only TA or MO accounts can update account profile");
            }

            String username = AccountProfileValidator.normalizeUsername(displayName);
            String normalizedRealName = AccountProfileValidator.normalizeInput(realName);
            String normalizedTitle = currentUser.getRole() == User.Role.MO
                    ? AccountProfileValidator.normalizeInput(professionalTitle)
                    // TA 没有职称输入框，保留旧值，避免前端未传字段导致清空。
                    : safeText(currentUser.getProfessionalTitle());

            String validationError = AccountProfileValidator.validateUsernameFormat(username);
            if (validationError == null) {
                validationError = validateUsernameAvailability(username, currentUser);
            }
            if (validationError == null) {
                validationError = AccountProfileValidator.validateNames(normalizedRealName, normalizedTitle);
            }

            Optional<Applicant> taApplicant = findTaApplicant(currentUser);
            String taRealNameError = AccountProfileValidator.validateTaSharedRealName(normalizedRealName, taApplicant.isPresent());
            if (validationError != null) return ServiceResult.badRequest(validationError);
            if (taRealNameError != null) return ServiceResult.badRequest(taRealNameError);

            String previousAvatarPath = currentUser.getAvatarPath();
            String nextAvatarPath = previousAvatarPath;
            if (AccountProfileValidator.isUsableFilePart(avatarPart)) {
                // 头像是账号级头像，不等同于 TA 档案 photo；两套资源路径分开管理。
                String avatarError = AccountProfileValidator.validateAvatar(avatarPart);
                if (avatarError != null) {
                    return ServiceResult.badRequest(avatarError);
                }
                newAvatarPath = saveAvatarFile(avatarPart, currentUser.getUserId());
                nextAvatarPath = newAvatarPath;
            }

            currentUser.setUsername(username);
            currentUser.setDisplayName(username);
            currentUser.setRealName(normalizedRealName);
            if (currentUser.getRole() == User.Role.MO) {
                currentUser.setProfessionalTitle(normalizedTitle);
            }
            currentUser.setAvatarPath(nextAvatarPath);

            User saved = userDao.update(currentUser);
            persisted = true;
            // 修改账号资料后，同步所有会直接显示旧快照的业务数据。
            syncTaApplicantRealName(saved, taApplicant);
            syncMoDisplayName(saved);
            updateSessionUser(session, saved);
            cleanupReplacedAvatar(previousAvatarPath, nextAvatarPath);

            return ServiceResult.ok(
                    "Account profile updated successfully",
                    AccountProfileResponseMapper.toPayload(saved, buildSharedRealName(saved), hasAccountAvatar(saved))
            );
        } catch (IllegalArgumentException e) {
            if (!persisted) {
                deleteNewAvatar(newAvatarPath);
            }
            return ServiceResult.badRequest(e.getMessage());
        } catch (Exception e) {
            if (!persisted) {
                deleteNewAvatar(newAvatarPath);
            }
            return ServiceResult.serverError("An error occurred. Please try again later.");
        }
    }

    /**
     * 返回账号头像资源。
     *
     * 只允许 account-avatars/ 下的文件，避免 avatarPath 被构造成任意文件读取。
     */
    public Optional<AvatarResource> avatar(User user) throws IOException {
        String avatarPath = safeText(user.getAvatarPath());
        if (!isAccountAvatarPath(avatarPath)) {
            return Optional.empty();
        }

        File file = new File(StoragePaths.getDataDir(), avatarPath);
        if (!file.exists() || !file.isFile()) {
            return Optional.empty();
        }

        String contentType = Files.probeContentType(file.toPath());
        if (!AccountProfileValidator.isNotEmpty(contentType) || !contentType.startsWith("image/")) {
            contentType = detectImageContentType(file.getName());
        }
        return Optional.of(new AvatarResource(file, contentType, "private, max-age=300"));
    }

    /**
     * 校验新用户名/展示名是否被其他账号占用。
     */
    private String validateUsernameAvailability(String username, User currentUser) {
        Optional<User> existing = userDao.findByUsername(username);
        if (existing.isPresent()
                && currentUser != null
                && !safeText(existing.get().getUserId()).equals(safeText(currentUser.getUserId()))) {
            return "Username already exists";
        }
        return null;
    }

    /**
     * TA 账号对应的 Applicant 档案。
     */
    private Optional<Applicant> findTaApplicant(User user) {
        if (user == null || user.getRole() != User.Role.TA) {
            return Optional.empty();
        }
        return applicantDao.findByUserId(user.getUserId());
    }

    /**
     * TA 优先使用 Applicant.fullName 作为共享实名，否则使用 User.realName。
     */
    private String buildSharedRealName(User user) {
        if (user == null) {
            return "";
        }
        Optional<Applicant> taApplicant = findTaApplicant(user);
        if (taApplicant.isPresent() && AccountProfileValidator.isNotEmpty(taApplicant.get().getFullName())) {
            // TA 的实名和档案 fullName 共用一个展示源，避免侧边栏和申请详情显示不同名字。
            return safeText(taApplicant.get().getFullName());
        }
        return safeText(user.getRealName());
    }

    /**
     * 同步 TA 账号实名到 Applicant 档案。
     */
    private void syncTaApplicantRealName(User user, Optional<Applicant> existingApplicant) {
        if (user == null || user.getRole() != User.Role.TA || existingApplicant.isEmpty()) {
            return;
        }

        String realName = safeText(user.getRealName()).trim();
        if (!AccountProfileValidator.isNotEmpty(realName)) {
            return;
        }

        Applicant applicant = existingApplicant.get();
        if (!realName.equals(safeText(applicant.getFullName()))) {
            applicant.setFullName(realName);
            Applicant savedApplicant = applicantDao.update(applicant);
            // 申请记录里保存了 applicantName 快照，因此档案实名变化后需要同步。
            syncApplicationApplicantName(savedApplicant);
        }
    }

    /**
     * 同步历史申请中的 applicantName 快照。
     */
    private void syncApplicationApplicantName(Applicant applicant) {
        if (applicant == null || !AccountProfileValidator.isNotEmpty(applicant.getApplicantId())) {
            return;
        }

        String fullName = safeText(applicant.getFullName()).trim();
        if (!AccountProfileValidator.isNotEmpty(fullName)) {
            return;
        }

        for (Application application : applicationDao.findByApplicantId(applicant.getApplicantId())) {
            if (!fullName.equals(safeText(application.getApplicantName()))) {
                application.setApplicantName(fullName);
                applicationDao.update(application);
            }
        }
    }

    /**
     * 同步 MO 已发布职位中的 moName 快照。
     */
    private void syncMoDisplayName(User user) {
        if (user == null || user.getRole() != User.Role.MO) {
            return;
        }

        String displayName = buildMoDisplayName(user);
        for (Job job : jobDao.findByMoId(user.getUserId())) {
            // 岗位 CSV 保存了 moName 快照，TA 职位列表不需要每次回查 User。
            job.setMoName(displayName);
            jobDao.update(job);
        }
    }

    /**
     * 构建 MO 展示名：职称 + 实名优先，其次 displayName/username。
     */
    private String buildMoDisplayName(User user) {
        String realName = safeText(user.getRealName()).trim();
        String title = safeText(user.getProfessionalTitle()).trim();
        if (!realName.isEmpty()) {
            return title.isEmpty() ? realName : title + " " + realName;
        }
        String displayName = safeText(user.getDisplayName()).trim();
        return displayName.isEmpty() ? safeText(user.getUsername()) : displayName;
    }

    /**
     * 保存账号头像文件并返回相对路径。
     */
    private String saveAvatarFile(Part avatarPart, String userId) throws IOException {
        ensureDirectoryExists(StoragePaths.getAccountAvatarDir());
        String originalName = avatarPart.getSubmittedFileName();
        String extension = AccountProfileValidator.extractExtension(originalName, ".jpg");
        String baseName = AccountProfileValidator.sanitizeBaseName(originalName, "avatar");
        String fileName = userId + "_" + System.currentTimeMillis() + "_" + baseName + extension;
        File target = new File(StoragePaths.getAccountAvatarDir(), fileName);
        Files.copy(avatarPart.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return AVATAR_DIR_NAME + "/" + fileName;
    }

    /**
     * 删除被替换的旧账号头像。
     */
    private void cleanupReplacedAvatar(String previousAvatarPath, String currentAvatarPath) {
        if (!isAccountAvatarPath(previousAvatarPath) || previousAvatarPath.equals(currentAvatarPath)) {
            return;
        }
        File file = new File(StoragePaths.getDataDir(), previousAvatarPath);
        if (file.exists() && !file.delete()) {
            Logger.i("AccountProfileService", "Unable to delete old account avatar: " + previousAvatarPath);
        }
    }

    /**
     * 保存失败时清理新写入但未持久化到用户记录的头像。
     */
    private void deleteNewAvatar(String newAvatarPath) {
        if (!isAccountAvatarPath(newAvatarPath)) {
            return;
        }
        File file = new File(StoragePaths.getDataDir(), newAvatarPath);
        if (file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    /**
     * 判断用户是否配置了账号头像。
     */
    private boolean hasAccountAvatar(User user) {
        return user != null && isAccountAvatarPath(user.getAvatarPath());
    }

    /**
     * 校验账号头像路径只指向 account-avatars/ 下的单个文件。
     */
    private boolean isAccountAvatarPath(String path) {
        String value = safeText(path).trim();
        if (!value.startsWith(AVATAR_DIR_NAME + "/")) {
            return false;
        }
        String fileName = value.substring((AVATAR_DIR_NAME + "/").length());
        // 只允许 account-avatars 目录下的单个文件名，防止通过 avatarPath 读取任意文件。
        return AccountProfileValidator.isNotEmpty(fileName)
                && !fileName.contains("/")
                && !fileName.contains("\\")
                && !fileName.contains("..");
    }

    /**
     * 写回 session，确保保存后当前请求后续页面拿到新账号信息。
     */
    private void updateSessionUser(HttpSession session, User user) {
        if (session == null || user == null) {
            return;
        }
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().name());
    }

    /**
     * 头像 content type 兜底。
     */
    private String detectImageContentType(String fileName) {
        String safeName = fileName != null ? fileName.toLowerCase() : "";
        if (safeName.endsWith(".png")) return "image/png";
        if (safeName.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    /**
     * 创建头像目录。
     */
    private void ensureDirectoryExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * null 安全字符串。
     */
    private String safeText(String value) {
        return value == null ? "" : value;
    }

    /**
     * Servlet 返回头像文件所需的资源描述。
     */
    public static final class AvatarResource {
        private final File file;
        private final String contentType;
        private final String cacheControl;

        private AvatarResource(File file, String contentType, String cacheControl) {
            this.file = file;
            this.contentType = contentType;
            this.cacheControl = cacheControl;
        }

        public File getFile() {
            return file;
        }

        public String getContentType() {
            return contentType;
        }

        public String getCacheControl() {
            return cacheControl;
        }
    }
}

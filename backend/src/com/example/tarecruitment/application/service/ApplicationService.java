package com.example.tarecruitment.application.service;

import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.application.mapper.ApplicationResponseMapper;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.application.validator.ApplicationValidator;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.search.FuzzySearchUtil;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.model.Applicant;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 申请业务流程服务。
 *
 * ApplicationServlet 只读取 HTTP 参数并调用这里；这里负责列表可见范围、
 * 创建申请、接受/拒绝/撤回状态流转，以及职位名额填满后的状态同步。
 */
public class ApplicationService {

    private static ApplicationService instance;

    private final ApplicantDao applicantDao;
    private final ApplicationDao applicationDao;
    private final JobDao jobDao;

    private ApplicationService() {
        this.applicantDao = ApplicantDao.getInstance();
        this.applicationDao = ApplicationDao.getInstance();
        this.jobDao = JobDao.getInstance();
    }

    public static synchronized ApplicationService getInstance() {
        if (instance == null) {
            instance = new ApplicationService();
        }
        return instance;
    }

    /**
     * 按当前登录角色查询申请列表。
     *
     * TA 只能看自己的申请，MO 只能看自己发布职位下的申请，ADMIN 用于后台审计。
     * applicantId/jobId/moId/status/keyword 都是列表页筛选条件，不改变数据。
     */
    public ServiceResult list(User currentUser,
                              String applicantId,
                              String jobId,
                              String moId,
                              String status,
                              String keyword) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }

        List<Application> applications;
        // 不同角色看到的申请列表不同：TA 看自己的申请，MO 看自己职位收到的申请，Admin 看全部。
        if (currentUser.getRole() == User.Role.TA) {
            applications = applicationDao.findByApplicantId(currentUser.getUserId());
        } else if (currentUser.getRole() == User.Role.MO) {
            applications = applicationDao.findByMoId(currentUser.getUserId());
        } else if (currentUser.getRole() == User.Role.ADMIN) {
            applications = applicationDao.findAll();
        } else {
            return ServiceResult.forbidden("Unauthorized role");
        }

        String normalizedJobId = trim(jobId);
        if (!normalizedJobId.isEmpty()) {
            applications = applications.stream()
                    .filter(a -> normalizedJobId.equals(a.getJobId()))
                    .collect(Collectors.toList());
        }

        String normalizedApplicantId = trim(applicantId);
        if (!normalizedApplicantId.isEmpty()) {
            if (currentUser.getRole() == User.Role.MO) {
                // MO 继续在自己可见列表里筛选，不能借 applicantId 越权查看其他职位申请。
                applications = applications.stream()
                        .filter(a -> normalizedApplicantId.equals(a.getApplicantId()))
                        .collect(Collectors.toList());
            } else {
                // TA/Admin 入口保留 applicantId 查询能力，用于自己的申请页或后台排查。
                applications = applicationDao.findByApplicantId(normalizedApplicantId);
            }
        }

        String normalizedMoId = trim(moId);
        if (!normalizedMoId.isEmpty()) {
            applications = applications.stream()
                    .filter(a -> normalizedMoId.equals(a.getMoId()))
                    .collect(Collectors.toList());
        }

        String normalizedStatus = trim(status);
        if (!normalizedStatus.isEmpty()) {
            try {
                Application.Status appStatus = Application.Status.valueOf(normalizedStatus.toUpperCase());
                applications = applications.stream()
                        .filter(a -> a.getStatus() == appStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                applications = List.of();
            }
        }

        FuzzySearchUtil.SearchOutcome<Application> searchOutcome = FuzzySearchUtil.search(
                applications,
                keyword,
                application -> ApplicationResponseMapper.searchFieldsForRole(application, currentUser.getRole())
        );
        // approximateOnly 会传到前端，用于提示“没有精确匹配，展示相近结果”。
        List<Application> visibleApplications = searchOutcome.getItems();

        return ServiceResult.ok(
                "Applications retrieved successfully",
                ApplicationResponseMapper.toListPayload(visibleApplications, searchOutcome)
        );
    }

    /**
     * 查询单个申请详情。
     *
     * 详情接口会再次校验记录归属，不能只依赖列表页已经过滤过的结果。
     */
    public ServiceResult detail(User currentUser, String applicationId) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }
        String error = ApplicationValidator.validateApplicationId(applicationId);
        if (error != null) {
            return ServiceResult.badRequest(error);
        }

        Optional<Application> appOpt = applicationDao.findById(applicationId.trim());
        if (appOpt.isEmpty()) {
            return ServiceResult.notFound("Application not found");
        }

        Application application = appOpt.get();
        if (!canAccessApplication(currentUser, application)) {
            return ServiceResult.forbidden("You don't have permission to view this application");
        }

        return ServiceResult.ok("Application retrieved successfully", ApplicationResponseMapper.toPayload(application));
    }

    /**
     * TA 提交职位申请。
     *
     * 创建前会确认职位仍开放、未过截止日期、TA 已有档案和简历，并禁止同一职位重复申请。
     */
    public ServiceResult create(User currentUser, String jobId, String coverLetter) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }
        if (currentUser.getRole() != User.Role.TA) {
            return ServiceResult.forbidden("Only TA can apply for jobs");
        }

        String normalizedJobId = trim(jobId);
        String jobIdError = ApplicationValidator.validateJobId(normalizedJobId);
        if (jobIdError != null) {
            return ServiceResult.badRequest(jobIdError);
        }
        String coverLetterError = ApplicationValidator.validateCoverLetter(coverLetter);
        if (coverLetterError != null) {
            return ServiceResult.badRequest(coverLetterError);
        }

        Optional<Job> jobOpt = jobDao.findById(normalizedJobId);
        if (jobOpt.isEmpty()) {
            return ServiceResult.notFound("Job not found");
        }

        Job job = jobOpt.get();
        if (job.getStatus() != Job.Status.OPEN) {
            return ServiceResult.badRequest("This job is no longer accepting applications");
        }
        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDateTime.now())) {
            return ServiceResult.badRequest("The application deadline for this job has passed");
        }

        Optional<Applicant> applicantOpt = applicantDao.findByUserId(currentUser.getUserId());
        if (applicantOpt.isEmpty()) {
            return ServiceResult.badRequest("Please create your applicant profile before applying");
        }

        Applicant applicant = applicantOpt.get();
        if (trim(applicant.getResumePath()).isEmpty()) {
            return ServiceResult.badRequest("Please upload your resume before applying");
        }

        // 同一 TA 对同一职位只允许一条申请，避免 MO 侧审核列表出现重复候选人。
        if (applicationDao.hasApplied(normalizedJobId, currentUser.getUserId())) {
            return ServiceResult.badRequest("You have already applied for this job");
        }

        Application application = new Application();
        // 申请里冗余保存职位和 MO 基本信息，便于 CSV 列表直接渲染历史申请。
        // 即使职位标题后续被 MO 修改，旧申请列表仍可保留当时的显示信息。
        application.setJobId(normalizedJobId);
        application.setApplicantId(currentUser.getUserId());
        application.setApplicantName(applicant.getFullName());
        application.setApplicantEmail(currentUser.getEmail());
        application.setJobTitle(job.getTitle());
        application.setCourseCode(job.getCourseCode());
        application.setMoId(job.getMoId());
        application.setMoName(job.getMoName());

        String normalizedCoverLetter = trim(coverLetter);
        application.setCoverLetter(normalizedCoverLetter.isEmpty() ? null : normalizedCoverLetter);

        Application savedApplication = applicationDao.create(application);
        return ServiceResult.created(
                "Application submitted successfully!",
                idPayload(savedApplication.getApplicationId())
        );
    }

    /**
     * 统一处理申请状态动作。
     *
     * 前端只提交 action 文本；这里把 accept/reject/withdraw 分发到各自的
     * 权限和状态流转函数，避免 Servlet 承担业务分支。
     */
    public ServiceResult transition(User currentUser, String applicationId, String action) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }

        String idError = ApplicationValidator.validateApplicationId(applicationId);
        if (idError != null) {
            return ServiceResult.badRequest(idError);
        }
        String actionError = ApplicationValidator.validateTransitionAction(action);
        if (actionError != null) {
            return ServiceResult.badRequest(actionError);
        }

        Optional<Application> appOpt = applicationDao.findById(applicationId.trim());
        if (appOpt.isEmpty()) {
            return ServiceResult.notFound("Application not found");
        }

        Application application = appOpt.get();
        String normalizedAction = action.trim().toLowerCase();
        return switch (normalizedAction) {
            case "accept" -> accept(application, currentUser);
            case "reject" -> reject(application, currentUser);
            case "withdraw" -> withdraw(application, currentUser);
            default -> ServiceResult.badRequest("Invalid action. Use 'accept', 'reject', or 'withdraw'");
        };
    }

    /**
     * MO 接受申请，并在岗位满员时同步职位状态。
     */
    private ServiceResult accept(Application application, User currentUser) {
        ServiceResult reviewPermission = validateMoReviewPermission(application, currentUser, "accept");
        if (reviewPermission != null) {
            return reviewPermission;
        }

        Optional<Job> jobOpt = jobDao.findById(application.getJobId());
        if (jobOpt.isEmpty()) {
            return ServiceResult.notFound("Job not found for this application");
        }

        Job job = jobOpt.get();
        if (job.getStatus() != Job.Status.OPEN) {
            return ServiceResult.badRequest("This job is no longer open for accepting applications");
        }

        long acceptedCount = applicationDao.countAcceptedByJobId(job.getJobId());
        if (acceptedCount >= job.getPositions()) {
            // 名额已满时同步职位状态，避免后续 TA 继续看到可申请的开放岗位。
            if (job.getStatus() != Job.Status.FILLED) {
                job.setStatus(Job.Status.FILLED);
                jobDao.update(job);
            }
            return ServiceResult.badRequest("This job has already filled all available positions");
        }

        boolean updated = applicationDao.accept(application.getApplicationId());
        if (!updated) {
            return ServiceResult.serverError("Failed to accept application");
        }

        long updatedAcceptedCount = applicationDao.countAcceptedByJobId(job.getJobId());
        if (updatedAcceptedCount >= job.getPositions() && job.getStatus() != Job.Status.FILLED) {
            // 接受本次申请后刚好满员，也要立刻把职位标记为 FILLED。
            job.setStatus(Job.Status.FILLED);
            jobDao.update(job);
        }
        return updatedApplication("Application accepted successfully!", application.getApplicationId());
    }

    /**
     * MO 拒绝申请。
     *
     * 拒绝不影响职位名额，只更新申请记录本身。
     */
    private ServiceResult reject(Application application, User currentUser) {
        ServiceResult reviewPermission = validateMoReviewPermission(application, currentUser, "reject");
        if (reviewPermission != null) {
            return reviewPermission;
        }

        boolean updated = applicationDao.reject(application.getApplicationId());
        if (!updated) {
            return ServiceResult.serverError("Failed to reject application");
        }
        return updatedApplication("Application rejected successfully!", application.getApplicationId());
    }

    /**
     * 撤回申请。
     *
     * 常规入口是 TA 撤回自己的待处理申请；MO/Admin 权限保留给异常数据处理。
     */
    private ServiceResult withdraw(Application application, User currentUser) {
        // 撤回允许 TA 撤自己的申请，也允许 MO/Admin 处理异常申请记录。
        boolean canWithdraw = false;
        if (currentUser.getRole() == User.Role.TA && currentUser.getUserId().equals(application.getApplicantId())) {
            canWithdraw = true;
        } else if (currentUser.getRole() == User.Role.MO && currentUser.getUserId().equals(application.getMoId())) {
            canWithdraw = true;
        } else if (currentUser.getRole() == User.Role.ADMIN) {
            canWithdraw = true;
        }

        if (!canWithdraw) {
            return ServiceResult.forbidden("You don't have permission to withdraw this application");
        }
        if (application.getStatus() != Application.Status.PENDING) {
            return ServiceResult.badRequest("This application can no longer be withdrawn");
        }

        boolean updated = applicationDao.withdraw(application.getApplicationId());
        if (!updated) {
            return ServiceResult.serverError("Failed to withdraw application");
        }
        return updatedApplication("Application withdrawn successfully!", application.getApplicationId());
    }

    /**
     * 校验 MO 是否能对这条申请做接受/拒绝。
     *
     * 只有职位发布者本人可以审核，并且只能审核仍处于 PENDING 的申请。
     */
    private ServiceResult validateMoReviewPermission(Application application, User currentUser, String action) {
        if (currentUser.getRole() != User.Role.MO) {
            return ServiceResult.forbidden("Only MO can " + action + " applications");
        }
        if (application.getMoId() == null || !application.getMoId().equals(currentUser.getUserId())) {
            return ServiceResult.forbidden("You can only review applications for your own jobs");
        }
        if (application.getStatus() != Application.Status.PENDING) {
            return ServiceResult.badRequest("This application has already been reviewed");
        }
        return null;
    }

    /**
     * 状态修改后重新读取申请，保证响应里包含 DAO 写回后的最新时间戳和阶段。
     */
    private ServiceResult updatedApplication(String message, String applicationId) {
        Optional<Application> updatedApp = applicationDao.findById(applicationId);
        if (updatedApp.isEmpty()) {
            return ServiceResult.serverError("Failed to retrieve updated application");
        }
        return ServiceResult.ok(message, ApplicationResponseMapper.toPayload(updatedApp.get()));
    }

    /**
     * 详情权限边界：
     * Admin 全量可见，TA 只能看自己的申请，MO 只能看自己职位下的申请。
     */
    private boolean canAccessApplication(User currentUser, Application application) {
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (currentUser.getRole() == User.Role.TA) {
            return currentUser.getUserId().equals(application.getApplicantId());
        }
        return currentUser.getRole() == User.Role.MO
                && application.getMoId() != null
                && application.getMoId().equals(currentUser.getUserId());
    }

    /**
     * 查询参数统一压成空字符串，避免每个筛选分支重复判空。
     */
    private String trim(String value) {
        return value != null ? value.trim() : "";
    }

    /**
     * 创建成功后只返回 applicationId，完整列表由前端按需重新加载。
     */
    private Map<String, Object> idPayload(String applicationId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicationId", applicationId);
        return data;
    }
}

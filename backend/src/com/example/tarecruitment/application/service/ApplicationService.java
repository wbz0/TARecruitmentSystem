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
 * Application business flow service.
 *
 * ApplicationServlet only reads HTTP parameters and calls here; this service is responsible for list visibility scope,
 * application creation, accept/reject/withdraw status transitions, and job slot synchronization when positions are filled.
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
     * Query application list by currently logged-in role.
     *
     * TA can only see their own applications, MO can only see applications for jobs they posted, ADMIN for backend audit.
     * applicantId/jobId/moId/status/keyword are all list page filter conditions, do not change data.
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
        // Different roles see different application lists: TA sees their own, MO sees applications for their jobs, Admin sees all.
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
                // MO continues filtering within their visible list, cannot use applicantId to cross-check other job applications.
                applications = applications.stream()
                        .filter(a -> normalizedApplicantId.equals(a.getApplicantId()))
                        .collect(Collectors.toList());
            } else {
                // TA/Admin entry retains applicantId query capability for their own application page or backend troubleshooting.
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
        // approximateOnly will be passed to frontend to hint “no exact match, showing approximate results”.
        List<Application> visibleApplications = searchOutcome.getItems();

        return ServiceResult.ok(
                "Applications retrieved successfully",
                ApplicationResponseMapper.toListPayload(visibleApplications, searchOutcome)
        );
    }

    /**
     * Query single application detail.
     *
     * Detail API will again verify record ownership, cannot rely only on already-filtered results from list page.
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
     * TA submits job application.
     *
     * Before creation, verifies job is still open, not past deadline, TA has profile and resume, and prohibits duplicate applications for same job.
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

        // Same TA is only allowed one application for the same job, to avoid duplicate candidates appearing in MO's review list.
        if (applicationDao.hasApplied(normalizedJobId, currentUser.getUserId())) {
            return ServiceResult.badRequest("You have already applied for this job");
        }

        Application application = new Application();
        // Application redundantly stores job and MO basic info for CSV list to directly render historical applications.
        // Even if job title is later modified by MO, old application list can still retain the display info from that time.
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
     * Unified handling of application status actions.
     *
     * Frontend only submits action text; here accept/reject/withdraw are dispatched to their own
     * permission and status transition functions, avoiding Servlet from bearing business branches.
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
     * MO accepts application and synchronizes job status when positions are filled.
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
            // When slots are filled, synchronize job status to avoid subsequent TAs continuing to see open jobs they can apply for.
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
            // After accepting this application and becoming full, also immediately mark the job as FILLED.
            job.setStatus(Job.Status.FILLED);
            jobDao.update(job);
        }
        return updatedApplication("Application accepted successfully!", application.getApplicationId());
    }

    /**
     * MO rejects application.
     *
     * Rejection does not affect job slots, only updates the application record itself.
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
     * Withdraw application.
     *
     * Normal entry is TA withdrawing their own pending application; MO/Admin permissions reserved for exceptional data handling.
     */
    private ServiceResult withdraw(Application application, User currentUser) {
        // Withdrawal allows TA to withdraw their own, also allows MO/Admin to handle exceptional application records.
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
     * Validate if MO can accept/reject this application.
     *
     * Only the job poster can review, and can only review applications still in PENDING status.
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
     * Re-read application after status modification to ensure response contains latest timestamp and stage written back by DAO.
     */
    private ServiceResult updatedApplication(String message, String applicationId) {
        Optional<Application> updatedApp = applicationDao.findById(applicationId);
        if (updatedApp.isEmpty()) {
            return ServiceResult.serverError("Failed to retrieve updated application");
        }
        return ServiceResult.ok(message, ApplicationResponseMapper.toPayload(updatedApp.get()));
    }

    /**
     * Detail permission boundary:
     * Admin sees all, TA can only see their own applications, MO can only see applications for their jobs.
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
     * Unify query parameters to empty string to avoid repetitive null checks in each filter branch.
     */
    private String trim(String value) {
        return value != null ? value.trim() : "";
    }

    /**
     * After successful creation, only return applicationId; full list is reloaded by frontend as needed.
     */
    private Map<String, Object> idPayload(String applicationId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicationId", applicationId);
        return data;
    }
}

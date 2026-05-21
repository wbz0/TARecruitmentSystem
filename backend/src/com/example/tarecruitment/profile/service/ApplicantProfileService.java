package com.example.tarecruitment.profile.service;

import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.mapper.ApplicantProfileRequestMapper;
import com.example.tarecruitment.profile.mapper.ApplicantProfileResponseMapper;
import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.profile.validator.ApplicantProfileInput;
import com.example.tarecruitment.profile.validator.ApplicantProfileValidator;
import com.example.tarecruitment.profile.validator.ProfileAssetValidator;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ApplicantProfileService - Current TA applicant profile business service.
 *
 * Called by ApplicantProfileServlet, corresponds to /api/me/applicant-profile.
 * It is responsible for profile create/update, resume draft to formal file, photo deletion, student ID uniqueness,
 * and syncing TA real name to account and historical application snapshot to ensure consistent display across multiple frontend pages.
 */
public class ApplicantProfileService {

    private static ApplicantProfileService instance;

    private final ApplicantDao applicantDao;
    private final ApplicationDao applicationDao;
    private final UserDao userDao;
    private final ProfileAssetService assetService;

    private ApplicantProfileService() {
        this.applicantDao = ApplicantDao.getInstance();
        this.applicationDao = ApplicationDao.getInstance();
        this.userDao = UserDao.getInstance();
        this.assetService = ProfileAssetService.getInstance();
    }

    public static synchronized ApplicantProfileService getInstance() {
        if (instance == null) {
            instance = new ApplicantProfileService();
        }
        return instance;
    }

    public ServiceResult get(User currentUser, HttpSession session) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }

        Optional<Applicant> applicantOpt = applicantDao.findByUserId(currentUser.getUserId());
        if (applicantOpt.isEmpty()) {
            // TA first entering profile page may not have saved profile yet, but session may have pending resume draft.
            return ServiceResult.of(404, false, "Applicant profile not found",
                    ApplicantProfileResponseMapper.draftResumePayload(session, assetService));
        }

        return ServiceResult.ok(
                "Applicant profile retrieved successfully",
                ApplicantProfileResponseMapper.toPayload(applicantOpt.get(), session, assetService)
        );
    }

    public ServiceResult saveForm(User currentUser,
                                  HttpSession session,
                                  ApplicantProfileInput input,
                                  boolean isUpdate) {
        String newResumePath = null;
        boolean persisted = false;
        try {
            if (currentUser == null) {
                return ServiceResult.unauthorized("Please login first");
            }

            Optional<Applicant> existingApplicant = applicantDao.findByUserId(currentUser.getUserId());
            if (isUpdate && existingApplicant.isEmpty()) {
                return ServiceResult.notFound("Applicant profile not found. Please create one first.");
            }
            if (!isUpdate && existingApplicant.isPresent()) {
                return ServiceResult.of(409, false, "Applicant profile already exists. Use PUT to update.", null);
            }

            String error = ApplicantProfileValidator.validateInput(input, true);
            if (error != null) {
                return ServiceResult.badRequest(error);
            }

            ServiceResult duplicateStudentId = validateStudentIdAvailability(input.getStudentId(), existingApplicant, isUpdate);
            if (duplicateStudentId != null) {
                return duplicateStudentId;
            }

            Applicant applicant = isUpdate ? existingApplicant.get() : new Applicant();
            if (!isUpdate) {
                applicant.setUserId(currentUser.getUserId());
            }

            String previousResumePath = applicant.getResumePath();
            String previousPhotoPath = applicant.getPhotoPath();
            applyAllFields(applicant, input);

            String draftResumePath = assetService.getDraftResumePath(session);
            String draftResumeName = assetService.getDraftResumeName(session);
            boolean clearDraftAfterSave = false;
            if (ApplicantProfileValidator.isNotEmpty(draftResumePath)) {
                // For regular form save, first copy separately uploaded draft resume to formal resumes directory.
                newResumePath = assetService.copyDraftResumeToFinal(draftResumePath, currentUser.getUserId(), draftResumeName);
                applicant.setResumePath(newResumePath);
                clearDraftAfterSave = true;
            }

            if (input.isRemovePhoto()) {
                // removePhoto comes from frontend explicit avatar delete action; does not mean no photo uploaded this time.
                applicant.setPhotoPath("");
            }

            if (!ApplicantProfileValidator.isNotEmpty(applicant.getResumePath())) {
                return ServiceResult.badRequest("Please upload your resume before saving your profile.");
            }

            Applicant savedApplicant = saveApplicant(applicant, isUpdate);
            persisted = true;
            // TA fullName is the common source for account realName and applicantName in application list.
            syncAccountRealName(currentUser, savedApplicant.getFullName(), session);
            syncApplicationApplicantName(savedApplicant);

            if (clearDraftAfterSave) {
                assetService.clearDraftResumeState(session, true);
            }
            assetService.cleanupReplacedResume(previousResumePath, savedApplicant.getResumePath());
            assetService.cleanupReplacedPhoto(previousPhotoPath, savedApplicant.getPhotoPath());

            String message = isUpdate ? "Applicant profile updated successfully!" : "Applicant profile created successfully!";
            Object data = ApplicantProfileResponseMapper.savedPayload(savedApplicant, draftResumeName, "", assetService);
            return isUpdate ? ServiceResult.ok(message, data) : ServiceResult.created(message, data);
        } catch (IllegalArgumentException e) {
            if (!persisted && ApplicantProfileValidator.isNotEmpty(newResumePath)) {
                assetService.deleteStoredFile(newResumePath);
            }
            return ServiceResult.badRequest(e.getMessage());
        } catch (Exception e) {
            if (!persisted && ApplicantProfileValidator.isNotEmpty(newResumePath)) {
                assetService.deleteStoredFile(newResumePath);
            }
            return ServiceResult.serverError("An error occurred. Please try again later.");
        }
    }

    public ServiceResult saveUpload(User currentUser,
                                    HttpSession session,
                                    ApplicantProfileRequestMapper.ApplicantProfileUpload upload) {
        String newResumePath = null;
        String newPhotoPath = null;
        boolean persisted = false;
        try {
            if (currentUser == null) {
                return ServiceResult.unauthorized("Please login first");
            }

            Optional<Applicant> applicantOpt = applicantDao.findByUserId(currentUser.getUserId());
            boolean isUpdate = applicantOpt.isPresent();
            Applicant applicant = isUpdate ? applicantOpt.get() : new Applicant();
            if (!isUpdate) {
                applicant.setUserId(currentUser.getUserId());
            }

            ApplicantProfileInput input = upload.getInput();
            String validationError = isUpdate
                    ? ApplicantProfileValidator.validatePartialInput(input)
                    : ApplicantProfileValidator.validateInput(input, true);
            if (validationError != null) {
                return ServiceResult.badRequest(validationError);
            }

            if (!isUpdate) {
                ServiceResult duplicateStudentId = validateStudentIdAvailability(input.getStudentId(), applicantOpt, false);
                if (duplicateStudentId != null) {
                    return duplicateStudentId;
                }
            } else if (input.getStudentIdRaw() != null) {
                ServiceResult duplicateStudentId = validateStudentIdAvailability(input.getStudentId(), applicantOpt, true);
                if (duplicateStudentId != null) {
                    return duplicateStudentId;
                }
            }

            String previousResumePath = applicant.getResumePath();
            String previousPhotoPath = applicant.getPhotoPath();
            String currentResumeName = assetService.getDraftResumeName(session);
            String currentPhotoName = "";
            boolean clearDraftAfterSave = false;

            Part resumePart = upload.getResumePart();
            if (resumePart != null) {
                // multipart save path is “direct file submission”; non-multipart uses draft resume state.
                String fileError = ProfileAssetValidator.validateResumeFile(resumePart);
                if (fileError != null) {
                    return ServiceResult.badRequest(fileError);
                }
                newResumePath = assetService.saveResumeFile(resumePart, currentUser.getUserId());
                applicant.setResumePath(newResumePath);
                currentResumeName = ProfileAssetValidator.extractFileName(resumePart);
                clearDraftAfterSave = assetService.hasDraftResume(session);
            } else if (assetService.hasDraftResume(session)) {
                // Allow user to upload draft resume first, then save other profile fields with regular form.
                newResumePath = assetService.copyDraftResumeToFinal(
                        assetService.getDraftResumePath(session),
                        currentUser.getUserId(),
                        currentResumeName
                );
                applicant.setResumePath(newResumePath);
                clearDraftAfterSave = true;
            }

            if (!ApplicantProfileValidator.isNotEmpty(applicant.getResumePath())) {
                return ServiceResult.badRequest("Please upload your resume before saving your profile.");
            }

            if (input.isRemovePhoto()) {
                applicant.setPhotoPath("");
            }

            Part photoPart = upload.getPhotoPart();
            if (photoPart != null) {
                String photoError = ProfileAssetValidator.validatePhotoFile(photoPart);
                if (photoError != null) {
                    cleanupNewFiles(newResumePath, null);
                    return ServiceResult.badRequest(photoError);
                }
                newPhotoPath = assetService.savePhotoFile(photoPart, currentUser.getUserId());
                applicant.setPhotoPath(newPhotoPath);
                currentPhotoName = ProfileAssetValidator.extractFileName(photoPart);
            }

            if (isUpdate) {
                // PUT/multipart update only overwrites fields present in request; avoid clearing unset or unshown fields.
                applyProvidedFields(applicant, input);
            } else {
                applyAllFields(applicant, input);
            }

            Applicant savedApplicant = saveApplicant(applicant, isUpdate);
            persisted = true;
            syncAccountRealName(currentUser, savedApplicant.getFullName(), session);
            syncApplicationApplicantName(savedApplicant);

            if (clearDraftAfterSave) {
                assetService.clearDraftResumeState(session, true);
            }
            assetService.cleanupReplacedResume(previousResumePath, savedApplicant.getResumePath());
            assetService.cleanupReplacedPhoto(previousPhotoPath, savedApplicant.getPhotoPath());

            String message = isUpdate ? "Applicant profile updated successfully!" : "Applicant profile created successfully!";
            Object data = ApplicantProfileResponseMapper.savedPayload(savedApplicant, currentResumeName, currentPhotoName, assetService);
            return isUpdate ? ServiceResult.ok(message, data) : ServiceResult.created(message, data);
        } catch (IllegalArgumentException e) {
            if (!persisted) {
                cleanupNewFiles(newResumePath, newPhotoPath);
            }
            return ServiceResult.badRequest(e.getMessage());
        } catch (Exception e) {
            if (!persisted) {
                cleanupNewFiles(newResumePath, newPhotoPath);
            }
            return ServiceResult.serverError("An error occurred. Please try again later.");
        }
    }

    private Applicant saveApplicant(Applicant applicant, boolean isUpdate) {
        if (isUpdate) {
            applicant.setUpdatedAt(LocalDateTime.now());
            return applicantDao.update(applicant);
        }
        return applicantDao.create(applicant);
    }

    private ServiceResult validateStudentIdAvailability(String studentId,
                                                        Optional<Applicant> currentApplicant,
                                                        boolean isUpdate) {
        // studentId is one of TA profile unique keys; updating own original student ID is not considered duplicate.
        Optional<Applicant> existingWithStudentId = applicantDao.findByStudentId(studentId);
        if (existingWithStudentId.isPresent()) {
            if (!isUpdate || currentApplicant.isEmpty()
                    || !existingWithStudentId.get().getApplicantId().equals(currentApplicant.get().getApplicantId())) {
                return ServiceResult.badRequest("Student ID already exists");
            }
        }
        return null;
    }

    private void applyAllFields(Applicant applicant, ApplicantProfileInput input) {
        applicant.setFullName(input.getFullName());
        applicant.setStudentId(input.getStudentId());
        applicant.setDepartment(input.getDepartment());
        applicant.setProgram(input.getProgram());
        applicant.setGpa(input.getGpa());
        applicant.setPhone(input.getPhone());
        applicant.setAddress(input.getAddress());
        applicant.setExperience(input.getExperience());
        applicant.setMotivation(input.getMotivation());
        applicant.setSkills(ApplicantProfileValidator.parseSkills(input.getSkills()));
    }

    private void applyProvidedFields(Applicant applicant, ApplicantProfileInput input) {
        if (input.getFullNameRaw() != null) applicant.setFullName(input.getFullName());
        if (input.getStudentIdRaw() != null) applicant.setStudentId(input.getStudentId());
        if (input.getDepartmentRaw() != null) applicant.setDepartment(input.getDepartment());
        if (input.getProgramRaw() != null) applicant.setProgram(input.getProgram());
        if (input.getGpaRaw() != null) applicant.setGpa(input.getGpa());
        if (input.getPhoneRaw() != null) applicant.setPhone(input.getPhone());
        if (input.getAddressRaw() != null) applicant.setAddress(input.getAddress());
        if (input.getExperienceRaw() != null) applicant.setExperience(input.getExperience());
        if (input.getMotivationRaw() != null) applicant.setMotivation(input.getMotivation());
        if (input.getSkillsRaw() != null) applicant.setSkills(ApplicantProfileValidator.parseSkills(input.getSkills()));
    }

    private void syncAccountRealName(User currentUser, String fullName, HttpSession session) {
        if (currentUser == null || !ApplicantProfileValidator.isNotEmpty(fullName)) {
            return;
        }

        String normalizedFullName = fullName.trim();
        if (normalizedFullName.equals(currentUser.getRealName())) {
            return;
        }

        currentUser.setRealName(normalizedFullName);
        User savedUser = userDao.update(currentUser);
        updateSessionUser(session, savedUser);
    }

    private void updateSessionUser(HttpSession session, User user) {
        if (session == null || user == null) {
            return;
        }
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().name());
    }

    private void syncApplicationApplicantName(Applicant applicant) {
        if (applicant == null || !ApplicantProfileValidator.isNotEmpty(applicant.getApplicantId())) {
            return;
        }

        String fullName = applicant.getFullName() == null ? "" : applicant.getFullName().trim();
        if (!ApplicantProfileValidator.isNotEmpty(fullName)) {
            return;
        }

        for (Application application : applicationDao.findByApplicantId(applicant.getApplicantId())) {
            // Application CSV saves applicantName snapshot; used so MO/Admin list doesn't need to query profile.
            String currentName = application.getApplicantName() == null ? "" : application.getApplicantName();
            if (!fullName.equals(currentName)) {
                application.setApplicantName(fullName);
                applicationDao.update(application);
            }
        }
    }

    private void cleanupNewFiles(String newResumePath, String newPhotoPath) {
        if (ApplicantProfileValidator.isNotEmpty(newResumePath)) {
            assetService.deleteStoredFile(newResumePath);
        }
        if (ApplicantProfileValidator.isNotEmpty(newPhotoPath)) {
            assetService.deleteStoredFile(newPhotoPath);
        }
    }
}

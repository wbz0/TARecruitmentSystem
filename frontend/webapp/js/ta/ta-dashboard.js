/*
 * TA dashboard profile script, corresponds to /jsp/ta/dashboard.jsp.
 *
 * Handles TA profile form, resume draft upload, profile photo preview/remove, and sidebar avatar refresh after save.
 * Related APIs: /api/me/applicant-profile, /api/me/applicant-profile/resume-draft,
 * /api/me/applicant-profile/photo, /api/me/applicant-profile/resume.
 */
(function () {
    var form = document.getElementById("ta-profile-form");
    if (!form) {
        return;
    }

    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var messageBox = document.getElementById("form-message");
    var submitButton = document.getElementById("profile-submit");
    var editButton = document.getElementById("profile-edit-btn");
    var cancelEditButton = document.getElementById("profile-cancel-btn");
    var formFields = form.querySelectorAll("input, select, textarea");
    var resumeFileTrigger = document.getElementById("resume-file-trigger");
    var resumeFileInput = document.getElementById("resume-file-input");
    var resumeUploadShell = document.getElementById("resume-upload-shell");
    var resumeEmptyState = document.getElementById("resume-empty-state");
    var resumeFilledState = document.getElementById("resume-filled-state");
    var resumeFileDisplayName = document.getElementById("resume-file-display-name");
    var resumeFileDisplayDetail = document.getElementById("resume-file-display-detail");
    var resumeRemoveButton = document.getElementById("resume-remove-btn");
    var resumeUploadMessage = document.getElementById("resume-upload-message");
    var photoFileTrigger = document.getElementById("photo-file-trigger");
    var photoFileInput = document.getElementById("photo-file-input");
    var photoUploadShell = document.getElementById("photo-upload-shell");
    var photoEmptyState = document.getElementById("photo-empty-state");
    var photoFilledState = document.getElementById("photo-filled-state");
    var photoPreviewImage = document.getElementById("photo-preview-image");
    var photoRemoveButton = document.getElementById("photo-remove-btn");
    var photoUploadMessage = document.getElementById("photo-upload-message");

    var ALLOWED_RESUME_EXTENSIONS = [".pdf", ".doc", ".docx"];
    var MAX_RESUME_SIZE = 10 * 1024 * 1024;
    var ALLOWED_PHOTO_EXTENSIONS = [".jpg", ".jpeg", ".png", ".webp"];
    var MAX_PHOTO_SIZE = 5 * 1024 * 1024;
    var SKILL_SEPARATOR_PATTERN = /[,，]/;
    var UNSUPPORTED_SKILL_SEPARATOR_PATTERN = /[;；、|]/;

    function localizeText(key, fallback) {
        if (window.AppI18n && typeof window.AppI18n.t === "function") {
            return window.AppI18n.t(key, fallback || key);
        }
        return fallback || key;
    }

    function localizeServerMessage(message, fallbackKey, fallbackText) {
        if (window.AppI18n && typeof window.AppI18n.localizeServerMessage === "function") {
            return window.AppI18n.localizeServerMessage(message, fallbackKey, fallbackText);
        }
        if (typeof message === "string" && message.trim()) {
            return message.trim();
        }
        return fallbackKey ? localizeText(fallbackKey, fallbackText) : (fallbackText || "");
    }

    // Form field mapping: key matches the field name accepted by backend ApplicantProfileRequestMapper.
    var inputs = {
        fullName: document.getElementById("full-name"),
        studentId: document.getElementById("student-id"),
        department: document.getElementById("department"),
        program: document.getElementById("program"),
        gpa: document.getElementById("gpa"),
        skills: document.getElementById("skills"),
        phone: document.getElementById("phone"),
        experience: document.getElementById("experience"),
        motivation: document.getElementById("motivation")
    };

    var state = {
        // saved* are backend-saved profile resources; pending* are resume drafts in current session; selected* are browser-local newly selected files.
        hasExistingProfile: false,
        isEditing: false,
        isSubmitting: false,
        isLoading: false,
        isUploadingResume: false,
        selectedResumeFile: null,
        resumePath: "",
        resumeName: "",
        resumeSize: 0,
        removedSavedResume: false,
        pendingResumePath: "",
        pendingResumeName: "",
        pendingResumeSize: 0,
        selectedPhotoFile: null,
        photoPath: "",
        photoName: "",
        photoSize: 0,
        removedSavedPhoto: false,
        photoObjectUrl: "",
        photoPreviewVersion: Date.now()
    };

    // touched distinguishes “user has edited” from “initialized with old values”, preventing immediate validation errors when loading existing profile.
    var fieldValidationState = {
        feedbackByKey: {},
        touchedByKey: {}
    };

    // Validation summary locates the first error field by page visual order.
    var orderedInputKeys = [
        "fullName",
        "studentId",
        "department",
        "program",
        "gpa",
        "phone",
        "skills",
        "experience",
        "motivation"
    ];

    initializeRealtimeValidation();
    initializeEnterKeyBehavior();

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        if (state.isSubmitting || state.isLoading) {
            return;
        }

        if (state.hasExistingProfile) {
            if (!state.isEditing) {
                return;
            }
            handleUpdate();
            return;
        }

        handleCreate();
    });

    if (editButton) {
        editButton.addEventListener("click", function () {
            if (!state.hasExistingProfile || state.isSubmitting || state.isLoading) {
                return;
            }
            enterEditMode();
        });
    }

    if (cancelEditButton) {
        cancelEditButton.addEventListener("click", function () {
            if (!state.hasExistingProfile || state.isSubmitting || state.isLoading || state.isUploadingResume) {
                return;
            }
            handleCancelEdit();
        });
    }

    if (resumeFileInput) {
        resumeFileInput.addEventListener("change", handleResumeFileChange);
    }

    if (resumeFileTrigger && resumeFileInput) {
        resumeFileTrigger.addEventListener("click", function () {
            if (canPreviewResume()) {
                openResumePreview();
                return;
            }
            if (resumeFileTrigger.disabled || !canEditResumeSection()) {
                return;
            }
            resumeFileInput.click();
        });
    }

    if (resumeRemoveButton) {
        resumeRemoveButton.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            handleResumeRemove();
        });
    }

    if (photoFileInput) {
        photoFileInput.addEventListener("change", handlePhotoFileChange);
    }

    if (photoFileTrigger && photoFileInput) {
        photoFileTrigger.addEventListener("click", function () {
            if (photoFileTrigger.disabled) {
                return;
            }
            if (photoUploadShell && photoUploadShell.classList.contains("is-filled")) {
                openPhotoLightbox();
            } else {
                photoFileInput.click();
            }
        });
    }

    if (photoRemoveButton) {
        photoRemoveButton.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            handlePhotoRemove();
        });
    }

    setupSharedRealNameSync();
    refreshResumeArea();
    refreshPhotoArea();
    loadExistingProfile({ silentWhenMissing: true });

    document.addEventListener("app:locale-changed", function () {
        refreshSubmitButton();
        refreshResumeArea();
        refreshPhotoArea();
    });

    /*
     * Create TA profile: validates required fields and files on frontend first, then submits to /api/me/applicant-profile.
     * On success, does not trust the local form directly; instead fetches the backend-saved result again.
     */
    function handleCreate() {
        hideMessage();

        var validationError = validateForm();
        if (validationError) {
            showValidationSummaryMessage();
            if (validationError.field && typeof validationError.field.focus === "function") {
                validationError.field.focus();
            }
            return;
        }

        validationError = validateResumeRequirement();
        if (validationError) {
            showMessage(validationError.message, "error", true);
            return;
        }

        validationError = validatePhotoSelection();
        if (validationError) {
            showMessage(validationError.message, "error", true);
            return;
        }

        setSubmitting(true);

        submitProfile(false)
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (response.status === 409) {
                    showMessage(localizeText("portal.dynamic.profileAlreadyExists", "A profile already exists for this account. Loading your saved profile..."), "error");
                    return loadExistingProfile({ afterCreate: false, silentWhenMissing: false });
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = localizeText("portal.dynamic.unableCreateProfile", "Unable to create your profile. Please review the form and try again.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.unableCreateProfile", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                return loadExistingProfile({ afterCreate: true, silentWhenMissing: false });
            })
            .catch(function () {
                showMessage(localizeText("portal.dynamic.networkErrorMoment", "Network error. Please try again in a moment."), "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    /*
     * Called both on page init and after save: reads current account's TA profile, saved resume/photo, and session draft.
     */
    function loadExistingProfile(options) {
        var settings = options || {};

        state.isLoading = true;
        refreshResumeArea();
        refreshPhotoArea();
        if (!state.isSubmitting) {
            submitButton.disabled = true;
            submitButton.textContent = localizeText("portal.dynamic.checkingProfile", "Checking profile...");
        }

        return request(window.TARecruitment.routes.me.applicantProfile(), {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;
                var payloadData = extractData(payload);

                if (response.status === 404) {
                    enableCreateMode(payloadData);
                    if (!settings.silentWhenMissing) {
                        showMessage(localizeText("portal.dynamic.noProfileFound", "No profile found yet. Please complete the form below."), "success");
                    }
                    return;
                }

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    enableCreateMode();
                    var errorMessage = localizeText("portal.dynamic.unableCheckProfile", "Unable to load your current profile. You can still create one below.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.unableCheckProfile", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                applyExistingProfile(payloadData, settings.afterCreate === true);
            })
            .catch(function () {
                enableCreateMode();
                showMessage(localizeText("portal.dynamic.unableCheckProfile", "Unable to check your existing profile right now. You can still try creating one."), "error");
            })
            .finally(function () {
                state.isLoading = false;
                refreshSubmitButton();
                refreshResumeArea();
                refreshPhotoArea();
            });
    }

    /*
     * Update existing profile: triggered only in edit mode, same flow as create but keeps deletion markers for old resources.
     */
    function handleUpdate() {
        hideMessage();

        var validationError = validateForm();
        if (validationError) {
            showValidationSummaryMessage();
            if (validationError.field && typeof validationError.field.focus === "function") {
                validationError.field.focus();
            }
            return;
        }

        validationError = validateResumeRequirement();
        if (validationError) {
            showMessage(validationError.message, "error", true);
            return;
        }

        validationError = validatePhotoSelection();
        if (validationError) {
            showMessage(validationError.message, "error", true);
            return;
        }

        setSubmitting(true);

        submitProfile(true)
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (response.status === 404) {
                    enableCreateMode();
                    showMessage(localizeText("portal.dynamic.noProfileFound", "No profile found yet. Please complete the form below."), "error");
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = localizeText("portal.dynamic.unableUpdateProfile", "Unable to update your profile. Please review the form and try again.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.unableUpdateProfile", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                showMessage(localizeText("portal.dynamic.profileUpdatedSuccess", "Profile updated successfully."), "success");
                return loadExistingProfile({ afterCreate: false, silentWhenMissing: false });
            })
            .catch(function () {
                showMessage(localizeText("portal.dynamic.networkErrorMoment", "Network error. Please try again in a moment."), "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    /*
     * Assemble profile submission payload.
     * Uses form encoding when no photo; uses multipart when photo is present or photo status is being updated, avoiding separate upload API call on frontend.
     */
    function submitProfile(isUpdate) {
        if (isUpdate) {
            // Legacy/pending removal: profile update currently still uses multipart POST branch.
            // Reason: same path can submit both text fields and photo; if unified to PUT multipart later, this detour explanation can be cleaned up.
            var updateData = new FormData();
            updateData.append("fullName", inputs.fullName.value.trim());
            updateData.append("studentId", inputs.studentId.value.trim());
            updateData.append("department", inputs.department.value.trim());
            updateData.append("program", inputs.program.value.trim());
            updateData.append("gpa", inputs.gpa.value.trim());
            updateData.append("skills", normalizeSkillsForSubmit(inputs.skills.value));
            updateData.append("phone", inputs.phone.value.trim());
            updateData.append("address", "");
            updateData.append("experience", inputs.experience.value.trim());
            updateData.append("motivation", inputs.motivation.value.trim());
            updateData.append("removePhoto", state.removedSavedPhoto ? "true" : "false");
            if (state.selectedPhotoFile) {
                updateData.append("photo", state.selectedPhotoFile, state.selectedPhotoFile.name);
            }

            return request(window.TARecruitment.routes.me.applicantProfile(), {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: updateData
            });
        }

        var createData = new URLSearchParams();
        createData.set("fullName", inputs.fullName.value.trim());
        createData.set("studentId", inputs.studentId.value.trim());
        createData.set("department", inputs.department.value.trim());
        createData.set("program", inputs.program.value.trim());
        createData.set("gpa", inputs.gpa.value.trim());
        createData.set("skills", normalizeSkillsForSubmit(inputs.skills.value));
        createData.set("phone", inputs.phone.value.trim());
        createData.set("address", "");
        createData.set("experience", inputs.experience.value.trim());
        createData.set("motivation", inputs.motivation.value.trim());
        createData.set("removePhoto", "false");

        if (state.selectedPhotoFile) {
            var createMultipartData = new FormData();
            createMultipartData.append("fullName", inputs.fullName.value.trim());
            createMultipartData.append("studentId", inputs.studentId.value.trim());
            createMultipartData.append("department", inputs.department.value.trim());
            createMultipartData.append("program", inputs.program.value.trim());
            createMultipartData.append("gpa", inputs.gpa.value.trim());
            createMultipartData.append("skills", normalizeSkillsForSubmit(inputs.skills.value));
            createMultipartData.append("phone", inputs.phone.value.trim());
            createMultipartData.append("address", "");
            createMultipartData.append("experience", inputs.experience.value.trim());
            createMultipartData.append("motivation", inputs.motivation.value.trim());
            createMultipartData.append("removePhoto", "false");
            createMultipartData.append("photo", state.selectedPhotoFile, state.selectedPhotoFile.name);

            return request(window.TARecruitment.routes.me.applicantProfile(), {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: createMultipartData
            });
        }

        return request(window.TARecruitment.routes.me.applicantProfile(), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: createData.toString()
        });
    }

    /*
     * Fill backend profile result into read-only form, sync sidebar display name and resource state.
     */
    function applyExistingProfile(payload, createdNow) {
        state.hasExistingProfile = true;
        state.isEditing = false;
        state.removedSavedResume = false;
        state.removedSavedPhoto = false;
        setSelectedPhotoFile(null);
        syncSavedResumeState(payload);
        syncSavedPhotoState(payload);
        syncResumeDraftState(payload);

        setFieldValue(inputs.fullName, payload.fullName);
        announceTaProfileRealName(payload.fullName || "");
        setFieldValue(inputs.studentId, payload.studentId);
        setFieldValue(inputs.department, payload.department);
        setSelectValue(inputs.program, payload.program);
        setFieldValue(inputs.gpa, payload.gpa);
        setFieldValue(inputs.skills, formatSkillsForDisplay(payload.skills));
        setFieldValue(inputs.phone, payload.phone);
        setFieldValue(inputs.experience, payload.experience);
        setFieldValue(inputs.motivation, payload.motivation);

        clearAllFieldValidation();
        resetFieldTouchedState();
        setFormDisabled(true);
        form.classList.add("is-readonly");
        hidePhotoMessage();

        updateProfileActionState();
        refreshResumeArea();
        refreshPhotoArea();

        if (createdNow) {
            showMessage(localizeText("portal.dynamic.profileCreatedSuccess", "Profile created successfully. Your saved information is now displayed below."), "success");
        } else {
            hideMessage();
        }
    }

    /*
     * Enter create mode when no profile or read failed; payload may only contain resume draft in session.
     */
    function enableCreateMode(payload) {
        state.hasExistingProfile = false;
        state.isEditing = false;
        state.resumePath = "";
        state.resumeName = "";
        state.resumeSize = 0;
        state.removedSavedResume = false;
        state.photoPath = "";
        state.photoName = "";
        state.photoSize = 0;
        state.removedSavedPhoto = false;
        setSelectedPhotoFile(null);
        syncResumeDraftState(payload);
        setFormDisabled(false);
        form.classList.remove("is-readonly");
        clearAllFieldValidation();
        resetFieldTouchedState();
        hidePhotoMessage();
        refreshSubmitButton();
        refreshResumeArea();
        refreshPhotoArea();
    }

    /*
     * Save/Edit/Cancel button visibility determined by “has existing profile” and “is editing”.
     */
    function updateProfileActionState() {
        if (!submitButton) {
            return;
        }

        if (!state.hasExistingProfile || state.isEditing) {
            submitButton.disabled = false;
            submitButton.textContent = localizeText("portal.taDashboard.saveChangesButton", "Save changes");
            if (editButton) {
                editButton.hidden = true;
            }
            if (cancelEditButton) {
                cancelEditButton.hidden = true;
            }
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = localizeText("portal.taDashboard.saveChangesButton", "Save changes");

        if (editButton) {
            editButton.hidden = false;
            editButton.disabled = false;
        }
        if (cancelEditButton) {
            cancelEditButton.hidden = true;
            cancelEditButton.disabled = false;
        }
    }

    /*
     * Enter edit mode: form becomes editable but saved resources remain displayed.
     */
    function enterEditMode() {
        state.isEditing = true;
        setFormDisabled(false);
        form.classList.remove("is-readonly");
        hideMessage();
        clearAllFieldValidation();

        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = localizeText("portal.taDashboard.saveChangesButton", "Save changes");
        }
        if (editButton) {
            editButton.hidden = true;
        }
        if (cancelEditButton) {
            cancelEditButton.hidden = false;
            cancelEditButton.disabled = false;
        }
        refreshResumeArea();
        refreshPhotoArea();
    }

    /*
     * Cancel edit discards the resume draft uploaded in this session, then reloads saved profile from backend.
     */
    function handleCancelEdit() {
        var reloadProfile = function () {
            state.isEditing = false;
            setSelectedResumeFile(null);
            setSelectedPhotoFile(null);
            state.removedSavedPhoto = false;
            hideResumeMessage();
            hidePhotoMessage();
            return loadExistingProfile({ afterCreate: false, silentWhenMissing: false });
        };

        if (!state.pendingResumePath) {
            reloadProfile();
            return;
        }

        discardPendingResume()
            .then(function () {
                return reloadProfile();
            })
            .catch(function (error) {
                var errorMessage = localizeText("portal.dynamic.resumeDiscardFailed", "Unable to discard the pending resume. Please try again.");
                if (error && typeof error.userMessage === "string" && error.userMessage.trim()) {
                    errorMessage = error.userMessage.trim();
                }
                showResumeMessage(errorMessage, "error");
            });
    }

    /*
     * Refresh main button based on loading/submitting/upload draft state, preventing duplicate saves during resource processing.
     */
    function refreshSubmitButton() {
        if (state.hasExistingProfile && !state.isEditing) {
            updateProfileActionState();
            return;
        }

        if (state.isSubmitting) {
            submitButton.textContent = localizeText("portal.dynamic.savingChanges", "Saving changes...");
            submitButton.disabled = true;
            return;
        }

        if (state.isLoading) {
            submitButton.textContent = localizeText("portal.dynamic.checkingProfile", "Checking profile...");
            submitButton.disabled = true;
            return;
        }

        if (state.isUploadingResume) {
            submitButton.textContent = localizeText("portal.dynamic.uploading", "Uploading") + "...";
            submitButton.disabled = true;
            return;
        }

        submitButton.textContent = localizeText("portal.taDashboard.saveChangesButton", "Save changes");
        submitButton.disabled = false;
    }

    /*
     * Toggle submitting state, sync disabled form and file upload area.
     */
    function setSubmitting(submitting) {
        state.isSubmitting = submitting;
        if (!state.hasExistingProfile || state.isEditing) {
            setFormDisabled(submitting);
        }
        refreshSubmitButton();
        refreshResumeArea();
        refreshPhotoArea();
    }

    /*
     * Only controls current form fields; upload area buttons handled separately by refreshResumeArea/refreshPhotoArea.
     */
    function setFormDisabled(disabled) {
        Array.prototype.forEach.call(formFields, function (field) {
            field.disabled = disabled;
        });
    }

    /*
     * Resume draft is stored in session, not yet converted to formal profile resume.
     */
    function syncResumeDraftState(payload) {
        state.pendingResumePath = payload && typeof payload.pendingResumePath === "string" ? payload.pendingResumePath : "";
        state.pendingResumeName = payload && typeof payload.pendingResumeName === "string" ? payload.pendingResumeName : "";
        state.pendingResumeSize = getPositiveNumber(payload && payload.pendingResumeSize);
    }

    /*
     * Resume is a prerequisite for saving profile: can be saved resume or this session's draft.
     */
    function validateResumeRequirement() {
        if (state.pendingResumePath || hasSavedResume()) {
            return null;
        }

        var errorMessage = localizeText("portal.dynamic.resumeRequiredToSave", "Please upload your resume before saving your profile.");
        showResumeMessage(errorMessage, "error");
        if (resumeFileTrigger && typeof resumeFileTrigger.focus === "function") {
            resumeFileTrigger.focus();
        }
        return buildValidationError(errorMessage, resumeFileTrigger || submitButton);
    }

    /*
     * Photo is not required; only validates type and size when user selects a new photo.
     */
    function validatePhotoSelection() {
        if (!state.selectedPhotoFile) {
            return null;
        }

        var photoError = validatePhotoFile(state.selectedPhotoFile);
        if (!photoError) {
            return null;
        }

        showPhotoMessage(photoError, "error");
        if (photoFileTrigger && typeof photoFileTrigger.focus === "function") {
            photoFileTrigger.focus();
        }
        return buildValidationError(photoError, photoFileTrigger || submitButton);
    }

    /*
     * Resume upload area must follow form edit state to prevent partial modification of read-only profile.
     */
    function canEditResumeSection() {
        if (state.isLoading || state.isSubmitting || state.isUploadingResume) {
            return false;
        }
        return !state.hasExistingProfile || state.isEditing;
    }

    /*
     * Photo area uses the same edit permission as resume area.
     */
    function canEditPhotoSection() {
        if (state.isLoading || state.isSubmitting || state.isUploadingResume) {
            return false;
        }
        return !state.hasExistingProfile || state.isEditing;
    }

    /*
     * After selecting resume, immediately upload as draft; backend converts draft to formal resume when profile is saved.
     */
    function handleResumeFileChange(event) {
        hideResumeMessage();

        var file = event && event.target && event.target.files ? event.target.files[0] : null;
        if (!file) {
            setSelectedResumeFile(null);
            return;
        }

        if (!canEditResumeSection()) {
            setSelectedResumeFile(null);
            return;
        }

        var fileError = validateResumeFile(file);
        if (fileError) {
            setSelectedResumeFile(null);
            showResumeMessage(fileError, "error");
            return;
        }

        setSelectedResumeFile(file);
        uploadDraftResume(file)
            .catch(function (error) {
                var uploadErrorMessage = localizeText("portal.dynamic.resumeUploadFailed", "Resume upload failed. Please try again.");
                if (error && typeof error.userMessage === "string" && error.userMessage.trim()) {
                    uploadErrorMessage = error.userMessage.trim();
                }
                setSelectedResumeFile(null);
                showResumeMessage(uploadErrorMessage, "error");
            });
    }

    /*
     * Upload session resume draft; on failure clears local selection to avoid page showing a file that does not exist on backend.
     */
    function uploadDraftResume(file) {
        if (!file) {
            var noFileError = new Error("No resume file selected.");
            noFileError.userMessage = localizeText("portal.dynamic.chooseResumeFirst", "Please choose a resume file first.");
            return Promise.reject(noFileError);
        }

        setResumeUploading(true);
        showResumeMessage(
            localizeText("portal.dynamic.resumeDraftUploading", "Uploading resume draft:") + " " + file.name + "...",
            "success"
        );

        return uploadDraftResumeWithProgress(file)
            .then(function (result) {
                var status = result.status;
                var payload = result.payload;

                if (status === 401) {
                    handleUnauthorized();
                    var unauthorizedError = new Error("Unauthorized.");
                    unauthorizedError.userMessage = localizeText("portal.dynamic.sessionExpiredRedirect", "Your session has expired. Redirecting to login...");
                    throw unauthorizedError;
                }

                if (status < 200 || status >= 300 || !payload || payload.success !== true) {
                    var serverMessage = payload && typeof payload.message === "string" && payload.message.trim()
                        ? localizeServerMessage(payload.message, "portal.dynamic.resumeUploadFailed", localizeText("portal.dynamic.resumeUploadFailed", "Resume upload failed. Please try again."))
                        : localizeText("portal.dynamic.resumeUploadFailed", "Resume upload failed. Please try again.");
                    var uploadError = new Error(serverMessage);
                    uploadError.userMessage = serverMessage;
                    throw uploadError;
                }

                syncResumeDraftState(extractData(payload));
                setSelectedResumeFile(null);
                showResumeMessage(
                    localizeText(
                        state.resumePath ? "portal.dynamic.resumeDraftReplaceSaved" : "portal.dynamic.resumeDraftSaved",
                        state.resumePath
                            ? "New resume uploaded. Save changes to replace the current resume."
                            : "Resume draft uploaded. Save changes to apply it."
                    ),
                    "success"
                );
            })
            .finally(function () {
                setResumeUploading(false);
                refreshResumeArea();
            });
    }

    /*
     * Low-level draft upload implementation: XMLHttpRequest is used to keep progress extension point for resume draft upload; current UI only shows uploading/success/failure.
     */
    function uploadDraftResumeWithProgress(file) {
        // XMLHttpRequest is used to keep progress extension point; current UI only shows uploading/success/failure.
        return new Promise(function (resolve, reject) {
            var xhr = new XMLHttpRequest();
            xhr.open("POST", window.TARecruitment.routes.me.resumeDraft(), true);
            xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");

            xhr.onerror = function () {
                var networkError = new Error("Network error.");
                networkError.userMessage = localizeText("portal.dynamic.uploadNetworkError", "Network error during file upload. Please try again.");
                reject(networkError);
            };

            xhr.onabort = function () {
                var abortError = new Error("Upload aborted.");
                abortError.userMessage = localizeText("portal.dynamic.uploadInterrupted", "Upload was interrupted. Please try again.");
                reject(abortError);
            };

            xhr.onload = function () {
                resolve({
                    status: xhr.status,
                    payload: parseResponse(xhr.responseText || "")
                });
            };

            var data = new FormData();
            data.append("resume", file, file.name);
            xhr.send(data);
        });
    }

    /*
     * Resume files only allow office documents and PDF; size limit consistent with ProfileAssetValidator.
     */
    function validateResumeFile(file) {
        if (!file) {
            return localizeText("portal.dynamic.chooseResumeFirst", "Please choose a resume file first.");
        }

        var lowerName = typeof file.name === "string" ? file.name.toLowerCase() : "";
        var extensionAllowed = ALLOWED_RESUME_EXTENSIONS.some(function (extension) {
            return lowerName.endsWith(extension);
        });
        if (!extensionAllowed) {
            return localizeText("portal.dynamic.invalidResumeFormat", "Invalid file format. Please upload a PDF, DOC, or DOCX file.");
        }

        if (typeof file.size === "number" && file.size > MAX_RESUME_SIZE) {
            return localizeText("portal.dynamic.resumeTooLarge", "File size exceeds 10MB. Please choose a smaller file.");
        }

        return null;
    }

    /*
     * Maintain browser-local selected resume file; clearing also resets input to allow selecting the same file again.
     */
    function setSelectedResumeFile(file) {
        state.selectedResumeFile = file || null;
        if (resumeFileInput && !file) {
            resumeFileInput.value = "";
        }
        refreshResumeArea();
    }

    /*
     * Draft uploading state affects save button, resume area, and photo area simultaneously.
     */
    function setResumeUploading(uploading) {
        state.isUploadingResume = uploading;
        refreshSubmitButton();
        refreshResumeArea();
        refreshPhotoArea();
    }

    /*
     * Remove resume by priority: discard session draft first, clear local selection second, mark saved resume for deletion last.
     */
    function handleResumeRemove() {
        if (!canEditResumeSection()) {
            return;
        }

        hideResumeMessage();

        if (state.pendingResumePath) {
            discardPendingResume()
                .then(function () {
                    setSelectedResumeFile(null);
                    showResumeMessage(localizeText("portal.dynamic.pendingResumeRemoved", "Pending resume removed."), "success");
                    refreshResumeArea();
                })
                .catch(function (error) {
                    var pendingRemoveError = localizeText("portal.dynamic.resumeDiscardFailed", "Unable to discard the pending resume. Please try again.");
                    if (error && typeof error.userMessage === "string" && error.userMessage.trim()) {
                        pendingRemoveError = error.userMessage.trim();
                    }
                    showResumeMessage(pendingRemoveError, "error");
                });
            return;
        }

        if (state.selectedResumeFile) {
            setSelectedResumeFile(null);
            return;
        }

        if (hasSavedResume()) {
            state.removedSavedResume = true;
            refreshResumeArea();
            showResumeMessage(
                localizeText("portal.dynamic.savedResumeRemoved", "Current resume removed. Upload a new one before saving changes."),
                "success"
            );
        }
    }

    /*
     * Refresh resume upload area based on “local selection / session draft / saved resume” three-layer state.
     */
    function refreshResumeArea() {
        var resumeSectionEditable = canEditResumeSection();
        var activeResumeCard = buildActiveResumeCard();
        var resumePreviewAvailable = canPreviewResume();

        if (resumeFileTrigger) {
            resumeFileTrigger.disabled = state.isUploadingResume || (!resumeSectionEditable && !resumePreviewAvailable);
            var savedResumePreviewUrl = activeResumeCard && hasSavedResume()
                && !state.selectedResumeFile
                && !state.pendingResumePath
                ? window.TARecruitment.routes.me.applicantResume()
                : "";
            if (savedResumePreviewUrl) {
                resumeFileTrigger.setAttribute("data-preview-url", savedResumePreviewUrl);
            } else {
                resumeFileTrigger.removeAttribute("data-preview-url");
            }
        }
        if (resumeFileInput) {
            resumeFileInput.disabled = !resumeSectionEditable;
        }

        if (resumeUploadShell) {
            resumeUploadShell.classList.toggle("is-empty", !activeResumeCard);
            resumeUploadShell.classList.toggle("is-filled", !!activeResumeCard);
            resumeUploadShell.classList.toggle("is-disabled", !resumeSectionEditable);
            resumeUploadShell.classList.toggle("is-uploading", state.isUploadingResume);
        }

        if (resumeEmptyState) {
            resumeEmptyState.hidden = !!activeResumeCard;
            resumeEmptyState.classList.toggle("hidden", !!activeResumeCard);
        }
        if (resumeFilledState) {
            resumeFilledState.hidden = !activeResumeCard;
            resumeFilledState.classList.toggle("hidden", !activeResumeCard);
        }

        if (resumeFileDisplayName && activeResumeCard) {
            resumeFileDisplayName.textContent = activeResumeCard.name;
        }
        if (resumeFileDisplayDetail && activeResumeCard) {
            resumeFileDisplayDetail.textContent = activeResumeCard.detail;
        }

        if (resumeRemoveButton) {
            var canRemoveCurrentResume = !!activeResumeCard && resumeSectionEditable && !state.isUploadingResume;
            resumeRemoveButton.hidden = !canRemoveCurrentResume;
            resumeRemoveButton.classList.toggle("hidden", !canRemoveCurrentResume);
            resumeRemoveButton.disabled = !canRemoveCurrentResume;
        }
    }

    /*
     * If any layer of resume resource exists, allow user to open preview.
     */
    function canPreviewResume() {
        return !state.isUploadingResume
            && (state.selectedResumeFile || state.pendingResumePath || hasSavedResume());
    }

    /*
     * Choose which resume card to display: local selection first, then draft, then saved file.
     */
    function buildActiveResumeCard() {
        if (state.selectedResumeFile) {
            return {
                name: state.selectedResumeFile.name,
                detail: buildResumeCardDetail(state.selectedResumeFile.size)
            };
        }

        if (state.pendingResumePath) {
            return {
                name: state.pendingResumeName || extractFileNameFromPath(state.pendingResumePath),
                detail: buildResumeCardDetail(state.pendingResumeSize)
            };
        }

        if (hasSavedResume()) {
            return {
                name: state.resumeName || extractFileNameFromPath(state.resumePath),
                detail: buildResumeCardDetail(state.resumeSize)
            };
        }

        return null;
    }

    /*
     * Resume card subtitle prefers showing file size; shows ready state when size is unknown.
     */
    function buildResumeCardDetail(fileSize) {
        if (typeof fileSize === "number" && fileSize > 0) {
            return formatFileSize(fileSize);
        }
        return localizeText("portal.dynamic.resumeReady", "Resume ready");
    }

    /*
     * Saved resume marked for removal by user is not treated as available resource in this edit session.
     */
    function hasSavedResume() {
        return hasText(state.resumePath) && !state.removedSavedResume;
    }

    /*
     * Sync formally saved resume resource info from backend profile payload.
     */
    function syncSavedResumeState(payload) {
        state.resumePath = payload && typeof payload.resumePath === "string" ? payload.resumePath : "";
        state.resumeName = payload && typeof payload.resumeName === "string" ? payload.resumeName : "";
        state.resumeSize = getPositiveNumber(payload && payload.resumeSize);
    }

    /*
     * Sync saved photo; refresh version to avoid browser cache when path changes.
     */
    function syncSavedPhotoState(payload) {
        var nextPhotoPath = payload && typeof payload.photoPath === "string" ? payload.photoPath : "";
        if (state.photoPath !== nextPhotoPath) {
            state.photoPreviewVersion = Date.now();
        }
        state.photoPath = nextPhotoPath;
        state.photoName = payload && typeof payload.photoName === "string" ? payload.photoName : "";
        state.photoSize = getPositiveNumber(payload && payload.photoSize);
    }

    /*
     * Photo is only submitted when saving profile; local preview and frontend format validation done here first.
     */
    function handlePhotoFileChange(event) {
        hidePhotoMessage();

        var file = event && event.target && event.target.files ? event.target.files[0] : null;
        if (!file) {
            setSelectedPhotoFile(null);
            return;
        }

        if (!canEditPhotoSection()) {
            setSelectedPhotoFile(null);
            return;
        }

        var fileError = validatePhotoFile(file);
        if (fileError) {
            setSelectedPhotoFile(null);
            showPhotoMessage(fileError, "error");
            return;
        }

        setSelectedPhotoFile(file);
        showPhotoMessage(
            localizeText("portal.dynamic.photoReadyToSave", "Photo selected. Save changes to apply it."),
            "success"
        );
    }

    /*
     * Photo format and size limits aligned with backend ProfileAssetValidator.
     */
    function validatePhotoFile(file) {
        if (!file) {
            return localizeText("portal.dynamic.choosePhotoFirst", "Please choose a photo file first.");
        }

        var lowerName = typeof file.name === "string" ? file.name.toLowerCase() : "";
        var extensionAllowed = ALLOWED_PHOTO_EXTENSIONS.some(function (extension) {
            return lowerName.endsWith(extension);
        });
        if (!extensionAllowed) {
            return localizeText("portal.dynamic.invalidPhotoFormat", "Invalid photo format. Please upload JPG, PNG, or WEBP.");
        }

        if (typeof file.size === "number" && file.size > MAX_PHOTO_SIZE) {
            return localizeText("portal.dynamic.photoTooLarge", "Photo size exceeds 5MB. Please choose a smaller file.");
        }

        return null;
    }

    /*
     * Maintain photo local preview URL; release old object URL when replacing file to avoid browser memory leak.
     */
    function setSelectedPhotoFile(file) {
        if (state.photoObjectUrl && typeof URL !== "undefined" && typeof URL.revokeObjectURL === "function") {
            URL.revokeObjectURL(state.photoObjectUrl);
        }
        state.photoObjectUrl = "";
        state.selectedPhotoFile = file || null;

        if (state.selectedPhotoFile && typeof URL !== "undefined" && typeof URL.createObjectURL === "function") {
            state.photoObjectUrl = URL.createObjectURL(state.selectedPhotoFile);
            state.removedSavedPhoto = false;
        }

        if (photoFileInput && !file) {
            photoFileInput.value = "";
        }

        refreshPhotoArea();
    }

    /*
     * When removing photo: locally selected photo is cleared directly; saved photo only records deletion marker, effective on save.
     */
    function handlePhotoRemove() {
        if (!canEditPhotoSection()) {
            return;
        }

        hidePhotoMessage();

        if (state.selectedPhotoFile) {
            setSelectedPhotoFile(null);
            return;
        }

        if (hasSavedPhoto()) {
            state.removedSavedPhoto = true;
            refreshPhotoArea();
            showPhotoMessage(
                localizeText("portal.dynamic.savedPhotoRemoved", "Current photo removed. Save changes to apply it."),
                "success"
            );
        }
    }

    /*
     * Refresh upload shell, thumbnail, remove button, and read-only state based on local or saved photo.
     */
    function refreshPhotoArea() {
        var photoSectionEditable = canEditPhotoSection();
        var activePhotoCard = buildActivePhotoCard();

        if (photoFileTrigger) {
            photoFileTrigger.disabled = !photoSectionEditable && !activePhotoCard;
        }
        if (photoFileInput) {
            photoFileInput.disabled = !photoSectionEditable;
        }

        if (photoUploadShell) {
            photoUploadShell.classList.toggle("is-empty", !activePhotoCard);
            photoUploadShell.classList.toggle("is-filled", !!activePhotoCard);
            photoUploadShell.classList.toggle("is-disabled", !photoSectionEditable);
        }

        if (photoEmptyState) {
            photoEmptyState.hidden = !!activePhotoCard;
            photoEmptyState.classList.toggle("hidden", !!activePhotoCard);
        }
        if (photoFilledState) {
            photoFilledState.hidden = !activePhotoCard;
            photoFilledState.classList.toggle("hidden", !activePhotoCard);
        }

        if (photoPreviewImage) {
            if (activePhotoCard && activePhotoCard.previewUrl) {
                if (photoPreviewImage.getAttribute("src") !== activePhotoCard.previewUrl) {
                    photoPreviewImage.src = activePhotoCard.previewUrl;
                }
                photoPreviewImage.alt = activePhotoCard.name || localizeText("portal.taDashboard.profilePhotoAlt", "Profile photo");
            } else {
                photoPreviewImage.removeAttribute("src");
                photoPreviewImage.alt = "";
            }
        }

        if (photoRemoveButton) {
            var canRemoveCurrentPhoto = !!activePhotoCard && photoSectionEditable;
            photoRemoveButton.hidden = !canRemoveCurrentPhoto;
            photoRemoveButton.classList.toggle("hidden", !canRemoveCurrentPhoto);
            photoRemoveButton.disabled = !canRemoveCurrentPhoto;
        }
    }

    /*
     * Current photo display source: local selection first, saved photo second.
     */
    function buildActivePhotoCard() {
        if (state.selectedPhotoFile && state.photoObjectUrl) {
            return {
                name: state.selectedPhotoFile.name,
                detail: buildPhotoCardDetail(state.selectedPhotoFile.size),
                previewUrl: state.photoObjectUrl
            };
        }

        if (hasSavedPhoto()) {
            return {
                name: state.photoName || extractFileNameFromPath(state.photoPath),
                detail: buildPhotoCardDetail(state.photoSize),
                previewUrl: buildSavedPhotoPreviewUrl()
            };
        }

        return null;
    }

    /*
     * Photo card subtitle consistent with resume: size first, show ready if unknown.
     */
    function buildPhotoCardDetail(fileSize) {
        if (typeof fileSize === "number" && fileSize > 0) {
            return formatFileSize(fileSize);
        }
        return localizeText("portal.dynamic.photoReady", "Photo ready");
    }

    /*
     * Saved photo marked for deletion is treated as not existing in this edit view.
     */
    function hasSavedPhoto() {
        return hasText(state.photoPath) && !state.removedSavedPhoto;
    }

    function buildSavedPhotoPreviewUrl() {
        // v parameter only used to break browser image cache so newly uploaded photo displays immediately.
        return window.TARecruitment.routes.me.applicantPhoto() + "?v=" + encodeURIComponent(String(state.photoPreviewVersion));
    }

    /*
     * Local message for photo upload area, does not affect main profile form save prompts.
     */
    function showPhotoMessage(message, type) {
        if (!photoUploadMessage) {
            return;
        }
        photoUploadMessage.textContent = message;
        photoUploadMessage.classList.remove("hidden", "error", "success");
        photoUploadMessage.classList.add(type === "success" ? "success" : "error");
    }

    /*
     * Clear photo area local message.
     */
    function hidePhotoMessage() {
        if (!photoUploadMessage) {
            return;
        }
        photoUploadMessage.textContent = "";
        photoUploadMessage.classList.remove("error", "success");
        photoUploadMessage.classList.add("hidden");
    }

    /*
     * Local message for resume upload area, specifically for draft upload, remove, and preview status.
     */
    function showResumeMessage(message, type) {
        if (!resumeUploadMessage) {
            return;
        }
        resumeUploadMessage.textContent = message;
        resumeUploadMessage.classList.remove("hidden", "error", "success");
        resumeUploadMessage.classList.add(type === "success" ? "success" : "error");
    }

    /*
     * Clear resume area local message.
     */
    function hideResumeMessage() {
        if (!resumeUploadMessage) {
            return;
        }
        resumeUploadMessage.textContent = "";
        resumeUploadMessage.classList.remove("error", "success");
        resumeUploadMessage.classList.add("hidden");
    }

    /*
     * Delete session draft resume; called when canceling edit or manually removing draft.
     */
    function discardPendingResume() {
        return request(window.TARecruitment.routes.me.resumeDraft(), {
            method: "DELETE",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        }).then(function (result) {
            var response = result.response;
            var payload = result.payload;

            if (response.status === 401) {
                handleUnauthorized();
                var unauthorizedError = new Error("Unauthorized.");
                unauthorizedError.userMessage = localizeText("portal.dynamic.sessionExpiredRedirect", "Your session has expired. Redirecting to login...");
                throw unauthorizedError;
            }

            if (!response.ok || !payload || payload.success !== true) {
                var errorMessage = payload && typeof payload.message === "string" && payload.message.trim()
                    ? localizeServerMessage(payload.message, "portal.dynamic.resumeDiscardFailed", localizeText("portal.dynamic.resumeDiscardFailed", "Unable to discard the pending resume. Please try again."))
                    : localizeText("portal.dynamic.resumeDiscardFailed", "Unable to discard the pending resume. Please try again.");
                var discardError = new Error(errorMessage);
                discardError.userMessage = errorMessage;
                throw discardError;
            }

            syncResumeDraftState(extractData(payload));
        });
    }

    /*
     * File size only for frontend display, not involved in backend save.
     */
    function formatFileSize(bytes) {
        if (typeof bytes !== "number" || bytes < 0) {
            return "0 B";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(1) + " KB";
        }
        return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    }

    function getPositiveNumber(value) {
        return typeof value === "number" && value > 0 ? value : 0;
    }

    function hasText(value) {
        return typeof value === "string" && value.trim().length > 0;
    }

    /*
     * Backend may return storage path; frontend only displays the last level filename.
     */
    function extractFileNameFromPath(path) {
        if (typeof path !== "string" || !path.trim()) {
            return "";
        }

        var normalizedPath = path.replace(/\\/g, "/");
        var segments = normalizedPath.split("/");
        return segments[segments.length - 1] || normalizedPath;
    }

    /*
     * Force validate all fields before submission, return first error for focus.
     */
    function validateForm() {
        var firstError = null;

        orderedInputKeys.forEach(function (key) {
            if (!inputs[key]) {
                return;
            }

            fieldValidationState.touchedByKey[key] = true;
            var result = validateSingleField(key, { forceRequired: true, animate: true });
            if (!firstError && result && result.message) {
                firstError = buildValidationError(result.message, result.field);
            }
        });

        return firstError;
    }

    /*
     * Initialize realtime validation: show errors only after user touches field, reducing interference when first opening page.
     */
    function initializeRealtimeValidation() {
        Object.keys(inputs).forEach(function (key) {
            var field = inputs[key];
            if (!field) {
                return;
            }

            fieldValidationState.feedbackByKey[key] = ensureFieldFeedbackNode(key, field);
            fieldValidationState.touchedByKey[key] = false;

            field.addEventListener("blur", function () {
                if (!canValidateField(field)) {
                    return;
                }
                var value = typeof field.value === "string" ? field.value.trim() : "";
                if (!value) return;
                fieldValidationState.touchedByKey[key] = true;
                validateSingleField(key, { forceRequired: true });
            });

            if (field.tagName === "SELECT") {
                field.addEventListener("change", function () {
                    if (!canValidateField(field)) {
                        return;
                    }
                    validateSingleField(key, {
                        forceRequired: fieldValidationState.touchedByKey[key] === true
                    });
                });
                return;
            }

            field.addEventListener("input", function () {
                if (!canValidateField(field)) {
                    return;
                }
                validateSingleField(key, {
                    forceRequired: fieldValidationState.touchedByKey[key] === true
                });
            });

            field.addEventListener("change", function () {
                if (!canValidateField(field)) {
                    return;
                }
                validateSingleField(key, {
                    forceRequired: fieldValidationState.touchedByKey[key] === true
                });
            });
        });
    }

    /*
     * Form Enter key: jump to next field by default, submit on last field to reduce accidental save probability.
     */
    function initializeEnterKeyBehavior() {
        form.addEventListener("keydown", function (event) {
            if (!event || event.key !== "Enter" || event.isComposing) {
                return;
            }

            var target = event.target;
            if (!target || target.form !== form) {
                return;
            }

            // Shift+Enter keeps native multiline editing; Enter alone moves forward.
            if (target.tagName === "TEXTAREA" && event.shiftKey) {
                return;
            }

            // Allow explicit submit from submit button.
            if (target === submitButton || (target.tagName === "BUTTON" && target.type === "submit")) {
                return;
            }

            // Avoid accidental submit from Enter while filling fields.
            event.preventDefault();

            if (!isProfileFormEditable() || target.disabled) {
                return;
            }

            if (!focusNextFormControl(target)) {
                submitFormFromEnter();
            }
        });
    }

    /*
     * Unified check if editing and realtime validation are currently allowed.
     */
    function isProfileFormEditable() {
        return (!state.hasExistingProfile || state.isEditing) && !state.isLoading && !state.isSubmitting;
    }

    /*
     * Fields only participate in realtime validation when form is editable and not disabled.
     */
    function canValidateField(field) {
        return !!field && isProfileFormEditable() && !field.disabled;
    }

    /*
     * Look up inputs key from DOM field, used by keyboard navigation and validation positioning.
     */
    function getFieldKeyByElement(element) {
        var matchedKey = "";
        Object.keys(inputs).some(function (key) {
            if (inputs[key] === element) {
                matchedKey = key;
                return true;
            }
            return false;
        });
        return matchedKey;
    }

    /*
     * Move focus in order of orderedInputKeys, skipping non-existent or disabled controls.
     */
    function focusNextFormControl(current) {
        var orderedControls = [];
        orderedInputKeys.forEach(function (key) {
            if (inputs[key]) {
                orderedControls.push(inputs[key]);
            }
        });

        var currentIndex = orderedControls.indexOf(current);
        if (currentIndex < 0) {
            return false;
        }

        var next;
        var i;
        for (i = currentIndex + 1; i < orderedControls.length; i += 1) {
            next = orderedControls[i];
            if (!next || next.disabled || typeof next.focus !== "function") {
                continue;
            }
            next.focus();
            return true;
        }

        return false;
    }

    /*
     * Compatible with browsers that do not support requestSubmit.
     */
    function submitFormFromEnter() {
        if (!submitButton || submitButton.disabled) {
            return;
        }

        if (typeof form.requestSubmit === "function") {
            form.requestSubmit(submitButton);
            return;
        }

        submitButton.click();
    }

    /*
     * Create or reuse error hint node for field and establish aria-describedby association.
     */
    function ensureFieldFeedbackNode(key, field) {
        var container = field.closest(".field");
        if (!container) {
            return null;
        }

        var selector = ".field-feedback[data-for=\"" + key + "\"]";
        var feedback = container.querySelector(selector);
        if (!feedback) {
            feedback = document.createElement("p");
            feedback.className = "field-feedback";
            feedback.setAttribute("data-for", key);
            feedback.setAttribute("role", "status");
            feedback.setAttribute("aria-live", "polite");
            feedback.id = field.id ? field.id + "-feedback" : key + "-feedback";

            var fieldHint = container.querySelector(".field-hint");
            if (fieldHint) {
                container.insertBefore(feedback, fieldHint);
            } else {
                container.appendChild(feedback);
            }
        }

        var describedBy = field.getAttribute("aria-describedby");
        if (!describedBy) {
            field.setAttribute("aria-describedby", feedback.id);
        } else if ((" " + describedBy + " ").indexOf(" " + feedback.id + " ") === -1) {
            field.setAttribute("aria-describedby", describedBy + " " + feedback.id);
        }

        return feedback;
    }

    /*
     * Clear all field errors when switching mode or reloading profile.
     */
    function clearAllFieldValidation() {
        Object.keys(inputs).forEach(function (key) {
            setFieldValidationResult(key, "");
        });
    }

    /*
     * Reset touched to avoid backend-filled fields being treated as user input.
     */
    function resetFieldTouchedState() {
        Object.keys(inputs).forEach(function (key) {
            fieldValidationState.touchedByKey[key] = false;
        });
    }

    /*
     * Write single field validation result, sync aria-invalid.
     */
    function setFieldValidationResult(key, message, animate) {
        var field = inputs[key];
        var feedback = fieldValidationState.feedbackByKey[key];
        if (!field || !feedback) {
            return;
        }

        if (message) {
            feedback.textContent = message;
            feedback.classList.add("is-visible");
            field.classList.add("is-invalid");
            field.setAttribute("aria-invalid", "true");
            if (animate) {
                field.classList.remove("is-flashing");
                requestAnimationFrame(function () {
                    requestAnimationFrame(function () {
                        field.classList.add("is-flashing");
                    });
                });
            }
            return;
        }

        feedback.textContent = "";
        feedback.classList.remove("is-visible");
        field.classList.remove("is-invalid", "is-flashing");
        field.removeAttribute("aria-invalid");
    }

    /*
     * Single field validation entry point, shared by realtime and submission validation.
     */
    function validateSingleField(key, options) {
        var field = inputs[key];
        if (!field) {
            return null;
        }

        if (field.disabled) {
            setFieldValidationResult(key, "");
            return {
                field: field,
                message: ""
            };
        }

        var settings = options || {};
        var forceRequired = settings.forceRequired === true;
        var animate = settings.animate === true;
        var value = typeof field.value === "string" ? field.value.trim() : "";
        var message = getFieldValidationMessage(key, value, forceRequired);
        setFieldValidationResult(key, message, animate);

        return {
            field: field,
            message: message
        };
    }

    /*
     * TA profile validation text unified under portal.taDashboard.validation namespace.
     */
    function tv(key, fallback) {
        return localizeText("portal.taDashboard.validation." + key, fallback);
    }

    /*
     * Frontend validation for immediate feedback; final trusted validation still done by ApplicantProfileValidator.
     */
    function getFieldValidationMessage(key, value, forceRequired) {
        var isRequired = key === "fullName"
            || key === "studentId"
            || key === "department"
            || key === "program"
            || key === "gpa"
            || key === "phone"
            || key === "skills"
            || key === "experience"
            || key === "motivation";
        if (isRequired && forceRequired && !value) {
            if (key === "fullName") {
                return tv("fullName.required", "Please enter your full name.");
            }
            if (key === "studentId") {
                return tv("studentId.required", "Please enter your student ID.");
            }
            if (key === "department") {
                return tv("department.required", "Please enter your department.");
            }
            if (key === "program") {
                return tv("program.required", "Please select your program.");
            }
            if (key === "gpa") {
                return tv("gpa.required", "Please enter your GPA.");
            }
            if (key === "phone") {
                return tv("phone.required", "Please enter your phone number.");
            }
            if (key === "skills") {
                return tv("skills.required", "Please enter at least one skill.");
            }
            if (key === "experience") {
                return tv("experience.required", "Please describe your related experience.");
            }
            return tv("motivation.required", "Please explain your motivation.");
        }

        if (!value) {
            return "";
        }

        if (key === "fullName") {
            if (value.length > 100) {
                return tv("fullName.tooLong", "Full name must be 100 characters or fewer.");
            }
            if (value.length < 2) {
                return tv("fullName.tooShort", "Full name must be at least 2 characters.");
            }
            if (!hasLetterOrCjk(value)) {
                return tv("fullName.noLetter", "Full name must include at least one letter.");
            }
            if (!/^[A-Za-z\u00C0-\u024F\u4E00-\u9FFF\s.'-]+$/.test(value)) {
                return tv("fullName.invalidChars", "Full name may only include letters, spaces, apostrophes, periods, and hyphens.");
            }
            if (hasExcessiveRepeatedChars(value, 4)) {
                return tv("fullName.tooManyRepeated", "Full name contains too many repeated characters.");
            }
            return "";
        }

        if (key === "studentId") {
            if (!/^\d{10}$/.test(value)) {
                return tv("studentId.notTenDigits", "Student ID must be exactly 10 digits, for example 2023213039.");
            }
            if (!/^20\d{8}$/.test(value)) {
                return tv("studentId.notStartWith20", "Student ID should start with 20, for example 2023213051.");
            }
            var intakeYear = parseInt(value.substring(0, 4), 10);
            if (isNaN(intakeYear) || intakeYear < 2010 || intakeYear > 2099) {
                return tv("studentId.invalidYear", "Student ID year appears invalid. Please check the first 4 digits.");
            }
            if (/^(\d)\1{9}$/.test(value)) {
                return tv("studentId.allSameDigit", "Student ID appears invalid. Please check your official 10-digit student number.");
            }
            return "";
        }

        if (key === "department") {
            if (value.length > 100) {
                return tv("department.tooLong", "Department must be 100 characters or fewer.");
            }
            if (value.length < 2) {
                return tv("department.tooShort", "Department must be at least 2 characters.");
            }
            if (!hasLetterOrCjk(value)) {
                return tv("department.noLetter", "Department should include letters.");
            }
            if (!/^[A-Za-z0-9\u00C0-\u024F\u4E00-\u9FFF\s&(),./'-]+$/.test(value)) {
                return tv("department.invalidChars", "Department contains unsupported characters.");
            }
            if (hasExcessiveRepeatedChars(value, 6)) {
                return tv("department.tooManyRepeated", "Department contains too many repeated characters.");
            }
            return "";
        }

        if (key === "program") {
            if (["Undergraduate", "Master", "PhD"].indexOf(value) === -1) {
                return tv("program.invalidOption", "Please select a valid program option.");
            }
            return "";
        }

        if (key === "gpa") {
            if (value.length > 20) {
                return tv("gpa.tooLong", "GPA must be 20 characters or fewer.");
            }
            if (!/^[0-9.,/\s]+$/.test(value)) {
                return tv("gpa.invalidChars", "GPA may only include digits, spaces, decimal separators, and '/'.");
            }

            var normalized = value.replace(/\s+/g, "").replace(/,/g, ".");
            if (normalized.split("/").length > 2) {
                return tv("gpa.multipleSlash", "GPA format is invalid. Use one optional '/'.");
            }
            var parts = normalized.split("/");
            if (!/^\d{1,3}(\.\d{1,2})?$/.test(parts[0])) {
                return tv("gpa.invalidValue", "GPA value supports up to 2 decimal places.");
            }

            var actual = parseFloat(parts[0]);
            if (isNaN(actual) || actual < 0) {
                return tv("gpa.negative", "GPA cannot be negative.");
            }

            if (parts.length === 2) {
                if (!/^\d{1,3}(\.\d{1,2})?$/.test(parts[1])) {
                    return tv("gpa.invalidScale", "GPA scale supports up to 2 decimal places.");
                }
                var scale = parseFloat(parts[1]);
                if (isNaN(scale) || scale < 4 || scale > 100) {
                    return tv("gpa.scaleOutOfRange", "GPA scale should be between 4 and 100.");
                }
                if (actual > scale) {
                    return tv("gpa.valueExceedsScale", "GPA value cannot be greater than the GPA scale.");
                }
            } else {
                if (actual > 4.3) {
                    return tv("gpa.tooHighWithoutScale", "For GPA above 4.3, please include scale (for example 85/100).");
                }
            }
            return "";
        }

        if (key === "skills") {
            if (value.length > 300) {
                return tv("skills.tooLong", "Skills must be 300 characters or fewer.");
            }
            if (UNSUPPORTED_SKILL_SEPARATOR_PATTERN.test(value)) {
                return tv("skills.useCommaSeparator", "Use English commas or Chinese commas to separate skills.");
            }
            if (/(^[,，]|[,，]\s*[,，]|[,，]\s*$)/.test(value)) {
                return tv("skills.emptyItems", "Please remove empty skill items between commas.");
            }

            var items = value.split(SKILL_SEPARATOR_PATTERN).map(function (item) {
                return item.trim();
            }).filter(function (item) {
                return item.length > 0;
            });

            if (items.length === 0) {
                return "";
            }
            if (items.length > 12) {
                return tv("skills.tooManySkills", "Please list up to 12 skills.");
            }

            var seen = {};
            var i;
            for (i = 0; i < items.length; i += 1) {
                var skill = items[i];
                if (skill.length < 2 || skill.length > 40) {
                    return tv("skills.skillLength", "Each skill should be 2 to 40 characters.");
                }
                if (!hasLetterOrCjk(skill)) {
                    return tv("skills.noLetter", "Each skill should include letters.");
                }
                if (!/^[A-Za-z0-9\u00C0-\u024F\u4E00-\u9FFF+#&./\-\s]+$/.test(skill)) {
                    return tv("skills.invalidChars", "Skills contain unsupported characters.");
                }
                if (hasExcessiveRepeatedChars(skill, 5)) {
                    return tv("skills.tooManyRepeated", "A skill item has too many repeated characters.");
                }
                var normalizedSkill = skill.toLowerCase().replace(/\s+/g, " ");
                if (seen[normalizedSkill]) {
                    return tv("skills.duplicate", "Duplicate skills found. Please keep each skill only once.");
                }
                seen[normalizedSkill] = true;
            }
            return "";
        }

        if (key === "phone") {
            if (value.length > 30) {
                return tv("phone.tooLong", "Phone number must be 30 characters or fewer.");
            }
            if (!/^[\d+\-()./\s]+$/.test(value)) {
                return tv("phone.invalidChars", "Phone number may only include digits, spaces, and + - ( ) . /.");
            }

            var plusMatches = value.match(/\+/g);
            if (plusMatches && plusMatches.length > 1) {
                return tv("phone.multiplePlus", "Phone number can contain only one '+'.");
            }
            if (value.indexOf("+") > 0) {
                return tv("phone.plusNotAtStart", "If used, '+' must be at the beginning.");
            }
            if (!hasBalancedParentheses(value)) {
                return tv("phone.unbalancedParens", "Phone number parentheses are not balanced.");
            }

            var digits = value.replace(/\D/g, "");
            if (digits.length < 8 || digits.length > 15) {
                return tv("phone.digitCount", "Phone number should contain 8 to 15 digits.");
            }
            if (/^(\d)\1+$/.test(digits)) {
                return tv("phone.allSameDigit", "Phone number appears invalid. Please check repeated digits.");
            }
            if (value.charAt(0) === "+" && digits.length < 10) {
                return tv("phone.internationalTooShort", "International format should usually contain at least 10 digits.");
            }
            return "";
        }

        if (key === "experience") {
            return validateLongTextField(value, "experience");
        }

        if (key === "motivation") {
            return validateLongTextField(value, "motivation");
        }

        return "";
    }

    /*
     * Name, department, skills etc. must contain at least readable text, not just symbols or numbers.
     */
    function hasLetterOrCjk(text) {
        return /[A-Za-z\u00C0-\u024F\u4E00-\u9FFF]/.test(text || "");
    }

    /*
     * Phone field allows parentheses but must be balanced.
     */
    function hasBalancedParentheses(text) {
        var balance = 0;
        var i;
        for (i = 0; i < text.length; i += 1) {
            var char = text.charAt(i);
            if (char === "(") {
                balance += 1;
            } else if (char === ")") {
                balance -= 1;
                if (balance < 0) {
                    return false;
                }
            }
        }
        return balance === 0;
    }

    /*
     * Roughly block meaningless repeated characters, e.g. aaaaaa or 111111.
     */
    function hasExcessiveRepeatedChars(text, threshold) {
        if (!text) {
            return false;
        }
        var safeThreshold = typeof threshold === "number" ? Math.max(1, threshold) : 4;
        var repeatedPattern = new RegExp("(.)\\1{" + safeThreshold + ",}");
        return repeatedPattern.test(text);
    }

    /*
     * Estimate content amount for mixed Chinese/English long text by “Chinese characters + English words”.
     */
    function getTextContentUnits(text) {
        if (!text) {
            return 0;
        }
        var cjkChars = text.match(/[\u4E00-\u9FFF]/g) || [];
        var latinWords = text
            .replace(/[\u4E00-\u9FFF]/g, " ")
            .match(/[A-Za-z0-9][A-Za-z0-9'-]*/g) || [];
        return cjkChars.length + latinWords.length;
    }

    /*
     * Experience and motivation share long text validation, requiring not just vague one or two words.
     */
    function validateLongTextField(value, keyPrefix) {
        if (value.length > 1200) {
            return tv(keyPrefix + ".tooLong", "Must be 1200 characters or fewer.");
        }
        if (!value) {
            return "";
        }
        if (value.length < 20) {
            return tv(keyPrefix + ".tooShort", "Should be at least 20 characters if provided.");
        }
        if (getTextContentUnits(value) < 10) {
            return tv(keyPrefix + ".notEnoughDetail", "Please provide more detail (about 10 words/characters).");
        }
        if (hasExcessiveRepeatedChars(value, 8)) {
            return tv(keyPrefix + ".tooManyRepeated", "Contains too many repeated characters.");
        }
        return "";
    }

    /*
     * Unify submission validation error structure for focusing first error field.
     */
    function buildValidationError(message, field) {
        return {
            message: message,
            field: field
        };
    }

    /*
     * Page requests prefer using shared TARecruitment.api.request for consistent context path and JSON parsing.
     */
    function request(url, options) {
        if (window.TARecruitment && window.TARecruitment.api) {
            return window.TARecruitment.api.request(url, options, { parser: parseResponse });
        }
        return fetch(url, options).then(function (response) {
            return response.text().then(function (bodyText) {
                return {
                    response: response,
                    payload: parseResponse(bodyText)
                };
            });
        });
    }

    /*
     * Profile API should return standard JSON; parse failure throws error into catch.
     */
    function parseResponse(bodyText) {
        return JSON.parse(bodyText);
    }

    /*
     * Standard response uses payload.data; compatible with legacy test calls that return object directly.
     */
    function extractData(payload) {
        if (!payload || typeof payload !== "object") {
            return {};
        }
        if (payload.data && typeof payload.data === "object") {
            return payload.data;
        }
        return payload;
    }

    /*
     * Normalize Chinese/English commas to English comma separator before submitting to backend.
     */
    function normalizeSkillsForSubmit(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }

        return value
            .split(SKILL_SEPARATOR_PATTERN)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            })
            .join(",");
    }

    /*
     * Backend legacy data may be stored with semicolons or Chinese commas; display normalizes to comma+space.
     */
    function formatSkillsForDisplay(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }

        return value
            .split(/[;,，]/)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            })
            .join(", ");
    }

    /*
     * Safe fill for normal input fields.
     */
    function setFieldValue(field, value) {
        if (field) {
            field.value = typeof value === "string" ? value : "";
        }
    }

    /*
     * If old CSV has a program not in current dropdown, temporarily inject option so user can see original value.
     */
    function setSelectValue(field, value) {
        if (!field) {
            return;
        }

        var normalizedValue = typeof value === "string" ? value.trim() : "";
        if (!normalizedValue) {
            field.value = "";
            return;
        }

        var hasOption = Array.prototype.some.call(field.options, function (option) {
            return option.value === normalizedValue;
        });

        if (!hasOption) {
            var injectedOption = document.createElement("option");
            injectedOption.value = normalizedValue;
            injectedOption.textContent = normalizedValue;
            field.appendChild(injectedOption);
        }

        field.value = normalizedValue;
    }

    /*
     * General prompt for submission failure; specific errors still displayed next to fields.
     */
    function showValidationSummaryMessage() {
        showMessage(
            localizeText("portal.dynamic.fixHighlightedFields", "Please fix the highlighted fields and try again."),
            "error",
            true
        );
    }

    /*
     * Top form message for save success, network failure, and session expiration.
     */
    function showMessage(message, type, animate) {
        messageBox.textContent = message;
        messageBox.classList.remove("hidden", "error", "success", "is-flashing");
        messageBox.classList.add(type === "success" ? "success" : "error");
        if (animate) {
            requestAnimationFrame(function () {
                requestAnimationFrame(function () {
                    messageBox.classList.add("is-flashing");
                });
            });
        }
    }

    /*
     * Clear old general message before new operation.
     */
    function hideMessage() {
        messageBox.textContent = "";
        messageBox.classList.remove("error", "success", "is-flashing");
        messageBox.classList.add("hidden");
    }

    /*
     * Login state expired redirects to login page, preserves contextPath for non-root path deployment compatibility.
     */
    function handleUnauthorized() {
        showMessage(localizeText("portal.dynamic.sessionExpiredRedirect", "Your session has expired. Redirecting to login..."), "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 1000);
    }

    /*
     * Sync real name with shared sidebar account profile: notify other side to update when either side changes.
     */
    function setupSharedRealNameSync() {
        if (inputs.fullName) {
            inputs.fullName.addEventListener("input", function () {
                announceTaProfileRealName(inputs.fullName.value || "");
            });
        }

        document.addEventListener("account-profile-updated", function (event) {
            var detail = event && event.detail ? event.detail : {};
            var realName = typeof detail.realName === "string" ? detail.realName : "";
            if (!inputs.fullName || inputs.fullName.value === realName) {
                return;
            }
            setFieldValue(inputs.fullName, realName);
            if (canValidateField(inputs.fullName)) {
                validateSingleField("fullName", { forceRequired: true });
            }
        });
    }

    /*
     * Broadcast TA profile real name; portal-sidebar.jspf uses it to sync account profile form.
     */
    function announceTaProfileRealName(realName) {
        if (typeof window.CustomEvent !== "function") {
            return;
        }
        document.dispatchEvent(new CustomEvent("ta-profile-real-name-updated", {
            detail: {
                realName: typeof realName === "string" ? realName : ""
            }
        }));
    }

    /*
     * Open resume preview: local newly selected file can be previewed temporarily, session draft needs to be saved as formal resume first.
     */
    function openResumePreview() {
        var url = "";
        if (state.selectedResumeFile) {
            url = URL.createObjectURL(state.selectedResumeFile);
            openPreviewUrl(url, true);
            return;
        }
        if (state.pendingResumePath) {
            showResumeMessage(
                localizeText("portal.dynamic.saveResumeBeforePreview", "Save changes before previewing the uploaded resume."),
                "success"
            );
            return;
        } else if (hasSavedResume()) {
            url = window.TARecruitment.routes.me.applicantResume();
        }
        if (url) {
            openPreviewUrl(url, false);
        }
    }

    /*
     * Fallback to current page navigation when new window fails, compatible with browser popup blocker.
     */
    function openPreviewUrl(url, openInNewTab) {
        if (!openInNewTab) {
            window.location.href = url;
            return;
        }
        var previewWindow = window.open(url, "_blank", "noopener");
        if (!previewWindow) {
            window.location.href = url;
        }
    }

    /*
     * Open photo preview layer on current page without additional backend metadata request.
     */
    function openPhotoLightbox() {
        var url = "";
        if (state.selectedPhotoFile && state.photoObjectUrl) {
            url = state.photoObjectUrl;
        } else if (hasSavedPhoto()) {
            url = buildSavedPhotoPreviewUrl();
        }
        if (!url) {
            return;
        }

        var overlay = document.createElement("div");
        overlay.className = "photo-lightbox-overlay";
        overlay.setAttribute("role", "dialog");
        overlay.setAttribute("aria-modal", "true");
        overlay.setAttribute("aria-label", localizeText("portal.taDashboard.photoUploadTitle", "Photo preview"));

        var img = document.createElement("img");
        img.src = url;
        img.className = "photo-lightbox-image";
        img.alt = localizeText("portal.taDashboard.profilePhotoAlt", "Profile photo");

        var closeBtn = document.createElement("button");
        closeBtn.className = "photo-lightbox-close";
        closeBtn.setAttribute("type", "button");
        closeBtn.setAttribute("aria-label", localizeText("portal.common.close", "Close"));
        closeBtn.innerHTML = '<svg viewBox="0 0 24 24" focusable="false" aria-hidden="true"><path d="m7 7 10 10"/><path d="M17 7 7 17"/></svg>';

        overlay.appendChild(closeBtn);
        overlay.appendChild(img);
        document.body.appendChild(overlay);

        requestAnimationFrame(function () {
            overlay.classList.add("is-visible");
        });

        function closeLightbox() {
            overlay.classList.remove("is-visible");
            setTimeout(function () {
                if (overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            }, 220);
        }

        closeBtn.addEventListener("click", closeLightbox);
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) {
                closeLightbox();
            }
        });

        function escHandler(e) {
            if (e.key === "Escape") {
                closeLightbox();
                document.removeEventListener("keydown", escHandler);
            }
        }
        document.addEventListener("keydown", escHandler);
    }

})();

/*
 * TA dashboard 档案脚本，对应 /jsp/ta/dashboard.jsp。
 *
 * 负责 TA 档案表单、简历草稿上传、档案照片预览/移除，以及保存后刷新侧边栏头像。
 * 相关 API：/api/me/applicant-profile、/api/me/applicant-profile/resume-draft、
 * /api/me/applicant-profile/photo、/api/me/applicant-profile/resume。
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

    // 表单字段映射：key 与后端 ApplicantProfileRequestMapper 接收的字段名保持一致。
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
        // saved* 是后端已保存的档案资源；pending* 是当前 session 的简历草稿；selected* 是浏览器本地刚选的文件。
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

    // touched 用于区分“用户已经编辑过”和“初始化填值”，避免加载旧档案时立刻报错。
    var fieldValidationState = {
        feedbackByKey: {},
        touchedByKey: {}
    };

    // 校验摘要按页面视觉顺序定位第一个错误字段。
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
     * 创建 TA 档案：先做前端必填/文件校验，再提交 /api/me/applicant-profile。
     * 成功后不直接相信本地表单，而是重新 GET 一次后端保存结果。
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
     * 页面初始化和保存后都会调用：读取当前账号的 TA 档案、已保存简历/照片、session 草稿。
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
     * 更新已有档案：只在编辑模式触发，流程与创建一致但会保留旧资源的删除标记。
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
     * 组装档案提交体。
     * 没照片时使用表单编码；有照片或更新照片状态时使用 multipart，避免前端另走上传接口。
     */
    function submitProfile(isUpdate) {
        if (isUpdate) {
            // 遗留/待移除：当前前端更新档案仍走 multipart POST 分支。
            // 保留原因：同一条路径可以同时提交文本字段和照片；后续若统一为 PUT multipart，可清理这段绕路说明。
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
     * 把后端档案结果回填到只读表单，同时同步侧边栏显示名和资源状态。
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
     * 没有档案或读取失败时进入创建态；payload 可能只包含 session 内的简历草稿。
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
     * 保存/编辑/取消按钮的可见性由“是否已有档案”和“是否正在编辑”共同决定。
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
     * 进入编辑态时恢复表单可编辑，但仍保留已保存资源的展示。
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
     * 取消编辑会丢弃本次 session 上传的简历草稿，再从后端重新加载已保存档案。
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
     * 根据加载、提交、上传草稿状态刷新主按钮，避免用户在资源处理中重复保存。
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
     * 切换提交中状态，并同步禁用表单和文件上传区域。
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
     * 只控制当前表单字段；上传区按钮由 refreshResumeArea/refreshPhotoArea 单独处理。
     */
    function setFormDisabled(disabled) {
        Array.prototype.forEach.call(formFields, function (field) {
            field.disabled = disabled;
        });
    }

    /*
     * 简历草稿保存在 session 中，尚未成为正式 profile resume。
     */
    function syncResumeDraftState(payload) {
        state.pendingResumePath = payload && typeof payload.pendingResumePath === "string" ? payload.pendingResumePath : "";
        state.pendingResumeName = payload && typeof payload.pendingResumeName === "string" ? payload.pendingResumeName : "";
        state.pendingResumeSize = getPositiveNumber(payload && payload.pendingResumeSize);
    }

    /*
     * 简历是保存档案的前置条件：可以是已保存简历，也可以是本次 session 草稿。
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
     * 照片不是必填；只有用户新选照片时才校验类型和大小。
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
     * 简历上传区必须跟随表单编辑态，避免只读档案被局部改动。
     */
    function canEditResumeSection() {
        if (state.isLoading || state.isSubmitting || state.isUploadingResume) {
            return false;
        }
        return !state.hasExistingProfile || state.isEditing;
    }

    /*
     * 照片区与简历区使用同一套编辑权限。
     */
    function canEditPhotoSection() {
        if (state.isLoading || state.isSubmitting || state.isUploadingResume) {
            return false;
        }
        return !state.hasExistingProfile || state.isEditing;
    }

    /*
     * 选择简历后立即上传为草稿，真正保存档案时后端再把草稿转为正式简历。
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
     * 上传 session 简历草稿；失败时清空本地选择，避免页面显示一个后端不存在的文件。
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
     * 底层草稿上传实现：使用 XMLHttpRequest 是为了给简历草稿上传保留进度扩展点。
     */
    function uploadDraftResumeWithProgress(file) {
        // 使用 XMLHttpRequest 是为了给简历草稿上传保留进度扩展点；当前 UI 只展示上传中/成功/失败。
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
     * 简历文件只允许办公文档和 PDF，大小限制与 ProfileAssetValidator 保持一致。
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
     * 维护浏览器本地选择的简历文件；清空时也重置 input，确保可重新选择同一文件。
     */
    function setSelectedResumeFile(file) {
        state.selectedResumeFile = file || null;
        if (resumeFileInput && !file) {
            resumeFileInput.value = "";
        }
        refreshResumeArea();
    }

    /*
     * 草稿上传态会同时影响保存按钮、简历区和照片区。
     */
    function setResumeUploading(uploading) {
        state.isUploadingResume = uploading;
        refreshSubmitButton();
        refreshResumeArea();
        refreshPhotoArea();
    }

    /*
     * 移除简历按优先级处理：先丢 session 草稿，再清本地选择，最后标记删除已保存简历。
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
     * 根据“本地选择 / session 草稿 / 已保存简历”三层状态刷新简历上传区。
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
     * 只要有任一层简历资源，就允许用户打开预览入口。
     */
    function canPreviewResume() {
        return !state.isUploadingResume
            && (state.selectedResumeFile || state.pendingResumePath || hasSavedResume());
    }

    /*
     * 选择当前应该展示的简历卡片：本地选择优先，其次草稿，最后已保存文件。
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
     * 简历卡片副文案优先显示文件大小，未知大小时显示 ready 状态。
     */
    function buildResumeCardDetail(fileSize) {
        if (typeof fileSize === "number" && fileSize > 0) {
            return formatFileSize(fileSize);
        }
        return localizeText("portal.dynamic.resumeReady", "Resume ready");
    }

    /*
     * 已保存简历被用户标记移除后，本次编辑态不再当作可用资源。
     */
    function hasSavedResume() {
        return hasText(state.resumePath) && !state.removedSavedResume;
    }

    /*
     * 从后端 profile payload 同步已正式保存的简历资源信息。
     */
    function syncSavedResumeState(payload) {
        state.resumePath = payload && typeof payload.resumePath === "string" ? payload.resumePath : "";
        state.resumeName = payload && typeof payload.resumeName === "string" ? payload.resumeName : "";
        state.resumeSize = getPositiveNumber(payload && payload.resumeSize);
    }

    /*
     * 同步已保存照片；路径变化时刷新版本号以避开浏览器缓存。
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
     * 照片只在保存档案时提交；这里先做本地预览和前端格式校验。
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
     * 照片格式和大小限制与后端 ProfileAssetValidator 对齐。
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
     * 维护照片本地预览 URL；替换文件时释放旧 object URL，避免浏览器内存泄漏。
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
     * 移除照片时，本地新选照片直接清空；已保存照片只记录删除标记，等保存时生效。
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
     * 根据本地照片或已保存照片刷新上传壳、缩略图、删除按钮和只读态。
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
     * 当前照片展示来源：本地选择优先，已保存照片次之。
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
     * 照片卡片副文案与简历保持一致：优先大小，未知则显示 ready。
     */
    function buildPhotoCardDetail(fileSize) {
        if (typeof fileSize === "number" && fileSize > 0) {
            return formatFileSize(fileSize);
        }
        return localizeText("portal.dynamic.photoReady", "Photo ready");
    }

    /*
     * 已保存照片被标记删除后，在本次编辑视图中视为不存在。
     */
    function hasSavedPhoto() {
        return hasText(state.photoPath) && !state.removedSavedPhoto;
    }

    function buildSavedPhotoPreviewUrl() {
        // v 参数只用于破坏浏览器图片缓存，让刚上传的新照片立即显示。
        return window.TARecruitment.routes.me.applicantPhoto() + "?v=" + encodeURIComponent(String(state.photoPreviewVersion));
    }

    /*
     * 照片上传区的局部消息，不影响整个档案表单的保存提示。
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
     * 清空照片区局部消息。
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
     * 简历上传区的局部消息，专门提示草稿上传、移除和预览状态。
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
     * 清空简历区局部消息。
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
     * 删除 session 草稿简历；取消编辑或手动移除草稿时调用。
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
     * 文件大小只用于前端展示，不参与后端保存。
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
     * 后端可能返回存储路径；前端只展示最后一级文件名。
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
     * 提交前强制校验全部字段，并返回第一个错误用于聚焦。
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
     * 初始化实时校验：用户触碰字段后再显示错误，减少初次打开页面的干扰。
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
     * 表单内 Enter 默认跳到下一个字段，最后一个字段再提交，降低误保存概率。
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
     * 统一判断当前是否允许编辑和实时校验。
     */
    function isProfileFormEditable() {
        return (!state.hasExistingProfile || state.isEditing) && !state.isLoading && !state.isSubmitting;
    }

    /*
     * 字段只有在表单可编辑且未禁用时才参与实时校验。
     */
    function canValidateField(field) {
        return !!field && isProfileFormEditable() && !field.disabled;
    }

    /*
     * 从 DOM 字段反查 inputs key，供键盘导航和校验定位使用。
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
     * 按 orderedInputKeys 顺序移动焦点，跳过不存在或禁用的控件。
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
     * 兼容不支持 requestSubmit 的浏览器容器。
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
     * 为字段创建或复用错误提示节点，并建立 aria-describedby 关联。
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
     * 切换模式或重新加载档案时清空所有字段错误。
     */
    function clearAllFieldValidation() {
        Object.keys(inputs).forEach(function (key) {
            setFieldValidationResult(key, "");
        });
    }

    /*
     * 重置 touched，避免后端回填字段被当成用户输入。
     */
    function resetFieldTouchedState() {
        Object.keys(inputs).forEach(function (key) {
            fieldValidationState.touchedByKey[key] = false;
        });
    }

    /*
     * 写入单字段校验结果，同时同步 aria-invalid。
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
     * 单字段校验入口，实时校验和提交校验共用。
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
     * TA 档案校验文案统一放在 portal.taDashboard.validation 命名空间。
     */
    function tv(key, fallback) {
        return localizeText("portal.taDashboard.validation." + key, fallback);
    }

    /*
     * 前端校验用于即时反馈；最终可信校验仍由 ApplicantProfileValidator 完成。
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
     * 名称、院系、技能等字段至少要包含可读文字，不能只有符号或数字。
     */
    function hasLetterOrCjk(text) {
        return /[A-Za-z\u00C0-\u024F\u4E00-\u9FFF]/.test(text || "");
    }

    /*
     * 电话字段允许括号，但必须成对出现。
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
     * 粗略拦截无意义重复字符，例如 aaaaaa 或 111111。
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
     * 中英文混合长文本按“中文字符 + 英文词”估算内容量。
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
     * 经验和动机共用长文本校验，要求不是空泛的一两个词。
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
     * 统一提交校验错误结构，便于聚焦第一个错误字段。
     */
    function buildValidationError(message, field) {
        return {
            message: message,
            field: field
        };
    }

    /*
     * 页面请求优先使用公共 TARecruitment.api.request，保证 context path 和 JSON 解析一致。
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
     * 档案接口应返回标准 JSON；解析失败直接抛错进入 catch。
     */
    function parseResponse(bodyText) {
        return JSON.parse(bodyText);
    }

    /*
     * 标准响应使用 payload.data；兼容直接返回对象的旧测试调用。
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
     * 提交给后端前把中英文逗号统一为英文逗号分隔。
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
     * 后端旧数据可能用分号或中文逗号保存，展示时统一成逗号+空格。
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
     * 安全回填普通输入字段。
     */
    function setFieldValue(field, value) {
        if (field) {
            field.value = typeof value === "string" ? value : "";
        }
    }

    /*
     * 如果旧 CSV 中保存了当前下拉没有的 program，临时注入选项让用户能看到原值。
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
     * 提交失败的总提示，具体错误仍显示在字段旁边。
     */
    function showValidationSummaryMessage() {
        showMessage(
            localizeText("portal.dynamic.fixHighlightedFields", "Please fix the highlighted fields and try again."),
            "error",
            true
        );
    }

    /*
     * 档案表单顶部总消息，用于保存成功、网络失败和会话失效。
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
     * 新一轮操作前清空旧总消息。
     */
    function hideMessage() {
        messageBox.textContent = "";
        messageBox.classList.remove("error", "success", "is-flashing");
        messageBox.classList.add("hidden");
    }

    /*
     * 登录态失效时回登录页，保留 contextPath 以兼容非根路径部署。
     */
    function handleUnauthorized() {
        showMessage(localizeText("portal.dynamic.sessionExpiredRedirect", "Your session has expired. Redirecting to login..."), "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 1000);
    }

    /*
     * 与共享侧边栏账号资料同步真实姓名：任一处修改时通知另一处更新。
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
     * 广播 TA 档案姓名，portal-sidebar.jspf 会用它同步账号资料表单。
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
     * 打开简历预览：本地新选文件可临时预览，session 草稿需要先保存成正式简历。
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
     * 新窗口失败时降级为当前页跳转，兼容浏览器弹窗拦截。
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
     * 在当前页打开照片预览层，不额外请求后端元数据。
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

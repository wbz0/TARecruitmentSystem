(function () {
    var form = document.getElementById("ta-profile-form");
    if (!form) {
        return;
    }

    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var messageBox = document.getElementById("form-message");
    var bannerBox = document.getElementById("existing-profile-banner");
    var submitButton = document.getElementById("profile-submit");
    var formFields = form.querySelectorAll("input, select, textarea");
    var resumeFileInput = document.getElementById("resume-file-input");
    var resumeFileName = document.getElementById("resume-file-name");
    var resumeUploadButton = document.getElementById("resume-upload-btn");
    var resumeUploadMessage = document.getElementById("resume-upload-message");
    var resumeCurrentInfo = document.getElementById("resume-current-info");
    var resumeProgressWrap = document.getElementById("resume-progress-wrap");
    var resumeProgressBar = document.getElementById("resume-progress-bar");
    var resumeProgressText = document.getElementById("resume-progress-text");
    var resumeProgressStatus = document.getElementById("resume-progress-status");

    var ALLOWED_RESUME_EXTENSIONS = [".pdf", ".doc", ".docx"];
    var MAX_RESUME_SIZE = 10 * 1024 * 1024;

    var inputs = {
        fullName: document.getElementById("full-name"),
        studentId: document.getElementById("student-id"),
        department: document.getElementById("department"),
        program: document.getElementById("program"),
        gpa: document.getElementById("gpa"),
        skills: document.getElementById("skills"),
        phone: document.getElementById("phone"),
        address: document.getElementById("address"),
        experience: document.getElementById("experience"),
        motivation: document.getElementById("motivation")
    };

    var state = {
        hasExistingProfile: false,
        isSubmitting: false,
        isLoading: false,
        isUploadingResume: false,
        selectedResumeFile: null,
        resumePath: ""
    };

    var fieldValidationState = {
        feedbackByKey: {},
        touchedByKey: {}
    };

    var orderedInputKeys = [
        "fullName",
        "studentId",
        "department",
        "program",
        "gpa",
        "phone",
        "skills",
        "address",
        "experience",
        "motivation"
    ];

    initializeRealtimeValidation();
    initializeEnterKeyBehavior();

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        if (state.hasExistingProfile || state.isSubmitting || state.isLoading) {
            return;
        }

        handleCreate();
    });

    if (resumeFileInput) {
        resumeFileInput.addEventListener("change", handleResumeFileChange);
    }

    if (resumeUploadButton) {
        resumeUploadButton.addEventListener("click", handleManualResumeUpload);
    }

    refreshResumeArea();
    hideResumeProgress();
    loadExistingProfile({ silentWhenMissing: true });

    function handleCreate() {
        hideMessage();

        var validationError = validateForm();
        if (validationError) {
            showMessage("Please fix the highlighted fields and try again.", "error");
            if (validationError.field && typeof validationError.field.focus === "function") {
                validationError.field.focus();
            }
            return;
        }

        setSubmitting(true);

        submitProfile()
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (response.status === 409) {
                    showMessage("A profile already exists for this account. Loading your saved profile...", "error");
                    return loadExistingProfile({ afterCreate: false, silentWhenMissing: false });
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = "Unable to create your profile. Please review the form and try again.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                if (!state.selectedResumeFile) {
                    return loadExistingProfile({ afterCreate: true, silentWhenMissing: false });
                }

                showMessage("Profile created. Uploading your selected resume...", "success");
                return uploadSelectedResume({ expectExistingProfile: true, fromCreateFlow: true })
                    .then(function () {
                        return { uploadSuccess: true };
                    })
                    .catch(function (error) {
                        var uploadErrorMessage = "Profile created, but resume upload failed. Please try uploading again.";
                        if (error && typeof error.userMessage === "string" && error.userMessage.trim()) {
                            uploadErrorMessage = error.userMessage.trim();
                        }
                        showResumeMessage(uploadErrorMessage, "error");
                        return {
                            uploadSuccess: false
                        };
                    })
                    .then(function (uploadResult) {
                        var createdNow = uploadResult && uploadResult.uploadSuccess === true;
                        return loadExistingProfile({ afterCreate: createdNow, silentWhenMissing: false });
                    });
            })
            .catch(function () {
                showMessage("Network error. Please try again in a moment.", "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    function loadExistingProfile(options) {
        var settings = options || {};

        state.isLoading = true;
        if (!state.isSubmitting) {
            submitButton.disabled = true;
            submitButton.textContent = "Checking profile...";
        }

        return request(contextPath + "/applicant", {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 404) {
                    enableCreateMode();
                    if (!settings.silentWhenMissing) {
                        showMessage("No profile found yet. Please complete the form below.", "success");
                    }
                    return;
                }

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    enableCreateMode();
                    var errorMessage = "Unable to load your current profile. You can still create one below.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                applyExistingProfile(payload, settings.afterCreate === true);
            })
            .catch(function () {
                enableCreateMode();
                showMessage("Unable to check your existing profile right now. You can still try creating one.", "error");
            })
            .finally(function () {
                state.isLoading = false;
                refreshSubmitButton();
                refreshResumeArea();
            });
    }

    function submitProfile() {
        var formData = new URLSearchParams();
        formData.set("fullName", inputs.fullName.value.trim());
        formData.set("studentId", inputs.studentId.value.trim());
        formData.set("department", inputs.department.value.trim());
        formData.set("program", inputs.program.value.trim());
        formData.set("gpa", inputs.gpa.value.trim());
        formData.set("skills", normalizeSkillsForSubmit(inputs.skills.value));
        formData.set("phone", inputs.phone.value.trim());
        formData.set("address", inputs.address.value.trim());
        formData.set("experience", inputs.experience.value.trim());
        formData.set("motivation", inputs.motivation.value.trim());

        return request(contextPath + "/applicant", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        });
    }

    function applyExistingProfile(payload, createdNow) {
        state.hasExistingProfile = true;
        state.resumePath = payload && typeof payload.resumePath === "string" ? payload.resumePath : "";

        setFieldValue(inputs.fullName, payload.fullName);
        setFieldValue(inputs.studentId, payload.studentId);
        setFieldValue(inputs.department, payload.department);
        setSelectValue(inputs.program, payload.program);
        setFieldValue(inputs.gpa, payload.gpa);
        setFieldValue(inputs.skills, formatSkillsForDisplay(payload.skills));
        setFieldValue(inputs.phone, payload.phone);
        setFieldValue(inputs.address, payload.address);
        setFieldValue(inputs.experience, payload.experience);
        setFieldValue(inputs.motivation, payload.motivation);

        clearAllFieldValidation();
        resetFieldTouchedState();
        setFormDisabled(true);
        form.classList.add("is-readonly");

        var completeness = typeof payload.completeness === "number" ? payload.completeness : null;
        var missingCount = Array.isArray(payload.missingFields) ? payload.missingFields.length : 0;
        var bannerMessage = "Your profile has already been created and is now shown in read-only mode.";

        if (completeness !== null) {
            bannerMessage += " Current completeness: " + completeness + "%.";
        }
        if (missingCount > 0) {
            bannerMessage += " You can continue improving the remaining fields and upload or replace your resume from the side panel.";
        }

        showBanner(bannerMessage);
        submitButton.textContent = "Profile already created";
        submitButton.disabled = true;
        refreshResumeArea();

        if (createdNow) {
            showMessage("Profile created successfully. Your saved information is now displayed below.", "success");
        } else {
            hideMessage();
        }
    }

    function enableCreateMode() {
        state.hasExistingProfile = false;
        state.resumePath = "";
        setFormDisabled(false);
        form.classList.remove("is-readonly");
        hideBanner();
        clearAllFieldValidation();
        resetFieldTouchedState();
        refreshSubmitButton();
        refreshResumeArea();
    }

    function refreshSubmitButton() {
        if (state.hasExistingProfile) {
            submitButton.textContent = "Profile already created";
            submitButton.disabled = true;
            return;
        }

        if (state.isSubmitting) {
            submitButton.textContent = "Creating profile...";
            submitButton.disabled = true;
            return;
        }

        if (state.isLoading) {
            submitButton.textContent = "Checking profile...";
            submitButton.disabled = true;
            return;
        }

        submitButton.textContent = "Create profile";
        submitButton.disabled = false;
    }

    function setSubmitting(submitting) {
        state.isSubmitting = submitting;
        if (!state.hasExistingProfile) {
            setFormDisabled(submitting);
        }
        refreshSubmitButton();
    }

    function setFormDisabled(disabled) {
        Array.prototype.forEach.call(formFields, function (field) {
            field.disabled = disabled;
        });
    }

    function handleResumeFileChange(event) {
        hideResumeMessage();
        hideResumeProgress();

        var file = event && event.target && event.target.files ? event.target.files[0] : null;
        if (!file) {
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
        if (state.hasExistingProfile) {
            showResumeMessage("Resume file is ready. Click upload to replace your current resume.", "success");
        } else {
            showResumeMessage("Resume file is ready and will upload right after profile creation.", "success");
        }
    }

    function handleManualResumeUpload() {
        hideMessage();

        if (state.isUploadingResume || state.isLoading || state.isSubmitting) {
            return;
        }

        if (!state.hasExistingProfile) {
            showResumeMessage("Please create your profile first. The selected resume will also upload automatically after creation.", "error");
            return;
        }

        uploadSelectedResume({ expectExistingProfile: true, fromCreateFlow: false })
            .then(function () {
                return loadExistingProfile({ afterCreate: false, silentWhenMissing: false });
            })
            .catch(function (error) {
                var uploadErrorMessage = "Resume upload failed. Please try again.";
                if (error && typeof error.userMessage === "string" && error.userMessage.trim()) {
                    uploadErrorMessage = error.userMessage.trim();
                }
                showResumeMessage(uploadErrorMessage, "error");
            });
    }

    function uploadSelectedResume(options) {
        var settings = options || {};

        if (!state.selectedResumeFile) {
            var noFileError = new Error("No resume file selected.");
            noFileError.userMessage = "Please choose a resume file first.";
            return Promise.reject(noFileError);
        }

        var file = state.selectedResumeFile;
        var fileError = validateResumeFile(file);
        if (fileError) {
            var invalidFileError = new Error(fileError);
            invalidFileError.userMessage = fileError;
            return Promise.reject(invalidFileError);
        }

        setResumeUploading(true);
        updateResumeProgress(0, "Uploading...");
        showResumeMessage("Uploading " + file.name + "...", "success");

        return uploadResumeWithProgress(file)
            .then(function (result) {
                var status = result.status;
                var payload = result.payload;

                if (status === 401) {
                    handleUnauthorized();
                    var unauthorizedError = new Error("Unauthorized.");
                    unauthorizedError.userMessage = "Your session has expired. Redirecting to login...";
                    throw unauthorizedError;
                }

                if (status === 404 && settings.expectExistingProfile) {
                    var notFoundError = new Error("Applicant profile not found.");
                    notFoundError.userMessage = "Please create your profile first, then upload the resume.";
                    throw notFoundError;
                }

                if (status < 200 || status >= 300 || !payload || payload.success !== true) {
                    var serverMessage = payload && typeof payload.message === "string" && payload.message.trim()
                        ? payload.message.trim()
                        : "Resume upload failed. Please try again.";
                    var uploadError = new Error(serverMessage);
                    uploadError.userMessage = serverMessage;
                    throw uploadError;
                }

                updateResumeProgress(100, "Upload completed");
                state.resumePath = typeof payload.resumePath === "string" ? payload.resumePath : state.resumePath;
                setSelectedResumeFile(null);
                showResumeMessage("Resume uploaded successfully.", "success");

                if (!settings.fromCreateFlow) {
                    showMessage("Resume updated successfully.", "success");
                }
            })
            .finally(function () {
                setResumeUploading(false);
                refreshResumeArea();
            });
    }

    function uploadResumeWithProgress(file) {
        return new Promise(function (resolve, reject) {
            var xhr = new XMLHttpRequest();
            xhr.open("PUT", contextPath + "/applicant", true);
            xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");

            xhr.upload.onprogress = function (event) {
                if (!event || !event.lengthComputable) {
                    updateResumeProgress(0, "Uploading...");
                    return;
                }

                var percent = Math.min(100, Math.max(0, Math.round((event.loaded / event.total) * 100)));
                updateResumeProgress(percent, "Uploading...");
            };

            xhr.onerror = function () {
                var networkError = new Error("Network error.");
                networkError.userMessage = "Network error during file upload. Please try again.";
                reject(networkError);
            };

            xhr.onabort = function () {
                var abortError = new Error("Upload aborted.");
                abortError.userMessage = "Upload was interrupted. Please try again.";
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

    function validateResumeFile(file) {
        if (!file) {
            return "Please choose a resume file first.";
        }

        var lowerName = typeof file.name === "string" ? file.name.toLowerCase() : "";
        var extensionAllowed = ALLOWED_RESUME_EXTENSIONS.some(function (extension) {
            return lowerName.endsWith(extension);
        });
        if (!extensionAllowed) {
            return "Invalid file format. Please upload a PDF, DOC, or DOCX file.";
        }

        if (typeof file.size === "number" && file.size > MAX_RESUME_SIZE) {
            return "File size exceeds 10MB. Please choose a smaller file.";
        }

        return null;
    }

    function setSelectedResumeFile(file) {
        state.selectedResumeFile = file || null;
        if (resumeFileInput && !file) {
            resumeFileInput.value = "";
        }
        refreshResumeArea();
    }

    function setResumeUploading(uploading) {
        state.isUploadingResume = uploading;
        refreshResumeArea();
    }

    function refreshResumeArea() {
        if (resumeFileName) {
            if (state.selectedResumeFile) {
                resumeFileName.textContent = state.selectedResumeFile.name + " (" + formatFileSize(state.selectedResumeFile.size) + ")";
            } else {
                resumeFileName.textContent = "No file selected.";
            }
        }

        if (resumeCurrentInfo) {
            if (state.resumePath) {
                resumeCurrentInfo.textContent = "Current uploaded resume: " + state.resumePath;
                resumeCurrentInfo.classList.remove("hidden");
            } else if (state.hasExistingProfile) {
                resumeCurrentInfo.textContent = "No resume uploaded yet.";
                resumeCurrentInfo.classList.remove("hidden");
            } else {
                resumeCurrentInfo.textContent = "";
                resumeCurrentInfo.classList.add("hidden");
            }
        }

        if (resumeUploadButton) {
            if (state.isUploadingResume) {
                resumeUploadButton.disabled = true;
                resumeUploadButton.textContent = "Uploading...";
            } else if (!state.hasExistingProfile) {
                resumeUploadButton.disabled = true;
                resumeUploadButton.textContent = state.selectedResumeFile
                    ? "Will upload after profile creation"
                    : "Upload selected resume";
            } else {
                resumeUploadButton.disabled = !state.selectedResumeFile;
                resumeUploadButton.textContent = state.resumePath ? "Replace uploaded resume" : "Upload selected resume";
            }
        }
    }

    function updateResumeProgress(percent, statusText) {
        var normalizedPercent = typeof percent === "number" ? Math.min(100, Math.max(0, percent)) : 0;
        if (resumeProgressWrap) {
            resumeProgressWrap.classList.remove("hidden");
        }
        if (resumeProgressBar) {
            resumeProgressBar.style.width = normalizedPercent + "%";
        }
        if (resumeProgressText) {
            resumeProgressText.textContent = normalizedPercent + "%";
        }
        if (resumeProgressStatus) {
            resumeProgressStatus.textContent = typeof statusText === "string" && statusText.trim()
                ? statusText
                : "Uploading...";
        }
    }

    function hideResumeProgress() {
        if (resumeProgressWrap) {
            resumeProgressWrap.classList.add("hidden");
        }
        if (resumeProgressBar) {
            resumeProgressBar.style.width = "0%";
        }
        if (resumeProgressText) {
            resumeProgressText.textContent = "0%";
        }
        if (resumeProgressStatus) {
            resumeProgressStatus.textContent = "Waiting to upload";
        }
    }

    function showResumeMessage(message, type) {
        if (!resumeUploadMessage) {
            return;
        }
        resumeUploadMessage.textContent = message;
        resumeUploadMessage.classList.remove("hidden", "error", "success");
        resumeUploadMessage.classList.add(type === "success" ? "success" : "error");
    }

    function hideResumeMessage() {
        if (!resumeUploadMessage) {
            return;
        }
        resumeUploadMessage.textContent = "";
        resumeUploadMessage.classList.remove("error", "success");
        resumeUploadMessage.classList.add("hidden");
    }

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

    function validateForm() {
        var firstError = null;

        Object.keys(inputs).forEach(function (key) {
            if (!inputs[key]) {
                return;
            }

            fieldValidationState.touchedByKey[key] = true;
            var result = validateSingleField(key, { forceRequired: true });
            if (!firstError && result && result.message) {
                firstError = buildValidationError(result.message, result.field);
            }
        });

        return firstError;
    }

    function initializeRealtimeValidation() {
        Object.keys(inputs).forEach(function (key) {
            var field = inputs[key];
            if (!field) {
                return;
            }

            fieldValidationState.feedbackByKey[key] = ensureFieldFeedbackNode(key, field);
            fieldValidationState.touchedByKey[key] = false;

            field.addEventListener("blur", function () {
                if (state.hasExistingProfile || field.disabled) {
                    return;
                }
                fieldValidationState.touchedByKey[key] = true;
                validateSingleField(key, { forceRequired: true });
            });

            if (field.tagName === "SELECT") {
                field.addEventListener("change", function () {
                    if (state.hasExistingProfile || field.disabled) {
                        return;
                    }
                    validateSingleField(key, {
                        forceRequired: fieldValidationState.touchedByKey[key] === true
                    });
                });
                return;
            }

            field.addEventListener("input", function () {
                if (state.hasExistingProfile || field.disabled) {
                    return;
                }
                validateSingleField(key, {
                    forceRequired: fieldValidationState.touchedByKey[key] === true
                });
            });

            field.addEventListener("change", function () {
                if (state.hasExistingProfile || field.disabled) {
                    return;
                }
                validateSingleField(key, {
                    forceRequired: fieldValidationState.touchedByKey[key] === true
                });
            });
        });
    }

    function initializeEnterKeyBehavior() {
        form.addEventListener("keydown", function (event) {
            if (!event || event.key !== "Enter" || event.isComposing) {
                return;
            }

            var target = event.target;
            if (!target || target.form !== form) {
                return;
            }

            // Keep native Enter behavior for multiline input.
            if (target.tagName === "TEXTAREA") {
                return;
            }

            // Allow explicit submit from submit button.
            if (target === submitButton || (target.tagName === "BUTTON" && target.type === "submit")) {
                return;
            }

            // Avoid accidental submit from Enter while filling fields.
            event.preventDefault();

            if (state.hasExistingProfile || state.isLoading || state.isSubmitting || target.disabled) {
                return;
            }

            var key = getFieldKeyByElement(target);
            if (key) {
                fieldValidationState.touchedByKey[key] = true;
                var result = validateSingleField(key, { forceRequired: true });
                if (result && result.message) {
                    return;
                }
            }

            focusNextFormControl(target);
        });
    }

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

    function focusNextFormControl(current) {
        var orderedControls = [];
        orderedInputKeys.forEach(function (key) {
            if (inputs[key]) {
                orderedControls.push(inputs[key]);
            }
        });
        if (submitButton) {
            orderedControls.push(submitButton);
        }

        var currentIndex = orderedControls.indexOf(current);
        if (currentIndex < 0) {
            return;
        }

        var next;
        var i;
        for (i = currentIndex + 1; i < orderedControls.length; i += 1) {
            next = orderedControls[i];
            if (!next || next.disabled || typeof next.focus !== "function") {
                continue;
            }
            next.focus();
            return;
        }
    }

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

    function clearAllFieldValidation() {
        Object.keys(inputs).forEach(function (key) {
            setFieldValidationResult(key, "");
        });
    }

    function resetFieldTouchedState() {
        Object.keys(inputs).forEach(function (key) {
            fieldValidationState.touchedByKey[key] = false;
        });
    }

    function setFieldValidationResult(key, message) {
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
            return;
        }

        feedback.textContent = "";
        feedback.classList.remove("is-visible");
        field.classList.remove("is-invalid");
        field.removeAttribute("aria-invalid");
    }

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
        var value = typeof field.value === "string" ? field.value.trim() : "";
        var message = getFieldValidationMessage(key, value, forceRequired);
        setFieldValidationResult(key, message);

        return {
            field: field,
            message: message
        };
    }

    function getFieldValidationMessage(key, value, forceRequired) {
        var isRequired = key === "fullName" || key === "studentId" || key === "department" || key === "program";
        if (isRequired && forceRequired && !value) {
            if (key === "fullName") {
                return "Please enter your full name.";
            }
            if (key === "studentId") {
                return "Please enter your student ID.";
            }
            if (key === "department") {
                return "Please enter your department.";
            }
            return "Please select your program.";
        }

        if (!value) {
            return "";
        }

        if (key === "fullName") {
            if (value.length > 100) {
                return "Full name must be 100 characters or fewer.";
            }
            if (value.length < 2) {
                return "Full name must be at least 2 characters.";
            }
            if (!hasLetterOrCjk(value)) {
                return "Full name must include at least one letter.";
            }
            if (!/^[A-Za-z\u00C0-\u024F\u4E00-\u9FFF\s.'-]+$/.test(value)) {
                return "Full name may only include letters, spaces, apostrophes, periods, and hyphens.";
            }
            if (hasExcessiveRepeatedChars(value, 4)) {
                return "Full name contains too many repeated characters.";
            }
            return "";
        }

        if (key === "studentId") {
            if (!/^\d{10}$/.test(value)) {
                return "Student ID must be exactly 10 digits, for example 2023213039.";
            }
            if (!/^20\d{8}$/.test(value)) {
                return "Student ID should start with 20, for example 2023213051.";
            }
            var intakeYear = parseInt(value.substring(0, 4), 10);
            if (isNaN(intakeYear) || intakeYear < 2010 || intakeYear > 2099) {
                return "Student ID year appears invalid. Please check the first 4 digits.";
            }
            if (/^(\d)\1{9}$/.test(value)) {
                return "Student ID appears invalid. Please check your official 10-digit student number.";
            }
            return "";
        }

        if (key === "department") {
            if (value.length > 100) {
                return "Department must be 100 characters or fewer.";
            }
            if (value.length < 2) {
                return "Department must be at least 2 characters.";
            }
            if (!hasLetterOrCjk(value)) {
                return "Department should include letters.";
            }
            if (!/^[A-Za-z0-9\u00C0-\u024F\u4E00-\u9FFF\s&(),./'-]+$/.test(value)) {
                return "Department contains unsupported characters.";
            }
            if (hasExcessiveRepeatedChars(value, 6)) {
                return "Department contains too many repeated characters.";
            }
            return "";
        }

        if (key === "program") {
            if (["Undergraduate", "Master", "PhD"].indexOf(value) === -1) {
                return "Please select a valid program option.";
            }
            return "";
        }

        if (key === "gpa") {
            if (value.length > 20) {
                return "GPA must be 20 characters or fewer.";
            }
            if (!/^[0-9.,/\s]+$/.test(value)) {
                return "GPA may only include digits, spaces, decimal separators, and '/'.";
            }

            var normalized = value.replace(/\s+/g, "").replace(/,/g, ".");
            if (normalized.split("/").length > 2) {
                return "GPA format is invalid. Use one optional '/'.";
            }
            var parts = normalized.split("/");
            if (!/^\d{1,3}(\.\d{1,2})?$/.test(parts[0])) {
                return "GPA value supports up to 2 decimal places.";
            }

            var actual = parseFloat(parts[0]);
            if (isNaN(actual) || actual < 0) {
                return "GPA cannot be negative.";
            }

            if (parts.length === 2) {
                if (!/^\d{1,3}(\.\d{1,2})?$/.test(parts[1])) {
                    return "GPA scale supports up to 2 decimal places.";
                }
                var scale = parseFloat(parts[1]);
                if (isNaN(scale) || scale < 4 || scale > 100) {
                    return "GPA scale should be between 4 and 100.";
                }
                if (actual > scale) {
                    return "GPA value cannot be greater than the GPA scale.";
                }
            } else {
                if (actual > 4.3) {
                    return "For GPA above 4.3, please include scale (for example 85/100).";
                }
            }
            return "";
        }

        if (key === "skills") {
            if (value.length > 300) {
                return "Skills must be 300 characters or fewer.";
            }
            if (/(^[;,]|[;,]\s*[;,]|[;,]\s*$)/.test(value)) {
                return "Please remove empty skill items between separators.";
            }

            var items = value.split(/[;,]/).map(function (item) {
                return item.trim();
            }).filter(function (item) {
                return item.length > 0;
            });

            if (items.length === 0) {
                return "";
            }
            if (items.length > 12) {
                return "Please list up to 12 skills.";
            }

            var seen = {};
            var i;
            for (i = 0; i < items.length; i += 1) {
                var skill = items[i];
                if (skill.length < 2 || skill.length > 40) {
                    return "Each skill should be 2 to 40 characters.";
                }
                if (!hasLetterOrCjk(skill)) {
                    return "Each skill should include letters.";
                }
                if (!/^[A-Za-z0-9\u00C0-\u024F\u4E00-\u9FFF+#&./\-\s]+$/.test(skill)) {
                    return "Skills contain unsupported characters.";
                }
                if (hasExcessiveRepeatedChars(skill, 5)) {
                    return "A skill item has too many repeated characters.";
                }
                var normalizedSkill = skill.toLowerCase().replace(/\s+/g, " ");
                if (seen[normalizedSkill]) {
                    return "Duplicate skills found. Please keep each skill only once.";
                }
                seen[normalizedSkill] = true;
            }
            return "";
        }

        if (key === "phone") {
            if (value.length > 30) {
                return "Phone number must be 30 characters or fewer.";
            }
            if (!/^[\d+\-()./\s]+$/.test(value)) {
                return "Phone number may only include digits, spaces, and + - ( ) . /.";
            }

            var plusMatches = value.match(/\+/g);
            if (plusMatches && plusMatches.length > 1) {
                return "Phone number can contain only one '+'.";
            }
            if (value.indexOf("+") > 0) {
                return "If used, '+' must be at the beginning.";
            }
            if (!hasBalancedParentheses(value)) {
                return "Phone number parentheses are not balanced.";
            }

            var digits = value.replace(/\D/g, "");
            if (digits.length < 8 || digits.length > 15) {
                return "Phone number should contain 8 to 15 digits.";
            }
            if (/^(\d)\1+$/.test(digits)) {
                return "Phone number appears invalid. Please check repeated digits.";
            }
            if (value.charAt(0) === "+" && digits.length < 10) {
                return "International format should usually contain at least 10 digits.";
            }
            return "";
        }

        if (key === "address") {
            if (value.length > 200) {
                return "Address must be 200 characters or fewer.";
            }
            if (value.length < 5) {
                return "Address should be at least 5 characters if provided.";
            }
            if (!hasLetterOrCjk(value)) {
                return "Address should include letters.";
            }
            if (hasOnlyPunctuationAndSpace(value)) {
                return "Address cannot contain only punctuation.";
            }
            if (!/^[A-Za-z0-9\u00C0-\u024F\u4E00-\u9FFF\s#&(),./:'-]+$/.test(value)) {
                return "Address contains unsupported characters.";
            }
            if (hasExcessiveRepeatedChars(value, 8)) {
                return "Address contains too many repeated characters.";
            }
            return "";
        }

        if (key === "experience") {
            return validateLongTextField(value, "Related experience");
        }

        if (key === "motivation") {
            return validateLongTextField(value, "Motivation");
        }

        return "";
    }

    function hasLetterOrCjk(text) {
        return /[A-Za-z\u00C0-\u024F\u4E00-\u9FFF]/.test(text || "");
    }

    function hasOnlyPunctuationAndSpace(text) {
        return !/[A-Za-z0-9\u00C0-\u024F\u4E00-\u9FFF]/.test(text || "");
    }

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

    function hasExcessiveRepeatedChars(text, threshold) {
        if (!text) {
            return false;
        }
        var safeThreshold = typeof threshold === "number" ? Math.max(1, threshold) : 4;
        var repeatedPattern = new RegExp("(.)\\1{" + safeThreshold + ",}");
        return repeatedPattern.test(text);
    }

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

    function validateLongTextField(value, label) {
        if (value.length > 1200) {
            return label + " must be 1200 characters or fewer.";
        }
        if (!value) {
            return "";
        }
        if (value.length < 20) {
            return label + " should be at least 20 characters if provided.";
        }
        if (getTextContentUnits(value) < 10) {
            return label + " should contain more detail (about 10 words/characters).";
        }
        if (hasExcessiveRepeatedChars(value, 8)) {
            return label + " contains too many repeated characters.";
        }
        return "";
    }

    function buildValidationError(message, field) {
        return {
            message: message,
            field: field
        };
    }

    function request(url, options) {
        return fetch(url, options).then(function (response) {
            return response.text().then(function (bodyText) {
                return {
                    response: response,
                    payload: parseResponse(bodyText)
                };
            });
        });
    }

    function parseResponse(bodyText) {
        try {
            return JSON.parse(bodyText);
        } catch (error) {
            return parseLegacyResponse(bodyText);
        }
    }

    function parseLegacyResponse(bodyText) {
        if (typeof bodyText !== "string") {
            return null;
        }

        var successMatch = bodyText.match(/"success"\s*:\s*(true|false)/i);
        if (!successMatch) {
            return null;
        }

        var payload = {
            success: successMatch[1].toLowerCase() === "true"
        };

        var messageMatch = bodyText.match(/"message"\s*:\s*"([^"]*)"/i);
        if (messageMatch) {
            payload.message = decodeEscapedText(messageMatch[1]);
        }

        return payload;
    }

    function normalizeSkillsForSubmit(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }

        return value
            .split(/[;,]/)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            })
            .join(",");
    }

    function formatSkillsForDisplay(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }

        return value
            .split(/[;,]/)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            })
            .join(", ");
    }

    function setFieldValue(field, value) {
        if (field) {
            field.value = typeof value === "string" ? value : "";
        }
    }

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

    function showMessage(message, type) {
        messageBox.textContent = message;
        messageBox.classList.remove("hidden", "error", "success");
        messageBox.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        messageBox.textContent = "";
        messageBox.classList.remove("error", "success");
        messageBox.classList.add("hidden");
    }

    function showBanner(message) {
        bannerBox.textContent = message;
        bannerBox.classList.remove("hidden");
    }

    function hideBanner() {
        bannerBox.textContent = "";
        bannerBox.classList.add("hidden");
    }

    function handleUnauthorized() {
        showMessage("Your session has expired. Redirecting to login...", "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 1000);
    }

    function decodeEscapedText(value) {
        return value
            .replace(/\\"/g, "\"")
            .replace(/\\\\/g, "\\")
            .replace(/\\n/g, "\n")
            .replace(/\\r/g, "\r")
            .replace(/\\t/g, "\t");
    }
})();

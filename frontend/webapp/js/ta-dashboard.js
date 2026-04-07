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
            showMessage(validationError.message, "error");
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
        var fullName = inputs.fullName.value.trim();
        var studentId = inputs.studentId.value.trim();
        var department = inputs.department.value.trim();
        var program = inputs.program.value.trim();
        var gpa = inputs.gpa.value.trim();
        var skills = inputs.skills.value.trim();
        var phone = inputs.phone.value.trim();
        var address = inputs.address.value.trim();
        var experience = inputs.experience.value.trim();
        var motivation = inputs.motivation.value.trim();

        if (!fullName) {
            return buildValidationError("Please enter your full name.", inputs.fullName);
        }
        if (fullName.length > 100) {
            return buildValidationError("Full name must be 100 characters or fewer.", inputs.fullName);
        }

        if (!studentId) {
            return buildValidationError("Please enter your student ID.", inputs.studentId);
        }
        if (studentId.length > 50) {
            return buildValidationError("Student ID must be 50 characters or fewer.", inputs.studentId);
        }

        if (!department) {
            return buildValidationError("Please enter your department.", inputs.department);
        }
        if (department.length > 100) {
            return buildValidationError("Department must be 100 characters or fewer.", inputs.department);
        }

        if (!program) {
            return buildValidationError("Please select your program.", inputs.program);
        }

        if (gpa.length > 20) {
            return buildValidationError("GPA must be 20 characters or fewer.", inputs.gpa);
        }

        if (skills.length > 300) {
            return buildValidationError("Skills must be 300 characters or fewer.", inputs.skills);
        }

        if (phone.length > 30) {
            return buildValidationError("Phone number must be 30 characters or fewer.", inputs.phone);
        }

        if (address.length > 200) {
            return buildValidationError("Address must be 200 characters or fewer.", inputs.address);
        }

        if (experience.length > 1200) {
            return buildValidationError("Related experience must be 1200 characters or fewer.", inputs.experience);
        }

        if (motivation.length > 1200) {
            return buildValidationError("Motivation must be 1200 characters or fewer.", inputs.motivation);
        }

        return null;
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

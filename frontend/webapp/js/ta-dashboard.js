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
        isLoading: false
    };

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        if (state.hasExistingProfile || state.isSubmitting || state.isLoading) {
            return;
        }

        handleCreate();
    });

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

                return loadExistingProfile({ afterCreate: true, silentWhenMissing: false });
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
            bannerMessage += " Resume upload and the remaining improvements will be handled in the next planned task.";
        }

        showBanner(bannerMessage);
        submitButton.textContent = "Profile already created";
        submitButton.disabled = true;

        if (createdNow) {
            showMessage("Profile created successfully. Your saved information is now displayed below.", "success");
        } else {
            hideMessage();
        }
    }

    function enableCreateMode() {
        state.hasExistingProfile = false;
        setFormDisabled(false);
        form.classList.remove("is-readonly");
        hideBanner();
        refreshSubmitButton();
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

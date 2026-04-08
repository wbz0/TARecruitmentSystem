(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentRole = typeof window.APP_CURRENT_ROLE === "string" ? window.APP_CURRENT_ROLE.trim().toUpperCase() : "";

    var titleNode = document.getElementById("job-title");
    var courseNode = document.getElementById("job-course");
    var statusNode = document.getElementById("job-status");
    var moNameNode = document.getElementById("job-mo-name");
    var positionsNode = document.getElementById("job-positions");
    var workloadNode = document.getElementById("job-workload");
    var salaryNode = document.getElementById("job-salary");
    var deadlineNode = document.getElementById("job-deadline");
    var descriptionNode = document.getElementById("job-description");
    var skillsNode = document.getElementById("job-skills");
    var detailMessageNode = document.getElementById("detail-message");

    var applyForm = document.getElementById("apply-form");
    var coverLetterInput = document.getElementById("cover-letter");
    var applySubmitButton = document.getElementById("apply-submit-btn");
    var applyStatusBanner = document.getElementById("apply-status-banner");

    if (!titleNode || !applyForm || !applySubmitButton) {
        return;
    }

    var state = {
        jobId: "",
        loadingJob: false,
        submitting: false,
        loadedJob: null,
        hasApplied: false
    };

    applyForm.addEventListener("submit", function (event) {
        event.preventDefault();
        submitApplication();
    });

    initialize();

    function initialize() {
        state.jobId = getJobIdFromLocation();
        if (!state.jobId) {
            showDetailMessage("Missing job ID in URL. Please return to the list page and try again.", "error");
            setApplyDisabled(true, "Job ID is missing.");
            return;
        }

        if (currentRole !== "TA") {
            setApplyDisabled(true, "Only TA accounts can submit applications.");
            showApplyStatus("Current account cannot submit applications on this page.", "error");
        }

        loadJobDetail();
    }

    function loadJobDetail() {
        if (state.loadingJob) {
            return;
        }

        setJobLoading(true);
        hideDetailMessage();

        request(contextPath + "/jobs?id=" + encodeURIComponent(state.jobId), {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (response.status === 404) {
                    showDetailMessage("Job not found. It may have been removed.", "error");
                    setApplyDisabled(true, "This job is no longer available.");
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = "Unable to load job details right now.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showDetailMessage(errorMessage, "error");
                    setApplyDisabled(true, "Please retry later.");
                    return;
                }

                state.loadedJob = payload;
                renderJob(payload);
                return refreshMyApplicationStatus();
            })
            .catch(function () {
                showDetailMessage("Network error. Please try again.", "error");
                setApplyDisabled(true, "Please retry later.");
            })
            .finally(function () {
                setJobLoading(false);
            });
    }

    function renderJob(job) {
        var status = safeText(job.status, "OPEN").toUpperCase();
        var courseText = safeText(job.courseCode, "-");
        if (job.courseName) {
            courseText += " · " + safeText(job.courseName);
        }

        titleNode.textContent = safeText(job.title, "Untitled position");
        courseNode.textContent = courseText;
        moNameNode.textContent = safeText(job.moName, "-");
        positionsNode.textContent = safeText(String(job.positions || 0), "-");
        workloadNode.textContent = safeText(job.workload, "-");
        salaryNode.textContent = safeText(job.salary, "-");
        deadlineNode.textContent = formatDateTime(job.deadline);
        descriptionNode.textContent = safeText(job.description, "No description provided.");

        statusNode.textContent = status;
        statusNode.className = "status-pill status-" + status.toLowerCase();

        renderSkills(job.requiredSkills);

        if (currentRole === "TA" && status !== "OPEN") {
            setApplyDisabled(true, "This job is not accepting new applications.");
            showApplyStatus("This position is currently " + status + ". New applications are disabled.", "error");
        }
    }

    function renderSkills(skillsValue) {
        skillsNode.innerHTML = "";
        var skills = normalizeSkills(skillsValue);
        if (skills.length === 0) {
            var empty = document.createElement("span");
            empty.className = "skill-chip muted";
            empty.textContent = "No specific skills listed.";
            skillsNode.appendChild(empty);
            return;
        }

        skills.forEach(function (skill) {
            var chip = document.createElement("span");
            chip.className = "skill-chip";
            chip.textContent = skill;
            skillsNode.appendChild(chip);
        });
    }

    function refreshMyApplicationStatus() {
        if (currentRole !== "TA") {
            return Promise.resolve();
        }

        return request(contextPath + "/apply?jobId=" + encodeURIComponent(state.jobId), {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    return;
                }

                var applications = Array.isArray(payload.applications) ? payload.applications : [];
                if (applications.length === 0) {
                    state.hasApplied = false;
                    if (state.loadedJob && safeText(state.loadedJob.status, "OPEN").toUpperCase() === "OPEN") {
                        setApplyDisabled(false);
                    }
                    hideApplyStatus();
                    return;
                }

                var application = applications[0];
                state.hasApplied = true;
                setApplyDisabled(true, "You have already applied for this job.");
                showApplyStatus("Application status: " + safeText(application.status, "PENDING") + ".", "success");
            });
    }

    function submitApplication() {
        if (state.submitting || currentRole !== "TA") {
            return;
        }

        if (!state.jobId) {
            showApplyStatus("Cannot submit because job ID is missing.", "error");
            return;
        }

        if (state.hasApplied) {
            showApplyStatus("You have already applied for this job.", "error");
            return;
        }

        var coverLetter = coverLetterInput.value.trim();
        if (coverLetter && containsControlChars(coverLetter)) {
            showApplyStatus("Cover letter contains unsupported control characters.", "error");
            coverLetterInput.focus();
            return;
        }
        if (coverLetter && containsDangerousMarkup(coverLetter)) {
            showApplyStatus("Cover letter contains unsupported markup.", "error");
            coverLetterInput.focus();
            return;
        }
        if (coverLetter.length > 2000) {
            showApplyStatus("Cover letter must be 2000 characters or fewer.", "error");
            coverLetterInput.focus();
            return;
        }

        setApplySubmitting(true);

        var formData = new URLSearchParams();
        formData.set("jobId", state.jobId);
        formData.set("coverLetter", coverLetter);

        request(contextPath + "/apply", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = "Failed to submit application. Please try again.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showApplyStatus(errorMessage, "error");
                    return;
                }

                showApplyStatus("Application submitted successfully. Redirecting to application status...", "success");
                coverLetterInput.value = "";
                state.hasApplied = true;
                setApplyDisabled(true, "Application has been submitted.");
                window.setTimeout(function () {
                    window.location.href = contextPath + "/jsp/ta/application-status.jsp";
                }, 900);
                return;
            })
            .catch(function () {
                showApplyStatus("Network error while submitting application.", "error");
            })
            .finally(function () {
                setApplySubmitting(false);
            });
    }

    function setJobLoading(loading) {
        state.loadingJob = loading;
        if (loading) {
            titleNode.textContent = "Loading job details...";
            courseNode.textContent = "-";
        }
    }

    function setApplySubmitting(submitting) {
        state.submitting = submitting;
        if (submitting) {
            applySubmitButton.disabled = true;
            applySubmitButton.textContent = "Submitting...";
            return;
        }

        if (!applySubmitButton.disabled) {
            applySubmitButton.textContent = "Apply for this job";
        } else if (applySubmitButton.dataset.reason) {
            applySubmitButton.textContent = applySubmitButton.dataset.reason;
        }
    }

    function setApplyDisabled(disabled, reasonText) {
        applySubmitButton.disabled = disabled;
        if (disabled) {
            applySubmitButton.dataset.reason = reasonText || "Application unavailable";
            applySubmitButton.textContent = applySubmitButton.dataset.reason;
            coverLetterInput.disabled = true;
            return;
        }

        delete applySubmitButton.dataset.reason;
        applySubmitButton.textContent = "Apply for this job";
        coverLetterInput.disabled = false;
    }

    function showDetailMessage(message, type) {
        detailMessageNode.textContent = message;
        detailMessageNode.classList.remove("hidden", "error", "success");
        detailMessageNode.classList.add(type === "success" ? "success" : "error");
    }

    function hideDetailMessage() {
        detailMessageNode.textContent = "";
        detailMessageNode.classList.remove("error", "success");
        detailMessageNode.classList.add("hidden");
    }

    function showApplyStatus(message, type) {
        applyStatusBanner.textContent = message;
        applyStatusBanner.classList.remove("hidden", "error", "success");
        applyStatusBanner.classList.add(type === "success" ? "success" : "error");
    }

    function hideApplyStatus() {
        applyStatusBanner.textContent = "";
        applyStatusBanner.classList.remove("error", "success");
        applyStatusBanner.classList.add("hidden");
    }

    function handleUnauthorized() {
        showDetailMessage("Your session has expired. Redirecting to login...", "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
    }

    function request(url, options) {
        return fetch(url, options).then(function (response) {
            return response.text().then(function (text) {
                return {
                    response: response,
                    payload: parseJson(text)
                };
            });
        });
    }

    function parseJson(text) {
        try {
            return JSON.parse(text);
        } catch (error) {
            return parseLegacyResponse(text);
        }
    }

    function parseLegacyResponse(text) {
        if (typeof text !== "string") {
            return null;
        }

        var successMatch = text.match(/"success"\s*:\s*(true|false)/i);
        if (!successMatch) {
            return null;
        }

        var payload = {
            success: successMatch[1].toLowerCase() === "true"
        };

        var messageMatch = text.match(/"message"\s*:\s*"([^"]*)"/i);
        if (messageMatch) {
            payload.message = decodeEscapedText(messageMatch[1]);
        }

        return payload;
    }

    function decodeEscapedText(value) {
        return value
            .replace(/\\"/g, "\"")
            .replace(/\\\\/g, "\\")
            .replace(/\\n/g, "\n")
            .replace(/\\r/g, "\r")
            .replace(/\\t/g, "\t");
    }

    function normalizeSkills(rawSkills) {
        if (typeof rawSkills !== "string" || !rawSkills.trim()) {
            return [];
        }
        return rawSkills
            .split(/[;,]/)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            });
    }

    function formatDateTime(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "-";
        }
        var date = new Date(value);
        if (isNaN(date.getTime())) {
            return value;
        }
        return date.getFullYear() + "-" +
            pad2(date.getMonth() + 1) + "-" +
            pad2(date.getDate()) + " " +
            pad2(date.getHours()) + ":" +
            pad2(date.getMinutes());
    }

    function pad2(value) {
        return value < 10 ? "0" + value : String(value);
    }

    function safeText(value, fallback) {
        if (typeof value === "string" && value.trim()) {
            return value.trim();
        }
        if (typeof value === "number") {
            return String(value);
        }
        return typeof fallback === "string" ? fallback : "";
    }

    function containsControlChars(value) {
        return /[\u0000-\u001F\u007F]/.test(value || "");
    }

    function containsDangerousMarkup(value) {
        if (typeof value !== "string" || !value) {
            return false;
        }
        return /<[^>]*>/.test(value) || /javascript:/i.test(value) || /on\w+\s*=/.test(value);
    }

    function getJobIdFromLocation() {
        try {
            var params = new URLSearchParams(window.location.search || "");
            return params.get("id") ? params.get("id").trim() : "";
        } catch (error) {
            return "";
        }
    }
})();

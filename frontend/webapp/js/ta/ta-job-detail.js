/*
 * TA 职位详情页脚本，对应 /jsp/ta/job-detail.jsp。
 *
 * 读取 /api/jobs/{jobId} 展示岗位，并提交 /api/applications 创建申请。
 */
(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentRole = typeof window.APP_CURRENT_ROLE === "string" ? window.APP_CURRENT_ROLE.trim().toUpperCase() : "";

    function t(key, fallback) {
        if (window.AppI18n && typeof window.AppI18n.t === "function") {
            return window.AppI18n.t(key, fallback);
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
        return fallbackKey ? t(fallbackKey, fallbackText) : (fallbackText || "");
    }

    var titleNode = document.getElementById("job-title");
    var courseNode = document.getElementById("job-course");
    var statusNode = document.getElementById("job-status");
    var positionsNode = document.getElementById("job-positions");
    var workloadNode = document.getElementById("job-workload");
    var salaryNode = document.getElementById("job-salary");
    var deadlineNode = document.getElementById("job-deadline");
    var descriptionNode = document.getElementById("job-description");
    var skillsNode = document.getElementById("job-skills");
    var detailMessageNode = document.getElementById("detail-message");
    var applyOpenButton = document.getElementById("apply-open-btn");
    var applyInlineStatus = document.getElementById("apply-inline-status");

    var applyForm = document.getElementById("apply-form");
    var coverLetterInput = document.getElementById("cover-letter");
    var applySubmitButton = document.getElementById("apply-submit-btn");
    var applyStatusBanner = document.getElementById("apply-status-banner");
    var applyModal = document.getElementById("apply-modal");
    var applyModalDialog = document.getElementById("apply-modal-dialog");
    var applyModalClose = document.getElementById("apply-modal-close");

    if (
        !titleNode ||
        !applyOpenButton ||
        !applyInlineStatus ||
        !applyForm ||
        !coverLetterInput ||
        !applySubmitButton ||
        !applyStatusBanner ||
        !applyModal ||
        !applyModalDialog
    ) {
        return;
    }

    var state = {
        // job/currentApplication 是页面主状态。
        jobId: "",
        loadingJob: false,
        submitting: false,
        loadedJob: null,
        hasApplied: false,
        applicationStatus: "",
        applyDisabled: true,
        applyDisabledMessage: "",
        applyDisabledTone: "error",
        lastFocus: null
    };

    applyForm.addEventListener("submit", function (event) {
        event.preventDefault();
        submitApplication();
    });

    applyOpenButton.addEventListener("click", function () {
        openApplyModal();
    });

    if (applyModalClose) {
        applyModalClose.addEventListener("click", function () {
            closeApplyModal();
        });
    }

    applyModal.addEventListener("click", function (event) {
        if (event.target && event.target.getAttribute("data-close-modal") !== null) {
            closeApplyModal();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape") {
            return;
        }
        if (isModalVisible(applyModal)) {
            closeApplyModal();
        }
    });

    document.addEventListener("app:locale-changed", function () {
        rerenderCurrentView();
    });

    initialize();

    function initialize() {
        syncApplyControls();
        state.jobId = getJobIdFromLocation();
        if (!state.jobId) {
            showDetailMessage(t("portal.taJobDetail.missingId", "Missing job ID. Please return to the list and try again."), "error");
            setApplyDisabled(true, t("portal.dynamic.jobIdMissing", "Job ID is missing."), "error");
            return;
        }

        if (currentRole !== "TA") {
            setApplyDisabled(true, t("portal.dynamic.currentAccountCannotSubmit", "Current account cannot submit applications on this page."), "error");
        }

        loadJobDetail();
    }

    function rerenderCurrentView() {
        syncApplyControls();
        if (!state.loadedJob) {
            return;
        }
        renderJob(state.loadedJob);
        if (state.hasApplied) {
            setApplyDisabled(true, applicationStatusMessage(state.applicationStatus), "success");
        }
    }

    function loadJobDetail() {
        if (state.loadingJob) {
            return;
        }

        setJobLoading(true);
        hideDetailMessage();

        request(window.TARecruitment.routes.jobs.detail(state.jobId), {
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
                    showDetailMessage(t("portal.dynamic.jobNotFound", "Job not found. It may have been removed."), "error");
                    setApplyDisabled(true, t("portal.dynamic.jobNoLongerAvailable", "This job is no longer available."), "error");
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = t("portal.dynamic.unableLoadJobDetailsNow", "Unable to load job details right now.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.unableLoadJobDetailsNow", errorMessage);
                    }
                    showDetailMessage(errorMessage, "error");
                    setApplyDisabled(true);
                    return;
                }

                var job = getPayloadDataObject(payload);
                if (!job) {
                    showDetailMessage(t("portal.dynamic.unableLoadJobDetailsNow", "Unable to load job details right now."), "error");
                    setApplyDisabled(true);
                    return;
                }

                state.loadedJob = job;
                renderJob(job);
                return refreshMyApplicationStatus();
            })
            .catch(function () {
                showDetailMessage(t("portal.dynamic.networkErrorMoment", "Network error. Please try again in a moment."), "error");
                setApplyDisabled(true);
            })
            .finally(function () {
                setJobLoading(false);
            });
    }

    function renderJob(job) {
        var status = safeText(job.status, "OPEN").toUpperCase();
        var courseParts = [];
        var courseCode = safeText(job.courseCode, "");
        var courseName = safeText(job.courseName, "");
        var moName = safeText(job.moName, "");

        if (courseCode) {
            courseParts.push(courseCode);
        }
        if (courseName) {
            courseParts.push(courseName);
        }
        if (moName) {
            courseParts.push(t("portal.taJobDetail.moduleOrganizer", "Module organizer") + " " + moName);
        }

        titleNode.textContent = safeText(job.title, t("portal.dynamic.untitledPosition", "Untitled position"));
        courseNode.textContent = courseParts.length ? courseParts.join(" · ") : "-";
        positionsNode.textContent = safeText(String(job.positions || 0), "-");
        workloadNode.textContent = safeText(job.workload, "-");
        salaryNode.textContent = safeText(job.salary, "-");
        deadlineNode.textContent = formatDateTime(job.deadline);
        descriptionNode.textContent = safeText(job.description, t("portal.taApplicationDetail.noDescription", "No description provided."));

        statusNode.textContent = statusLabel(status);
        statusNode.className = "status-pill status-" + status.toLowerCase();

        renderSkills(job.requiredSkills);

        if (currentRole !== "TA") {
            setApplyDisabled(true, t("portal.dynamic.currentAccountCannotSubmit", "Current account cannot submit applications on this page."), "error");
            return;
        }

        if (currentRole === "TA" && status !== "OPEN") {
            setApplyDisabled(true, t("portal.dynamic.jobNotAccepting", "This job is not accepting new applications."), "error");
            return;
        }

        if (!state.hasApplied) {
            setApplyDisabled(false);
        }
    }

    function renderSkills(skillsValue) {
        skillsNode.innerHTML = "";
        var skills = normalizeSkills(skillsValue);
        if (skills.length === 0) {
            var empty = document.createElement("span");
            empty.className = "skill-chip muted";
            empty.textContent = t("portal.dynamic.noSpecificSkills", "No specific skills listed.");
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

        return request(window.TARecruitment.routes.applications.list({ jobId: state.jobId }), {
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

                var applications = getPayloadDataArray(payload, "applications");
                if (applications.length === 0) {
                    state.hasApplied = false;
                    state.applicationStatus = "";
                    if (state.loadedJob && safeText(state.loadedJob.status, "OPEN").toUpperCase() === "OPEN") {
                        setApplyDisabled(false);
                    }
                    hideApplyStatus();
                    return;
                }

                var application = applications[0];
                state.hasApplied = true;
                state.applicationStatus = safeText(application.status, "PENDING").toUpperCase();
                hideApplyStatus();
                setApplyDisabled(true, applicationStatusMessage(state.applicationStatus), "success");
            });
    }

    function submitApplication() {
        if (state.submitting || currentRole !== "TA") {
            return;
        }

        if (state.applyDisabled) {
            showApplyStatus(state.applyDisabledMessage || t("portal.dynamic.applicationUnavailable", "Application unavailable"), state.applyDisabledTone);
            return;
        }

        if (!state.jobId) {
            showApplyStatus(t("portal.dynamic.cannotSubmitMissingJobId", "Cannot submit because job ID is missing."), "error");
            return;
        }

        if (state.hasApplied) {
            showApplyStatus(t("portal.dynamic.alreadyApplied", "You have already applied for this job."), "error");
            return;
        }

        var coverLetter = coverLetterInput.value.trim();
        if (coverLetter && containsControlChars(coverLetter)) {
            showApplyStatus(t("portal.dynamic.coverLetterControlChars", "Cover letter contains unsupported control characters."), "error");
            coverLetterInput.focus();
            return;
        }
        if (coverLetter && containsDangerousMarkup(coverLetter)) {
            showApplyStatus(t("portal.dynamic.coverLetterUnsupportedMarkup", "Cover letter contains unsupported markup."), "error");
            coverLetterInput.focus();
            return;
        }
        if (coverLetter.length > 2000) {
            showApplyStatus(t("portal.dynamic.coverLetterTooLong", "Cover letter must be 2000 characters or fewer."), "error");
            coverLetterInput.focus();
            return;
        }

        hideApplyStatus();
        setApplySubmitting(true);

        var formData = new URLSearchParams();
        formData.set("jobId", state.jobId);
        formData.set("coverLetter", coverLetter);

        request(window.TARecruitment.routes.applications.create(), {
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
                    var errorMessage = t("portal.dynamic.failedSubmitApplication", "Failed to submit application. Please try again.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.failedSubmitApplication", errorMessage);
                    }
                    showApplyStatus(errorMessage, "error");
                    return;
                }

                state.hasApplied = true;
                state.applicationStatus = "PENDING";
                setApplyDisabled(true, applicationStatusMessage(state.applicationStatus), "success");
                showApplyStatus(t("portal.dynamic.applicationSubmittedRedirect", "Application submitted successfully. Redirecting to application status..."), "success");
                coverLetterInput.value = "";
                window.setTimeout(function () {
                    window.location.href = contextPath + "/jsp/ta/application-status.jsp";
                }, 900);
                return;
            })
            .catch(function () {
                showApplyStatus(t("portal.dynamic.networkErrorSubmitApplication", "Network error while submitting application."), "error");
            })
            .finally(function () {
                setApplySubmitting(false);
            });
    }

    function setJobLoading(loading) {
        state.loadingJob = loading;
        if (loading) {
            titleNode.textContent = t("portal.taJobDetail.loadingDetails", "Loading job details...");
            courseNode.textContent = "-";
        }
        syncApplyControls();
    }

    function setApplySubmitting(submitting) {
        state.submitting = submitting;
        syncApplyControls();
    }

    function setApplyDisabled(disabled, reasonText, tone) {
        state.applyDisabled = !!disabled;
        state.applyDisabledMessage = typeof reasonText === "string" ? reasonText : "";
        state.applyDisabledTone = tone === "success" ? "success" : "error";
        syncApplyControls();
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

    function showApplyInlineStatus(message, type) {
        applyInlineStatus.textContent = message;
        applyInlineStatus.classList.remove("hidden", "error", "success");
        applyInlineStatus.classList.add(type === "success" ? "success" : "error");
    }

    function hideApplyInlineStatus() {
        applyInlineStatus.textContent = "";
        applyInlineStatus.classList.remove("error", "success");
        applyInlineStatus.classList.add("hidden");
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

    function openApplyModal() {
        if (applyOpenButton.disabled || state.applyDisabled) {
            return;
        }
        state.lastFocus = document.activeElement;
        hideApplyStatus();
        applyModal.classList.remove("hidden");
        applyModal.setAttribute("aria-hidden", "false");
        applyOpenButton.setAttribute("aria-expanded", "true");
        syncBodyModalState();
        if (!coverLetterInput.disabled) {
            coverLetterInput.focus();
        } else {
            applyModalDialog.focus();
        }
    }

    function closeApplyModal() {
        if (applyModal.classList.contains("hidden")) {
            return;
        }
        applyModal.classList.add("hidden");
        applyModal.setAttribute("aria-hidden", "true");
        applyOpenButton.setAttribute("aria-expanded", "false");
        syncBodyModalState();
        if (state.lastFocus && typeof state.lastFocus.focus === "function") {
            state.lastFocus.focus();
        } else {
            applyOpenButton.focus();
        }
    }

    function syncBodyModalState() {
        if (!document.body) {
            return;
        }
        if (isModalVisible(applyModal)) {
            document.body.classList.add("apply-modal-open");
        } else {
            document.body.classList.remove("apply-modal-open");
        }
    }

    function isModalVisible(modalNode) {
        return !!(modalNode && !modalNode.classList.contains("hidden"));
    }

    function handleUnauthorized() {
        showDetailMessage(t("portal.dynamic.sessionExpiredRedirect", "Session expired. Redirecting to login..."), "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
    }

    function shouldShowStoppedApplyButton() {
        return currentRole === "TA" &&
            !state.hasApplied &&
            !!state.loadedJob &&
            safeText(state.loadedJob.status, "OPEN").toUpperCase() !== "OPEN";
    }

    function syncApplyControls() {
        var controlsDisabled = state.applyDisabled || state.loadingJob;
        var hideTrigger = false;
        var showInlineStatus = false;
        var inlineMessage = "";
        var inlineTone = state.applyDisabledTone;
        var buttonLabel = t("portal.dynamic.applyNow", "Apply now");

        coverLetterInput.disabled = controlsDisabled || state.submitting;
        applySubmitButton.textContent = state.submitting
            ? t("portal.dynamic.submitting", "Submitting...")
            : t("portal.taJobDetail.applyNow", "Apply for this job");
        applySubmitButton.disabled = controlsDisabled || state.submitting;
        coverLetterInput.placeholder = t(
            "portal.taJobDetail.coverLetterPlaceholder",
            "Briefly explain your relevant experience, strengths, and availability."
        );

        if (state.hasApplied) {
            hideTrigger = true;
            showInlineStatus = true;
            inlineMessage = applicationStatusMessage(state.applicationStatus);
            inlineTone = "success";
        } else if (shouldShowStoppedApplyButton()) {
            buttonLabel = t("portal.dynamic.applicationStopped", "Applications closed");
        } else if (state.applyDisabledMessage) {
            showInlineStatus = true;
            inlineMessage = state.applyDisabledMessage;
            hideTrigger = !state.loadedJob || currentRole !== "TA";
        }

        applyOpenButton.hidden = hideTrigger;
        applyOpenButton.disabled = hideTrigger ? true : controlsDisabled;
        applyOpenButton.textContent = buttonLabel;

        if (hideTrigger) {
            applyOpenButton.setAttribute("aria-expanded", "false");
        }

        if (showInlineStatus) {
            showApplyInlineStatus(inlineMessage, inlineTone);
        } else {
            hideApplyInlineStatus();
        }
    }

    function applicationStatusMessage(status) {
        return statusLabel(status || "PENDING");
    }

    function statusLabel(status) {
        var value = safeText(status, "").toUpperCase();
        if (value === "OPEN") {
            return t("portal.common.openUpper", "OPEN");
        }
        if (value === "CLOSED") {
            return t("portal.common.closed", "Closed");
        }
        if (value === "FILLED") {
            return t("portal.common.filled", "Filled");
        }
        if (value === "PENDING") {
            return t("portal.common.pending", "Pending");
        }
        if (value === "ACCEPTED") {
            return t("portal.common.accepted", "Accepted");
        }
        if (value === "REJECTED") {
            return t("portal.common.rejected", "Rejected");
        }
        if (value === "WITHDRAWN") {
            return t("portal.common.withdrawn", "Withdrawn");
        }
        return value || "-";
    }

    function request(url, options) {
        if (window.TARecruitment && window.TARecruitment.api) {
            return window.TARecruitment.api.request(url, options, { parser: parseJson });
        }
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
        return JSON.parse(text);
    }

    function getPayloadDataArray(payload, key) {
        if (!payload || typeof payload !== "object") {
            return [];
        }
        if (payload.data && Array.isArray(payload.data[key])) {
            return payload.data[key];
        }
        if (Array.isArray(payload[key])) {
            return payload[key];
        }
        return [];
    }

    function getPayloadDataObject(payload) {
        if (!payload || typeof payload !== "object") {
            return null;
        }
        if (payload.data && typeof payload.data === "object" && !Array.isArray(payload.data)) {
            return payload.data;
        }
        return null;
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

/*
 * TA application detail page script, corresponds to /jsp/ta/application-detail.jsp.
 *
 * Reads application details, applicant snapshot, and job details from the URL application id.
 * Only PENDING status shows the withdraw button; withdrawal is done via /api/applications/{applicationId}/transition.
 */
(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";

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

    var teaserTrigger = document.getElementById("job-teaser-trigger");
    var messageNode = document.getElementById("detail-message");
    var withdrawButton = document.getElementById("withdraw-application-btn");

    var state = {
        // application is the primary record; applicant/job are for supplementary profile hints and job summary on the page.
        applicationId: "",
        application: null,
        applicant: null,
        job: null
    };

    if (!teaserTrigger) {
        return;
    }

    document.addEventListener("app:locale-changed", function () {
        if (state.application) {
            renderAll();
        }
    });

    initialize();

    function initialize() {
        state.applicationId = getApplicationIdFromLocation();
        if (!state.applicationId) {
            showMessage(t("portal.taApplicationDetail.missingId", "Missing application ID. Return to the list and try again."), "error");
            disableJobTeaser();
            return;
        }

        if (withdrawButton) {
            withdrawButton.addEventListener("click", handleWithdrawClick);
        }

        loadAll()
            .then(function () {
                renderAll();
            })
            .catch(function () {
                /* errors handled in loadAll */
            });

    }

    function getApplicationIdFromLocation() {
        try {
            var params = new URLSearchParams(window.location.search || "");
            var id = params.get("id");
            return id ? id.trim() : "";
        } catch (e) {
            return "";
        }
    }

    function loadAll() {
        return request(window.TARecruitment.routes.applications.detail(state.applicationId), {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;
                if (response.status === 401) {
                    handleUnauthorized();
                    throw new Error("401");
                }
                if (!response.ok || !payload || payload.success !== true) {
                    var msg = t("portal.taApplicationDetail.loadAppFailed", "Unable to load application.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        msg = localizeServerMessage(payload.message, "portal.taApplicationDetail.loadAppFailed", msg);
                    }
                    showMessage(msg, "error");
                    throw new Error("app");
                }
                state.application = getPayloadDataObject(payload);
                if (!state.application) {
                    showMessage(t("portal.taApplicationDetail.loadAppFailed", "Unable to load application."), "error");
                    throw new Error("app");
                }
                var jobId = safeText(state.application.jobId, "");
                return Promise.all([
                    request(window.TARecruitment.routes.applications.applicant(state.applicationId), {
                        method: "GET",
                        headers: { "X-Requested-With": "XMLHttpRequest" }
                    }),
                    jobId
                        ? request(window.TARecruitment.routes.jobs.detail(jobId), {
                              method: "GET",
                              headers: { "X-Requested-With": "XMLHttpRequest" }
                          })
                        : Promise.resolve({ response: { ok: false }, payload: null })
                ]);
            })
            .then(function (results) {
                var detailResult = results[0];
                var jobResult = results[1];

                if (detailResult.response.ok && detailResult.payload && detailResult.payload.success === true) {
                    state.applicant = getPayloadDataObject(detailResult.payload);
                }

                if (jobResult.response.ok && jobResult.payload && jobResult.payload.success === true) {
                    state.job = getPayloadDataObject(jobResult.payload);
                }
            })
            .catch(function (err) {
                if (err && err.message === "401") {
                    return;
                }
                if (!messageNode || messageNode.classList.contains("error")) {
                    return;
                }
                showMessage(t("portal.taApplicationDetail.networkError", "Network error. Please try again."), "error");
            });
    }

    function renderAll() {
        hideMessage();
        var app = state.application;
        var job = state.job;
        var detail = state.applicant;

        var title = safeText(app.jobTitle, t("portal.taApplicationDetail.untitled", "Untitled position"));
        var courseCode = safeText(app.courseCode, "");

        var titleEl = document.getElementById("app-detail-title");
        var submittedEl = document.getElementById("app-detail-submitted");
        var badgeEl = document.getElementById("app-course-badge");
        var chipEl = document.getElementById("app-status-chip");

        if (titleEl) {
            titleEl.textContent = title;
        }
        if (badgeEl) {
            badgeEl.textContent = courseBadgeText(courseCode, title);
        }
        if (submittedEl) {
            submittedEl.textContent =
                t("portal.taApplicationDetail.submittedPrefix", "Submitted on") +
                " " +
                formatDisplayDate(app.appliedAt);
        }

        if (chipEl) {
            var st = safeText(app.status, "PENDING").toUpperCase();
            var sc = statusToChipClass(st);
            chipEl.className = "application-status-chip status-" + sc;
            chipEl.innerHTML =
                getStatusIconMarkup(sc) +
                '<span class="application-status-text">' +
                escapeHtml(statusLabel(st)) +
                "</span>";
        }

        renderCoverLetter(app, detail);
        renderProfileGuidance(detail);
        renderJobTeaser(job);
        renderWithdrawAction(app);
    }

    function renderWithdrawAction(app) {
        if (!withdrawButton) {
            return;
        }
        var status = safeText(app && app.status, "PENDING").toUpperCase();
        var canWithdraw = status === "PENDING";
        withdrawButton.hidden = !canWithdraw;
        withdrawButton.disabled = false;
        withdrawButton.textContent = t("portal.taApplicationDetail.withdrawAction", "Withdraw application");
        withdrawButton.setAttribute("aria-label", t("portal.taApplicationDetail.withdrawAction", "Withdraw application"));
    }

    function handleWithdrawClick() {
        if (!state.applicationId || !state.application) {
            return;
        }
        var status = safeText(state.application.status, "PENDING").toUpperCase();
        if (status !== "PENDING") {
            showMessage(t("portal.taApplicationDetail.withdrawUnavailable", "This application cannot be withdrawn."), "error");
            renderWithdrawAction(state.application);
            return;
        }

        var confirmed = window.confirm(t(
            "portal.taApplicationDetail.withdrawConfirm",
            "Withdraw this application? The MO will see it as withdrawn."
        ));
        if (!confirmed) {
            return;
        }

        setWithdrawSubmitting(true);
        var formData = new URLSearchParams();
        formData.set("action", "withdraw");
        request(window.TARecruitment.routes.applications.transition(state.applicationId), {
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
                    var msg = t("portal.taApplicationDetail.withdrawFailed", "Unable to withdraw this application.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        msg = localizeServerMessage(payload.message, "portal.taApplicationDetail.withdrawFailed", msg);
                    }
                    showMessage(msg, "error");
                    return;
                }

                var updated = getPayloadDataObject(payload);
                if (updated) {
                    state.application = updated;
                } else {
                    state.application.status = "WITHDRAWN";
                }
                renderAll();
                showMessage(
                    localizeServerMessage(
                        payload.message,
                        "portal.taApplicationDetail.withdrawSuccess",
                        "Application withdrawn successfully."
                    ),
                    "success"
                );
            })
            .catch(function () {
                showMessage(t("portal.taApplicationDetail.withdrawNetworkError", "Network error while withdrawing application."), "error");
            })
            .finally(function () {
                setWithdrawSubmitting(false);
                if (state.application) {
                    renderWithdrawAction(state.application);
                }
            });
    }

    function setWithdrawSubmitting(isSubmitting) {
        if (!withdrawButton) {
            return;
        }
        withdrawButton.disabled = !!isSubmitting;
        withdrawButton.textContent = isSubmitting
            ? t("portal.taApplicationDetail.withdrawing", "Withdrawing...")
            : t("portal.taApplicationDetail.withdrawAction", "Withdraw application");
    }

    function courseBadgeText(courseCode, title) {
        var c = safeText(courseCode, "");
        if (c.length >= 2) {
            return c.substring(0, 2).toUpperCase();
        }
        var t0 = safeText(title, "TA");
        return t0.substring(0, 2).toUpperCase();
    }

    function statusToChipClass(status) {
        if (status === "PENDING") {
            return "pending";
        }
        if (status === "ACCEPTED") {
            return "accepted";
        }
        if (status === "REJECTED") {
            return "rejected";
        }
        if (status === "WITHDRAWN") {
            return "withdrawn";
        }
        return "unknown";
    }

    function statusLabel(status) {
        if (status === "PENDING") {
            return t("portal.common.pending", "Pending");
        }
        if (status === "ACCEPTED") {
            return t("portal.common.accepted", "Accepted");
        }
        if (status === "REJECTED") {
            return t("portal.common.rejected", "Rejected");
        }
        if (status === "WITHDRAWN") {
            return t("portal.common.withdrawn", "Withdrawn");
        }
        return status;
    }

    function getStatusIconMarkup(statusClass) {
        if (statusClass === "pending") {
            return (
                '<span class="application-status-icon" aria-hidden="true">' +
                '<svg viewBox="0 0 20 20" focusable="false" aria-hidden="true">' +
                "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                "<path d=\"M10 6.25v4.1l2.7 1.7\"></path>" +
                "</svg></span>"
            );
        }
        if (statusClass === "accepted") {
            return (
                '<span class="application-status-icon" aria-hidden="true">' +
                '<svg viewBox="0 0 20 20" focusable="false" aria-hidden="true">' +
                "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                "<path d=\"M6.7 10.2l2.2 2.2 4.4-4.5\"></path>" +
                "</svg></span>"
            );
        }
        if (statusClass === "rejected") {
            return (
                '<span class="application-status-icon" aria-hidden="true">' +
                '<svg viewBox="0 0 20 20" focusable="false" aria-hidden="true">' +
                "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                "<path d=\"M7.2 7.2l5.6 5.6\"></path>" +
                "<path d=\"M12.8 7.2l-5.6 5.6\"></path>" +
                "</svg></span>"
            );
        }
        return (
            '<span class="application-status-icon" aria-hidden="true">' +
            '<svg viewBox="0 0 20 20" focusable="false" aria-hidden="true">' +
            "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
            "</svg></span>"
        );
    }

    function renderCoverLetter(app, detail) {
        var el = document.getElementById("cover-letter-body");
        if (!el) {
            return;
        }
        var text = safeText(app.coverLetter, "");
        if (!text && detail) {
            text = safeText(detail.coverLetter, "");
        }
        el.textContent = text || t("portal.taApplicationDetail.noCoverLetter", "No cover letter provided.");
    }

    function renderProfileGuidance(detail) {
        var profileCopy = document.getElementById("profile-jump-copy");
        if (profileCopy) {
            profileCopy.textContent =
                detail && detail.hasResume === true
                    ? t("portal.taApplicationDetail.profileCardHintReady", "View or edit your resume and skills.")
                    : t("portal.taApplicationDetail.profileCardHintMissingResume", "Add or update your resume, skills, and profile details.");
        }

        var noteText = document.getElementById("profile-sync-note-text");
        if (!noteText) {
            return;
        }

        var baseNote = t(
            "portal.taApplicationDetail.profileSyncNote",
            "Your profile and resume were sent with this application to the MO. You can update your profile after submission, and changes will sync to the MO view."
        );
        noteText.textContent = baseNote;
    }

    function renderJobTeaser(job) {
        var meta = document.getElementById("job-teaser-meta");
        if (!meta) {
            return;
        }
        meta.innerHTML = "";
        if (!job) {
            meta.textContent = t("portal.taApplicationDetail.jobUnavailable", "Job details unavailable.");
            disableJobTeaser();
            return;
        }
        enableJobTeaser(job);

        var workload = safeText(job.workload, "—");
        var applicants =
            typeof job.applicantCount === "number"
                ? String(job.applicantCount)
                : safeText(job.applicantCount, "0");
        var deadline = formatDisplayDateTime(job.deadline);
        var deadlineText = deadline;
        if (deadline && deadline !== "—") {
            deadlineText = t("portal.taApplicationDetail.deadlinePrefix", "Deadline") + " " + deadline;
        }

        meta.appendChild(teaserMetaItem("workload", t("portal.taApplicationDetail.workload", "Workload"), workload));
        meta.appendChild(teaserMetaItem("applicants", t("portal.taApplicationDetail.applicants", "Applicants"), applicants));
        meta.appendChild(teaserMetaItem("deadline", t("portal.taApplicationDetail.deadline", "Deadline"), deadlineText));
    }

    function enableJobTeaser(job) {
        var jobId = safeText(job.jobId, "");
        if (!jobId && state.application) {
            jobId = safeText(state.application.jobId, "");
        }
        if (!jobId) {
            disableJobTeaser();
            return;
        }
        teaserTrigger.href = contextPath + "/jsp/ta/job-detail.jsp?id=" + encodeURIComponent(jobId);
        teaserTrigger.removeAttribute("aria-disabled");
        teaserTrigger.classList.remove("is-disabled");
    }

    function disableJobTeaser() {
        teaserTrigger.removeAttribute("href");
        teaserTrigger.setAttribute("aria-disabled", "true");
        teaserTrigger.classList.add("is-disabled");
    }

    function teaserMetaItem(iconType, label, value) {
        var span = document.createElement("span");
        span.innerHTML =
            teaserMetaIconMarkup(iconType) +
            "<strong>" +
            escapeHtml(value) +
            "</strong>";
        span.setAttribute("title", label);
        return span;
    }

    function teaserMetaIconMarkup(iconType) {
        if (iconType === "workload") {
            return (
                "<span class=\"job-teaser-meta-icon\" aria-hidden=\"true\">" +
                    "<svg viewBox=\"0 0 20 20\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\">" +
                        "<circle cx=\"10\" cy=\"10\" r=\"7\"></circle>" +
                        "<path d=\"M10 6.4v3.8l2.4 1.6\"></path>" +
                    "</svg>" +
                "</span>"
            );
        }
        if (iconType === "applicants") {
            return (
                "<span class=\"job-teaser-meta-icon\" aria-hidden=\"true\">" +
                    "<svg viewBox=\"0 0 20 20\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\">" +
                        "<circle cx=\"7.2\" cy=\"7.4\" r=\"2.6\"></circle>" +
                        "<circle cx=\"13.5\" cy=\"8.4\" r=\"2.2\"></circle>" +
                        "<path d=\"M2.8 15.8c.6-2.1 2.3-3.4 4.4-3.4s3.8 1.3 4.4 3.4\"></path>" +
                        "<path d=\"M11.5 15.8c.4-1.5 1.6-2.5 3.1-2.5 1.2 0 2.3.7 2.8 1.8\"></path>" +
                    "</svg>" +
                "</span>"
            );
        }
        return (
            "<span class=\"job-teaser-meta-icon\" aria-hidden=\"true\">" +
                "<svg viewBox=\"0 0 20 20\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\">" +
                    "<rect x=\"3.6\" y=\"5.2\" width=\"12.8\" height=\"11\" rx=\"2\"></rect>" +
                    "<path d=\"M6.2 3.8v2.4\"></path>" +
                    "<path d=\"M13.8 3.8v2.4\"></path>" +
                    "<path d=\"M3.6 8.6h12.8\"></path>" +
                "</svg>" +
            "</span>"
        );
    }

    function formatDisplayDate(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "—";
        }
        var d = new Date(value);
        if (isNaN(d.getTime())) {
            return value;
        }
        return (
            d.getFullYear() +
            "-" +
            pad2(d.getMonth() + 1) +
            "-" +
            pad2(d.getDate())
        );
    }

    function formatDisplayDateTime(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "—";
        }
        var d = new Date(value);
        if (isNaN(d.getTime())) {
            return value;
        }
        return (
            d.getFullYear() +
            "-" +
            pad2(d.getMonth() + 1) +
            "-" +
            pad2(d.getDate()) +
            " " +
            pad2(d.getHours()) +
            ":" +
            pad2(d.getMinutes())
        );
    }

    function pad2(n) {
        return n < 10 ? "0" + n : String(n);
    }

    function showMessage(text, type) {
        if (!messageNode) {
            return;
        }
        messageNode.textContent = text;
        messageNode.classList.remove("hidden", "error", "success");
        messageNode.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        if (!messageNode) {
            return;
        }
        messageNode.textContent = "";
        messageNode.classList.add("hidden");
        messageNode.classList.remove("error", "success");
    }

    function handleUnauthorized() {
        showMessage(t("portal.taApplicationDetail.sessionExpired", "Your session has expired. Redirecting to login..."), "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
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
        try {
            return JSON.parse(text);
        } catch (e) {
            return null;
        }
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

    function safeText(value, fallback) {
        if (typeof value === "string" && value.trim()) {
            return value.trim();
        }
        if (typeof value === "number") {
            return String(value);
        }
        return typeof fallback === "string" ? fallback : "";
    }

    function escapeHtml(value) {
        if (typeof value !== "string") {
            return "";
        }
        return value
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }
})();
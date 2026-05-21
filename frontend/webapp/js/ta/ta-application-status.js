/*
 * TA application status page script, corresponds to /jsp/ta/application-status.jsp.
 *
 * Reads /api/applications?keyword=... and renders current TA's application cards and status timeline.
 * approximateOnly is returned by backend search to indicate "no exact match, only showing approximate results".
 */
(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";

    var searchForm = document.getElementById("status-search-form");
    var searchInput = document.getElementById("status-search-input");
    var searchButton = document.getElementById("status-search-btn");
    var messageNode = document.getElementById("status-message");
    var listSummaryNode = document.getElementById("list-summary");
    var listNode = document.getElementById("applications-list");

    if (!searchForm || !searchInput || !listNode || !listSummaryNode) {
        return;
    }

    var state = {
        // currentApplications saves the most recent API result; on language switch it re-renders directly without repeating requests.
        loading: false,
        loadError: false,
        approximateOnly: false,
        lastKeyword: "",
        keywordSearchTriggered: false,
        currentApplications: []
    };

    searchForm.addEventListener("submit", function (event) {
        event.preventDefault();
        submitSearch();
    });

    searchInput.addEventListener("blur", function () {
        if (searchInput.value.trim()) {
            return;
        }
        if (state.lastKeyword !== "" || state.keywordSearchTriggered) {
            loadApplications("", false);
        }
    });

    document.addEventListener("app:locale-changed", function () {
        renderList(state.currentApplications);
        setLoading(state.loading);
    });

    loadApplications("", false);

    function submitSearch() {
        loadApplications(searchInput.value.trim(), true);
    }

    function loadApplications(keyword, isUserTriggeredSearch) {
        if (state.loading) {
            return;
        }

        var normalizedKeyword = typeof keyword === "string" ? keyword.trim() : searchInput.value.trim();
        state.lastKeyword = normalizedKeyword;
        state.keywordSearchTriggered = !!isUserTriggeredSearch && normalizedKeyword.length > 0;

        setLoading(true);
        state.loadError = false;
        state.approximateOnly = false;
        hideMessage();
        setListSummary(t("portal.taApplicationStatus.loadingApplications", "Loading applications..."));
        listNode.innerHTML = "";

        request(buildApplyUrl(normalizedKeyword), {
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

                if (response.status === 403) {
                    showMessage(t("portal.dynamic.taOnlyPage", "This page is available for TA accounts only."), "error");
                    state.loadError = true;
                    state.currentApplications = [];
                    renderList([]);
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = t("portal.dynamic.unableLoadApplications", "Unable to load your applications.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.unableLoadApplications", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    state.loadError = true;
                    state.currentApplications = [];
                    renderList([]);
                    return;
                }

                var data = getPayloadDataObject(payload);
                var applications = getPayloadDataArray(payload, "applications");
                state.approximateOnly = !!data.approximateOnly;
                state.currentApplications = applications;
                renderList(applications);
            })
            .catch(function () {
                showMessage(t("portal.dynamic.networkErrorTryAgain", "Network error. Please try again."), "error");
                state.loadError = true;
                state.currentApplications = [];
                renderList([]);
            })
            .finally(function () {
                setLoading(false);
            });
    }

    function buildApplyUrl(keyword) {
        return window.TARecruitment.routes.applications.list({
            keyword: keyword
        });
    }

    function renderList(applications) {
        listNode.innerHTML = "";
        var keyword = state.lastKeyword;
        var hasKeywordSearch = state.keywordSearchTriggered;

        if (state.loadError) {
            setListSummary(t("portal.dynamic.unableLoadApplicationsNow", "Unable to load applications right now."));
            listNode.appendChild(createEmptyState("load-error"));
            return;
        }

        if (!Array.isArray(applications) || applications.length === 0) {
            if (hasKeywordSearch && keyword) {
                setListSummary(t("portal.dynamic.noApplicationsForSearch", "No applications match your keyword."));
                listNode.appendChild(createEmptyState("no-match"));
            } else {
                setListSummary(t("portal.dynamic.noApplicationsSubmitted", "No applications submitted yet."));
                listNode.appendChild(createEmptyState("no-applications"));
            }
            return;
        }

        setListSummary(buildSummaryText(applications.length, t("portal.dynamic.applicationUnit", "application")));

        if (state.approximateOnly && hasKeywordSearch) {
            showMessage(t("portal.dynamic.closestMatchesNotice", "No exact matches. Showing closest results."), "success");
        } else {
            hideMessage();
        }

        applications.forEach(function (app) {
            listNode.appendChild(createApplicationCard(app));
        });
    }

    function createApplicationCard(app) {
        var card = document.createElement("article");
        var applicationId = safeText(app.applicationId, "");
        var status = safeText(app.status, "PENDING").toUpperCase();
        var statusClass = getApplicationStatusClass(status);
        var detailLink =
            contextPath + "/jsp/ta/application-detail.jsp?id=" + encodeURIComponent(applicationId);
        var title = safeText(app.jobTitle, t("portal.dynamic.untitledPosition", "Untitled position"));
        var subtitle = buildApplicationSubtitle(safeText(app.courseCode, ""), app.appliedAt);

        card.className = "application-card status-" + statusClass;
        card.setAttribute("role", "link");
        card.setAttribute("tabindex", "0");
        card.setAttribute("aria-label", t("portal.dynamic.viewDetails", "View details") + " " + title);
        card.setAttribute("data-application-id", applicationId);

        card.innerHTML =
            "<span class=\"application-accent\" aria-hidden=\"true\"></span>" +
            "<div class=\"application-main\">" +
                "<div class=\"application-heading\">" +
                    "<h3>" + escapeHtml(title) + "</h3>" +
                    "<p class=\"application-subtitle\">" + subtitle + "</p>" +
                    buildApplicationTimeline(statusClass) +
                "</div>" +
            "</div>" +
            "<div class=\"application-side\">" +
                "<span class=\"application-status-chip status-" + statusClass + "\">" +
                    getStatusIconMarkup(statusClass) +
                    "<span class=\"application-status-text\">" + escapeHtml(getApplicationStatusLabel(status)) + "</span>" +
                "</span>" +
            "</div>";

        card.addEventListener("click", function () {
            window.location.href = detailLink;
        });
        card.addEventListener("keydown", function (event) {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                window.location.href = detailLink;
            }
        });

        return card;
    }

    function buildApplicationTimeline(statusClass) {
        var isPending = statusClass === "pending";
        var isAccepted = statusClass === "accepted";
        var isRejected = statusClass === "rejected";
        var isWithdrawn = statusClass === "withdrawn";
        var reviewState = isPending ? "current" : "done";
        var decisionState = isAccepted || isRejected || isWithdrawn ? "current" : "";
        var decisionLabel = isAccepted
            ? t("portal.common.accepted", "Accepted")
            : isRejected
                ? t("portal.common.rejected", "Rejected")
                : isWithdrawn
                    ? t("portal.common.withdrawn", "Withdrawn")
                    : t("portal.dynamic.decision", "Decision");

        return "<ol class=\"application-timeline\" aria-label=\"" + escapeHtml(t("portal.dynamic.applicationTimeline", "Application timeline")) + "\">" +
            "<li class=\"timeline-step is-done\">" +
                "<span class=\"timeline-dot\" aria-hidden=\"true\"></span>" +
                "<span>" + escapeHtml(t("portal.dynamic.applied", "Applied")) + "</span>" +
            "</li>" +
            "<li class=\"timeline-step is-" + reviewState + "\">" +
                "<span class=\"timeline-dot\" aria-hidden=\"true\"></span>" +
                "<span>" + escapeHtml(t("portal.dynamic.review", "Review")) + "</span>" +
            "</li>" +
            "<li class=\"timeline-step" + (decisionState ? " is-" + decisionState : "") + "\">" +
                "<span class=\"timeline-dot\" aria-hidden=\"true\"></span>" +
                "<span>" + escapeHtml(decisionLabel) + "</span>" +
            "</li>" +
        "</ol>";
    }

    function buildApplicationSubtitle(courseCode, appliedAt) {
        var parts = [];
        if (courseCode) {
            parts.push("<span class=\"application-course-code\">" + escapeHtml(courseCode) + "</span>");
        }

        parts.push("<span class=\"application-date-label\">" + escapeHtml(t("portal.dynamic.appliedAt", "Applied at")) + "</span>");
        parts.push(
            "<time class=\"application-date-value\" datetime=\"" + escapeHtml(safeText(appliedAt, "")) + "\">" +
                escapeHtml(formatDateTime(appliedAt)) +
            "</time>"
        );

        return parts.join("<span class=\"application-subtitle-separator\" aria-hidden=\"true\">·</span>");
    }

    function getApplicationStatusClass(status) {
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

    function getApplicationStatusLabel(status) {
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
        return safeText(status, "-");
    }

    function getStatusIconMarkup(statusClass) {
        if (statusClass === "pending") {
            return "<span class=\"application-status-icon\" aria-hidden=\"true\">" +
                "<svg viewBox=\"0 0 20 20\" focusable=\"false\" aria-hidden=\"true\">" +
                    "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                    "<path d=\"M10 6.25v4.1l2.7 1.7\"></path>" +
                "</svg>" +
            "</span>";
        }

        if (statusClass === "accepted") {
            return "<span class=\"application-status-icon\" aria-hidden=\"true\">" +
                "<svg viewBox=\"0 0 20 20\" focusable=\"false\" aria-hidden=\"true\">" +
                    "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                    "<path d=\"M6.7 10.2l2.2 2.2 4.4-4.5\"></path>" +
                "</svg>" +
            "</span>";
        }

        if (statusClass === "rejected") {
            return "<span class=\"application-status-icon\" aria-hidden=\"true\">" +
                "<svg viewBox=\"0 0 20 20\" focusable=\"false\" aria-hidden=\"true\">" +
                    "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                    "<path d=\"M7.2 7.2l5.6 5.6\"></path>" +
                    "<path d=\"M12.8 7.2l-5.6 5.6\"></path>" +
                "</svg>" +
            "</span>";
        }

        if (statusClass === "withdrawn") {
            return "<span class=\"application-status-icon\" aria-hidden=\"true\">" +
                "<svg viewBox=\"0 0 20 20\" focusable=\"false\" aria-hidden=\"true\">" +
                    "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                    "<path d=\"M6.6 10h6.8\"></path>" +
                "</svg>" +
            "</span>";
        }

        return "<span class=\"application-status-icon\" aria-hidden=\"true\">" +
            "<svg viewBox=\"0 0 20 20\" focusable=\"false\" aria-hidden=\"true\">" +
                "<circle cx=\"10\" cy=\"10\" r=\"7.25\"></circle>" +
                "<path d=\"M10 10h0\"></path>" +
            "</svg>" +
        "</span>";
    }

    function createEmptyState(mode) {
        var empty = document.createElement("div");
        empty.className = "empty-state";

        if (mode === "load-error") {
            empty.innerHTML =
                "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.unableLoadApplicationsTitle", "Unable to load applications")) + "</p>" +
                "<p class=\"empty-copy\">" + escapeHtml(t("portal.dynamic.networkErrorTryAgain", "Network error. Please try again.")) + "</p>";
            return empty;
        }

        if (mode === "no-match") {
            empty.innerHTML =
                "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.noMatchingApplicationsTitle", "No matching applications")) + "</p>" +
                "<p class=\"empty-copy\">" + escapeHtml(t("portal.dynamic.tryAnotherKeyword", "Try another keyword.")) + "</p>";
            return empty;
        }

        empty.innerHTML =
            "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.noApplicationsYetTitle", "No applications yet")) + "</p>" +
            "<p class=\"empty-copy\">" + escapeHtml(t("portal.dynamic.statusAppearsAfterApply", "After you apply for a job, the status will appear here.")) + "</p>";
        return empty;
    }

    function setLoading(loading) {
        state.loading = loading;
        if (searchButton) {
            searchButton.disabled = loading;
            searchButton.textContent = loading
                ? t("portal.dynamic.searching", "Searching...")
                : t("portal.common.search", "Search");
        }
    }

    function setListSummary(text) {
        if (!state.lastKeyword) {
            listSummaryNode.hidden = true;
            listSummaryNode.textContent = "";
            return;
        }
        listSummaryNode.hidden = false;
        listSummaryNode.textContent = text;
    }

    function showMessage(message, type) {
        messageNode.textContent = message;
        messageNode.classList.remove("hidden", "error", "success");
        messageNode.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        messageNode.textContent = "";
        messageNode.classList.remove("error", "success");
        messageNode.classList.add("hidden");
    }

    function handleUnauthorized() {
        showMessage(t("portal.dynamic.sessionExpiredRedirect", "Session expired. Redirecting to login..."), "error");
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
            return {};
        }
        if (payload.data && typeof payload.data === "object") {
            return payload.data;
        }
        return payload;
    }

    function buildSummaryText(count, singularUnit) {
        var unit = singularUnit;
        if (useEnglishPluralSuffix() && count !== 1) {
            unit += "s";
        }
        return t("portal.dynamic.showing", "Showing") + " " + count + " " + unit + ".";
    }

    function useEnglishPluralSuffix() {
        if (window.AppI18n && typeof window.AppI18n.getLocale === "function") {
            return window.AppI18n.getLocale() === "en";
        }
        return true;
    }

    function t(key, fallback) {
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
        return fallbackKey ? t(fallbackKey, fallbackText) : (fallbackText || "");
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
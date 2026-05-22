/*
 * TA job list page script, corresponds to /jsp/ta/job-list.jsp.
 *
 * Normal search calls /api/jobs; AI recommendation mode calls /api/ta/job-recommendations.
 * AI mode only reorders/marks real jobs returned by the backend, no fake jobs are generated on the frontend.
 */
(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";

    var searchForm = document.getElementById("job-search-form");
    var searchInput = document.getElementById("job-search-input");
    var searchButton = document.getElementById("job-search-btn");
    var searchModeButton = document.getElementById("job-search-mode-toggle");
    var listMessage = document.getElementById("list-message");
    var listSummary = document.getElementById("job-list-summary");
    var jobList = document.getElementById("job-list");

    if (!searchForm || !searchInput || !listSummary || !jobList) {
        return;
    }

    /*
     * Centralized page state:
     * - searchMode determines normal keyword search or AI recommendation;
     * - aiRecommendationsByJobId only stores recommendation reasons returned by backend, no fake jobs generated;
     * - currentJobs is used for re-rendering existing results after language switch.
     */
    var state = {
        // searchMode controls normal search/AI recommendation; currentJobs is always the actual rendered job array.
        loading: false,
        loadError: false,
        approximateOnly: false,
        lastKeyword: "",
        keywordSearchTriggered: false,
        searchMode: "search",
        aiSearchLoading: false,
        aiSearchActive: false,
        aiRecommendationsByJobId: {},
        currentJobs: []
    };

    searchForm.addEventListener("submit", function (event) {
        event.preventDefault();
        submitSearch();
    });

    searchInput.addEventListener("blur", function () {
        if (state.searchMode !== "search") {
            return;
        }
        if (searchInput.value.trim()) {
            return;
        }
        if (state.lastKeyword !== "" || state.keywordSearchTriggered) {
            loadJobs("", false);
        }
    });

    if (searchModeButton) {
        searchModeButton.addEventListener("click", function (event) {
            var target = event.target && event.target.closest
                ? event.target.closest("[data-search-mode-option]")
                : null;
            var requestedMode = target
                ? target.getAttribute("data-search-mode-option")
                : (state.searchMode === "ai" ? "search" : "ai");
            setSearchMode(requestedMode === "ai" ? "ai" : "search");
        });
    }

    document.addEventListener("app:locale-changed", function () {
        setSearchMode(state.searchMode);
        renderJobs(state.currentJobs);
        updateSearchControls();
    });

    setSearchMode("search");
    loadJobs("", false);

    /*
     * Unified entry point for search form submission.
     * If currently in AI mode, requests /api/ta/job-recommendations, otherwise requests /api/jobs.
     */
    function submitSearch() {
        if (state.searchMode === "ai") {
            runAiSearch();
            return;
        }
        loadJobs(searchInput.value.trim(), true);
    }

    /*
     * Normal job list loading.
     * keyword goes to /api/jobs?keyword=..., backend returns approximateOnly for the page to show approximate match notice.
     */
    function loadJobs(keyword, isUserTriggeredSearch) {
        if (state.loading || state.aiSearchLoading) {
            return;
        }

        var normalizedKeyword = typeof keyword === "string" ? keyword.trim() : searchInput.value.trim();
        state.lastKeyword = normalizedKeyword;
        state.keywordSearchTriggered = !!isUserTriggeredSearch && normalizedKeyword.length > 0;
        state.aiSearchActive = false;
        state.aiRecommendationsByJobId = {};

        setLoading(true);
        state.loadError = false;
        state.approximateOnly = false;
        hideMessage();
        setListSummary(t("portal.taJobList.loadingPositions", "Loading positions..."));
        jobList.innerHTML = "";

        request(buildJobsUrl(normalizedKeyword), {
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
                    var errorMessage = t("portal.dynamic.unableLoadJobsRetry", "Unable to load jobs right now. Please try again.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.unableLoadJobsRetry", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    state.loadError = true;
                    renderJobs([]);
                    return;
                }

                var data = getPayloadDataObject(payload);
                var jobs = getPayloadDataArray(payload, "jobs");
                state.approximateOnly = !!data.approximateOnly;
                renderJobs(jobs);
            })
            .catch(function () {
                showMessage(t("portal.dynamic.networkErrorMoment", "Network error. Please try again in a moment."), "error");
                state.loadError = true;
                renderJobs([]);
            })
            .finally(function () {
                setLoading(false);
            });
    }

    /*
     * TA-side AI recommendation search.
     * Only displays real open jobs and recommendation reasons returned by backend; shows failure state when DeepSeek is unavailable.
     */
    function runAiSearch() {
        if (state.loading || state.aiSearchLoading) {
            return;
        }

        var query = searchInput.value.trim();
        var params = new URLSearchParams();
        params.set("query", query);

        setAiSearchLoading(true);
        state.loadError = false;
        state.approximateOnly = false;
        showMessage(t("portal.taJobList.aiSearchLoading", "AI is recommending jobs..."), "success");

        request(window.TARecruitment.routes.ta.jobRecommendations(), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: params.toString()
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                var data = getPayloadDataObject(payload);
                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = t("portal.taJobList.aiSearchUnavailable", "AI recommendation is unavailable right now.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.taJobList.aiSearchUnavailable", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                if (data.action === "out_of_scope") {
                    showMessage(
                        localizeServerMessage(data.message || payload.message, "portal.taJobList.aiOutOfScope", "I cannot handle your question. I can recommend jobs, compare jobs, or explain recommendation reasons based on your profile and open positions."),
                        "error"
                    );
                    return;
                }

                var jobs = getPayloadDataArray(payload, "jobs");
                var recommendationsByJobId = data.recommendationsByJobId && typeof data.recommendationsByJobId === "object"
                    ? data.recommendationsByJobId
                    : {};
                state.aiSearchActive = true;
                state.aiRecommendationsByJobId = recommendationsByJobId;
                state.lastKeyword = query;
                state.keywordSearchTriggered = query.length > 0;
                renderJobs(jobs);

                var successMessage = localizeServerMessage(data.message || payload.message, "", "");
                if (successMessage) {
                    showMessage(successMessage, "success");
                } else {
                    hideMessage();
                }
            })
            .catch(function () {
                showMessage(t("portal.taJobList.aiSearchUnavailable", "AI recommendation is unavailable right now."), "error");
            })
            .finally(function () {
                setAiSearchLoading(false);
            });
    }

    /*
     * Build normal job list URL.
     * API paths must go through TARecruitment.routes to ensure correct deployment context path.
     */
    function buildJobsUrl(keyword) {
        var params = new URLSearchParams();
        if (keyword) {
            params.set("keyword", keyword);
        }
        return window.TARecruitment.routes.jobs.list({
            keyword: keyword
        });
    }

    /*
     * Render current job list.
     * Handles four states: loading error, empty list, normal search no results, and AI recommendation no results.
     */
    function renderJobs(jobs) {
        var keyword = state.lastKeyword;
        var hasKeywordSearch = state.keywordSearchTriggered;
        state.currentJobs = Array.isArray(jobs) ? jobs : [];
        jobList.innerHTML = "";

        if (state.loadError) {
            setListSummary(t("portal.dynamic.unableLoadJobs", "Unable to load jobs right now."));
            jobList.appendChild(createEmptyState("load-error"));
            return;
        }

        if (!Array.isArray(jobs) || jobs.length === 0) {
            if (state.aiSearchActive) {
                setListSummary(t("portal.taJobList.aiNoRecommendations", "No AI recommendations for the current open positions."));
                jobList.appendChild(createEmptyState("ai-empty"));
                return;
            }
            if (hasKeywordSearch && keyword) {
                setListSummary(t("portal.dynamic.noJobsForSearch", "No jobs match your keyword."));
                jobList.appendChild(createEmptyState("no-match"));
            } else {
                setListSummary(t("portal.dynamic.noJobsAvailable", "No jobs available right now."));
                jobList.appendChild(createEmptyState("no-jobs"));
            }
            return;
        }

        if (state.aiSearchActive) {
            setListSummary(buildSummaryText(jobs.length, t("portal.taJobList.aiRecommendedUnit", "AI recommendation")));
            jobs.forEach(function (job) {
                jobList.appendChild(createJobCard(job));
            });
            return;
        }

        setListSummary(buildSummaryText(jobs.length, t("portal.dynamic.jobUnit", "job")));
        if (state.approximateOnly && hasKeywordSearch) {
            showMessage(t("portal.dynamic.closestMatchesNotice", "No exact matches. Showing closest results."), "success");
        } else {
            hideMessage();
        }

        jobs.forEach(function (job) {
            jobList.appendChild(createJobCard(job));
        });
    }

    /*
     * Create a single job card.
     * Card click navigates to the reused job detail page; AI recommendation reason is only shown in AI search results.
     */
    function createJobCard(job) {
        var card = document.createElement("article");
        var jobId = getSafeText(job.jobId, "");
        var status = getSafeText(job.status || "OPEN").toUpperCase();
        var statusClass = getJobStatusClass(status);
        var detailHref = contextPath + "/jsp/ta/job-detail.jsp?id=" + encodeURIComponent(jobId);
        var title = getSafeText(job.title, t("portal.dynamic.untitledPosition", "Untitled position"));
        var subtitle = buildJobSubtitle(job);
        var metaLine = buildJobMeta(job);
        var courseBadge = getSafeText(job.courseCode, "TA").slice(0, 8).toUpperCase();
        var skillTags = buildSkillTags(job.requiredSkills || job.skills || "");
        var recommendation = state.aiSearchActive
            ? getSafeText(state.aiRecommendationsByJobId[jobId], "")
            : "";

        card.className = "job-card status-" + statusClass + (recommendation ? " job-card--ai" : "");
        card.setAttribute("role", "link");
        card.setAttribute("tabindex", "0");
        card.setAttribute("aria-label", t("portal.dynamic.viewDetails", "View details") + " " + title);
        card.setAttribute("data-job-id", jobId);

        card.innerHTML =
            "<span class=\"job-course-badge\" aria-hidden=\"true\">" + escapeHtml(courseBadge || "TA") + "</span>" +
            "<div class=\"job-main\">" +
                "<div class=\"job-heading\">" +
                    "<h3>" + escapeHtml(title) + "</h3>" +
                    "<p class=\"job-subtitle\">" + subtitle + "</p>" +
                    (metaLine
                        ? "<p class=\"job-meta-line\">" + metaLine + "</p>"
                        : "") +
                    skillTags +
                "</div>" +
            "</div>" +
            "<div class=\"job-side\">" +
                "<span class=\"job-status-chip status-" + statusClass + "\">" + escapeHtml(getJobStatusLabel(status)) + "</span>" +
            "</div>" +
            (recommendation
                ? "<div class=\"job-ai-note\">" +
                    "<p class=\"job-ai-note-title\">" + escapeHtml(t("portal.taJobList.aiRecommendationTitle", "Recommendation (AI generated)")) + "</p>" +
                    "<p class=\"job-ai-note-copy\">" + escapeHtml(recommendation) + "</p>" +
                "</div>"
                : "");

        card.addEventListener("click", function () {
            window.location.href = detailHref;
        });
        card.addEventListener("keydown", function (event) {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                window.location.href = detailHref;
            }
        });

        return card;
    }

    /*
     * Map backend enum status to CSS class suffix.
     */
    function getJobStatusClass(status) {
        if (status === "OPEN") {
            return "open";
        }
        if (status === "CLOSED") {
            return "closed";
        }
        if (status === "FILLED") {
            return "filled";
        }
        return "unknown";
    }

    /*
     * Map backend enum status to page-readable label.
     */
    function getJobStatusLabel(status) {
        if (status === "OPEN") {
            return t("portal.common.open", "Open");
        }
        if (status === "CLOSED") {
            return t("portal.common.closed", "Closed");
        }
        if (status === "FILLED") {
            return t("portal.common.filled", "Filled");
        }
        return getSafeText(status, "-");
    }

    /*
     * Build job subtitle: course, MO, and deadline.
     */
    function buildJobSubtitle(job) {
        var parts = [];
        var courseCode = getSafeText(job.courseCode, "");
        var courseName = getSafeText(job.courseName, "");
        var moName = getSafeText(job.moName, "-");
        var deadlineLabel = t("portal.common.deadline", "Deadline");
        var deadlineText = formatDateTime(job.deadline);

        if (courseCode) {
            parts.push("<span class=\"job-course-code\">" + escapeHtml(courseCode) + "</span>");
        }
        if (courseName) {
            parts.push("<span class=\"job-course-name\">" + escapeHtml(courseName) + "</span>");
        }
        parts.push("<span class=\"job-mo\">" + escapeHtml(t("portal.dynamic.moShort", "MO") + " " + moName) + "</span>");
        parts.push("<span class=\"job-deadline\">" + escapeHtml(deadlineLabel + " " + deadlineText) + "</span>");

        return parts.join("<span class=\"job-subtitle-separator\" aria-hidden=\"true\">·</span>");
    }

    /*
     * Build card meta info: positions, salary, workload.
     */
    function buildJobMeta(job) {
        var parts = [];

        parts.push(escapeHtml(t("portal.common.positions", "Positions") + " " + String(job.positions || 0)));

        if (job.salary) {
            parts.push(escapeHtml(t("portal.common.salary", "Salary") + " " + getSafeText(job.salary)));
        }
        if (job.workload) {
            parts.push(escapeHtml(t("portal.common.workload", "Workload") + " " + getSafeText(job.workload)));
        }

        return parts.join("<span class=\"job-meta-separator\" aria-hidden=\"true\">·</span>");
    }

    /*
     * Skill tags only show the first 4 to avoid job cards being too tall.
     */
    function buildSkillTags(skillsText) {
        var skills = String(skillsText || "")
            .split(/[,;，；]/)
            .map(function (item) { return item.trim(); })
            .filter(Boolean)
            .slice(0, 4);
        if (skills.length === 0) {
            return "";
        }
        return "<div class=\"job-skill-row\">" + skills.map(function (skill) {
            return "<span>" + escapeHtml(skill) + "</span>";
        }).join("") + "</div>";
    }

    /*
     * Generate appropriate text for different empty states.
     */
    function createEmptyState(mode) {
        var empty = document.createElement("div");
        empty.className = "empty-state";

        if (mode === "load-error") {
            empty.innerHTML =
                "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.unableLoadPositionsTitle", "Unable to load positions")) + "</p>" +
                "<p class=\"empty-copy\">" + escapeHtml(t("portal.dynamic.refreshAfterNetworkCheck", "Please refresh the list after checking your network connection.")) + "</p>";
            return empty;
        }

        if (mode === "no-jobs") {
            empty.innerHTML =
                "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.noPositionsPublishedTitle", "No positions published yet")) + "</p>" +
                "<p class=\"empty-copy\">" + escapeHtml(t("portal.dynamic.positionsAppearAfterPublish", "When MO publishes new jobs, they will appear here.")) + "</p>";
            return empty;
        }

        if (mode === "ai-empty") {
            empty.innerHTML =
                "<p class=\"empty-title\">" + escapeHtml(t("portal.taJobList.aiNoRecommendations", "No AI recommendations for the current open positions.")) + "</p>" +
                "<p class=\"empty-copy\">" + escapeHtml(t("portal.taJobList.aiNoRecommendationsHint", "Try asking for a different teaching focus or check again when more open jobs are available.")) + "</p>";
            return empty;
        }

        empty.innerHTML =
            "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.noMatchingPositionsTitle", "No matching positions")) + "</p>" +
            "<p class=\"empty-copy\">" + escapeHtml(t("portal.dynamic.tryAnotherKeyword", "Try another keyword.")) + "</p>";
        return empty;
    }

    /*
     * Toggle between normal search and AI recommendation mode.
     * Only changes control state, does not automatically send requests, to avoid AI calls triggered by user accidentally clicking mode switch.
     */
    function setSearchMode(mode) {
        state.searchMode = mode === "ai" ? "ai" : "search";
        if (searchModeButton) {
            searchModeButton.setAttribute("data-mode", state.searchMode);
            var options = searchModeButton.querySelectorAll("[data-search-mode-option]");
            options.forEach(function (option) {
                var isActive = option.getAttribute("data-search-mode-option") === state.searchMode;
                option.classList.toggle("is-active", isActive);
                option.setAttribute("aria-pressed", isActive ? "true" : "false");
            });
        }
        if (searchInput) {
            searchInput.setAttribute("placeholder", state.searchMode === "ai"
                ? t("portal.taJobList.aiSearchPlaceholder", "Ask for recommended jobs based on your profile")
                : t("portal.taJobList.searchPlaceholder", "Search jobs by title, course code, or keywords"));
        }
        updateSearchControls();
    }

    function setLoading(loading) {
        state.loading = loading;
        updateSearchControls();
    }

    function setAiSearchLoading(loading) {
        state.aiSearchLoading = loading;
        updateSearchControls();
    }

    /*
     * Synchronize search button, AI mode button, and loading state text.
     */
    function updateSearchControls() {
        var busy = state.loading || state.aiSearchLoading;
        if (searchButton) {
            searchButton.disabled = busy;
            searchButton.classList.toggle("search-submit--ai", state.searchMode === "ai");
            if (state.aiSearchLoading) {
                searchButton.textContent = t("portal.taJobList.aiSearching", "AI searching...");
            } else if (state.loading) {
                searchButton.textContent = t("portal.dynamic.searching", "Searching...");
            } else {
                searchButton.textContent = state.searchMode === "ai"
                    ? t("portal.taJobList.aiSearchButton", "AI")
                    : t("portal.common.search", "Search");
            }
        }
        if (searchModeButton) {
            searchModeButton.disabled = busy;
        }
    }

    /*
     * List summary is only shown during search, AI, or error states; default homepage list stays clean.
     */
    function setListSummary(text) {
        if (!state.lastKeyword && !state.aiSearchActive && !state.loadError) {
            listSummary.hidden = true;
            listSummary.textContent = "";
            return;
        }
        listSummary.hidden = false;
        listSummary.textContent = text;
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

    function showMessage(message, type) {
        if (!listMessage) {
            return;
        }
        listMessage.textContent = message;
        listMessage.classList.remove("hidden", "error", "success");
        listMessage.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        if (!listMessage) {
            return;
        }
        listMessage.textContent = "";
        listMessage.classList.remove("error", "success");
        listMessage.classList.add("hidden");
    }

    function handleUnauthorized() {
        showMessage(t("portal.dynamic.sessionExpiredRedirect", "Session expired. Redirecting to login..."), "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
    }

    /*
     * Page-local request wrapper stays as a compatibility layer; internally prioritizes the shared request utility.
     */
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

    /*
     * Extract array data from unified response; compatible with both data.jobs and legacy top-level jobs formats.
     */
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

    /*
     * Extract data object from unified response; returns payload itself as fallback if no data wrapper exists.
     */
    function getPayloadDataObject(payload) {
        if (!payload || typeof payload !== "object") {
            return {};
        }
        if (payload.data && typeof payload.data === "object") {
            return payload.data;
        }
        return payload;
    }

    /*
     * Convert backend datetime string to local short datetime display.
     */
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

    function getSafeText(value, fallback) {
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
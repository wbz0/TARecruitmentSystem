/*
 * Admin workload dashboard script for /jsp/admin/dashboard.jsp.
 *
 * The backend owns workload calculations. This file only loads the report,
 * applies client-side search/pagination, and renders TA workload cards.
 */
(function () {
    "use strict";

    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var i18n = window.AppI18n && typeof window.AppI18n.t === "function" ? window.AppI18n : null;

    var filterForm = document.getElementById("workload-filter-form");
    var searchInput = document.getElementById("workload-search-input");
    var applyButton = document.getElementById("apply-filter-btn");
    var messageNode = document.getElementById("dashboard-message");
    var taSummaryNode = document.getElementById("ta-summary");
    var taListNode = document.getElementById("ta-list");
    var paginationNode = document.getElementById("workload-pagination");

    if (!filterForm || !searchInput || !taSummaryNode || !taListNode) {
        return;
    }

    var PAGE_SIZE = 5;
    var EMPTY_REPORT = {
        taWorkloads: [],
        invalidJobs: [],
        totalTaCount: 0,
        totalAcceptedJobs: 0,
        totalWorkWeeks: 0,
        totalWorkHours: 0
    };
    var state = {
        loading: false,
        report: EMPTY_REPORT,
        expandedTaIds: {},
        searchQuery: "",
        currentPage: 1
    };

    searchInput.addEventListener("keydown", function (event) {
        if (event.key !== "Enter" || event.isComposing) return;
        event.preventDefault();
        applySearch();
    });

    filterForm.addEventListener("submit", function (event) {
        event.preventDefault();
        applySearch();
    });

    document.addEventListener("app:locale-changed", function () {
        renderDashboard(state.report);
        setLoadingState(state.loading);
    });

    loadDashboard();

    function loadDashboard() {
        if (state.loading) return Promise.resolve();

        state.loading = true;
        state.currentPage = 1;
        hideMessage();
        setLoadingState(true);
        taSummaryNode.textContent = t("portal.adminDashboard.loadingWorkload", "Loading workload...");
        taListNode.innerHTML = "";
        hidePagination();

        return fetchWorkloadReport()
            .then(function (result) {
                if (result.unauthorized) {
                    handleUnauthorized();
                    return;
                }
                if (!result.ok) {
                    state.report = EMPTY_REPORT;
                    showMessage(result.message || t("portal.dynamic.failedLoadTaWorkloads", "Failed to load TA workloads."), "error");
                } else {
                    state.report = normalizeReport(result.payload);
                }
                renderDashboard(state.report);
            })
            .catch(function () {
                state.report = EMPTY_REPORT;
                showMessage(t("portal.dynamic.networkErrorLoadingDashboard", "Network error while loading dashboard."), "error");
                renderDashboard(state.report);
            })
            .finally(function () {
                state.loading = false;
                setLoadingState(false);
            });
    }

    function fetchWorkloadReport() {
        var url = workloadStatisticsUrl();
        if (!url) {
            return Promise.resolve({
                unauthorized: false,
                ok: false,
                payload: null,
                message: t("portal.dynamic.failedLoadTaWorkloads", "Failed to load TA workloads.")
            });
        }
        return request(url, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (result) {
            return normalizeApiResult(result, "TA workload request failed.");
        });
    }

    function workloadStatisticsUrl() {
        if (
            window.TARecruitment &&
            window.TARecruitment.routes &&
            window.TARecruitment.routes.admin &&
            typeof window.TARecruitment.routes.admin.workloadStatistics === "function"
        ) {
            return window.TARecruitment.routes.admin.workloadStatistics();
        }
        return "";
    }

    function applySearch() {
        state.searchQuery = searchInput.value.trim();
        state.currentPage = 1;
        renderDashboard(state.report);
    }

    function renderDashboard(report) {
        renderTaCards(report && Array.isArray(report.taWorkloads) ? report.taWorkloads : []);
    }

    function renderTaCards(taWorkloads) {
        taListNode.innerHTML = "";
        hidePagination();

        if (!Array.isArray(taWorkloads) || taWorkloads.length === 0) {
            taSummaryNode.textContent = t("portal.dynamic.noTaWorkloadSelectedRange", "No TA workload data in selected work range.");
            taListNode.appendChild(createEmptyState(t("portal.dynamic.adjustWorkRangeHint", "Adjust the work range or add structured work-hour fields to accepted jobs.")));
            return;
        }

        var filtered = filterTaWorkloads(taWorkloads, state.searchQuery);
        if (filtered.length === 0) {
            taSummaryNode.textContent = t("portal.adminDashboard.noWorkloadMatches", "No TA workload matches your keyword.");
            taListNode.appendChild(createEmptyState(t("portal.adminDashboard.noWorkloadMatchesHint", "Try another TA name, job title, or course code.")));
            return;
        }

        var sorted = filtered.slice().sort(function (a, b) {
            return toNumber(b.totalWorkHours) - toNumber(a.totalWorkHours);
        });
        var maxValue = sorted.reduce(function (max, item) {
            return Math.max(max, toNumber(item.totalWorkHours));
        }, 0);

        pruneExpandedState(sorted);

        var totalPages = Math.max(1, Math.ceil(sorted.length / PAGE_SIZE));
        state.currentPage = Math.min(Math.max(1, state.currentPage), totalPages);

        var pageStart = (state.currentPage - 1) * PAGE_SIZE;
        var pageItems = sorted.slice(pageStart, pageStart + PAGE_SIZE);

        taSummaryNode.textContent = buildWorkloadSummary(pageItems.length, sorted.length, taWorkloads.length);
        pageItems.forEach(function (item) {
            taListNode.appendChild(createTaCard(item, maxValue));
        });
        renderPagination(totalPages);
    }

    function createTaCard(item, maxValue) {
        var taId = safeText(item.taId, safeText(item.taName, ""));
        var isExpanded = !!state.expandedTaIds[taId];
        var totalHours = toNumber(item.totalWorkHours);
        var percent = maxValue > 0 ? Math.round((totalHours * 100) / maxValue) : 0;

        var element = document.createElement("article");
        element.className = "ta-workload-card" + (isExpanded ? " is-expanded" : "");
        element.innerHTML =
            "<button class=\"ta-summary-card\" type=\"button\" aria-expanded=\"" + (isExpanded ? "true" : "false") + "\">" +
                "<span class=\"ta-summary-main\">" +
                    "<strong>" + escapeHtml(safeText(item.taName, "TA User")) + "</strong>" +
                    "<small>" + escapeHtml(t("portal.adminDashboard.acceptedJobs", "Accepted Jobs")) + " " + escapeHtml(String(toNumber(item.acceptedJobCount))) + "</small>" +
                "</span>" +
                "<span class=\"ta-summary-meter\" aria-hidden=\"true\">" +
                    "<i style=\"width:" + escapeHtml(String(percent)) + "%\"></i>" +
                "</span>" +
                "<span class=\"ta-summary-hours\">" +
                    "<strong>" + escapeHtml(formatNumber(totalHours)) + "</strong>" +
                    "<small>" + escapeHtml(t("portal.adminDashboard.hours", "hours")) + "</small>" +
                "</span>" +
                "<span class=\"ta-summary-action\">" + escapeHtml(isExpanded ? t("portal.adminDashboard.collapseDetails", "Hide details") : t("portal.adminDashboard.viewDetails", "View details")) + "</span>" +
            "</button>";

        element.querySelector("button").addEventListener("click", function () {
            if (isExpanded) {
                delete state.expandedTaIds[taId];
            } else {
                state.expandedTaIds[taId] = true;
            }
            renderTaCards(state.report.taWorkloads);
        });

        if (isExpanded) {
            element.appendChild(createTaDetail(item));
        }
        return element;
    }

    function createTaDetail(item) {
        var jobs = Array.isArray(item.jobs) ? item.jobs : [];
        var jobRows = jobs.map(function (job) {
            return "<li class=\"ta-job-row\">" +
                "<div>" +
                    "<strong>" + escapeHtml(safeText(job.jobTitle, "Untitled job")) + "</strong>" +
                    "<span>" + escapeHtml(safeText(job.courseCode, "-")) + "</span>" +
                "</div>" +
                "<p>" +
                    escapeHtml(formatNumber(job.weeklyHours)) + " " + escapeHtml(t("portal.adminDashboard.hoursPerWeek", "hours/week")) +
                    " - " + escapeHtml(String(toNumber(job.countedWeeks))) + " " + escapeHtml(t("portal.adminDashboard.weeks", "weeks")) +
                    " - " + escapeHtml(formatNumber(job.countedHours)) + " " + escapeHtml(t("portal.adminDashboard.hours", "hours")) +
                "</p>" +
                "<small>" + escapeHtml(safeText(job.workStartDate, "-")) + " - " + escapeHtml(safeText(job.workEndDate, "-")) + "</small>" +
            "</li>";
        }).join("");

        var element = document.createElement("div");
        element.className = "ta-workload-detail";
        element.innerHTML =
            "<div class=\"ta-item-stats\">" +
                "<p><span>" + escapeHtml(t("portal.adminDashboard.acceptedJobs", "Accepted Jobs")) + "</span><strong>" + escapeHtml(String(toNumber(item.acceptedJobCount))) + "</strong></p>" +
                "<p><span>" + escapeHtml(t("portal.adminDashboard.totalWorkWeeks", "Work Weeks")) + "</span><strong>" + escapeHtml(String(toNumber(item.totalWorkWeeks))) + "</strong></p>" +
                "<p><span>" + escapeHtml(t("portal.adminDashboard.totalWorkHours", "Total Work Hours")) + "</span><strong>" + escapeHtml(formatNumber(item.totalWorkHours)) + "</strong></p>" +
            "</div>" +
            "<ul class=\"ta-job-list\">" + jobRows + "</ul>";
        return element;
    }

    function filterTaWorkloads(taWorkloads, query) {
        var normalizedQuery = normalizeSearchText(query);
        if (!normalizedQuery) return taWorkloads;
        return taWorkloads.filter(function (item) {
            return buildSearchText(item).indexOf(normalizedQuery) !== -1;
        });
    }

    function buildSearchText(item) {
        var parts = [
            safeText(item.taName, ""),
            safeText(item.taId, ""),
            String(toNumber(item.acceptedJobCount)),
            formatNumber(item.totalWorkHours),
            formatNumber(item.totalWorkWeeks)
        ];
        var jobs = Array.isArray(item.jobs) ? item.jobs : [];
        jobs.forEach(function (job) {
            parts.push(
                safeText(job.jobTitle, ""),
                safeText(job.courseCode, ""),
                safeText(job.jobId, ""),
                formatNumber(job.weeklyHours),
                formatNumber(job.countedWeeks),
                formatNumber(job.countedHours),
                safeText(job.workStartDate, ""),
                safeText(job.workEndDate, "")
            );
        });
        return normalizeSearchText(parts.join(" "));
    }

    function normalizeSearchText(value) {
        return safeText(value, "").toLowerCase().replace(/\s+/g, " ").trim();
    }

    function pruneExpandedState(items) {
        var visibleIds = {};
        items.forEach(function (item) {
            visibleIds[safeText(item.taId, safeText(item.taName, ""))] = true;
        });
        Object.keys(state.expandedTaIds).forEach(function (taId) {
            if (!visibleIds[taId]) {
                delete state.expandedTaIds[taId];
            }
        });
    }

    function buildWorkloadSummary(pageCount, filteredCount, totalCount) {
        var summary = formatLoadedSummary(filteredCount, "portal.dynamic.taWorkloadItemUnit", "TA workload item");
        if (state.searchQuery) {
            summary += " " + t("portal.adminDashboard.filteredSummary", "Filtered from") + " " + totalCount + ".";
        }
        if (filteredCount > PAGE_SIZE) {
            summary += " " + t("portal.adminDashboard.pageSummaryPrefix", "Showing") + " " + pageCount + " / " + filteredCount + ".";
        }
        return summary;
    }

    function renderPagination(totalPages) {
        if (!paginationNode) return;
        paginationNode.innerHTML = "";
        if (totalPages <= 1) {
            hidePagination();
            return;
        }
        paginationNode.classList.remove("hidden");
        buildPaginationPages(totalPages, state.currentPage).forEach(function (page) {
            if (page === "...") {
                appendPageEllipsis();
            } else {
                appendPageButton(page);
            }
        });
    }

    function buildPaginationPages(totalPages, currentPage) {
        if (totalPages <= 7) {
            var allPages = [];
            for (var page = 1; page <= totalPages; page++) {
                allPages.push(page);
            }
            return allPages;
        }
        var pages = [1, 2];
        var start = Math.max(3, currentPage - 1);
        var end = Math.min(totalPages - 1, currentPage + 1);
        if (start > 3) pages.push("...");
        for (var middle = start; middle <= end; middle++) {
            pages.push(middle);
        }
        if (end < totalPages - 1) pages.push("...");
        pages.push(totalPages);
        return pages;
    }

    function appendPageButton(page) {
        var button = document.createElement("button");
        button.type = "button";
        button.className = "workload-page-btn" + (page === state.currentPage ? " is-active" : "");
        button.textContent = String(page);
        button.setAttribute("aria-label", t("portal.adminDashboard.pageButtonAria", "Page") + " " + page);
        if (page === state.currentPage) {
            button.setAttribute("aria-current", "page");
        }
        button.addEventListener("click", function () {
            if (state.currentPage === page) return;
            state.currentPage = page;
            renderTaCards(state.report.taWorkloads);
        });
        paginationNode.appendChild(button);
    }

    function appendPageEllipsis() {
        var ellipsis = document.createElement("span");
        ellipsis.className = "workload-page-ellipsis";
        ellipsis.textContent = "...";
        paginationNode.appendChild(ellipsis);
    }

    function hidePagination() {
        if (!paginationNode) return;
        paginationNode.innerHTML = "";
        paginationNode.classList.add("hidden");
    }

    function createEmptyState(copy) {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.noWorkloadDataYetTitle", "No workload data yet")) + "</p>" +
            "<p class=\"empty-copy\">" + escapeHtml(copy || t("portal.dynamic.adjustWorkRangeHint", "Adjust the work range or wait for accepted jobs to appear.")) + "</p>";
        return empty;
    }

    function normalizeReport(payload) {
        var data = payload && payload.data && typeof payload.data === "object" ? payload.data : payload;
        if (!data || typeof data !== "object") return EMPTY_REPORT;
        return {
            taWorkloads: Array.isArray(data.taWorkloads) ? data.taWorkloads : [],
            invalidJobs: Array.isArray(data.invalidJobs) ? data.invalidJobs : [],
            totalTaCount: toNumber(data.totalTaCount),
            totalAcceptedJobs: toNumber(data.totalAcceptedJobs),
            totalWorkWeeks: toNumber(data.totalWorkWeeks),
            totalWorkHours: toNumber(data.totalWorkHours)
        };
    }

    function normalizeApiResult(result, fallbackMessage) {
        var response = result.response || {};
        var payload = result.payload || {};
        if (response.status === 401) {
            return {
                unauthorized: true,
                ok: false,
                payload: null,
                message: t("portal.dynamic.sessionExpiredRedirect", "Session expired. Redirecting to login...")
            };
        }
        if (!response.ok || payload.success !== true) {
            return {
                unauthorized: false,
                ok: false,
                payload: payload,
                message: localizeServerMessage(payload.message, "portal.dynamic.failedLoadTaWorkloads", fallbackMessage)
            };
        }
        return { unauthorized: false, ok: true, payload: payload, message: "" };
    }

    function request(url, options) {
        if (window.TARecruitment && window.TARecruitment.api && typeof window.TARecruitment.api.request === "function") {
            return window.TARecruitment.api.request(url, options, { parser: parseJson, strictJson: false });
        }
        return fetch(url, options).then(function (response) {
            return response.text().then(function (text) {
                return { response: response, payload: parseJson(text) };
            });
        });
    }

    function parseJson(text) {
        try {
            return JSON.parse(text);
        } catch (error) {
            return {};
        }
    }

    function setLoadingState(loading) {
        if (!applyButton) return;
        applyButton.disabled = loading;
        applyButton.textContent = loading ? t("portal.common.loading", "Loading...") : t("portal.common.search", "Search");
    }

    function showMessage(message, type) {
        if (!messageNode) return;
        messageNode.textContent = message;
        messageNode.classList.remove("hidden", "error", "success");
        messageNode.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        if (!messageNode) return;
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

    function formatLoadedSummary(count, unitKey, fallbackUnit) {
        var summary = t("portal.dynamic.loaded", "Loaded") + " " + count + " " + t(unitKey, fallbackUnit);
        if (window.AppI18n && window.AppI18n.getLocale && window.AppI18n.getLocale() === "en" && count !== 1) {
            summary += "s";
        }
        return summary + ".";
    }

    function toNumber(value) {
        var number = Number(value);
        return isFinite(number) ? number : 0;
    }

    function formatNumber(value) {
        var number = toNumber(value);
        if (Math.abs(number - Math.round(number)) < 0.0001) {
            return String(Math.round(number));
        }
        return String(Math.round(number * 10) / 10);
    }

    function safeText(value, fallback) {
        if (typeof value === "string" && value.trim()) return value.trim();
        if (typeof value === "number") return formatNumber(value);
        return typeof fallback === "string" ? fallback : "";
    }

    function escapeHtml(value) {
        if (typeof value !== "string") return "";
        return value
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function t(key, fallback) {
        if (i18n) return i18n.t(key, fallback);
        return fallback || key;
    }

    function localizeServerMessage(message, fallbackKey, fallbackText) {
        if (window.AppI18n && typeof window.AppI18n.localizeServerMessage === "function") {
            return window.AppI18n.localizeServerMessage(message, fallbackKey, fallbackText);
        }
        if (typeof message === "string" && message.trim()) return message.trim();
        return fallbackKey ? t(fallbackKey, fallbackText) : (fallbackText || "");
    }
})();

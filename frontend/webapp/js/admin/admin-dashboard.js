/*
 * Admin dashboard 工作量统计脚本，对应 /jsp/admin/dashboard.jsp。
 *
 * 读取 /api/admin/workload-statistics，把后端统计好的 TA 工作量做搜索、分页和展开详情。
 * 统计口径不在前端计算，前端只处理展示状态。
 */
(function () {
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
        // report 是后端完整结果；searchQuery/currentPage 只影响当前浏览视图。
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

    /*
     * 拉取并渲染后台工作量统计。
     */
    function loadDashboard() {
        if (state.loading) return Promise.resolve();

        state.loading = true;
        setLoadingState(true);
        hideMessage();
        taSummaryNode.textContent = t("portal.adminDashboard.loadingWorkload", "Loading workload...");
        taListNode.innerHTML = "";

        return fetchWorkloadReport().then(function (result) {
            if (result.unauthorized) {
                handleUnauthorized();
                return;
            }
            if (!result.ok) {
                showMessage(localizeServerMessage(result.message, "portal.dynamic.failedLoadTaWorkloads", "Failed to load TA workloads."), "error");
                state.report = EMPTY_REPORT;
            } else {
                state.report = normalizeReport(result.payload);
            }
            renderDashboard(state.report);
        }).catch(function () {
            showMessage(t("portal.dynamic.networkErrorLoadingDashboard", "Network error while loading dashboard."), "error");
            state.report = EMPTY_REPORT;
            renderDashboard(state.report);
        }).finally(function () {
            state.loading = false;
            setLoadingState(false);
        });
    }

    /*
     * 应用 TA 搜索关键词，只影响前端当前列表，不重新请求后端。
     */
    function applySearch() {
        state.searchQuery = searchInput.value.trim();
        state.currentPage = 1;
        renderDashboard(state.report);
    }

    /*
     * 请求 /api/admin/workload-statistics。
     */
    function fetchWorkloadReport() {
        // 当前前端只展示 TA workload；其它 mode 已在后端标为遗留预留。
        return request(window.TARecruitment.routes.admin.workloadStatistics(), {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (result) {
            return normalizeApiResult(result, "TA workload request failed.");
        });
    }

    /*
     * 渲染 dashboard 主体，目前只展示 TA 工作量卡片。
     */
    function renderDashboard(report) {
        renderTaCards(report.taWorkloads);
    }

    /*
     * 搜索、排序、分页后渲染 TA 工作量卡片。
     */
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
        if (state.currentPage > totalPages) {
            state.currentPage = totalPages;
        }
        if (state.currentPage < 1) {
            state.currentPage = 1;
        }
        var pageStart = (state.currentPage - 1) * PAGE_SIZE;
        var pageItems = sorted.slice(pageStart, pageStart + PAGE_SIZE);

        taSummaryNode.textContent = buildWorkloadSummary(pageItems.length, sorted.length, taWorkloads.length);
        pageItems.forEach(function (item) {
            taListNode.appendChild(createTaCard(item, maxValue));
        });
        renderPagination(totalPages);
    }

    /*
     * 创建单个 TA 工作量卡片。
     * maxValue 用于计算横向进度条比例，不改变真实小时数。
     */
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

        var button = element.querySelector("button");
        button.addEventListener("click", function () {
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

    /*
     * 创建展开详情：列出该 TA 被统计的已接受岗位。
     */
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
                    " · " + escapeHtml(String(toNumber(job.countedWeeks))) + " " + escapeHtml(t("portal.adminDashboard.weeks", "weeks")) +
                    " · " + escapeHtml(formatNumber(job.countedHours)) + " " + escapeHtml(t("portal.adminDashboard.hours", "hours")) +
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

    /*
     * 在前端做轻量搜索：后端仍返回完整统计，关键词只影响当前列表视图。
     */
    function filterTaWorkloads(taWorkloads, query) {
        var normalizedQuery = normalizeSearchText(query);
        if (!normalizedQuery) {
            return taWorkloads;
        }
        return taWorkloads.filter(function (item) {
            return buildSearchText(item).indexOf(normalizedQuery) !== -1;
        });
    }

    /*
     * 把 TA 名称、岗位、课程号和统计数字拼成一个可搜索字符串。
     */
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

    /*
     * 搜索统一小写并折叠空白，避免中英文空格差异影响匹配。
     */
    function normalizeSearchText(value) {
        return safeText(value, "").toLowerCase().replace(/\s+/g, " ").trim();
    }

    /*
     * 搜索或分页后，移除已经不可见的展开状态，避免下次回到列表时展开错卡片。
     */
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

    /*
     * 构造列表上方的加载摘要，区分“总数”“过滤后数量”和“当前页数量”。
     */
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

    /*
     * 渲染分页控件；分页只在浏览器端切片，不重新请求 /api/admin/workload-statistics。
     */
    function renderPagination(totalPages) {
        if (!paginationNode) {
            return;
        }
        paginationNode.innerHTML = "";
        if (totalPages <= 1) {
            hidePagination();
            return;
        }
        paginationNode.classList.remove("hidden");

        var pages = buildPaginationPages(totalPages, state.currentPage);
        pages.forEach(function (page) {
            if (page === "...") {
                appendPageEllipsis();
            } else {
                appendPageButton(page);
            }
        });
    }

    /*
     * 页数较多时压缩中间页码，保持控件宽度稳定。
     */
    function buildPaginationPages(totalPages, currentPage) {
        if (totalPages <= 7) {
            var simplePages = [];
            for (var page = 1; page <= totalPages; page++) {
                simplePages.push(page);
            }
            return simplePages;
        }
        var pages = [1, 2];
        var start = Math.max(3, currentPage - 1);
        var end = Math.min(totalPages - 1, currentPage + 1);
        if (start > 3) {
            pages.push("...");
        }
        for (var middle = start; middle <= end; middle++) {
            pages.push(middle);
        }
        if (end < totalPages - 1) {
            pages.push("...");
        }
        pages.push(totalPages);
        return pages;
    }

    /*
     * 单个分页按钮会更新 state.currentPage，并复用当前 report 重新渲染。
     */
    function appendPageButton(page) {
        if (!paginationNode) {
            return;
        }
        var button = document.createElement("button");
        button.type = "button";
        button.className = "workload-page-btn" + (page === state.currentPage ? " is-active" : "");
        button.textContent = String(page);
        button.setAttribute("aria-label", t("portal.adminDashboard.pageButtonAria", "Page") + " " + page);
        if (page === state.currentPage) {
            button.setAttribute("aria-current", "page");
        }
        button.addEventListener("click", function () {
            if (state.currentPage === page) {
                return;
            }
            state.currentPage = page;
            renderTaCards(state.report.taWorkloads);
        });
        paginationNode.appendChild(button);
    }

    /*
     * 分页省略号只是视觉占位，不绑定点击事件。
     */
    function appendPageEllipsis() {
        if (!paginationNode) {
            return;
        }
        var ellipsis = document.createElement("span");
        ellipsis.className = "workload-page-ellipsis";
        ellipsis.textContent = "...";
        paginationNode.appendChild(ellipsis);
    }

    /*
     * 空列表或单页数据时隐藏分页，避免旧页码残留在页面上。
     */
    function hidePagination() {
        if (!paginationNode) {
            return;
        }
        paginationNode.innerHTML = "";
        paginationNode.classList.add("hidden");
    }

    /*
     * 统一空状态结构，文案由调用方决定是无数据还是无搜索结果。
     */
    function createEmptyState(copy) {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">" + escapeHtml(t("portal.dynamic.noWorkloadDataYetTitle", "No workload data yet")) + "</p>" +
            "<p class=\"empty-copy\">" + escapeHtml(copy || t("portal.dynamic.adjustWorkRangeHint", "Adjust the work range or wait for accepted jobs to appear.")) + "</p>";
        return empty;
    }

    /*
     * 后端标准响应是 {success, data}；这里兼容直接传 data 的单测/旧调用形态。
     */
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

    /*
     * 把公共 request 的 response/payload 转成页面更好判断的结果对象。
     */
    function normalizeApiResult(result, fallbackMessage) {
        var response = result.response;
        var payload = result.payload;
        if (response.status === 401) {
            return { unauthorized: true, ok: false, payload: null, message: localizeServerMessage("Please login first.", "portal.dynamic.sessionExpiredRedirect", "Session expired. Redirecting to login...") };
        }
        if (!response.ok || !payload || payload.success !== true) {
            return {
                unauthorized: false,
                ok: false,
                payload: payload || null,
                message: localizeServerMessage(payload && payload.message, "", fallbackMessage)
            };
        }
        return { unauthorized: false, ok: true, payload: payload, message: "" };
    }

    /*
     * 控制搜索按钮加载态，避免请求未完成时重复触发。
     */
    function setLoadingState(loading) {
        if (applyButton) {
            applyButton.disabled = loading;
            applyButton.textContent = loading ? t("portal.common.loading", "Loading...") : t("portal.common.search", "Search");
        }
    }

    /*
     * 页面请求优先走 TARecruitment.api.request，保留 fetch 作为公共脚本未加载时的兜底。
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

    /*
     * 后端异常页或空响应不会打断渲染，统一解析成空对象后交给 normalizeApiResult。
     */
    function parseJson(text) {
        try {
            return JSON.parse(text);
        } catch (e) {
            return {};
        }
    }

    /*
     * 顶部消息区只显示当前 dashboard 请求状态。
     */
    function showMessage(message, type) {
        if (!messageNode) return;
        messageNode.textContent = message;
        messageNode.classList.remove("hidden", "error", "success");
        messageNode.classList.add(type === "success" ? "success" : "error");
    }

    /*
     * 请求前清空旧提示，防止成功数据和旧错误同时出现。
     */
    function hideMessage() {
        if (!messageNode) return;
        messageNode.textContent = "";
        messageNode.classList.remove("error", "success");
        messageNode.classList.add("hidden");
    }

    /*
     * 管理员会话失效时回登录页，路径通过 contextPath 兼容 /groupproject 部署。
     */
    function handleUnauthorized() {
        showMessage(t("portal.dynamic.sessionExpiredRedirect", "Session expired. Redirecting to login..."), "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
    }

    /*
     * 统一 loaded 摘要，并在英文环境下补复数。
     */
    function formatLoadedSummary(count, unitKey, fallbackUnit) {
        var summary = t("portal.dynamic.loaded", "Loaded") + " " + count + " " + t(unitKey, fallbackUnit);
        if (window.AppI18n && window.AppI18n.getLocale && window.AppI18n.getLocale() === "en" && count !== 1) {
            summary += "s";
        }
        return summary + ".";
    }

    /*
     * 后端 CSV 统计值可能以字符串返回，渲染前统一转成安全数字。
     */
    function toNumber(value) {
        var number = Number(value);
        return isFinite(number) ? number : 0;
    }

    /*
     * 展示小时数：整数不带小数，非整数保留一位，避免卡片数字过长。
     */
    function formatNumber(value) {
        var number = toNumber(value);
        if (Math.abs(number - Math.round(number)) < 0.0001) {
            return String(Math.round(number));
        }
        return String(Math.round(number * 10) / 10);
    }

    /*
     * 渲染文本前做空值兜底，避免 null/undefined 直接进入 DOM。
     */
    function safeText(value, fallback) {
        if (typeof value === "string" && value.trim()) return value.trim();
        if (typeof value === "number") return formatNumber(value);
        return typeof fallback === "string" ? fallback : "";
    }

    /*
     * 所有通过 innerHTML 拼接的后端字段必须先转义。
     */
    function escapeHtml(value) {
        if (typeof value !== "string") return "";
        return value
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    /*
     * i18n 不可用时使用英文兜底，保证页面仍可读。
     */
    function t(key, fallback) {
        if (i18n) return i18n.t(key, fallback);
        return fallback || key;
    }

    /*
     * 后端 message 先尝试按 i18n 字典映射，映射不到再显示原文或兜底文案。
     */
    function localizeServerMessage(message, fallbackKey, fallbackText) {
        if (window.AppI18n && typeof window.AppI18n.localizeServerMessage === "function") {
            return window.AppI18n.localizeServerMessage(message, fallbackKey, fallbackText);
        }
        if (typeof message === "string" && message.trim()) return message.trim();
        return fallbackKey ? t(fallbackKey, fallbackText) : (fallbackText || "");
    }
})();

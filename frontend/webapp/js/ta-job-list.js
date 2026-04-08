(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentRole = typeof window.APP_CURRENT_ROLE === "string" ? window.APP_CURRENT_ROLE.trim().toUpperCase() : "";

    var filterForm = document.getElementById("job-filter-form");
    var keywordInput = document.getElementById("keyword");
    var statusSelect = document.getElementById("status");
    var courseCodeInput = document.getElementById("course-code");
    var refreshButton = document.getElementById("refresh-jobs-btn");
    var clearButton = document.getElementById("clear-filters-btn");
    var listMessage = document.getElementById("list-message");
    var listSummary = document.getElementById("job-list-summary");
    var jobList = document.getElementById("job-list");
    var searchButton = document.getElementById("search-btn");

    if (!filterForm || !jobList || !listSummary) {
        return;
    }

    var state = {
        loading: false,
        jobs: [],
        loadError: false
    };

    filterForm.addEventListener("submit", function (event) {
        event.preventDefault();
        loadJobs();
    });

    if (refreshButton) {
        refreshButton.addEventListener("click", function () {
            loadJobs();
        });
    }

    if (clearButton) {
        clearButton.addEventListener("click", function () {
            keywordInput.value = "";
            statusSelect.value = "";
            courseCodeInput.value = "";
            hideMessage();
            loadJobs();
        });
    }

    loadJobs();

    function loadJobs() {
        if (state.loading) {
            return;
        }

        setLoading(true);
        state.loadError = false;
        hideMessage();

        request(buildJobsUrl(), {
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
                    var errorMessage = "Unable to load jobs right now. Please try again.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    state.loadError = true;
                    renderJobs([]);
                    return;
                }

                var jobs = Array.isArray(payload.jobs) ? payload.jobs : [];
                state.jobs = jobs.slice();
                renderJobs(jobs);
            })
            .catch(function () {
                showMessage("Network error. Please try again in a moment.", "error");
                state.loadError = true;
                renderJobs([]);
            })
            .finally(function () {
                setLoading(false);
            });
    }

    function buildJobsUrl() {
        var params = new URLSearchParams();
        var keyword = keywordInput.value.trim();
        var status = statusSelect.value.trim();
        var courseCode = courseCodeInput.value.trim();

        if (keyword) {
            params.set("keyword", keyword);
        }
        if (status) {
            params.set("status", status);
        }
        if (courseCode) {
            params.set("courseCode", courseCode);
        }

        var queryString = params.toString();
        return contextPath + "/jobs" + (queryString ? "?" + queryString : "");
    }

    function renderJobs(jobs) {
        jobList.innerHTML = "";

        if (state.loadError) {
            listSummary.textContent = "Unable to load jobs right now.";
            jobList.appendChild(createEmptyState("load-error"));
            return;
        }

        if (!Array.isArray(jobs) || jobs.length === 0) {
            var hasFilters = !!keywordInput.value.trim() || !!statusSelect.value.trim() || !!courseCodeInput.value.trim();
            if (hasFilters) {
                listSummary.textContent = "No jobs found for the current filters.";
                jobList.appendChild(createEmptyState("no-match"));
                return;
            }
            listSummary.textContent = "No jobs available right now.";
            jobList.appendChild(createEmptyState("no-jobs"));
            return;
        }

        listSummary.textContent = "Showing " + jobs.length + " job" + (jobs.length > 1 ? "s" : "") + ".";

        jobs.forEach(function (job) {
            jobList.appendChild(createJobCard(job));
        });
    }

    function createJobCard(job) {
        var article = document.createElement("article");
        article.className = "job-card";

        var status = getSafeText(job.status || "OPEN").toUpperCase();
        var canApply = currentRole === "TA" && status === "OPEN";
        var detailHref = contextPath + "/jsp/ta/job-detail.jsp?id=" + encodeURIComponent(getSafeText(job.jobId));

        var tagsHtml = buildTagItems(job);

        article.innerHTML =
            "<header class=\"job-card-header\">" +
                "<div class=\"job-heading\">" +
                    "<h2>" + escapeHtml(getSafeText(job.title, "Untitled position")) + "</h2>" +
                    "<p>" + escapeHtml(getSafeText(job.courseCode, "-")) +
                        (job.courseName ? " · " + escapeHtml(job.courseName) : "") + "</p>" +
                "</div>" +
                "<span class=\"status-pill status-" + escapeHtml(status.toLowerCase()) + "\">" + escapeHtml(status) + "</span>" +
            "</header>" +
            "<div class=\"job-meta\">" +
                "<p><span class=\"meta-label\">MO</span><span class=\"meta-value\">" + escapeHtml(getSafeText(job.moName, "-")) + "</span></p>" +
                "<p><span class=\"meta-label\">Positions</span><span class=\"meta-value\">" + escapeHtml(String(job.positions || 0)) + "</span></p>" +
                "<p><span class=\"meta-label\">Deadline</span><span class=\"meta-value\">" + escapeHtml(formatDateTime(job.deadline)) + "</span></p>" +
            "</div>" +
            "<div class=\"job-tags\">" + tagsHtml + "</div>" +
            "<div class=\"job-card-actions\">" +
                "<a class=\"primary-link\" href=\"" + detailHref + "\">View details</a>" +
                (canApply ? "<a class=\"ghost-link\" href=\"" + detailHref + "#apply\">Apply now</a>" : "") +
            "</div>";

        return article;
    }

    function buildTagItems(job) {
        var tags = [];
        if (job.salary) {
            tags.push({ label: "Salary", value: getSafeText(job.salary) });
        }
        if (job.workload) {
            tags.push({ label: "Workload", value: getSafeText(job.workload) });
        }
        if (job.requiredSkills) {
            tags.push({ label: "Skills", value: normalizeSkills(job.requiredSkills) });
        }

        if (tags.length === 0) {
            return "<span class=\"tag-item muted\">No extra tags</span>";
        }

        return tags.map(function (tag) {
            return "<span class=\"tag-item\"><strong>" + escapeHtml(tag.label) + ":</strong> " + escapeHtml(tag.value) + "</span>";
        }).join("");
    }

    function createEmptyState(mode) {
        var empty = document.createElement("div");
        empty.className = "empty-state";

        if (mode === "load-error") {
            empty.innerHTML =
                "<p class=\"empty-title\">Unable to load positions</p>" +
                "<p class=\"empty-copy\">Please refresh the list after checking your network connection.</p>";
            return empty;
        }

        if (mode === "no-jobs") {
            empty.innerHTML =
                "<p class=\"empty-title\">No positions published yet</p>" +
                "<p class=\"empty-copy\">When MO publishes new jobs, they will appear here.</p>";
            return empty;
        }

        empty.innerHTML =
            "<p class=\"empty-title\">No matching positions</p>" +
            "<p class=\"empty-copy\">Try broadening your keyword or clearing one filter.</p>";
        return empty;
    }

    function setLoading(loading) {
        state.loading = loading;
        var loadingText = loading ? "Loading..." : "Apply filters";
        if (searchButton) {
            searchButton.disabled = loading;
            searchButton.textContent = loadingText;
        }
        if (refreshButton) {
            refreshButton.disabled = loading;
        }
        if (clearButton) {
            clearButton.disabled = loading;
        }
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
        showMessage("Your session has expired. Redirecting to login...", "error");
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
            return "-";
        }
        return rawSkills
            .split(/[;,]/)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            })
            .join(", ");
    }

    function formatDateTime(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "-";
        }

        var date = new Date(value);
        if (isNaN(date.getTime())) {
            return value;
        }

        var year = date.getFullYear();
        var month = pad2(date.getMonth() + 1);
        var day = pad2(date.getDate());
        var hour = pad2(date.getHours());
        var minute = pad2(date.getMinutes());
        return year + "-" + month + "-" + day + " " + hour + ":" + minute;
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

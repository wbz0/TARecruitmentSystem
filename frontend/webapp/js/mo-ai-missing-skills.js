(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentUserId = typeof window.APP_CURRENT_USER_ID === "string" ? window.APP_CURRENT_USER_ID.trim() : "";

    var filterForm = document.getElementById("gap-filter-form");
    var jobFilter = document.getElementById("gap-job-filter");
    var loadButton = document.getElementById("load-gap-btn");
    var refreshButton = document.getElementById("refresh-gap-btn");
    var messageNode = document.getElementById("gap-message");
    var listSummaryNode = document.getElementById("gap-list-summary");
    var listNode = document.getElementById("gap-list");

    var summaryNodes = {
        applicantCount: document.getElementById("gap-applicant-count"),
        requiredCount: document.getElementById("gap-required-count"),
        uniqueCount: document.getElementById("gap-unique-count")
    };

    if (!filterForm || !jobFilter || !listNode || !listSummaryNode) {
        return;
    }

    var state = {
        loading: false,
        loadingJobs: false,
        jobs: []
    };

    filterForm.addEventListener("submit", function (event) {
        event.preventDefault();
        loadGaps();
    });

    if (refreshButton) {
        refreshButton.addEventListener("click", function () {
            loadJobs().then(function () {
                loadGaps();
            });
        });
    }

    loadJobs();

    function loadJobs() {
        if (state.loadingJobs) {
            return Promise.resolve();
        }
        state.loadingJobs = true;
        hideMessage();

        var url = contextPath + "/jobs";
        if (currentUserId) {
            url += "?moId=" + encodeURIComponent(currentUserId);
        }

        return request(url, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (result) {
            var response = result.response;
            var payload = result.payload;

            if (response.status === 401) {
                handleUnauthorized();
                return;
            }
            if (!response.ok || !payload || payload.success !== true) {
                showMessage("Unable to load jobs for gap analysis.", "error");
                renderJobOptions([]);
                return;
            }
            state.jobs = Array.isArray(payload.jobs) ? payload.jobs : [];
            renderJobOptions(state.jobs);
        }).catch(function () {
            showMessage("Network error while loading jobs.", "error");
            renderJobOptions([]);
        }).finally(function () {
            state.loadingJobs = false;
        });
    }

    function renderJobOptions(jobs) {
        var previous = jobFilter.value;
        jobFilter.innerHTML = "<option value=\"\">Select a job</option>";
        jobs.forEach(function (job) {
            var option = document.createElement("option");
            option.value = safeText(job.jobId, "");
            option.textContent = buildJobLabel(job);
            jobFilter.appendChild(option);
        });
        var hasPrevious = Array.prototype.some.call(jobFilter.options, function (option) {
            return option.value === previous;
        });
        if (hasPrevious) {
            jobFilter.value = previous;
        }
    }

    function loadGaps() {
        if (state.loading) {
            return Promise.resolve();
        }
        var selectedJobId = jobFilter.value.trim();
        if (!selectedJobId) {
            resetSummary();
            renderList([]);
            listSummaryNode.textContent = "Choose a job to load missing skills insights.";
            return Promise.resolve();
        }

        state.loading = true;
        setLoading(true);
        hideMessage();
        listSummaryNode.textContent = "Loading missing skills data...";
        listNode.innerHTML = "";

        var url = contextPath + "/api/mo/missing-skills?jobId=" + encodeURIComponent(selectedJobId);
        return request(url, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (result) {
            var response = result.response;
            var payload = result.payload;

            if (response.status === 401) {
                handleUnauthorized();
                resetSummary();
                renderList([]);
                return;
            }

            if (!response.ok || !payload || payload.success !== true) {
                var msg = "Unable to load missing skills data.";
                if (payload && typeof payload.message === "string" && payload.message.trim()) {
                    msg = payload.message.trim();
                }
                showMessage(msg, "error");
                resetSummary();
                renderList([]);
                return;
            }

            var frequency = Array.isArray(payload.missingSkillFrequency) ? payload.missingSkillFrequency : [];
            var applicantCount = Number(payload.applicantCount || 0);
            var requiredCount = Number(payload.requiredSkillCount || 0);
            updateSummary(applicantCount, requiredCount, frequency.length);
            renderList(frequency);
        }).catch(function () {
            showMessage("Network error while loading missing skills data.", "error");
            resetSummary();
            renderList([]);
        }).finally(function () {
            state.loading = false;
            setLoading(false);
        });
    }

    function updateSummary(applicantCount, requiredCount, uniqueCount) {
        summaryNodes.applicantCount.textContent = String(applicantCount);
        summaryNodes.requiredCount.textContent = String(requiredCount);
        summaryNodes.uniqueCount.textContent = String(uniqueCount);
    }

    function resetSummary() {
        updateSummary(0, 0, 0);
    }

    function renderList(frequency) {
        listNode.innerHTML = "";
        if (!frequency.length) {
            listSummaryNode.textContent = "No missing skills found for selected data.";
            listNode.appendChild(createEmptyState());
            return;
        }

        listSummaryNode.textContent = "Found " + frequency.length + " gap skill" + (frequency.length > 1 ? "s" : "") + ".";
        frequency.forEach(function (item, index) {
            listNode.appendChild(createGapCard(item, index));
        });
    }

    function createGapCard(item, index) {
        var card = document.createElement("article");
        card.className = "gap-card";
        var skill = safeText(item.skill, "Unknown Skill");
        var count = Number(item.count || 0);
        var recommendation = buildRecommendation(skill, count, index);
        card.innerHTML =
            "<header class=\"gap-card-header\">" +
                "<h3>" + escapeHtml(skill) + "</h3>" +
                "<span class=\"gap-count-badge\">" + escapeHtml(String(count)) + " applicant(s)</span>" +
            "</header>" +
            "<p class=\"gap-meta\">This skill appears as a repeated gap across applicants for the selected job.</p>" +
            "<div class=\"gap-recommend\"><p>" + escapeHtml(recommendation) + "</p></div>";
        return card;
    }

    function buildRecommendation(skill, count, rankIndex) {
        if (rankIndex === 0) {
            return "Top priority: add a focused screening question and onboarding plan for \"" + skill + "\".";
        }
        if (count >= 3) {
            return "Recommend creating a short training module for \"" + skill + "\" before interview rounds.";
        }
        return "Consider a quick practical check for \"" + skill + "\" during candidate review.";
    }

    function createEmptyState() {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">No gap skills available</p>" +
            "<p class=\"empty-copy\">When applicants and job data are ready, this panel will show missing skills insights.</p>";
        return empty;
    }

    function setLoading(loading) {
        if (loadButton) {
            loadButton.disabled = loading;
            loadButton.textContent = loading ? "Loading..." : "Load gaps";
        }
        if (refreshButton) {
            refreshButton.disabled = loading;
        }
    }

    function showMessage(message, type) {
        if (!messageNode) {
            return;
        }
        messageNode.textContent = message;
        messageNode.classList.remove("hidden", "error", "success");
        messageNode.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        if (!messageNode) {
            return;
        }
        messageNode.textContent = "";
        messageNode.classList.remove("error", "success");
        messageNode.classList.add("hidden");
    }

    function buildJobLabel(job) {
        var title = safeText(job.title, "Untitled");
        var courseCode = safeText(job.courseCode, "");
        if (!courseCode) {
            return title;
        }
        return title + " (" + courseCode + ")";
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

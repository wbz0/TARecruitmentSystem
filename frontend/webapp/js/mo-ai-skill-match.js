(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentUserId = typeof window.APP_CURRENT_USER_ID === "string" ? window.APP_CURRENT_USER_ID.trim() : "";

    var filterForm = document.getElementById("match-filter-form");
    var jobFilter = document.getElementById("job-filter");
    var loadButton = document.getElementById("load-match-btn");
    var refreshButton = document.getElementById("refresh-match-btn");
    var messageNode = document.getElementById("match-message");
    var listSummaryNode = document.getElementById("match-list-summary");
    var listNode = document.getElementById("match-list");

    var summaryNodes = {
        total: document.getElementById("summary-total"),
        high: document.getElementById("summary-high"),
        medium: document.getElementById("summary-medium"),
        low: document.getElementById("summary-low")
    };
    var visualNodes = {
        averageRing: document.getElementById("average-ring"),
        averageText: document.getElementById("average-score-text"),
        distHigh: document.getElementById("dist-high"),
        distMedium: document.getElementById("dist-medium"),
        distLow: document.getElementById("dist-low"),
        distHighLabel: document.getElementById("dist-high-label"),
        distMediumLabel: document.getElementById("dist-medium-label"),
        distLowLabel: document.getElementById("dist-low-label")
    };

    if (!filterForm || !jobFilter || !listNode || !listSummaryNode) {
        return;
    }

    var state = {
        loadingJobs: false,
        loadingMatches: false,
        jobs: [],
        selectedJob: null,
        matches: []
    };

    filterForm.addEventListener("submit", function (event) {
        event.preventDefault();
        loadMatches();
    });

    if (refreshButton) {
        refreshButton.addEventListener("click", function () {
            loadJobs().then(function () {
                loadMatches();
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
                showMessage("Unable to load jobs for matching.", "error");
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

    function loadMatches() {
        if (state.loadingMatches) {
            return Promise.resolve();
        }
        var selectedJobId = jobFilter.value.trim();
        if (!selectedJobId) {
            state.selectedJob = null;
            state.matches = [];
            renderSummary([]);
            renderList([]);
            listSummaryNode.textContent = "Choose a job to load skill match results.";
            return Promise.resolve();
        }

        state.selectedJob = findJobById(selectedJobId);
        state.loadingMatches = true;
        setLoading(true);
        hideMessage();
        listSummaryNode.textContent = "Loading match results...";
        listNode.innerHTML = "";

        var url = contextPath + "/apply?jobId=" + encodeURIComponent(selectedJobId);
        return request(url, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (result) {
            var response = result.response;
            var payload = result.payload;

            if (response.status === 401) {
                handleUnauthorized();
                state.matches = [];
                renderSummary([]);
                renderList([]);
                return;
            }

            if (!response.ok || !payload || payload.success !== true) {
                var msg = "Unable to load application matches.";
                if (payload && typeof payload.message === "string" && payload.message.trim()) {
                    msg = payload.message.trim();
                }
                showMessage(msg, "error");
                state.matches = [];
                renderSummary([]);
                renderList([]);
                return;
            }

            var applications = Array.isArray(payload.applications) ? payload.applications : [];
            state.matches = applications.map(function (application) {
                return buildMatchModel(application, state.selectedJob);
            });
            renderSummary(state.matches);
            renderList(state.matches);
        }).catch(function () {
            showMessage("Network error while loading match data.", "error");
            state.matches = [];
            renderSummary([]);
            renderList([]);
        }).finally(function () {
            state.loadingMatches = false;
            setLoading(false);
        });
    }

    function buildMatchModel(application, job) {
        var requiredSkills = parseRequiredSkills(job && job.requiredSkills);
        var coverText = safeText(application.coverLetter, "");
        var score = estimateMatchScore(requiredSkills, coverText);
        return {
            applicationId: safeText(application.applicationId, ""),
            applicantId: safeText(application.applicantId, ""),
            applicantName: safeText(application.applicantName, "Unknown applicant"),
            applicantEmail: safeText(application.applicantEmail, "-"),
            status: safeText(application.status, "PENDING").toUpperCase(),
            appliedAt: safeText(application.appliedAt, ""),
            courseCode: safeText(application.courseCode, "-"),
            jobTitle: safeText(application.jobTitle, "Untitled position"),
            requiredSkills: requiredSkills,
            score: score,
            scoreLevel: score >= 85 ? "high" : (score >= 60 ? "medium" : "low"),
            coverLetter: coverText
        };
    }

    function parseRequiredSkills(raw) {
        if (typeof raw !== "string" || !raw.trim()) {
            return [];
        }
        return raw.split(/[;,]/).map(function (item) {
            return item.trim();
        }).filter(function (item) {
            return item.length > 0;
        });
    }

    function estimateMatchScore(requiredSkills, coverText) {
        if (!requiredSkills.length) {
            return 100;
        }
        var lowerCover = coverText.toLowerCase();
        var matched = 0;
        requiredSkills.forEach(function (skill) {
            var skillLower = skill.toLowerCase();
            if (lowerCover.indexOf(skillLower) >= 0) {
                matched += 1;
            }
        });
        var base = Math.round((matched * 100) / requiredSkills.length);
        if (matched === 0 && coverText.length > 0) {
            return 35;
        }
        return Math.max(0, Math.min(100, base));
    }

    function renderSummary(matches) {
        var total = matches.length;
        var high = 0;
        var medium = 0;
        var low = 0;
        var sumScore = 0;
        matches.forEach(function (match) {
            sumScore += match.score;
            if (match.score >= 85) {
                high++;
            } else if (match.score >= 60) {
                medium++;
            } else {
                low++;
            }
        });
        summaryNodes.total.textContent = String(total);
        summaryNodes.high.textContent = String(high);
        summaryNodes.medium.textContent = String(medium);
        summaryNodes.low.textContent = String(low);

        var average = total > 0 ? Math.round(sumScore / total) : 0;
        updateAverageRing(average);
        updateDistribution(total, high, medium, low);
    }

    function renderList(matches) {
        listNode.innerHTML = "";
        if (!matches.length) {
            listSummaryNode.textContent = "No applicants found for selected job.";
            listNode.appendChild(createEmptyState());
            return;
        }

        listSummaryNode.textContent = "Showing " + matches.length + " applicant match result" + (matches.length > 1 ? "s" : "") + ".";
        matches.forEach(function (match) {
            listNode.appendChild(createMatchCard(match));
        });
    }

    function createMatchCard(match) {
        var card = document.createElement("article");
        card.className = "match-card";
        var skillsPreview = buildSkillPreview(match.requiredSkills, match.coverLetter);
        var ringStyle = buildRingStyle(match.score);
        card.innerHTML =
            "<header class=\"match-card-header\">" +
                "<div class=\"match-card-heading\">" +
                    "<h3>" + escapeHtml(match.applicantName) + "</h3>" +
                    "<p>" + escapeHtml(match.applicantEmail) + "</p>" +
                "</div>" +
                "<div class=\"score-ring " + escapeHtml(match.scoreLevel) + "\" style=\"" + escapeHtml(ringStyle) + "\">" +
                    "<strong>" + escapeHtml(String(match.score)) + "%</strong>" +
                "</div>" +
            "</header>" +
            "<div class=\"match-meta\">" +
                "<p><span>Job</span><strong>" + escapeHtml(match.jobTitle) + "</strong></p>" +
                "<p><span>Course</span><strong>" + escapeHtml(match.courseCode) + "</strong></p>" +
                "<p><span>Status</span><strong>" + escapeHtml(match.status) + "</strong></p>" +
            "</div>" +
            "<div class=\"skills-preview\">" + skillsPreview + "</div>";
        return card;
    }

    function updateAverageRing(score) {
        if (!visualNodes.averageRing || !visualNodes.averageText) {
            return;
        }
        var percent = Math.max(0, Math.min(100, score));
        visualNodes.averageText.textContent = percent + "%";
        visualNodes.averageRing.style.background = buildConicGradient("#0071e3", percent);
    }

    function updateDistribution(total, high, medium, low) {
        if (total <= 0) {
            setDistributionVisual(0, 0, 0);
            return;
        }
        var highPct = Math.round((high * 100) / total);
        var mediumPct = Math.round((medium * 100) / total);
        var lowPct = Math.max(0, 100 - highPct - mediumPct);
        setDistributionVisual(highPct, mediumPct, lowPct);
    }

    function setDistributionVisual(highPct, mediumPct, lowPct) {
        if (visualNodes.distHigh) {
            visualNodes.distHigh.style.width = highPct + "%";
        }
        if (visualNodes.distMedium) {
            visualNodes.distMedium.style.width = mediumPct + "%";
        }
        if (visualNodes.distLow) {
            visualNodes.distLow.style.width = lowPct + "%";
        }
        if (visualNodes.distHighLabel) {
            visualNodes.distHighLabel.textContent = highPct + "%";
        }
        if (visualNodes.distMediumLabel) {
            visualNodes.distMediumLabel.textContent = mediumPct + "%";
        }
        if (visualNodes.distLowLabel) {
            visualNodes.distLowLabel.textContent = lowPct + "%";
        }
    }

    function buildRingStyle(score) {
        var color = "#0071e3";
        if (score >= 85) {
            color = "#1f7a34";
        } else if (score >= 60) {
            color = "#0d4d8c";
        } else {
            color = "#b42332";
        }
        return "background: " + buildConicGradient(color, score) + ";";
    }

    function buildConicGradient(color, score) {
        var turn = Math.max(0, Math.min(100, score)) / 100;
        return "conic-gradient(" + color + " 0turn, " + color + " " + turn + "turn, #e8ecf1 " + turn + "turn)";
    }

    function buildSkillPreview(requiredSkills, coverText) {
        if (!requiredSkills.length) {
            return "<span class=\"skill-chip\">No required skills configured</span>";
        }
        var lowerCover = coverText.toLowerCase();
        return requiredSkills.map(function (skill) {
            var hit = lowerCover.indexOf(skill.toLowerCase()) >= 0;
            return "<span class=\"skill-chip" + (hit ? " is-hit" : "") + "\">" + escapeHtml(skill) + "</span>";
        }).join("");
    }

    function createEmptyState() {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">No match data available</p>" +
            "<p class=\"empty-copy\">Ask candidates to apply first, then load match results again.</p>";
        return empty;
    }

    function findJobById(jobId) {
        for (var i = 0; i < state.jobs.length; i++) {
            if (safeText(state.jobs[i].jobId, "") === jobId) {
                return state.jobs[i];
            }
        }
        return null;
    }

    function setLoading(loading) {
        if (loadButton) {
            loadButton.disabled = loading;
            loadButton.textContent = loading ? "Loading..." : "Load results";
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

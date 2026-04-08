(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentUserId = typeof window.APP_CURRENT_USER_ID === "string" ? window.APP_CURRENT_USER_ID.trim() : "";

    var messageNode = document.getElementById("overview-message");
    var activitySummaryNode = document.getElementById("activity-summary");
    var activityListNode = document.getElementById("activity-list");

    var statNodes = {
        activeJobs: document.getElementById("stat-active-jobs"),
        totalApplicants: document.getElementById("stat-total-applicants"),
        pendingReview: document.getElementById("stat-pending-review"),
        offersSent: document.getElementById("stat-offers-sent")
    };

    if (!activitySummaryNode || !activityListNode || !statNodes.activeJobs || !statNodes.totalApplicants || !statNodes.pendingReview || !statNodes.offersSent) {
        return;
    }

    loadOverview();

    function loadOverview() {
        hideMessage();
        activitySummaryNode.textContent = "Loading activity...";
        activityListNode.innerHTML = "";

        Promise.allSettled([fetchJobs(), fetchApplications()])
            .then(function (results) {
                var jobs = results[0].status === "fulfilled" ? results[0].value : [];
                var applications = results[1].status === "fulfilled" ? results[1].value : [];

                if (results[0].status === "rejected" || results[1].status === "rejected") {
                    showMessage("Some overview data could not be loaded. Showing available results.", "error");
                }

                renderStats(jobs, applications);
                renderActivity(applications);
            })
            .catch(function () {
                showMessage("Unable to load overview data right now.", "error");
                renderStats([], []);
                renderActivity([]);
            });
    }

    function fetchJobs() {
        var url = contextPath + "/jobs";
        if (currentUserId) {
            url += "?moId=" + encodeURIComponent(currentUserId);
        }

        return request(url, {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        }).then(function (result) {
            var response = result.response;
            var payload = result.payload;

            if (response.status === 401) {
                handleUnauthorized();
                return [];
            }

            if (!response.ok || !payload || payload.success !== true) {
                throw new Error("jobs-load-failed");
            }

            return Array.isArray(payload.jobs) ? payload.jobs : [];
        });
    }

    function fetchApplications() {
        return request(contextPath + "/apply", {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        }).then(function (result) {
            var response = result.response;
            var payload = result.payload;

            if (response.status === 401) {
                handleUnauthorized();
                return [];
            }

            if (response.status === 403) {
                showMessage("This page is available for MO accounts only.", "error");
                return [];
            }

            if (!response.ok || !payload || payload.success !== true) {
                throw new Error("applications-load-failed");
            }

            return Array.isArray(payload.applications) ? payload.applications : [];
        });
    }

    function renderStats(jobs, applications) {
        var activeJobs = countActiveJobs(jobs);
        var pendingReview = 0;
        var offersSent = 0;

        applications.forEach(function (application) {
            var status = safeText(application.status, "PENDING").toUpperCase();
            if (status === "PENDING") {
                pendingReview += 1;
            } else if (status === "ACCEPTED") {
                offersSent += 1;
            }
        });

        statNodes.activeJobs.textContent = String(activeJobs);
        statNodes.totalApplicants.textContent = String(applications.length);
        statNodes.pendingReview.textContent = String(pendingReview);
        statNodes.offersSent.textContent = String(offersSent);
    }

    function countActiveJobs(jobs) {
        if (!Array.isArray(jobs) || jobs.length === 0) {
            return 0;
        }

        var openCount = jobs.filter(function (job) {
            var status = safeText(job.status, "OPEN").toUpperCase();
            return status === "OPEN" || status === "ACTIVE" || status === "PUBLISHED";
        }).length;

        return openCount > 0 ? openCount : jobs.length;
    }

    function renderActivity(applications) {
        activityListNode.innerHTML = "";
        if (!Array.isArray(applications) || applications.length === 0) {
            activitySummaryNode.textContent = "No activity yet.";
            activityListNode.appendChild(createEmptyState());
            return;
        }

        var sorted = applications.slice().sort(function (a, b) {
            return toTimestamp(b.appliedAt) - toTimestamp(a.appliedAt);
        });

        var visible = sorted.slice(0, 6);
        activitySummaryNode.textContent = "Tracking " + applications.length + " application" + (applications.length > 1 ? "s" : "") + ".";

        visible.forEach(function (application) {
            activityListNode.appendChild(createActivityItem(application));
        });
    }

    function createActivityItem(application) {
        var item = document.createElement("article");
        item.className = "activity-item";

        var status = safeText(application.status, "PENDING").toUpperCase();
        var statusClass = getStatusClass(status);
        var title = getActivityTitle(status);
        var subtitle = safeText(application.jobTitle, "Untitled position");
        var courseCode = safeText(application.courseCode, "");
        if (courseCode) {
            subtitle += " (" + courseCode + ")";
        }

        item.innerHTML =
            "<div class=\"activity-main\">" +
                "<span class=\"activity-dot " + escapeHtml(statusClass) + "\" aria-hidden=\"true\"></span>" +
                "<div class=\"activity-text\">" +
                    "<p class=\"activity-title\">" + escapeHtml(title) + "</p>" +
                    "<p class=\"activity-subtitle\">" + escapeHtml(subtitle) + "</p>" +
                "</div>" +
            "</div>" +
            "<span class=\"activity-time\">" + escapeHtml(formatRelativeTime(application.appliedAt)) + "</span>";

        return item;
    }

    function createEmptyState() {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">No recent activity</p>" +
            "<p class=\"empty-copy\">Once TAs apply for your jobs, latest updates will appear here.</p>";
        return empty;
    }

    function getActivityTitle(status) {
        if (status === "PENDING") {
            return "New application received";
        }
        if (status === "ACCEPTED") {
            return "Offer accepted";
        }
        if (status === "REJECTED") {
            return "Application rejected";
        }
        if (status === "WITHDRAWN") {
            return "Application withdrawn";
        }
        return "Application updated";
    }

    function getStatusClass(status) {
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

    function formatRelativeTime(value) {
        var timestamp = toTimestamp(value);
        if (timestamp <= 0) {
            return "-";
        }

        var diffMs = Date.now() - timestamp;
        if (diffMs < 0) {
            return formatDateTime(value);
        }

        var minute = 60 * 1000;
        var hour = 60 * minute;
        var day = 24 * hour;

        if (diffMs < hour) {
            var minutes = Math.max(1, Math.floor(diffMs / minute));
            return minutes + "m ago";
        }
        if (diffMs < day) {
            var hours = Math.max(1, Math.floor(diffMs / hour));
            return hours + "h ago";
        }

        var days = Math.max(1, Math.floor(diffMs / day));
        if (days <= 7) {
            return days + "d ago";
        }

        return formatDateTime(value);
    }

    function toTimestamp(value) {
        if (typeof value !== "string" || !value.trim()) {
            return 0;
        }
        var date = new Date(value);
        if (isNaN(date.getTime())) {
            return 0;
        }
        return date.getTime();
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

    function handleUnauthorized() {
        showMessage("Your session has expired. Redirecting to login...", "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
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

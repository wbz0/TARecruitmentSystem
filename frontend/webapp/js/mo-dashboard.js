(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentUserId = typeof window.APP_CURRENT_USER_ID === "string" ? window.APP_CURRENT_USER_ID.trim() : "";

    var form = document.getElementById("job-create-form");
    var publishButton = document.getElementById("publish-btn");
    var resetButton = document.getElementById("reset-btn");
    var refreshJobsButton = document.getElementById("refresh-jobs-btn");
    var jobsListNode = document.getElementById("jobs-list");
    var jobsSummaryNode = document.getElementById("jobs-summary");
    var messageNode = document.getElementById("form-message");

    if (!form || !publishButton || !jobsListNode || !jobsSummaryNode) {
        return;
    }

    var fields = {
        title: document.getElementById("job-title"),
        courseCode: document.getElementById("course-code"),
        courseName: document.getElementById("course-name"),
        description: document.getElementById("description"),
        requiredSkills: document.getElementById("required-skills"),
        positions: document.getElementById("positions"),
        deadline: document.getElementById("deadline"),
        workload: document.getElementById("workload"),
        salary: document.getElementById("salary")
    };

    var state = {
        submitting: false,
        loadingJobs: false
    };

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        submitCreate();
    });

    form.addEventListener("reset", function () {
        hideMessage();
    });

    if (refreshJobsButton) {
        refreshJobsButton.addEventListener("click", function () {
            loadMyJobs();
        });
    }

    loadMyJobs();

    function submitCreate() {
        if (state.submitting) {
            return;
        }

        hideMessage();

        var validationMessage = validateForm();
        if (validationMessage) {
            showMessage(validationMessage, "error");
            return;
        }

        var formData = new URLSearchParams();
        formData.set("title", fields.title.value.trim());
        formData.set("courseCode", fields.courseCode.value.trim());
        formData.set("courseName", fields.courseName.value.trim());
        formData.set("description", fields.description.value.trim());
        formData.set("requiredSkills", normalizeSkillsForSubmit(fields.requiredSkills.value));
        formData.set("positions", fields.positions.value.trim());
        formData.set("workload", fields.workload.value.trim());
        formData.set("salary", fields.salary.value.trim());

        var deadlineValue = normalizeDeadline(fields.deadline.value);
        if (deadlineValue) {
            formData.set("deadline", deadlineValue);
        }

        setSubmitting(true);

        request(contextPath + "/jobs", {
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

                if (response.status === 403) {
                    showMessage("Only MO accounts can publish jobs.", "error");
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = "Failed to publish job. Please check your input and try again.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                showMessage("Job posted successfully.", "success");
                form.reset();
                fields.positions.value = "1";
                loadMyJobs();
            })
            .catch(function () {
                showMessage("Network error while posting job.", "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    function loadMyJobs() {
        if (state.loadingJobs) {
            return;
        }

        setLoadingJobs(true);
        jobsListNode.innerHTML = "";
        jobsSummaryNode.textContent = "Loading your jobs...";

        var url = contextPath + "/jobs";
        if (currentUserId) {
            url += "?moId=" + encodeURIComponent(currentUserId);
        }

        request(url, {
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
                    jobsSummaryNode.textContent = "Unable to load postings right now.";
                    return;
                }

                var jobs = Array.isArray(payload.jobs) ? payload.jobs : [];
                renderJobs(jobs);
            })
            .catch(function () {
                jobsSummaryNode.textContent = "Unable to load postings right now.";
            })
            .finally(function () {
                setLoadingJobs(false);
            });
    }

    function renderJobs(jobs) {
        jobsListNode.innerHTML = "";
        if (!Array.isArray(jobs) || jobs.length === 0) {
            jobsSummaryNode.textContent = "No jobs posted yet.";
            jobsListNode.appendChild(createEmptyState());
            return;
        }

        jobsSummaryNode.textContent = "You have posted " + jobs.length + " job" + (jobs.length > 1 ? "s" : "") + ".";

        jobs.forEach(function (job) {
            jobsListNode.appendChild(createJobItem(job));
        });
    }

    function createJobItem(job) {
        var item = document.createElement("article");
        item.className = "job-item";

        var status = safeText(job.status, "OPEN").toUpperCase();
        var courseText = safeText(job.courseCode, "-");
        if (job.courseName) {
            courseText += " · " + safeText(job.courseName);
        }

        item.innerHTML =
            "<header class=\"job-item-header\">" +
                "<h4>" + escapeHtml(safeText(job.title, "Untitled position")) + "</h4>" +
                "<span class=\"status-pill status-" + escapeHtml(status.toLowerCase()) + "\">" + escapeHtml(status) + "</span>" +
            "</header>" +
            "<p class=\"job-item-course\">" + escapeHtml(courseText) + "</p>" +
            "<div class=\"job-item-meta\">" +
                "<span>Positions: " + escapeHtml(String(job.positions || 0)) + "</span>" +
                "<span>Deadline: " + escapeHtml(formatDateTime(job.deadline)) + "</span>" +
            "</div>";

        return item;
    }

    function createEmptyState() {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">No postings yet</p>" +
            "<p class=\"empty-copy\">Use the form to publish your first TA position.</p>";
        return empty;
    }

    function validateForm() {
        var title = fields.title.value.trim();
        var courseCode = fields.courseCode.value.trim();
        var positionsText = fields.positions.value.trim();

        if (!title) {
            fields.title.focus();
            return "Job title is required.";
        }
        if (title.length > 200) {
            fields.title.focus();
            return "Job title must be 200 characters or fewer.";
        }
        if (!courseCode) {
            fields.courseCode.focus();
            return "Course code is required.";
        }

        var positions = parseInt(positionsText, 10);
        if (isNaN(positions) || positions < 1) {
            fields.positions.focus();
            return "Positions must be at least 1.";
        }

        return "";
    }

    function setSubmitting(submitting) {
        state.submitting = submitting;
        publishButton.disabled = submitting;
        if (resetButton) {
            resetButton.disabled = submitting;
        }
        publishButton.textContent = submitting ? "Publishing..." : "Publish job";
    }

    function setLoadingJobs(loading) {
        state.loadingJobs = loading;
        if (refreshJobsButton) {
            refreshJobsButton.disabled = loading;
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

    function normalizeSkillsForSubmit(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }
        return value
            .split(/[;,]/)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            })
            .join(",");
    }

    function normalizeDeadline(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }
        var text = value.trim();
        if (text.length === 16) {
            return text + ":00";
        }
        return text;
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

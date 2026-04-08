(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";

    var filterForm = document.getElementById("status-filter-form");
    var statusFilter = document.getElementById("status-filter");
    var keywordFilter = document.getElementById("keyword-filter");
    var resetButton = document.getElementById("reset-btn");
    var refreshButton = document.getElementById("refresh-btn");
    var searchButton = document.getElementById("search-btn");
    var messageNode = document.getElementById("status-message");
    var listSummaryNode = document.getElementById("list-summary");
    var listNode = document.getElementById("applications-list");

    var summaryNodes = {
        total: document.getElementById("summary-total"),
        pending: document.getElementById("summary-pending"),
        accepted: document.getElementById("summary-accepted"),
        rejected: document.getElementById("summary-rejected"),
        withdrawn: document.getElementById("summary-withdrawn")
    };

    if (!filterForm || !listNode || !listSummaryNode) {
        return;
    }

    var state = {
        loading: false,
        applications: [],
        withdrawingId: ""
    };

    filterForm.addEventListener("submit", function (event) {
        event.preventDefault();
        render();
    });

    if (resetButton) {
        resetButton.addEventListener("click", function () {
            statusFilter.value = "";
            keywordFilter.value = "";
            render();
        });
    }

    if (refreshButton) {
        refreshButton.addEventListener("click", function () {
            loadApplications();
        });
    }

    loadApplications();

    function loadApplications() {
        if (state.loading) {
            return;
        }

        setLoading(true);
        hideMessage();
        listSummaryNode.textContent = "Loading applications...";
        listNode.innerHTML = "";

        request(contextPath + "/apply", {
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
                    showMessage("This page is available for TA accounts only.", "error");
                    state.applications = [];
                    render();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = "Unable to load your applications.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    state.applications = [];
                    render();
                    return;
                }

                state.applications = Array.isArray(payload.applications) ? payload.applications : [];
                render();
            })
            .catch(function () {
                showMessage("Network error. Please try again.", "error");
                state.applications = [];
                render();
            })
            .finally(function () {
                setLoading(false);
            });
    }

    function render() {
        var filtered = getFilteredApplications();
        renderSummary(state.applications);
        renderList(filtered);
    }

    function getFilteredApplications() {
        var selectedStatus = statusFilter.value.trim().toUpperCase();
        var keyword = keywordFilter.value.trim().toLowerCase();

        return state.applications.filter(function (app) {
            var appStatus = safeText(app.status, "").toUpperCase();
            if (selectedStatus && appStatus !== selectedStatus) {
                return false;
            }

            if (!keyword) {
                return true;
            }

            var searchable = [
                safeText(app.jobTitle, ""),
                safeText(app.courseCode, ""),
                safeText(app.moName, ""),
                safeText(app.status, "")
            ].join(" ").toLowerCase();

            return searchable.indexOf(keyword) >= 0;
        });
    }

    function renderSummary(applications) {
        var counts = {
            total: 0,
            pending: 0,
            accepted: 0,
            rejected: 0,
            withdrawn: 0
        };

        if (Array.isArray(applications)) {
            counts.total = applications.length;
            applications.forEach(function (app) {
                var status = safeText(app.status, "PENDING").toUpperCase();
                if (status === "PENDING") {
                    counts.pending += 1;
                } else if (status === "ACCEPTED") {
                    counts.accepted += 1;
                } else if (status === "REJECTED") {
                    counts.rejected += 1;
                } else if (status === "WITHDRAWN") {
                    counts.withdrawn += 1;
                }
            });
        }

        summaryNodes.total.textContent = String(counts.total);
        summaryNodes.pending.textContent = String(counts.pending);
        summaryNodes.accepted.textContent = String(counts.accepted);
        summaryNodes.rejected.textContent = String(counts.rejected);
        summaryNodes.withdrawn.textContent = String(counts.withdrawn);
    }

    function renderList(applications) {
        listNode.innerHTML = "";

        if (!Array.isArray(applications) || applications.length === 0) {
            listSummaryNode.textContent = "No applications match the current filters.";
            listNode.appendChild(createEmptyState());
            return;
        }

        listSummaryNode.textContent = "Showing " + applications.length + " application" + (applications.length > 1 ? "s" : "") + ".";

        applications.forEach(function (app) {
            listNode.appendChild(createApplicationCard(app));
        });
    }

    function createApplicationCard(app) {
        var card = document.createElement("article");
        card.className = "application-card";

        var status = safeText(app.status, "PENDING").toUpperCase();
        var canWithdraw = status === "PENDING";
        var buttonDisabled = !canWithdraw || state.withdrawingId === safeText(app.applicationId, "");
        var buttonLabel = canWithdraw ? (buttonDisabled && state.withdrawingId === safeText(app.applicationId, "") ? "Withdrawing..." : "Withdraw") : "Cannot withdraw";
        var detailLink = contextPath + "/jsp/ta/job-detail.jsp?id=" + encodeURIComponent(safeText(app.jobId, ""));

        card.innerHTML =
            "<header class=\"application-header\">" +
                "<div>" +
                    "<h3>" + escapeHtml(safeText(app.jobTitle, "Untitled job")) + "</h3>" +
                    "<p>" + escapeHtml(safeText(app.courseCode, "-")) + " · MO: " + escapeHtml(safeText(app.moName, "-")) + "</p>" +
                "</div>" +
                "<span class=\"status-pill status-" + escapeHtml(status.toLowerCase()) + "\">" + escapeHtml(status) + "</span>" +
            "</header>" +
            "<div class=\"application-meta\">" +
                "<p><span>Applied at:</span><strong>" + escapeHtml(formatDateTime(app.appliedAt)) + "</strong></p>" +
                "<p><span>Cover letter:</span><strong>" + escapeHtml(shortenText(safeText(app.coverLetter, "-"), 160)) + "</strong></p>" +
            "</div>" +
            "<div class=\"application-actions\">" +
                "<a class=\"ghost-link\" href=\"" + detailLink + "\">View job</a>" +
                "<button class=\"danger-btn\" type=\"button\"" + (buttonDisabled ? " disabled" : "") + " data-action=\"withdraw\" data-id=\"" + escapeHtml(safeText(app.applicationId, "")) + "\">" + escapeHtml(buttonLabel) + "</button>" +
            "</div>";

        var withdrawButton = card.querySelector("button[data-action=\"withdraw\"]");
        if (withdrawButton && canWithdraw) {
            withdrawButton.addEventListener("click", function () {
                handleWithdraw(safeText(app.applicationId, ""));
            });
        }

        return card;
    }

    function createEmptyState() {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">No applications to display</p>" +
            "<p class=\"empty-copy\">After you apply for a job, the status will appear here.</p>";
        return empty;
    }

    function handleWithdraw(applicationId) {
        if (!applicationId || state.withdrawingId) {
            return;
        }

        state.withdrawingId = applicationId;
        render();
        hideMessage();

        request(contextPath + "/apply?id=" + encodeURIComponent(applicationId) + "&action=withdraw", {
            method: "PUT",
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
                    var errorMessage = "Unable to withdraw this application.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                showMessage("Application withdrawn successfully.", "success");
                loadApplications();
            })
            .catch(function () {
                showMessage("Network error while withdrawing application.", "error");
            })
            .finally(function () {
                state.withdrawingId = "";
                render();
            });
    }

    function setLoading(loading) {
        state.loading = loading;
        if (refreshButton) {
            refreshButton.disabled = loading;
        }
        if (resetButton) {
            resetButton.disabled = loading;
        }
        if (searchButton) {
            searchButton.disabled = loading;
            searchButton.textContent = loading ? "Loading..." : "Apply filters";
        }
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

    function shortenText(value, maxLength) {
        var text = safeText(value, "");
        if (!text) {
            return "-";
        }
        if (text.length <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 1) + "…";
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

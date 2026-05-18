(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";

    var filterForm = document.getElementById("workload-filter-form");
    var startInput = document.getElementById("start-time");
    var endInput = document.getElementById("end-time");
    var clearButton = document.getElementById("clear-filter-btn");
    var refreshButton = document.getElementById("refresh-btn");
    var exportButton = document.getElementById("export-btn");
    var applyButton = document.getElementById("apply-filter-btn");
    var messageNode = document.getElementById("dashboard-message");
    var moSummaryNode = document.getElementById("mo-summary");
    var moListNode = document.getElementById("mo-list");

    var summaryNodes = {
        total: document.getElementById("summary-total"),
        pending: document.getElementById("summary-pending"),
        accepted: document.getElementById("summary-accepted"),
        rejected: document.getElementById("summary-rejected"),
        withdrawn: document.getElementById("summary-withdrawn")
    };

    if (!filterForm || !startInput || !endInput || !moSummaryNode || !moListNode) {
        return;
    }

    var state = {
        loading: false,
        exporting: false
    };

    filterForm.addEventListener("submit", function (event) {
        event.preventDefault();
        loadDashboard();
    });

    if (clearButton) {
        clearButton.addEventListener("click", function () {
            startInput.value = "";
            endInput.value = "";
            loadDashboard();
        });
    }

    if (refreshButton) {
        refreshButton.addEventListener("click", function () {
            loadDashboard();
        });
    }

    if (exportButton) {
        exportButton.addEventListener("click", function () {
            exportCsv();
        });
    }

    loadDashboard();

    function loadDashboard() {
        if (state.loading) {
            return Promise.resolve();
        }

        var validationError = validateTimeRange();
        if (validationError) {
            showMessage(validationError, "error");
            return Promise.resolve();
        }

        state.loading = true;
        setLoadingState(true);
        hideMessage();
        moSummaryNode.textContent = "Loading workload...";
        moListNode.innerHTML = "";

        return Promise.all([
            fetchCounts(),
            fetchMoWorkloads()
        ]).then(function (values) {
            var countsResult = values[0];
            var moResult = values[1];

            if (countsResult.unauthorized || moResult.unauthorized) {
                handleUnauthorized();
                return;
            }

            if (!countsResult.ok) {
                showMessage(countsResult.message || "Failed to load application totals.", "error");
                resetSummary();
            } else {
                renderSummary(countsResult.payload);
            }

            if (!moResult.ok) {
                showMessage(moResult.message || "Failed to load MO workloads.", "error");
                renderMoList([]);
            } else {
                var moWorkloads = Array.isArray(moResult.payload.moWorkloads) ? moResult.payload.moWorkloads : [];
                renderMoList(moWorkloads);
            }
        }).catch(function () {
            showMessage("Network error while loading dashboard.", "error");
            resetSummary();
            renderMoList([]);
        }).finally(function () {
            state.loading = false;
            setLoadingState(false);
        });
    }

    function fetchCounts() {
        var url = contextPath + "/api/admin/workload" + buildTimeQuery("");
        return request(url, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (result) {
            return normalizeApiResult(result, "Application totals request failed.");
        });
    }

    function fetchMoWorkloads() {
        var url = contextPath + "/api/admin/workload?mode=mo" + buildTimeQuery("&");
        return request(url, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (result) {
            return normalizeApiResult(result, "MO workload request failed.");
        });
    }

    function exportCsv() {
        if (state.exporting) {
            return;
        }

        var validationError = validateTimeRange();
        if (validationError) {
            showMessage(validationError, "error");
            return;
        }

        state.exporting = true;
        if (exportButton) {
            exportButton.disabled = true;
            exportButton.textContent = "Exporting...";
        }

        var url = contextPath + "/api/admin/workload?mode=mo&export=csv" + buildTimeQuery("&");
        fetch(url, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        }).then(function (response) {
            if (response.status === 401) {
                handleUnauthorized();
                return null;
            }
            if (!response.ok) {
                throw new Error("Failed to export CSV.");
            }
            return response.text();
        }).then(function (csvText) {
            if (typeof csvText !== "string") {
                return;
            }
            downloadCsv(csvText);
            showMessage("CSV exported successfully.", "success");
        }).catch(function () {
            showMessage("Unable to export CSV.", "error");
        }).finally(function () {
            state.exporting = false;
            if (exportButton) {
                exportButton.disabled = false;
                exportButton.textContent = "Export CSV";
            }
        });
    }

    function normalizeApiResult(result, fallbackMessage) {
        var response = result.response;
        var payload = result.payload;

        if (response.status === 401) {
            return { unauthorized: true, ok: false, payload: null, message: "Please login first." };
        }
        if (!response.ok || !payload || payload.success !== true) {
            return {
                unauthorized: false,
                ok: false,
                payload: payload || null,
                message: payload && payload.message ? String(payload.message) : fallbackMessage
            };
        }
        return { unauthorized: false, ok: true, payload: payload, message: "" };
    }

    function renderSummary(payload) {
        summaryNodes.total.textContent = String(toNumber(payload.total));
        summaryNodes.pending.textContent = String(toNumber(payload.pending));
        summaryNodes.accepted.textContent = String(toNumber(payload.accepted));
        summaryNodes.rejected.textContent = String(toNumber(payload.rejected));
        summaryNodes.withdrawn.textContent = String(toNumber(payload.withdrawn));
    }

    function resetSummary() {
        summaryNodes.total.textContent = "0";
        summaryNodes.pending.textContent = "0";
        summaryNodes.accepted.textContent = "0";
        summaryNodes.rejected.textContent = "0";
        summaryNodes.withdrawn.textContent = "0";
    }

    function renderMoList(moWorkloads) {
        moListNode.innerHTML = "";
        if (!Array.isArray(moWorkloads) || moWorkloads.length === 0) {
            moSummaryNode.textContent = "No MO workload data in selected range.";
            moListNode.appendChild(createEmptyState());
            return;
        }

        moSummaryNode.textContent = "Loaded " + moWorkloads.length + " MO workload item" + (moWorkloads.length > 1 ? "s" : "") + ".";
        moWorkloads.forEach(function (item) {
            moListNode.appendChild(createMoItem(item));
        });
    }

    function createMoItem(item) {
        var element = document.createElement("article");
        element.className = "mo-item";
        element.innerHTML =
            "<div class=\"mo-item-header\">" +
                "<h3>" + escapeHtml(safeText(item.moName, "MO User")) + "</h3>" +
                "<span class=\"mo-item-id\">" + escapeHtml(safeText(item.moId, "-")) + "</span>" +
            "</div>" +
            "<div class=\"mo-item-stats\">" +
                "<p><span>Total</span><strong>" + escapeHtml(String(toNumber(item.totalApplications))) + "</strong></p>" +
                "<p><span>Pending</span><strong>" + escapeHtml(String(toNumber(item.pending))) + "</strong></p>" +
                "<p><span>Processed</span><strong>" + escapeHtml(String(toNumber(item.processed))) + "</strong></p>" +
                "<p><span>Accepted</span><strong>" + escapeHtml(String(toNumber(item.accepted))) + "</strong></p>" +
            "</div>";
        return element;
    }

    function createEmptyState() {
        var empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML =
            "<p class=\"empty-title\">No workload data yet</p>" +
            "<p class=\"empty-copy\">Adjust time range or wait for application activity to appear.</p>";
        return empty;
    }

    function setLoadingState(loading) {
        if (applyButton) {
            applyButton.disabled = loading;
            applyButton.textContent = loading ? "Loading..." : "Apply range";
        }
        if (clearButton) {
            clearButton.disabled = loading;
        }
        if (refreshButton) {
            refreshButton.disabled = loading;
        }
        if (exportButton) {
            exportButton.disabled = loading || state.exporting;
        }
    }

    function buildTimeQuery(prefix) {
        var query = typeof prefix === "string" ? prefix : "";
        var startValue = normalizeDateTime(startInput.value);
        var endValue = normalizeDateTime(endInput.value);
        if (startValue) {
            query += "start=" + encodeURIComponent(startValue);
            if (endValue) {
                query += "&";
            }
        }
        if (endValue) {
            query += "end=" + encodeURIComponent(endValue);
        }
        return query;
    }

    function validateTimeRange() {
        var startValue = normalizeDateTime(startInput.value);
        var endValue = normalizeDateTime(endInput.value);
        if (!startValue || !endValue) {
            return "";
        }
        var startTime = Date.parse(startValue);
        var endTime = Date.parse(endValue);
        if (isNaN(startTime) || isNaN(endTime)) {
            return "Invalid datetime format.";
        }
        if (startTime > endTime) {
            return "Start time cannot be after end time.";
        }
        return "";
    }

    function normalizeDateTime(value) {
        if (typeof value !== "string") {
            return "";
        }
        var trimmed = value.trim();
        if (!trimmed) {
            return "";
        }
        return trimmed.length === 16 ? trimmed + ":00" : trimmed;
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
        var numberFields = ["total", "pending", "accepted", "rejected", "withdrawn"];
        numberFields.forEach(function (field) {
            var regex = new RegExp("\"" + field + "\"\\s*:\\s*(-?\\d+)", "i");
            var match = text.match(regex);
            if (match) {
                payload[field] = Number(match[1]);
            }
        });
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
        showMessage("Session expired. Redirecting to login...", "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
    }

    function downloadCsv(csvText) {
        var blob = new Blob([csvText], { type: "text/csv;charset=UTF-8" });
        var url = window.URL.createObjectURL(blob);
        var link = document.createElement("a");
        link.href = url;
        link.download = "mo-workload-stats.csv";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    }

    function toNumber(value) {
        var number = Number(value);
        return isFinite(number) ? number : 0;
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

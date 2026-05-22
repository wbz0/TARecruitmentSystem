/*
 * Admin invite code panel script, corresponds to /jsp/admin/invite.jsp.
 *
 * Calls /api/admin/invitations/current-code to read or refresh the current short invite code,
 * and displays a countdown on the frontend.
 * The actual validity verification is done by InviteCodeService.
 */
(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var i18n = window.AppI18n && typeof window.AppI18n.t === "function" ? window.AppI18n : null;

    var codeDisplay = document.getElementById("code-display");
    var countdownBar = document.getElementById("countdown-bar");
    var countdownLabel = document.getElementById("countdown-label");
    var rotateBtn = document.getElementById("rotate-btn");
    var codeError = document.getElementById("code-error");

    if (!codeDisplay) return;

    var countdownInterval = null;
    // Temporary placeholder before backend returns actual seconds; will be overwritten by secondsRemaining after normal load.
    var currentSeconds = 30;

    fetchCurrentCode();

    if (rotateBtn) {
        rotateBtn.addEventListener("click", function () {
            rotateBtn.disabled = true;
            hideError();
            fetch(window.TARecruitment.routes.admin.currentInvitationCode(), {
                method: "POST",
                headers: { "X-Requested-With": "XMLHttpRequest" }
            })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    if (!data || data.success !== true) {
                        showError(t("portal.adminDashboard.codePanel.refreshError", "Failed to refresh code."));
                        return;
                    }
                    renderCode(data.data.code, data.data.secondsRemaining);
                })
                .catch(function () {
                    showError(t("portal.adminDashboard.codePanel.refreshError", "Failed to refresh code."));
                })
                .finally(function () {
                    rotateBtn.disabled = false;
                });
        });
    }

    /*
     * Fetch current invite code; 401 means admin session expired, redirect to login page.
     */
    function fetchCurrentCode() {
        hideError();
        fetch(window.TARecruitment.routes.admin.currentInvitationCode(), {
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
            .then(function (res) {
                if (res.status === 401) {
                    window.location.href = contextPath + "/login.jsp";
                    return null;
                }
                return res.json();
            })
            .then(function (data) {
                if (!data) return;
                if (!data.success) {
                    showError(t("portal.adminDashboard.codePanel.loadError", "Failed to load invite code."));
                    return;
                }
                renderCode(data.data.code, data.data.secondsRemaining);
            })
            .catch(function () {
                showError(t("portal.adminDashboard.codePanel.loadError", "Failed to load invite code."));
            });
    }

    /*
     * Backend returns code and remaining seconds; frontend only handles formatting and countdown display.
     */
    function renderCode(code, seconds) {
        if (codeDisplay) {
            var formatted = formatCode(code);
            codeDisplay.textContent = formatted;
            codeDisplay.classList.remove("code-loading");
        }
        startCountdown(seconds);
    }

    /*
     * 8-digit short code displayed as 4+4; actual submission value is returned by backend API.
     */
    function formatCode(code) {
        if (typeof code !== "string" || code.length !== 8) return code || "-";
        return code.slice(0, 4) + " " + code.slice(4);
    }

    /*
     * Auto-refresh when countdown reaches 0 to avoid displaying expired invite code.
     */
    function startCountdown(seconds) {
        clearInterval(countdownInterval);
        currentSeconds = typeof seconds === "number" ? Math.max(0, seconds) : 600;
        updateCountdownUI();

        countdownInterval = setInterval(function () {
            currentSeconds -= 1;
            if (currentSeconds <= 0) {
                clearInterval(countdownInterval);
                fetchCurrentCode();
            } else {
                updateCountdownUI();
            }
        }, 1000);
    }

    /*
     * Progress bar calculated based on 10-minute window; window length must stay consistent with InviteCodeService.
     */
    function updateCountdownUI() {
        var pct = Math.max(0, currentSeconds / 600) * 100;
        if (countdownBar) countdownBar.style.width = pct + "%";
        if (countdownLabel) {
            var m = Math.floor(currentSeconds / 60);
            var s = currentSeconds % 60;
            countdownLabel.textContent = m + ":" + (s < 10 ? "0" : "") + s;
        }
    }

    /*
     * Invite code panel's own error area, does not affect other dashboard notifications.
     */
    function showError(msg) {
        if (codeError) {
            codeError.textContent = msg;
            codeError.classList.remove("hidden");
        }
    }

    /*
     * Clear old error before each request to prevent stale error message after successful refresh.
     */
    function hideError() {
        if (codeError) {
            codeError.textContent = "";
            codeError.classList.add("hidden");
        }
    }

    /*
     * i18n fallback for standalone page scripts.
     */
    function t(key, fallback) {
        if (i18n) return i18n.t(key, fallback);
        return fallback || key;
    }
})();
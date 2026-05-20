/*
 * 管理员邀请码面板脚本，对应 /jsp/admin/invite.jsp。
 *
 * 调用 /api/admin/invitations/current-code 读取或刷新当前短邀请码，
 * 并在前端做倒计时展示。真正的有效期校验以 InviteCodeService 为准。
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
    // 后端返回真实秒数前的临时占位；正常加载后会被 secondsRemaining 覆盖。
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
     * 读取当前邀请码；401 说明管理员登录态失效，直接回登录页。
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
     * 后端返回 code 和剩余秒数，前端只负责格式化和倒计时展示。
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
     * 8 位短码按 4+4 显示，真实提交值仍由后端接口返回。
     */
    function formatCode(code) {
        if (typeof code !== "string" || code.length !== 8) return code || "—";
        return code.slice(0, 4) + " " + code.slice(4);
    }

    /*
     * 倒计时到 0 后自动重新拉取，避免页面停留在过期邀请码上。
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
     * 进度条按 10 分钟窗口计算；窗口长度需要与 InviteCodeService 保持一致。
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
     * 邀请码面板自己的错误区域，避免影响 dashboard 其它提示。
     */
    function showError(msg) {
        if (codeError) {
            codeError.textContent = msg;
            codeError.classList.remove("hidden");
        }
    }

    /*
     * 每次请求前清空旧错误，防止刷新成功后仍显示失败文案。
     */
    function hideError() {
        if (codeError) {
            codeError.textContent = "";
            codeError.classList.add("hidden");
        }
    }

    /*
     * 独立页面脚本的 i18n 兜底。
     */
    function t(key, fallback) {
        if (i18n) return i18n.t(key, fallback);
        return fallback || key;
    }
})();

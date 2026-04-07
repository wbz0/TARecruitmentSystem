(function () {
    var form = document.getElementById("login-form");
    if (!form) {
        return;
    }

    var usernameInput = document.getElementById("username");
    var passwordInput = document.getElementById("password");
    var roleInput = document.getElementById("login-role");
    var loginButtons = form.querySelectorAll(".login-action-btn");
    var messageBox = document.getElementById("form-message");
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var selectedRole = getNormalizedRole(roleInput ? roleInput.value : "") || "MO";

    setSelectedRole(selectedRole);

    Array.prototype.forEach.call(loginButtons, function (button) {
        button.addEventListener("click", function (event) {
            event.preventDefault();
            setSelectedRole(button.getAttribute("data-role"));
            handleLogin(selectedRole);
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        var submitter = event.submitter || document.activeElement;
        var submitterRole = getNormalizedRole(submitter && submitter.getAttribute ? submitter.getAttribute("data-role") : "");
        if (submitterRole) {
            setSelectedRole(submitterRole);
        }

        handleLogin(selectedRole);
    });

    function submitLogin(username, password, role) {
        var formData = new URLSearchParams();
        formData.set("username", username);
        formData.set("password", password);
        if (role) {
            formData.set("role", role);
        }

        return fetch(contextPath + "/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        })
            .then(function (response) {
                return response.text().then(function (bodyText) {
                    var payload = parseLoginResponse(bodyText);
                    return { response: response, payload: payload };
                });
            })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (!payload || payload.success !== true || !response.ok) {
                    var errorMessage = "登录失败，请检查用户名和密码。";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                showMessage("登录成功，正在跳转...", "success");

                var redirect = "";
                if (typeof payload.redirect === "string") {
                    redirect = payload.redirect.trim();
                }
                if (!redirect) {
                    redirect = contextPath + "/index.jsp";
                }

                window.location.href = redirect;
            });
    }

    function setSubmitting(submitting) {
        Array.prototype.forEach.call(loginButtons, function (button) {
            button.disabled = submitting;
        });
    }

    function handleLogin(role) {
        hideMessage();

        var username = usernameInput.value.trim();
        var password = passwordInput.value;

        if (!username) {
            showMessage("请输入用户名。", "error");
            usernameInput.focus();
            return;
        }

        if (!password) {
            showMessage("请输入密码。", "error");
            passwordInput.focus();
            return;
        }

        setSubmitting(true);
        submitLogin(username, password, role)
            .catch(function () {
                showMessage("网络异常，请稍后重试。", "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    function setSelectedRole(role) {
        var normalizedRole = getNormalizedRole(role);
        if (!normalizedRole) {
            return;
        }

        selectedRole = normalizedRole;
        if (roleInput) {
            roleInput.value = normalizedRole;
        }

        Array.prototype.forEach.call(loginButtons, function (button) {
            var buttonRole = getNormalizedRole(button.getAttribute("data-role"));
            if (buttonRole === normalizedRole) {
                button.classList.add("is-selected");
            } else {
                button.classList.remove("is-selected");
            }
        });
    }

    function getNormalizedRole(value) {
        if (typeof value !== "string") {
            return "";
        }
        var normalized = value.trim().toUpperCase();
        if (normalized === "TA" || normalized === "MO") {
            return normalized;
        }
        return "";
    }

    function showMessage(message, type) {
        messageBox.textContent = message;
        messageBox.classList.remove("hidden", "error", "success");
        messageBox.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        messageBox.textContent = "";
        messageBox.classList.remove("error", "success");
        messageBox.classList.add("hidden");
    }

    function parseLoginResponse(bodyText) {
        try {
            return JSON.parse(bodyText);
        } catch (error) {
            return parseLegacyLoginResponse(bodyText);
        }
    }

    // 兼容后端当前返回的非标准 JSON 文本格式。
    function parseLegacyLoginResponse(bodyText) {
        if (typeof bodyText !== "string") {
            return null;
        }

        var successMatch = bodyText.match(/"success"\s*:\s*(true|false)/i);
        if (!successMatch) {
            return null;
        }

        var payload = {
            success: successMatch[1].toLowerCase() === "true"
        };

        var messageMatch = bodyText.match(/"message"\s*:\s*"([^"]*)"/i);
        if (messageMatch) {
            payload.message = decodeEscapedText(messageMatch[1]);
        }

        var redirectMatch = bodyText.match(/"redirect"\s*:\s*"([^"]*)"/i);
        if (redirectMatch) {
            payload.redirect = decodeEscapedText(redirectMatch[1]);
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
})();

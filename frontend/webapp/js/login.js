(function () {
    var USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9_]{2,19}$/;
    var LOGIN_IDENTIFIER_MAX_LENGTH = 100;
    var PASSWORD_MIN_LENGTH = 6;
    var PASSWORD_MAX_LENGTH = 100;

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
        button.addEventListener("click", function () {
            setSelectedRole(button.getAttribute("data-role"));
            hideMessage();
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
                    var errorMessage = "Login failed. Please check your username and password.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                showMessage("Login successful! Redirecting...", "success");

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

        var identifier = getTrimmedValue(usernameInput);
        var password = getTrimmedValue(passwordInput);

        if (!identifier) {
            showMessage("Please enter your username or email.", "error");
            usernameInput.focus();
            return;
        }

        if (identifier.length > LOGIN_IDENTIFIER_MAX_LENGTH) {
            showMessage("Username or email is too long.", "error");
            usernameInput.focus();
            return;
        }

        if (containsControlChars(identifier) || containsDangerousMarkup(identifier)) {
            showMessage("Username or email contains unsupported characters.", "error");
            usernameInput.focus();
            return;
        }

        if (identifier.indexOf("@") >= 0) {
            if (!isValidEmailAddress(identifier)) {
                showMessage("Please enter a valid email address.", "error");
                usernameInput.focus();
                return;
            }
        } else if (!USERNAME_PATTERN.test(identifier)) {
            showMessage("Username must start with a letter and contain 3-20 letters, numbers, or underscores.", "error");
            usernameInput.focus();
            return;
        }

        if (!password) {
            showMessage("Please enter your password.", "error");
            passwordInput.focus();
            return;
        }

        if (password.length < PASSWORD_MIN_LENGTH) {
            showMessage("Password must be at least 6 characters.", "error");
            passwordInput.focus();
            return;
        }

        if (password.length > PASSWORD_MAX_LENGTH) {
            showMessage("Password is too long.", "error");
            passwordInput.focus();
            return;
        }

        if (containsControlChars(password)) {
            showMessage("Password contains unsupported characters.", "error");
            passwordInput.focus();
            return;
        }

        setSubmitting(true);
        submitLogin(identifier, password, role)
            .catch(function () {
                showMessage("Network error. Please try again.", "error");
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
        if (normalized === "TA" || normalized === "MO" || normalized === "ADMIN") {
            return normalized;
        }
        return "";
    }

    function getTrimmedValue(input) {
        if (!input || typeof input.value !== "string") {
            return "";
        }
        return input.value.trim();
    }

    function containsControlChars(value) {
        return /[\u0000-\u001F\u007F]/.test(value || "");
    }

    function containsDangerousMarkup(value) {
        if (typeof value !== "string" || !value) {
            return false;
        }
        return /<[^>]*>/.test(value) || /javascript:/i.test(value) || /on\w+\s*=/.test(value);
    }

    function isValidEmailAddress(email) {
        if (typeof email !== "string") {
            return false;
        }
        if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
            return false;
        }

        var parts = email.split("@");
        if (parts.length !== 2) {
            return false;
        }

        var local = parts[0];
        var domain = parts[1];
        if (!local || !domain) {
            return false;
        }

        if (local.charAt(0) === "." || local.charAt(local.length - 1) === "." || local.indexOf("..") !== -1) {
            return false;
        }
        if (domain.charAt(0) === "." || domain.charAt(domain.length - 1) === "." || domain.indexOf("..") !== -1) {
            return false;
        }

        return true;
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

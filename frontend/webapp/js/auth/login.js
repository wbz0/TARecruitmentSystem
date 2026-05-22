/*
 * Login page script, corresponds to login.jsp.
 *
 * Handles role selection, frontend basic validation, password visibility toggle,
 * and submits to /api/auth/login.
 * After successful login, redirect address is returned by backend LoginServlet;
 * frontend has a fallback redirect to login.jsp.
 */
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
    var roleButtons = form.querySelectorAll(".role-option");
    var loginSubmitButton = document.getElementById("login-submit");
    var messageBox = document.getElementById("form-message");
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var i18n = window.AppI18n && typeof window.AppI18n.t === "function" ? window.AppI18n : null;
    var selectedRole = getNormalizedRole(roleInput ? roleInput.value : "") || "TA";

    setupPasswordToggles();
    setSelectedRole(selectedRole);

    Array.prototype.forEach.call(roleButtons, function (button) {
        button.addEventListener("click", function () {
            setSelectedRole(button.getAttribute("data-role"));
            hideMessage();
        });
    });

    // Enter key: username -> password -> submit.
    var loginFieldOrder = [usernameInput, passwordInput];
    Array.prototype.forEach.call(loginFieldOrder, function (input, idx) {
        if (!input) return;
        input.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" || event.isComposing) return;
            event.preventDefault();
            var next = loginFieldOrder[idx + 1];
            if (next) {
                next.focus();
            } else {
                handleLogin(selectedRole);
            }
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        handleLogin(selectedRole);
    });

    function submitLogin(username, password, role) {
        // API path must be generated via TARecruitment.routes to work when deployed under /groupproject.
        var formData = new URLSearchParams();
        formData.set("username", username);
        formData.set("password", password);
        if (role) {
            formData.set("role", role);
        }

        return fetch(window.TARecruitment.routes.auth.login(), {
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
                    var status = response.status;
                    if (status === 400 || status === 403) {
                        showMessage(t("login.msg.roleError", "Role selection error."), "error");
                    } else {
                        showMessage(t("login.msg.credentialError", "Username/email or password is incorrect."), "error");
                    }
                    return;
                }

                showMessage(t("login.msg.successRedirect", "Login successful! Redirecting..."), "success");

                var redirect = "";
                if (payload && payload.data && typeof payload.data.redirect === "string") {
                    redirect = payload.data.redirect.trim();
                } else if (typeof payload.redirect === "string") {
                    redirect = payload.redirect.trim();
                }
                if (!redirect) {
                    redirect = contextPath + "/login.jsp";
                }

                window.location.href = redirect;
            });
    }

    function setSubmitting(submitting) {
        Array.prototype.forEach.call(roleButtons, function (button) {
            button.disabled = submitting;
        });
        if (loginSubmitButton) {
            loginSubmitButton.disabled = submitting;
            loginSubmitButton.textContent = submitting
                ? t("login.msg.loggingIn", "Logging in...")
                : t("login.form.submit", "Log in");
        }
    }

    function handleLogin(role) {
        hideMessage();

        var normalizedRole = getNormalizedRole(role);
        if (!normalizedRole) {
            showMessage(t("login.msg.roleError", "Role selection error."), "error");
            return;
        }

        var identifier = getTrimmedValue(usernameInput);
        var password = getTrimmedValue(passwordInput);

        var credentialInvalid =
            !identifier ||
            identifier.length > LOGIN_IDENTIFIER_MAX_LENGTH ||
            containsControlChars(identifier) ||
            containsDangerousMarkup(identifier) ||
            (identifier.indexOf("@") >= 0 ? !isValidEmailAddress(identifier) : !USERNAME_PATTERN.test(identifier)) ||
            !password ||
            password.length < PASSWORD_MIN_LENGTH ||
            password.length > PASSWORD_MAX_LENGTH ||
            containsControlChars(password);

        if (credentialInvalid) {
            showMessage(t("login.msg.credentialError", "Username/email or password is incorrect."), "error");
            return;
        }

        setSubmitting(true);
        submitLogin(identifier, password, normalizedRole)
            .catch(function () {
                showMessage(t("login.msg.networkError", "Network error. Please try again."), "error");
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

        Array.prototype.forEach.call(roleButtons, function (button) {
            var buttonRole = getNormalizedRole(button.getAttribute("data-role"));
            var isSelected = buttonRole === normalizedRole;
            button.classList.toggle("is-selected", isSelected);
            button.setAttribute("aria-pressed", isSelected ? "true" : "false");
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
        return JSON.parse(bodyText);
    }

    function setupPasswordToggles() {
        var buttons = form.querySelectorAll("[data-password-toggle]");
        Array.prototype.forEach.call(buttons, function (button) {
            var wrapper = button.closest(".password-input-wrap");
            var input = wrapper ? wrapper.querySelector("input") : null;
            if (!input) return;

            button.addEventListener("click", function () {
                var shouldShow = input.type === "password";
                input.type = shouldShow ? "text" : "password";
                button.setAttribute("aria-label", shouldShow
                    ? t("common.password.hide", "Hide password")
                    : t("common.password.show", "Show password"));

                var eye = button.querySelector(".password-toggle-eye");
                var eyeOff = button.querySelector(".password-toggle-eye-off");
                if (eye) eye.hidden = shouldShow;
                if (eyeOff) eyeOff.hidden = !shouldShow;

                input.focus({ preventScroll: true });
            });
        });
    }

    function t(key, fallback) {
        if (i18n) {
            return i18n.t(key, fallback);
        }
        return fallback || key;
    }
})();
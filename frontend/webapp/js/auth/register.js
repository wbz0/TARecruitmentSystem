/*
 * TA/MO 注册页脚本，对应 register.jsp。
 *
 * 负责角色切换、字段即时校验、用户名/邮箱可用性检查，并提交到 /api/auth/register。
 * Admin 注册不走这里，必须使用 admin-invite.jsp 的邀请码流程。
 */
(function () {
    var USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9_]{2,19}$/;
    var USERNAME_MAX_LENGTH = 20;
    var EMAIL_MAX_LENGTH = 100;
    var PASSWORD_MIN_LENGTH = 8;
    var PASSWORD_MAX_LENGTH = 100;

    var form = document.getElementById("register-form");
    if (!form) {
        return;
    }

    var usernameInput = document.getElementById("username");
    var emailInput = document.getElementById("email");
    var passwordInput = document.getElementById("password");
    var confirmPasswordInput = document.getElementById("confirm-password");
    var roleInput = document.getElementById("register-role");
    var roleButtons = form.querySelectorAll(".role-option");
    var submitButton = document.getElementById("register-submit");
    var messageBox = document.getElementById("form-message");
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var i18n = window.AppI18n && typeof window.AppI18n.t === "function" ? window.AppI18n : null;
    var isAdminOnlyPage = roleButtons.length === 0;
    var selectedRole = getNormalizedRole(roleInput ? roleInput.value : "") || "TA";

    // Availability check request counters: prevents stale responses from overwriting later results.
    var usernameCheckId = 0;
    var emailCheckId = 0;

    if (!isAdminOnlyPage && selectedRole === "ADMIN") {
        selectedRole = "TA";
    }

    setupPasswordToggles();
    setSelectedRole(selectedRole);

    Array.prototype.forEach.call(roleButtons, function (button) {
        button.addEventListener("click", function () {
            setSelectedRole(button.getAttribute("data-role"));
        });
    });

    // Per-field event listeners
    Array.prototype.forEach.call([usernameInput, emailInput, passwordInput, confirmPasswordInput], function (input) {
        if (!input) return;

        input.addEventListener("blur", function () {
            if (input.value.trim() === "") return; // empty blur → skip
            var error = getFieldError(input);
            if (error) {
                setFieldError(input, error, true);
            } else {
                clearFieldError(input);
                // Async availability check for username/email after format passes
                if (input === usernameInput) checkFieldAvailability(input, "username");
                if (input === emailInput) checkFieldAvailability(input, "email");
            }
        });

        input.addEventListener("input", function () {
            if (input.classList.contains("is-invalid")) {
                clearFieldError(input);
            }
            // When password changes, re-validate confirm-password if it has content
            if (input === passwordInput && confirmPasswordInput.value !== "") {
                var confirmError = getFieldError(confirmPasswordInput);
                if (confirmError) setFieldError(confirmPasswordInput, confirmError, false);
                else clearFieldError(confirmPasswordInput);
            }
        });
    });

    // Enter key: advance to next field, only submit on confirm-password
    var fieldOrder = [usernameInput, emailInput, passwordInput, confirmPasswordInput];
    Array.prototype.forEach.call(fieldOrder, function (input, idx) {
        if (!input) return;
        input.addEventListener("keydown", function (event) {
            if (event.key !== "Enter") return;
            event.preventDefault();
            var next = fieldOrder[idx + 1];
            if (next) {
                next.focus();
            } else {
                handleRegister(); // confirm-password is last
            }
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        handleRegister();
    });

    function handleRegister() {
        hideMessage();

        var firstInvalid = validateAll();
        if (firstInvalid) {
            firstInvalid.focus();
            return;
        }

        var role = selectedRole;
        if (!role) {
            showMessage(t("register.msg.selectRole", "Please select a role."), "error");
            return;
        }
        if (!isAdminOnlyPage && role === "ADMIN") {
            showMessage(t("register.msg.adminUsePage", "Admin accounts are created from the invite code page."), "error");
            return;
        }

        setSubmitting(true);
        submitRegister(
            getTrimmedValue(usernameInput),
            getTrimmedValue(emailInput),
            passwordInput.value,          // no trim for passwords
            confirmPasswordInput.value,    // no trim for passwords
            role
        )
            .catch(function () {
                showMessage(t("register.msg.networkError", "Network error. Please try again."), "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    // Validate all fields at once; return first invalid input or null
    function validateAll() {
        var inputs = [usernameInput, emailInput, passwordInput, confirmPasswordInput];
        var firstInvalid = null;
        Array.prototype.forEach.call(inputs, function (input) {
            if (!input) return;
            var error = getFieldError(input);
            if (error) {
                setFieldError(input, error, true);
                if (!firstInvalid) firstInvalid = input;
            } else {
                clearFieldError(input);
            }
        });
        return firstInvalid;
    }

    // Returns error message string for the given input, or null if valid
    function getFieldError(input) {
        if (!input) return null;
        var id = input.id;

        if (id === "username") {
            var val = getTrimmedValue(input);
            if (!val) return t("register.msg.enterUsername", "Please enter a username.");
            if (val.length > USERNAME_MAX_LENGTH) return t("register.msg.usernameTooLong", "Username is too long.");
            if (containsControlChars(val) || containsDangerousMarkup(val)) return t("register.msg.usernameUnsupported", "Username contains unsupported characters.");
            if (!USERNAME_PATTERN.test(val)) return t("register.msg.usernameInvalid", "Must start with a letter, 3-20 letters/numbers/underscores.");
            if (val.indexOf("__") !== -1) return t("register.msg.usernameConsecutiveUnderscore", "Username cannot contain consecutive underscores.");
            if (val.charAt(val.length - 1) === "_") return t("register.msg.usernameTrailingUnderscore", "Username cannot end with an underscore.");
            return null;
        }

        if (id === "email") {
            var val = getTrimmedValue(input);
            if (!val) return t("register.msg.enterEmail", "Please enter your email address.");
            if (val.length > EMAIL_MAX_LENGTH) return t("register.msg.emailTooLong", "Email is too long.");
            if (containsControlChars(val) || containsDangerousMarkup(val)) return t("register.msg.emailUnsupported", "Email contains unsupported characters.");
            if (!isValidEmailAddress(val)) return t("register.msg.emailInvalid", "Please enter a valid email address.");
            return null;
        }

        if (id === "password") {
            var val = input.value; // no trim for passwords
            if (!val) return t("register.msg.enterPassword", "Please create a password.");
            if (val.length < PASSWORD_MIN_LENGTH) return t("register.msg.passwordTooShort", "Password must be at least 8 characters.");
            if (val.length > PASSWORD_MAX_LENGTH) return t("register.msg.passwordTooLong", "Password is too long.");
            if (containsControlChars(val)) return t("register.msg.passwordUnsupported", "Password contains unsupported characters.");
            if (!/[A-Za-z]/.test(val) || !/[0-9]/.test(val)) return t("register.msg.passwordTooSimple", "Password must contain at least one letter and one number.");
            return null;
        }

        if (id === "confirm-password") {
            var val = input.value; // no trim for passwords
            if (!val) return t("register.msg.enterConfirmPassword", "Please confirm your password.");
            if (passwordInput.value !== val) return t("register.msg.passwordMismatch", "Passwords do not match.");
            return null;
        }

        return null;
    }

    // Async availability check for username/email (fires only when format is valid)
    function checkFieldAvailability(input, type) {
        var value = getTrimmedValue(input);
        if (!value) return;

        // 每次 blur 都递增 id，较慢的旧请求回来时不会覆盖新的校验结果。
        var checkId;
        if (type === "username") {
            usernameCheckId += 1;
            checkId = usernameCheckId;
        } else {
            emailCheckId += 1;
            checkId = emailCheckId;
        }

        var url = window.TARecruitment.routes.auth.availability(type, value);
        fetch(url, { headers: { "X-Requested-With": "XMLHttpRequest" } })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                var currentId = type === "username" ? usernameCheckId : emailCheckId;
                if (checkId !== currentId) return; // stale response
                if (data && data.data && data.data.available === false) {
                    var msg = type === "username"
                        ? t("register.msg.usernameUnavailable", "Username is already taken.")
                        : t("register.msg.emailUnavailable", "Email is already registered.");
                    setFieldError(input, msg, false); // no flash for availability errors
                }
            })
            .catch(function () { /* silently ignore network errors on availability check */ });
    }

    // Show inline error on a field with optional flash animation
    function setFieldError(input, message, animate) {
        var errorEl = document.getElementById(input.id + "-error");
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.classList.add("visible");
        }
        input.classList.add("is-invalid");
        if (animate) {
            input.classList.remove("is-flashing");
            requestAnimationFrame(function () {
                requestAnimationFrame(function () {
                    input.classList.add("is-flashing");
                });
            });
        }
    }

    // Clear inline error state from a field
    function clearFieldError(input) {
        var errorEl = document.getElementById(input.id + "-error");
        if (errorEl) {
            errorEl.textContent = "";
            errorEl.classList.remove("visible");
        }
        input.classList.remove("is-invalid", "is-flashing");
    }

    function submitRegister(username, email, password, confirmPassword, role) {
        // 页面允许 TA/MO；ADMIN 会被 getNormalizedRole 拦截并引导到邀请注册页。
        var formData = new URLSearchParams();
        formData.set("username", username);
        formData.set("email", email);
        formData.set("password", password);
        formData.set("confirmPassword", confirmPassword);
        formData.set("role", role);

        return fetch(window.TARecruitment.routes.auth.register(), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        })
            .then(function (response) {
                return response.text().then(function (bodyText) {
                    return { response: response, payload: JSON.parse(bodyText) };
                });
            })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (!payload || payload.success !== true || !response.ok) {
                    var errorMessage = t("register.msg.failed", "Registration failed. Please check your information and try again.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "register.msg.failed", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                showMessage(t("register.msg.successRedirect", "Registration successful! Redirecting to login..."), "success");
                window.setTimeout(function () {
                    window.location.href = contextPath + "/login.jsp";
                }, 1200);
            });
    }

    function setSubmitting(submitting) {
        submitButton.disabled = submitting;
        Array.prototype.forEach.call(roleButtons, function (button) {
            button.disabled = submitting;
        });
    }

    function setSelectedRole(role) {
        var normalizedRole = getNormalizedRole(role);
        if (!normalizedRole) return;
        selectedRole = normalizedRole;
        if (roleInput) roleInput.value = normalizedRole;
        Array.prototype.forEach.call(roleButtons, function (button) {
            var buttonRole = getNormalizedRole(button.getAttribute("data-role"));
            var isSelected = buttonRole === normalizedRole;
            button.classList.toggle("is-selected", isSelected);
            button.setAttribute("aria-pressed", isSelected ? "true" : "false");
        });
    }

    function getNormalizedRole(value) {
        if (typeof value !== "string") return "";
        var normalized = value.trim().toUpperCase();
        if (!isAdminOnlyPage && normalized === "ADMIN") return "";
        if (normalized === "TA" || normalized === "MO" || normalized === "ADMIN") return normalized;
        return "";
    }

    function getTrimmedValue(input) {
        if (!input || typeof input.value !== "string") return "";
        return input.value.trim();
    }

    function containsControlChars(value) {
        return /[\u0000-\u001F\u007F]/.test(value || "");
    }

    function containsDangerousMarkup(value) {
        if (typeof value !== "string" || !value) return false;
        return /<[^>]*>/.test(value) || /javascript:/i.test(value) || /on\w+\s*=/.test(value);
    }

    function isValidEmailAddress(email) {
        if (typeof email !== "string") return false;
        if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) return false;
        var parts = email.split("@");
        if (parts.length !== 2) return false;
        var local = parts[0];
        var domain = parts[1];
        if (!local || !domain) return false;
        if (local.charAt(0) === "." || local.charAt(local.length - 1) === "." || local.indexOf("..") !== -1) return false;
        if (domain.charAt(0) === "." || domain.charAt(domain.length - 1) === "." || domain.indexOf("..") !== -1) return false;
        return true;
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

    function t(key, fallback) {
        if (i18n) return i18n.t(key, fallback);
        return fallback || key;
    }

    function localizeServerMessage(message, fallbackKey, fallbackText) {
        if (window.AppI18n && typeof window.AppI18n.localizeServerMessage === "function") {
            return window.AppI18n.localizeServerMessage(message, fallbackKey, fallbackText);
        }
        if (typeof message === "string" && message.trim()) return message.trim();
        return fallbackKey ? t(fallbackKey, fallbackText) : (fallbackText || "");
    }
})();

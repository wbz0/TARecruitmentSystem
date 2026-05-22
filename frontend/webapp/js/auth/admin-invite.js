/*
 * Admin invite code registration page script, corresponds to admin-invite.jsp.
 *
 * Current main flow: enter 8-digit short invite code from admin, then submit to
 * /api/admin/invitations/acceptance to create ADMIN account.
 */
(function () {
    var USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9_]{2,19}$/;
    var EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    var USERNAME_MAX_LENGTH = 20;
    var EMAIL_MAX_LENGTH = 100;
    var PASSWORD_MIN_LENGTH = 8;
    var PASSWORD_MAX_LENGTH = 100;

    var form = document.getElementById("admin-invite-form");
    if (!form) return;

    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var i18n = window.AppI18n && typeof window.AppI18n.t === "function" ? window.AppI18n : null;
    var messageBox = document.getElementById("form-message");
    var emailInput = document.getElementById("email");
    var inviteCodeInput = document.getElementById("invite-code");
    var usernameInput = document.getElementById("username");
    var passwordInput = document.getElementById("password");
    var confirmPasswordInput = document.getElementById("confirm-password");
    var submitButton = document.getElementById("invite-submit");

    // Availability check request counters: prevents stale responses from overwriting later results.
    var usernameCheckId = 0;
    var emailCheckId = 0;

    setupPasswordToggles();

    // Per-field event listeners for validated fields
    var validatedFields = [emailInput, inviteCodeInput, usernameInput, passwordInput, confirmPasswordInput];
    Array.prototype.forEach.call(validatedFields, function (input) {
        if (!input) return;

        input.addEventListener("blur", function () {
            if (input.value.trim() === "") return; // empty blur -> skip
            var error = getFieldError(input);
            if (error) {
                setFieldError(input, error, true);
            } else {
                clearFieldError(input);
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

    // Enter key: advance to next field; only confirm-password submits
    var fieldOrder = [emailInput, inviteCodeInput, usernameInput, passwordInput, confirmPasswordInput];
    Array.prototype.forEach.call(fieldOrder, function (input, idx) {
        if (!input) return;
        input.addEventListener("keydown", function (event) {
            if (event.key !== "Enter") return;
            event.preventDefault();
            var next = fieldOrder[idx + 1];
            if (next) {
                next.focus();
            } else {
                handleSubmit();
            }
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        handleSubmit();
    });

    function handleSubmit() {
        hideMessage();

        var firstInvalid = validateAll();
        if (firstInvalid) {
            firstInvalid.focus();
            return;
        }

        var inviteCode = inviteCodeInput ? inviteCodeInput.value.replace(/\s+/g, "").toUpperCase() : "";

        setSubmitting(true);
        // No longer submitting token; old email link token flow is a legacy backend interface.
        var formData = new URLSearchParams();
        formData.set("email", getTrimmedValue(emailInput).toLowerCase());
        formData.set("username", getTrimmedValue(usernameInput).toLowerCase());
        formData.set("password", passwordInput.value);          // no trim for passwords
        formData.set("confirmPassword", confirmPasswordInput.value); // no trim for passwords
        formData.set("inviteCode", inviteCode);

        fetch(window.TARecruitment.routes.admin.invitationAcceptance(), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        })
            .then(function (response) {
                return response.text().then(function (text) {
                    return { response: response, payload: JSON.parse(text) };
                });
            })
            .then(function (result) {
                var payload = result.payload;
                if (!result.response.ok || !payload || payload.success !== true) {
                    var errorMsg = t("adminInvite.msg.createFailed", "Failed to create admin account.");
                    var errorKey = "";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        var localizedError = localizeServerMessage(payload.message);
                        errorMsg = localizedError.message;
                        errorKey = localizedError.key;
                    }
                    showMessage(errorMsg, "error", errorKey);
                    return;
                }
                showMessage(t("adminInvite.msg.createSuccessRedirect", "Admin account created. Redirecting to login..."), "success");
                window.setTimeout(function () {
                    window.location.href = contextPath + "/login.jsp";
                }, 1200);
            })
            .catch(function () {
                showMessage(t("adminInvite.msg.networkError", "Network error. Please try again."), "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    // Validate all fields at once; return first invalid input or null
    function validateAll() {
        var inputs = [emailInput, inviteCodeInput, usernameInput, passwordInput, confirmPasswordInput];
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

        if (id === "invite-code") {
            var val = input.value.trim();
            if (!val) return t("adminInvite.msg.inviteCodeRequired", "Invite code is required.");
            return null;
        }

        if (id === "email") {
            var val = getTrimmedValue(input);
            if (!val) return t("adminInvite.msg.enterEmail", "Please enter your email address.");
            if (val.length > EMAIL_MAX_LENGTH) return t("adminInvite.msg.emailTooLong", "Email is too long.");
            if (containsControlChars(val) || containsDangerousMarkup(val)) return t("adminInvite.msg.emailUnsupported", "Email contains unsupported characters.");
            if (!isValidEmailAddress(val)) return t("adminInvite.msg.emailInvalid", "Please enter a valid email address.");
            return null;
        }

        if (id === "username") {
            var val = getTrimmedValue(input);
            if (!val) return t("adminInvite.msg.enterUsername", "Please enter a username.");
            if (val.length > USERNAME_MAX_LENGTH) return t("adminInvite.msg.usernameTooLong", "Username is too long.");
            if (containsControlChars(val) || containsDangerousMarkup(val)) return t("adminInvite.msg.usernameUnsupported", "Username contains unsupported characters.");
            if (!USERNAME_PATTERN.test(val)) return t("adminInvite.msg.usernameInvalid", "Must start with a letter, 3-20 letters/numbers/underscores.");
            if (val.indexOf("__") !== -1) return t("adminInvite.msg.usernameConsecutiveUnderscore", "Username cannot contain consecutive underscores.");
            if (val.charAt(val.length - 1) === "_") return t("adminInvite.msg.usernameTrailingUnderscore", "Username cannot end with an underscore.");
            return null;
        }

        if (id === "password") {
            var val = input.value; // no trim for passwords
            if (!val) return t("adminInvite.msg.enterPassword", "Please create a password.");
            if (val.length < PASSWORD_MIN_LENGTH) return t("adminInvite.msg.passwordTooShort", "Password must be at least 8 characters.");
            if (val.length > PASSWORD_MAX_LENGTH) return t("adminInvite.msg.passwordTooLong", "Password is too long.");
            if (containsControlChars(val)) return t("adminInvite.msg.passwordUnsupported", "Password contains unsupported characters.");
            if (!/[A-Za-z]/.test(val) || !/[0-9]/.test(val)) return t("adminInvite.msg.passwordTooSimple", "Password must contain at least one letter and one number.");
            return null;
        }

        if (id === "confirm-password") {
            var val = input.value; // no trim for passwords
            if (!val) return t("adminInvite.msg.enterConfirmPassword", "Please confirm your password.");
            if (passwordInput.value !== val) return t("adminInvite.msg.passwordMismatch", "Passwords do not match.");
            return null;
        }

        return null;
    }

    // Async availability check for username/email (fires only when format is valid)
    function checkFieldAvailability(input, type) {
        var value = getTrimmedValue(input);
        if (!value) return;

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
                        ? t("adminInvite.msg.usernameUnavailable", "Username is already taken.")
                        : t("adminInvite.msg.emailUnavailable", "Email is already registered.");
                    setFieldError(input, msg, false); // no flash for availability errors
                }
            })
            .catch(function () { /* silently ignore */ });
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

    function setSubmitting(submitting) {
        if (submitButton) {
            submitButton.disabled = submitting;
            submitButton.textContent = submitting
                ? t("adminInvite.msg.creating", "Creating...")
                : t("adminInvite.form.submit", "Create admin account");
        }
    }

    function showMessage(message, type, i18nKey) {
        if (!messageBox) return;
        if (i18nKey) {
            messageBox.setAttribute("data-i18n", i18nKey);
        } else {
            messageBox.removeAttribute("data-i18n");
        }
        messageBox.textContent = message;
        messageBox.classList.remove("hidden", "error", "success");
        messageBox.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        if (!messageBox) return;
        messageBox.textContent = "";
        messageBox.removeAttribute("data-i18n");
        messageBox.classList.remove("error", "success");
        messageBox.classList.add("hidden");
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
        if (!EMAIL_PATTERN.test(email)) return false;
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

    function localizeServerMessage(message) {
        var text = typeof message === "string" ? message.trim() : "";
        var normalized = text.toLowerCase();
        var key = "";
        if (normalized === "invite code is invalid or expired") {
            // Current page only needs short invite code error mapping; old token verification error has no visible page entry.
            key = "server.adminInvite.codeInvalidOrExpired";
        }
        if (window.AppI18n && typeof window.AppI18n.localizeServerMessage === "function") {
            return {
                key: key,
                message: window.AppI18n.localizeServerMessage(text, "adminInvite.msg.createFailed", "Failed to create admin account.")
            };
        }
        if (key) {
            return {
                key: key,
                message: t(key, text)
            };
        }
        return {
            key: "",
            message: text || t("adminInvite.msg.createFailed", "Failed to create admin account.")
        };
    }

    function t(key, fallback) {
        if (i18n) return i18n.t(key, fallback);
        return fallback || key;
    }
})();
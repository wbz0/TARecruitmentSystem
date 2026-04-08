(function () {
    var USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9_]{2,19}$/;
    var USERNAME_MAX_LENGTH = 20;
    var EMAIL_MAX_LENGTH = 100;
    var PASSWORD_MIN_LENGTH = 6;
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
    var isAdminOnlyPage = roleButtons.length === 0;
    var selectedRole = getNormalizedRole(roleInput ? roleInput.value : "") || "TA";

    if (!isAdminOnlyPage && selectedRole === "ADMIN") {
        selectedRole = "TA";
    }

    setSelectedRole(selectedRole);

    Array.prototype.forEach.call(roleButtons, function (button) {
        button.addEventListener("click", function () {
            setSelectedRole(button.getAttribute("data-role"));
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        handleRegister();
    });

    function handleRegister() {
        hideMessage();

        var username = getTrimmedValue(usernameInput);
        var email = getTrimmedValue(emailInput);
        var password = getTrimmedValue(passwordInput);
        var confirmPassword = getTrimmedValue(confirmPasswordInput);
        var role = selectedRole;

        if (!username) {
            showMessage("Please enter a username.", "error");
            usernameInput.focus();
            return;
        }

        if (username.length > USERNAME_MAX_LENGTH) {
            showMessage("Username is too long.", "error");
            usernameInput.focus();
            return;
        }

        if (containsControlChars(username) || containsDangerousMarkup(username)) {
            showMessage("Username contains unsupported characters.", "error");
            usernameInput.focus();
            return;
        }

        if (!USERNAME_PATTERN.test(username)) {
            showMessage("Username must start with a letter and contain 3-20 letters, numbers, or underscores.", "error");
            usernameInput.focus();
            return;
        }

        if (!email) {
            showMessage("Please enter your email address.", "error");
            emailInput.focus();
            return;
        }

        if (email.length > EMAIL_MAX_LENGTH) {
            showMessage("Email is too long.", "error");
            emailInput.focus();
            return;
        }

        if (containsControlChars(email) || containsDangerousMarkup(email)) {
            showMessage("Email contains unsupported characters.", "error");
            emailInput.focus();
            return;
        }

        if (!isValidEmailAddress(email)) {
            showMessage("Please enter a valid email address.", "error");
            emailInput.focus();
            return;
        }

        if (!password) {
            showMessage("Please create a password.", "error");
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

        if (!confirmPassword) {
            showMessage("Please confirm your password.", "error");
            confirmPasswordInput.focus();
            return;
        }

        if (password !== confirmPassword) {
            showMessage("Passwords do not match.", "error");
            confirmPasswordInput.focus();
            return;
        }

        if (!role) {
            showMessage("Please select a role.", "error");
            return;
        }
        if (!isAdminOnlyPage && role === "ADMIN") {
            showMessage("Please use admin registration page for Admin account.", "error");
            return;
        }

        setSubmitting(true);
        submitRegister(username, email, password, confirmPassword, role)
            .catch(function () {
                showMessage("Network error. Please try again.", "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    function submitRegister(username, email, password, confirmPassword, role) {
        var formData = new URLSearchParams();
        formData.set("username", username);
        formData.set("email", email);
        formData.set("password", password);
        formData.set("confirmPassword", confirmPassword);
        formData.set("role", role);

        return fetch(contextPath + "/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        })
            .then(function (response) {
                return response.text().then(function (bodyText) {
                    var payload = parseResponse(bodyText);
                    return { response: response, payload: payload };
                });
            })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (!payload || payload.success !== true || !response.ok) {
                    var errorMessage = "Registration failed. Please check your information and try again.";
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = payload.message.trim();
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                showMessage("Registration successful! Redirecting to login...", "success");
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
        if (!isAdminOnlyPage && normalized === "ADMIN") {
            return "";
        }
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

    function parseResponse(bodyText) {
        try {
            return JSON.parse(bodyText);
        } catch (error) {
            return parseLegacyResponse(bodyText);
        }
    }

    function parseLegacyResponse(bodyText) {
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

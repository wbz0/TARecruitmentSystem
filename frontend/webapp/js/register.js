(function () {
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
    var selectedRole = getNormalizedRole(roleInput ? roleInput.value : "") || "TA";

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

        var username = usernameInput.value.trim();
        var email = emailInput.value.trim();
        var password = passwordInput.value;
        var confirmPassword = confirmPasswordInput.value;
        var role = selectedRole;

        if (!username) {
            showMessage("Please enter a username.", "error");
            usernameInput.focus();
            return;
        }

        if (!/^[A-Za-z][A-Za-z0-9_]{2,19}$/.test(username)) {
            showMessage("Username must start with a letter and contain 3-20 letters, numbers, or underscores.", "error");
            usernameInput.focus();
            return;
        }

        if (!email) {
            showMessage("Please enter your email address.", "error");
            emailInput.focus();
            return;
        }

        if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
            showMessage("Please enter a valid email address.", "error");
            emailInput.focus();
            return;
        }

        if (!password) {
            showMessage("Please create a password.", "error");
            passwordInput.focus();
            return;
        }

        if (password.length < 6) {
            showMessage("Password must be at least 6 characters.", "error");
            passwordInput.focus();
            return;
        }

        if (password.length > 100) {
            showMessage("Password is too long.", "error");
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

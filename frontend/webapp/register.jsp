<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户注册 - TA 招聘系统</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/register.css">
</head>
<body>
    <main class="register-page">
        <section class="register-hero" aria-labelledby="register-title">
            <div class="hero-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                    <path d="M12 4.5L20 8.5L12 12.5L4 8.5L12 4.5ZM7.2 10.1V14.2C7.2 16.6 9.5 18.4 12 18.4C14.5 18.4 16.8 16.6 16.8 14.2V10.1L12 12.5L7.2 10.1Z" />
                </svg>
            </div>
            <h1 id="register-title">Create your account</h1>
            <p class="subtitle">Join the TA Hiring Portal in a few steps</p>
        </section>

        <section class="register-card" aria-label="注册表单">
            <div id="form-message" class="form-message hidden" role="alert" aria-live="polite"></div>

            <form id="register-form" class="register-form" method="post" action="<%= contextPath %>/register" novalidate>
                <div class="field">
                    <div class="field-label-row">
                        <label for="username">Username</label>
                    </div>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="john_smith"
                        autocomplete="username"
                        maxlength="20"
                        required
                    >
                    <p class="field-hint">3-20 characters, start with a letter, and use only letters, numbers, or underscores.</p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="email">Email address</label>
                    </div>
                    <input
                        id="email"
                        name="email"
                        type="email"
                        placeholder="name@university.edu"
                        autocomplete="email"
                        maxlength="100"
                        inputmode="email"
                        required
                    >
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="password">Password</label>
                    </div>
                    <input
                        id="password"
                        name="password"
                        type="password"
                        placeholder="Create a password"
                        autocomplete="new-password"
                        maxlength="100"
                        required
                    >
                    <p class="field-hint">Use at least 6 characters.</p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="confirm-password">Confirm password</label>
                    </div>
                    <input
                        id="confirm-password"
                        name="confirmPassword"
                        type="password"
                        placeholder="Re-enter your password"
                        autocomplete="new-password"
                        maxlength="100"
                        required
                    >
                </div>

                <div class="field role-field">
                    <div class="field-label-row">
                        <span class="field-label">Register as</span>
                    </div>
                    <input id="register-role" type="hidden" name="role" value="TA">
                    <div class="role-selector" role="group" aria-label="角色选择按钮">
                        <button
                            class="role-option is-selected"
                            type="button"
                            data-role="TA"
                            aria-pressed="true"
                        >
                            <span class="role-option-title">TA</span>
                            <span class="role-option-desc">Applicant</span>
                        </button>
                        <button
                            class="role-option"
                            type="button"
                            data-role="MO"
                            aria-pressed="false"
                        >
                            <span class="role-option-title">MO</span>
                            <span class="role-option-desc">Module Organizer</span>
                        </button>
                    </div>
                </div>

                <button id="register-submit" class="register-submit-btn" type="submit">
                    Create account
                </button>
            </form>
        </section>

        <p class="page-switch-hint">
            Already have an account?
            <a href="<%= contextPath %>/login.jsp">Back to login</a>
        </p>

        <p class="login-footer">University Hiring System © 2026</p>
    </main>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/register.js" defer></script>
</body>
</html>

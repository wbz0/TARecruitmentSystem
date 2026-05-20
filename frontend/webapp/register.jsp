<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>
<%-- TA/MO 注册页：Admin 注册不在这里开放，必须走 admin-invite.jsp 的邀请码流程。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js?v=20260513-password-toggle"></script>
    <title data-i18n="register.page.title">Register - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/auth/register.css?v=20260513-password-toggle">
</head>
<body>
    <main class="register-page">
        <div class="page-utility">
            <a class="utility-link" href="<%= contextPath %>/" data-i18n="common.utility.backToPortal">Portal home</a>
            <div class="locale-switch" role="group" data-i18n-aria-label="common.locale.switchAria">
                <button class="locale-btn" type="button" data-locale-switch data-locale="zh-CN" data-i18n="common.locale.zh">中文</button>
                <span class="locale-divider">/</span>
                <button class="locale-btn" type="button" data-locale-switch data-locale="en" data-i18n="common.locale.en">English</button>
            </div>
        </div>

        <section class="register-hero" aria-labelledby="register-title">
            <div class="hero-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                    <path d="M12 4.5L20 8.5L12 12.5L4 8.5L12 4.5ZM7.2 10.1V14.2C7.2 16.6 9.5 18.4 12 18.4C14.5 18.4 16.8 16.6 16.8 14.2V10.1L12 12.5L7.2 10.1Z" />
                </svg>
            </div>
            <h1 id="register-title" data-i18n="register.hero.title">Create your account</h1>
            <p class="subtitle" data-i18n="register.hero.subtitle">Join TA Hiring System in a few steps</p>
        </section>

        <section class="register-card" data-i18n-aria-label="register.form.aria">
            <div id="form-message" class="form-message hidden" role="alert" aria-live="polite"></div>

            <form id="register-form" class="register-form" method="post" action="<%= contextPath %>/api/auth/register" novalidate>
                <div class="field">
                    <div class="field-label-row">
                        <label for="username" data-i18n="register.form.usernameLabel">Username</label>
                        <button type="button" class="field-info-btn" aria-label="Username rules" data-i18n-aria-label="register.form.usernameInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="register.form.usernameTooltip">3-20 characters, start with a letter, letters/numbers/underscore only.</span>
                        </button>
                    </div>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="john_smith"
                        data-i18n-placeholder="register.form.usernamePlaceholder"
                        autocomplete="username"
                        maxlength="20"
                        required
                    >
                    <p class="field-error" id="username-error" role="alert" aria-live="polite"></p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="email" data-i18n="register.form.emailLabel">Email address</label>
                        <button type="button" class="field-info-btn" aria-label="Email rules" data-i18n-aria-label="register.form.emailInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="register.form.emailTooltip">Enter a valid email address (e.g. name@university.edu).</span>
                        </button>
                    </div>
                    <input
                        id="email"
                        name="email"
                        type="email"
                        placeholder="name@university.edu"
                        data-i18n-placeholder="register.form.emailPlaceholder"
                        autocomplete="email"
                        maxlength="100"
                        inputmode="email"
                        required
                    >
                    <p class="field-error" id="email-error" role="alert" aria-live="polite"></p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="password" data-i18n="register.form.passwordLabel">Password</label>
                        <button type="button" class="field-info-btn" aria-label="Password rules" data-i18n-aria-label="register.form.passwordInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="register.form.passwordTooltip">At least 6 characters.</span>
                        </button>
                    </div>
                    <div class="password-input-wrap">
                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Create a password"
                            data-i18n-placeholder="register.form.passwordPlaceholder"
                            autocomplete="new-password"
                            maxlength="100"
                            required
                        >
                        <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" data-i18n-aria-label="common.password.show">
                            <svg class="password-toggle-eye" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M2.75 12s3.25-6 9.25-6s9.25 6 9.25 6-3.25 6-9.25 6-9.25-6-9.25-6z"></path>
                                <circle cx="12" cy="12" r="2.6"></circle>
                            </svg>
                            <svg class="password-toggle-eye-off" viewBox="0 0 24 24" aria-hidden="true" focusable="false" hidden>
                                <path d="M3 3l18 18"></path>
                                <path d="M10.6 10.6A2.6 2.6 0 0 0 12 14.6c.7 0 1.35-.28 1.82-.74"></path>
                                <path d="M7.1 7.6C4.28 9.22 2.75 12 2.75 12s3.25 6 9.25 6c1.7 0 3.18-.48 4.43-1.18"></path>
                                <path d="M12 6c6 0 9.25 6 9.25 6a16.3 16.3 0 0 1-2.54 3.3"></path>
                            </svg>
                        </button>
                    </div>
                    <p class="field-error" id="password-error" role="alert" aria-live="polite"></p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="confirm-password" data-i18n="register.form.confirmLabel">Confirm password</label>
                        <button type="button" class="field-info-btn" aria-label="Confirm password rules" data-i18n-aria-label="register.form.confirmInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="register.form.confirmTooltip">Re-enter the password you created above.</span>
                        </button>
                    </div>
                    <div class="password-input-wrap">
                        <input
                            id="confirm-password"
                            name="confirmPassword"
                            type="password"
                            placeholder="Re-enter your password"
                            data-i18n-placeholder="register.form.confirmPlaceholder"
                            autocomplete="new-password"
                            maxlength="100"
                            required
                        >
                        <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" data-i18n-aria-label="common.password.show">
                            <svg class="password-toggle-eye" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <path d="M2.75 12s3.25-6 9.25-6s9.25 6 9.25 6-3.25 6-9.25 6-9.25-6-9.25-6z"></path>
                                <circle cx="12" cy="12" r="2.6"></circle>
                            </svg>
                            <svg class="password-toggle-eye-off" viewBox="0 0 24 24" aria-hidden="true" focusable="false" hidden>
                                <path d="M3 3l18 18"></path>
                                <path d="M10.6 10.6A2.6 2.6 0 0 0 12 14.6c.7 0 1.35-.28 1.82-.74"></path>
                                <path d="M7.1 7.6C4.28 9.22 2.75 12 2.75 12s3.25 6 9.25 6c1.7 0 3.18-.48 4.43-1.18"></path>
                                <path d="M12 6c6 0 9.25 6 9.25 6a16.3 16.3 0 0 1-2.54 3.3"></path>
                            </svg>
                        </button>
                    </div>
                    <p class="field-error" id="confirm-password-error" role="alert" aria-live="polite"></p>
                </div>

                <div class="field role-field">
                    <div class="field-label-row">
                        <span class="field-label" data-i18n="register.form.roleLabel">Register as</span>
                    </div>
                    <input id="register-role" type="hidden" name="role" value="TA">
                    <div class="role-selector" role="group" data-i18n-aria-label="register.form.roleAria">
                        <button
                            class="role-option is-selected"
                            type="button"
                            data-role="TA"
                            aria-pressed="true"
                        >
                            <span class="role-option-title" data-i18n="register.form.roleTaTitle">TA</span>
                            <span class="role-option-desc" data-i18n="register.form.roleTaDesc">Applicant</span>
                        </button>
                        <button
                            class="role-option"
                            type="button"
                            data-role="MO"
                            aria-pressed="false"
                        >
                            <span class="role-option-title" data-i18n="register.form.roleMoTitle">MO</span>
                            <span class="role-option-desc" data-i18n="register.form.roleMoDesc">Module Organizer</span>
                        </button>
                    </div>
                </div>

                <button id="register-submit" class="register-submit-btn" type="submit" data-i18n="register.form.submit">Create account</button>
            </form>
        </section>

        <p class="page-switch-hint">
            <span data-i18n="register.links.haveAccount">Already have an account?</span>
            <a href="<%= contextPath %>/login.jsp" data-i18n="register.links.backLogin">Back to login</a>
        </p>
        <p class="page-switch-hint">
            <span data-i18n="register.links.adminQuestion">Need an Admin account?</span>
            <a href="<%= contextPath %>/admin-invite.jsp" data-i18n="register.links.adminLink">Use admin invitation</a>
        </p>

        <p class="login-footer" data-i18n="common.footer.copyright">TA Hiring System © 2026</p>
    </main>

    <script>
        // 注入给公共 routes 工具，避免前端在不同 context path 下请求错接口。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js?v=20260513-password-toggle" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/auth/register.js?v=20260513-password-toggle" defer></script>
</body>
</html>

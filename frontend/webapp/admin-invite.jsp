<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>
<%-- Current admin registration main page: enters short invite code and calls /api/admin/invitations/acceptance. --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js?v=20260513-admin-invite-i18n"></script>
    <title data-i18n="adminInvite.page.title">Admin Invitation - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/auth/register.css?v=20260513-admin-invite-i18n">
</head>
<body>
    <main class="register-page">
        <div class="page-utility">
            <a class="utility-link" href="<%= contextPath %>/" data-i18n="common.utility.backToPortal">Portal home</a>
            <div class="locale-switch" role="group" data-i18n-aria-label="common.locale.switchAria">
                <button class="locale-btn" type="button" data-locale-switch data-locale="zh-CN" data-i18n="common.locale.zh">Chinese</button>
                <span class="locale-divider">/</span>
                <button class="locale-btn" type="button" data-locale-switch data-locale="en" data-i18n="common.locale.en">English</button>
            </div>
        </div>

        <section class="register-hero" aria-labelledby="invite-title">
            <div class="hero-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                    <path d="M12 4.5L20 8.5L12 12.5L4 8.5L12 4.5ZM7.2 10.1V14.2C7.2 16.6 9.5 18.4 12 18.4C14.5 18.4 16.8 16.6 16.8 14.2V10.1L12 12.5L7.2 10.1Z" />
                </svg>
            </div>
            <h1 id="invite-title" data-i18n="adminInvite.hero.title">Complete admin invitation</h1>
            <p class="subtitle" data-i18n="adminInvite.hero.subtitle">Use an invite code from the team to create an Admin account</p>
        </section>

        <section class="register-card" aria-label="Admin invite registration form" data-i18n-aria-label="adminInvite.form.aria">
            <div id="form-message" class="form-message hidden" role="alert" aria-live="polite"></div>

            <div class="invite-contact-hint">
                <p data-i18n="adminInvite.contactHint.intro">To get an invite code, send an email from the address you plan to register with to the contact below. You will receive the invite code in reply.</p>
                <p><span data-i18n="adminInvite.contactHint.contactLabel">Contact:</span> <a href="mailto:admin@example.com" data-i18n-href="adminInvite.contactHint.contactEmail"><span data-i18n="adminInvite.contactHint.contactEmail">admin@example.com</span></a></p>
            </div>

            <form id="admin-invite-form" class="register-form" method="post" action="<%= contextPath %>/api/admin/invitations/acceptance" novalidate>

                <div class="field">
                    <div class="field-label-row">
                        <label for="email" data-i18n="adminInvite.form.emailLabel">Email address</label>
                        <button type="button" class="field-info-btn" aria-label="Email address requirements" data-i18n-aria-label="adminInvite.form.emailInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="adminInvite.form.emailTooltip">Enter the email address the admin invitation was sent to.</span>
                        </button>
                    </div>
                    <input
                        id="email"
                        name="email"
                        type="email"
                        placeholder="admin@university.edu"
                        data-i18n-placeholder="adminInvite.form.emailPlaceholder"
                        autocomplete="email"
                        maxlength="100"
                        required
                    >
                    <p class="field-error" id="email-error" role="alert" aria-live="polite"></p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="invite-code" data-i18n="adminInvite.form.inviteCodeLabel">Invite code</label>
                        <button type="button" class="field-info-btn" aria-label="Invite code help" data-i18n-aria-label="adminInvite.form.inviteCodeInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="adminInvite.form.inviteCodeTooltip">Enter the 8-character invite code provided by an admin.</span>
                        </button>
                    </div>
                    <input
                        id="invite-code"
                        name="inviteCode"
                        type="text"
                        placeholder="ABCDEFGH"
                        data-i18n-placeholder="adminInvite.form.inviteCodePlaceholder"
                        autocomplete="one-time-code"
                        maxlength="16"
                        required
                    >
                    <p class="field-error" id="invite-code-error" role="alert" aria-live="polite"></p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="username" data-i18n="adminInvite.form.usernameLabel">Username</label>
                        <button type="button" class="field-info-btn" aria-label="Username requirements" data-i18n-aria-label="adminInvite.form.usernameInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="adminInvite.form.usernameTooltip">3-20 chars, start with a letter, letters/numbers/underscore. No consecutive __ or trailing _.</span>
                        </button>
                    </div>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="admin_username"
                        data-i18n-placeholder="adminInvite.form.usernamePlaceholder"
                        autocomplete="username"
                        maxlength="20"
                        required
                    >
                    <p class="field-error" id="username-error" role="alert" aria-live="polite"></p>
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="password" data-i18n="adminInvite.form.passwordLabel">Password</label>
                        <button type="button" class="field-info-btn" aria-label="Password requirements" data-i18n-aria-label="adminInvite.form.passwordInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="adminInvite.form.passwordTooltip">At least 8 characters, including at least one letter and one number.</span>
                        </button>
                    </div>
                    <div class="password-input-wrap">
                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Create a password"
                            data-i18n-placeholder="adminInvite.form.passwordPlaceholder"
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
                        <label for="confirm-password" data-i18n="adminInvite.form.confirmLabel">Confirm password</label>
                        <button type="button" class="field-info-btn" aria-label="Confirm password requirements" data-i18n-aria-label="adminInvite.form.confirmInfoAria">
                            <span aria-hidden="true">i</span>
                            <span class="field-tooltip" data-i18n="adminInvite.form.confirmTooltip">Re-enter your password to confirm.</span>
                        </button>
                    </div>
                    <div class="password-input-wrap">
                        <input
                            id="confirm-password"
                            name="confirmPassword"
                            type="password"
                            placeholder="Re-enter your password"
                            data-i18n-placeholder="adminInvite.form.confirmPlaceholder"
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

                <button id="invite-submit" class="register-submit-btn" type="submit" data-i18n="adminInvite.form.submit">Create admin account</button>
            </form>
        </section>

        <p class="page-switch-hint">
            <span data-i18n="adminInvite.links.haveAccount">Already have an account?</span>
            <a href="<%= contextPath %>/login.jsp" data-i18n="adminInvite.links.backLogin">Back to login</a>
        </p>
        <p class="login-footer" data-i18n="common.footer.copyright">University Hiring System © 2026</p>
    </main>

    <script>
        // Injected for admin-invite.js; this page no longer depends on the old email token parameter.
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js?v=20260513-admin-invite-i18n" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/auth/admin-invite.js?v=20260513-admin-invite-i18n" defer></script>
</body>
</html>

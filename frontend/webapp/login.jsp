<%@ page contentType="text/html;charset=UTF-8" language="java" import="com.example.tarecruitment.auth.dao.UserDao" %>
<%
    UserDao.getInstance();
    String contextPath = request.getContextPath();
%>
<%-- 登录页：只渲染表单和全局 contextPath；登录校验与跳转逻辑在 js/auth/login.js。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js?v=20260513-password-toggle"></script>
    <title data-i18n="login.page.title">Login - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/auth/login.css?v=20260513-password-toggle">
</head>
<body>
    <main class="login-page">
        <div class="page-utility">
            <a class="utility-link" href="<%= contextPath %>/" data-i18n="common.utility.backToPortal">Portal home</a>
            <div class="locale-switch" role="group" data-i18n-aria-label="common.locale.switchAria">
                <button class="locale-btn" type="button" data-locale-switch data-locale="zh-CN" data-i18n="common.locale.zh">中文</button>
                <span class="locale-divider">/</span>
                <button class="locale-btn" type="button" data-locale-switch data-locale="en" data-i18n="common.locale.en">English</button>
            </div>
        </div>

        <section class="login-hero" aria-labelledby="login-title">
            <div class="hero-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                    <path d="M12 4.5L20 8.5L12 12.5L4 8.5L12 4.5ZM7.2 10.1V14.2C7.2 16.6 9.5 18.4 12 18.4C14.5 18.4 16.8 16.6 16.8 14.2V10.1L12 12.5L7.2 10.1Z" />
                </svg>
            </div>
            <h1 id="login-title" data-i18n="login.hero.title">TA Hiring System</h1>
            <p class="subtitle" data-i18n="login.hero.subtitle">Sign in to your account</p>
        </section>

        <section class="login-card" data-i18n-aria-label="login.form.aria">

            <div id="form-message" class="form-message hidden" role="alert" aria-live="polite"></div>

            <form id="login-form" class="login-form" method="post" action="<%= contextPath %>/api/auth/login" novalidate>
                <div class="field">
                    <div class="field-label-row">
                        <label for="username" data-i18n="login.form.usernameLabel">Username or email</label>
                    </div>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="username or name@university.edu"
                        data-i18n-placeholder="login.form.usernamePlaceholder"
                        autocomplete="username"
                        maxlength="100"
                        required
                    >
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="password" data-i18n="login.form.passwordLabel">Password</label>
                        <button class="forgot-link" type="button" disabled data-i18n="login.form.forgot">Forgot?</button>
                    </div>
                    <div class="password-input-wrap">
                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Enter your password"
                            data-i18n-placeholder="login.form.passwordPlaceholder"
                            autocomplete="current-password"
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
                </div>

                <div class="field role-field">
                    <div class="field-label-row">
                        <span class="field-label" data-i18n="login.form.roleLabel">Sign in as</span>
                    </div>
                    <input id="login-role" type="hidden" name="role" value="TA">
                    <div class="role-selector" role="group" data-i18n-aria-label="login.form.roleAria">
                        <button class="role-option is-selected" type="button" data-role="TA" aria-pressed="true">
                            <span class="role-option-title" data-i18n="login.form.ta">TA</span>
                            <span class="role-option-desc" data-i18n="login.form.taDesc">Applicant</span>
                        </button>
                        <button class="role-option" type="button" data-role="MO" aria-pressed="false">
                            <span class="role-option-title" data-i18n="login.form.mo">MO</span>
                            <span class="role-option-desc" data-i18n="login.form.moDesc">Module Organizer</span>
                        </button>
                        <button class="role-option" type="button" data-role="ADMIN" aria-pressed="false">
                            <span class="role-option-title" data-i18n="login.form.admin">Admin</span>
                            <span class="role-option-desc" data-i18n="login.form.adminDesc">Manager</span>
                        </button>
                    </div>
                </div>

                <button id="login-submit" class="login-submit-btn" type="submit" data-i18n="login.form.submit">Log in</button>
            </form>
        </section>

        <p class="register-hint">
            <span data-i18n="login.links.noAccount">Don't have an account?</span>
            <a href="<%= contextPath %>/register.jsp" data-i18n="login.links.createAccount">Create one now</a>
        </p>
        <p class="register-hint">
            <span data-i18n="login.links.needAdmin">Need admin access?</span>
            <a href="<%= contextPath %>/admin-invite.jsp" data-i18n="login.links.createAdmin">Use admin invitation</a>
        </p>

        <p class="login-footer" data-i18n="common.footer.copyright">TA Hiring System © 2026</p>
    </main>

    <script>
        // 注入给 ta-recruitment.js 和 login.js，用于生成部署相关的 /api/... 路径。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js?v=20260513-password-toggle" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/auth/login.js?v=20260513-password-toggle" defer></script>
</body>
</html>

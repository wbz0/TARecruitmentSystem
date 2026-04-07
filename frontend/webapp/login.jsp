<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户登录 - TA 招聘系统</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/login.css">
</head>
<body>
    <main class="login-page">
        <section class="login-hero" aria-labelledby="login-title">
            <div class="hero-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                    <path d="M12 4.5L20 8.5L12 12.5L4 8.5L12 4.5ZM7.2 10.1V14.2C7.2 16.6 9.5 18.4 12 18.4C14.5 18.4 16.8 16.6 16.8 14.2V10.1L12 12.5L7.2 10.1Z" />
                </svg>
            </div>
            <h1 id="login-title">TA Hiring Portal</h1>
            <p class="subtitle">Sign in to your account</p>
        </section>

        <section class="login-card" aria-label="登录表单">

            <div id="form-message" class="form-message hidden" role="alert" aria-live="polite"></div>

            <form id="login-form" class="login-form" method="post" action="<%= contextPath %>/login" novalidate>
                <div class="field">
                    <div class="field-label-row">
                        <label for="username">Email address</label>
                    </div>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="name@university.edu"
                        autocomplete="username"
                        maxlength="50"
                        required
                    >
                </div>

                <div class="field">
                    <div class="field-label-row">
                        <label for="password">Password</label>
                        <button class="forgot-link" type="button" disabled>Forgot?</button>
                    </div>
                    <input
                        id="password"
                        name="password"
                        type="password"
                        placeholder="••••••••"
                        autocomplete="current-password"
                        maxlength="100"
                        required
                    >
                </div>

                <label class="keep-signed-in">
                    <input type="checkbox" name="rememberMe" value="1">
                    <span>Keep me signed in</span>
                </label>

                <input id="login-role" type="hidden" name="role" value="MO">

                <div class="login-actions" role="group" aria-label="角色登录按钮">
                    <button
                        id="ta-login-submit"
                        class="login-action-btn login-action-btn-secondary"
                        type="submit"
                        data-role="TA"
                    >
                        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <path d="M12 12C14.07 12 15.75 10.32 15.75 8.25C15.75 6.18 14.07 4.5 12 4.5C9.93 4.5 8.25 6.18 8.25 8.25C8.25 10.32 9.93 12 12 12Z" />
                            <path d="M5.25 18.75C5.25 16.2647 8.26472 14.25 12 14.25C15.7353 14.25 18.75 16.2647 18.75 18.75" />
                        </svg>
                        <span>TA Login</span>
                    </button>
                    <button
                        id="mo-login-submit"
                        class="login-action-btn login-action-btn-primary"
                        type="submit"
                        data-role="MO"
                    >
                        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <path d="M12 3.75L18.75 6.75V11.25C18.75 15.2141 15.901 18.5261 12 19.5C8.09904 18.5261 5.25 15.2141 5.25 11.25V6.75L12 3.75Z" />
                        </svg>
                        <span>MO Login</span>
                    </button>
                </div>
            </form>
        </section>

        <p class="register-hint">
            Don't have an account?
            <a href="<%= contextPath %>/register.jsp">Create one now</a>
        </p>

        <p class="login-footer">University Hiring System © 2026</p>
    </main>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/login.js" defer></script>
</body>
</html>

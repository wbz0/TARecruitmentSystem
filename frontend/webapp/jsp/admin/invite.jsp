<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) {
        username = usernameObj.toString();
    }
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "A";
%>
<%-- Admin 邀请码页：展示并刷新当前短邀请码，不展示旧邮件邀请列表。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.adminInviteManagement.title">Admin Invite Code - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/admin/admin-invite-management.css">
</head>
<body>
    <div class="portal-shell portal-shell-admin">
        <% String portalRole = "admin"; String activeNav = "invite"; String pageTitleKey = "portal.adminDashboard.inviteTitle"; String pageTitleFallback = "Invite Code"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="admin-invite-page">
                    <section class="admin-invite-hero" aria-labelledby="admin-invite-title">
                        <h1 id="admin-invite-title" class="portal-page-title" data-i18n="portal.adminDashboard.inviteTitle">Invite Code</h1>
                        <p class="subtitle" data-i18n="portal.adminDashboard.inviteLead">Share the current code with applicants who request admin access.</p>
                    </section>

                    <section class="invite-page-panel" aria-labelledby="code-panel-title">
                        <header class="invite-panel-header">
                            <h2 id="code-panel-title" data-i18n="portal.adminDashboard.codePanel.title">Current invite code</h2>
                            <p data-i18n="portal.adminDashboard.codePanel.subtitle">Copy this code and reply to the applicant's email.</p>
                        </header>

                        <div class="code-card" id="code-card">
                            <div class="code-display" id="code-display" aria-live="polite" aria-label="Current invite code" data-i18n-aria-label="portal.adminDashboard.codePanel.displayAria">
                                <span class="code-loading" data-i18n="portal.adminDashboard.codePanel.loading">Loading...</span>
                            </div>
                            <div class="code-countdown-row">
                                <div class="code-countdown-bar-track">
                                    <div class="code-countdown-bar-fill" id="countdown-bar"></div>
                                </div>
                                <span class="code-countdown-label" id="countdown-label">—</span>
                            </div>
                        </div>

                        <div class="code-actions">
                            <button id="rotate-btn" class="primary-btn" type="button" data-i18n="portal.adminDashboard.codePanel.refreshBtn">Force refresh</button>
                            <span id="code-error" class="code-error-msg hidden" role="alert" aria-live="polite"></span>
                        </div>

                        <div class="workflow-steps">
                            <h3 data-i18n="portal.adminDashboard.codePanel.workflowTitle">How it works</h3>
                            <ol>
                                <li data-i18n="portal.adminDashboard.codePanel.step1">Applicant emails the team contact address from the email they plan to register with.</li>
                                <li data-i18n="portal.adminDashboard.codePanel.step2">Check this page for the current invite code.</li>
                                <li data-i18n="portal.adminDashboard.codePanel.step3">Reply to the applicant's email with the invite code.</li>
                                <li data-i18n="portal.adminDashboard.codePanel.step4">Applicant enters the code on the registration page to create their admin account.</li>
                            </ol>
                        </div>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        // 注入给 admin-invite-management.js 的公共路由工具。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/admin/admin-invite-management.js" defer></script>
</body>
</html>

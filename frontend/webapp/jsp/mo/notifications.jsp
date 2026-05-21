<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) username = usernameObj.toString();
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "M";
%>
<%-- MO notifications page: read-only global announcements from /api/notifications. --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.moNotifications.title">Notifications - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/common/notifications.css">
</head>
<body>
    <div class="portal-shell portal-shell-mo">
        <% String portalRole = "mo"; String activeNav = "notifications"; String pageTitleKey = "portal.nav.mo.notifications"; String pageTitleFallback = "Notifications"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="notifications-page">
                    <section class="notifications-hero">
                        <h1 data-i18n="portal.nav.mo.notifications">Notifications</h1>
                        <p class="subtitle" data-i18n="portal.notifications.subtitle">Announcements from the admin team</p>
                    </section>

                    <div class="notifications-panel">
                        <div class="notifications-list-section">
                            <div id="notifications-list">
                                <div class="notifications-empty">
                                    <p class="notifications-empty-text" data-i18n="portal.common.loading">Loading...</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </section>
    </div>

    <script>
        // Injected for mo-notifications.js as a shared routing utility.
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js"></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture"></script>
    <script src="<%= contextPath %>/js/mo/mo-notifications.js"></script>
</body>
</html>

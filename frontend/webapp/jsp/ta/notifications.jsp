<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) username = usernameObj.toString();
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "T";
%>
<%-- TA 通知页：只读全站公告 /api/notifications。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.taNotifications.title">Notifications - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/common/notifications.css">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <% String portalRole = "ta"; String activeNav = "notifications"; String pageTitleKey = "portal.nav.ta.notifications"; String pageTitleFallback = "Notifications"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="notifications-page">
                    <section class="notifications-hero">
                        <h1 data-i18n="portal.nav.ta.notifications">Notifications</h1>
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
        // 注入给 ta-notifications.js 的公共路由工具。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js"></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture"></script>
    <script src="<%= contextPath %>/js/ta/ta-notifications.js"></script>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) username = usernameObj.toString();
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "A";
%>
<%-- Admin 通知页：发布/删除全站公告，TA/MO 页面只读同一个公告流。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.adminNotifications.title">Notifications - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/common/notifications.css">
</head>
<body>
    <div class="portal-shell portal-shell-admin">
        <% String portalRole = "admin"; String activeNav = "notifications"; String pageTitleKey = "portal.nav.admin.notifications"; String pageTitleFallback = "Notifications"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="notifications-page">
                    <section class="notifications-hero">
                        <h1 data-i18n="portal.nav.admin.notifications">Notifications</h1>
                        <p class="subtitle" data-i18n="portal.notifications.subtitle">Announcements from the admin team</p>
                    </section>

                    <div class="notifications-panel">

                        <!-- Compose card (admin only) -->
                        <div class="compose-card">
                            <h2 class="compose-card-title" data-i18n="portal.notifications.composeTitle">Publish Notification</h2>
                            <form id="compose-form" novalidate>
                                <div class="compose-fields">
                                    <div class="compose-field">
                                        <label for="compose-title" data-i18n="portal.notifications.titleLabel">Title</label>
                                        <input id="compose-title" type="text" maxlength="200"
                                               data-i18n-placeholder="portal.notifications.titlePlaceholder"
                                               placeholder="Notification title">
                                    </div>
                                    <div class="compose-field">
                                        <label for="compose-content" data-i18n="portal.notifications.contentLabel">Message</label>
                                        <textarea id="compose-content" rows="4" maxlength="2000"
                                                  data-i18n-placeholder="portal.notifications.contentPlaceholder"
                                                  placeholder="Write your message here…"></textarea>
                                    </div>
                                </div>
                                <div class="compose-actions">
                                    <button type="submit" class="primary-btn" data-i18n="portal.notifications.publishBtn">Publish</button>
                                </div>
                                <p id="compose-message" class="compose-message hidden"></p>
                            </form>
                        </div>

                        <!-- Existing notifications -->
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
        // 注入给 admin-notifications.js 的公共路由工具。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js"></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture"></script>
    <script src="<%= contextPath %>/js/admin/admin-notifications.js"></script>
</body>
</html>

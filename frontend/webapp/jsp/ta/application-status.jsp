<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) {
        username = usernameObj.toString();
    }
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "T";
%>
<%-- TA 申请状态页：展示当前 TA 自己的 /api/applications 列表。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.taApplicationStatus.title">Application status - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta/ta-application-status.css">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <% String portalRole = "ta"; String activeNav = "applications"; String pageTitleKey = "portal.taApplicationStatus.title"; String pageTitleFallback = "My Applications"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="status-page">
                    <section class="status-hero" aria-labelledby="status-title">
                        <h1 id="status-title" class="portal-page-title" data-i18n="portal.taApplicationStatus.title">My Applications</h1>
                        <p class="subtitle" data-i18n="portal.taApplicationStatus.subtitle">Track the status of your submitted applications.</p>
                    </section>

                    <section class="status-panel" aria-label="申请状态列表" data-i18n-aria-label="portal.taApplicationStatus.panelAria">
                        <form id="status-search-form" class="search-form" novalidate>
                            <label for="status-search-input" data-i18n="portal.common.search">Search</label>
                            <div class="search-row">
                                <input
                                    id="status-search-input"
                                    name="keyword"
                                    type="text"
                                    maxlength="120"
                                    data-i18n-placeholder="portal.taApplicationStatus.searchPlaceholder"
                                    placeholder="Search by job title, course code, or MO"
                                >
                                <button class="primary-btn search-submit" id="status-search-btn" type="submit" data-i18n="portal.common.search">Search</button>
                            </div>
                        </form>

                        <div id="status-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <p id="list-summary" class="list-summary" data-i18n="portal.taApplicationStatus.loadingApplications" hidden>Loading applications...</p>
                        <div id="applications-list" class="applications-list" aria-live="polite"></div>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        // 注入给公共 routes 工具，申请状态页不需要额外用户变量。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/ta/ta-application-status.js?v=20260513-ta-card-i18n-refresh" defer></script>
</body>
</html>

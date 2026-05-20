<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String userId = "";
    Object userIdObj = session.getAttribute("userId");
    if (userIdObj != null) {
        userId = userIdObj.toString();
    }
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) {
        username = usernameObj.toString();
    }
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "A";
%>
<%-- Admin 工作量页：展示 /api/admin/workload-statistics 返回的 TA 工作量统计。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js?v=20260513-workload-stats-row"></script>
    <title data-i18n="portal.page.adminDashboard.title">TA Workload - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/admin/admin-dashboard.css?v=20260513-workload-search-align">
</head>
<body>
    <div class="portal-shell portal-shell-admin">
        <% String portalRole = "admin"; String activeNav = "dashboard"; String pageTitleKey = "portal.adminDashboard.title"; String pageTitleFallback = "TA Workload"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="admin-dashboard-page">
                    <section class="admin-hero" aria-labelledby="admin-title">
                        <h1 id="admin-title" class="portal-page-title" data-i18n="portal.adminDashboard.title">TA Workload</h1>
                        <p class="subtitle" data-i18n="portal.adminDashboard.subtitle">Track accepted TA job workload by weekly hours and active work period.</p>
                    </section>

                    <section class="admin-panel" aria-label="管理员工作量统计仪表盘" data-i18n-aria-label="portal.adminDashboard.panelAria">
                        <form id="workload-filter-form" class="filter-form workload-search-form" novalidate>
                            <div class="field-group workload-search-field">
                                <label for="workload-search-input" data-i18n="portal.adminDashboard.searchLabel">Search</label>
                                <input
                                    id="workload-search-input"
                                    name="query"
                                    type="search"
                                    data-i18n-placeholder="portal.adminDashboard.searchPlaceholder"
                                    placeholder="Search by TA name, job title, or course code"
                                    autocomplete="off"
                                >
                            </div>
                            <div class="filter-actions">
                                <button id="apply-filter-btn" class="primary-btn" type="submit" data-i18n="portal.common.search">Search</button>
                            </div>
                        </form>

                        <div id="dashboard-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <section class="workload-panel" aria-label="纳入统计的工作量" data-i18n-aria-label="portal.adminDashboard.includedWorkloadPanelAria">
                            <header class="workload-panel-header">
                                <h2 data-i18n="portal.adminDashboard.includedWorkloadPanel">Included Workload</h2>
                                <p id="ta-summary" data-i18n="portal.adminDashboard.loadingWorkload">Loading workload...</p>
                            </header>
                            <p class="workload-panel-lead" data-i18n="portal.adminDashboard.includedWorkloadLead">TA cards are sorted by total accepted workload. Click a card to view the counted jobs.</p>
                            <div id="ta-list" class="workload-card-list" aria-live="polite"></div>
                            <nav id="workload-pagination" class="workload-pagination hidden" aria-label="工作量分页" data-i18n-aria-label="portal.adminDashboard.paginationAria"></nav>
                        </section>

                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        // 注入给 admin-dashboard.js；username 仅作前端兜底展示，不参与权限判断。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_USER_ID = "<%= userId %>";
        window.APP_CURRENT_USERNAME = "<%= username %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js?v=20260513-workload-stats-row" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js?v=20260513-workload-stats-row" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/admin/admin-dashboard.js?v=20260513-workload-search-only" defer></script>
</body>
</html>

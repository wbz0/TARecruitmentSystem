<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String currentRole = "";
    Object roleObj = session.getAttribute("role");
    if (roleObj != null) {
        currentRole = roleObj.toString();
    }
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) {
        username = usernameObj.toString();
    }
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "T";
%>
<%-- TA 职位列表页：展示 /api/jobs 和 /api/ta/job-recommendations 的结果。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.taJobList.title">Job list - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta/ta-job-list.css">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <% String portalRole = "ta"; String activeNav = "jobs"; String pageTitleKey = "portal.nav.ta.jobs"; String pageTitleFallback = "Job List"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="jobs-page">
                    <section class="jobs-hero" aria-labelledby="jobs-page-title">
                        <h1 id="jobs-page-title" class="portal-page-title" data-i18n="portal.nav.ta.jobs">Job List</h1>
                        <p class="subtitle" data-i18n="portal.taJobList.subtitle">Browse and apply for open TA positions.</p>
                    </section>

                    <section class="jobs-panel" aria-label="职位列表与筛选" data-i18n-aria-label="portal.taJobList.panelAria">
                        <form id="job-search-form" class="search-form" novalidate>
                            <label for="job-search-input" data-i18n="portal.common.search">Search</label>
                            <div class="search-row">
                                <input
                                    id="job-search-input"
                                    name="keyword"
                                    type="text"
                                    maxlength="120"
                                    data-i18n-placeholder="portal.taJobList.searchPlaceholder"
                                    placeholder="Search jobs by title, course code, or keywords"
                                >
                                <button id="job-search-btn" class="primary-btn search-submit" type="submit" data-i18n="portal.common.search">Search</button>
                                <button
                                    id="job-search-mode-toggle"
                                    class="search-mode-toggle"
                                    type="button"
                                    data-mode="search"
                                    data-i18n-aria-label="portal.taJobList.searchModeToggle"
                                    aria-label="Search mode"
                                >
                                    <span class="search-mode-option is-active" data-search-mode-option="search" data-i18n="portal.common.search">Search</span>
                                    <span class="search-mode-option" data-search-mode-option="ai" data-i18n="portal.taJobList.aiSearchButton">AI</span>
                                </button>
                            </div>
                        </form>

                        <div id="list-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <p id="job-list-summary" class="list-summary" data-i18n="portal.taJobList.loadingPositions" hidden>Loading positions...</p>

                        <div id="job-list" class="job-list" aria-live="polite"></div>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        // 注入给 ta-job-list.js：contextPath 用于路由，currentRole 用于控制 TA 视角按钮。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_ROLE = "<%= currentRole %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/ta/ta-job-list.js?v=20260513-ta-card-i18n-refresh" defer></script>
</body>
</html>

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
<%-- TA application detail page: reads application from URL id with job summary and withdraw action. --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.taApplicationDetail.title">Application detail - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta/ta-application-detail.css">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <% String portalRole = "ta"; String activeNav = "applications"; String pageTitleKey = "portal.taApplicationDetail.title"; String pageTitleFallback = "Application detail"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="application-detail-page" id="application-detail-root">
                    <div class="detail-back-row">
                        <a class="detail-back-link" href="<%= contextPath %>/jsp/ta/application-status.jsp" data-i18n="portal.taApplicationDetail.backToList">← My applications</a>
                    </div>

                    <div id="detail-message" class="form-message hidden" role="status" aria-live="polite"></div>

                    <section class="app-detail-header-card" id="app-header-card" aria-labelledby="app-detail-title">
                        <div class="app-detail-header-icon" id="app-course-badge" aria-hidden="true">—</div>
                        <div class="app-detail-header-main">
                            <h1 id="app-detail-title" class="app-detail-title">—</h1>
                            <p class="app-detail-submitted" id="app-detail-submitted">—</p>
                        </div>
                        <div class="app-detail-header-actions">
                            <span class="application-status-chip status-pending" id="app-status-chip"><span class="application-status-text">—</span></span>
                            <button
                                class="application-withdraw-btn"
                                id="withdraw-application-btn"
                                type="button"
                                hidden
                                data-i18n="portal.taApplicationDetail.withdrawAction"
                            >Withdraw application</button>
                        </div>
                    </section>

                    <div class="app-detail-layout">
                        <a class="job-teaser-card is-disabled" id="job-teaser-trigger" aria-disabled="true">
                            <div class="job-teaser-icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.6">
                                    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                                    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                                </svg>
                            </div>
                            <div class="job-teaser-body">
                                <p class="job-teaser-label" data-i18n="portal.taApplicationDetail.jobTeaserTitle">Applied position details</p>
                                <div class="job-teaser-meta" id="job-teaser-meta"></div>
                            </div>
                            <span class="job-teaser-cta" data-i18n="portal.taApplicationDetail.viewDetailsCta">View details →</span>
                            <span class="job-teaser-arrow" aria-hidden="true">›</span>
                        </a>

                        <article class="app-info-card app-info-card--cover" aria-labelledby="cover-title">
                            <div class="cover-card-head">
                                <span class="cover-card-icon" aria-hidden="true">
                                    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8">
                                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                                    </svg>
                                </span>
                                <h2 id="cover-title" class="app-info-card-title" data-i18n="portal.taJobDetail.coverLetter">Cover letter</h2>
                            </div>
                            <p class="cover-body" id="cover-letter-body">—</p>
                        </article>

                        <a class="profile-jump-card" id="profile-jump-card" href="<%= contextPath %>/jsp/ta/dashboard.jsp">
                            <span class="profile-jump-icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8">
                                    <circle cx="12" cy="8" r="3.2"></circle>
                                    <path d="M5.2 19a6.8 6.8 0 0 1 13.6 0"></path>
                                </svg>
                            </span>
                            <span class="profile-jump-body">
                                <span class="profile-jump-title" data-i18n="portal.taApplicationDetail.profileCardTitle">My profile</span>
                                <span class="profile-jump-copy" id="profile-jump-copy" data-i18n="portal.taApplicationDetail.profileCardHint">View or edit your resume and skills.</span>
                            </span>
                            <span class="profile-jump-arrow" aria-hidden="true">›</span>
                        </a>

                        <p class="profile-sync-note" id="profile-sync-note">
                            <span class="profile-sync-note-icon" aria-hidden="true">
                                <svg viewBox="0 0 20 20" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                                    <circle cx="10" cy="10" r="8"></circle>
                                    <path d="M10 6.2v.2"></path>
                                    <path d="M10 9v4.7"></path>
                                </svg>
                            </span>
                            <span id="profile-sync-note-text" data-i18n="portal.taApplicationDetail.profileSyncNote">
                                Your profile and resume were sent with this application to the MO. You can update your profile after submission, and changes will sync to the MO view.
                            </span>
                        </p>
                    </div>
                </main>
            </div>
        </section>
    </div>

    <script>
        // Injected for ta-application-detail.js; applicationId is read from query parameters, not parsed in JSP.
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/ta/ta-application-detail.js" defer></script>
</body>
</html>

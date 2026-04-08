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
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MO Overview - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/mo-overview.css">
</head>
<body>
    <div class="portal-shell portal-shell-mo">
        <aside class="portal-sidebar" aria-label="MO portal navigation">
            <p class="portal-brand">MO Portal</p>
            <nav class="portal-nav">
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/mo/overview.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 12h7V4H4z"></path>
                        <path d="M13 20h7v-8h-7z"></path>
                        <path d="M13 11h7V4h-7z"></path>
                        <path d="M4 20h7v-6H4z"></path>
                    </svg>
                    <span>Overview</span>
                </a>
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/applicant-selection.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M7 18c.2-2.6 2.4-4.5 5-4.5s4.8 1.9 5 4.5"></path>
                        <circle cx="12" cy="8.5" r="3"></circle>
                        <path d="M3.5 18c.1-1.6 1.3-2.8 2.9-3.1"></path>
                        <path d="M20.5 18c-.1-1.6-1.3-2.8-2.9-3.1"></path>
                    </svg>
                    <span>Applicants</span>
                </a>
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/dashboard.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M12 5v14"></path>
                        <path d="M5 12h14"></path>
                    </svg>
                    <span>Post Job</span>
                </a>
                <span class="portal-nav-link is-disabled" aria-disabled="true">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M12 8v8"></path>
                        <path d="M8 12h8"></path>
                        <circle cx="12" cy="12" r="8"></circle>
                    </svg>
                    <span>Settings</span>
                </span>
            </nav>
            <div class="portal-sidebar-bottom">
                <a class="portal-nav-link" href="<%= contextPath %>/login.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 7h12"></path>
                        <path d="m12 4 4 3-4 3"></path>
                        <path d="M20 17H8"></path>
                        <path d="m12 20-4-3 4-3"></path>
                    </svg>
                    <span>Switch Roles</span>
                </a>
            </div>
        </aside>

        <section class="portal-main">
            <header class="portal-topbar">
                <div class="portal-user">
                    <span class="portal-user-avatar"><%= username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "M" %></span>
                    <span class="portal-user-name"><%= username == null || username.isEmpty() ? "MO User" : username %></span>
                </div>
                <a class="portal-topbar-link" href="<%= contextPath %>/logout">Sign Out</a>
            </header>

            <div class="portal-content">
                <main class="mo-overview-page">
                    <section class="overview-hero" aria-labelledby="overview-title">
                        <h1 id="overview-title">Overview</h1>
                        <p class="subtitle">Track hiring activity, then jump directly to posting and applicant review workflows.</p>
                    </section>

                    <section class="overview-panel" aria-label="MO activity overview">
                        <div id="overview-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <section class="stats-grid" aria-label="Hiring statistics">
                            <article class="stat-card">
                                <span class="stat-icon icon-jobs" aria-hidden="true">J</span>
                                <p class="stat-value" id="stat-active-jobs">0</p>
                                <p class="stat-label">Active jobs</p>
                            </article>
                            <article class="stat-card">
                                <span class="stat-icon icon-applicants" aria-hidden="true">A</span>
                                <p class="stat-value" id="stat-total-applicants">0</p>
                                <p class="stat-label">Total applicants</p>
                            </article>
                            <article class="stat-card">
                                <span class="stat-icon icon-pending" aria-hidden="true">P</span>
                                <p class="stat-value" id="stat-pending-review">0</p>
                                <p class="stat-label">Pending review</p>
                            </article>
                            <article class="stat-card">
                                <span class="stat-icon icon-offers" aria-hidden="true">O</span>
                                <p class="stat-value" id="stat-offers-sent">0</p>
                                <p class="stat-label">Offers sent</p>
                            </article>
                        </section>

                        <section class="activity-panel" aria-label="Recent activity">
                            <header class="activity-header">
                                <p class="activity-label">Recent activity</p>
                                <a class="nav-link activity-link" href="<%= contextPath %>/jsp/mo/applicant-selection.jsp">View applicants</a>
                            </header>
                            <p id="activity-summary" class="activity-summary">Loading activity...</p>
                            <div id="activity-list" class="activity-list" aria-live="polite"></div>
                        </section>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_USER_ID = "<%= userId %>";
    </script>
    <script src="<%= contextPath %>/js/mo-overview.js" defer></script>
</body>
</html>

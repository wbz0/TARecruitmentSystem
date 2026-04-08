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
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Job list - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta-job-list.css">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <aside class="portal-sidebar" aria-label="TA portal navigation">
            <p class="portal-brand">TA Portal</p>
            <nav class="portal-nav">
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/ta/job-list.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M3 7.5h18v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                        <path d="M9 7.5V6A1.5 1.5 0 0 1 10.5 4.5h3A1.5 1.5 0 0 1 15 6v1.5" />
                        <path d="M3 12h18" />
                    </svg>
                    <span>Jobs</span>
                </a>
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/ta/application-status.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <circle cx="12" cy="12" r="8"></circle>
                        <path d="m8.5 12.5 2.2 2.2L15.5 10"></path>
                    </svg>
                    <span>Status</span>
                </a>
                <span class="portal-nav-link is-disabled" aria-disabled="true">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M12 3v3"></path>
                        <path d="M12 18v3"></path>
                        <path d="M3 12h3"></path>
                        <path d="M18 12h3"></path>
                        <path d="m6 6 2 2"></path>
                        <path d="m16 16 2 2"></path>
                        <path d="m6 18 2-2"></path>
                        <path d="m16 8 2-2"></path>
                    </svg>
                    <span>AI Match</span>
                </span>
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/ta/dashboard.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <circle cx="12" cy="8" r="3"></circle>
                        <path d="M6 18c1.2-2 3.2-3 6-3s4.8 1 6 3"></path>
                    </svg>
                    <span>Profile</span>
                </a>
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
                    <span class="portal-user-avatar"><%= userInitial %></span>
                    <span class="portal-user-name"><%= username == null || username.isEmpty() ? "TA User" : username %></span>
                </div>
                <a class="portal-topbar-link" href="<%= contextPath %>/logout">Sign Out</a>
            </header>

            <div class="portal-content">
                <main class="jobs-page">
                    <section class="jobs-hero" aria-labelledby="jobs-page-title">
                        <h1 id="jobs-page-title">Jobs</h1>
                        <p class="subtitle">Browse and apply for open TA positions.</p>
                    </section>

                    <section class="jobs-panel" aria-label="职位列表与筛选">
                        <form id="job-filter-form" class="filter-form" novalidate>
                            <div class="field-group">
                                <label for="keyword">Keyword</label>
                                <input
                                    id="keyword"
                                    name="keyword"
                                    type="text"
                                    maxlength="120"
                                    placeholder="Title, course code, or course name"
                                >
                            </div>
                            <div class="field-group">
                                <label for="status">Status</label>
                                <select id="status" name="status">
                                    <option value="">All</option>
                                    <option value="OPEN">Open</option>
                                    <option value="CLOSED">Closed</option>
                                    <option value="FILLED">Filled</option>
                                </select>
                            </div>
                            <div class="field-group">
                                <label for="course-code">Course code</label>
                                <input
                                    id="course-code"
                                    name="courseCode"
                                    type="text"
                                    maxlength="50"
                                    placeholder="e.g. EBU6304"
                                >
                            </div>

                            <div class="filter-actions">
                                <button id="search-btn" class="primary-btn" type="submit">Apply filters</button>
                                <button id="clear-filters-btn" class="ghost-btn" type="button">Clear</button>
                            </div>
                        </form>

                        <div id="list-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <div class="list-meta">
                            <p id="job-list-summary">Loading positions...</p>
                            <button id="refresh-jobs-btn" class="inline-btn" type="button">Refresh</button>
                        </div>

                        <div id="job-list" class="job-list" aria-live="polite"></div>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_ROLE = "<%= currentRole %>";
    </script>
    <script src="<%= contextPath %>/js/ta-job-list.js" defer></script>
</body>
</html>

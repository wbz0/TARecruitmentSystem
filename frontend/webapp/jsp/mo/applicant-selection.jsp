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
    <title>Applicant review - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/mo-applicant-selection.css">
</head>
<body>
    <div class="portal-shell portal-shell-mo">
        <aside class="portal-sidebar" aria-label="MO portal navigation">
            <p class="portal-brand">MO Portal</p>
            <nav class="portal-nav">
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/overview.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 12h7V4H4z"></path>
                        <path d="M13 20h7v-8h-7z"></path>
                        <path d="M13 11h7V4h-7z"></path>
                        <path d="M4 20h7v-6H4z"></path>
                    </svg>
                    <span>Overview</span>
                </a>
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/mo/applicant-selection.jsp">
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
                <main class="mo-selection-page">
                    <section class="selection-hero" aria-labelledby="selection-title">
                        <h1 id="selection-title">Applicants</h1>
                        <p class="subtitle">Review and manage all candidate applications.</p>
                    </section>

                    <section class="selection-panel" aria-label="申请人筛选与审核列表">
                        <form id="selection-filter-form" class="filter-form" novalidate>
                            <div class="field-group">
                                <label for="job-filter">Job</label>
                                <select id="job-filter" name="jobId">
                                    <option value="">All jobs</option>
                                </select>
                            </div>

                            <div class="field-group">
                                <label for="status-filter">Status</label>
                                <select id="status-filter" name="status">
                                    <option value="">All</option>
                                    <option value="PENDING">Pending</option>
                                    <option value="ACCEPTED">Accepted</option>
                                    <option value="REJECTED">Rejected</option>
                                    <option value="WITHDRAWN">Withdrawn</option>
                                </select>
                            </div>

                            <div class="filter-actions">
                                <button id="search-btn" class="primary-btn" type="submit">Apply filters</button>
                                <button id="clear-btn" class="ghost-btn" type="button">Clear</button>
                                <button id="refresh-btn" class="inline-btn" type="button">Refresh</button>
                            </div>
                        </form>

                        <div id="selection-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <section class="summary-grid" aria-label="申请统计">
                            <article class="summary-card">
                                <p>Total</p>
                                <strong id="summary-total">0</strong>
                            </article>
                            <article class="summary-card pending">
                                <p>Pending</p>
                                <strong id="summary-pending">0</strong>
                            </article>
                            <article class="summary-card accepted">
                                <p>Accepted</p>
                                <strong id="summary-accepted">0</strong>
                            </article>
                            <article class="summary-card rejected">
                                <p>Rejected</p>
                                <strong id="summary-rejected">0</strong>
                            </article>
                        </section>

                        <p id="selection-list-summary" class="list-summary">Loading applications...</p>
                        <div id="applications-list" class="applications-list" aria-live="polite"></div>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_USER_ID = "<%= userId %>";
    </script>
    <script src="<%= contextPath %>/js/mo-applicant-selection.js" defer></script>
</body>
</html>

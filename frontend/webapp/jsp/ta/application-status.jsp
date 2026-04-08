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
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Application status - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta-application-status.css">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <aside class="portal-sidebar" aria-label="TA portal navigation">
            <p class="portal-brand">TA Portal</p>
            <nav class="portal-nav">
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/ta/job-list.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M3 7.5h18v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                        <path d="M9 7.5V6A1.5 1.5 0 0 1 10.5 4.5h3A1.5 1.5 0 0 1 15 6v1.5" />
                        <path d="M3 12h18" />
                    </svg>
                    <span>Jobs</span>
                </a>
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/ta/application-status.jsp">
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
                <main class="status-page">
                    <section class="status-hero" aria-labelledby="status-title">
                        <h1 id="status-title">My Applications</h1>
                        <p class="subtitle">Track the status of your submitted applications.</p>
                    </section>

                    <section class="status-panel" aria-label="申请状态列表">
                        <form id="status-filter-form" class="filter-form" novalidate>
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
                            <div class="field-group field-wide">
                                <label for="keyword-filter">Keyword</label>
                                <input
                                    id="keyword-filter"
                                    name="keyword"
                                    type="text"
                                    maxlength="120"
                                    placeholder="Search by job title, course code, or MO"
                                >
                            </div>
                            <div class="filter-actions">
                                <button class="primary-btn" id="search-btn" type="submit">Apply filters</button>
                                <button class="ghost-btn" id="reset-btn" type="button">Clear</button>
                                <button class="inline-btn" id="refresh-btn" type="button">Refresh</button>
                            </div>
                        </form>

                        <div id="status-message" class="form-message hidden" role="status" aria-live="polite"></div>

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
                            <article class="summary-card withdrawn">
                                <p>Withdrawn</p>
                                <strong id="summary-withdrawn">0</strong>
                            </article>
                        </section>

                        <p id="list-summary" class="list-summary">Loading applications...</p>
                        <div id="applications-list" class="applications-list" aria-live="polite"></div>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/ta-application-status.js" defer></script>
</body>
</html>

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
    <title>Admin Workload Dashboard - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/admin-dashboard.css">
</head>
<body>
    <div class="portal-shell portal-shell-admin">
        <aside class="portal-sidebar" aria-label="Admin portal navigation">
            <p class="portal-brand">Admin Portal</p>
            <nav class="portal-nav">
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/admin/dashboard.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 12h7V4H4z"></path>
                        <path d="M13 20h7v-8h-7z"></path>
                        <path d="M13 11h7V4h-7z"></path>
                        <path d="M4 20h7v-6H4z"></path>
                    </svg>
                    <span>Dashboard</span>
                </a>
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/overview.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M12 5v14"></path>
                        <path d="M5 12h14"></path>
                    </svg>
                    <span>MO View</span>
                </a>
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/ai-skill-match.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 19h16"></path>
                        <path d="M7 16V8"></path>
                        <path d="M12 16V5"></path>
                        <path d="M17 16v-6"></path>
                    </svg>
                    <span>AI Match</span>
                </a>
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/ai-missing-skills.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 18h16"></path>
                        <path d="M6 14h4"></path>
                        <path d="M6 10h8"></path>
                        <path d="M6 6h12"></path>
                    </svg>
                    <span>Skill Gaps</span>
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
                    <span class="portal-user-avatar"><%= username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "A" %></span>
                    <span class="portal-user-name"><%= username == null || username.isEmpty() ? "Admin User" : username %></span>
                </div>
                <a class="portal-topbar-link" href="<%= contextPath %>/logout">Sign Out</a>
            </header>

            <div class="portal-content">
                <main class="admin-dashboard-page">
                    <section class="admin-hero" aria-labelledby="admin-title">
                        <h1 id="admin-title">Admin Workload Dashboard</h1>
                        <p class="subtitle">Track application volume and module owner review workload in one place.</p>
                    </section>

                    <section class="admin-panel" aria-label="管理员工作量统计仪表盘">
                        <form id="workload-filter-form" class="filter-form" novalidate>
                            <div class="field-group">
                                <label for="start-time">Start</label>
                                <input id="start-time" name="start" type="datetime-local">
                            </div>
                            <div class="field-group">
                                <label for="end-time">End</label>
                                <input id="end-time" name="end" type="datetime-local">
                            </div>
                            <div class="filter-actions">
                                <button id="apply-filter-btn" class="primary-btn" type="submit">Apply range</button>
                                <button id="clear-filter-btn" class="ghost-btn" type="button">Clear</button>
                                <button id="refresh-btn" class="inline-btn" type="button">Refresh</button>
                                <button id="export-btn" class="inline-btn" type="button">Export CSV</button>
                            </div>
                        </form>

                        <div id="dashboard-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <section class="summary-grid" aria-label="申请状态统计">
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

                        <section class="mo-panel" aria-label="MO工作量列表">
                            <header class="mo-panel-header">
                                <h2>MO Workload</h2>
                                <p id="mo-summary">Loading workload...</p>
                            </header>
                            <div id="mo-list" class="mo-list" aria-live="polite"></div>
                        </section>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_USER_ID = "<%= userId %>";
        window.APP_CURRENT_USERNAME = "<%= username %>";
    </script>
    <script src="<%= contextPath %>/js/admin-dashboard.js" defer></script>
</body>
</html>

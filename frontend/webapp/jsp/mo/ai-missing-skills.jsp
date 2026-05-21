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
    <title>AI Missing Skills - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/mo-ai-missing-skills.css">
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
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/ai-skill-match.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 19h16"></path>
                        <path d="M7 16V8"></path>
                        <path d="M12 16V5"></path>
                        <path d="M17 16v-6"></path>
                    </svg>
                    <span>AI Match</span>
                </a>
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/mo/ai-missing-skills.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 18h16"></path>
                        <path d="M6 14h4"></path>
                        <path d="M6 10h8"></path>
                        <path d="M6 6h12"></path>
                    </svg>
                    <span>Skill Gaps</span>
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
                <main class="ai-gap-page ai-module-page">
                    <section class="gap-hero ai-module-hero" aria-labelledby="gap-title">
                        <h1 id="gap-title">AI Missing Skills</h1>
                        <p class="subtitle">Identify the most common capability gaps and plan targeted upskilling actions.</p>
                    </section>

                    <section class="gap-panel ai-module-panel" aria-label="缺失技能展示">
                        <form id="gap-filter-form" class="filter-form ai-module-filter-form" novalidate>
                            <div class="field-group ai-module-field-group">
                                <label for="gap-job-filter">Job</label>
                                <select id="gap-job-filter" name="jobId">
                                    <option value="">Select a job</option>
                                </select>
                            </div>
                            <div class="filter-actions ai-module-filter-actions">
                                <button id="load-gap-btn" class="primary-btn" type="submit">Load gaps</button>
                                <button id="refresh-gap-btn" class="ghost-btn" type="button">Refresh</button>
                            </div>
                        </form>

                        <div id="gap-message" class="form-message ai-module-form-message hidden" role="status" aria-live="polite"></div>

                        <section class="summary-grid ai-module-summary-grid" aria-label="缺失技能统计概览">
                            <article class="summary-card ai-module-summary-card">
                                <p>Applicants</p>
                                <strong id="gap-applicant-count">0</strong>
                            </article>
                            <article class="summary-card ai-module-summary-card">
                                <p>Required skills</p>
                                <strong id="gap-required-count">0</strong>
                            </article>
                            <article class="summary-card ai-module-summary-card">
                                <p>Unique gap skills</p>
                                <strong id="gap-unique-count">0</strong>
                            </article>
                        </section>

                        <section class="chart-grid" aria-label="技能对比图表">
                            <article class="chart-card">
                                <header class="chart-header">
                                    <h3>Missing Skill Frequency</h3>
                                    <p>Most frequently missing capabilities</p>
                                </header>
                                <div id="gap-frequency-chart" class="bar-chart" aria-live="polite"></div>
                            </article>
                            <article class="chart-card">
                                <header class="chart-header">
                                    <h3>Match Score Buckets</h3>
                                    <p>Distribution by high / medium / low / none</p>
                                </header>
                                <div id="score-bucket-chart" class="bar-chart" aria-live="polite"></div>
                            </article>
                        </section>

                        <p id="gap-list-summary" class="list-summary ai-module-list-summary">Choose a job to load missing skills insights.</p>
                        <div id="gap-list" class="gap-list" aria-live="polite"></div>
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
    <script src="<%= contextPath %>/js/mo-ai-missing-skills.js" defer></script>
</body>
</html>

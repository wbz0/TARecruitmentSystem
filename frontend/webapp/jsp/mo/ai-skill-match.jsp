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
    <title>AI Skill Match - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/mo-ai-skill-match.css">
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
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/mo/ai-skill-match.jsp">
                    <svg class="portal-nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M4 19h16"></path>
                        <path d="M7 16V8"></path>
                        <path d="M12 16V5"></path>
                        <path d="M17 16v-6"></path>
                    </svg>
                    <span>AI Match</span>
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
                <main class="ai-match-page">
                    <section class="match-hero" aria-labelledby="match-title">
                        <h1 id="match-title">AI Skill Match</h1>
                        <p class="subtitle">Review applicant matching scores aligned with your posted job requirements.</p>
                    </section>

                    <section class="match-panel" aria-label="技能匹配结果">
                        <form id="match-filter-form" class="filter-form" novalidate>
                            <div class="field-group">
                                <label for="job-filter">Job</label>
                                <select id="job-filter" name="jobId">
                                    <option value="">Select a job</option>
                                </select>
                            </div>
                            <div class="filter-actions">
                                <button id="load-match-btn" class="primary-btn" type="submit">Load results</button>
                                <button id="refresh-match-btn" class="ghost-btn" type="button">Refresh</button>
                            </div>
                        </form>

                        <div id="match-message" class="form-message hidden" role="status" aria-live="polite"></div>

                        <section class="summary-grid" aria-label="匹配统计概览">
                            <article class="summary-card">
                                <p>Total applicants</p>
                                <strong id="summary-total">0</strong>
                            </article>
                            <article class="summary-card">
                                <p>High match (≥85)</p>
                                <strong id="summary-high">0</strong>
                            </article>
                            <article class="summary-card">
                                <p>Medium match (60-84)</p>
                                <strong id="summary-medium">0</strong>
                            </article>
                            <article class="summary-card">
                                <p>Low match (&lt;60)</p>
                                <strong id="summary-low">0</strong>
                            </article>
                        </section>

                        <section class="visual-grid" aria-label="匹配度可视化组件">
                            <article class="visual-card average-card">
                                <p class="visual-title">Average Match Score</p>
                                <div id="average-ring" class="average-ring" aria-label="平均匹配度">
                                    <span id="average-score-text">0%</span>
                                </div>
                            </article>
                            <article class="visual-card distribution-card">
                                <p class="visual-title">Score Distribution</p>
                                <div class="distribution-list">
                                    <div class="distribution-item">
                                        <span>High</span>
                                        <div class="distribution-track"><i id="dist-high"></i></div>
                                        <strong id="dist-high-label">0%</strong>
                                    </div>
                                    <div class="distribution-item">
                                        <span>Medium</span>
                                        <div class="distribution-track"><i id="dist-medium"></i></div>
                                        <strong id="dist-medium-label">0%</strong>
                                    </div>
                                    <div class="distribution-item">
                                        <span>Low</span>
                                        <div class="distribution-track"><i id="dist-low"></i></div>
                                        <strong id="dist-low-label">0%</strong>
                                    </div>
                                </div>
                            </article>
                        </section>

                        <p id="match-list-summary" class="list-summary">Choose a job to load skill match results.</p>
                        <div id="match-list" class="match-list" aria-live="polite"></div>
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
    <script src="<%= contextPath %>/js/mo-ai-skill-match.js" defer></script>
</body>
</html>

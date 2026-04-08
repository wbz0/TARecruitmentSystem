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
    <title>Job detail - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta-job-detail.css">
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
                <main class="job-detail-page">
                    <section class="detail-hero" aria-labelledby="job-detail-title">
                        <h1 id="job-detail-title">Job Detail</h1>
                        <p class="subtitle">Review role requirements and submit your application.</p>
                    </section>

                    <section class="detail-layout">
                        <article class="detail-card" aria-label="职位详细信息">
                            <header class="detail-header">
                                <div class="detail-heading">
                                    <h2 id="job-title">Loading job details...</h2>
                                    <p id="job-course">-</p>
                                </div>
                                <span id="job-status" class="status-pill status-open">OPEN</span>
                            </header>

                            <div id="detail-message" class="form-message hidden" role="status" aria-live="polite"></div>

                            <dl class="detail-grid">
                                <div class="detail-item">
                                    <dt>Module organizer</dt>
                                    <dd id="job-mo-name">-</dd>
                                </div>
                                <div class="detail-item">
                                    <dt>Positions</dt>
                                    <dd id="job-positions">-</dd>
                                </div>
                                <div class="detail-item">
                                    <dt>Workload</dt>
                                    <dd id="job-workload">-</dd>
                                </div>
                                <div class="detail-item">
                                    <dt>Salary</dt>
                                    <dd id="job-salary">-</dd>
                                </div>
                                <div class="detail-item">
                                    <dt>Deadline</dt>
                                    <dd id="job-deadline">-</dd>
                                </div>
                            </dl>

                            <section class="detail-block" aria-labelledby="description-title">
                                <h3 id="description-title">Description</h3>
                                <p id="job-description">-</p>
                            </section>

                            <section class="detail-block" aria-labelledby="skills-title">
                                <h3 id="skills-title">Required skills</h3>
                                <div id="job-skills" class="skills-wrap"></div>
                            </section>
                        </article>

                        <aside id="apply" class="apply-panel" aria-label="申请职位操作">
                            <p class="eyebrow">Application</p>
                            <h3>Submit your application</h3>
                            <p class="apply-copy">Add a short cover letter to highlight your fit for this role.</p>

                            <div id="apply-status-banner" class="status-banner hidden" role="status" aria-live="polite"></div>

                            <form id="apply-form" class="apply-form" novalidate>
                                <div class="field-group">
                                    <label for="cover-letter">Cover letter</label>
                                    <textarea
                                        id="cover-letter"
                                        name="coverLetter"
                                        rows="7"
                                        maxlength="2000"
                                        placeholder="Briefly explain your relevant experience, strengths, and availability."
                                    ></textarea>
                                </div>
                                <button id="apply-submit-btn" class="primary-btn" type="submit">Apply for this job</button>
                            </form>

                            <p class="side-hint">Only TA accounts can submit applications. If you have already applied, this panel will show your latest status.</p>
                        </aside>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_ROLE = "<%= currentRole %>";
    </script>
    <script src="<%= contextPath %>/js/ta-job-detail.js" defer></script>
</body>
</html>

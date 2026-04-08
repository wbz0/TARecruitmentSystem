<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String currentRole = "";
    Object roleObj = session.getAttribute("role");
    if (roleObj != null) {
        currentRole = roleObj.toString();
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>职位详情 - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta-job-detail.css">
</head>
<body>
    <main class="job-detail-page">
        <section class="detail-hero" aria-labelledby="job-detail-title">
            <div class="hero-actions">
                <a class="nav-link" href="<%= contextPath %>/jsp/ta/job-list.jsp">Back to job list</a>
                <a class="logout-link" href="<%= contextPath %>/logout">Log out</a>
            </div>
            <span class="detail-badge">Position detail</span>
            <h1 id="job-detail-title">Review position details and apply</h1>
            <p class="subtitle">Check role requirements, workload, and deadline before submitting your application.</p>
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

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_ROLE = "<%= currentRole %>";
    </script>
    <script src="<%= contextPath %>/js/ta-job-detail.js" defer></script>
</body>
</html>

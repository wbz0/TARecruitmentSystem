<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>申请状态 - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta-application-status.css">
</head>
<body>
    <main class="status-page">
        <section class="status-hero" aria-labelledby="status-title">
            <div class="hero-actions">
                <a class="nav-link" href="<%= contextPath %>/jsp/ta/job-list.jsp">Browse jobs</a>
                <a class="nav-link" href="<%= contextPath %>/jsp/ta/dashboard.jsp">Back to profile</a>
                <a class="logout-link" href="<%= contextPath %>/logout">Log out</a>
            </div>
            <span class="status-badge">TA Workspace</span>
            <h1 id="status-title">Track your application status</h1>
            <p class="subtitle">Review all submitted applications and withdraw pending ones if needed.</p>
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

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/ta-application-status.js" defer></script>
</body>
</html>

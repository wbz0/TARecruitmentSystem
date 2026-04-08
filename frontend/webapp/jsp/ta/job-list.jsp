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
    <title>职位列表 - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta-job-list.css">
</head>
<body>
    <main class="jobs-page">
        <section class="jobs-hero" aria-labelledby="jobs-page-title">
            <div class="hero-actions">
                <a class="nav-link" href="<%= contextPath %>/jsp/ta/dashboard.jsp">Back to profile</a>
                <a class="nav-link" href="<%= contextPath %>/jsp/ta/application-status.jsp">Application status</a>
                <a class="logout-link" href="<%= contextPath %>/logout">Log out</a>
            </div>
            <span class="jobs-badge">TA Workspace</span>
            <h1 id="jobs-page-title">Browse available TA positions</h1>
            <p class="subtitle">Search by keyword, filter by status, and open position details to apply.</p>
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
                    <button id="search-btn" class="primary-btn" type="submit">Search jobs</button>
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

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_ROLE = "<%= currentRole %>";
    </script>
    <script src="<%= contextPath %>/js/ta-job-list.js" defer></script>
</body>
</html>

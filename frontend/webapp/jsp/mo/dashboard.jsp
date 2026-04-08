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
    <title>MO Dashboard - Post TA Jobs</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/mo-dashboard.css">
</head>
<body>
    <main class="mo-page">
        <section class="mo-hero" aria-labelledby="mo-page-title">
            <div class="hero-actions">
                <a class="nav-link" href="<%= contextPath %>/jsp/mo/applicant-selection.jsp">Applicant selection</a>
                <a class="logout-link" href="<%= contextPath %>/logout">Log out</a>
            </div>
            <span class="mo-badge">MO Workspace</span>
            <h1 id="mo-page-title">Create and manage TA job postings</h1>
            <p class="subtitle">Publish role details, then monitor the positions you have already posted.</p>
        </section>

        <section class="mo-layout">
            <section class="mo-card" aria-label="发布职位表单">
                <div class="section-heading">
                    <div>
                        <p class="eyebrow">Create posting</p>
                        <h2>Post a new TA position</h2>
                        <p class="section-copy">Fields marked with * are required for publishing.</p>
                    </div>
                </div>

                <div id="form-message" class="form-message hidden" role="status" aria-live="polite"></div>

                <form id="job-create-form" class="mo-form" novalidate>
                    <div class="field-grid">
                        <div class="field field-full">
                            <label for="job-title">Job title *</label>
                            <input id="job-title" name="title" type="text" maxlength="200" placeholder="e.g. Teaching Assistant - Data Structures" required>
                        </div>

                        <div class="field">
                            <label for="course-code">Course code *</label>
                            <input id="course-code" name="courseCode" type="text" maxlength="50" placeholder="e.g. EBU6304" required>
                        </div>

                        <div class="field">
                            <label for="course-name">Course name</label>
                            <input id="course-name" name="courseName" type="text" maxlength="120" placeholder="e.g. Software Engineering">
                        </div>

                        <div class="field field-full">
                            <label for="description">Description</label>
                            <textarea id="description" name="description" rows="5" maxlength="4000" placeholder="Describe responsibilities, expectations, and any course-specific requirements."></textarea>
                        </div>

                        <div class="field field-full">
                            <label for="required-skills">Required skills</label>
                            <input id="required-skills" name="requiredSkills" type="text" maxlength="500" placeholder="Separate skills with commas, e.g. Java, SQL, communication">
                        </div>

                        <div class="field">
                            <label for="positions">Positions *</label>
                            <input id="positions" name="positions" type="number" min="1" max="200" value="1" required>
                        </div>

                        <div class="field">
                            <label for="deadline">Application deadline</label>
                            <input id="deadline" name="deadline" type="datetime-local">
                        </div>

                        <div class="field">
                            <label for="workload">Workload</label>
                            <input id="workload" name="workload" type="text" maxlength="120" placeholder="e.g. 8 hours / week">
                        </div>

                        <div class="field">
                            <label for="salary">Salary</label>
                            <input id="salary" name="salary" type="text" maxlength="120" placeholder="e.g. 25 SGD / hour">
                        </div>
                    </div>

                    <div class="form-actions">
                        <button id="publish-btn" class="primary-btn" type="submit">Publish job</button>
                        <button id="reset-btn" class="ghost-btn" type="reset">Reset form</button>
                    </div>
                </form>
            </section>

            <aside class="side-card" aria-label="我的职位列表">
                <div class="side-header">
                    <div>
                        <p class="side-card-label">My postings</p>
                        <h3>Published jobs</h3>
                    </div>
                    <button id="refresh-jobs-btn" class="inline-btn" type="button">Refresh</button>
                </div>

                <p id="jobs-summary" class="jobs-summary">Loading your jobs...</p>
                <div id="jobs-list" class="jobs-list" aria-live="polite"></div>
            </aside>
        </section>
    </main>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_USER_ID = "<%= userId %>";
        window.APP_CURRENT_USERNAME = "<%= username %>";
    </script>
    <script src="<%= contextPath %>/js/mo-dashboard.js" defer></script>
</body>
</html>

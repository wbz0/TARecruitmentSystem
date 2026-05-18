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
                <a class="portal-nav-link is-active" href="<%= contextPath %>/jsp/mo/dashboard.jsp">
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
                <a class="portal-nav-link" href="<%= contextPath %>/jsp/mo/ai-missing-skills.jsp">
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
                <main class="mo-page">
                    <section class="mo-hero" aria-labelledby="mo-page-title">
                        <h1 id="mo-page-title">Post New Job</h1>
                        <p class="subtitle">Create a new TA position listing for your course.</p>
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
            </div>
        </section>
    </div>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_USER_ID = "<%= userId %>";
        window.APP_CURRENT_USERNAME = "<%= username %>";
    </script>
    <script src="<%= contextPath %>/js/mo-dashboard.js" defer></script>
</body>
</html>

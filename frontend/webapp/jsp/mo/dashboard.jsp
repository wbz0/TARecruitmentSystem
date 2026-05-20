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
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "M";
%>
<%-- MO dashboard：同页承载岗位发布、我的发布、申请审核和候选人详情。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.moDashboard.title">MO Dashboard - Post TA Jobs</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/mo/mo-dashboard.css">
</head>
<body>
    <div class="portal-shell portal-shell-mo">
        <%
            String portalRole = "mo";
            String tabParam = request.getParameter("tab");
            String activeNav = "post-job".equals(tabParam) ? "post-job" : "my-jobs";
            String heroTitleKey = "post-job".equals(activeNav) ? "portal.moDashboard.title" : "portal.moDashboard.myJobs";
            String heroTitleFallback = "post-job".equals(activeNav) ? "Post New Job" : "My Postings";
            String heroSubtitleKey = "post-job".equals(activeNav) ? "portal.moDashboard.subtitle" : "portal.moDashboard.myJobsHeroSubtitle";
            String heroSubtitleFallback = "post-job".equals(activeNav)
                    ? "Create a new TA position listing for your course."
                    : "View and manage the TA job postings you have published.";
            String pageTitleKey = heroTitleKey;
            String pageTitleFallback = heroTitleFallback;
        %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="mo-page">
                    <section class="mo-hero" aria-labelledby="mo-page-title">
                        <h1 id="mo-page-title" class="portal-page-title" data-i18n="<%= heroTitleKey %>"><%= heroTitleFallback %></h1>
                        <p class="subtitle" data-i18n="<%= heroSubtitleKey %>"><%= heroSubtitleFallback %></p>
                    </section>

                    <%--
                        “我的发布”面板。
                        mo-dashboard.js 会把职位卡片渲染到 #job-list，并从卡片进入申请人子视图。
                    --%>
                    <div class="mo-tab-panel <%= "my-jobs".equals(activeNav) ? "is-active" : "" %>" id="panel-my-jobs" <%= "post-job".equals(activeNav) ? "hidden" : "" %>>
                        <section class="mo-card" aria-label="我的岗位列表" data-i18n-aria-label="portal.moDashboard.myJobsPanelAria">
                            <div class="section-heading">
                                <div>
                                    <p class="eyebrow" data-i18n="portal.moDashboard.manage">Manage</p>
                                    <h2 data-i18n="portal.moDashboard.myJobs">My Postings</h2>
                                    <p class="section-copy" data-i18n="portal.moDashboard.myJobsDesc">View and manage your job postings.</p>
                                </div>
                            </div>

                            <div id="jobs-list-message" class="form-message hidden" role="status" aria-live="polite"></div>

                            <div id="jobs-list-container" class="job-list-container">
                                <!-- Jobs list will be loaded here -->
                                <div class="jobs-loading" id="jobs-loading">
                                    <span data-i18n="portal.common.loading">Loading...</span>
                                </div>
                                <div class="job-list hidden" id="job-list" role="list"></div>
                                <div class="empty-state hidden" id="jobs-empty">
                                    <p class="empty-title" data-i18n="portal.moDashboard.noJobsTitle">No job postings yet</p>
                                    <p class="empty-copy" data-i18n="portal.moDashboard.noJobsDesc">Click "Post New Job" to create your first TA position listing.</p>
                                </div>
                            </div>
                        </section>
                    </div>

                    <%--
                        “发布新职位”面板。
                        表单字段名与 JobServlet.JOB_FIELDS 保持一致，提交到 /api/jobs。
                    --%>
                    <div class="mo-tab-panel <%= "post-job".equals(activeNav) ? "is-active" : "" %>" id="panel-post-job" <%= "my-jobs".equals(activeNav) ? "hidden" : "" %>>
                        <section class="mo-card" aria-label="发布职位表单" data-i18n-aria-label="portal.moDashboard.postJobPanelAria">
                            <div class="section-heading">
                                <div>
                                    <p class="eyebrow" data-i18n="portal.moDashboard.createPosting">Create posting</p>
                                    <h2 data-i18n="portal.moDashboard.postPosition">Post a new TA position</h2>
                                    <p class="section-copy" data-i18n="portal.moDashboard.requiredLead">Fields labeled Required are required for publishing.</p>
                                </div>
                            </div>

                            <div id="form-message" class="form-message hidden" role="status" aria-live="polite"></div>

                            <form id="job-create-form" class="mo-form" novalidate>
                                <div class="form-cluster">
                                    <p class="form-cluster-title" data-i18n="portal.moDashboard.courseInfo">Course information</p>
                                    <div class="field-grid">
                                    <div class="field field-full">
                                        <div class="field-label-row">
                                            <label for="job-title" data-i18n="portal.moDashboard.jobTitle">Job title</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.title">最多 200 字符，不含 HTML 标签</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="job-title" name="title" type="text" maxlength="200" placeholder="e.g. Teaching Assistant - Data Structures" data-i18n-placeholder="portal.moDashboard.jobTitlePlaceholder" required>
                                    </div>

                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="course-code" data-i18n="portal.common.courseCode">Course code</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.courseCode">字母或数字开头，如 EBU6304，最多 50 字符，不含空格</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="course-code" name="courseCode" type="text" maxlength="50" placeholder="e.g. EBU6304" data-i18n-placeholder="portal.moDashboard.courseCodePlaceholder" required>
                                    </div>

                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="course-name" data-i18n="portal.moDashboard.courseName">Course name</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.courseName">课程全称，最多 120 字符</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="course-name" name="courseName" type="text" maxlength="120" placeholder="e.g. Software Engineering" data-i18n-placeholder="portal.moDashboard.courseNamePlaceholder" required>
                                    </div>
                                    </div>
                                </div>

                                <div class="form-cluster">
                                    <p class="form-cluster-title" data-i18n="portal.moDashboard.roleRequirements">Role requirements</p>
                                    <div class="field-grid">
                                    <div class="field field-full">
                                        <div class="field-label-row">
                                            <label for="description" data-i18n="portal.common.description">Description</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.description">详细描述职责与要求，最多 4000 字符</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <textarea id="description" name="description" rows="5" maxlength="4000" placeholder="Describe responsibilities, expectations, and any course-specific requirements." data-i18n-placeholder="portal.moDashboard.descriptionPlaceholder" required></textarea>
                                    </div>

                                    <div class="field field-full">
                                        <div class="field-label-row">
                                            <label for="required-skills" data-i18n="portal.common.requiredSkills">Required skills</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.requiredSkills">必须用英文逗号或中文逗号分隔，最多 20 项，如：Java, SQL, Git</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="required-skills" name="requiredSkills" type="text" maxlength="500" placeholder="Use English or Chinese commas only, e.g. Java, SQL, communication" data-i18n-placeholder="portal.moDashboard.requiredSkillsPlaceholder" required>
                                    </div>
                                    </div>
                                </div>

                                <div class="form-cluster">
                                    <p class="form-cluster-title" data-i18n="portal.moDashboard.hiringSettings">Hiring settings</p>
                                    <div class="field-grid">
                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="positions" data-i18n="portal.common.positions">Positions</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.positions">招募名额，1 至 200 之间的整数</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="positions" name="positions" type="number" min="1" max="200" value="1" required>
                                    </div>

                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="deadline" data-i18n="portal.moDashboard.applicationDeadline">Application deadline</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.deadline">须晚于当前时间，且不超过 2 年</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="deadline" name="deadline" type="datetime-local" required>
                                    </div>

                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="weekly-hours" data-i18n="portal.moDashboard.weeklyHours">Weekly hours</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.weeklyHours">每周工作小时数，0.5 至 40，最多 1 位小数</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="weekly-hours" name="weeklyHours" type="number" min="0.5" max="40" step="0.1" placeholder="8" data-i18n-placeholder="portal.moDashboard.weeklyHoursPlaceholder" required>
                                    </div>

                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="work-start-date" data-i18n="portal.moDashboard.workStartDate">Work start date</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.workStartDate">不得早于申请截止日期</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="work-start-date" name="workStartDate" type="date" required>
                                    </div>

                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="work-end-date" data-i18n="portal.moDashboard.workEndDate">Work end date</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.workEndDate">不得早于工作开始日期</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="work-end-date" name="workEndDate" type="date" required>
                                    </div>

                                    <div class="field">
                                        <div class="field-label-row">
                                            <label for="salary" data-i18n="portal.common.salary">Salary</label>
                                            <span class="field-label-end">
                                                <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                    <span aria-hidden="true">i</span>
                                                    <span class="field-tooltip" data-i18n="portal.moDashboard.hint.salary">自由描述，如：25 RMB / 小时，最多 120 字符</span>
                                                </button>
                                                <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                            </span>
                                        </div>
                                        <input id="salary" name="salary" type="text" maxlength="120" placeholder="e.g. 25 RMB / hour" data-i18n-placeholder="portal.moDashboard.salaryPlaceholder" required>
                                    </div>
                                    </div>
                                </div>

                                <div class="form-actions">
                                    <button id="publish-btn" class="primary-btn" type="submit" data-i18n="portal.moDashboard.publishJob">Publish job</button>
                                    <button id="reset-btn" class="ghost-btn" type="reset" data-i18n="portal.moDashboard.resetForm">Reset form</button>
                                </div>
                            </form>
                        </section>
                    </div>
                    <!-- End of Tab Panels -->

                <!-- Applicant Sub-View Panel -->
                    <div id="panel-applicants" class="mo-applicant-subview hidden"
                         aria-label="Applicant list"
                         data-i18n-aria-label="portal.moApplicantSelection.panelAria">
                        <div class="subview-header">
                            <button id="subview-back-btn" class="ghost-btn subview-back-btn" type="button" aria-label="Back to course list" data-i18n-aria-label="portal.moApplicantSelection.backToCourseList">
                                <svg class="subview-back-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"
                                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M15 18l-6-6 6-6"/>
                                </svg>
                            </button>
                            <h2 id="subview-job-title" class="subview-job-title"></h2>
                        </div>

                        <form id="subview-search-form" class="search-form" novalidate>
                            <div class="search-row">
                                <input id="subview-search-input" name="keyword" type="text" maxlength="160"
                                    data-i18n-placeholder="portal.moApplicantSelection.searchPlaceholder"
                                    placeholder="Search by applicant name, email, or job title">
                                <button id="subview-search-btn" class="primary-btn search-submit" type="submit"
                                    data-i18n="portal.common.search">Search</button>
                                <button id="subview-ai-search-btn" class="search-mode-toggle" type="button"
                                    aria-label="Search mode" data-i18n-aria-label="portal.moApplicantSelection.searchModeToggle">
                                    <span class="search-mode-option search-mode-option--search" data-mode-option="search"
                                        data-i18n="portal.common.search">Search</span>
                                    <span class="search-mode-option search-mode-option--ai" data-mode-option="ai"
                                        data-i18n="portal.moApplicantSelection.aiSearchButton">AI</span>
                                </button>
                            </div>
                        </form>

                        <div id="subview-message" class="form-message hidden" role="status" aria-live="polite"></div>
                        <p id="subview-list-summary" class="list-summary" hidden></p>
                        <div id="subview-list" class="applications-list" aria-live="polite"></div>
                    </div>

                <!-- Edit Job Modal -->
                    <div class="modal-overlay hidden" id="edit-job-modal" role="dialog" aria-modal="true" aria-labelledby="edit-modal-title">
                        <div class="modal-container">
                            <div class="modal-header">
                                <h2 id="edit-modal-title" data-i18n="portal.moDashboard.editJob">Edit Job</h2>
                                <button class="modal-close" id="edit-modal-close" aria-label="Close" data-i18n-aria-label="portal.common.close">&times;</button>
                            </div>
                            <div class="modal-body">
                                <div id="edit-form-message" class="form-message hidden" role="status" aria-live="polite"></div>
                                <form id="job-edit-form" class="mo-form" novalidate>
                                    <input type="hidden" id="edit-job-id" name="jobId">

                                    <div class="field-grid">
                                        <div class="field field-full">
                                            <div class="field-label-row">
                                                <label for="edit-job-title" data-i18n="portal.moDashboard.jobTitle">Job title</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.title">最多 200 字符，不含 HTML 标签</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-job-title" name="title" type="text" maxlength="200" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-course-code" data-i18n="portal.common.courseCode">Course code</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.courseCode">字母或数字开头，如 EBU6304，最多 50 字符，不含空格</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-course-code" name="courseCode" type="text" maxlength="50" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-course-name" data-i18n="portal.moDashboard.courseName">Course name</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.courseName">课程全称，最多 120 字符</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-course-name" name="courseName" type="text" maxlength="120" required>
                                        </div>

                                        <div class="field field-full">
                                            <div class="field-label-row">
                                                <label for="edit-description" data-i18n="portal.common.description">Description</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.description">详细描述职责与要求，最多 4000 字符</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <textarea id="edit-description" name="description" rows="5" maxlength="4000" required></textarea>
                                        </div>

                                        <div class="field field-full">
                                            <div class="field-label-row">
                                                <label for="edit-required-skills" data-i18n="portal.common.requiredSkills">Required skills</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.requiredSkills">必须用英文逗号或中文逗号分隔，最多 20 项，如：Java, SQL, Git</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-required-skills" name="requiredSkills" type="text" maxlength="500" placeholder="Use English or Chinese commas only, e.g. Java, SQL, communication" data-i18n-placeholder="portal.moDashboard.requiredSkillsPlaceholder" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-positions" data-i18n="portal.common.positions">Positions</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.positions">招募名额，1 至 200 之间的整数</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-positions" name="positions" type="number" min="1" max="200" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-deadline" data-i18n="portal.moDashboard.applicationDeadline">Application deadline</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.deadline">须晚于当前时间，且不超过 2 年</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-deadline" name="deadline" type="datetime-local" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-weekly-hours" data-i18n="portal.moDashboard.weeklyHours">Weekly hours</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.weeklyHours">每周工作小时数，0.5 至 40，最多 1 位小数</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-weekly-hours" name="weeklyHours" type="number" min="0.5" max="40" step="0.1" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-work-start-date" data-i18n="portal.moDashboard.workStartDate">Work start date</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.workStartDate">不得早于申请截止日期</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-work-start-date" name="workStartDate" type="date" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-work-end-date" data-i18n="portal.moDashboard.workEndDate">Work end date</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.workEndDate">不得早于工作开始日期</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-work-end-date" name="workEndDate" type="date" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-salary" data-i18n="portal.common.salary">Salary</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="填写提示" data-i18n-aria-label="portal.moDashboard.hintAria">
                                                        <span aria-hidden="true">i</span>
                                                        <span class="field-tooltip" data-i18n="portal.moDashboard.hint.salary">自由描述，如：25 RMB / 小时，最多 120 字符</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.moDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input id="edit-salary" name="salary" type="text" maxlength="120" placeholder="e.g. 25 RMB / hour" data-i18n-placeholder="portal.moDashboard.salaryPlaceholder" required>
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="edit-status" data-i18n="portal.common.status">Status</label>
                                            </div>
                                            <select id="edit-status" name="status">
                                                <option value="OPEN" data-i18n="portal.common.open">Open</option>
                                                <option value="CLOSED" data-i18n="portal.common.closed">Closed</option>
                                                <option value="FILLED" data-i18n="portal.common.filled">Filled</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="form-actions">
                                        <button id="edit-save-btn" class="primary-btn" type="submit" data-i18n="portal.action.save">Save Changes</button>
                                        <button id="edit-cancel-btn" class="ghost-btn" type="button" data-i18n="portal.action.cancel">Cancel</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- Delete Confirmation Modal -->
                    <div class="modal-overlay hidden" id="delete-job-modal" role="dialog" aria-modal="true" aria-labelledby="delete-modal-title">
                        <div class="modal-container modal-small">
                            <div class="modal-header">
                                <h2 id="delete-modal-title" data-i18n="portal.moDashboard.confirmDelete">Confirm Delete</h2>
                                <button class="modal-close" id="delete-modal-close" aria-label="Close" data-i18n-aria-label="portal.common.close">&times;</button>
                            </div>
                            <div class="modal-body">
                                <p id="delete-message" data-i18n="portal.moDashboard.deleteConfirmMsg">Are you sure you want to delete this job posting?</p>
                                <p class="delete-job-title" id="delete-job-title"></p>
                                <div class="form-actions">
                                    <button id="delete-confirm-btn" class="danger-btn" type="button" data-i18n="portal.action.delete">Delete</button>
                                    <button id="delete-cancel-btn" class="ghost-btn" type="button" data-i18n="portal.action.cancel">Cancel</button>
                                </div>
                            </div>
                        </div>
                    </div>

                </main>
            </div>
        </section>
    </div>

    <script>
        /*
         * 注入给 mo-dashboard.js：
         * APP_CONTEXT_PATH 用于公共 API routes；
         * APP_CURRENT_USER_ID/USERNAME 用于当前 MO 视角和页面兜底展示；
         * APP_INITIAL_TAB 由 ?tab=post-job 控制默认打开“我的发布”或“发布新职位”。
         */
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_USER_ID = "<%= userId %>";
        window.APP_CURRENT_USERNAME = "<%= username %>";
        window.APP_INITIAL_TAB = "<%= activeNav %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/mo/mo-dashboard.js" defer></script>
</body>
</html>

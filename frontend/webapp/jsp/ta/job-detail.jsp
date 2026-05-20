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
<%-- TA 职位详情页：从 URL id 读取岗位并支持申请。 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.taJobDetail.title">Job detail - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta/ta-job-detail.css">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <% String portalRole = "ta"; String activeNav = "jobs"; String pageTitleKey = "portal.taJobDetail.title"; String pageTitleFallback = "Job Detail"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="job-detail-page">
                    <section class="detail-hero" aria-labelledby="job-detail-title">
                        <div class="detail-hero-copy">
                            <h1 id="job-detail-title" class="portal-page-title" data-i18n="portal.taJobDetail.title">Job Detail</h1>
                            <p class="subtitle" data-i18n="portal.taJobDetail.subtitle">Review role requirements and submit your application.</p>
                        </div>
                        <div class="detail-back-row">
                            <a class="detail-back-link" href="<%= contextPath %>/jsp/ta/job-list.jsp" data-i18n="portal.taJobDetail.backToJobs">← Job list</a>
                        </div>
                    </section>

                    <section class="detail-layout">
                        <article class="detail-card" aria-label="职位详细信息" data-i18n-aria-label="portal.taJobDetail.detailCardAria">
                            <header class="detail-header">
                                <div class="detail-heading">
                                    <h2 id="job-title" data-i18n="portal.taJobDetail.loadingDetails">Loading job details...</h2>
                                    <p id="job-course">-</p>
                                </div>
                                <span id="job-status" class="status-pill status-open" data-i18n="portal.common.openUpper">OPEN</span>
                            </header>

                            <div id="detail-message" class="form-message hidden" role="status" aria-live="polite"></div>

                            <dl class="detail-grid">
                                <div class="detail-item">
                                    <dt data-i18n="portal.common.positions">Positions</dt>
                                    <dd id="job-positions">-</dd>
                                </div>
                                <div class="detail-item">
                                    <dt data-i18n="portal.common.workload">Workload</dt>
                                    <dd id="job-workload">-</dd>
                                </div>
                                <div class="detail-item">
                                    <dt data-i18n="portal.common.salary">Salary</dt>
                                    <dd id="job-salary">-</dd>
                                </div>
                                <div class="detail-item">
                                    <dt data-i18n="portal.common.deadline">Deadline</dt>
                                    <dd id="job-deadline">-</dd>
                                </div>
                            </dl>

                            <section class="detail-block" aria-labelledby="description-title">
                                <h3 id="description-title" data-i18n="portal.common.description">Description</h3>
                                <p id="job-description">-</p>
                            </section>

                            <section class="detail-block" aria-labelledby="skills-title">
                                <h3 id="skills-title" data-i18n="portal.common.requiredSkills">Required skills</h3>
                                <div id="job-skills" class="skills-wrap"></div>
                            </section>

                            <section class="detail-apply-card" aria-labelledby="apply-action-title">
                                <div class="detail-apply-copy">
                                    <p class="eyebrow" data-i18n="portal.common.application">Application</p>
                                    <h3 id="apply-action-title" data-i18n="portal.taJobDetail.submitApplicationTitle">Submit your application</h3>
                                    <p class="detail-apply-hint" data-i18n="portal.taJobDetail.applyProfileHint">
                                        When you submit, your profile and cover letter will be sent to the MO together.
                                    </p>
                                </div>
                                <div class="detail-apply-actions">
                                    <button
                                        type="button"
                                        id="apply-open-btn"
                                        class="primary-btn detail-apply-trigger"
                                        aria-haspopup="dialog"
                                        aria-expanded="false"
                                        aria-controls="apply-modal-dialog"
                                        data-i18n="portal.dynamic.applyNow"
                                    >
                                        Apply now
                                    </button>
                                    <div id="apply-inline-status" class="status-banner hidden" role="status" aria-live="polite"></div>
                                </div>
                            </section>
                        </article>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <div id="apply-modal" class="apply-modal hidden" aria-hidden="true">
        <div class="apply-modal-backdrop" data-close-modal tabindex="-1" aria-hidden="true"></div>
        <div
            class="apply-modal-panel"
            id="apply-modal-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="apply-modal-title"
            aria-describedby="apply-modal-copy"
            tabindex="-1"
        >
            <div class="apply-modal-header">
                <div class="apply-modal-heading">
                    <p class="eyebrow" data-i18n="portal.common.application">Application</p>
                    <h2 id="apply-modal-title" data-i18n="portal.taJobDetail.submitApplicationTitle">Submit your application</h2>
                    <p id="apply-modal-copy" class="apply-modal-copy" data-i18n="portal.taJobDetail.applyProfileHint">
                        When you submit, your profile and cover letter will be sent to the MO together.
                    </p>
                </div>
                <button type="button" class="apply-modal-close" id="apply-modal-close" data-i18n-aria-label="portal.taApplicationDetail.closeModal" aria-label="Close">
                    <span aria-hidden="true">×</span>
                </button>
            </div>

            <div id="apply-status-banner" class="status-banner hidden" role="status" aria-live="polite"></div>

            <form id="apply-form" class="apply-form" novalidate>
                <div class="field-group">
                    <label for="cover-letter" data-i18n="portal.taJobDetail.coverLetter">Cover letter</label>
                    <textarea
                        id="cover-letter"
                        name="coverLetter"
                        rows="7"
                        maxlength="2000"
                        data-i18n-placeholder="portal.taJobDetail.coverLetterPlaceholder"
                        placeholder="Briefly explain your relevant experience, strengths, and availability."
                    ></textarea>
                </div>
                <button id="apply-submit-btn" class="primary-btn" type="submit" data-i18n="portal.taJobDetail.applyNow">Apply for this job</button>
            </form>
        </div>
    </div>

    <script>
        // 注入给 ta-job-detail.js：用于生成 API 路径和判断当前角色是否可申请。
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
        window.APP_CURRENT_ROLE = "<%= currentRole %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/ta/ta-job-detail.js" defer></script>
</body>
</html>

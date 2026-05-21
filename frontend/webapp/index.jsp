<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>
<%-- Portal home page: displays only entry points and process overview, does not call business APIs directly. --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="index.page.title">TA Hiring System - Home</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/portal/portal-home.css">
</head>
<body>
    <header class="home-header">
        <div class="home-header-inner">
            <a class="home-brand" href="<%= contextPath %>/">
                <span class="home-brand-mark" aria-hidden="true">TA</span>
                <span data-i18n="common.portalBrand">TA Hiring System</span>
            </a>

            <nav class="home-nav" data-i18n-aria-label="index.nav.aria">
                <a href="#overview" data-i18n="index.nav.overview">Overview</a>
                <a href="#for-ta" data-i18n="index.nav.forTa">For TA</a>
                <a href="#for-mo" data-i18n="index.nav.forMo">For MO</a>
                <a href="#for-admin" data-i18n="index.nav.forAdmin">For Admin</a>
                <a href="#process" data-i18n="index.nav.process">Process</a>
                <a href="#faq" data-i18n="index.nav.faq">FAQ</a>
            </nav>

            <div class="home-header-actions">
                <div class="locale-switch" role="group" data-i18n-aria-label="common.locale.switchAria">
                    <button class="locale-btn" type="button" data-locale-switch data-locale="zh-CN" data-i18n="common.locale.zh">Chinese</button>
                    <span class="locale-divider">/</span>
                    <button class="locale-btn" type="button" data-locale-switch data-locale="en" data-i18n="common.locale.en">English</button>
                </div>
                <a class="home-link-btn secondary" href="<%= contextPath %>/login.jsp" data-i18n="common.action.signIn">Sign in</a>
                <a class="home-link-btn primary" href="<%= contextPath %>/register.jsp" data-i18n="common.action.createAccount">Create account</a>
            </div>
        </div>
    </header>

    <main class="portal-home">

        <section id="overview" class="home-hero">
            <div class="hero-copy">
                <p class="hero-badge" data-i18n="index.hero.badge">Role-based TA hiring system</p>
                <h1 data-i18n="index.hero.title">Coordinate TA profiles, postings, reviews, and workload</h1>
                <p class="hero-subtitle" data-i18n="index.hero.subtitle">
                    A role-based system where TAs apply, MOs review applications, and admins manage workload, invite codes, and announcements.
                </p>
                <div class="hero-actions">
                    <a class="home-link-btn primary" href="<%= contextPath %>/register.jsp" data-i18n="index.hero.primary">Get started</a>
                    <a class="home-link-btn secondary" href="<%= contextPath %>/login.jsp" data-i18n="index.hero.secondary">Sign in</a>
                </div>
                <p class="hero-admin">
                    <span data-i18n="index.hero.adminHint">Need admin access?</span>
                    <a href="<%= contextPath %>/admin-invite.jsp" data-i18n="index.hero.adminLink">Use admin invite code</a>
                </p>
            </div>
            <div class="hero-workflow-board" aria-label="TA hiring workflow preview" data-i18n-aria-label="index.preview.workflowAriaLabel">
                <div class="board-heading">
                    <p data-i18n="index.preview.workflowTitle">Live role flow</p>
                    <span data-i18n="index.preview.workflowSubtitle">CSV-backed Servlet/JSP project</span>
                </div>
                <div class="workflow-lanes">
                    <article class="workflow-lane lane-ta">
                        <header>
                            <span class="lane-code">TA</span>
                            <div>
                                <h3 data-i18n="index.preview.taLaneTitle">Applicant workspace</h3>
                                <p data-i18n="index.preview.taLaneMeta">Profile, jobs, applications</p>
                            </div>
                        </header>
                        <ul>
                            <li><span data-i18n="index.preview.taItem1">Profile and resume ready</span><b data-i18n="index.preview.stateReady">Ready</b></li>
                            <li><span data-i18n="index.preview.taItem2">Open jobs available</span><b data-i18n="index.preview.stateOpen">Open</b></li>
                            <li><span data-i18n="index.preview.taItem3">Application status tracked</span><b data-i18n="index.preview.stateActive">Active</b></li>
                        </ul>
                    </article>
                    <article class="workflow-lane lane-mo">
                        <header>
                            <span class="lane-code">MO</span>
                            <div>
                                <h3 data-i18n="index.preview.moLaneTitle">Organizer review</h3>
                                <p data-i18n="index.preview.moLaneMeta">Postings, applicants, decisions</p>
                            </div>
                        </header>
                        <ul>
                            <li><span data-i18n="index.preview.moItem1">Published TA postings</span><b data-i18n="index.preview.stateOpen">Open</b></li>
                            <li><span data-i18n="index.preview.moItem2">Applicant list by posting</span><b data-i18n="index.preview.stateReview">Review</b></li>
                            <li><span data-i18n="index.preview.moItem3">Accept or reject applications</span><b data-i18n="index.preview.stateDecision">Decision</b></li>
                        </ul>
                    </article>
                    <article class="workflow-lane lane-admin">
                        <header>
                            <span class="lane-code">AD</span>
                            <div>
                                <h3 data-i18n="index.preview.adminLaneTitle">Admin operations</h3>
                                <p data-i18n="index.preview.adminLaneMeta">Workload, invite code, notices</p>
                            </div>
                        </header>
                        <ul>
                            <li><span data-i18n="index.preview.adminItem1">Accepted TA workload</span><b data-i18n="index.preview.stateActive">Active</b></li>
                            <li><span data-i18n="index.preview.adminItem2">8-character invite code</span><b data-i18n="index.preview.stateReady">Ready</b></li>
                            <li><span data-i18n="index.preview.adminItem3">System announcements</span><b data-i18n="index.preview.stateLive">Live</b></li>
                        </ul>
                    </article>
                </div>
            </div>
        </section>

        <section class="home-preview">
            <div class="section-head">
                <h2 data-i18n="index.preview.title">Current project modules at a glance</h2>
                <p data-i18n="index.preview.subtitle">The homepage reflects the role pages and API flows implemented in this project.</p>
            </div>
            <div class="preview-grid">
                <article class="preview-card">
                    <h3 data-i18n="index.preview.cardTaTitle">TA workspace</h3>
                    <p data-i18n="index.preview.cardTaDesc">Maintain a profile, upload resume/photo, browse openings, apply, and track application status.</p>
                </article>
                <article class="preview-card">
                    <h3 data-i18n="index.preview.cardMoTitle">MO workspace</h3>
                    <p data-i18n="index.preview.cardMoDesc">Publish postings, manage your jobs, review applications, and accept or reject candidates.</p>
                </article>
                <article class="preview-card">
                    <h3 data-i18n="index.preview.cardAdminTitle">Admin workspace</h3>
                    <p data-i18n="index.preview.cardAdminDesc">Review accepted TA workload, refresh invite codes, and publish announcements.</p>
                </article>
            </div>
        </section>

        <section id="for-ta" class="role-section role-ta">
            <h2 data-i18n="index.forTa.title">For teaching assistants</h2>
            <p class="role-lead" data-i18n="index.forTa.lead">Everything a TA needs from profile setup to application status tracking.</p>
            <ul>
                <li data-i18n="index.forTa.item1">Build and update your profile with resume and skills.</li>
                <li data-i18n="index.forTa.item2">Search open positions, or request optional AI job recommendations.</li>
                <li data-i18n="index.forTa.item3">Submit applications, open job details, and check pending, accepted, rejected, or withdrawn updates.</li>
            </ul>
            <a class="inline-link" href="<%= contextPath %>/login.jsp" data-i18n="index.forTa.cta">Sign in as TA</a>
        </section>

        <section id="for-mo" class="role-section role-mo">
            <h2 data-i18n="index.forMo.title">For module organizers</h2>
            <p class="role-lead" data-i18n="index.forMo.lead">Publish openings, manage your postings, and review applications from one workflow.</p>
            <ul>
                <li data-i18n="index.forMo.item1">Create and maintain postings with course, skills, slots, workload, salary, and deadline.</li>
                <li data-i18n="index.forMo.item2">Open applicant lists from your postings and review profiles, resumes, and cover letters.</li>
                <li data-i18n="index.forMo.item3">Accept or reject applications, with optional AI recommendations and analysis as support.</li>
            </ul>
            <a class="inline-link" href="<%= contextPath %>/login.jsp" data-i18n="index.forMo.cta">Sign in as MO</a>
        </section>

        <section id="for-admin" class="role-section role-admin">
            <h2 data-i18n="index.forAdmin.title">For administrators</h2>
            <p class="role-lead" data-i18n="index.forAdmin.lead">Manage the operational pieces that support the hiring workflow.</p>
            <ul>
                <li data-i18n="index.forAdmin.item1">Review accepted TA workload by TA, job, course, weekly hours, and active period.</li>
                <li data-i18n="index.forAdmin.item2">View or refresh the current 8-character admin invite code.</li>
                <li data-i18n="index.forAdmin.item3">Publish announcements that TA, MO, and Admin users can read.</li>
            </ul>
            <a class="inline-link" href="<%= contextPath %>/login.jsp" data-i18n="index.forAdmin.cta">Sign in as Admin</a>
        </section>

        <section id="process" class="process-section">
            <div class="section-head">
                <h2 data-i18n="index.process.title">From registration to final offer</h2>
                <p data-i18n="index.process.lead">The homepage mirrors the current end-to-end process in the system.</p>
            </div>
            <div class="process-grid">
                <article class="process-card">
                    <h3 data-i18n="index.process.step1Title">1. Register account</h3>
                    <p data-i18n="index.process.step1Desc">TA/MO use standard registration. Admin accounts are created with an 8-character invite code.</p>
                </article>
                <article class="process-card">
                    <h3 data-i18n="index.process.step2Title">2. Complete profile or post job</h3>
                    <p data-i18n="index.process.step2Desc">TAs prepare profile details. MOs publish openings with requirements and deadlines.</p>
                </article>
                <article class="process-card">
                    <h3 data-i18n="index.process.step3Title">3. Apply and review</h3>
                    <p data-i18n="index.process.step3Desc">TAs submit applications. MOs review applicants and make selection decisions.</p>
                </article>
                <article class="process-card">
                    <h3 data-i18n="index.process.step4Title">4. Track status and workload</h3>
                    <p data-i18n="index.process.step4Desc">TAs monitor outcomes, MOs complete decisions, and admins review accepted TA workload.</p>
                </article>
            </div>
        </section>

        <section class="ai-section">
            <div class="section-head">
                <h2 data-i18n="index.ai.title">Optional AI recommendation and analysis</h2>
                <p data-i18n="index.ai.lead">AI is an add-on to TA and MO workflows, not a separate workflow.</p>
            </div>
            <ul class="ai-list">
                <li data-i18n="index.ai.item1">TAs can request job recommendations based on their profile and open positions.</li>
                <li data-i18n="index.ai.item2">MOs can request applicant recommendations for their published jobs.</li>
                <li data-i18n="index.ai.item3">TA/MO detail pages can request analysis; if AI is unavailable, manual review still works.</li>
            </ul>
        </section>

        <section id="faq" class="faq-section">
            <h2 data-i18n="index.faq.title">Frequently asked questions</h2>
            <article class="faq-item">
                <h3 data-i18n="index.faq.q1">Do I need to visit this page every time?</h3>
                <p data-i18n="index.faq.a1">No. Returning users can open the login page directly and continue from there.</p>
            </article>
            <article class="faq-item">
                <h3 data-i18n="index.faq.q2">Which role should I choose?</h3>
                <p data-i18n="index.faq.a2">Choose TA for applicants, MO for module organizers, and Admin only for platform managers with an invite code.</p>
            </article>
            <article class="faq-item">
                <h3 data-i18n="index.faq.q3">Can I switch language later?</h3>
                <p data-i18n="index.faq.a3">Yes. Use the top-right language switch at any time. Your choice is remembered.</p>
            </article>
        </section>

        <footer class="home-footer">
            <p data-i18n="common.footer.copyright">TA Hiring System © 2026</p>
        </footer>
    </main>

    <script src="<%= contextPath %>/js/common/i18n.js" defer></script>
</body>
</html>

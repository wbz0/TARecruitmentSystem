<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TA Profile Setup - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta-dashboard.css">
</head>
<body>
    <main class="profile-page">
        <section class="profile-hero" aria-labelledby="profile-page-title">
            <div class="hero-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                    <path d="M12 4.5L20 8.5L12 12.5L4 8.5L12 4.5ZM7.2 10.1V14.2C7.2 16.6 9.5 18.4 12 18.4C14.5 18.4 16.8 16.6 16.8 14.2V10.1L12 12.5L7.2 10.1Z" />
                </svg>
            </div>
            <span class="profile-badge">TA Workspace</span>
            <h1 id="profile-page-title">Build your applicant profile</h1>
            <p class="subtitle">Complete your academic details first. Resume upload will be added in the next step.</p>
        </section>

        <section class="profile-layout" aria-label="TA applicant profile setup">
            <section class="profile-card">
                <div class="section-heading">
                    <div>
                        <p class="eyebrow">Step 1</p>
                        <h2>Create your TA profile</h2>
                        <p class="section-copy">Provide the information the system needs before you browse and apply for positions.</p>
                    </div>
                    <a class="logout-link" href="<%= contextPath %>/logout">Log out</a>
                </div>

                <div id="form-message" class="form-message hidden" role="alert" aria-live="polite"></div>
                <div id="existing-profile-banner" class="profile-banner hidden" role="status" aria-live="polite"></div>

                <form id="ta-profile-form" class="profile-form" method="post" action="<%= contextPath %>/applicant" novalidate>
                    <section class="form-section" aria-labelledby="section-basic-info">
                        <div class="form-section-header">
                            <h3 id="section-basic-info">Basic details</h3>
                            <p>These fields are required to create your profile.</p>
                        </div>

                        <div class="field-grid">
                            <div class="field">
                                <div class="field-label-row">
                                    <label for="full-name">Full name</label>
                                    <span class="field-tag">Required</span>
                                </div>
                                <input
                                    id="full-name"
                                    name="fullName"
                                    type="text"
                                    placeholder="Your full name"
                                    autocomplete="name"
                                    maxlength="100"
                                    required
                                >
                            </div>

                            <div class="field">
                                <div class="field-label-row">
                                    <label for="student-id">Student ID</label>
                                    <span class="field-tag">Required</span>
                                </div>
                                <input
                                    id="student-id"
                                    name="studentId"
                                    type="text"
                                    placeholder="e.g. 2026123456"
                                    maxlength="50"
                                    required
                                >
                            </div>

                            <div class="field">
                                <div class="field-label-row">
                                    <label for="department">Department</label>
                                    <span class="field-tag">Required</span>
                                </div>
                                <input
                                    id="department"
                                    name="department"
                                    type="text"
                                    placeholder="School or department"
                                    maxlength="100"
                                    required
                                >
                            </div>

                            <div class="field">
                                <div class="field-label-row">
                                    <label for="program">Program</label>
                                    <span class="field-tag">Required</span>
                                </div>
                                <select id="program" name="program" required>
                                    <option value="">Select your program</option>
                                    <option value="Undergraduate">Undergraduate</option>
                                    <option value="Master">Master</option>
                                    <option value="PhD">PhD</option>
                                </select>
                            </div>
                        </div>
                    </section>

                    <section class="form-section" aria-labelledby="section-additional-info">
                        <div class="form-section-header">
                            <h3 id="section-additional-info">Additional information</h3>
                            <p>These fields are optional for now, but completing them will make your profile stronger.</p>
                        </div>

                        <div class="field-grid">
                            <div class="field">
                                <div class="field-label-row">
                                    <label for="gpa">GPA</label>
                                </div>
                                <input
                                    id="gpa"
                                    name="gpa"
                                    type="text"
                                    placeholder="e.g. 3.85 / 4.00"
                                    inputmode="decimal"
                                    maxlength="20"
                                >
                            </div>

                            <div class="field">
                                <div class="field-label-row">
                                    <label for="phone">Phone number</label>
                                </div>
                                <input
                                    id="phone"
                                    name="phone"
                                    type="tel"
                                    placeholder="+86 138 0000 0000"
                                    autocomplete="tel"
                                    maxlength="30"
                                >
                            </div>

                            <div class="field field-full">
                                <div class="field-label-row">
                                    <label for="skills">Skills</label>
                                </div>
                                <input
                                    id="skills"
                                    name="skills"
                                    type="text"
                                    placeholder="Separate skills with commas, for example Java, JSP, SQL"
                                    maxlength="300"
                                >
                                <p class="field-hint">Use commas to separate each skill. The current backend stores your skills as a list.</p>
                            </div>

                            <div class="field field-full">
                                <div class="field-label-row">
                                    <label for="address">Address</label>
                                </div>
                                <input
                                    id="address"
                                    name="address"
                                    type="text"
                                    placeholder="City, district, or campus address"
                                    autocomplete="street-address"
                                    maxlength="200"
                                >
                            </div>

                            <div class="field field-full">
                                <div class="field-label-row">
                                    <label for="experience">Related experience</label>
                                </div>
                                <textarea
                                    id="experience"
                                    name="experience"
                                    rows="5"
                                    maxlength="1200"
                                    placeholder="Describe tutoring, teaching, grading, or project experience relevant to a TA role."
                                ></textarea>
                            </div>

                            <div class="field field-full">
                                <div class="field-label-row">
                                    <label for="motivation">Motivation</label>
                                </div>
                                <textarea
                                    id="motivation"
                                    name="motivation"
                                    rows="5"
                                    maxlength="1200"
                                    placeholder="Explain why you want this TA opportunity and what value you can bring."
                                ></textarea>
                            </div>
                        </div>
                    </section>

                    <div class="form-actions">
                        <button id="profile-submit" class="profile-submit-btn" type="submit">
                            Create profile
                        </button>
                        <p class="action-hint">You can continue to enrich this profile later in the next planned steps.</p>
                    </div>
                </form>
            </section>

            <aside class="profile-side-panel" aria-label="profile setup guidance">
                <section class="side-card">
                    <p class="side-card-label">Profile checklist</p>
                    <h3>What to prepare now</h3>
                    <ul class="checklist">
                        <li>Your official full name and student ID</li>
                        <li>Your department and current study program</li>
                        <li>Your GPA, skills, and contact details</li>
                        <li>A short summary of experience and motivation</li>
                    </ul>
                </section>

                <section class="side-card upload-placeholder-card" aria-labelledby="resume-placeholder-title">
                    <p class="side-card-label">Next planned task</p>
                    <h3 id="resume-placeholder-title">Resume upload placeholder</h3>
                    <p class="side-card-copy">Resume upload and progress display will be connected in the next implementation round.</p>
                    <div class="upload-placeholder-box">
                        <div class="upload-placeholder-icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24" focusable="false">
                                <path d="M12 3.75L16.5 8.25V18.75H7.5V5.25H12ZM12.75 4.81V8.25H16.19L12.75 4.81ZM9.75 12H14.25V13.5H9.75V12ZM9.75 15H14.25V16.5H9.75V15ZM9.75 9H11.25V10.5H9.75V9Z" />
                            </svg>
                        </div>
                        <div>
                            <p class="upload-placeholder-title">Upload disabled for this round</p>
                            <p class="upload-placeholder-text">Accepted file types will be PDF, DOC, and DOCX after the next planned task is implemented.</p>
                        </div>
                    </div>
                    <button class="placeholder-button" type="button" disabled>Upload resume soon</button>
                </section>
            </aside>
        </section>
    </main>

    <script>
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/ta-dashboard.js" defer></script>
</body>
</html>

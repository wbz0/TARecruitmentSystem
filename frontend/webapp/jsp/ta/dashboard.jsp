<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String username = "";
    Object usernameObj = session.getAttribute("username");
    if (usernameObj != null) {
        username = usernameObj.toString();
    }
    String userInitial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "T";
%>
<%-- TA profile page: form fields are saved to /api/me/applicant-profile by ta-dashboard.js. --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="<%= contextPath %>/js/common/locale-bootstrap.js"></script>
    <title data-i18n="portal.page.taDashboard.title">TA Profile Setup - TA Hiring System</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/ta/ta-dashboard.css?v=20260326a">
</head>
<body>
    <div class="portal-shell portal-shell-ta">
        <% String portalRole = "ta"; String activeNav = "profile"; String pageTitleKey = "portal.taDashboard.profileLabel"; String pageTitleFallback = "Personal Profile"; %>
        <%@ include file="/WEB-INF/jsp/fragments/portal-sidebar.jspf" %>

        <section class="portal-main">
            <%@ include file="/WEB-INF/jsp/fragments/portal-topbar.jspf" %>

            <div class="portal-content">
                <main class="profile-page">
                    <section class="profile-hero" aria-labelledby="profile-page-title">
                        <h1 id="profile-page-title" class="portal-page-title" data-i18n="portal.taDashboard.profileLabel">Personal Profile</h1>
                        <p class="subtitle" data-i18n="portal.taDashboard.subtitle">Manage your personal information and academic background.</p>
                    </section>

                    <section class="profile-layout" aria-label="TA applicant profile setup" data-i18n-aria-label="portal.taDashboard.profileLayoutAria">
                        <section class="profile-card">
                            <div class="section-heading">
                                <h2 data-i18n="portal.taDashboard.createProfileTitle">Create your TA profile</h2>
                            </div>

                            <div id="form-message" class="form-message hidden" role="alert" aria-live="polite"></div>

                            <form id="ta-profile-form" class="profile-form" method="post" action="<%= contextPath %>/api/me/applicant-profile" novalidate>
                                <section class="form-section" aria-labelledby="section-basic-info">
                                    <div class="form-section-header">
                                        <h3 id="section-basic-info" data-i18n="portal.taDashboard.basicDetails">Basic details</h3>
                                    </div>

                                    <div class="avatar-upload-area">
                                        <label class="sr-only" for="photo-file-input" data-i18n="portal.taDashboard.chooseFile">Choose file</label>
                                        <input
                                            id="photo-file-input"
                                            class="upload-file-input"
                                            type="file"
                                            name="photo"
                                            accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                                        >
                                        <div id="photo-upload-shell" class="photo-upload-shell is-empty">
                                            <button
                                                id="photo-file-trigger"
                                                class="avatar-trigger"
                                                type="button"
                                                aria-label="Upload profile photo"
                                                data-i18n-aria-label="portal.taDashboard.photoUploadTitle"
                                            >
                                                <span id="photo-empty-state" class="avatar-empty-state">
                                                    <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                                                        <path d="M12 15.5a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z"/>
                                                        <path d="M5.25 8.75h2l1.1-1.65h7.3l1.1 1.65h2a1.25 1.25 0 0 1 1.25 1.25v7a1.25 1.25 0 0 1-1.25 1.25H5.25A1.25 1.25 0 0 1 4 16.75V10a1.25 1.25 0 0 1 1.25-1.25Z"/>
                                                    </svg>
                                                </span>
                                                <span id="photo-filled-state" class="avatar-filled-state hidden" hidden>
                                                    <img id="photo-preview-image" class="avatar-preview-image" alt="Profile photo" data-i18n-alt="portal.taDashboard.profilePhotoAlt">
                                                </span>
                                            </button>
                                            <button
                                                id="photo-remove-btn"
                                                class="avatar-remove-btn hidden"
                                                type="button"
                                                data-i18n-aria-label="portal.taDashboard.photoRemoveAria"
                                                aria-label="Remove photo"
                                                hidden
                                            >
                                                <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                                                    <path d="m7 7 10 10"></path>
                                                    <path d="M17 7 7 17"></path>
                                                </svg>
                                            </button>
                                        </div>
                                        <div class="avatar-upload-meta">
                                            <p class="avatar-upload-label" data-i18n="portal.taDashboard.photoUploadTitle">Photo upload</p>
                                            <p class="avatar-upload-hint" data-i18n="portal.taDashboard.photoCardEmptyHint">JPG, PNG, or WEBP. Maximum size is 5MB.</p>
                                            <div id="photo-upload-message" class="upload-message hidden" role="status" aria-live="polite"></div>
                                        </div>
                                    </div>

                                    <div class="field-grid">
                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="full-name" data-i18n="portal.taDashboard.fullName">Full name</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Full name format" data-i18n-aria-label="portal.taDashboard.fullNameInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.fullNameTooltip">Letters, spaces, hyphens, apostrophes, and periods. At least 2 characters.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input
                                                id="full-name"
                                                name="fullName"
                                                type="text"
                                                placeholder="Your full name"
                                                data-i18n-placeholder="portal.taDashboard.fullNamePlaceholder"
                                                autocomplete="name"
                                                maxlength="100"
                                                required
                                            >
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="student-id" data-i18n="portal.taDashboard.studentId">Student ID</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Student ID format" data-i18n-aria-label="portal.taDashboard.studentIdInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.studentIdTooltip">10-digit number starting with 20, e.g. 2023213039.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input
                                                id="student-id"
                                                name="studentId"
                                                type="text"
                                                placeholder="e.g. 2023213039"
                                                data-i18n-placeholder="portal.taDashboard.studentIdPlaceholder"
                                                inputmode="numeric"
                                                maxlength="10"
                                                required
                                            >
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="department" data-i18n="portal.taDashboard.department">Department</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Department format" data-i18n-aria-label="portal.taDashboard.departmentInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.departmentTooltip">Your school or department name, 2–100 characters.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input
                                                id="department"
                                                name="department"
                                                type="text"
                                                placeholder="School or department"
                                                data-i18n-placeholder="portal.taDashboard.departmentPlaceholder"
                                                maxlength="100"
                                                required
                                            >
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="program" data-i18n="portal.taDashboard.program">Program</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Program info" data-i18n-aria-label="portal.taDashboard.programInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.programTooltip">Select the level that matches your current enrollment.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <select id="program" name="program" required>
                                                <option value="" data-i18n="portal.taDashboard.selectProgram">Select your program</option>
                                                <option value="Undergraduate" data-i18n="portal.taDashboard.programUndergraduate">Undergraduate</option>
                                                <option value="Master" data-i18n="portal.taDashboard.programMaster">Master</option>
                                                <option value="PhD" data-i18n="portal.taDashboard.programPhd">PhD</option>
                                            </select>
                                        </div>
                                    </div>
                                </section>

                                <section class="form-section">
                                    <div class="field-grid">
                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="gpa" data-i18n="portal.taDashboard.gpa">GPA</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="GPA format" data-i18n-aria-label="portal.taDashboard.gpaInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.gpaTooltip">Enter your GPA, e.g. 3.85 or 3.85/4.00 (value/scale).</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input
                                                id="gpa"
                                                name="gpa"
                                                type="text"
                                                placeholder="e.g. 3.85 / 4.00"
                                                data-i18n-placeholder="portal.taDashboard.gpaPlaceholder"
                                                inputmode="decimal"
                                                maxlength="20"
                                                required
                                            >
                                        </div>

                                        <div class="field">
                                            <div class="field-label-row">
                                                <label for="phone" data-i18n="portal.taDashboard.phone">Phone number</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Phone number format" data-i18n-aria-label="portal.taDashboard.phoneInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.phoneTooltip">8–15 digits, international format accepted, e.g. +86 138 0000 0000.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input
                                                id="phone"
                                                name="phone"
                                                type="tel"
                                                placeholder="+86 138 0000 0000"
                                                data-i18n-placeholder="portal.taDashboard.phonePlaceholder"
                                                autocomplete="tel"
                                                maxlength="30"
                                                required
                                            >
                                        </div>

                                        <div class="field field-full">
                                            <div class="field-label-row">
                                                <label for="skills" data-i18n="portal.taDashboard.skills">Skills</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Skills format" data-i18n-aria-label="portal.taDashboard.skillsInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.skillsTooltip">Comma or semicolon-separated, up to 12 skills, e.g. Java, SQL, Python.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <input
                                                id="skills"
                                                name="skills"
                                                type="text"
                                                placeholder="Separate skills with commas, for example Java, JSP, SQL"
                                                data-i18n-placeholder="portal.taDashboard.skillsPlaceholder"
                                                maxlength="300"
                                                required
                                            >
                                            <p class="field-hint" data-i18n="portal.taDashboard.skillsHint">Use commas to separate each skill. The current backend stores your skills as a list.</p>
                                        </div>

                                        <div class="field field-full">
                                            <div class="field-label-row">
                                                <label for="experience" data-i18n="portal.taDashboard.experience">Related experience</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Related experience info" data-i18n-aria-label="portal.taDashboard.experienceInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.experienceTooltip">Describe relevant teaching or tutoring experience. At least 10 words.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <textarea
                                                id="experience"
                                                name="experience"
                                                rows="5"
                                                maxlength="1200"
                                                placeholder="Describe tutoring, teaching, grading, or project experience relevant to a TA role."
                                                data-i18n-placeholder="portal.taDashboard.experiencePlaceholder"
                                                required
                                            ></textarea>
                                        </div>

                                        <div class="field field-full">
                                            <div class="field-label-row">
                                                <label for="motivation" data-i18n="portal.taDashboard.motivation">Motivation</label>
                                                <span class="field-label-end">
                                                    <button type="button" class="field-info-btn" aria-label="Motivation info" data-i18n-aria-label="portal.taDashboard.motivationInfoAria">
                                                        i
                                                        <span class="field-tooltip" data-i18n="portal.taDashboard.motivationTooltip">Explain your motivation for this TA role. At least 10 words.</span>
                                                    </button>
                                                    <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                                </span>
                                            </div>
                                            <textarea
                                                id="motivation"
                                                name="motivation"
                                                rows="5"
                                                maxlength="1200"
                                                placeholder="Explain why you want this TA opportunity and what value you can bring."
                                                data-i18n-placeholder="portal.taDashboard.motivationPlaceholder"
                                                required
                                            ></textarea>
                                        </div>
                                    </div>
                                </section>

                            </form>

                            <div class="profile-card-divider" aria-hidden="true"></div>

                            <section class="upload-card" aria-labelledby="resume-upload-title">
                                <div class="form-section-header">
                                    <div class="section-header-row">
                                        <h3 id="resume-upload-title" data-i18n="portal.taDashboard.resumeUploadTitle">Resume upload</h3>
                                        <span class="field-tag" data-i18n="portal.taDashboard.required">Required</span>
                                    </div>
                                </div>

                                <div class="upload-file-panel">
                                    <label class="sr-only" for="resume-file-input" data-i18n="portal.taDashboard.chooseFile">Choose file</label>
                                    <input
                                        id="resume-file-input"
                                        class="upload-file-input"
                                        type="file"
                                        name="resume"
                                        accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    >
                                    <div id="resume-upload-shell" class="resume-upload-shell is-empty">
                                        <button
                                            id="resume-file-trigger"
                                            class="resume-upload-card"
                                            type="button"
                                            onclick="if (this.dataset.previewUrl) { window.location.href = this.dataset.previewUrl; return false; }"
                                        >
                                            <span id="resume-empty-state" class="resume-card-empty">
                                                <span class="resume-card-icon resume-card-icon-upload" aria-hidden="true">
                                                    <svg viewBox="0 0 24 24" focusable="false">
                                                        <path d="M12 15V7.5"></path>
                                                        <path d="m8.75 10.75 3.25-3.25 3.25 3.25"></path>
                                                        <path d="M5.25 15.75v1.5a1.5 1.5 0 0 0 1.5 1.5h10.5a1.5 1.5 0 0 0 1.5-1.5v-1.5"></path>
                                                    </svg>
                                                </span>
                                                <span class="resume-card-title" data-i18n="portal.taDashboard.resumeCardEmptyTitle">Upload your resume</span>
                                                <span class="resume-card-subtitle" data-i18n="portal.taDashboard.resumeCardEmptyHint">PDF, DOC, or DOCX. Maximum size is 10MB.</span>
                                            </span>
                                            <span id="resume-filled-state" class="resume-card-file hidden" hidden>
                                                <span class="resume-card-icon resume-card-icon-file" aria-hidden="true">
                                                    <svg viewBox="0 0 24 24" focusable="false">
                                                        <path d="M8.25 3.75h5.19l4.31 4.31v10.19a1.5 1.5 0 0 1-1.5 1.5h-8a1.5 1.5 0 0 1-1.5-1.5v-13a1.5 1.5 0 0 1 1.5-1.5Z"></path>
                                                        <path d="M13.5 3.75v4.5H18"></path>
                                                        <path d="M9.75 12h4.5"></path>
                                                        <path d="M9.75 15h4.5"></path>
                                                    </svg>
                                                </span>
                                                <span class="resume-card-file-meta">
                                                    <span id="resume-file-display-name" class="resume-card-file-name">resume.pdf</span>
                                                    <span id="resume-file-display-detail" class="resume-card-file-detail">0 KB</span>
                                                </span>
                                            </span>
                                        </button>
                                        <button
                                            id="resume-remove-btn"
                                            class="resume-remove-btn hidden"
                                            type="button"
                                            data-i18n-aria-label="portal.taDashboard.resumeRemoveAria"
                                            aria-label="Remove resume"
                                            hidden
                                        >
                                            <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                                                <path d="m7 7 10 10"></path>
                                                <path d="M17 7 7 17"></path>
                                            </svg>
                                        </button>
                                    </div>
                                </div>
                                <div id="resume-upload-message" class="upload-message hidden" role="status" aria-live="polite"></div>
                            </section>

                            <div id="profile-card-actions" class="form-actions profile-card-actions">
                                <button
                                    id="profile-submit"
                                    class="profile-submit-btn"
                                    type="submit"
                                    form="ta-profile-form"
                                    data-i18n="portal.taDashboard.saveChangesButton"
                                >
                                    Save changes
                                </button>
                                <button id="profile-edit-btn" class="ghost-btn" type="button" hidden data-i18n="portal.taDashboard.editProfileButton">Edit profile</button>
                                <button id="profile-cancel-btn" class="ghost-btn" type="button" hidden data-i18n="portal.taDashboard.cancelButton">Cancel</button>
                            </div>
                        </section>
                    </section>
                </main>
            </div>
        </section>
    </div>

    <script>
        // Injected for ta-dashboard.js and shared sidebar account dialog.
        window.APP_CONTEXT_PATH = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/common/i18n.js?v=20260326a" defer></script>
    <script src="<%= contextPath %>/js/common/portal-i18n.js?v=20260326a" defer></script>
    <script src="<%= contextPath %>/js/common/ta-recruitment.js?v=20260514-architecture" defer></script>
    <script src="<%= contextPath %>/js/ta/ta-dashboard.js?v=20260514-profile-fixes3" defer></script>
</body>
</html>

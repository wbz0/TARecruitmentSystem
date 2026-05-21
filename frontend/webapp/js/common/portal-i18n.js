/*
 * Portal i18n dynamic text patches.
 *
 * Centralized handling of prompts, button text and server error translations generated during page runtime.
 * Legacy/pending removal: if certain keys have been migrated back to i18n.js and have no dynamic generation entry, the duplicate mapping can be removed from here.
 */
(function () {
    "use strict";
    var CHINESE_LOCALE = "zh-CN";

    /*
     * Check if global AppI18n has been loaded.
     */
    function hasI18n() {
        return window.AppI18n && typeof window.AppI18n.t === "function";
    }

    /*
     * Dynamic text read fallback.
     */
    function t(key, fallback) {
        if (hasI18n()) {
            return window.AppI18n.t(key, fallback || key);
        }
        return fallback || key;
    }

    /*
     * English dynamic plural text needs plural suffix, Chinese does not.
     */
    function useEnglishPluralSuffix() {
        if (!hasI18n() || typeof window.AppI18n.getLocale !== "function") {
            return true;
        }
        return window.AppI18n.getLocale() === "en";
    }

    /*
     * Read current language for dynamic scripts to choose Chinese patch or English original.
     */
    function currentLocale() {
        if (window.AppI18n && typeof window.AppI18n.getLocale === "function") {
            return window.AppI18n.getLocale();
        }
        var lang = (document.documentElement.getAttribute("lang") || "").toLowerCase();
        if (lang.indexOf("zh") === 0) {
            return CHINESE_LOCALE;
        }
        return "en";
    }

    /*
     * Chinese dynamic text patches.
     * Mainly covers messages assembled at JS runtime, does not replace i18n.js static dictionary.
     */
    var localZhByKey = {
        "portal.dynamic.currentCompleteness": "Current completeness:",
        "portal.dynamic.checkingProfile": "Checking profile...",
        "portal.dynamic.creatingProfile": "Creating profile...",
        "portal.dynamic.profileAlreadyExists": "A profile already exists for this account, loading your saved content...",
        "portal.dynamic.fixHighlightedFields": "Please fix highlighted fields before trying again.",
        "portal.dynamic.noProfileFound": "No profile found yet. Please complete the form below.",
        "portal.dynamic.unableCheckProfile": "Unable to check existing profile right now. You can still try creating one.",
        "portal.dynamic.unableCreateProfile": "Unable to create profile. Please check the form and try again.",
        "portal.dynamic.unableUpdateProfile": "Unable to update profile. Please check the form and try again.",
        "portal.dynamic.profileCreatedUploadingResume": "Profile created successfully. Uploading your selected resume...",
        "portal.dynamic.profileCreatedResumeFailed": "Profile created successfully, but resume upload failed. Please try again later.",
        "portal.dynamic.profileCreatedSuccess": "Profile created successfully. Your saved information is now displayed below.",
        "portal.dynamic.profileCreatedResumeFailed": "Profile created successfully, but resume upload failed. Please try again later.",
        "portal.dynamic.profileUpdatedSuccess": "Profile updated successfully.",
        "portal.dynamic.savingChanges": "Saving...",
        "portal.dynamic.profileReadonly": "Your profile has been created and is currently displayed in read-only mode.",
        "portal.dynamic.currentResumePrefix": "Current uploaded resume:",
        "portal.dynamic.noResumeUploaded": "No resume uploaded yet.",
        "portal.dynamic.noResumeSelected": "No resume file selected.",
        "portal.dynamic.chooseResumeFirst": "Please select a resume file first.",
        "portal.dynamic.resumeRequiredToSave": "Please upload your resume before saving your profile.",
        "portal.dynamic.choosePhotoFirst": "Please select a photo file first.",
        "portal.dynamic.photoReadyToSave": "Photo selected. Save changes to apply it.",
        "portal.dynamic.savedPhotoRemoved": "Current photo removed. Save changes to apply it.",
        "portal.dynamic.photoReady": "Photo ready",
        "portal.dynamic.resumeDraftUploading": "Uploading resume draft:",
        "portal.dynamic.resumeDraftSaved": "Resume draft uploaded. Save changes to apply it.",
        "portal.dynamic.resumeDraftReplaceSaved": "New resume uploaded. Save changes to replace the current resume.",
        "portal.dynamic.pendingResumePrefix": "Pending resume:",
        "portal.dynamic.pendingResumeCreateSuffix": " Fill in other information and click Save changes to create profile.",
        "portal.dynamic.pendingResumeReplaceSuffix": " Click Save changes to replace current resume.",
        "portal.dynamic.resumeDiscardFailed": "Unable to discard pending resume. Please try again later.",
        "portal.dynamic.createProfileThenUpload": "Please create profile first, then upload resume.",
        "portal.dynamic.createProfileAutoUpload": "Please create profile first. The selected resume will upload automatically after successful creation.",
        "portal.dynamic.resumeReadyAfterCreate": "Resume file is ready and will upload automatically after profile creation.",
        "portal.dynamic.resumeReadyReplace": "Resume file is ready. Click upload to replace the current resume.",
        "portal.dynamic.resumeWillUploadAfterCreate": "Will upload after profile creation",
        "portal.dynamic.replaceUploadedResume": "Replace uploaded resume",
        "portal.dynamic.uploading": "Uploading",
        "portal.dynamic.uploadCompleted": "Upload completed",
        "portal.dynamic.uploadAborted": "Upload aborted.",
        "portal.dynamic.uploadInterrupted": "Upload interrupted. Please try again.",
        "portal.dynamic.uploadNetworkError": "Network error during resume upload. Please try again later.",
        "portal.dynamic.resumeUploadSuccess": "Resume uploaded successfully.",
        "portal.dynamic.resumeUpdateSuccess": "Resume updated successfully.",
        "portal.dynamic.resumeUploadFailed": "Resume upload failed. Please try again later.",
        "portal.dynamic.invalidResumeFormat": "Invalid file format. Please upload PDF, DOC or DOCX file.",
        "portal.dynamic.resumeTooLarge": "File exceeds 10MB. Please select a smaller file.",
        "portal.dynamic.invalidPhotoFormat": "Invalid photo format. Please upload JPG, PNG or WEBP file.",
        "portal.dynamic.photoTooLarge": "Photo exceeds 5MB. Please select a smaller file.",
        "portal.dynamic.noSpecificSkills": "No specific skills listed.",
        "portal.dynamic.unableLoadJobs": "Unable to load positions right now.",
        "portal.dynamic.unableLoadJobsRetry": "Unable to load positions right now. Please try again later.",
        "portal.dynamic.noJobsForFilters": "No positions under current filter conditions.",
        "portal.dynamic.noJobsAvailable": "No positions available right now.",
        "portal.dynamic.showing": "Showing",
        "portal.dynamic.jobUnit": "position",
        "portal.dynamic.unableLoadPositionsTitle": "Unable to load positions",
        "portal.dynamic.refreshAfterNetworkCheck": "Please check network and click refresh to retry.",
        "portal.dynamic.noPositionsPublishedTitle": "No positions published yet",
        "portal.dynamic.positionsAppearAfterPublish": "When MO publishes new positions, they will appear here.",
        "portal.dynamic.noMatchingPositionsTitle": "No matching positions",
        "portal.dynamic.broadenKeywordHint": "Try broadening keywords or clearing some filter conditions.",
        "portal.dynamic.noExtraTags": "No extra tags",
        "portal.dynamic.viewDetails": "View details",
        "portal.dynamic.applyNow": "Apply now",
        "portal.dynamic.moShort": "MO",
        "portal.dynamic.submitting": "Submitting...",
        "portal.dynamic.applicationSubmitted": "Application submitted.",
        "portal.dynamic.applicationSubmittedRedirect": "Application submitted successfully. Redirecting to application status page...",
        "portal.dynamic.failedSubmitApplication": "Failed to submit application. Please try again.",
        "portal.dynamic.currentAccountCannotSubmit": "Current account cannot submit applications on this page.",
        "portal.dynamic.onlyTaSubmit": "Only TA accounts can submit applications.",
        "portal.dynamic.alreadyApplied": "You have already applied for this position.",
        "portal.dynamic.jobNoLongerAvailable": "This position does not exist. It may have been removed.",
        "portal.dynamic.jobNotAccepting": "This position is not accepting new applications.",
        "portal.dynamic.positionCurrently": "This position is currently",
        "portal.dynamic.newApplicationsDisabled": ". New applications are disabled.",
        "portal.dynamic.jobNotFound": "Position does not exist. It may have been removed.",
        "portal.dynamic.applicationUnavailable": "Application unavailable",
        "portal.dynamic.applicationStatusPrefix": "Application status:",
        "portal.dynamic.applicationAlreadySubmitted": "Already submitted an application.",
        "portal.dynamic.applicationStopped": "Application stopped",
        "portal.dynamic.networkErrorSubmitApplication": "Network error when submitting application.",
        "portal.dynamic.taOnlyPage": "This page is only accessible to TA accounts.",
        "portal.dynamic.unableLoadApplications": "Unable to load your application data.",
        "portal.dynamic.unableLoadApplicationsNow": "Unable to load application data right now.",
        "portal.dynamic.noApplicationsSubmitted": "You have not submitted any applications yet.",
        "portal.dynamic.noApplicationsMatchFilters": "No application records under current filter conditions.",
        "portal.dynamic.applicationUnit": "application",
        "portal.dynamic.unableLoadApplicationsTitle": "Unable to load applications",
        "portal.dynamic.noMatchingApplicationsTitle": "No matching applications",
        "portal.dynamic.noApplicationsYetTitle": "No applications yet",
        "portal.dynamic.statusAppearsAfterApply": "Status will appear here after you apply for a position.",
        "portal.dynamic.clearFiltersToBroaden": "Try clearing status or keyword filters to broaden results.",
        "portal.dynamic.applicationWithdrawnSuccess": "Application withdrawn successfully.",
        "portal.dynamic.unableWithdrawApplication": "Unable to withdraw this application.",
        "portal.dynamic.networkErrorWithdrawApplication": "Network error when withdrawing application.",
        "portal.dynamic.appliedAt": "Application time",
        "portal.dynamic.coverLetterColon": "Cover letter:",
        "portal.dynamic.noCoverLetterProvided": "No cover letter provided.",
        "portal.dynamic.viewJob": "View position",
        "portal.dynamic.withdraw": "Withdraw",
        "portal.dynamic.onlyMoPublish": "Only MO accounts can publish positions.",
        "portal.dynamic.failedPublishJob": "Failed to publish position. Please check input and try again.",
        "portal.dynamic.jobPostedSuccess": "Position published successfully.",
        "portal.dynamic.networkErrorPostingJob": "Network error when publishing position.",
        "portal.dynamic.unableLoadPostings": "Unable to load posting list right now.",
        "portal.dynamic.noJobsPostedYet": "You have not published any positions yet.",
        "portal.dynamic.youHavePosted": "You have published",
        "portal.dynamic.noPostingsYetTitle": "No posting records yet",
        "portal.dynamic.publishFirstTaPosition": "Use the form to publish your first TA position.",
        "portal.dynamic.reviewApplicants": "Review applicants",
        "portal.dynamic.untitledPosition": "Untitled position",
        "portal.dynamic.overviewPartialLoad": "Some overview data failed to load. Available results are displayed.",
        "portal.dynamic.unableLoadOverview": "Unable to load overview data right now.",
        "portal.dynamic.moOnlyPage": "This page is only accessible to MO accounts.",
        "portal.dynamic.noActivityYet": "No activity yet.",
        "portal.dynamic.tracking": "Tracking",
        "portal.dynamic.noRecentActivityTitle": "No recent activity",
        "portal.dynamic.latestUpdatesAppear": "When TAs apply for your positions, latest updates will appear here.",
        "portal.dynamic.newApplicationReceived": "New application received",
        "portal.dynamic.offerAccepted": "Offer accepted",
        "portal.dynamic.applicationRejected": "Application rejected",
        "portal.dynamic.applicationWithdrawn": "Application withdrawn",
        "portal.dynamic.applicationUpdated": "Application updated",
        "portal.dynamic.unknownApplicant": "Unknown applicant",
        "portal.dynamic.failedLoadApplicationTotals": "Failed to load application totals.",
        "portal.dynamic.failedLoadMoWorkloads": "Failed to load MO workloads.",
        "portal.dynamic.networkErrorLoadingDashboard": "Network error when loading dashboard.",
        "portal.dynamic.exporting": "Exporting...",
        "portal.dynamic.csvExportedSuccess": "CSV exported successfully.",
        "portal.dynamic.unableExportCsv": "Unable to export CSV.",
        "portal.dynamic.noMoWorkloadSelectedRange": "No MO workload data in selected time range.",
        "portal.dynamic.loaded": "Loaded",
        "portal.dynamic.moWorkloadItemUnit": "MO workload record",
        "portal.dynamic.noStatusData": "No status data yet.",
        "portal.dynamic.noMoWorkloadData": "No MO workload data yet.",
        "portal.dynamic.noWorkloadDataYetTitle": "No workload data yet",
        "portal.dynamic.adjustTimeRangeHint": "Please adjust the time range or wait for more application activity.",
        "portal.dynamic.sessionExpiredRedirect": "Session expired. Redirecting to login page...",
        "portal.dynamic.networkErrorTryAgain": "Network error. Please try again.",
        "portal.dynamic.networkErrorMoment": "Network error. Please try again in a moment."
    };

    /*
     * Resolve dynamic Chinese text by key.
     * Prefers AppI18n formal dictionary; localZhByKey is only dynamic patch fallback.
     */
    function resolveByKey(key, fallbackText) {
        if (hasI18n()) {
            var localized = window.AppI18n.t(key, "");
            if (localized) {
                return localized;
            }
        }
        if (currentLocale() === CHINESE_LOCALE && Object.prototype.hasOwnProperty.call(localZhByKey, key)) {
            return localZhByKey[key];
        }
        return fallbackText || key;
    }

    /*
     * Static English text to i18n key mapping.
     * Legacy/pending removal: as these texts are gradually migrated to JSP data-i18n, corresponding mappings can be removed.
     */
    function textMap() {
        return {
            "TA Profile Setup - TA Hiring System": { key: "portal.page.taDashboard.title" },
            "Job list - TA Hiring System": { key: "portal.page.taJobList.title" },
            "Job detail - TA Hiring System": { key: "portal.page.taJobDetail.title" },
            "Application status - TA Hiring System": { key: "portal.page.taApplicationStatus.title" },
            "Application detail - TA Hiring System": { key: "portal.page.taApplicationDetail.title" },
            "MO Dashboard - Post TA Jobs": { key: "portal.page.moDashboard.title" },
            "TA Workload - TA Hiring System": { key: "portal.page.adminDashboard.title" },

            "Sign Out": { key: "portal.action.signOut" },
            "Switch Roles": { key: "portal.action.switchRoles" },
            "Jobs": { key: "portal.nav.ta.jobs" },
            "Job List": { key: "portal.nav.ta.jobs" },
            "Status": { key: "portal.nav.ta.status" },
            "Profile": { key: "portal.nav.ta.profile" },
            "Post Job": { key: "portal.nav.mo.postJob" },
            "TA Workload": { key: "portal.nav.admin.dashboard" },
            "TA Portal": { key: "portal.brand.ta" },
            "MO Portal": { key: "portal.brand.mo" },
            "Admin Portal": { key: "portal.brand.admin" },

            "Manage your personal information and academic background.": { key: "portal.taDashboard.subtitle" },
            "Create your TA profile": { key: "portal.taDashboard.createProfileTitle" },
            "Complete the required fields first, then enrich optional details. After creation, this form becomes read-only and you can replace your resume from the right panel.": { key: "portal.taDashboard.createProfileLead" },
            "Basic details": { key: "portal.taDashboard.basicDetails" },
            "These fields are required to create your profile.": { key: "portal.taDashboard.basicDetailsLead" },
            "Full name": { key: "portal.taDashboard.fullName" },
            "Required": { key: "portal.taDashboard.required" },
            "Student ID": { key: "portal.taDashboard.studentId" },
            "Department": { key: "portal.taDashboard.department" },
            "Program": { key: "portal.taDashboard.program" },
            "Select your program": { key: "portal.taDashboard.selectProgram" },
            "Undergraduate": { key: "portal.taDashboard.programUndergraduate" },
            "Master": { key: "portal.taDashboard.programMaster" },
            "PhD": { key: "portal.taDashboard.programPhd" },
            "Additional information": { key: "portal.taDashboard.additionalInfo" },
            "These fields are optional for now, but completing them will make your profile stronger.": { key: "portal.taDashboard.additionalInfoLead" },
            "GPA": { key: "portal.taDashboard.gpa" },
            "Phone number": { key: "portal.taDashboard.phone" },
            "Skills": { key: "portal.taDashboard.skills" },
            "Use commas to separate each skill. The current backend stores your skills as a list.": { key: "portal.taDashboard.skillsHint" },
            "Related experience": { key: "portal.taDashboard.experience" },
            "Motivation": { key: "portal.taDashboard.motivation" },
            "Create profile": { key: "portal.taDashboard.createProfileButton" },
            "Save changes": { key: "portal.taDashboard.saveChangesButton" },
            "Edit profile": { key: "portal.taDashboard.editProfileButton" },
            "Cancel": { key: "portal.taDashboard.cancelButton" },
            "You can continue to enrich this profile later in the next planned steps.": { key: "portal.taDashboard.profileHint" },
            "Photo upload": { key: "portal.taDashboard.photoUploadTitle" },
            "Upload your photo": { key: "portal.taDashboard.photoCardEmptyTitle" },
            "JPG, PNG, or WEBP. Maximum size is 5MB.": { key: "portal.taDashboard.photoCardEmptyHint" },
            "Remove photo": { key: "portal.taDashboard.photoRemoveAria" },
            "Resume upload": { key: "portal.taDashboard.resumeUploadTitle" },
            "Upload one PDF, DOC, or DOCX resume. Maximum size is 10MB.": { key: "portal.taDashboard.resumeUploadLead" },
            "Upload your resume in PDF, DOC, or DOCX format. Maximum size is 10MB.": { key: "portal.taDashboard.resumeUploadLead" },
            "Choose file": { key: "portal.taDashboard.chooseFile" },
            "No file selected.": { key: "portal.taDashboard.noFileSelected" },
            "Waiting to upload": { key: "portal.taDashboard.waitingUpload" },
            "Save together": { key: "portal.taDashboard.resumeDraftTitle" },
            "Create profile first": { key: "portal.taDashboard.createProfileFirst" },
            "You can upload the resume first or fill the other fields first. The newest file takes effect after you save changes.": { key: "portal.taDashboard.resumeTip" },
            "If you choose a file before profile creation, it will upload automatically right after the profile is created.": { key: "portal.taDashboard.resumeTip" },
            "Upload selected resume": { key: "portal.taDashboard.uploadSelectedResume" },

            "Browse and apply for open TA positions.": { key: "portal.taJobList.subtitle" },
            "Keyword": { key: "portal.common.keyword" },
            "All": { key: "portal.common.all" },
            "Open": { key: "portal.common.open" },
            "Closed": { key: "portal.common.closed" },
            "Filled": { key: "portal.common.filled" },
            "Course code": { key: "portal.common.courseCode" },
            "Apply filters": { key: "portal.common.applyFilters" },
            "Clear": { key: "portal.common.clear" },
            "Loading...": { key: "portal.common.loading" },
            "Loading positions...": { key: "portal.taJobList.loadingPositions" },
            "Refresh": { key: "portal.common.refresh" },

            "Job Detail": { key: "portal.taJobDetail.title" },
            "Review role requirements and submit your application.": { key: "portal.taJobDetail.subtitle" },
            "Loading job details...": { key: "portal.taJobDetail.loadingDetails" },
            "OPEN": { key: "portal.common.openUpper" },
            "Module organizer": { key: "portal.taJobDetail.moduleOrganizer" },
            "Positions": { key: "portal.common.positions" },
            "Workload": { key: "portal.common.workload" },
            "Salary": { key: "portal.common.salary" },
            "Deadline": { key: "portal.common.deadline" },
            "Description": { key: "portal.common.description" },
            "Required skills": { key: "portal.common.requiredSkills" },
            "Application": { key: "portal.common.application" },
            "Submit your application": { key: "portal.taJobDetail.submitApplicationTitle" },
            "Add a short cover letter to highlight your fit for this role.": { key: "portal.taJobDetail.coverLetterHint" },
            "Cover letter": { key: "portal.taJobDetail.coverLetter" },
            "Apply for this job": { key: "portal.taJobDetail.applyNow" },
            "Only TA accounts can submit applications. If you have already applied, this panel will show your latest status.": { key: "portal.taJobDetail.onlyTaHint" },

            "My Applications": { key: "portal.taApplicationStatus.title" },
            "Track the status of your submitted applications.": { key: "portal.taApplicationStatus.subtitle" },
            "Pending": { key: "portal.common.pending" },
            "Accepted": { key: "portal.common.accepted" },
            "Rejected": { key: "portal.common.rejected" },
            "Withdrawn": { key: "portal.common.withdrawn" },
            "Total": { key: "portal.common.total" },
            "Loading applications...": { key: "portal.taApplicationStatus.loadingApplications" },

            "Application detail": { key: "portal.taApplicationDetail.title" },
            "< Back to My applications": { key: "portal.taApplicationDetail.backToList" },
            "Applied position details": { key: "portal.taApplicationDetail.jobTeaserTitle" },
            "View details ->": { key: "portal.taApplicationDetail.viewDetailsCta" },
            "Out of 4.0": { key: "portal.taApplicationDetail.gpaScale" },
            "Application progress": { key: "portal.taApplicationDetail.progressTitle" },
            "My skills": { key: "portal.taApplicationDetail.mySkills" },
            "Responsibilities": { key: "portal.taApplicationDetail.responsibilities" },
            "View file": { key: "portal.taApplicationDetail.viewResumeFile" },
            "Resume": { key: "portal.taApplicationDetail.resumeShort" },
            "Close": { key: "portal.taApplicationDetail.closeModal" },

            "Post New Job": { key: "portal.moDashboard.title" },
            "Create a new TA position listing for your course.": { key: "portal.moDashboard.subtitle" },
            "Create posting": { key: "portal.moDashboard.createPosting" },
            "Post a new TA position": { key: "portal.moDashboard.postPosition" },
            "Fields marked with * are required for publishing.": { key: "portal.moDashboard.requiredLead" },
            "Fields labeled Required are required for publishing.": { key: "portal.moDashboard.requiredLead" },
            "Job title": { key: "portal.moDashboard.jobTitle" },
            "Job title *": { key: "portal.moDashboard.jobTitleRequired" },
            "Course code *": { key: "portal.moDashboard.courseCodeRequired" },
            "Course name": { key: "portal.moDashboard.courseName" },
            "Application deadline": { key: "portal.moDashboard.applicationDeadline" },
            "Publish job": { key: "portal.moDashboard.publishJob" },
            "Reset form": { key: "portal.moDashboard.resetForm" },
            "My postings": { key: "portal.moDashboard.myPostings" },
            "Published jobs": { key: "portal.moDashboard.publishedJobs" },
            "Loading your jobs...": { key: "portal.moDashboard.loadingJobs" },
            "My Postings": { key: "portal.moDashboard.myJobs" },
            "Post New Job": { key: "portal.moDashboard.postNew" },
            "View and manage your job postings.": { key: "portal.moDashboard.myJobsDesc" },
            "No job postings yet": { key: "portal.moDashboard.noJobsTitle" },
            "Click \"Post New Job\" to create your first TA position listing.": { key: "portal.moDashboard.noJobsDesc" },
            "Edit Job": { key: "portal.moDashboard.editJob" },
            "Confirm Delete": { key: "portal.moDashboard.confirmDelete" },
            "Are you sure you want to delete this job posting?": { key: "portal.moDashboard.deleteConfirmMsg" },

            "Save Changes": { key: "portal.action.save" },
            "Cancel": { key: "portal.action.cancel" },
            "Delete": { key: "portal.action.delete" },

            "Review and manage all candidate applications.": { key: "portal.moApplicantSelection.subtitle" },
            "Job": { key: "portal.moApplicantSelection.job" },
            "All jobs": { key: "portal.moApplicantSelection.allJobs" },
            "Applicant profile": { key: "portal.moApplicantSelection.applicantProfile" },
            "Select an applicant": { key: "portal.moApplicantSelection.selectApplicant" },
            "View resume": { key: "portal.moApplicantSelection.viewResume" },
            "Academic": { key: "portal.moApplicantSelection.academic" },
            "Contact": { key: "portal.moApplicantSelection.contact" },
            "Email": { key: "portal.moApplicantSelection.email" },
            "Phone": { key: "portal.moApplicantSelection.phone" },
            "Experience": { key: "portal.moApplicantSelection.experience" },

            "Select a job": { key: "portal.common.selectJob" },
            "High": { key: "portal.common.high" },
            "Medium": { key: "portal.common.medium" },
            "Low": { key: "portal.common.low" },

            "TA Workload": { key: "portal.adminDashboard.title" },
            "Track application volume and module owner review workload in one place.": { key: "portal.adminDashboard.subtitle" },
            "Start": { key: "portal.adminDashboard.start" },
            "End": { key: "portal.adminDashboard.end" },
            "Apply range": { key: "portal.adminDashboard.applyRange" },
            "Export CSV": { key: "portal.adminDashboard.exportCsv" },
            "Application Status Distribution": { key: "portal.adminDashboard.applicationStatusDistribution" },
            "Breakdown by review status in current range.": { key: "portal.adminDashboard.applicationStatusLead" },
            "MO Workload Overview": { key: "portal.adminDashboard.moWorkloadOverview" },
            "Workload intensity by module owner.": { key: "portal.adminDashboard.moWorkloadLead" },
            "MO Workload": { key: "portal.adminDashboard.moWorkload" },
            "Loading workload...": { key: "portal.adminDashboard.loadingWorkload" },

            "Checking profile...": { key: "portal.dynamic.checkingProfile" },
            "Creating profile...": { key: "portal.dynamic.creatingProfile" },
            "A profile already exists for this account. Loading your saved profile...": { key: "portal.dynamic.profileAlreadyExists" },
            "Please fix the highlighted fields and try again.": { key: "portal.dynamic.fixHighlightedFields" },
            "No profile found yet. Please complete the form below.": { key: "portal.dynamic.noProfileFound" },
            "Unable to check your existing profile right now. You can still try creating one.": { key: "portal.dynamic.unableCheckProfile" },
            "Unable to create your profile. Please review the form and try again.": { key: "portal.dynamic.unableCreateProfile" },
            "Profile created. Uploading your selected resume...": { key: "portal.dynamic.profileCreatedUploadingResume" },
            "Profile created, but resume upload failed. Please try uploading again.": { key: "portal.dynamic.profileCreatedResumeFailed" },
            "Profile created successfully. Your saved information is now displayed below.": { key: "portal.dynamic.profileCreatedSuccess" },
            "Your profile has already been created and is now shown in read-only mode.": { key: "portal.dynamic.profileReadonly" },
            "Current uploaded resume:": { key: "portal.dynamic.currentResumePrefix" },
            "No resume uploaded yet.": { key: "portal.dynamic.noResumeUploaded" },
            "No resume file selected.": { key: "portal.dynamic.noResumeSelected" },
            "Please choose a resume file first.": { key: "portal.dynamic.chooseResumeFirst" },
            "Please choose a photo file first.": { key: "portal.dynamic.choosePhotoFirst" },
            "Please upload your resume before saving your profile.": { key: "portal.dynamic.resumeRequiredToSave" },
            "Photo selected. Save changes to apply it.": { key: "portal.dynamic.photoReadyToSave" },
            "Current photo removed. Save changes to apply it.": { key: "portal.dynamic.savedPhotoRemoved" },
            "Photo ready": { key: "portal.dynamic.photoReady" },
            "Uploading resume draft:": { key: "portal.dynamic.resumeDraftUploading" },
            "Resume draft uploaded. Save changes to apply it.": { key: "portal.dynamic.resumeDraftSaved" },
            "New resume uploaded. Save changes to replace the current resume.": { key: "portal.dynamic.resumeDraftReplaceSaved" },
            "Pending resume:": { key: "portal.dynamic.pendingResumePrefix" },
            " It will be saved when you create the profile.": { key: "portal.dynamic.pendingResumeCreateSuffix" },
            " It will replace your current resume after you save.": { key: "portal.dynamic.pendingResumeReplaceSuffix" },
            "Unable to discard the pending resume. Please try again.": { key: "portal.dynamic.resumeDiscardFailed" },
            "Please create your profile first, then upload the resume.": { key: "portal.dynamic.createProfileThenUpload" },
            "Please create your profile first. The selected resume will also upload automatically after creation.": { key: "portal.dynamic.createProfileAutoUpload" },
            "Resume file is ready and will upload right after profile creation.": { key: "portal.dynamic.resumeReadyAfterCreate" },
            "Resume file is ready. Click upload to replace your current resume.": { key: "portal.dynamic.resumeReadyReplace" },
            "Uploading": { key: "portal.dynamic.uploading" },
            "Upload completed": { key: "portal.dynamic.uploadCompleted" },
            "Upload aborted.": { key: "portal.dynamic.uploadAborted" },
            "Upload was interrupted. Please try again.": { key: "portal.dynamic.uploadInterrupted" },
            "Network error during file upload. Please try again.": { key: "portal.dynamic.uploadNetworkError" },
            "Resume uploaded successfully.": { key: "portal.dynamic.resumeUploadSuccess" },
            "Resume updated successfully.": { key: "portal.dynamic.resumeUpdateSuccess" },
            "Resume upload failed. Please try again.": { key: "portal.dynamic.resumeUploadFailed" },
            "Invalid file format. Please upload a PDF, DOC, or DOCX file.": { key: "portal.dynamic.invalidResumeFormat" },
            "File size exceeds 10MB. Please choose a smaller file.": { key: "portal.dynamic.resumeTooLarge" },
            "Invalid photo format. Please upload JPG, PNG, or WEBP.": { key: "portal.dynamic.invalidPhotoFormat" },
            "Photo size exceeds 5MB. Please choose a smaller file.": { key: "portal.dynamic.photoTooLarge" },
            "No specific skills listed.": { key: "portal.dynamic.noSpecificSkills" },

            "Unable to load jobs right now.": { key: "portal.dynamic.unableLoadJobs" },
            "Unable to load jobs right now. Please try again.": { key: "portal.dynamic.unableLoadJobsRetry" },
            "No jobs found for the current filters.": { key: "portal.dynamic.noJobsForFilters" },
            "No jobs available right now.": { key: "portal.dynamic.noJobsAvailable" },
            "Showing": { key: "portal.dynamic.showing" },
            "job": { key: "portal.dynamic.jobUnit" },
            "Unable to load positions": { key: "portal.dynamic.unableLoadPositionsTitle" },
            "Please refresh the list after checking your network connection.": { key: "portal.dynamic.refreshAfterNetworkCheck" },
            "No positions published yet": { key: "portal.dynamic.noPositionsPublishedTitle" },
            "When MO publishes new jobs, they will appear here.": { key: "portal.dynamic.positionsAppearAfterPublish" },
            "No matching positions": { key: "portal.dynamic.noMatchingPositionsTitle" },
            "Try broadening your keyword or clearing one filter.": { key: "portal.dynamic.broadenKeywordHint" },
            "No extra tags": { key: "portal.dynamic.noExtraTags" },
            "View details": { key: "portal.dynamic.viewDetails" },
            "Apply now": { key: "portal.dynamic.applyNow" },
            "Salary": { key: "portal.common.salary" },
            "Workload": { key: "portal.common.workload" },
            "Skills": { key: "portal.taDashboard.skills" },
            "MO": { key: "portal.dynamic.moShort" },

            "Submitting...": { key: "portal.dynamic.submitting" },
            "Application has been submitted.": { key: "portal.dynamic.applicationSubmitted" },
            "Application submitted successfully. Redirecting to application status...": { key: "portal.dynamic.applicationSubmittedRedirect" },
            "Failed to submit application. Please try again.": { key: "portal.dynamic.failedSubmitApplication" },
            "Current account cannot submit applications on this page.": { key: "portal.dynamic.currentAccountCannotSubmit" },
            "Only TA accounts can submit applications.": { key: "portal.dynamic.onlyTaSubmit" },
            "You have already applied for this job.": { key: "portal.dynamic.alreadyApplied" },
            "This job is no longer available.": { key: "portal.dynamic.jobNoLongerAvailable" },
            "This job is not accepting new applications.": { key: "portal.dynamic.jobNotAccepting" },
            "This position is currently": { key: "portal.dynamic.positionCurrently" },
            ". New applications are disabled.": { key: "portal.dynamic.newApplicationsDisabled" },
            "Job not found. It may have been removed.": { key: "portal.dynamic.jobNotFound" },
            "Application unavailable": { key: "portal.dynamic.applicationUnavailable" },
            "Application status:": { key: "portal.dynamic.applicationStatusPrefix" },
            "Network error while submitting application.": { key: "portal.dynamic.networkErrorSubmitApplication" },

            "This page is available for TA accounts only.": { key: "portal.dynamic.taOnlyPage" },
            "Unable to load your applications.": { key: "portal.dynamic.unableLoadApplications" },
            "Unable to load applications right now.": { key: "portal.dynamic.unableLoadApplicationsNow" },
            "No applications submitted yet.": { key: "portal.dynamic.noApplicationsSubmitted" },
            "No applications match the current filters.": { key: "portal.dynamic.noApplicationsMatchFilters" },
            "application": { key: "portal.dynamic.applicationUnit" },
            "Unable to load applications": { key: "portal.dynamic.unableLoadApplicationsTitle" },
            "No matching applications": { key: "portal.dynamic.noMatchingApplicationsTitle" },
            "No applications yet": { key: "portal.dynamic.noApplicationsYetTitle" },
            "After you apply for a job, the status will appear here.": { key: "portal.dynamic.statusAppearsAfterApply" },
            "Try clearing status or keyword filters to broaden results.": { key: "portal.dynamic.clearFiltersToBroaden" },
            "Application withdrawn successfully.": { key: "portal.dynamic.applicationWithdrawnSuccess" },
            "Unable to withdraw this application.": { key: "portal.dynamic.unableWithdrawApplication" },
            "Network error while withdrawing application.": { key: "portal.dynamic.networkErrorWithdrawApplication" },
            "Applied at": { key: "portal.dynamic.appliedAt" },
            "Cover letter:": { key: "portal.dynamic.coverLetterColon" },
            "No cover letter provided.": { key: "portal.dynamic.noCoverLetterProvided" },
            "View job": { key: "portal.dynamic.viewJob" },
            "Withdraw": { key: "portal.dynamic.withdraw" },

            "Only MO accounts can publish jobs.": { key: "portal.dynamic.onlyMoPublish" },
            "Failed to publish job. Please check your input and try again.": { key: "portal.dynamic.failedPublishJob" },
            "Job posted successfully.": { key: "portal.dynamic.jobPostedSuccess" },
            "Network error while posting job.": { key: "portal.dynamic.networkErrorPostingJob" },
            "Unable to load postings right now.": { key: "portal.dynamic.unableLoadPostings" },
            "No jobs posted yet.": { key: "portal.dynamic.noJobsPostedYet" },
            "You have posted": { key: "portal.dynamic.youHavePosted" },
            "No postings yet": { key: "portal.dynamic.noPostingsYetTitle" },
            "Use the form to publish your first TA position.": { key: "portal.dynamic.publishFirstTaPosition" },
            "Review applicants": { key: "portal.dynamic.reviewApplicants" },
            "Untitled position": { key: "portal.dynamic.untitledPosition" },

            "Some overview data could not be loaded. Showing available results.": { key: "portal.dynamic.overviewPartialLoad" },
            "Unable to load overview data right now.": { key: "portal.dynamic.unableLoadOverview" },
            "This page is available for MO accounts only.": { key: "portal.dynamic.moOnlyPage" },
            "No activity yet.": { key: "portal.dynamic.noActivityYet" },
            "Tracking": { key: "portal.dynamic.tracking" },
            "No recent activity": { key: "portal.dynamic.noRecentActivityTitle" },
            "Once TAs apply for your jobs, latest updates will appear here.": { key: "portal.dynamic.latestUpdatesAppear" },
            "New application received": { key: "portal.dynamic.newApplicationReceived" },
            "Offer accepted": { key: "portal.dynamic.offerAccepted" },
            "Application rejected": { key: "portal.dynamic.applicationRejected" },
            "Application withdrawn": { key: "portal.dynamic.applicationWithdrawn" },
            "Application updated": { key: "portal.dynamic.applicationUpdated" },
            "Unknown applicant": { key: "portal.dynamic.unknownApplicant" },

            "Failed to load application totals.": { key: "portal.dynamic.failedLoadApplicationTotals" },
            "Failed to load MO workloads.": { key: "portal.dynamic.failedLoadMoWorkloads" },
            "Network error while loading dashboard.": { key: "portal.dynamic.networkErrorLoadingDashboard" },
            "Exporting...": { key: "portal.dynamic.exporting" },
            "CSV exported successfully.": { key: "portal.dynamic.csvExportedSuccess" },
            "Unable to export CSV.": { key: "portal.dynamic.unableExportCsv" },
            "No MO workload data in selected range.": { key: "portal.dynamic.noMoWorkloadSelectedRange" },
            "Loaded": { key: "portal.dynamic.loaded" },
            "MO workload item": { key: "portal.dynamic.moWorkloadItemUnit" },
            "No status data available.": { key: "portal.dynamic.noStatusData" },
            "No MO workload data available.": { key: "portal.dynamic.noMoWorkloadData" },
            "No workload data yet": { key: "portal.dynamic.noWorkloadDataYetTitle" },
            "Adjust time range or wait for application activity to appear.": { key: "portal.dynamic.adjustTimeRangeHint" },
            "Session expired. Redirecting to login...": { key: "portal.dynamic.sessionExpiredRedirect" },
            "Your session has expired. Redirecting to login...": { key: "portal.dynamic.sessionExpiredRedirect" },
            "Saving changes...": { key: "portal.dynamic.savingChanges" },
            "Profile updated successfully.": { key: "portal.dynamic.profileUpdatedSuccess" },
            "Unable to update your profile. Please review the form and try again.": { key: "portal.dynamic.unableUpdateProfile" },
            "Will upload after profile creation": { key: "portal.dynamic.resumeWillUploadAfterCreate" },
            "Replace uploaded resume": { key: "portal.dynamic.replaceUploadedResume" },
            "Network error. Please try again.": { key: "portal.dynamic.networkErrorTryAgain" },
            "Network error. Please try again in a moment.": { key: "portal.dynamic.networkErrorMoment" }
        };
    }

    var mapping = textMap();

    /*
     * Dynamic pattern mapping for runtime-composed text like quantities, upload filenames, statuses.
     */
    function dynamicPatternMap() {
        return [
            {
                regex: /^Showing (\d+) job(s?)\.$/,
                handler: function (match) {
                    var count = match[1];
                    var plural = useEnglishPluralSuffix() && count !== "1" ? "s" : "";
                    return t("portal.dynamic.showing", "Showing") + " " + count + " " +
                        t("portal.dynamic.jobUnit", "job") + plural + ".";
                }
            },
            {
                regex: /^You have posted (\d+) job(s?)\.$/,
                handler: function (match) {
                    var count = match[1];
                    var plural = useEnglishPluralSuffix() && count !== "1" ? "s" : "";
                    return t("portal.dynamic.youHavePosted", "You have posted") + " " + count + " " +
                        t("portal.dynamic.jobUnit", "job") + plural + ".";
                }
            },
            {
                regex: /^Showing (\d+) application(s?)\.$/,
                handler: function (match) {
                    var count = match[1];
                    var plural = useEnglishPluralSuffix() && count !== "1" ? "s" : "";
                    return t("portal.dynamic.showing", "Showing") + " " + count + " " +
                        t("portal.dynamic.applicationUnit", "application") + plural + ".";
                }
            },
            {
                regex: /^Tracking (\d+) application(s?)\.$/,
                handler: function (match) {
                    var count = match[1];
                    var plural = useEnglishPluralSuffix() && count !== "1" ? "s" : "";
                    return t("portal.dynamic.tracking", "Tracking") + " " + count + " " +
                        t("portal.dynamic.applicationUnit", "application") + plural + ".";
                }
            },
            {
                regex: /^Loaded (\d+) MO workload item(s?)\.$/,
                handler: function (match) {
                    var count = match[1];
                    var plural = useEnglishPluralSuffix() && count !== "1" ? "s" : "";
                    return t("portal.dynamic.loaded", "Loaded") + " " + count + " " +
                        t("portal.dynamic.moWorkloadItemUnit", "MO workload item") + plural + ".";
                }
            },
            {
                regex: /^Found (\d+) applicant(s?)$/,
                handler: function (match) {
                    return t("portal.dynamic.found", "Found") + " " + match[1] + " " +
                        t("portal.dynamic.applicantsSuffix", "applicant(s)");
                }
            },
            {
                regex: /^Current uploaded resume:\s*(.+)$/,
                handler: function (match) {
                    return t("portal.dynamic.currentResumePrefix", "Current uploaded resume:") + " " + match[1];
                }
            },
            {
                regex: /^Uploading\s+(.+)$/,
                handler: function (match) {
                    return t("portal.dynamic.uploading", "Uploading") + " " + match[1];
                }
            },
            {
                regex: /^Current completeness:\s*(\d+)%$/,
                handler: function (match) {
                    return t("portal.dynamic.currentCompleteness", "Current completeness:") + " " + match[1] + "%";
                }
            },
            {
                regex: /^This position is currently\s+([A-Z]+)\.\s+New applications are disabled\.$/,
                handler: function (match) {
                    return t("portal.dynamic.positionCurrently", "This position is currently") + " " + match[1] +
                        t("portal.dynamic.newApplicationsDisabled", ". New applications are disabled.");
                }
            }
        ];
    }

    var dynamicPatterns = dynamicPatternMap();

    /*
     * Try matching dynamic patterns.
     */
    function tryDynamicPattern(text) {
        for (var i = 0; i < dynamicPatterns.length; i += 1) {
            var pattern = dynamicPatterns[i];
            var match = text.match(pattern.regex);
            if (match) {
                return pattern.handler(match);
            }
        }
        return "";
    }

    /*
     * Translate single raw text segment.
     */
    function translateRawText(text) {
        var entry = mapping[text];
        if (entry) {
            return resolveByKey(entry.key, text);
        }
        return tryDynamicPattern(text) || "";
    }

    /*
     * Replace one text node, skipping script/style.
     */
    function replaceTextNode(node) {
        if (!node || !node.nodeValue) {
            return;
        }
        if (node.parentElement) {
            var tagName = node.parentElement.tagName;
            if (tagName === "SCRIPT" || tagName === "STYLE") {
                return;
            }
        }
        var raw = node.nodeValue;
        var text = raw.replace(/\s+/g, " ").trim();
        if (!text) {
            return;
        }
        var translated = translateRawText(text);
        if (translated && translated !== text) {
            node.nodeValue = raw.replace(text, translated);
        }
    }

    /*
     * Traverse text nodes in a DOM subtree.
     */
    function translateNodeTree(root) {
        if (!root) {
            return;
        }
        var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null);
        var node = walker.nextNode();
        while (node) {
            replaceTextNode(node);
            node = walker.nextNode();
        }
    }

    /*
     * Translate entire document body.
     */
    function translateDocument() {
        if (!hasI18n()) {
            return;
        }
        translateNodeTree(document.body);
    }

    /*
     * Observe dynamically inserted nodes to supplement translation of JS-rendered cards and prompts.
     */
    function observeMutations() {
        if (!window.MutationObserver || !document.body) {
            return;
        }
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                if (mutation.type === "childList") {
                    Array.prototype.forEach.call(mutation.addedNodes, function (node) {
                        if (node && node.nodeType === Node.ELEMENT_NODE) {
                            translateNodeTree(node);
                        } else if (node && node.nodeType === Node.TEXT_NODE) {
                            replaceTextNode(node);
                        }
                    });
                    return;
                }
                if (mutation.type === "characterData" && mutation.target) {
                    replaceTextNode(mutation.target);
                }
            });
        });
        observer.observe(document.body, {
            childList: true,
            characterData: true,
            subtree: true
        });
    }

    /*
     * Re-translate dynamic text after language switch.
     */
    function bindLocaleEvent() {
        document.addEventListener("app:locale-changed", function () {
            window.setTimeout(function () {
                translateDocument();
            }, 0);
        });
    }

    /*
     * Initialize dynamic translation patches.
     */
    function initialize() {
        translateDocument();
        observeMutations();
        bindLocaleEvent();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initialize);
    } else {
        initialize();
    }
})();

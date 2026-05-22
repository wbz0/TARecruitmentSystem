/*
 * MO dashboard script, corresponds to /jsp/mo/dashboard.jsp.
 *
 * Handles job posting/edit, my job list, application review, candidate details, and AI recommendations in one page.
 * APIs are generated through TARecruitment.routes to avoid falling back to old /jobs or /apply paths.
 */
(function () {
    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var currentUserId = typeof window.APP_CURRENT_USER_ID === "string" ? window.APP_CURRENT_USER_ID : "";
    var SKILL_SEPARATOR_PATTERN = /[,，]/;
    var UNSUPPORTED_SKILL_SEPARATOR_PATTERN = /[;；、|]/;

    /*
     * Job management main state:
     * jobs is “my postings” list data; submitting/deleting/editing control button disabled and modal state.
     */
    var state = {
        jobs: [],
        submitting: false,
        deletingJobId: null,
        editingJobId: null
    };

    /*
     * Applicant sub-view state.
     * When MO enters applicant list from a job card, list, detail, AI recommendations, and review actions all depend on this.
     */
    var svState = {
        // Selection view state: MO viewing job applications and AI recommendations from “my postings”.
        loading: false,
        jobId: "",
        jobTitle: "",
        keyword: "",
        searchMode: "search",
        aiSearchLoading: false,
        aiSearchActive: false,
        aiRecommendationsById: {},
        viewMode: "list",
        applications: [],
        detailsById: {},
        selectedApplicationId: "",
        reviewingId: ""
    };

    /*
     * Edit modal realtime validation state.
     * touched/dirty/feedback separation makes it easy to show field errors only after user interaction.
     */
    var editFieldValidationState = {
        touchedByKey: {},
        dirtyByKey: {},
        feedbackByKey: {}
    };

    // DOM Elements - Create Form
    var form = document.getElementById("job-create-form");
    var publishButton = document.getElementById("publish-btn");
    var resetButton = document.getElementById("reset-btn");
    var messageNode = document.getElementById("form-message");

    // DOM Elements - Page Panels
    var panelMyJobs = document.getElementById("panel-my-jobs");
    var panelPostJob = document.getElementById("panel-post-job");

    // DOM Elements - Job List
    var jobsLoading = document.getElementById("jobs-loading");
    var jobList = document.getElementById("job-list");
    var jobsEmpty = document.getElementById("jobs-empty");
    var jobsListMessage = document.getElementById("jobs-list-message");

    // ==========================================
    // DOM Elements - Edit Modal
    // ==========================================
    var editModal = document.getElementById("edit-job-modal");
    var editForm = document.getElementById("job-edit-form");
    var editFormMessage = document.getElementById("edit-form-message");
    var editJobId = document.getElementById("edit-job-id");
    var editSaveBtn = document.getElementById("edit-save-btn");
    var editCancelBtn = document.getElementById("edit-cancel-btn");
    var editModalClose = document.getElementById("edit-modal-close");

    var editFields = {
        title: document.getElementById("edit-job-title"),
        courseCode: document.getElementById("edit-course-code"),
        courseName: document.getElementById("edit-course-name"),
        description: document.getElementById("edit-description"),
        requiredSkills: document.getElementById("edit-required-skills"),
        positions: document.getElementById("edit-positions"),
        deadline: document.getElementById("edit-deadline"),
        weeklyHours: document.getElementById("edit-weekly-hours"),
        workStartDate: document.getElementById("edit-work-start-date"),
        workEndDate: document.getElementById("edit-work-end-date"),
        salary: document.getElementById("edit-salary"),
        status: document.getElementById("edit-status")
    };

    // ==========================================
    // DOM Elements - Delete Modal
    // ==========================================
    var deleteModal = document.getElementById("delete-job-modal");
    var deleteJobTitle = document.getElementById("delete-job-title");
    var deleteConfirmBtn = document.getElementById("delete-confirm-btn");
    var deleteCancelBtn = document.getElementById("delete-cancel-btn");
    var deleteModalClose = document.getElementById("delete-modal-close");

    // ==========================================
    // DOM Elements - Applicant Sub-View
    // ==========================================
    var panelApplicants = document.getElementById("panel-applicants");
    var subviewBackBtn = document.getElementById("subview-back-btn");
    var subviewJobTitle = document.getElementById("subview-job-title");
    var subviewSearchForm = document.getElementById("subview-search-form");
    var subviewSearchInput = document.getElementById("subview-search-input");
    var subviewSearchBtn = document.getElementById("subview-search-btn");
    var subviewAiSearchBtn = document.getElementById("subview-ai-search-btn");
    var subviewMessage = document.getElementById("subview-message");
    var subviewListSummary = document.getElementById("subview-list-summary");
    var subviewList = document.getElementById("subview-list");

    // ==========================================
    // Create Form Fields
    // ==========================================
    var createFields = {
        title: document.getElementById("job-title"),
        courseCode: document.getElementById("course-code"),
        courseName: document.getElementById("course-name"),
        description: document.getElementById("description"),
        requiredSkills: document.getElementById("required-skills"),
        positions: document.getElementById("positions"),
        deadline: document.getElementById("deadline"),
        weeklyHours: document.getElementById("weekly-hours"),
        workStartDate: document.getElementById("work-start-date"),
        workEndDate: document.getElementById("work-end-date"),
        salary: document.getElementById("salary")
    };

    var orderedFieldKeys = [
        "title",
        "courseCode",
        "courseName",
        "description",
        "requiredSkills",
        "positions",
        "deadline",
        "weeklyHours",
        "workStartDate",
        "workEndDate",
        "salary"
    ];

    var createFieldValidationState = {
        touchedByKey: {},
        dirtyByKey: {},
        feedbackByKey: {}
    };

    // ==========================================
    // Initialization
    // ==========================================
    function init() {
        if (!form || !publishButton) {
            return;
        }

        initializeRealtimeValidation();
        initializeEnterKeyNavigation();
        initJobList();
        initEditModal();
        initDeleteModal();
        initApplicantSubView();

        // Create form submit
        form.addEventListener("submit", function (event) {
            event.preventDefault();
            submitCreate();
        });

        // Create form reset
        form.addEventListener("reset", function () {
            hideMessage();
            clearAllFieldValidation();
            resetFieldTouchedState();
        });

        document.addEventListener("app:locale-changed", function () {
            renderJobList();
            if (panelApplicants && !panelApplicants.classList.contains("hidden")) {
                if (svState.viewMode === "detail") {
                    renderSvDetail(svState.selectedApplicationId);
                } else {
                    renderSvList();
                }
                updateSvSearchControls();
            }
            orderedFieldKeys.forEach(function (key) {
                if (createFieldValidationState.touchedByKey[key] === true) {
                    validateSingleField(key, { forceRequired: true });
                }
            });
            if (!state.submitting) {
                setSubmitting(false);
                setDeleteSubmitting(false);
                setEditSubmitting(false);
            }
        });
    }

    // ==========================================
    // Enter Key Navigation
    // ==========================================
    function initializeEnterKeyNavigation() {
        form.addEventListener("keydown", function (event) {
            if (!event || event.key !== "Enter" || event.isComposing) return;
            var target = event.target;
            if (!target || target.form !== form) return;
            if (target.tagName === "TEXTAREA") return;
            if (target.tagName === "BUTTON") return;
            event.preventDefault();
            focusNextField(target);
        });
    }

    function focusNextField(current) {
        var fields = Array.prototype.slice.call(
            form.querySelectorAll("input:not([disabled]):not([type='hidden']), textarea:not([disabled])")
        );
        var idx = fields.indexOf(current);
        if (idx >= 0 && idx < fields.length - 1) {
            fields[idx + 1].focus();
        }
    }

    function switchTab(tabId) {
        if (panelApplicants) panelApplicants.classList.add("hidden");

        if (tabId === "my-jobs") {
            panelMyJobs.classList.add("is-active");
            panelMyJobs.hidden = false;
            panelPostJob.classList.remove("is-active");
            panelPostJob.hidden = true;
            loadJobs();
        } else {
            panelPostJob.classList.add("is-active");
            panelPostJob.hidden = false;
            panelMyJobs.classList.remove("is-active");
            panelMyJobs.hidden = true;
        }
    }

    // ==========================================
    // Job List
    // ==========================================
    function initJobList() {
        loadJobs();
    }

    function loadJobs() {
        if (!currentUserId) {
            showJobsMessage(t("portal.moDashboard.userNotLoggedIn", "User not logged in."), "error");
            return;
        }

        showJobsLoading();

        request(window.TARecruitment.routes.jobs.list({ moId: currentUserId }), {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    showJobsMessage(t("portal.moDashboard.failedLoadJobs", "Failed to load jobs."), "error");
                    return;
                }

                state.jobs = payload.data && payload.data.jobs ? payload.data.jobs : [];
                renderJobList();
            })
            .catch(function () {
                showJobsMessage(t("portal.dynamic.networkErrorTryAgain", "Network error. Please try again."), "error");
            });
    }

    function showJobsLoading() {
        hideElement(jobsEmpty);
        hideElement(jobList);
        showElement(jobsLoading);
        hideJobsMessage();
    }

    function renderJobList() {
        hideElement(jobsLoading);

        if (state.jobs.length === 0) {
            showElement(jobsEmpty);
            hideElement(jobList);
            return;
        }

        hideElement(jobsEmpty);
        showElement(jobList);
        jobList.innerHTML = "";

        state.jobs.forEach(function (job) {
            var card = createJobCard(job);
            jobList.appendChild(card);
        });
    }

    function createJobCard(job) {
        var card = document.createElement("div");
        var status = String(job.status || "unknown").toUpperCase();
        var statusClass = status.toLowerCase();
        var applicationCount = getApplicationCount(job);
        card.className = "mo-job-card status-" + statusClass;
        card.setAttribute("role", "listitem");

        var formattedDeadline = formatDeadline(job.deadline);

        card.innerHTML =
            '<div class="job-accent"></div>' +
            '<div class="job-main">' +
            '<div class="job-heading">' +
            '<h3>' + escapeHtml(job.title || "") + '</h3>' +
            '</div>' +
            '<p class="job-subtitle">' +
            '<span class="job-course-code">' + escapeHtml(job.courseCode || "") + '</span>' +
            '<span> - </span>' +
            '<span>' + escapeHtml(job.courseName || "") + '</span>' +
            '</p>' +
            '<p class="job-meta-line">' +
            '<span>' + escapeHtml(t("portal.common.positions", "Positions") + " " + (job.positions || "0")) + '</span>' +
            '<span class="job-meta-separator">|</span>' +
            '<span>' + escapeHtml(applicationCount + " " + t("portal.dynamic.applicationUnit", "application")) + '</span>' +
            '<span class="job-meta-separator">|</span>' +
            '<span>' + escapeHtml(formattedDeadline) + '</span>' +
            '</p>' +
            '</div>' +
            '<div class="job-actions">' +
            '<span class="job-status-chip status-' + escapeHtml(statusClass) + '">' + escapeHtml(getJobStatusLabel(status)) + '</span>' +
            '<button class="edit-btn" type="button" data-job-id="' + escapeHtml(job.jobId || "") + '">' + escapeHtml(t("portal.action.edit", "Edit")) + '</button>' +
            '<button class="delete-btn" type="button" data-job-id="' + escapeHtml(job.jobId || "") + '">' + escapeHtml(t("portal.action.delete", "Delete")) + '</button>' +
            '</div>';

        // Attach event listeners
        var editBtn = card.querySelector(".edit-btn");
        var deleteBtn = card.querySelector(".delete-btn");

        if (editBtn) {
            editBtn.addEventListener("click", function (e) {
                e.stopPropagation();
                openEditModal(job.jobId);
            });
        }

        if (deleteBtn) {
            deleteBtn.addEventListener("click", function (e) {
                e.stopPropagation();
                openDeleteModal(job.jobId, job.title);
            });
        }

        card.style.cursor = "pointer";
        card.setAttribute("tabindex", "0");
        card.addEventListener("click", function () {
            openApplicantSubView(job.jobId, job.title || job.courseCode || "");
        });
        card.addEventListener("keydown", function (event) {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                openApplicantSubView(job.jobId, job.title || job.courseCode || "");
            }
        });

        return card;
    }

    function getApplicationCount(job) {
        if (job.applicationCount != null) {
            return job.applicationCount;
        }
        if (job.applicationsCount != null) {
            return job.applicationsCount;
        }
        if (job.applicantCount != null) {
            return job.applicantCount;
        }
        return 0;
    }

    function getJobStatusLabel(status) {
        if (status === "OPEN") {
            return t("portal.common.open", "Open");
        }
        if (status === "CLOSED") {
            return t("portal.common.closed", "Closed");
        }
        if (status === "FILLED") {
            return t("portal.common.filled", "Filled");
        }
        return status || "-";
    }

    function t(key, fallback) {
        if (window.AppI18n && typeof window.AppI18n.t === "function") {
            return window.AppI18n.t(key, fallback || key);
        }
        return fallback || key;
    }

    function localizeServerMessage(message, fallbackKey, fallbackText) {
        if (window.AppI18n && typeof window.AppI18n.localizeServerMessage === "function") {
            return window.AppI18n.localizeServerMessage(message, fallbackKey, fallbackText);
        }
        if (typeof message === "string" && message.trim()) {
            return message.trim();
        }
        return fallbackKey ? t(fallbackKey, fallbackText) : (fallbackText || "");
    }

    // ==========================================
    // Edit Modal
    // ==========================================
    function initEditModal() {
        if (!editModal) return;

        // Close button
        if (editModalClose) {
            editModalClose.addEventListener("click", closeEditModal);
        }

        // Cancel button
        if (editCancelBtn) {
            editCancelBtn.addEventListener("click", closeEditModal);
        }

        // Form submit
        if (editForm) {
            editForm.addEventListener("submit", function (event) {
                event.preventDefault();
                submitEdit();
            });
        }

        // Close on overlay click
        editModal.addEventListener("click", function (event) {
            if (event.target === editModal) {
                closeEditModal();
            }
        });

        // Close on Escape
        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape" && !editModal.classList.contains("hidden")) {
                closeEditModal();
            }
        });

        initializeEditRealtimeValidation();
        initializeEditEnterKeyNavigation();
    }

    function openEditModal(jobId) {
        var job = findJobById(jobId);
        if (!job) {
            showEditMessage(t("portal.moDashboard.jobNotFound", "Job not found."), "error");
            return;
        }

        state.editingJobId = jobId;

        // Fill form fields
        editJobId.value = job.jobId || "";
        editFields.title.value = job.title || "";
        editFields.courseCode.value = job.courseCode || "";
        editFields.courseName.value = job.courseName || "";
        editFields.description.value = job.description || "";
        editFields.requiredSkills.value = job.requiredSkills || "";
        editFields.positions.value = job.positions || "1";
        editFields.weeklyHours.value = job.weeklyHours != null ? String(job.weeklyHours) : "";
        editFields.workStartDate.value = job.workStartDate || "";
        editFields.workEndDate.value = job.workEndDate || "";
        editFields.salary.value = job.salary || "";
        editFields.status.value = job.status || "OPEN";

        // Format deadline for datetime-local input
        if (job.deadline) {
            editFields.deadline.value = formatDeadlineForInput(job.deadline);
        }

        hideEditMessage();

        // Reset validation state
        editFieldValidationState.touchedByKey = {};
        editFieldValidationState.dirtyByKey = {};
        orderedFieldKeys.forEach(function (key) {
            setEditFieldValidationResult(key, "");
        });

        showEditModal();
    }

    function showEditModal() {
        editModal.classList.remove("hidden");
        editModal.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
    }

    function closeEditModal() {
        editModal.classList.add("hidden");
        editModal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
        state.editingJobId = null;
        hideEditMessage();
    }

    function submitEdit() {
        if (state.submitting) return;
        if (!state.editingJobId) return;

        var validationResult = validateEditForm();
        if (validationResult && validationResult.message) {
            showEditMessage(validationResult.message, "error");
            shakeInvalidFields(editForm);
            if (validationResult.field) {
                validationResult.field.scrollIntoView({ behavior: "smooth", block: "center" });
                validationResult.field.focus({ preventScroll: true });
            }
            return;
        }

        var formData = new URLSearchParams();
        formData.set("title", editFields.title.value.trim());
        formData.set("courseCode", editFields.courseCode.value.trim());
        formData.set("courseName", editFields.courseName.value.trim());
        formData.set("description", editFields.description.value.trim());
        formData.set("requiredSkills", normalizeSkillsForSubmit(editFields.requiredSkills.value));
        formData.set("positions", editFields.positions.value.trim());
        formData.set("weeklyHours", editFields.weeklyHours.value.trim());
        formData.set("workStartDate", editFields.workStartDate.value.trim());
        formData.set("workEndDate", editFields.workEndDate.value.trim());
        formData.set("salary", editFields.salary.value.trim());
        formData.set("status", editFields.status.value.trim());

        var deadlineValue = normalizeDeadline(editFields.deadline.value);
        if (deadlineValue) {
            formData.set("deadline", deadlineValue);
        }

        setEditSubmitting(true);

        request(window.TARecruitment.routes.jobs.detail(state.editingJobId), {
            method: "PUT",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = t("portal.moDashboard.failedUpdateJob", "Failed to update job.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.moDashboard.failedUpdateJob", errorMessage);
                    }
                    showEditMessage(errorMessage, "error");
                    return;
                }

                closeEditModal();
                showJobsMessage(t("portal.moDashboard.jobUpdatedSuccess", "Job updated successfully."), "success");
                loadJobs();
            })
            .catch(function () {
                showEditMessage(t("portal.dynamic.networkErrorTryAgain", "Network error. Please try again."), "error");
            })
            .finally(function () {
                setEditSubmitting(false);
            });
    }

    function setEditSubmitting(submitting) {
        state.submitting = submitting;
        editSaveBtn.disabled = submitting;
        editSaveBtn.textContent = submitting ? t("portal.dynamic.savingChanges", "Saving changes...") : t("portal.action.save", "Save Changes");
    }

    function showEditMessage(message, type) {
        if (!editFormMessage) return;
        editFormMessage.textContent = message;
        editFormMessage.classList.remove("hidden", "error", "success");
        editFormMessage.classList.add(type);
    }

    function hideEditMessage() {
        if (!editFormMessage) return;
        editFormMessage.textContent = "";
        editFormMessage.classList.remove("error", "success");
        editFormMessage.classList.add("hidden");
    }

    // ==========================================
    // Delete Modal
    // ==========================================
    function initDeleteModal() {
        if (!deleteModal) return;

        // Close button
        if (deleteModalClose) {
            deleteModalClose.addEventListener("click", closeDeleteModal);
        }

        // Cancel button
        if (deleteCancelBtn) {
            deleteCancelBtn.addEventListener("click", closeDeleteModal);
        }

        // Confirm button
        if (deleteConfirmBtn) {
            deleteConfirmBtn.addEventListener("click", confirmDelete);
        }

        // Close on overlay click
        deleteModal.addEventListener("click", function (event) {
            if (event.target === deleteModal) {
                closeDeleteModal();
            }
        });

        // Close on Escape
        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape" && !deleteModal.classList.contains("hidden")) {
                closeDeleteModal();
            }
        });
    }

    function openDeleteModal(jobId, jobTitle) {
        state.deletingJobId = jobId;
        deleteJobTitle.textContent = jobTitle || "";
        showDeleteModal();
    }

    function showDeleteModal() {
        deleteModal.classList.remove("hidden");
        deleteModal.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
    }

    function closeDeleteModal() {
        deleteModal.classList.add("hidden");
        deleteModal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
        state.deletingJobId = null;
    }

    function confirmDelete() {
        if (!state.deletingJobId) return;
        if (state.submitting) return;

        setDeleteSubmitting(true);

        request(window.TARecruitment.routes.jobs.detail(state.deletingJobId), {
            method: "DELETE",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = t("portal.moDashboard.failedDeleteJob", "Failed to delete job.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.moDashboard.failedDeleteJob", errorMessage);
                    }
                    showJobsMessage(errorMessage, "error");
                    closeDeleteModal();
                    return;
                }

                closeDeleteModal();
                showJobsMessage(t("portal.moDashboard.jobDeletedSuccess", "Job deleted successfully."), "success");
                loadJobs();
            })
            .catch(function () {
                showJobsMessage(t("portal.dynamic.networkErrorTryAgain", "Network error. Please try again."), "error");
                closeDeleteModal();
            })
            .finally(function () {
                setDeleteSubmitting(false);
            });
    }

    function setDeleteSubmitting(submitting) {
        state.submitting = submitting;
        deleteConfirmBtn.disabled = submitting;
        deleteConfirmBtn.textContent = submitting ? t("portal.moDashboard.deleting", "Deleting...") : t("portal.action.delete", "Delete");
    }

    // ==========================================
    // Job List Messages
    // ==========================================
    function showJobsMessage(message, type) {
        if (!jobsListMessage) return;
        jobsListMessage.textContent = message;
        jobsListMessage.classList.remove("hidden", "error", "success");
        jobsListMessage.classList.add(type);
    }

    function hideJobsMessage() {
        if (!jobsListMessage) return;
        jobsListMessage.textContent = "";
        jobsListMessage.classList.remove("error", "success");
        jobsListMessage.classList.add("hidden");
    }

    // ==========================================
    // Create Form (Existing functionality)
    // ==========================================
    function submitCreate() {
        if (state.submitting) {
            return;
        }

        hideMessage();

        var validationResult = validateForm();
        if (validationResult && validationResult.message) {
            showMessage(validationResult.message, "error");
            shakeInvalidFields(form);
            if (validationResult.field) {
                validationResult.field.scrollIntoView({ behavior: "smooth", block: "center" });
                validationResult.field.focus({ preventScroll: true });
            }
            return;
        }

        var formData = new URLSearchParams();
        formData.set("title", createFields.title.value.trim());
        formData.set("courseCode", createFields.courseCode.value.trim());
        formData.set("courseName", createFields.courseName.value.trim());
        formData.set("description", createFields.description.value.trim());
        formData.set("requiredSkills", normalizeSkillsForSubmit(createFields.requiredSkills.value));
        formData.set("positions", createFields.positions.value.trim());
        formData.set("weeklyHours", createFields.weeklyHours.value.trim());
        formData.set("workStartDate", createFields.workStartDate.value.trim());
        formData.set("workEndDate", createFields.workEndDate.value.trim());
        formData.set("salary", createFields.salary.value.trim());

        var deadlineValue = normalizeDeadline(createFields.deadline.value);
        if (deadlineValue) {
            formData.set("deadline", deadlineValue);
        }

        setSubmitting(true);

        request(window.TARecruitment.routes.jobs.list(), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData.toString()
        })
            .then(function (result) {
                var response = result.response;
                var payload = result.payload;

                if (response.status === 401) {
                    handleUnauthorized();
                    return;
                }

                if (response.status === 403) {
                    showMessage(t("portal.dynamic.onlyMoPublish", "Only MO accounts can publish jobs."), "error");
                    return;
                }

                if (!response.ok || !payload || payload.success !== true) {
                    var errorMessage = t("portal.dynamic.failedPublishJob", "Failed to publish job. Please check your input and try again.");
                    if (payload && typeof payload.message === "string" && payload.message.trim()) {
                        errorMessage = localizeServerMessage(payload.message, "portal.dynamic.failedPublishJob", errorMessage);
                    }
                    showMessage(errorMessage, "error");
                    return;
                }

                form.reset();
                createFields.positions.value = "1";
                showMessage(t("portal.dynamic.jobPostedSuccess", "Job posted successfully."), "success");
                // Refresh job list if visible
                if (!panelMyJobs.hidden) {
                    loadJobs();
                }
            })
            .catch(function () {
                showMessage(t("portal.dynamic.networkErrorPostingJob", "Network error while posting job."), "error");
            })
            .finally(function () {
                setSubmitting(false);
            });
    }

    // ==========================================
    // Validation (from original code)
    // ==========================================
    function validateForm() {
        var firstError = null;

        orderedFieldKeys.forEach(function (key) {
            var field = createFields[key];
            if (!field) {
                return;
            }
            createFieldValidationState.touchedByKey[key] = true;
            var result = validateSingleField(key, { forceRequired: true });
            if (!firstError && result && result.message) {
                firstError = result;
            }
        });
        if (!firstError) {
            firstError = validateWorkPeriodForFields(createFields, setFieldValidationResult);
        }

        if (!firstError) {
            return null;
        }
        return buildValidationError(firstError.message, firstError.field);
    }

    function initializeRealtimeValidation() {
        orderedFieldKeys.forEach(function (key) {
            var field = createFields[key];
            if (!field) {
                return;
            }

            createFieldValidationState.feedbackByKey[key] = ensureFieldFeedbackNode(key, field);
            createFieldValidationState.touchedByKey[key] = false;

            field.addEventListener("blur", function () {
                createFieldValidationState.touchedByKey[key] = true;
                // Only show "required" error on blur if the user has actually typed
                // something (dirty). Clicking in-and-out of an empty field stays silent.
                validateSingleField(key, {
                    forceRequired: createFieldValidationState.dirtyByKey[key] === true
                });
                if (isScheduleField(key)) {
                    validateWorkPeriodForFields(createFields, setFieldValidationResult);
                }
            });

            field.addEventListener("input", function () {
                createFieldValidationState.dirtyByKey[key] = true;
                validateSingleField(key, { forceRequired: true });
                if (isScheduleField(key)) {
                    validateWorkPeriodForFields(createFields, setFieldValidationResult);
                }
            });

            field.addEventListener("change", function () {
                createFieldValidationState.dirtyByKey[key] = true;
                validateSingleField(key, { forceRequired: true });
                if (isScheduleField(key)) {
                    validateWorkPeriodForFields(createFields, setFieldValidationResult);
                }
            });
        });
    }

    function isScheduleField(key) {
        return key === "deadline" || key === "workStartDate" || key === "workEndDate";
    }

    function validateSingleField(key, options) {
        var field = createFields[key];
        if (!field) {
            return null;
        }

        var settings = options || {};
        var value = typeof field.value === "string" ? field.value.trim() : "";
        var message = getFieldValidationMessage(key, value, settings.forceRequired === true);
        setFieldValidationResult(key, message);

        return {
            field: field,
            message: message
        };
    }

    function getFieldValidationMessage(key, value, forceRequired) {
        if (key === "title") return validateTitle(value, forceRequired);
        if (key === "courseCode") return validateCourseCode(value, forceRequired);
        if (key === "courseName") return validateCourseName(value, forceRequired);
        if (key === "description") return validateDescription(value, forceRequired);
        if (key === "requiredSkills") return validateRequiredSkills(value, forceRequired);
        if (key === "positions") return validatePositions(value, forceRequired);
        if (key === "weeklyHours") return validateWeeklyHours(value, forceRequired);
        if (key === "workStartDate") return validateWorkDate(value, forceRequired, "start");
        if (key === "workEndDate") return validateWorkDate(value, forceRequired, "end");
        if (key === "salary") return validateSalary(value, forceRequired);
        if (key === "deadline") return validateDeadline(value, forceRequired);
        return "";
    }

    function setFieldValidationResult(key, message) {
        var field = createFields[key];
        var feedback = createFieldValidationState.feedbackByKey[key];
        if (!field) {
            return;
        }

        if (feedback) {
            if (message) {
                feedback.textContent = message;
                feedback.classList.add("is-visible");
            } else {
                feedback.textContent = "";
                feedback.classList.remove("is-visible");
            }
        }

        if (message) {
            field.classList.add("is-invalid");
            field.setAttribute("aria-invalid", "true");
            return;
        }

        field.classList.remove("is-invalid");
        field.removeAttribute("aria-invalid");
    }

    function clearAllFieldValidation() {
        orderedFieldKeys.forEach(function (key) {
            setFieldValidationResult(key, "");
        });
    }

    function resetFieldTouchedState() {
        orderedFieldKeys.forEach(function (key) {
            createFieldValidationState.touchedByKey[key] = false;
            createFieldValidationState.dirtyByKey[key] = false;
        });
    }

    function ensureFieldFeedbackNode(key, field) {
        var container = field.closest(".field");
        if (!container) {
            return null;
        }

        var selector = ".field-feedback[data-for=\"" + key + "\"]";
        var feedback = container.querySelector(selector);
        if (!feedback) {
            feedback = document.createElement("p");
            feedback.className = "field-feedback";
            feedback.setAttribute("data-for", key);
            feedback.setAttribute("role", "status");
            feedback.setAttribute("aria-live", "polite");
            feedback.id = field.id ? field.id + "-feedback" : key + "-feedback";
            container.appendChild(feedback);
        }

        var describedBy = field.getAttribute("aria-describedby");
        if (!describedBy) {
            field.setAttribute("aria-describedby", feedback.id);
        } else if ((" " + describedBy + " ").indexOf(" " + feedback.id + " ") === -1) {
            field.setAttribute("aria-describedby", describedBy + " " + feedback.id);
        }

        return feedback;
    }

    // Validation functions
    function validateTitle(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationJobTitleRequired", "Job title is required.");
        if (!value) return "";
        if (value.length > 200) return t("portal.moDashboard.validationJobTitleLength", "Job title must be 200 characters or fewer.");
        if (containsControlChars(value) || containsDangerousMarkup(value)) return t("portal.moDashboard.validationJobTitleUnsupported", "Job title contains unsupported characters.");
        return "";
    }

    function validateCourseCode(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationCourseCodeRequired", "Course code is required.");
        if (!value) return "";
        if (value.length > 50) return t("portal.moDashboard.validationCourseCodeLength", "Course code must be 50 characters or fewer.");
        if (!/^[A-Za-z0-9][A-Za-z0-9_\-/.]{0,49}$/.test(value)) return t("portal.moDashboard.validationCourseCodeUnsupported", "Course code contains unsupported characters.");
        return "";
    }

    function validateCourseName(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationCourseNameRequired", "Course name is required.");
        if (!value) return "";
        if (value.length > 120) return t("portal.moDashboard.validationCourseNameLength", "Course name must be 120 characters or fewer.");
        if (containsControlChars(value) || containsDangerousMarkup(value)) return t("portal.moDashboard.validationCourseNameUnsupported", "Course name contains unsupported characters.");
        return "";
    }

    function validateDescription(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationDescriptionRequired", "Description is required.");
        if (!value) return "";
        if (value.length > 4000) return t("portal.moDashboard.validationDescriptionLength", "Description must be 4000 characters or fewer.");
        if (containsControlChars(value) || containsDangerousMarkup(value)) return t("portal.moDashboard.validationDescriptionUnsupported", "Description contains unsupported characters.");
        return "";
    }

    function validateRequiredSkills(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationSkillsRequired", "Required skills are required.");
        if (!value) return "";
        if (value.length > 500) return t("portal.moDashboard.validationSkillsLength", "Required skills must be 500 characters or fewer.");
        if (containsControlChars(value) || containsDangerousMarkup(value)) return t("portal.moDashboard.validationSkillsUnsupported", "Required skills contain unsupported characters.");
        if (UNSUPPORTED_SKILL_SEPARATOR_PATTERN.test(value)) return t("portal.moDashboard.validationSkillsCommaSeparator", "Please use English commas or Chinese commas to separate skills.");
        if (/(^[,，]|[,，]\s*[,，]|[,，]\s*$)/.test(value)) return t("portal.moDashboard.validationSkillsEmpty", "Please remove empty skill items.");
        var normalizedSkills = normalizeSkillsForSubmit(value);
        if (!normalizedSkills) return t("portal.moDashboard.validationSkillsEmpty", "Please remove empty skill items.");
        if (normalizedSkills.split(",").length > 20) return t("portal.moDashboard.validationSkillsLimit", "Please list up to 20 skills.");
        var seen = {};
        var items = normalizedSkills.split(",");
        for (var i = 0; i < items.length; i += 1) {
            var normalizedSkill = items[i].toLowerCase().replace(/\s+/g, " ");
            if (seen[normalizedSkill]) {
                return t("portal.moDashboard.validationSkillsDuplicate", "Duplicate skills found. Please keep each skill only once.");
            }
            seen[normalizedSkill] = true;
        }
        return "";
    }

    function validatePositions(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationPositionsRequired", "Positions must be a whole number.");
        if (!value) return "";
        if (!/^\d+$/.test(value)) return t("portal.moDashboard.validationPositionsNumber", "Positions must be a whole number.");
        var positions = Number(value);
        if (!isFinite(positions) || positions < 1 || positions > 200) return t("portal.moDashboard.validationPositionsRange", "Positions must be between 1 and 200.");
        return "";
    }

    function validateWeeklyHours(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationWeeklyHoursRequired", "Weekly hours are required.");
        if (!value) return "";
        if (!/^\d+(?:\.\d)?$/.test(value)) return t("portal.moDashboard.validationWeeklyHoursNumber", "Weekly hours must be a number with at most one decimal place.");
        var hours = Number(value);
        if (!isFinite(hours) || hours < 0.5 || hours > 40) return t("portal.moDashboard.validationWeeklyHoursRange", "Weekly hours must be between 0.5 and 40.");
        return "";
    }

    function validateWorkDate(value, forceRequired, type) {
        var label = type === "end"
            ? t("portal.moDashboard.workEndDate", "Work end date")
            : t("portal.moDashboard.workStartDate", "Work start date");
        if (forceRequired && !value) return t("portal.moDashboard.validationWorkDateRequired", "{field} is required.").replace("{field}", label);
        if (!value) return "";
        if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return t("portal.moDashboard.validationWorkDateInvalid", "{field} must use yyyy-MM-dd.").replace("{field}", label);
        var parsed = new Date(value + "T00:00:00");
        if (isNaN(parsed.getTime())) return t("portal.moDashboard.validationWorkDateInvalid", "{field} must use yyyy-MM-dd.").replace("{field}", label);
        return "";
    }

    function validateWorkPeriodForFields(fields, setter) {
        if (!fields || !fields.workStartDate || !fields.workEndDate) {
            return null;
        }
        var startValue = fields.workStartDate.value.trim();
        var endValue = fields.workEndDate.value.trim();
        var deadlineValue = fields.deadline && fields.deadline.value ? fields.deadline.value.trim() : "";
        if (startValue && deadlineValue) {
            var deadline = parseLocalDateTime(deadlineValue);
            var startForDeadline = new Date(startValue + "T00:00:00");
            if (deadline && !isNaN(startForDeadline.getTime()) && startForDeadline.getTime() < startOfDay(deadline).getTime()) {
                var startMessage = t("portal.moDashboard.validationWorkStartBeforeDeadline", "Work start date cannot be before application deadline.");
                setter("workStartDate", startMessage);
                return { field: fields.workStartDate, message: startMessage };
            }
            setter("workStartDate", "");
        }
        if (!startValue || !endValue) {
            return null;
        }
        var start = new Date(startValue + "T00:00:00");
        var end = new Date(endValue + "T00:00:00");
        if (isNaN(start.getTime()) || isNaN(end.getTime())) {
            return null;
        }
        if (end.getTime() < start.getTime()) {
            var message = t("portal.moDashboard.validationWorkPeriodOrder", "Work end date cannot be before work start date.");
            setter("workEndDate", message);
            return { field: fields.workEndDate, message: message };
        }
        setter("workEndDate", "");
        return null;
    }

    function validateSalary(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationSalaryRequired", "Salary is required.");
        if (!value) return "";
        if (value.length > 120) return t("portal.moDashboard.validationSalaryLength", "Salary must be 120 characters or fewer.");
        if (containsControlChars(value) || containsDangerousMarkup(value)) return t("portal.moDashboard.validationSalaryUnsupported", "Salary contains unsupported characters.");
        return "";
    }

    function validateDeadline(value, forceRequired) {
        if (forceRequired && !value) return t("portal.moDashboard.validationDeadlineRequired", "Application deadline is required.");
        if (!value) return "";
        var parsedDeadline = parseLocalDateTime(value);
        if (!parsedDeadline) return t("portal.moDashboard.validationDeadlineInvalid", "Invalid deadline format.");
        if (parsedDeadline.getTime() < Date.now() - 60000) return t("portal.moDashboard.validationDeadlinePast", "Deadline cannot be in the past.");
        if (parsedDeadline.getTime() > Date.now() + 2 * 365.25 * 24 * 60 * 60 * 1000) return t("portal.moDashboard.validationDeadlineTooFar", "Deadline cannot be more than 2 years in the future.");
        return "";
    }

    // ==========================================
    // Utility Functions
    // ==========================================
    function shakeInvalidFields(container) {
        container.querySelectorAll(".is-invalid").forEach(function (field) {
            var wrap = field.closest(".field");
            if (!wrap) return;
            wrap.classList.remove("field-shake");
            void wrap.offsetWidth; // force reflow to restart animation
            wrap.classList.add("field-shake");
            wrap.addEventListener("animationend", function () {
                wrap.classList.remove("field-shake");
            }, { once: true });
        });
    }

    function setSubmitting(submitting) {
        state.submitting = submitting;
        publishButton.disabled = submitting;
        if (resetButton) {
            resetButton.disabled = submitting;
        }
        publishButton.textContent = submitting ? t("portal.moDashboard.publishing", "Publishing...") : t("portal.moDashboard.publishJob", "Publish job");
    }

    function showMessage(message, type) {
        if (!messageNode) {
            return;
        }
        messageNode.textContent = message;
        messageNode.classList.remove("hidden", "error", "success");
        messageNode.classList.add(type === "success" ? "success" : "error");
    }

    function hideMessage() {
        if (!messageNode) {
            return;
        }
        messageNode.textContent = "";
        messageNode.classList.remove("error", "success");
        messageNode.classList.add("hidden");
    }

    function handleUnauthorized() {
        showMessage(t("portal.dynamic.sessionExpiredRedirect", "Your session has expired. Redirecting to login..."), "error");
        window.setTimeout(function () {
            window.location.href = contextPath + "/login.jsp";
        }, 900);
    }

    function request(url, options) {
        if (window.TARecruitment && window.TARecruitment.api) {
            return window.TARecruitment.api.request(url, options, { parser: parseJson });
        }
        return fetch(url, options).then(function (response) {
            return response.text().then(function (text) {
                return {
                    response: response,
                    payload: parseJson(text)
                };
            });
        });
    }

    function parseJson(text) {
        try {
            return JSON.parse(text);
        } catch (e) {
            return {};
        }
    }

    function normalizeSkillsForSubmit(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }
        return value
            .split(SKILL_SEPARATOR_PATTERN)
            .map(function (item) {
                return item.trim();
            })
            .filter(function (item) {
                return item.length > 0;
            })
            .join(",");
    }

    function parseLocalDateTime(value) {
        if (typeof value !== "string" || !value.trim()) {
            return null;
        }
        var date = new Date(value);
        if (isNaN(date.getTime())) {
            return null;
        }
        return date;
    }

    function startOfDay(date) {
        return new Date(date.getFullYear(), date.getMonth(), date.getDate());
    }

    function containsControlChars(value) {
        return /[\u0000-\u001F\u007F]/.test(value || "");
    }

    function containsDangerousMarkup(value) {
        if (typeof value !== "string" || !value) {
            return false;
        }
        return /<[^>]*>/.test(value) || /javascript:/i.test(value) || /on\w+\s*=/.test(value);
    }

    function buildValidationError(message, field) {
        return {
            message: message,
            field: field || null
        };
    }

    function normalizeDeadline(value) {
        if (typeof value !== "string" || !value.trim()) {
            return "";
        }
        var text = value.trim();
        if (text.length === 16) {
            return text + ":00";
        }
        return text;
    }

    function formatDeadline(deadlineStr) {
        if (!deadlineStr) return t("portal.moDashboard.noDeadline", "No deadline");
        try {
            var date = new Date(deadlineStr);
            if (isNaN(date.getTime())) return deadlineStr;
            return date.toLocaleDateString("en-US", {
                year: "numeric",
                month: "short",
                day: "numeric"
            });
        } catch (e) {
            return deadlineStr;
        }
    }

    function formatDeadlineForInput(deadlineStr) {
        if (!deadlineStr) return "";
        try {
            var date = new Date(deadlineStr);
            if (isNaN(date.getTime())) return "";
            var year = date.getFullYear();
            var month = ("0" + (date.getMonth() + 1)).slice(-2);
            var day = ("0" + date.getDate()).slice(-2);
            var hours = ("0" + date.getHours()).slice(-2);
            var minutes = ("0" + date.getMinutes()).slice(-2);
            return year + "-" + month + "-" + day + "T" + hours + ":" + minutes;
        } catch (e) {
            return "";
        }
    }

    function findJobById(jobId) {
        for (var i = 0; i < state.jobs.length; i++) {
            if (state.jobs[i].jobId === jobId) {
                return state.jobs[i];
            }
        }
        return null;
    }

    function escapeHtml(str) {
        if (str == null) return "";
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function showElement(el) {
        if (el) {
            el.classList.remove("hidden");
        }
    }

    function hideElement(el) {
        if (el) {
            el.classList.add("hidden");
        }
    }

    // ==========================================
    // Applicant Sub-View
    // ==========================================
    /*
     * Initialize “My Postings -> Applicant List” sub-view.
     * Sub-view is no longer an independent navigation page, but a nested flow entered from job cards.
     */
    function initApplicantSubView() {
        if (!panelApplicants || !subviewBackBtn || !subviewSearchForm) return;

        subviewBackBtn.addEventListener("click", function () {
            if (svState.viewMode === "detail") {
                svShowListView();
            } else {
                closeApplicantSubView();
            }
        });

        subviewSearchForm.addEventListener("submit", function (event) {
            event.preventDefault();
            svState.keyword = subviewSearchInput ? subviewSearchInput.value.trim() : "";
            if (svState.searchMode === "ai") {
                runSvAiSearch();
                return;
            }
            clearSvAiRecommendationState();
            svShowListView();
            loadSvApplications();
        });

        if (subviewAiSearchBtn) {
            subviewAiSearchBtn.addEventListener("click", function () {
                setSvSearchMode(svState.searchMode === "ai" ? "search" : "ai");
            });
        }
        updateSvSearchControls();
    }

    /*
     * Open applicant sub-view for a job.
     * Resets search/AI/detail state, then loads applications for that job by jobId.
     */
    function openApplicantSubView(jobId, jobTitle) {
        svState.jobId = jobId;
        svState.jobTitle = jobTitle;
        svState.keyword = "";
        svState.searchMode = "search";
        svState.aiSearchLoading = false;
        svState.viewMode = "list";
        svState.applications = [];
        svState.detailsById = {};
        clearSvAiRecommendationState();
        svState.selectedApplicationId = "";
        svState.reviewingId = "";

        if (subviewSearchInput) subviewSearchInput.value = "";
        if (subviewJobTitle) subviewJobTitle.textContent = jobTitle;
        updateSvSearchControls();

        panelMyJobs.classList.remove("is-active");
        panelMyJobs.hidden = true;
        panelPostJob.classList.remove("is-active");
        panelPostJob.hidden = true;
        panelApplicants.classList.remove("hidden");

        loadSvApplications();
    }

    /*
     * Close applicant sub-view, return to “My Postings” list.
     */
    function closeApplicantSubView() {
        if (panelApplicants) panelApplicants.classList.add("hidden");
        svState.viewMode = "list";
        switchTab("my-jobs");
    }

    /*
     * Return to applicant list from application detail.
     */
    function svShowListView() {
        svState.viewMode = "list";
        svState.selectedApplicationId = "";
        if (subviewJobTitle) subviewJobTitle.textContent = svState.jobTitle;
        updateSvSearchControls();
        renderSvList();
    }

    /*
     * Switch to single application detail.
     * Title shows candidate name, search area hidden to prevent detail page from accidentally triggering list search.
     */
    function svShowDetailView(applicationId) {
        svState.viewMode = "detail";
        svState.selectedApplicationId = applicationId;
        var application = null;
        for (var i = 0; i < svState.applications.length; i++) {
            if (svState.applications[i].applicationId === applicationId) {
                application = svState.applications[i];
                break;
            }
        }
        var detail = svState.detailsById[applicationId];
        var name = (detail && detail.fullName) || (application && application.applicantName) || "Applicant";
        if (subviewJobTitle) subviewJobTitle.textContent = name;
        updateSvSearchControls();
        renderSvDetail(applicationId);
    }

    /*
     * Load application list for current job.
     * Backend /api/applications returns MO-visible applications, then filtered by current jobId on frontend to sub-view data.
     */
    function loadSvApplications() {
        if (svState.loading || svState.aiSearchLoading) return;
        svState.loading = true;
        hideSvMessage();
        setSvListSummary(t("portal.moApplicantSelection.loadingApplications", "Loading applications..."));
        if (subviewList) subviewList.innerHTML = "";
        if (subviewSearchBtn) {
            subviewSearchBtn.disabled = true;
        }
        if (subviewAiSearchBtn) {
            subviewAiSearchBtn.disabled = true;
        }
        updateSvSearchControls();

        var url = window.TARecruitment.routes.applications.list({ keyword: svState.keyword });

        request(url, { method: "GET", headers: { "X-Requested-With": "XMLHttpRequest" } })
            .then(function (result) {
                var payload = result.payload;
                if (result.response.status === 401) { handleUnauthorized(); return; }
                if (!result.response.ok || !payload || payload.success !== true) {
                    showSvMessage(t("portal.dynamic.unableLoadApplicationsNow", "Unable to load applications."), "error");
                    svState.applications = [];
                    renderSvList();
                    return;
                }
                var all = getPayloadDataArray(payload, "applications");
                svState.applications = all.filter(function (app) {
                    return String(app.jobId) === String(svState.jobId);
                });
                return loadSvApplicantDetails(svState.applications);
            })
            .then(function () { renderSvList(); })
            .catch(function () {
                showSvMessage(t("portal.dynamic.networkErrorTryAgain", "Network error. Please try again."), "error");
                svState.applications = [];
                renderSvList();
            })
            .finally(function () {
                svState.loading = false;
                setSvAiSearchLoading(false);
            });
    }

    /*
     * Applicant AI recommendation search.
     * Requests /api/mo/applicant-recommendations, backend returns real applicant list and recommendation reasons;
     * No fabricated candidates on frontend.
     */
    function runSvAiSearch() {
        if (svState.loading || svState.aiSearchLoading) return;
        if (!svState.jobId) {
            showSvMessage(t("portal.dynamic.jobIdMissing", "Job ID is missing."), "error");
            return;
        }

        var query = subviewSearchInput ? subviewSearchInput.value.trim() : "";
        svShowListView();
        hideSvMessage();
        setSvListSummary(t("portal.moApplicantSelection.aiSearchLoading", "AI searching..."));
        setSvAiSearchLoading(true);

        var params = new URLSearchParams();
        params.append("jobId", svState.jobId);
        params.append("query", query);

        request(window.TARecruitment.routes.mo.applicantRecommendations(), {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: params.toString()
        })
            .then(function (result) {
                var payload = result.payload;
                var data = getPayloadDataObject(payload);
                if (result.response.status === 401) {
                    handleUnauthorized();
                    return;
                }
                if (!result.response.ok || !payload || payload.success !== true) {
                    showSvMessage(
                        localizeServerMessage(payload && payload.message, "portal.moApplicantSelection.aiSearchUnavailable", "AI search is currently unavailable."),
                        "error"
                    );
                    renderSvList();
                    return;
                }

                if (data.action === "out_of_scope") {
                    showSvMessage(
                        localizeServerMessage(data.message || payload.message, "portal.moApplicantSelection.aiOutOfScope", "I cannot process your request. Based on current job applicant information, I can help recommend candidates, compare applicants, or explain recommendation reasons."),
                        "error"
                    );
                    renderSvList();
                    return;
                }

                if (data.action !== "recommend") {
                    showSvMessage(t("portal.moApplicantSelection.aiSearchUnavailable", "AI search is currently unavailable."), "error");
                    renderSvList();
                    return;
                }

                svState.keyword = query;
                svState.applications = Array.isArray(data.applications) ? data.applications : [];
                svState.aiSearchActive = true;
                svState.aiRecommendationsById = buildSvRecommendationMap(data);
                return loadSvApplicantDetails(svState.applications).then(function () {
                    renderSvList();
                    if (data.message) {
                        showSvMessage(localizeServerMessage(data.message, "", ""), "success");
                    }
                });
            })
            .catch(function () {
                showSvMessage(t("portal.moApplicantSelection.aiSearchUnavailable", "AI search is currently unavailable."), "error");
                renderSvList();
            })
            .finally(function () {
                setSvAiSearchLoading(false);
            });
    }

    /*
     * Clear AI recommendation markers.
     * Must reset when normal search reloads to avoid old recommendation reasons being applied to new list.
     */
    function clearSvAiRecommendationState() {
        svState.aiSearchActive = false;
        svState.aiRecommendationsById = {};
    }

    /*
     * Toggle sub-view search mode, only updates controls, does not auto-send request.
     */
    function setSvSearchMode(mode) {
        svState.searchMode = mode === "ai" ? "ai" : "search";
        updateSvSearchControls();
    }

    /*
     * Organize backend recommendation reasons into applicationId -> reason map.
     * Compatible with both recommendationsByApplicationId and recommendations array response formats.
     */
    function buildSvRecommendationMap(data) {
        var map = {};
        var byId = data && data.recommendationsByApplicationId;
        if (byId && typeof byId === "object" && !Array.isArray(byId)) {
            Object.keys(byId).forEach(function (applicationId) {
                if (applicationId && byId[applicationId]) {
                    map[applicationId] = String(byId[applicationId]);
                }
            });
        }

        var recommendations = data && Array.isArray(data.recommendations) ? data.recommendations : [];
        recommendations.forEach(function (item) {
            if (item && item.applicationId && item.recommendation) {
                map[item.applicationId] = String(item.recommendation);
            }
        });
        return map;
    }

    function setSvAiSearchLoading(loading) {
        svState.aiSearchLoading = loading === true;
        updateSvSearchControls();
    }

    /*
     * Sync sub-view search box, AI toggle, and button text.
     * Hide search form in detail mode to avoid mixing list and detail interactions.
     */
    function updateSvSearchControls() {
        var busy = svState.loading || svState.aiSearchLoading;
        var detailMode = svState.viewMode === "detail";
        if (subviewSearchForm) {
            subviewSearchForm.hidden = detailMode;
            subviewSearchForm.classList.toggle("hidden", detailMode);
        }
        if (subviewAiSearchBtn) {
            subviewAiSearchBtn.disabled = busy;
            subviewAiSearchBtn.setAttribute("data-mode", svState.searchMode);
            subviewAiSearchBtn.setAttribute("aria-pressed", svState.searchMode === "ai" ? "true" : "false");
            var searchOption = subviewAiSearchBtn.querySelector("[data-mode-option=\"search\"]");
            var aiOption = subviewAiSearchBtn.querySelector("[data-mode-option=\"ai\"]");
            if (searchOption) {
                searchOption.textContent = t("portal.common.search", "Search");
                searchOption.classList.toggle("is-active", svState.searchMode !== "ai");
            }
            if (aiOption) {
                aiOption.textContent = t("portal.moApplicantSelection.aiSearchButton", "AI");
                aiOption.classList.toggle("is-active", svState.searchMode === "ai");
            }
        }
        if (subviewSearchBtn) {
            subviewSearchBtn.disabled = busy;
            subviewSearchBtn.classList.toggle("search-submit--ai", svState.searchMode === "ai");
            if (svState.aiSearchLoading) {
                subviewSearchBtn.textContent = t("portal.moApplicantSelection.aiSearching", "AI...");
            } else if (svState.loading) {
                subviewSearchBtn.textContent = t("portal.dynamic.searching", "Searching...");
            } else if (svState.searchMode === "ai") {
                subviewSearchBtn.textContent = t("portal.moApplicantSelection.aiSearchButton", "AI");
            } else {
                subviewSearchBtn.textContent = t("portal.common.search", "Search");
            }
        }
    }

    /*
     * Supplement load applicant profile snapshot.
     * Single detail failure does not block list; page continues to display basic info already in Application.
     */
    function loadSvApplicantDetails(applications) {
        if (!Array.isArray(applications) || applications.length === 0) return Promise.resolve();
        var requests = applications.map(function (app) {
            var id = app.applicationId || "";
            if (!id) return Promise.resolve();
            return request(window.TARecruitment.routes.applications.applicant(id), {
                method: "GET", headers: { "X-Requested-With": "XMLHttpRequest" }
            }).then(function (result) {
                if (result.response.ok && result.payload && result.payload.success === true) {
                    svState.detailsById[id] = getPayloadDataObject(result.payload);
                }
            }).catch(function () {});
        });
        return Promise.all(requests);
    }

    /*
     * Render applicant list or detail.
     * When viewMode=detail, delegates directly to renderSvDetail.
     */
    function renderSvList() {
        if (!subviewList) return;
        updateSvSearchControls();
        subviewList.innerHTML = "";

        if (svState.viewMode === "detail") {
            renderSvDetail(svState.selectedApplicationId);
            return;
        }

        var apps = svState.applications;
        if (!apps || apps.length === 0) {
            setSvListSummary("");
            var empty = document.createElement("div");
            empty.className = "empty-state";
            empty.innerHTML =
                "<p class=\"empty-title\">" + escapeHtml(svState.keyword
                    ? t("portal.dynamic.noMatchingApplicationsTitle", "No matching applications")
                    : t("portal.dynamic.noApplicationsYetTitle", "No applications yet")) + "</p>" +
                "<p class=\"empty-copy\">" + escapeHtml(svState.keyword
                    ? t("portal.dynamic.tryAnotherKeyword", "Try another keyword.")
                    : t("portal.dynamic.noApplicationsForPostedJobsHint", "Once TAs apply, applicants will appear here.")) + "</p>";
            subviewList.appendChild(empty);
            return;
        }

        setSvListSummary(apps.length + " " + (svState.aiSearchActive
            ? t("portal.moApplicantSelection.aiRecommendedUnit", "AI recommendation(s)")
            : t("portal.dynamic.applicationUnit", "application(s)")));
        apps.forEach(function (app) {
            subviewList.appendChild(createSvApplicantItem(app));
        });
    }

    /*
     * Asynchronously mount photo to candidate avatar container.
     * On failure, keep initial letter avatar without exposing image load failure as error message.
     */
    function attachApplicantPhoto(avatarEl, applicationId) {
        if (!avatarEl || !applicationId) return;
        var img = new Image();
        img.className = "applicant-avatar-photo";
        img.alt = "";
        img.onload = function () {
            avatarEl.textContent = "";
            avatarEl.appendChild(img);
        };
        img.src = window.TARecruitment.routes.applications.applicantPhoto(applicationId) + "?v=" + Date.now();
    }

    /*
     * Create applicant list item.
     * AI recommendation reason only displayed in AI search results; normal list keeps application status and skills summary.
     */
    function createSvApplicantItem(application) {
        var item = document.createElement("article");
        var id = application.applicationId || "";
        var detail = svState.detailsById[id];
        var name = (detail && detail.fullName) || application.applicantName || t("portal.moApplicantSelection.unknownApplicant", "Unknown applicant");
        var email = application.applicantEmail || "-";
        var status = String(application.status || "PENDING").toUpperCase();
        var statusClass = getStatusClass(status);
        var statusLabel = getStatusDisplayLabel(status);
        var avatarLetter = name.trim() ? name.trim().charAt(0).toUpperCase() : "?";
        var skills = detail && Array.isArray(detail.skills) ? detail.skills.slice(0, 3) : [];
        var recommendation = safeValue(svState.aiRecommendationsById[id], "");
        var skillsMarkup = skills.map(function (s) {
            return "<span class=\"course-applicant-skill\">" + escapeHtml(s) + "</span>";
        }).join("");
        var recommendationMarkup = recommendation
            ? "<div class=\"course-applicant-ai-note\">" +
                "<p class=\"course-applicant-ai-note-title\">" +
                    escapeHtml(t("portal.moApplicantSelection.aiRecommendationTitle", "Recommendation (AI Generated)")) +
                "</p>" +
                "<p class=\"course-applicant-ai-note-copy\">" + escapeHtml(recommendation) + "</p>" +
              "</div>"
            : "";

        item.className = "course-applicant-item course-applicant-item--status-" + statusClass +
            (recommendation ? " course-applicant-item--ai" : "");
        item.setAttribute("role", "button");
        item.setAttribute("tabindex", "0");

        item.innerHTML =
            "<div class=\"course-applicant-lead\">" +
                "<div class=\"course-applicant-avatar\" aria-hidden=\"true\">" + escapeHtml(avatarLetter) + "</div>" +
                "<div class=\"course-applicant-text\">" +
                    "<p class=\"course-applicant-name\">" + escapeHtml(name) + "</p>" +
                    "<p class=\"course-applicant-email\">" + escapeHtml(email) + "</p>" +
                "</div>" +
            "</div>" +
            "<div class=\"course-applicant-skills" + (skillsMarkup ? "" : " course-applicant-skills--empty") + "\">" + skillsMarkup + "</div>" +
            "<div class=\"course-applicant-trail\">" +
                "<span class=\"status-pill status-" + escapeHtml(statusClass) + "\">" + escapeHtml(statusLabel) + "</span>" +
            "</div>" +
            recommendationMarkup;

        attachApplicantPhoto(item.querySelector(".course-applicant-avatar"), id);
        item.addEventListener("click", function () { svShowDetailView(id); });
        item.addEventListener("keydown", function (e) {
            if (e.key === "Enter" || e.key === " ") { e.preventDefault(); svShowDetailView(id); }
        });
        return item;
    }

    /*
     * Render single application detail.
     */
    function renderSvDetail(applicationId) {
        if (!subviewList || !applicationId) return;
        updateSvSearchControls();
        subviewList.innerHTML = "";

        var application = null;
        for (var i = 0; i < svState.applications.length; i++) {
            if (svState.applications[i].applicationId === applicationId) {
                application = svState.applications[i];
                break;
            }
        }
        if (!application) return;

        var detail = svState.detailsById[applicationId];
        var wrapper = document.createElement("section");
        wrapper.className = "single-application-view";

        var lead = document.createElement("p");
        lead.className = "single-application-lead";
        lead.textContent = t("portal.moApplicantSelection.applicationDetailLead", "Review applicant profile and complete your decision.");
        wrapper.appendChild(lead);

        var card = createSvApplicationCard(application, detail);
        wrapper.appendChild(card);

        subviewList.appendChild(wrapper);
    }

    /*
     * Create application detail card.
     * Card merges Application basic info and applicant profile snapshot, shows review actions in PENDING state.
     */
    function createSvApplicationCard(application, detail) {
        var card = document.createElement("article");
        card.className = "application-card";

        var applicationId = safeValue(application.applicationId, "");
        var status = String(application.status || "PENDING").toUpperCase();
        var progressStage = String(application.progressStage || "UNDER_REVIEW").toUpperCase();
        var statusClass = getStatusClass(status);
        var reviewingThis = svState.reviewingId === applicationId;
        var profileName = detail ? safeValue(detail.fullName, "") : "";
        var applicantDisplayName = profileName || safeValue(application.applicantName, t("portal.moApplicantSelection.unknownApplicant", "Unknown applicant"));
        var nameTrim = applicantDisplayName.trim();
        var avatarLetter = nameTrim ? nameTrim.charAt(0).toUpperCase() : "?";
        var profileUpdatedAt = detail ? safeValue(detail.profileUpdatedAt, "") : "";
        var profileSyncHint = profileUpdatedAt
            ? "<p class=\"application-sync-hint\">" +
                escapeHtml(t("portal.moApplicantSelection.profileSyncedAt", "Profile synced at")) + " " +
                escapeHtml(formatSvDateTime(profileUpdatedAt)) +
              "</p>"
            : "";
        var coverLetter = safeValue(application.coverLetter, "");
        var coverLetterText = coverLetter
            ? (coverLetter.length > 280 ? coverLetter.substring(0, 279) + "…" : coverLetter)
            : t("portal.moApplicantSelection.noCoverLetter", "No cover letter provided.");

        var jobValue = safeValue(application.jobTitle, t("portal.moApplicantSelection.noJobTitle", "No job title available"));
        var courseValue = safeValue(application.courseCode, "-");
        var appliedValue = formatSvDateTime(application.appliedAt);
        card.innerHTML =
            "<header class=\"application-card-head\">" +
                "<div class=\"application-avatar\" aria-hidden=\"true\">" + escapeHtml(avatarLetter) + "</div>" +
                "<div class=\"application-head-body\">" +
                    "<div class=\"application-head-top\">" +
                        "<div class=\"application-heading\">" +
                            "<h3 class=\"application-name\">" + escapeHtml(applicantDisplayName) + "</h3>" +
                            "<p class=\"application-email\">" + escapeHtml(safeValue(application.applicantEmail, "-")) + "</p>" +
                            profileSyncHint +
                        "</div>" +
                        "<div class=\"application-head-actions\">" +
                            "<span class=\"status-pill status-" + escapeHtml(statusClass) + "\">" +
                                escapeHtml(getStatusDisplayLabel(status)) +
                            "</span>" +
                        "</div>" +
                    "</div>" +
                "</div>" +
            "</header>" +
            "<div class=\"application-meta-card\">" +
                "<div class=\"application-meta\" role=\"presentation\">" +
                    buildSvMetaStat("application-meta-stat--job", buildSvSvgBriefcase(), t("portal.moApplicantSelection.job", "Job"), jobValue) +
                    buildSvMetaStat("application-meta-stat--course", buildSvSvgCourse(), t("portal.common.courseCode", "Course code"), courseValue) +
                    buildSvMetaStat("application-meta-stat--applied", buildSvSvgCalendar(), t("portal.moApplicantSelection.appliedAtLabel", "Applied at"), appliedValue) +
                "</div>" +
            "</div>" +
            buildSvDetailBlock(detail, applicationId, profileUpdatedAt) +
            "<div class=\"cover-letter-block\">" +
                "<p class=\"cover-letter-label\">" + escapeHtml(t("portal.taJobDetail.coverLetter", "Cover letter")) + "</p>" +
                "<p class=\"cover-letter-content\">" + escapeHtml(coverLetterText) + "</p>" +
            "</div>" +
            buildSvReviewActionsHtml(status, progressStage, applicationId, reviewingThis);

        attachApplicantPhoto(card.querySelector(".application-avatar"), applicationId);

        if (status === "PENDING" && applicationId) {
            card.querySelectorAll("button[data-sv-action]").forEach(function (btn) {
                var action = btn.getAttribute("data-sv-action");
                btn.addEventListener("click", function () {
                    handleSvReview(applicationId, action);
                });
            });
        }

        return card;
    }

    /*
     * Job/course/application time mini metrics at top of detail page.
     */
    function buildSvMetaStat(cls, iconHtml, label, value) {
        return "<div class=\"application-meta-stat " + escapeHtml(cls) + "\">" +
            "<span class=\"application-meta-icon\" aria-hidden=\"true\">" + iconHtml + "</span>" +
            "<div class=\"application-meta-text\">" +
                "<span class=\"application-meta-label\">" + escapeHtml(label) + "</span>" +
                "<span class=\"application-meta-value\">" + escapeHtml(value) + "</span>" +
            "</div>" +
        "</div>";
    }

    /*
     * Build applicant profile detail block.
     * When detail is empty, show unavailable message without blocking MO from viewing application status.
     */
    function buildSvDetailBlock(detail, applicationId, profileUpdatedAt) {
        var profileTitle = t("portal.moApplicantSelection.applicantProfile", "Applicant profile");
        if (!detail) {
            return "<section class=\"applicant-detail-block\">" +
                "<div class=\"detail-panel detail-panel--empty\">" +
                    "<p class=\"detail-panel-title\">" + escapeHtml(profileTitle) + "</p>" +
                    "<p class=\"detail-empty\">" + escapeHtml(t("portal.moApplicantSelection.profileUnavailable", "Applicant profile details are temporarily unavailable.")) + "</p>" +
                "</div>" +
            "</section>";
        }

        var skills = Array.isArray(detail.skills) ? detail.skills : [];
        var skillsMarkup = skills.length
            ? skills.map(function (s) { return "<span class=\"detail-chip\">" + escapeHtml(safeValue(s, "")) + "</span>"; }).join("")
            : "<span class=\"detail-chip muted\">" + escapeHtml(t("portal.moApplicantSelection.noSkillsListed", "No skills listed")) + "</span>";

        var resumeRow = detail.hasResume
            ? "<div class=\"material-row\">" +
                "<svg class=\"material-doc-icon\" viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\">" +
                "<path d=\"M7 3.5h6.2L18 8.3V20a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 6 20V5A1.5 1.5 0 0 1 7.5 3.5\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>" +
                "<path d=\"M13 3.8V8a1 1 0 0 0 1 1h4\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>" +
                "<path d=\"M9.5 13h5M9.5 16.5h4\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\"/>" +
                "</svg>" +
                "<div class=\"material-info\"><span class=\"material-name\">" + escapeHtml(t("portal.moApplicantSelection.resumeDocument", "Resume")) + "</span>" +
                "<span class=\"material-meta\">" + escapeHtml(t("portal.moApplicantSelection.resumeFormatHint", "Uploaded file")) + "</span></div>" +
                "<a class=\"material-view-link\" href=\"" + window.TARecruitment.routes.applications.applicantResume(applicationId) + "\" target=\"_blank\" rel=\"noopener\">" + escapeHtml(t("portal.moApplicantSelection.viewAction", "View")) + "</a>" +
            "</div>"
            : "<p class=\"detail-muted material-empty\">" + escapeHtml(t("portal.moApplicantSelection.resumeNotUploaded", "Resume not uploaded")) + "</p>";

        return "<section class=\"applicant-detail-block\">" +
            "<div class=\"detail-panel\">" +
                "<p class=\"detail-panel-title\">" + escapeHtml(profileTitle) + "</p>" +
                "<div class=\"detail-grid\">" +
                    buildSvDetailItem(t("portal.taDashboard.department", "Department"), detail.department) +
                    buildSvDetailItem(t("portal.taDashboard.program", "Program"), detail.program) +
                    buildSvDetailItem(t("portal.taDashboard.gpa", "GPA"), detail.gpa) +
                    buildSvDetailItem(t("portal.moApplicantSelection.phone", "Phone"), detail.phone) +
                "</div>" +
            "</div>" +
            "<div class=\"detail-panel detail-panel--skills\">" +
                "<p class=\"detail-panel-title\">" + escapeHtml(t("portal.taDashboard.skills", "Skills")) + "</p>" +
                "<div class=\"detail-chips\">" + skillsMarkup + "</div>" +
            "</div>" +
            "<div class=\"detail-panel\">" +
                "<p class=\"detail-panel-title\">" + escapeHtml(t("portal.moApplicantSelection.experience", "Experience")) + "</p>" +
                "<p class=\"detail-copy detail-copy--prose\">" +
                    escapeHtml(safeValue(detail.experience, t("portal.moApplicantSelection.noExperience", "No experience provided."))) +
                "</p>" +
            "</div>" +
            "<div class=\"detail-panel\">" +
                "<p class=\"detail-panel-title\">" + escapeHtml(t("portal.moApplicantSelection.motivationLabel", "Motivation")) + "</p>" +
                "<p class=\"detail-copy detail-copy--prose\">" +
                    escapeHtml(safeValue(detail.motivation, t("portal.moApplicantSelection.noMotivation", "No motivation statement provided."))) +
                "</p>" +
            "</div>" +
            "<div class=\"detail-panel detail-panel--materials\">" +
                "<p class=\"detail-panel-title\">" + escapeHtml(t("portal.moApplicantSelection.applicationMaterials", "Application materials")) + "</p>" +
                "<div class=\"application-materials-inner\">" + resumeRow + "</div>" +
            "</div>" +
        "</section>";
    }

    /*
     * Profile key-value item.
     */
    function buildSvDetailItem(label, value) {
        return "<div class=\"detail-item\">" +
            "<span class=\"detail-item-label\">" + escapeHtml(label) + "</span>" +
            "<strong class=\"detail-item-value\">" + escapeHtml(safeValue(value, "-")) + "</strong>" +
        "</div>";
    }

    /*
     * Only show accept/reject buttons for PENDING applications.
     * progressStage currently only used for display, does not change final decision entry here.
     */
    function buildSvReviewActionsHtml(status, progressStage, applicationId, reviewingThis) {
        if (status !== "PENDING") return "";
        var dis = reviewingThis ? " disabled" : "";
        var proc = t("portal.moApplicantSelection.processing", "Processing...");
        var acceptLabel = reviewingThis ? proc : t("portal.moApplicantSelection.hireApplicant", "Hire applicant");
        var rejectLabel = reviewingThis ? proc : t("portal.moApplicantSelection.rejectApplicant", "Reject");

        return "<div class=\"review-actions review-actions--staged\">" +
            "<div class=\"review-decision-row\">" +
            "<button class=\"accept-btn\" type=\"button\" data-sv-action=\"accept\" data-id=\"" + escapeHtml(applicationId) + "\"" + dis + ">" +
            "<svg class=\"review-btn-icon\" viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\"><path d=\"M20 6L9 17l-5-5\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.25\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/></svg>" +
            "<span class=\"review-btn-text\">" + escapeHtml(acceptLabel) + "</span>" +
            "</button>" +
            "<button class=\"reject-btn\" type=\"button\" data-sv-action=\"reject\" data-id=\"" + escapeHtml(applicationId) + "\"" + dis + ">" +
            "<svg class=\"review-btn-icon\" viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\"><path d=\"M18 6L6 18M6 6l12 12\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.25\" stroke-linecap=\"round\"/></svg>" +
            "<span class=\"review-btn-text\">" + escapeHtml(rejectLabel) + "</span>" +
            "</button>" +
            "</div></div>";
    }

    /*
     * Handle MO accept/reject for applications.
     * On success, return to list and reload to ensure status and job quota are in sync.
     */
    function handleSvReview(applicationId, action) {
        if (svState.reviewingId) return;
        svState.reviewingId = applicationId;
        renderSvList();

        var formData = new URLSearchParams();
        formData.set("action", action);
        request(window.TARecruitment.routes.applications.transition(applicationId), {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "X-Requested-With": "XMLHttpRequest" },
            body: formData.toString()
        }).then(function (result) {
            if (result.response.status === 401) { handleUnauthorized(); return; }
            if (!result.response.ok || !result.payload || result.payload.success !== true) {
                showSvMessage(t("portal.dynamic.unableLoadApplicationsNow", "Unable to update application."), "error");
                return;
            }
            showSvMessage(action === "accept"
                ? t("portal.dynamic.applicationAccepted", "Application accepted.")
                : t("portal.dynamic.applicationRejected", "Application rejected."), "success");
            svState.viewMode = "list";
            svState.selectedApplicationId = "";
            if (subviewJobTitle) subviewJobTitle.textContent = svState.jobTitle;
        }).catch(function () {
            showSvMessage(t("portal.dynamic.networkErrorTryAgain", "Network error."), "error");
        }).finally(function () {
            svState.reviewingId = "";
            loadSvApplications();
        });
    }

    function showSvMessage(message, type) {
        if (!subviewMessage) return;
        subviewMessage.textContent = message;
        subviewMessage.classList.remove("hidden", "error", "success");
        subviewMessage.classList.add(type === "success" ? "success" : "error");
    }

    function hideSvMessage() {
        if (!subviewMessage) return;
        subviewMessage.textContent = "";
        subviewMessage.classList.remove("error", "success");
        subviewMessage.classList.add("hidden");
    }

    function setSvListSummary(text) {
        if (!subviewListSummary) return;
        if (!text) { subviewListSummary.hidden = true; subviewListSummary.textContent = ""; return; }
        subviewListSummary.hidden = false;
        subviewListSummary.textContent = text;
    }

    function getStatusClass(status) {
        if (status === "PENDING") return "pending";
        if (status === "ACCEPTED") return "accepted";
        if (status === "REJECTED") return "rejected";
        if (status === "WITHDRAWN") return "withdrawn";
        return "unknown";
    }

    function getStatusDisplayLabel(status) {
        if (status === "PENDING") return t("portal.common.pending", "Pending");
        if (status === "ACCEPTED") return t("portal.common.accepted", "Accepted");
        if (status === "REJECTED") return t("portal.common.rejected", "Rejected");
        if (status === "WITHDRAWN") return t("portal.common.withdrawn", "Withdrawn");
        return status || "-";
    }

    function safeValue(value, fallback) {
        if (typeof value === "string" && value.trim()) return value.trim();
        if (typeof value === "number") return String(value);
        return typeof fallback === "string" ? fallback : "";
    }

    function formatSvDateTime(value) {
        if (typeof value !== "string" || !value.trim()) return "-";
        var date = new Date(value);
        if (isNaN(date.getTime())) return value;
        var pad = function (n) { return n < 10 ? "0" + n : String(n); };
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate()) +
            " " + pad(date.getHours()) + ":" + pad(date.getMinutes());
    }

    function buildSvSvgBriefcase() {
        return "<svg viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.75\">" +
            "<rect x=\"2\" y=\"7\" width=\"20\" height=\"14\" rx=\"2\"/>" +
            "<path d=\"M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2\"/>" +
            "</svg>";
    }

    function buildSvSvgCourse() {
        return "<svg viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.75\">" +
            "<path d=\"M4 19.5A2.5 2.5 0 0 1 6.5 17H20\"/>" +
            "<path d=\"M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z\"/>" +
            "</svg>";
    }

    function buildSvSvgCalendar() {
        return "<svg viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.75\">" +
            "<rect x=\"3\" y=\"4\" width=\"18\" height=\"18\" rx=\"2\"/>" +
            "<path d=\"M16 2v4M8 2v4M3 10h18\"/>" +
            "</svg>";
    }

    function getPayloadDataArray(payload, key) {
        if (!payload || typeof payload !== "object") return [];
        if (payload.data && Array.isArray(payload.data[key])) return payload.data[key];
        if (Array.isArray(payload[key])) return payload[key];
        return [];
    }

    function getPayloadDataObject(payload) {
        if (!payload || typeof payload !== "object") return {};
        if (payload.data && typeof payload.data === "object") return payload.data;
        return payload;
    }

    // ==========================================
    // Edit Modal Validation
    // ==========================================
    function initializeEditRealtimeValidation() {
        orderedFieldKeys.forEach(function (key) {
            var field = editFields[key];
            if (!field) return;

            editFieldValidationState.feedbackByKey[key] = ensureEditFieldFeedbackNode(key, field);
            editFieldValidationState.touchedByKey[key] = false;

            field.addEventListener("blur", function () {
                editFieldValidationState.touchedByKey[key] = true;
                validateEditSingleField(key, { forceRequired: editFieldValidationState.dirtyByKey[key] === true });
                if (isScheduleField(key)) {
                    validateWorkPeriodForFields(editFields, setEditFieldValidationResult);
                }
            });
            field.addEventListener("input", function () {
                editFieldValidationState.dirtyByKey[key] = true;
                validateEditSingleField(key, { forceRequired: true });
                if (isScheduleField(key)) {
                    validateWorkPeriodForFields(editFields, setEditFieldValidationResult);
                }
            });
            field.addEventListener("change", function () {
                editFieldValidationState.dirtyByKey[key] = true;
                validateEditSingleField(key, { forceRequired: true });
                if (isScheduleField(key)) {
                    validateWorkPeriodForFields(editFields, setEditFieldValidationResult);
                }
            });
        });
    }

    function initializeEditEnterKeyNavigation() {
        if (!editForm) return;
        editForm.addEventListener("keydown", function (event) {
            if (!event || event.key !== "Enter" || event.isComposing) return;
            var target = event.target;
            if (!target || target.form !== editForm) return;
            if (target.tagName === "TEXTAREA") return;
            if (target.tagName === "BUTTON") return;
            if (target.tagName === "SELECT") return;
            event.preventDefault();
            focusNextEditField(target);
        });
    }

    function focusNextEditField(current) {
        var fields = Array.prototype.slice.call(
            editForm.querySelectorAll("input:not([disabled]):not([type='hidden']), textarea:not([disabled])")
        );
        var idx = fields.indexOf(current);
        if (idx >= 0 && idx < fields.length - 1) {
            fields[idx + 1].focus();
        }
    }

    function ensureEditFieldFeedbackNode(key, field) {
        var container = field.closest(".field");
        if (!container) return null;
        var selector = ".field-feedback[data-for=\"edit-" + key + "\"]";
        var feedback = container.querySelector(selector);
        if (!feedback) {
            feedback = document.createElement("p");
            feedback.className = "field-feedback";
            feedback.setAttribute("data-for", "edit-" + key);
            feedback.setAttribute("role", "status");
            feedback.setAttribute("aria-live", "polite");
            feedback.id = field.id + "-feedback";
            container.appendChild(feedback);
        }
        var describedBy = field.getAttribute("aria-describedby");
        if (!describedBy) {
            field.setAttribute("aria-describedby", feedback.id);
        } else if ((" " + describedBy + " ").indexOf(" " + feedback.id + " ") === -1) {
            field.setAttribute("aria-describedby", describedBy + " " + feedback.id);
        }
        return feedback;
    }

    function validateEditSingleField(key, options) {
        var field = editFields[key];
        if (!field) return null;
        var settings = options || {};
        var value = typeof field.value === "string" ? field.value.trim() : "";
        var message = getFieldValidationMessage(key, value, settings.forceRequired === true);
        setEditFieldValidationResult(key, message);
        return { field: field, message: message };
    }

    function setEditFieldValidationResult(key, message) {
        var field = editFields[key];
        var feedback = editFieldValidationState.feedbackByKey[key];
        if (!field) return;
        if (feedback) {
            if (message) { feedback.textContent = message; feedback.classList.add("is-visible"); }
            else { feedback.textContent = ""; feedback.classList.remove("is-visible"); }
        }
        if (message) { field.classList.add("is-invalid"); field.setAttribute("aria-invalid", "true"); return; }
        field.classList.remove("is-invalid");
        field.removeAttribute("aria-invalid");
    }

    function validateEditForm() {
        var firstError = null;
        orderedFieldKeys.forEach(function (key) {
            var field = editFields[key];
            if (!field) return;
            editFieldValidationState.touchedByKey[key] = true;
            var result = validateEditSingleField(key, { forceRequired: true });
            if (!firstError && result && result.message) {
                firstError = result;
            }
        });
        if (!firstError) {
            firstError = validateWorkPeriodForFields(editFields, setEditFieldValidationResult);
        }
        if (!firstError) return null;
        return buildValidationError(firstError.message, firstError.field);
    }

    // ==========================================
    // Start
    // ==========================================
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

})();

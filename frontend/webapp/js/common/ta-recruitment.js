/*
 * TARecruitment common frontend utilities.
 *
 * Purpose:
 * - Unified /api/... path generation based on window.APP_CONTEXT_PATH, replacing old hardcoded paths;
 * - Wrapper for fetch response parsing and unauthorized redirect;
 * - Provides reusable text/date/HTML escape utilities for all role pages.
 */
(function (window) {
    "use strict";

    var namespace = window.TARecruitment || {};

    /*
     * Parse backend unified JSON response.
     * When strictJson=true, preserves parse exceptions so pages can treat non-JSON responses as errors.
     */
    function parseJson(text, strictJson) {
        if (typeof text !== "string" || !text.trim()) {
            return {};
        }
        try {
            return JSON.parse(text);
        } catch (error) {
            if (strictJson) {
                throw error;
            }
            return {};
        }
    }

    /*
     * Global fetch wrapper.
     * Returns { response, payload } so pages can check both HTTP status and business success.
     */
    function request(url, options, meta) {
        var settings = options || {};
        var requestMeta = meta || {};
        // parser allows a few pages to customize parsing, but defaults to this file's JSON parsing rules.
        var parser = typeof requestMeta.parser === "function" ? requestMeta.parser : null;
        var strictJson = requestMeta.strictJson !== false;
        var redirectOnUnauthorized = requestMeta.redirectOnUnauthorized === true;

        return fetch(url, settings).then(function (response) {
            if (redirectOnUnauthorized && response.status === 401) {
                redirectToLogin();
            }
            return response.text().then(function (bodyText) {
                return {
                    response: response,
                    payload: parser ? parser(bodyText) : parseJson(bodyText, strictJson)
                };
            });
        });
    }

    /*
     * Unified login expired redirect entry.
     * Local page request wrappers can call this to avoid scattered different login.jsp path construction.
     */
    function redirectToLogin(delayMs) {
        window.setTimeout(function () {
            window.location.href = resolveContextPath() + "/login.jsp";
        }, typeof delayMs === "number" ? delayMs : 900);
    }

    /*
     * Parse deployment context path.
     * Prefers JSP-injected APP_CONTEXT_PATH, falls back to legacy window.contextPath and meta tag.
     */
    function resolveContextPath() {
        if (typeof window.APP_CONTEXT_PATH === "string") {
            return window.APP_CONTEXT_PATH;
        }
        if (typeof window.contextPath === "string") {
            return window.contextPath;
        }
        var meta = document.querySelector("meta[name='context-path']");
        return meta ? meta.getAttribute("content") || "" : "";
    }

    /*
     * Append context path to backend API paths.
     * The path received here must be /api/..., old root paths like /jobs and /apply should not be passed.
     */
    function apiPath(path) {
        return resolveContextPath() + path;
    }

    /*
     * Append query parameters, automatically skipping empty values.
     * The query object passed by pages only expresses valid filter conditions.
     */
    function appendQuery(url, query) {
        if (!query) {
            return url;
        }
        var parts = [];
        Object.keys(query).forEach(function (key) {
            var value = query[key];
            if (value === null || typeof value === "undefined" || value === "") {
                return;
            }
            parts.push(encodeURIComponent(key) + "=" + encodeURIComponent(String(value)));
        });
        return parts.length ? url + "?" + parts.join("&") : url;
    }

    /*
     * Text utility: converts null/undefined to empty string or specified fallback.
     */
    function safeText(value, fallback) {
        if (value === null || typeof value === "undefined") {
            return typeof fallback === "string" ? fallback : "";
        }
        return String(value);
    }

    /*
     * HTML escape utility for page rendering functions that use innerHTML.
     */
    function escapeHtml(value) {
        return safeText(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    /*
     * Lightweight datetime formatting: backend LocalDateTime string to page short time.
     */
    function formatDateTime(value, fallback) {
        if (!value) {
            return typeof fallback === "string" ? fallback : "-";
        }
        return String(value).replace("T", " ").slice(0, 16);
    }

    /*
     * Lightweight date formatting: shows only yyyy-MM-dd.
     */
    function formatDate(value, fallback) {
        if (!value) {
            return typeof fallback === "string" ? fallback : "-";
        }
        return String(value).slice(0, 10);
    }

    /*
     * Number display fallback to avoid NaN appearing in statistics cards.
     */
    function formatNumber(value, fallback) {
        var number = Number(value);
        if (!Number.isFinite(number)) {
            return typeof fallback === "string" ? fallback : "0";
        }
        return String(number);
    }

    /*
     * Normalize status to uppercase, consistent with backend enum and frontend CSS class judgment.
     */
    function normalizeStatus(value) {
        return safeText(value).trim().toUpperCase();
    }

    namespace.api = Object.assign({}, namespace.api, {
        request: request,
        redirectToLogin: redirectToLogin,
        parseJson: parseJson
    });
    namespace.dom = Object.assign({}, namespace.dom, {
        escapeHtml: escapeHtml,
        safeText: safeText
    });
    namespace.format = Object.assign({}, namespace.format, {
        date: formatDate,
        dateTime: formatDateTime,
        number: formatNumber,
        status: normalizeStatus
    });
    namespace.routes = Object.assign({}, namespace.routes, {
        // auth routes shared by login, register, logout, and availability check pages.
        auth: {
            login: function () {
                return apiPath("/api/auth/login");
            },
            register: function () {
                return apiPath("/api/auth/register");
            },
            logout: function () {
                return apiPath("/api/auth/logout");
            },
            availability: function (type, value) {
                return appendQuery(apiPath("/api/auth/availability"), {
                    type: type,
                    value: value
                });
            }
        },
        // jobs routes cover TA job list/detail and MO posting management.
        jobs: {
            list: function (query) {
                return appendQuery(apiPath("/api/jobs"), query);
            },
            detail: function (jobId) {
                return apiPath("/api/jobs/" + encodeURIComponent(jobId));
            },
            item: function (jobId) {
                return this.detail(jobId);
            }
        },
        // applications routes cover application list, status transitions, and applicant profile attachment sub-resources.
        applications: {
            list: function (query) {
                return appendQuery(apiPath("/api/applications"), query);
            },
            detail: function (applicationId) {
                return apiPath("/api/applications/" + encodeURIComponent(applicationId));
            },
            create: function () {
                return apiPath("/api/applications");
            },
            transition: function (applicationId) {
                return apiPath("/api/applications/" + encodeURIComponent(applicationId) + "/transition");
            },
            applicant: function (applicationId) {
                return apiPath("/api/applications/" + encodeURIComponent(applicationId) + "/" + "applicant");
            },
            applicantResume: function (applicationId) {
                return apiPath("/api/applications/" + encodeURIComponent(applicationId) + "/" + "applicant/resume");
            },
            applicantPhoto: function (applicationId) {
                return apiPath("/api/applications/" + encodeURIComponent(applicationId) + "/" + "applicant/photo");
            }
        },
        // me routes only handle current logged-in user profile and TA profile attachments.
        me: {
            account: function () {
                return apiPath("/api/me/account");
            },
            avatar: function () {
                return apiPath("/api/me/avatar");
            },
            applicantProfile: function () {
                return apiPath("/api/me/applicant-profile");
            },
            applicantPhoto: function () {
                return apiPath("/api/me/applicant-profile/photo");
            },
            applicantResume: function () {
                return apiPath("/api/me/applicant-profile/resume");
            },
            resumeDraft: function () {
                return apiPath("/api/me/applicant-profile/resume-draft");
            }
        },
        // admin routes cover statistics and invitation code management pages.
        admin: {
            workloadStatistics: function (query) {
                return appendQuery(apiPath("/api/admin/workload-statistics"), query);
            },
            invitationAcceptance: function () {
                return apiPath("/api/admin/invitations/acceptance");
            },
            currentInvitationCode: function () {
                return apiPath("/api/admin/invitations/current-code");
            }
        },
        // mo routes cover MO-side applicant recommendations.
        mo: {
            applicantRecommendations: function () {
                return apiPath("/api/mo/applicant-recommendations");
            }
        },
        // ta routes cover TA-side job recommendations.
        ta: {
            jobRecommendations: function () {
                return apiPath("/api/ta/job-recommendations");
            }
        },
        notifications: function () {
            return apiPath("/api/notifications");
        }
    });

    window.TARecruitment = namespace;
})(window);

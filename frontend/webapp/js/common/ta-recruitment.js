/*
 * TARecruitment 公共前端工具。
 *
 * 作用：
 * - 统一根据 window.APP_CONTEXT_PATH 生成 /api/... 路径，避免页面 JS 手写旧接口；
 * - 包装 fetch 响应解析和未登录跳转；
 * - 提供少量文本/日期/HTML 转义工具给各角色页面复用。
 */
(function (window) {
    "use strict";

    var namespace = window.TARecruitment || {};

    /*
     * 解析后端统一 JSON 响应。
     * strictJson=true 时保留解析异常，方便页面把非 JSON 响应当成错误处理。
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
     * 全站 fetch 包装。
     * 返回 { response, payload }，让页面可以同时判断 HTTP 状态码和业务 success。
     */
    function request(url, options, meta) {
        var settings = options || {};
        var requestMeta = meta || {};
        // parser 允许少数页面自定义解析，但默认仍走本文件的 JSON 解析规则。
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
     * 登录失效统一跳转入口。
     * 页面本地 request 包装可以调用它，避免散落不同的 login.jsp 拼接方式。
     */
    function redirectToLogin(delayMs) {
        window.setTimeout(function () {
            window.location.href = resolveContextPath() + "/login.jsp";
        }, typeof delayMs === "number" ? delayMs : 900);
    }

    /*
     * 解析部署 context path。
     * 优先使用 JSP 注入的 APP_CONTEXT_PATH，其次兼容旧 window.contextPath 和 meta 标签。
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
     * 给后端 API 路径补上 context path。
     * 这里接收的 path 必须是 /api/...，不要传旧 /jobs、/apply 等根路径。
     */
    function apiPath(path) {
        return resolveContextPath() + path;
    }

    /*
     * 追加查询参数，自动跳过空值。
     * 页面传入的 query 对象只表达有效筛选条件。
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
     * 文本工具：把 null/undefined 转为空字符串或指定 fallback。
     */
    function safeText(value, fallback) {
        if (value === null || typeof value === "undefined") {
            return typeof fallback === "string" ? fallback : "";
        }
        return String(value);
    }

    /*
     * HTML 转义工具，供手写 innerHTML 的页面渲染函数复用。
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
     * 轻量时间格式化：后端 LocalDateTime 字符串转页面短时间。
     */
    function formatDateTime(value, fallback) {
        if (!value) {
            return typeof fallback === "string" ? fallback : "-";
        }
        return String(value).replace("T", " ").slice(0, 16);
    }

    /*
     * 轻量日期格式化：只展示 yyyy-MM-dd。
     */
    function formatDate(value, fallback) {
        if (!value) {
            return typeof fallback === "string" ? fallback : "-";
        }
        return String(value).slice(0, 10);
    }

    /*
     * 数字展示兜底，避免 NaN 直接出现在统计卡片。
     */
    function formatNumber(value, fallback) {
        var number = Number(value);
        if (!Number.isFinite(number)) {
            return typeof fallback === "string" ? fallback : "0";
        }
        return String(number);
    }

    /*
     * 状态统一大写，和后端枚举/前端 CSS class 判断保持一致。
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
        // auth 路由供登录、注册、退出、可用性检查页面共用。
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
        // jobs 路由覆盖 TA 职位列表/详情和 MO 发布管理。
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
        // applications 路由覆盖申请列表、状态流转和申请人资料附件子资源。
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
        // me 路由只处理当前登录用户资料和 TA 档案附件。
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
        // admin 路由覆盖统计页和邀请码管理页。
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
        // mo 路由覆盖 MO 侧申请人推荐。
        mo: {
            applicantRecommendations: function () {
                return apiPath("/api/mo/applicant-recommendations");
            }
        },
        // ta 路由覆盖 TA 侧职位推荐。
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

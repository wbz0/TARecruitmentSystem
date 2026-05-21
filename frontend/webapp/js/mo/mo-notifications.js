/*
 * MO 通知页脚本，对应 /jsp/mo/notifications.jsp。
 *
 * 只读 /api/notifications 并渲染公告列表；发布和删除只存在 Admin 通知页。
 */
(function () {
    "use strict";

    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var t = window.AppI18n ? window.AppI18n.t.bind(window.AppI18n) : function (k, fb) { return fb || k; };

    var listEl = document.getElementById("notifications-list");

    function formatDate(iso) {
        if (!iso) return "";
        try {
            var d = new Date(iso.replace("T", " "));
            return d.toLocaleString();
        } catch (e) {
            return iso;
        }
    }

    function escapeHtml(str) {
        if (!str) return "";
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function renderList(notifications) {
        if (!listEl) return;
        listEl.innerHTML = "";

        if (!notifications || notifications.length === 0) {
            var empty = document.createElement("div");
            empty.className = "notifications-empty";
            empty.innerHTML = "<p class=\"notifications-empty-text\" data-i18n=\"portal.notifications.empty\">" +
                escapeHtml(t("portal.notifications.empty", "No announcements yet")) + "</p>";
            listEl.appendChild(empty);
            return;
        }

        notifications.forEach(function (n) {
            var card = document.createElement("article");
            card.className = "notification-card";
            card.innerHTML =
                "<div class=\"notification-card-head\">" +
                    "<h3 class=\"notification-title\">" + escapeHtml(n.title) + "</h3>" +
                "</div>" +
                "<p class=\"notification-content\">" + escapeHtml(n.content) + "</p>" +
                "<p class=\"notification-meta\">" +
                    escapeHtml(t("portal.notifications.publishedBy", "Published by")) + " " +
                    escapeHtml(n.publishedByUsername) + " · " +
                    escapeHtml(formatDate(n.publishedAt)) +
                "</p>";
            listEl.appendChild(card);
        });
    }

    function loadNotifications() {
        fetch(window.TARecruitment.routes.notifications(), {
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            renderList(data && data.data ? data.data : []);
        })
        .catch(function () {
            renderList([]);
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        t = window.AppI18n ? window.AppI18n.t.bind(window.AppI18n) : t;
        loadNotifications();
    });

    document.addEventListener("app:locale-changed", function () {
        t = window.AppI18n ? window.AppI18n.t.bind(window.AppI18n) : t;
        loadNotifications();
    });
})();

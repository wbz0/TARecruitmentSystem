/*
 * Admin 通知页脚本，对应 /jsp/admin/notifications.jsp。
 *
 * 读取全站公告列表，并允许 ADMIN 通过 /api/notifications 发布和删除公告。
 * TA/MO 通知页共用同一个读接口，但没有发布/删除表单。
 */
(function () {
    "use strict";

    var contextPath = typeof window.APP_CONTEXT_PATH === "string" ? window.APP_CONTEXT_PATH : "";
    var t = window.AppI18n ? window.AppI18n.t.bind(window.AppI18n) : function (k, fb) { return fb || k; };

    function localizeServerMessage(message, fallbackKey, fallbackText) {
        if (window.AppI18n && typeof window.AppI18n.localizeServerMessage === "function") {
            return window.AppI18n.localizeServerMessage(message, fallbackKey, fallbackText);
        }
        if (typeof message === "string" && message.trim()) {
            return message.trim();
        }
        return fallbackKey ? t(fallbackKey, fallbackText) : (fallbackText || "");
    }

    var listEl     = document.getElementById("notifications-list");
    var form       = document.getElementById("compose-form");
    var titleInput = document.getElementById("compose-title");
    var contentInput = document.getElementById("compose-content");
    var msgEl      = document.getElementById("compose-message");

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

    function showMessage(text, isError) {
        if (!msgEl) return;
        msgEl.textContent = text;
        msgEl.className = "compose-message form-message " + (isError ? "is-error" : "is-success");
        msgEl.classList.remove("hidden");
        setTimeout(function () { msgEl.classList.add("hidden"); }, 4000);
    }

    function renderList(notifications) {
        if (!listEl) return;
        listEl.innerHTML = "";

        if (!notifications || notifications.length === 0) {
            var empty = document.createElement("div");
            empty.className = "notifications-empty";
            empty.innerHTML = "<p class=\"notifications-empty-text\">" +
                escapeHtml(t("portal.notifications.empty", "No announcements yet")) + "</p>";
            listEl.appendChild(empty);
            return;
        }

        notifications.forEach(function (n) {
            var card = document.createElement("article");
            card.className = "notification-card";
            card.dataset.id = n.notificationId;
            card.innerHTML =
                "<div class=\"notification-card-head\">" +
                    "<h3 class=\"notification-title\">" + escapeHtml(n.title) + "</h3>" +
                    "<button class=\"notification-delete-btn\" aria-label=\"" +
                        escapeHtml(t("portal.notifications.deleteBtn", "Delete")) + "\" title=\"" +
                        escapeHtml(t("portal.notifications.deleteBtn", "Delete")) + "\">×</button>" +
                "</div>" +
                "<p class=\"notification-content\">" + escapeHtml(n.content) + "</p>" +
                "<p class=\"notification-meta\">" +
                    escapeHtml(t("portal.notifications.publishedBy", "Published by")) + " " +
                    escapeHtml(n.publishedByUsername) + " · " +
                    escapeHtml(formatDate(n.publishedAt)) +
                "</p>";

            var deleteBtn = card.querySelector(".notification-delete-btn");
            if (deleteBtn) {
                deleteBtn.addEventListener("click", function () {
                    deleteNotification(n.notificationId);
                });
            }

            listEl.appendChild(card);
        });
    }

    function loadNotifications() {
        // NotificationServlet 仍返回 data 数组，前端保持按数组渲染。
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

    function deleteNotification(id) {
        fetch(window.TARecruitment.routes.notifications() + "?notificationId=" + encodeURIComponent(id), {
            method: "DELETE",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (data && data.success) {
                loadNotifications();
            } else {
                showMessage(localizeServerMessage(data && data.message, "portal.notifications.deleteFailed", "Failed to delete notification."), true);
            }
        })
        .catch(function () {
            showMessage(t("portal.notifications.networkError", "Network error."), true);
        });
    }

    function bindForm() {
        if (!form) return;
        form.addEventListener("submit", function (e) {
            e.preventDefault();
            var title   = titleInput ? titleInput.value.trim() : "";
            var content = contentInput ? contentInput.value.trim() : "";
            if (!title || !content) {
                showMessage(t("portal.notifications.fillAll", "Please fill in both title and message."), true);
                return;
            }

            var params = new URLSearchParams();
            params.append("title", title);
            params.append("content", content);

            fetch(window.TARecruitment.routes.notifications(), {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: params.toString()
            })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data && data.success) {
                    if (titleInput)   titleInput.value = "";
                    if (contentInput) contentInput.value = "";
                    showMessage(t("portal.notifications.published", "Notification published."), false);
                    loadNotifications();
                } else {
                    showMessage(localizeServerMessage(data && data.message, "portal.notifications.publishFailed", "Failed to publish notification."), true);
                }
            })
            .catch(function () {
                showMessage(t("portal.notifications.networkError", "Network error."), true);
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        t = window.AppI18n ? window.AppI18n.t.bind(window.AppI18n) : t;
        loadNotifications();
        bindForm();
    });

    document.addEventListener("app:locale-changed", function () {
        t = window.AppI18n ? window.AppI18n.t.bind(window.AppI18n) : t;
        loadNotifications();
    });
})();

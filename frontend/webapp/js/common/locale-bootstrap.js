(function () {
    // 在 i18n.js 加载前先给 <html> 标记语言，避免中文页面首屏闪英文。
    var STORAGE_KEY = "ta_hiring_locale";
    var CHINESE_LOCALE = "zh-CN";
    var root = document.documentElement;

    /*
     * 把浏览器或 localStorage 中的语言值折叠到项目支持的语言集合。
     */
    function normalizeLocale(input) {
        if (typeof input !== "string" || !input.trim()) {
            return "";
        }
        var normalized = input.trim().toLowerCase();
        if (normalized === "en" || normalized.indexOf("en-") === 0) {
            return "en";
        }
        if (normalized === "zh" || normalized === "zh-cn" || normalized.indexOf("zh-") === 0) {
            return CHINESE_LOCALE;
        }
        return "";
    }

    /*
     * i18n.js 加载前先读本地偏好，减少首屏语言闪烁。
     */
    function readSavedLocale() {
        try {
            return normalizeLocale(window.localStorage.getItem(STORAGE_KEY) || "");
        } catch (error) {
            // 隐私模式或浏览器策略禁用 localStorage 时，继续回退到浏览器语言。
            return "";
        }
    }

    /*
     * 没有本地偏好时从浏览器语言推断。
     */
    function readBrowserLocale() {
        var languages = [];
        if (Array.isArray(window.navigator.languages)) {
            languages = window.navigator.languages.slice();
        }
        if (typeof window.navigator.language === "string" && window.navigator.language) {
            languages.push(window.navigator.language);
        }
        for (var i = 0; i < languages.length; i += 1) {
            var locale = normalizeLocale(languages[i]);
            if (locale) {
                return locale;
            }
        }
        return "";
    }

    var locale = readSavedLocale() || readBrowserLocale() || "en";
    root.setAttribute("lang", locale === CHINESE_LOCALE ? CHINESE_LOCALE : "en");
    root.setAttribute("data-initial-locale", locale);

    if (locale === CHINESE_LOCALE) {
        // 中文资源稍后由 i18n.js 接管；短暂 pending 类用于隐藏首屏翻译闪烁。
        root.classList.add("i18n-pending");
        window.setTimeout(function () {
            root.classList.remove("i18n-pending");
        }, 1600);
    }
})();

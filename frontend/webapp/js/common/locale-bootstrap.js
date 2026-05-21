(function () {
    // Set language on <html> before i18n.js loads to prevent Chinese page flash to English on first render.
    var STORAGE_KEY = "ta_hiring_locale";
    var CHINESE_LOCALE = "zh-CN";
    var root = document.documentElement;

    /*
     * Fold browser or localStorage language value to the set of languages supported by the project.
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
     * Read local preference before i18n.js loads to reduce first-screen language flash.
     */
    function readSavedLocale() {
        try {
            return normalizeLocale(window.localStorage.getItem(STORAGE_KEY) || "");
        } catch (error) {
            // When privacy mode or browser policy disables localStorage, continue fallback to browser language.
            return "";
        }
    }

    /*
     * Infer from browser language when no local preference is available.
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
        // Chinese resources will be taken over by i18n.js later; brief pending class hides first-screen translation flash.
        root.classList.add("i18n-pending");
        window.setTimeout(function () {
            root.classList.remove("i18n-pending");
        }, 1600);
    }
})();
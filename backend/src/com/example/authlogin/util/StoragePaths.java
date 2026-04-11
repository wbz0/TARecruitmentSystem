package com.example.authlogin.util;

import java.nio.file.Paths;

/**
 * StoragePaths - 统一管理运行时数据目录
 * 优先使用显式配置，其次使用 Tomcat 的 catalina.base，最后回退到当前工作目录。
 */
public final class StoragePaths {

    private static final String DATA_DIR_PROPERTY = "ta.hiring.data.dir";
    private static final String DATA_DIR_ENV = "TA_HIRING_DATA_DIR";
    private static final String APP_NAME = "groupproject";

    private StoragePaths() {
    }

    public static String getDataDir() {
        String configuredDir = firstNonBlank(
                System.getProperty(DATA_DIR_PROPERTY),
                System.getenv(DATA_DIR_ENV)
        );

        if (configuredDir != null) {
            return configuredDir;
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
            return Paths.get(catalinaBase, "data", APP_NAME).toString();
        }

        return Paths.get(System.getProperty("user.dir"), "data").toString();
    }

    public static String getResumeDir() {
        return Paths.get(getDataDir(), "resumes").toString();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}

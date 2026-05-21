package com.example.tarecruitment.common.storage;

import java.nio.file.Paths;

/**
 * StoragePaths - Unified management of runtime data directory.
 *
 * Configuration: Set TA_HIRING_DATA_DIR via scripts/config.sh or scripts/config.bat.
 * Code only reads this environment variable, does not write runtime data into the repository.
 *
 * Data directory structure:
 * ${TA_HIRING_DATA_DIR}/
 * ├── users/
 * ├── jobs/
 * ├── applicants/
 * ├── applications/
 * ├── invites/
 * ├── resumes/
 * ├── photos/
 * └── account-avatars/
 */
public final class StoragePaths {

    private static final String DATA_DIR_ENV = "TA_HIRING_DATA_DIR";

    private StoragePaths() {
    }

    /**
     * Get data root directory
     * TA_HIRING_DATA_DIR environment variable must be configured via script.
     */
    public static String getDataDir() {
        String dataDir = System.getenv(DATA_DIR_ENV);
        if (dataDir != null && !dataDir.trim().isEmpty()) {
            return dataDir.trim();
        }
        throw new IllegalStateException(
            "Data directory not configured. Set TA_HIRING_DATA_DIR environment variable in config.bat.\n" +
            "Example: set TA_HIRING_DATA_DIR=%CATALINA_HOME%\\data"
        );
    }

    public static String getUsersDir() {
        return Paths.get(getDataDir(), "users").toString();
    }

    public static String getApplicantsDir() {
        return Paths.get(getDataDir(), "applicants").toString();
    }

    public static String getJobsDir() {
        return Paths.get(getDataDir(), "jobs").toString();
    }

    public static String getApplicationsDir() {
        return Paths.get(getDataDir(), "applications").toString();
    }

    public static String getInvitesDir() {
        return Paths.get(getDataDir(), "invites").toString();
    }

    public static String getResumeDir() {
        return Paths.get(getDataDir(), "resumes").toString();
    }

    public static String getResumeDraftDir() {
        // Draft dir is only used for current TA profile editing flow; not exposed as public resume file.
        return Paths.get(getDataDir(), "resume-drafts").toString();
    }

    public static String getPhotoDir() {
        return Paths.get(getDataDir(), "photos").toString();
    }

    public static String getPhotoDraftDir() {
        // Draft dir is only used for avatar/photo editing flow; formal resources are still in photos.
        return Paths.get(getDataDir(), "photo-drafts").toString();
    }

    public static String getAccountAvatarDir() {
        return Paths.get(getDataDir(), "account-avatars").toString();
    }

    public static String getNotificationsDir() {
        return Paths.get(getDataDir(), "notifications").toString();
    }
}

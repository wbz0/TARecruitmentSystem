package com.example.tarecruitment.common.api;

public final class ApiRoutes {

    /*
     * API paths are centralized here. Frontend must generate URLs via TARecruitment.routes.
     * When deployed to context paths like /groupproject, root path is not hardcoded.
     */

    // Auth endpoints: login, register, logout, and username/email availability check share these.
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String AUTH_REGISTER = "/api/auth/register";
    public static final String AUTH_LOGOUT = "/api/auth/logout";
    public static final String AUTH_AVAILABILITY = "/api/auth/availability";

    // Job and application main resources; details and sub-actions are parsed by Servlet after these root paths via pathInfo.
    public static final String JOBS = "/api/jobs";
    public static final String APPLICATIONS = "/api/applications";

    // Current logged-in user profile endpoints; used by shared sidebar, TA dashboard and file upload/preview.
    public static final String ME_ACCOUNT = "/api/me/account";
    public static final String ME_AVATAR = "/api/me/avatar";
    public static final String ME_APPLICANT_PROFILE = "/api/me/applicant-profile";
    public static final String ME_APPLICANT_RESUME_DRAFT = "/api/me/applicant-profile/resume-draft";
    public static final String ME_APPLICANT_PHOTO = "/api/me/applicant-profile/photo";
    public static final String ME_APPLICANT_RESUME = "/api/me/applicant-profile/resume";

    // Notification endpoints; TA/MO/Admin notification pages share the same Servlet.
    public static final String NOTIFICATIONS = "/api/notifications";

    // Admin dashboard endpoints: stats cards, short invitation code management and invitation registration.
    public static final String ADMIN_WORKLOAD_STATISTICS = "/api/admin/workload-statistics";
    public static final String ADMIN_INVITATION_ACCEPTANCE = "/api/admin/invitations/acceptance";
    public static final String ADMIN_CURRENT_INVITATION_CODE = "/api/admin/invitations/current-code";

    // MO side AI endpoint: applicant recommendations.
    public static final String MO_APPLICANT_RECOMMENDATIONS = "/api/mo/applicant-recommendations";

    // TA side AI endpoint: job recommendation search.
    public static final String TA_JOB_RECOMMENDATIONS = "/api/ta/job-recommendations";

    private ApiRoutes() {
    }
}

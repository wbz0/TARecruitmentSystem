package com.example.tarecruitment.common.api;

public final class ApiRoutes {

    /*
     * API 路径集中放在这里，前端必须通过 TARecruitment.routes 生成 URL。
     * 这样应用部署到 /groupproject 等 context path 时，不会写死根路径。
     */

    // 认证相关接口：登录页、注册页、退出登录和用户名/邮箱可用性检查共用。
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String AUTH_REGISTER = "/api/auth/register";
    public static final String AUTH_LOGOUT = "/api/auth/logout";
    public static final String AUTH_AVAILABILITY = "/api/auth/availability";

    // 职位与申请主资源；详情和子动作由 Servlet 在这两个根路径后解析 pathInfo。
    public static final String JOBS = "/api/jobs";
    public static final String APPLICATIONS = "/api/applications";

    // 当前登录用户资料接口，供共享侧边栏、TA dashboard 和附件上传/预览使用。
    public static final String ME_ACCOUNT = "/api/me/account";
    public static final String ME_AVATAR = "/api/me/avatar";
    public static final String ME_APPLICANT_PROFILE = "/api/me/applicant-profile";
    public static final String ME_APPLICANT_RESUME_DRAFT = "/api/me/applicant-profile/resume-draft";
    public static final String ME_APPLICANT_PHOTO = "/api/me/applicant-profile/photo";
    public static final String ME_APPLICANT_RESUME = "/api/me/applicant-profile/resume";

    // 站内通知接口，TA/MO/Admin 三类通知页共用同一个 Servlet。
    public static final String NOTIFICATIONS = "/api/notifications";

    // Admin 工作台接口：统计卡片、短邀请码管理和邀请注册。
    public static final String ADMIN_WORKLOAD_STATISTICS = "/api/admin/workload-statistics";
    public static final String ADMIN_INVITATION_ACCEPTANCE = "/api/admin/invitations/acceptance";
    public static final String ADMIN_CURRENT_INVITATION_CODE = "/api/admin/invitations/current-code";

    // MO 侧 AI 接口：申请人推荐。
    public static final String MO_APPLICANT_RECOMMENDATIONS = "/api/mo/applicant-recommendations";

    // TA 侧 AI 接口：职位推荐搜索。
    public static final String TA_JOB_RECOMMENDATIONS = "/api/ta/job-recommendations";

    private ApiRoutes() {
    }
}

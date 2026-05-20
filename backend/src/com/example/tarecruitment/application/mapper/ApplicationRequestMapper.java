package com.example.tarecruitment.application.mapper;

/**
 * 解析 `/api/applications...` 后面的资源路径。
 *
 * ApplicationServlet 只根据这里判断集合、详情、状态流转和申请人资源子路径，
 * 避免在 Servlet 里散落字符串判断。这里不做权限校验，权限在 service 层处理。
 */
public final class ApplicationRequestMapper {

    private ApplicationRequestMapper() {
    }

    /**
     * 读取路径中的 applicationId，也就是 /api/applications/{applicationId} 的第一段。
     */
    public static String applicationId(String pathInfo) {
        String[] segments = segments(pathInfo);
        return segments.length > 0 ? segments[0] : "";
    }

    /**
     * 是否访问申请集合资源：/api/applications。
     */
    public static boolean isCollection(String pathInfo) {
        return applicationId(pathInfo).isEmpty();
    }

    /**
     * 是否访问单条申请详情：/api/applications/{applicationId}。
     */
    public static boolean isDetail(String pathInfo) {
        return segments(pathInfo).length == 1;
    }

    /**
     * 是否访问状态流转子资源：/api/applications/{applicationId}/transition。
     */
    public static boolean isTransition(String pathInfo) {
        String[] segments = segments(pathInfo);
        return segments.length == 2 && "transition".equals(segments[1]);
    }

    /**
     * 是否访问申请人资料快照：/api/applications/{applicationId}/applicant。
     */
    public static boolean isApplicantDetail(String pathInfo) {
        String[] segments = segments(pathInfo);
        return segments.length == 2 && "applicant".equals(segments[1]);
    }

    public static boolean isApplicantResume(String pathInfo) {
        // 当前 MO/TA 申请详情页会使用这个子资源预览申请人的简历文件。
        String[] segments = segments(pathInfo);
        return segments.length == 3 && "applicant".equals(segments[1]) && "resume".equals(segments[2]);
    }

    public static boolean isApplicantPhoto(String pathInfo) {
        // 当前 MO/TA 申请详情页会使用这个子资源显示申请人的个人照片。
        String[] segments = segments(pathInfo);
        return segments.length == 3 && "applicant".equals(segments[1]) && "photo".equals(segments[2]);
    }

    /**
     * 把 Servlet pathInfo 拆成稳定路径段。
     *
     * 只处理当前 REST 风格路径，不解码业务 ID；业务 ID 的合法性由 validator 校验。
     */
    private static String[] segments(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
            return new String[0];
        }
        String trimmed = pathInfo.trim();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isEmpty()) {
            return new String[0];
        }
        return trimmed.split("/");
    }
}

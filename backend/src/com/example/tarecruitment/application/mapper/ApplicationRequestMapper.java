package com.example.tarecruitment.application.mapper;

/**
 * Parses resource paths after `/api/applications...`.
 *
 * ApplicationServlet only determines collection, detail, status transition and applicant resource subpaths here,
 * avoiding scattered string checks in the Servlet.
 * No permission validation here; permissions are handled in the service layer.
 */
public final class ApplicationRequestMapper {

    private ApplicationRequestMapper() {
    }

    /**
     * Read applicationId from path, which is the first segment of /api/applications/{applicationId}.
     */
    public static String applicationId(String pathInfo) {
        String[] segments = segments(pathInfo);
        return segments.length > 0 ? segments[0] : "";
    }

    /**
     * Whether accessing application collection resource: /api/applications.
     */
    public static boolean isCollection(String pathInfo) {
        return applicationId(pathInfo).isEmpty();
    }

    /**
     * Whether accessing single application detail: /api/applications/{applicationId}.
     */
    public static boolean isDetail(String pathInfo) {
        return segments(pathInfo).length == 1;
    }

    /**
     * Whether accessing status transition sub-resource: /api/applications/{applicationId}/transition.
     */
    public static boolean isTransition(String pathInfo) {
        String[] segments = segments(pathInfo);
        return segments.length == 2 && "transition".equals(segments[1]);
    }

    /**
     * Whether accessing applicant profile snapshot: /api/applications/{applicationId}/applicant.
     */
    public static boolean isApplicantDetail(String pathInfo) {
        String[] segments = segments(pathInfo);
        return segments.length == 2 && "applicant".equals(segments[1]);
    }

    public static boolean isApplicantResume(String pathInfo) {
        // Current MO/TA application detail page uses this sub-resource to preview applicant's resume file.
        String[] segments = segments(pathInfo);
        return segments.length == 3 && "applicant".equals(segments[1]) && "resume".equals(segments[2]);
    }

    public static boolean isApplicantPhoto(String pathInfo) {
        // Current MO/TA application detail page uses this sub-resource to display applicant's personal photo.
        String[] segments = segments(pathInfo);
        return segments.length == 3 && "applicant".equals(segments[1]) && "photo".equals(segments[2]);
    }

    /**
     * Split Servlet pathInfo into stable path segments.
     *
     * Only handles current REST-style paths; does not decode business IDs.
     * Business ID validity is validated by the validator.
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

package com.example.tarecruitment.job.mapper;

import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.search.FuzzySearchUtil;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.job.model.Job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JobResponseMapper - Conversion layer from job object to frontend JSON payload.
 *
 * Page JS only consumes field names output here; CSV details in Job model should not be directly exposed to frontend.
 */
public final class JobResponseMapper {

    private JobResponseMapper() {
    }

    /**
     * Detail page calculates job effective status with current time by default.
     */
    public static Map<String, Object> toPayload(Job job, long applicantCount, UserDao userDao) {
        return toPayload(job, applicantCount, LocalDateTime.now(), userDao);
    }

    /**
     * Convert single job to frontend JSON.
     *
     * referenceTime is used in testing or list batch conversion to fix time, avoiding inconsistency
     * from multiple LocalDateTime.now() calls producing inconsistent status.
     */
    public static Map<String, Object> toPayload(Job job, long applicantCount, LocalDateTime referenceTime, UserDao userDao) {
        Map<String, Object> data = new LinkedHashMap<>();
        Job.Status effectiveStatus = job.getEffectiveStatus(referenceTime);
        // status outputs effective status: OPEN past deadline displays as CLOSED on frontend.
        data.put("jobId", safeText(job.getJobId()));
        data.put("moId", safeText(job.getMoId()));
        data.put("moName", resolveMoDisplayName(job, userDao));
        data.put("title", safeText(job.getTitle()));
        data.put("courseCode", safeText(job.getCourseCode()));
        data.put("courseName", safeText(job.getCourseName()));
        data.put("description", safeText(job.getDescription()));
        data.put("requiredSkills", safeText(job.getRequiredSkillsAsString()));
        data.put("positions", job.getPositions());
        data.put("workload", safeText(job.getWorkload()));
        data.put("weeklyHours", job.getWeeklyHours());
        data.put("workStartDate", job.getWorkStartDate() != null ? job.getWorkStartDate().toString() : "");
        data.put("workEndDate", job.getWorkEndDate() != null ? job.getWorkEndDate().toString() : "");
        data.put("salary", safeText(job.getSalary()));
        data.put("deadline", job.getDeadline() != null ? job.getDeadline().toString() : "");
        data.put("status", effectiveStatus.name());
        if (applicantCount >= 0) {
            data.put("applicantCount", applicantCount);
        }
        return data;
    }

    /**
     * Convert job list payload.
     *
     * jobs array is main page data; keywordApplied/approximateOnly/hasMatches
     * only used for search prompts, do not change list structure.
     */
    public static Map<String, Object> toListPayload(List<Job> jobs,
                                                     FuzzySearchUtil.SearchOutcome<Job> searchOutcome,
                                                     LocalDateTime referenceTime,
                                                     ApplicationDao applicationDao,
                                                     UserDao userDao) {
        List<Map<String, Object>> jobPayloads = new ArrayList<>();
        for (Job job : jobs) {
            // List page also shows applicant count, so add applicationDao count here.
            long applicantCount = applicationDao.countByJobId(job.getJobId());
            jobPayloads.add(toPayload(job, applicantCount, referenceTime, userDao));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jobs", jobPayloads);
        data.put("total", jobs.size());
        // Search metadata gives frontend “no exact match/spell check” hint; does not affect jobs array format.
        data.put("keywordApplied", searchOutcome != null && searchOutcome.isKeywordApplied());
        data.put("approximateOnly", searchOutcome != null && searchOutcome.isApproximateOnly());
        data.put("hasMatches", searchOutcome != null && searchOutcome.hasMatches());
        return data;
    }

    /**
     * Create/update API only needs to return jobId to frontend for subsequent refresh or redirect.
     */
    public static Map<String, Object> idPayload(Job job) {
        return ApiResponses.objectMap("jobId", job.getJobId());
    }

    /**
     * Build MO display name.
     *
     * Prefer using real name and professional title from account profile; old moName in CSV only as compatibility fallback.
     */
    public static String buildMoDisplayName(User user, String fallbackName) {
        if (user == null) {
            return fallbackName != null ? fallbackName : "";
        }
        // MO display name prefers realName+title to ensure TA sees teacher identity, not login username.
        String realName = safeText(user.getRealName()).trim();
        String professionalTitle = safeText(user.getProfessionalTitle()).trim();
        if (!realName.isEmpty()) {
            return professionalTitle.isEmpty() ? realName : professionalTitle + " " + realName;
        }
        String displayName = safeText(user.getDisplayName()).trim();
        if (!displayName.isEmpty()) {
            return displayName;
        }
        String storedName = safeText(fallbackName).trim();
        if (!storedName.isEmpty() && !storedName.equals(user.getUsername())) {
            return storedName;
        }
        return safeText(user.getUsername());
    }

    /**
     * Query latest account info by moId to avoid old names from job CSV showing for long.
     */
    private static String resolveMoDisplayName(Job job, UserDao userDao) {
        if (job == null) {
            return "";
        }
        return userDao.findById(job.getMoId())
                .map(user -> buildMoDisplayName(user, job.getMoName()))
                .orElse(job.getMoName());
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }
}

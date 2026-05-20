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
 * JobResponseMapper - 职位对象到前端 JSON payload 的转换层。
 *
 * 页面 JS 只消费这里输出的字段名；Job 模型里的 CSV 细节不要直接暴露给前端。
 */
public final class JobResponseMapper {

    private JobResponseMapper() {
    }

    /**
     * 详情页默认以当前时间计算职位生效状态。
     */
    public static Map<String, Object> toPayload(Job job, long applicantCount, UserDao userDao) {
        return toPayload(job, applicantCount, LocalDateTime.now(), userDao);
    }

    /**
     * 转换单个职位为前端 JSON。
     *
     * referenceTime 用于测试或列表批量转换时固定时间，避免同一批结果
     * 因多次 LocalDateTime.now() 产生不一致的状态。
     */
    public static Map<String, Object> toPayload(Job job, long applicantCount, LocalDateTime referenceTime, UserDao userDao) {
        Map<String, Object> data = new LinkedHashMap<>();
        Job.Status effectiveStatus = job.getEffectiveStatus(referenceTime);
        // status 输出生效状态：超过 deadline 的 OPEN 会在前端显示为 CLOSED。
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
     * 转换职位列表 payload。
     *
     * jobs 数组是页面主体数据；keywordApplied/approximateOnly/hasMatches
     * 只用于搜索提示，不改变列表结构。
     */
    public static Map<String, Object> toListPayload(List<Job> jobs,
                                                     FuzzySearchUtil.SearchOutcome<Job> searchOutcome,
                                                     LocalDateTime referenceTime,
                                                     ApplicationDao applicationDao,
                                                     UserDao userDao) {
        List<Map<String, Object>> jobPayloads = new ArrayList<>();
        for (Job job : jobs) {
            // 列表页也显示申请人数，因此这里补一次 applicationDao 计数。
            long applicantCount = applicationDao.countByJobId(job.getJobId());
            jobPayloads.add(toPayload(job, applicantCount, referenceTime, userDao));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jobs", jobPayloads);
        data.put("total", jobs.size());
        // 搜索元信息给前端决定“无精确匹配/拼写容错”提示，不影响 jobs 数组格式。
        data.put("keywordApplied", searchOutcome != null && searchOutcome.isKeywordApplied());
        data.put("approximateOnly", searchOutcome != null && searchOutcome.isApproximateOnly());
        data.put("hasMatches", searchOutcome != null && searchOutcome.hasMatches());
        return data;
    }

    /**
     * 创建/更新接口只需要把 jobId 返给前端用于后续刷新或跳转。
     */
    public static Map<String, Object> idPayload(Job job) {
        return ApiResponses.objectMap("jobId", job.getJobId());
    }

    /**
     * 构建 MO 展示名。
     *
     * 优先使用账号资料里的实名和职称；旧 CSV 中存的 moName 只作为兼容兜底。
     */
    public static String buildMoDisplayName(User user, String fallbackName) {
        if (user == null) {
            return fallbackName != null ? fallbackName : "";
        }
        // MO 展示名优先实名+职称，保证 TA 看到的是老师身份，而不是登录用户名。
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
     * 按 moId 查询最新账号资料，避免职位 CSV 里的旧名字长期显示。
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

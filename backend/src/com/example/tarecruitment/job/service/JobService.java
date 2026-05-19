package com.example.tarecruitment.job.service;

import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.search.FuzzySearchUtil;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.job.mapper.JobRequestMapper;
import com.example.tarecruitment.job.mapper.JobResponseMapper;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.job.validator.JobValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JobService - 职位业务服务。
 *
 * 被 JobServlet 调用，对外 API 是 /api/jobs 和 /api/jobs/{jobId}。
 * 这里负责列表筛选、MO 创建/更新/删除职位、状态生效规则和响应 payload 组装；
 * Servlet 只读取 HTTP 参数并把 ServiceResult 写回前端。
 */
public class JobService {

    private static JobService instance;

    private final JobDao jobDao;
    private final ApplicationDao applicationDao;
    private final UserDao userDao;

    private JobService() {
        this.jobDao = JobDao.getInstance();
        this.applicationDao = ApplicationDao.getInstance();
        this.userDao = UserDao.getInstance();
    }

    public static synchronized JobService getInstance() {
        if (instance == null) {
            instance = new JobService();
        }
        return instance;
    }

    /**
     * 查询职位列表，并把前端筛选条件统一应用到 CSV 数据上。
     *
     * 参数来自 TA 职位列表和 MO dashboard；返回 payload 会包含职位展示字段、
     * 模糊搜索元数据和申请人数等前端卡片需要的信息。
     */
    public ServiceResult list(String courseCode, String status, String keyword, String moId) {
        List<Job> jobs = jobDao.findAll();
        // effectiveNow 用于计算动态状态，例如截止时间过后自动显示 CLOSED。
        LocalDateTime effectiveNow = LocalDateTime.now();

        // 课程、状态、MO 三个筛选条件来自 TA 职位列表和 MO dashboard 的查询控件。
        String courseCodeText = trim(courseCode);
        if (!courseCodeText.isEmpty()) {
            jobs = jobs.stream()
                    .filter(j -> j.getCourseCode().equalsIgnoreCase(courseCodeText))
                    .collect(Collectors.toList());
        }

        String statusText = trim(status);
        if (!statusText.isEmpty()) {
            try {
                Job.Status jobStatus = Job.Status.valueOf(statusText.toUpperCase());
                jobs = jobs.stream()
                        .filter(j -> j.getEffectiveStatus(effectiveNow) == jobStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // 非法状态不报 500，直接返回空列表，前端可以按“没有结果”处理。
                jobs = new ArrayList<>();
            }
        }

        String moIdText = trim(moId);
        if (!moIdText.isEmpty()) {
            jobs = jobs.stream()
                    .filter(j -> j.getMoId().equals(moIdText))
                    .collect(Collectors.toList());
        }

        FuzzySearchUtil.SearchOutcome<Job> searchOutcome = jobDao.searchWithMetadata(keyword, jobs);
        // approximateOnly 会告诉前端：关键词有效，但没有精确命中，只返回拼写容错结果。
        List<Job> visibleJobs = searchOutcome.getItems();
        return ServiceResult.ok(
                "Jobs retrieved successfully",
                JobResponseMapper.toListPayload(visibleJobs, searchOutcome, effectiveNow, applicationDao, userDao)
        );
    }

    /**
     * 查询单个职位详情。
     *
     * 详情页额外展示 applicantCount，所以这里会跨 ApplicationDao 统计；
     * Job 模型本身仍只表示职位 CSV 数据。
     */
    public ServiceResult detail(String jobId) {
        String jobIdText = trim(jobId);
        if (jobIdText.isEmpty()) {
            return ServiceResult.badRequest("Job ID is required");
        }

        Optional<Job> jobOpt = jobDao.findById(jobIdText);
        if (jobOpt.isEmpty()) {
            return ServiceResult.notFound("Job not found");
        }

        Job job = jobOpt.get();
        long applicantCount = applicationDao.countByJobId(job.getJobId());
        // 详情页需要 applicantCount 展示竞争情况；它不是 Job CSV 的字段。
        return ServiceResult.ok("Job retrieved successfully", JobResponseMapper.toPayload(job, applicantCount, userDao));
    }

    /**
     * MO 创建职位。
     *
     * 该方法负责权限、字段校验、默认值补齐和 Job 模型组装；
     * Servlet 只传入白名单表单字段，避免 HTTP 细节进入业务层。
     */
    public ServiceResult create(User currentUser, Map<String, String> parameters) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }
        if (currentUser.getRole() != User.Role.MO) {
            return ServiceResult.forbidden("Only MO can post jobs");
        }

        // create 使用白名单字段，避免表单传入未知参数直接写进 CSV。
        String title = parameter(parameters, "title");
        String courseCode = parameter(parameters, "courseCode");
        String courseName = parameter(parameters, "courseName");
        String description = parameter(parameters, "description");
        String skills = parameter(parameters, "requiredSkills");
        String positionsStr = parameter(parameters, "positions");
        String weeklyHoursStr = parameter(parameters, "weeklyHours");
        String workStartDateStr = parameter(parameters, "workStartDate");
        String workEndDateStr = parameter(parameters, "workEndDate");
        String salary = parameter(parameters, "salary");
        String deadlineStr = parameter(parameters, "deadline");

        String error = JobValidator.validateCreate(title, courseCode, courseName, description, skills,
                positionsStr, weeklyHoursStr, workStartDateStr, workEndDateStr, salary, deadlineStr);
        if (error != null) {
            return ServiceResult.badRequest(error);
        }

        Job job = new Job();
        job.setMoId(currentUser.getUserId());
        // 存一份发布者展示名，后续账号资料改名时响应 mapper 仍会优先取最新账号名。
        job.setMoName(JobResponseMapper.buildMoDisplayName(currentUser, currentUser.getUsername()));
        job.setTitle(trim(title));
        job.setCourseCode(trim(courseCode));
        job.setCourseName(trim(courseName));
        job.setDescription(trim(description));
        job.setRequiredSkills(JobRequestMapper.normalizeSkillsToList(skills));

        String positionsText = trim(positionsStr);
        job.setPositions(positionsText.isEmpty() ? 1 : Integer.parseInt(positionsText));
        job.setWeeklyHours(JobRequestMapper.parseWeeklyHours(weeklyHoursStr));
        job.setWorkStartDate(LocalDate.parse(trim(workStartDateStr)));
        job.setWorkEndDate(LocalDate.parse(trim(workEndDateStr)));
        job.setSalary(trim(salary));

        LocalDateTime deadline = JobRequestMapper.parseDeadline(deadlineStr);
        if (deadline != null) {
            job.setDeadline(deadline);
        }

        Job savedJob = jobDao.create(job);
        return ServiceResult.created("Job created successfully!", JobResponseMapper.idPayload(savedJob));
    }

    /**
     * MO 更新自己发布的职位。
     *
     * 这里采用“只覆盖请求携带字段”的 PATCH 风格，虽然 HTTP method 是 PUT；
     * 前端编辑弹窗可以只提交有值字段，不会把未提交字段清空。
     */
    public ServiceResult update(User currentUser, String jobId, Map<String, String> parameters) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }
        String jobIdText = trim(jobId);
        if (jobIdText.isEmpty()) {
            return ServiceResult.badRequest("Job ID is required");
        }

        Optional<Job> jobOpt = jobDao.findById(jobIdText);
        if (jobOpt.isEmpty()) {
            return ServiceResult.notFound("Job not found");
        }

        Job job = jobOpt.get();
        if (!job.getMoId().equals(currentUser.getUserId())) {
            // Job 写操作只允许发布该职位的 MO，ADMIN 不能从这个业务接口代改。
            return ServiceResult.forbidden("You can only update your own jobs");
        }

        String error = applyUpdateFields(job, parameters);
        if (error != null) {
            return ServiceResult.badRequest(error);
        }

        job.setMoName(JobResponseMapper.buildMoDisplayName(currentUser, job.getMoName()));
        Job updatedJob = jobDao.update(job);
        return ServiceResult.ok("Job updated successfully!", JobResponseMapper.idPayload(updatedJob));
    }

    /**
     * MO 删除自己发布的职位。
     *
     * 删除前先读取职位确认归属，防止 MO 通过手写 jobId 删除他人岗位。
     */
    public ServiceResult delete(User currentUser, String jobId) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }
        String jobIdText = trim(jobId);
        if (jobIdText.isEmpty()) {
            return ServiceResult.badRequest("Job ID is required");
        }

        Optional<Job> jobOpt = jobDao.findById(jobIdText);
        if (jobOpt.isEmpty()) {
            return ServiceResult.notFound("Job not found");
        }

        Job job = jobOpt.get();
        if (!job.getMoId().equals(currentUser.getUserId())) {
            return ServiceResult.forbidden("You can only delete your own jobs");
        }

        boolean deleted = jobDao.delete(jobIdText);
        if (!deleted) {
            return ServiceResult.serverError("Failed to delete job");
        }
        return ServiceResult.ok("Job deleted successfully!", null);
    }

    /**
     * 把编辑弹窗提交的字段逐项应用到现有 Job。
     *
     * 每个字段先做单项校验，再写回模型；日期字段最后再整体校验，
     * 因为截止时间、开始时间、结束时间之间存在相互依赖。
     */
    private String applyUpdateFields(Job job, Map<String, String> parameters) {
        // PATCH 风格更新：只校验并覆盖本次请求携带的字段，空 map 不会清空职位。
        String title = parameter(parameters, "title");
        if (title != null) {
            String titleText = title.trim();
            String error = JobValidator.validateTitle(titleText, true);
            if (error != null) return error;
            job.setTitle(titleText);
        }

        String courseCode = parameter(parameters, "courseCode");
        if (courseCode != null) {
            String courseCodeText = courseCode.trim();
            String error = JobValidator.validateCourseCode(courseCodeText, true);
            if (error != null) return error;
            job.setCourseCode(courseCodeText);
        }

        String courseName = parameter(parameters, "courseName");
        if (courseName != null) {
            String courseNameText = courseName.trim();
            String error = JobValidator.validateCourseName(courseNameText, true);
            if (error != null) return error;
            job.setCourseName(courseNameText);
        }

        String description = parameter(parameters, "description");
        if (description != null) {
            String descriptionText = description.trim();
            String error = JobValidator.validateDescription(descriptionText, true);
            if (error != null) return error;
            job.setDescription(descriptionText);
        }

        String skills = parameter(parameters, "requiredSkills");
        if (skills != null) {
            String skillsText = skills.trim();
            String error = JobValidator.validateSkills(skillsText, true);
            if (error != null) return error;
            job.setRequiredSkills(JobRequestMapper.normalizeSkillsToList(skillsText));
        }

        String positions = parameter(parameters, "positions");
        if (positions != null) {
            String positionsText = positions.trim();
            String error = JobValidator.validatePositions(positionsText, true);
            if (error != null) return error;
            job.setPositions(Integer.parseInt(positionsText));
        }

        String weeklyHours = parameter(parameters, "weeklyHours");
        if (weeklyHours != null) {
            String weeklyHoursText = weeklyHours.trim();
            String error = JobValidator.validateWeeklyHours(weeklyHoursText, true);
            if (error != null) return error;
            job.setWeeklyHours(JobRequestMapper.parseWeeklyHours(weeklyHoursText));
        }

        String workStartDate = parameter(parameters, "workStartDate");
        if (workStartDate != null) {
            String workStartDateText = workStartDate.trim();
            String error = JobValidator.validateWorkDate(workStartDateText, "Work start date", true);
            if (error != null) return error;
            job.setWorkStartDate(LocalDate.parse(workStartDateText));
        }

        String workEndDate = parameter(parameters, "workEndDate");
        if (workEndDate != null) {
            String workEndDateText = workEndDate.trim();
            String error = JobValidator.validateWorkDate(workEndDateText, "Work end date", true);
            if (error != null) return error;
            job.setWorkEndDate(LocalDate.parse(workEndDateText));
        }

        String salary = parameter(parameters, "salary");
        if (salary != null) {
            String salaryText = salary.trim();
            String error = JobValidator.validateSalary(salaryText, true);
            if (error != null) return error;
            job.setSalary(salaryText);
        }

        String deadline = parameter(parameters, "deadline");
        if (deadline != null) {
            String deadlineText = deadline.trim();
            String error = JobValidator.validateDeadline(deadlineText, true);
            if (error != null) return error;
            LocalDateTime parsedDeadline = JobRequestMapper.parseDeadline(deadlineText);
            if (parsedDeadline == null) {
                return "Invalid deadline format";
            }
            job.setDeadline(parsedDeadline);
        }

        if (deadline != null || workStartDate != null || workEndDate != null) {
            // 只要改动任一日期，就用更新后的整体时间表重新校验一次前后关系。
            String error = JobValidator.validateWorkSchedule(job.getDeadline(), job.getWorkStartDate(), job.getWorkEndDate(), true);
            if (error != null) return error;
        }

        String status = parameter(parameters, "status");
        if (status != null) {
            String statusText = status.trim();
            String error = JobValidator.validateStatus(statusText, true);
            if (error != null) return error;
            job.setStatus(Job.Status.valueOf(statusText.toUpperCase()));
        }
        return null;
    }

    /**
     * 读取参数时保留 null 语义。
     *
     * null 表示“前端没有提交这个字段”，空字符串表示“提交了但值为空”，
     * 更新流程需要区分这两种情况。
     */
    private String parameter(Map<String, String> parameters, String name) {
        return parameters != null && parameters.containsKey(name) ? parameters.get(name) : null;
    }

    /**
     * 查询类参数统一按空字符串处理，便于筛选逻辑串联。
     */
    private String trim(String value) {
        return value != null ? value.trim() : "";
    }
}

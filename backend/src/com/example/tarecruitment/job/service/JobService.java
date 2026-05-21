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
 * JobService - Job business service.
 *
 * Called by JobServlet, external API is /api/jobs and /api/jobs/{jobId}.
 * Here responsible for list filtering, MO create/update/delete jobs, status effective rules and response payload assembly;
 * Servlet only reads HTTP parameters and writes ServiceResult back to frontend.
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
     * Query job list and uniformly apply frontend filter conditions to CSV data.
     *
     * Parameters come from TA job list and MO dashboard; returned payload contains job display fields,
     * fuzzy search metadata and applicant count that frontend cards need.
     */
    public ServiceResult list(String courseCode, String status, String keyword, String moId) {
        List<Job> jobs = jobDao.findAll();
        // effectiveNow is used for calculating dynamic status, e.g. auto-show CLOSED after deadline passes.
        LocalDateTime effectiveNow = LocalDateTime.now();

        // Course, status, MO three filter conditions come from TA job list and MO dashboard query controls.
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
                // Invalid status does not report 500; directly return empty list, frontend can treat as “no results”.
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
        // approximateOnly tells frontend: keyword valid but no exact match, only returns spell-checking results.
        List<Job> visibleJobs = searchOutcome.getItems();
        return ServiceResult.ok(
                "Jobs retrieved successfully",
                JobResponseMapper.toListPayload(visibleJobs, searchOutcome, effectiveNow, applicationDao, userDao)
        );
    }

    /**
     * Query single job detail.
     *
     * Detail page additionally shows applicantCount, so crosses ApplicationDao to count here;
     * Job model itself still only represents job CSV data.
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
        // Detail page needs applicantCount to show competition; not a Job CSV field.
        return ServiceResult.ok("Job retrieved successfully", JobResponseMapper.toPayload(job, applicantCount, userDao));
    }

    /**
     * MO creates job.
     *
     * This method is responsible for permission, field validation, default value filling and Job model assembly;
     * Servlet only passes whitelist form fields, avoiding HTTP details entering business layer.
     */
    public ServiceResult create(User currentUser, Map<String, String> parameters) {
        if (currentUser == null) {
            return ServiceResult.unauthorized("Please login first");
        }
        if (currentUser.getRole() != User.Role.MO) {
            return ServiceResult.forbidden("Only MO can post jobs");
        }

        // create uses whitelist fields to avoid unknown request parameters directly written to CSV.
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
        // Save a snapshot of publisher display name; when account profile changes later, response mapper still prefers latest account name.
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
     * MO updates their own published job.
     *
     * Here adopts “only overwrite fields carried by request” PATCH style, although HTTP method is PUT;
     * frontend edit dialog can submit only fields with values, will not clear unsubmitted fields.
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
            // Job write operation only allowed for MO who published the job; ADMIN cannot modify from this business interface.
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
     * MO deletes their own published job.
     *
     * Before delete, read job to confirm ownership, prevent MO from deleting others' jobs by writing jobId.
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
     * Apply fields submitted by edit dialog to existing Job item by item.
     *
     * Each field does single validation first, then writes back to model; date fields do overall validation last,
     * because deadline, start time, end time have interdependencies.
     */
    private String applyUpdateFields(Job job, Map<String, String> parameters) {
        // PATCH style update: only validate and overwrite fields carried by this request, empty map does not clear job.
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
            // If any date is changed, re-validate the overall schedule relationship once with updated timeline.
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
     * When reading parameters, retain null semantics.
     *
     * null means “frontend did not submit this field”, empty string means “submitted but value is empty”,
     * update flow needs to distinguish these two cases.
     */
    private String parameter(Map<String, String> parameters, String name) {
        return parameters != null && parameters.containsKey(name) ? parameters.get(name) : null;
    }

    /**
     * Query parameters are uniformly processed as empty string for easy filter logic chaining.
     */
    private String trim(String value) {
        return value != null ? value.trim() : "";
    }
}

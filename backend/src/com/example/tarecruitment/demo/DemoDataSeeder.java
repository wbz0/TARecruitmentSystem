package com.example.tarecruitment.demo;

import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.profile.model.Applicant;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.storage.StoragePaths;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * On startup, supplement a set of demo data for local display, but do not repeat create existing records.
 *
 * Scope:
 * - Fixed demo accounts;
 * - TA profiles, jobs, applications;
 * - Simple PDF resume placeholder files.
 *
 * Here serves local teaching/demo, not user-visible admin management function.
 */
public final class DemoDataSeeder {

    private static final String DEFAULT_PASSWORD = "Pass1234";

    private static final List<DemoUserSpec> DEMO_USERS = List.of(
            new DemoUserSpec("demo-user-mo-alice", "mo_demo_alice", "mo_demo_alice@local.test", User.Role.MO),
            new DemoUserSpec("demo-user-mo-brian", "mo_demo_brian", "mo_demo_brian@local.test", User.Role.MO),
            new DemoUserSpec("demo-user-ta-mia", "ta_demo_mia", "ta_demo_mia@local.test", User.Role.TA),
            new DemoUserSpec("demo-user-ta-noah", "ta_demo_noah", "ta_demo_noah@local.test", User.Role.TA),
            new DemoUserSpec("demo-user-ta-olivia", "ta_demo_olivia", "ta_demo_olivia@local.test", User.Role.TA),
            new DemoUserSpec("demo-user-ta-liam", "ta_demo_liam", "ta_demo_liam@local.test", User.Role.TA)
    );

    private static final List<ApplicantSpec> APPLICANT_SPECS = List.of(
            new ApplicantSpec(
                    "demo-applicant-ta-core",
                    "ta_demo",
                    "Alex Chen",
                    "2023101001",
                    "Computer Science",
                    "Master",
                    "3.90 / 4.00",
                    List.of("Java", "JSP", "SQL", "Git", "Teaching"),
                    "resumes/demo-alex-chen-resume.pdf",
                    "+65 8123 1001",
                    "Block A, Graduate Residence",
                    "Supported database labs and graded weekly programming assignments.",
                    "I enjoy mentoring junior students and helping classes run smoothly."
            ),
            new ApplicantSpec(
                    "demo-applicant-ta-mia",
                    "ta_demo_mia",
                    "Mia Wong",
                    "2023101002",
                    "Information Systems",
                    "Master",
                    "3.84 / 4.00",
                    List.of("Python", "Data Analysis", "Communication", "Excel"),
                    "resumes/demo-mia-wong-resume.pdf",
                    "+65 8123 1002",
                    "Block B, Graduate Residence",
                    "Led tutorial consultations for analytics and statistics modules.",
                    "I want to support students with practical examples and patient guidance."
            ),
            new ApplicantSpec(
                    "demo-applicant-ta-noah",
                    "ta_demo_noah",
                    "Noah Patel",
                    "2023101003",
                    "Computer Engineering",
                    "PhD",
                    "3.95 / 4.00",
                    List.of("Algorithms", "Java", "C++", "Linux"),
                    "resumes/demo-noah-patel-resume.pdf",
                    "+65 8123 1003",
                    "Block C, Research Housing",
                    "Worked as a grader for advanced programming and algorithm courses.",
                    "I can help students understand both theory and implementation details."
            ),
            new ApplicantSpec(
                    "demo-applicant-ta-olivia",
                    "ta_demo_olivia",
                    "Olivia Lin",
                    "2023101004",
                    "Software Engineering",
                    "Undergraduate",
                    "3.78 / 4.00",
                    List.of("JavaScript", "HTML", "CSS", "UI Testing"),
                    "resumes/demo-olivia-lin-resume.pdf",
                    "+65 8123 1004",
                    "Student Residence East",
                    "Built frontend coursework demos and supported peer code reviews.",
                    "I would like to help students gain confidence in practical web development."
            ),
            new ApplicantSpec(
                    "demo-applicant-ta-liam",
                    "ta_demo_liam",
                    "Liam Zhao",
                    "2023101005",
                    "Data Science",
                    "Master",
                    "3.88 / 4.00",
                    List.of("SQL", "Statistics", "Data Visualization", "Reporting"),
                    "resumes/demo-liam-zhao-resume.pdf",
                    "+65 8123 1005",
                    "Innovation Hall",
                    "Prepared weekly dashboards and reviewed analytical lab submissions.",
                    "I can support students with clear explanations and structured feedback."
            )
    );

    private static final List<JobSpec> JOB_SPECS = List.of(
            new JobSpec(
                    "demo-job-se601",
                    "mo_demo",
                    "Dr. Morgan Lee",
                    "Teaching Assistant - Software Engineering Studio",
                    "DEMO-SE601",
                    "Software Engineering Studio",
                    "Support lab sessions, review pull requests, and assist with sprint demos.",
                    List.of("Java", "JSP", "SQL", "Git"),
                    2,
                    8.0,
                    0,
                    84,
                    "28 SGD / hour",
                    21,
                    Job.Status.OPEN
            ),
            new JobSpec(
                    "demo-job-db602",
                    "mo_demo",
                    "Dr. Morgan Lee",
                    "Teaching Assistant - Database Systems",
                    "DEMO-DB602",
                    "Database Systems",
                    "Help with SQL clinics, grading, and exam preparation sessions.",
                    List.of("SQL", "JDBC", "ER Modeling"),
                    1,
                    6.0,
                    -21,
                    35,
                    "30 SGD / hour",
                    14,
                    Job.Status.FILLED
            ),
            new JobSpec(
                    "demo-job-ai603",
                    "mo_demo_alice",
                    "Prof. Alice Carter",
                    "Teaching Assistant - Applied AI",
                    "DEMO-AI603",
                    "Applied AI",
                    "Guide tutorial discussions and support mini-project checkpoints.",
                    List.of("Python", "Machine Learning", "Presentation"),
                    2,
                    7.0,
                    7,
                    91,
                    "32 SGD / hour",
                    18,
                    Job.Status.OPEN
            ),
            new JobSpec(
                    "demo-job-web604",
                    "mo_demo_alice",
                    "Prof. Alice Carter",
                    "Teaching Assistant - Web Development",
                    "DEMO-WEB604",
                    "Web Development",
                    "Previously used demo posting for frontend office hours and grading.",
                    List.of("HTML", "CSS", "JavaScript", "Testing"),
                    1,
                    5.0,
                    -70,
                    -14,
                    "26 SGD / hour",
                    -3,
                    Job.Status.CLOSED
            ),
            new JobSpec(
                    "demo-job-alg605",
                    "mo_demo_brian",
                    "Dr. Brian Xu",
                    "Teaching Assistant - Algorithms",
                    "DEMO-ALG605",
                    "Algorithms",
                    "Support recitations, solution walkthroughs, and coding exercises.",
                    List.of("Algorithms", "Java", "C++"),
                    1,
                    6.0,
                    3,
                    73,
                    "31 SGD / hour",
                    16,
                    Job.Status.OPEN
            ),
            new JobSpec(
                    "demo-job-da606",
                    "mo_demo_brian",
                    "Dr. Brian Xu",
                    "Teaching Assistant - Data Analytics",
                    "DEMO-DA606",
                    "Data Analytics",
                    "Assist students with dashboards, data cleaning, and weekly reports.",
                    List.of("Python", "SQL", "Data Visualization"),
                    1,
                    8.0,
                    -14,
                    56,
                    "29 SGD / hour",
                    12,
                    Job.Status.FILLED
            )
    );

    private static final List<ApplicationSpec> APPLICATION_SPECS = List.of(
            new ApplicationSpec(
                    "demo-application-ta-core-se601",
                    "ta_demo",
                    "demo-job-se601",
                    Application.Status.PENDING,
                    "I can support sprint reviews, debugging sessions, and Git workflow questions.",
                    Application.ProgressStage.UNDER_REVIEW
            ),
            new ApplicationSpec(
                    "demo-application-ta-core-db602",
                    "ta_demo",
                    "demo-job-db602",
                    Application.Status.ACCEPTED,
                    "I have prior experience helping students with SQL labs and schema design.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-core-web604",
                    "ta_demo",
                    "demo-job-web604",
                    Application.Status.REJECTED,
                    "I can mentor beginner developers on frontend foundations and debugging.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-mia-se601",
                    "ta_demo_mia",
                    "demo-job-se601",
                    Application.Status.ACCEPTED,
                    "My communication skills help me translate complex concepts into simple examples.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-mia-ai603",
                    "ta_demo_mia",
                    "demo-job-ai603",
                    Application.Status.PENDING,
                    "I can guide project teams through data prep and model evaluation tasks.",
                    Application.ProgressStage.UNDER_REVIEW
            ),
            new ApplicationSpec(
                    "demo-application-ta-mia-alg605",
                    "ta_demo_mia",
                    "demo-job-alg605",
                    Application.Status.WITHDRAWN,
                    "I was interested in the role but decided to focus on analytics modules this term.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-noah-se601",
                    "ta_demo_noah",
                    "demo-job-se601",
                    Application.Status.REJECTED,
                    "I can help with systems design and code performance reviews.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-noah-ai603",
                    "ta_demo_noah",
                    "demo-job-ai603",
                    Application.Status.ACCEPTED,
                    "My research background lets me coach students on experiments and implementation.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-noah-da606",
                    "ta_demo_noah",
                    "demo-job-da606",
                    Application.Status.ACCEPTED,
                    "I can help students reason about analysis pipelines and reproducible results.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-olivia-ai603",
                    "ta_demo_olivia",
                    "demo-job-ai603",
                    Application.Status.PENDING,
                    "I would like to support student teams with demos, slides, and testing.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-olivia-alg605",
                    "ta_demo_olivia",
                    "demo-job-alg605",
                    Application.Status.PENDING,
                    "I am comfortable helping students debug implementations during lab hours.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-liam-web604",
                    "ta_demo_liam",
                    "demo-job-web604",
                    Application.Status.REJECTED,
                    "I can contribute structured feedback and support assignment moderation.",
                    null
            ),
            new ApplicationSpec(
                    "demo-application-ta-liam-se601",
                    "ta_demo_liam",
                    "demo-job-se601",
                    Application.Status.WITHDRAWN,
                    "I was initially available for this role but later adjusted my term workload.",
                    null
            )
    );

    private final UserDao userDao;
    private final ApplicantDao applicantDao;
    private final JobDao jobDao;
    private final ApplicationDao applicationDao;

    public DemoDataSeeder(UserDao userDao, ApplicantDao applicantDao, JobDao jobDao, ApplicationDao applicationDao) {
        this.userDao = userDao;
        this.applicantDao = applicantDao;
        this.jobDao = jobDao;
        this.applicationDao = applicationDao;
    }

    public static DemoDataSeeder createDefault() {
        return new DemoDataSeeder(
                UserDao.getInstance(),
                ApplicantDao.getInstance(),
                JobDao.getInstance(),
                ApplicationDao.getInstance()
        );
    }

    public SeedSummary seed() {
        // Order matters: applications depend on users, applicant profiles and jobs already existing.
        SeedSummary summary = new SeedSummary();
        List<User> users = ensureUsers(summary);
        List<Applicant> applicants = ensureApplicants(users, summary);
        List<Job> jobs = ensureJobs(users, summary);
        ensureApplications(users, applicants, jobs, summary);
        return summary;
    }

    private List<User> ensureUsers(SeedSummary summary) {
        List<User> users = new ArrayList<>();
        users.add(applyDemoAccountDisplay(userDao.findByUsername("admin_demo").orElseGet(() -> createUser(
                new DemoUserSpec("demo-user-admin-core", "admin_demo", "admin_demo@local.test", User.Role.ADMIN),
                summary
        ))));
        users.add(applyDemoAccountDisplay(userDao.findByUsername("mo_demo").orElseGet(() -> createUser(
                new DemoUserSpec("demo-user-mo-core", "mo_demo", "mo_demo@local.test", User.Role.MO),
                summary
        ))));
        users.add(applyDemoAccountDisplay(userDao.findByUsername("ta_demo").orElseGet(() -> createUser(
                new DemoUserSpec("demo-user-ta-core", "ta_demo", "ta_demo@local.test", User.Role.TA),
                summary
        ))));

        for (DemoUserSpec spec : DEMO_USERS) {
            users.add(applyDemoAccountDisplay(userDao.findByUsername(spec.username()).orElseGet(() -> createUser(spec, summary))));
        }
        return users;
    }

    private User applyDemoAccountDisplay(User user) {
        if (user == null || user.getRole() != User.Role.MO) {
            return user;
        }

        // Fill in missing real name and professional title for MO demo accounts, so TA job list shows teacher identity.
        String username = safeText(user.getUsername());
        String title = "";
        String realName = "";
        if ("mo_demo".equals(username)) {
            title = "Dr.";
            realName = "Morgan Lee";
        } else if ("mo_demo_alice".equals(username)) {
            title = "Prof.";
            realName = "Alice Carter";
        } else if ("mo_demo_brian".equals(username)) {
            title = "Dr.";
            realName = "Brian Xu";
        }

        boolean changed = false;
        changed = fillBlank(user.getProfessionalTitle(), user::setProfessionalTitle, title) || changed;
        changed = fillBlank(user.getRealName(), user::setRealName, realName) || changed;
        return changed ? userDao.update(user) : user;
    }

    private User createUser(DemoUserSpec spec, SeedSummary summary) {
        User user = new User(spec.username(), DEFAULT_PASSWORD, resolveAvailableEmail(spec.username(), spec.email()), spec.role());
        user.setUserId(spec.userId());
        User savedUser = userDao.create(user);
        summary.incrementCreatedUsers();
        return savedUser;
    }

    private List<Applicant> ensureApplicants(List<User> users, SeedSummary summary) {
        List<Applicant> applicants = new ArrayList<>();
        for (ApplicantSpec spec : APPLICANT_SPECS) {
            User user = requireUser(users, spec.username());
            Applicant applicant = applicantDao.findByUserId(user.getUserId())
                    .or(() -> applicantDao.findById(spec.applicantId()))
                    .orElseGet(() -> createApplicant(user, spec, summary));

            boolean changed = false;
            if (!user.getUserId().equals(applicant.getUserId())) {
                applicant.setUserId(user.getUserId());
                changed = true;
            }
            changed = fillBlankApplicantDefaults(applicant, spec) || changed;

            if (changed) {
                applicant = applicantDao.update(applicant);
            }

            if (ensureResumeFile(applicant.getResumePath(), applicant.getFullName(), applicant.getSkills())) {
                summary.incrementCreatedResumes();
            }
            applicants.add(applicant);
        }
        return applicants;
    }

    private Applicant createApplicant(User user, ApplicantSpec spec, SeedSummary summary) {
        Applicant applicant = new Applicant();
        applicant.setApplicantId(spec.applicantId());
        applicant.setUserId(user.getUserId());
        applicant.setFullName(spec.fullName());
        applicant.setStudentId(spec.studentId());
        applicant.setDepartment(spec.department());
        applicant.setProgram(spec.program());
        applicant.setGpa(spec.gpa());
        applicant.setSkills(spec.skills());
        applicant.setResumePath(spec.resumePath());
        applicant.setPhone(spec.phone());
        applicant.setAddress(spec.address());
        applicant.setExperience(spec.experience());
        applicant.setMotivation(spec.motivation());
        Applicant savedApplicant = applicantDao.create(applicant);
        summary.incrementCreatedApplicants();
        if (ensureResumeFile(savedApplicant.getResumePath(), savedApplicant.getFullName(), savedApplicant.getSkills())) {
            summary.incrementCreatedResumes();
        }
        return savedApplicant;
    }

    private boolean fillBlankApplicantDefaults(Applicant applicant, ApplicantSpec spec) {
        // Only fill blank fields, do not overwrite content changed by user during local demo.
        boolean changed = false;
        changed = fillBlank(applicant.getFullName(), applicant::setFullName, spec.fullName()) || changed;
        changed = fillBlank(applicant.getStudentId(), applicant::setStudentId, spec.studentId()) || changed;
        changed = fillBlank(applicant.getDepartment(), applicant::setDepartment, spec.department()) || changed;
        changed = fillBlank(applicant.getProgram(), applicant::setProgram, spec.program()) || changed;
        changed = fillBlank(applicant.getGpa(), applicant::setGpa, spec.gpa()) || changed;
        changed = fillBlank(applicant.getResumePath(), applicant::setResumePath, spec.resumePath()) || changed;
        changed = fillBlank(applicant.getPhone(), applicant::setPhone, spec.phone()) || changed;
        changed = fillBlank(applicant.getAddress(), applicant::setAddress, spec.address()) || changed;
        changed = fillBlank(applicant.getExperience(), applicant::setExperience, spec.experience()) || changed;
        changed = fillBlank(applicant.getMotivation(), applicant::setMotivation, spec.motivation()) || changed;

        if ((applicant.getSkills() == null || applicant.getSkills().isEmpty()) && !spec.skills().isEmpty()) {
            applicant.setSkills(spec.skills());
            changed = true;
        }
        return changed;
    }

    private List<Job> ensureJobs(List<User> users, SeedSummary summary) {
        List<Job> jobs = new ArrayList<>();
        for (JobSpec spec : JOB_SPECS) {
            User moUser = requireUser(users, spec.moUsername());
            Job job = jobDao.findById(spec.jobId())
                    .or(() -> findExistingDemoJob(spec))
                    .orElseGet(() -> createJob(moUser, spec, summary));

            boolean changed = false;
            if (!hasText(job.getMoId()) || !hasLiveUser(job.getMoId())) {
                // If CSV still has old demo user id, rebind to current real MO.
                job.setMoId(moUser.getUserId());
                changed = true;
            }
            if (!hasText(job.getMoName())) {
                job.setMoName(spec.moDisplayName());
                changed = true;
            }
            changed = fillBlank(job.getTitle(), job::setTitle, spec.title()) || changed;
            changed = fillBlank(job.getCourseCode(), job::setCourseCode, spec.courseCode()) || changed;
            changed = fillBlank(job.getCourseName(), job::setCourseName, spec.courseName()) || changed;
            changed = fillBlank(job.getDescription(), job::setDescription, spec.description()) || changed;
            if (job.getWeeklyHours() == null || Math.abs(job.getWeeklyHours() - spec.weeklyHours()) > 0.0001) {
                job.setWeeklyHours(spec.weeklyHours());
                changed = true;
            }
            if (job.getWorkStartDate() == null) {
                job.setWorkStartDate(LocalDate.now().plusDays(spec.workStartOffsetDays()));
                changed = true;
            }
            if (job.getWorkEndDate() == null) {
                job.setWorkEndDate(LocalDate.now().plusDays(spec.workEndOffsetDays()));
                changed = true;
            }
            changed = fillBlank(job.getSalary(), job::setSalary, spec.salary()) || changed;
            if ((job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) && !spec.requiredSkills().isEmpty()) {
                job.setRequiredSkills(spec.requiredSkills());
                changed = true;
            }
            if (job.getPositions() <= 0) {
                job.setPositions(spec.positions());
                changed = true;
            }
            if (job.getDeadline() == null) {
                job.setDeadline(LocalDateTime.now().plusDays(spec.deadlineOffsetDays()));
                changed = true;
            }
            if (job.getStatus() == null) {
                job.setStatus(spec.status());
                changed = true;
            }

            if (changed) {
                job = jobDao.update(job);
            }
            jobs.add(job);
        }
        return jobs;
    }

    private Job createJob(User moUser, JobSpec spec, SeedSummary summary) {
        Job job = new Job();
        job.setJobId(spec.jobId());
        job.setMoId(moUser.getUserId());
        job.setMoName(spec.moDisplayName());
        job.setTitle(spec.title());
        job.setCourseCode(spec.courseCode());
        job.setCourseName(spec.courseName());
        job.setDescription(spec.description());
        job.setRequiredSkills(spec.requiredSkills());
        job.setPositions(spec.positions());
        job.setWeeklyHours(spec.weeklyHours());
        job.setWorkStartDate(LocalDate.now().plusDays(spec.workStartOffsetDays()));
        job.setWorkEndDate(LocalDate.now().plusDays(spec.workEndOffsetDays()));
        job.setSalary(spec.salary());
        job.setDeadline(LocalDateTime.now().plusDays(spec.deadlineOffsetDays()));
        job.setStatus(spec.status());
        Job savedJob = jobDao.create(job);
        summary.incrementCreatedJobs();
        return savedJob;
    }

    private Optional<Job> findExistingDemoJob(JobSpec spec) {
        // Compatible with early demo data without fixed jobId: find old records by course code + title to continue reusing.
        return jobDao.findByCourseCode(spec.courseCode()).stream()
                .filter(job -> spec.title().equalsIgnoreCase(safeText(job.getTitle())))
                .findFirst();
    }

    private void ensureApplications(List<User> users, List<Applicant> applicants, List<Job> jobs, SeedSummary summary) {
        for (ApplicationSpec spec : APPLICATION_SPECS) {
            User applicantUser = requireUser(users, spec.username());
            Applicant applicant = requireApplicant(applicants, applicantUser.getUserId());
            Job job = requireJob(jobs, spec.jobId());

            Application application = applicationDao.findById(spec.applicationId())
                    .or(() -> applicationDao.findByJobIdAndApplicantId(job.getJobId(), applicantUser.getUserId()))
                    .orElseGet(() -> createApplication(applicantUser, applicant, job, spec, summary));

            boolean changed = false;
            if (!job.getJobId().equals(application.getJobId())) {
                application.setJobId(job.getJobId());
                changed = true;
            }
            if (!applicantUser.getUserId().equals(application.getApplicantId())) {
                application.setApplicantId(applicantUser.getUserId());
                changed = true;
            }
            changed = fillBlank(application.getApplicantName(), application::setApplicantName, applicant.getFullName()) || changed;
            changed = fillBlank(application.getApplicantEmail(), application::setApplicantEmail, applicantUser.getEmail()) || changed;
            changed = fillBlank(application.getJobTitle(), application::setJobTitle, job.getTitle()) || changed;
            changed = fillBlank(application.getCourseCode(), application::setCourseCode, job.getCourseCode()) || changed;
            changed = fillBlank(application.getMoId(), application::setMoId, job.getMoId()) || changed;
            changed = fillBlank(application.getMoName(), application::setMoName, job.getMoName()) || changed;
            changed = fillBlank(application.getCoverLetter(), application::setCoverLetter, spec.coverLetter()) || changed;
            if (application.getStatus() == null) {
                application.setStatus(spec.status());
                changed = true;
            }
            if (application.getStatus() != Application.Status.PENDING && application.getReviewedAt() == null) {
                application.setReviewedAt(LocalDateTime.now());
                changed = true;
            }

            if (application.getStatus() != Application.Status.PENDING) {
                if (application.getProgressStage() != Application.ProgressStage.COMPLETED) {
                    // Non-PENDING status is treated as reaching final result on timeline.
                    application.setProgressStage(Application.ProgressStage.COMPLETED);
                    changed = true;
                }
                if (application.getFinalDecisionAt() == null) {
                    application.setFinalDecisionAt(
                            application.getReviewedAt() != null ? application.getReviewedAt() : LocalDateTime.now());
                    changed = true;
                }
            } else if (spec.pendingProgressStage() != null) {
                Application.ProgressStage target = spec.pendingProgressStage();
                if (application.getProgressStage() != target) {
                    application.setProgressStage(target);
                    changed = true;
                }
                if (target == Application.ProgressStage.UNDER_REVIEW) {
                    if (application.getReviewStartedAt() == null) {
                        application.setReviewStartedAt(LocalDateTime.now().minusDays(2));
                        changed = true;
                    }
                }
                if (target == Application.ProgressStage.INTERVIEW_SCHEDULED) {
                    if (application.getReviewStartedAt() == null) {
                        application.setReviewStartedAt(LocalDateTime.now().minusDays(5));
                        changed = true;
                    }
                    if (application.getInterviewScheduledAt() == null) {
                        application.setInterviewScheduledAt(LocalDateTime.now().minusDays(1));
                        changed = true;
                    }
                }
            }

            if (changed) {
                applicationDao.update(application);
            }
        }
    }

    private Application createApplication(User applicantUser, Applicant applicant, Job job, ApplicationSpec spec, SeedSummary summary) {
        Application application = new Application();
        application.setApplicationId(spec.applicationId());
        application.setJobId(job.getJobId());
        application.setApplicantId(applicantUser.getUserId());
        application.setApplicantName(applicant.getFullName());
        application.setApplicantEmail(applicantUser.getEmail());
        application.setJobTitle(job.getTitle());
        application.setCourseCode(job.getCourseCode());
        application.setMoId(job.getMoId());
        application.setMoName(job.getMoName());
        application.setStatus(spec.status());
        application.setCoverLetter(spec.coverLetter());
        if (spec.status() != Application.Status.PENDING) {
            application.setReviewedAt(LocalDateTime.now());
            application.setProgressStage(Application.ProgressStage.COMPLETED);
            application.setFinalDecisionAt(application.getReviewedAt());
        } else if (spec.pendingProgressStage() != null) {
            application.setProgressStage(spec.pendingProgressStage());
            if (spec.pendingProgressStage() == Application.ProgressStage.UNDER_REVIEW) {
                application.setReviewStartedAt(LocalDateTime.now().minusDays(2));
            }
            if (spec.pendingProgressStage() == Application.ProgressStage.INTERVIEW_SCHEDULED) {
                application.setReviewStartedAt(LocalDateTime.now().minusDays(5));
                application.setInterviewScheduledAt(LocalDateTime.now().minusDays(1));
            }
        }
        Application savedApplication = applicationDao.create(application);
        summary.incrementCreatedApplications();
        return savedApplication;
    }

    private User requireUser(List<User> users, String username) {
        return users.stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing demo user: " + username));
    }

    private Applicant requireApplicant(List<Applicant> applicants, String userId) {
        return applicants.stream()
                .filter(applicant -> userId.equals(applicant.getUserId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing demo applicant for user: " + userId));
    }

    private Job requireJob(List<Job> jobs, String jobId) {
        return jobs.stream()
                .filter(job -> jobId.equals(job.getJobId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing demo job: " + jobId));
    }

    private String resolveAvailableEmail(String username, String preferredEmail) {
        if (userDao.findByEmail(preferredEmail).isEmpty()) {
            return preferredEmail;
        }

        // Local users may have already occupied demo email, append +N to ensure seeder does not fail due to duplicate email.
        int suffix = 1;
        while (true) {
            String candidate = username + "+" + suffix + "@local.test";
            if (userDao.findByEmail(candidate).isEmpty()) {
                return candidate;
            }
            suffix++;
        }
    }

    private boolean hasLiveUser(String userId) {
        return hasText(userId) && userDao.findById(userId).isPresent();
    }

    private boolean ensureResumeFile(String relativePath, String applicantName, List<String> skills) {
        if (!hasText(relativePath)) {
            return false;
        }

        Path filePath = Path.of(StoragePaths.getDataDir()).resolve(relativePath).normalize();
        if (Files.exists(filePath)) {
            return false;
        }

        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filePath, buildResumePdf(applicantName, skills));
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create demo resume file: " + relativePath, e);
        }
    }

    private byte[] buildResumePdf(String applicantName, List<String> skills) {
        // Generate minimal openable PDF for demo resume, avoiding extra PDF library or build tools.
        String skillText = skills == null || skills.isEmpty() ? "General TA support" : String.join(", ", skills);
        List<String> lines = List.of(
                "Demo Resume",
                "Applicant: " + safeText(applicantName),
                "Skills: " + skillText,
                "This file was generated automatically for local demo data."
        );

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("BT\n");
        contentBuilder.append("/F1 18 Tf\n");
        contentBuilder.append("72 740 Td\n");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                contentBuilder.append("0 -24 Td\n");
            }
            contentBuilder.append('(')
                    .append(escapePdfText(lines.get(i)))
                    .append(") Tj\n");
        }
        contentBuilder.append("ET");

        String content = contentBuilder.toString();
        String object1 = "<< /Type /Catalog /Pages 2 0 R >>";
        String object2 = "<< /Type /Pages /Kids [3 0 R] /Count 1 >>";
        String object3 = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>";
        String object4 = "<< /Length " + content.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + content + "\nendstream";
        String object5 = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>";

        String[] objects = {object1, object2, object3, object4, object5};
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            writeAscii(output, "%PDF-1.4\n");
            List<Integer> offsets = new ArrayList<>();
            for (int index = 0; index < objects.length; index++) {
                offsets.add(output.size());
                writeAscii(output, (index + 1) + " 0 obj\n");
                writeAscii(output, objects[index]);
                writeAscii(output, "\nendobj\n");
            }

            int xrefOffset = output.size();
            writeAscii(output, "xref\n0 6\n");
            writeAscii(output, "0000000000 65535 f \n");
            for (Integer offset : offsets) {
                writeAscii(output, String.format(Locale.ROOT, "%010d 00000 n \n", offset));
            }
            writeAscii(output, "trailer\n<< /Size 6 /Root 1 0 R >>\n");
            writeAscii(output, "startxref\n" + xrefOffset + "\n%%EOF\n");
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build demo resume PDF", e);
        }
    }

    private void writeAscii(ByteArrayOutputStream output, String text) throws IOException {
        output.write(text.getBytes(StandardCharsets.US_ASCII));
    }

    private String escapePdfText(String value) {
        return safeText(value)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private boolean fillBlank(String currentValue, java.util.function.Consumer<String> setter, String fallbackValue) {
        if (!hasText(currentValue) && hasText(fallbackValue)) {
            setter.accept(fallbackValue);
            return true;
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private record DemoUserSpec(String userId, String username, String email, User.Role role) {
    }

    private record ApplicantSpec(
            String applicantId,
            String username,
            String fullName,
            String studentId,
            String department,
            String program,
            String gpa,
            List<String> skills,
            String resumePath,
            String phone,
            String address,
            String experience,
            String motivation
    ) {
    }

    private record JobSpec(
            String jobId,
            String moUsername,
            String moDisplayName,
            String title,
            String courseCode,
            String courseName,
            String description,
            List<String> requiredSkills,
            int positions,
            double weeklyHours,
            int workStartOffsetDays,
            int workEndOffsetDays,
            String salary,
            int deadlineOffsetDays,
            Job.Status status
    ) {
    }

    private record ApplicationSpec(
            String applicationId,
            String username,
            String jobId,
            Application.Status status,
            String coverLetter,
            /** Only used for demo progress when status is PENDING; ignored for non-PENDING. */
            Application.ProgressStage pendingProgressStage
    ) {
    }

    public static final class SeedSummary {
        private int createdUsers;
        private int createdApplicants;
        private int createdJobs;
        private int createdApplications;
        private int createdResumes;

        public int getCreatedUsers() {
            return createdUsers;
        }

        public int getCreatedApplicants() {
            return createdApplicants;
        }

        public int getCreatedJobs() {
            return createdJobs;
        }

        public int getCreatedApplications() {
            return createdApplications;
        }

        public int getCreatedResumes() {
            return createdResumes;
        }

        private void incrementCreatedUsers() {
            createdUsers++;
        }

        private void incrementCreatedApplicants() {
            createdApplicants++;
        }

        private void incrementCreatedJobs() {
            createdJobs++;
        }

        private void incrementCreatedApplications() {
            createdApplications++;
        }

        private void incrementCreatedResumes() {
            createdResumes++;
        }

        @Override
        public String toString() {
            return "users=" + createdUsers
                    + ", applicants=" + createdApplicants
                    + ", jobs=" + createdJobs
                    + ", applications=" + createdApplications
                    + ", resumes=" + createdResumes;
        }
    }
}

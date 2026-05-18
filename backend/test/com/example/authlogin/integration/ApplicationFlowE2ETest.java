package com.example.authlogin.integration;

import com.example.authlogin.dao.ApplicantDao;
import com.example.authlogin.dao.ApplicationDao;
import com.example.authlogin.dao.JobDao;
import com.example.authlogin.dao.UserDao;
import com.example.authlogin.model.Applicant;
import com.example.authlogin.model.Application;
import com.example.authlogin.model.Job;
import com.example.authlogin.model.User;

import java.util.Arrays;

/**
 * 申请流程端到端测试
 */
public class ApplicationFlowE2ETest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        UserDao userDao = UserDao.getInstance();
        ApplicantDao applicantDao = ApplicantDao.getInstance();
        JobDao jobDao = JobDao.getInstance();
        ApplicationDao applicationDao = ApplicationDao.getInstance();

        userDao.deleteAll();
        applicantDao.deleteAll();
        jobDao.deleteAll();
        applicationDao.deleteAll();

        try {
            test("End-to-end: create users and profile", () -> {
                User mo = userDao.create(new User("e2e_mo", "Pass1234", "e2e_mo@example.com", User.Role.MO));
                User ta = userDao.create(new User("e2e_ta", "Pass1234", "e2e_ta@example.com", User.Role.TA));

                Applicant applicant = new Applicant();
                applicant.setUserId(ta.getUserId());
                applicant.setFullName("E2E TA");
                applicant.setStudentId("2023002002");
                applicant.setDepartment("Computer Science");
                applicant.setProgram("Master");
                applicant.setSkills(Arrays.asList("Java", "SQL", "Linux"));
                applicantDao.create(applicant);

                assert mo.getUserId() != null : "MO user should be created";
                assert ta.getUserId() != null : "TA user should be created";
                assert applicantDao.findByUserId(ta.getUserId()).isPresent() : "TA applicant profile should exist";
            });

            test("End-to-end: MO posts job and TA submits application", () -> {
                User mo = userDao.findByUsername("e2e_mo").orElseThrow();
                User ta = userDao.findByUsername("e2e_ta").orElseThrow();

                Job job = new Job();
                job.setMoId(mo.getUserId());
                job.setMoName("Dr. E2E");
                job.setTitle("E2E TA Position");
                job.setCourseCode("CS601");
                job.setCourseName("System Integration");
                job.setRequiredSkills(Arrays.asList("Java", "SQL"));
                job.setStatus(Job.Status.OPEN);
                jobDao.create(job);

                Application application = new Application();
                application.setJobId(job.getJobId());
                application.setApplicantId(ta.getUserId());
                application.setApplicantName(ta.getUsername());
                application.setApplicantEmail(ta.getEmail());
                application.setJobTitle(job.getTitle());
                application.setCourseCode(job.getCourseCode());
                application.setMoId(job.getMoId());
                application.setMoName(job.getMoName());
                application.setCoverLetter("I can support labs and assignment marking.");
                applicationDao.create(application);

                assert applicationDao.hasApplied(job.getJobId(), ta.getUserId()) : "application should be recorded";
                assert applicationDao.findByMoId(mo.getUserId()).size() == 1 : "MO should see one application";
            });

            test("End-to-end: MO accepts application", () -> {
                Application app = applicationDao.findAll().stream().findFirst().orElseThrow();
                boolean updated = applicationDao.accept(app.getApplicationId());
                assert updated : "accept operation should succeed";
                Application refreshed = applicationDao.findById(app.getApplicationId()).orElseThrow();
                assert refreshed.getStatus() == Application.Status.ACCEPTED : "application status should become ACCEPTED";
            });
        } finally {
            applicationDao.deleteAll();
            jobDao.deleteAll();
            applicantDao.deleteAll();
            userDao.deleteAll();
        }

        System.out.println("========================================");
        System.out.println("ApplicationFlowE2ETest Summary");
        System.out.println("========================================");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));
        System.out.println("========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, Runnable runnable) {
        try {
            runnable.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + name + " - " + t.getMessage());
        }
    }
}

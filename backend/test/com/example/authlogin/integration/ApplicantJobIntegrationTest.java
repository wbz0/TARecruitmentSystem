package com.example.authlogin.integration;

import com.example.authlogin.dao.ApplicantDao;
import com.example.authlogin.dao.JobDao;
import com.example.authlogin.dao.UserDao;
import com.example.authlogin.model.Applicant;
import com.example.authlogin.model.Job;
import com.example.authlogin.model.User;

import java.util.Arrays;
import java.util.Optional;

/**
 * 档案和职位模块集成测试
 */
public class ApplicantJobIntegrationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        UserDao userDao = UserDao.getInstance();
        ApplicantDao applicantDao = ApplicantDao.getInstance();
        JobDao jobDao = JobDao.getInstance();

        userDao.deleteAll();
        applicantDao.deleteAll();
        jobDao.deleteAll();

        try {
            test("Create TA and MO users", () -> {
                User ta = userDao.create(new User("aj_ta", "Pass1234", "aj_ta@example.com", User.Role.TA));
                User mo = userDao.create(new User("aj_mo", "Pass1234", "aj_mo@example.com", User.Role.MO));
                assert ta.getUserId() != null : "TA user id should exist";
                assert mo.getUserId() != null : "MO user id should exist";
            });

            test("Create applicant profile linked to TA", () -> {
                User ta = userDao.findByUsername("aj_ta").orElseThrow();
                Applicant applicant = new Applicant();
                applicant.setUserId(ta.getUserId());
                applicant.setFullName("Alice Job");
                applicant.setStudentId("2023001001");
                applicant.setDepartment("Computer Science");
                applicant.setProgram("Master");
                applicant.setSkills(Arrays.asList("Java", "SQL", "Linux"));
                applicantDao.create(applicant);

                Optional<Applicant> found = applicantDao.findByUserId(ta.getUserId());
                assert found.isPresent() : "applicant profile should be found by userId";
                assert found.get().getSkills().size() == 3 : "applicant should have 3 skills";
            });

            test("Create job linked to MO and query it", () -> {
                User mo = userDao.findByUsername("aj_mo").orElseThrow();
                Job job = new Job();
                job.setMoId(mo.getUserId());
                job.setMoName("Dr. Work");
                job.setTitle("TA for Database");
                job.setCourseCode("CS502");
                job.setCourseName("Advanced Database");
                job.setRequiredSkills(Arrays.asList("SQL", "Database Design"));
                job.setStatus(Job.Status.OPEN);
                jobDao.create(job);

                assert jobDao.findByMoId(mo.getUserId()).size() == 1 : "MO should have 1 job";
                assert jobDao.findOpenJobs().size() == 1 : "open jobs should be 1";
                assert jobDao.search("database").size() >= 1 : "keyword search should return created job";
            });
        } finally {
            jobDao.deleteAll();
            applicantDao.deleteAll();
            userDao.deleteAll();
        }

        System.out.println("========================================");
        System.out.println("ApplicantJobIntegrationTest Summary");
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

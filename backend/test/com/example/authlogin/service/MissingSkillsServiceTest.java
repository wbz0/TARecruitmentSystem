package com.example.authlogin.service;

import java.util.Arrays;

/**
 * MissingSkillsService 自动化测试
 */
public class MissingSkillsServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        MissingSkillsService service = new MissingSkillsService();

        test("Service skeleton should return analysis object", () -> {
            MissingSkillsService.MissingSkillsAnalysis analysis = service.analyzeMissingSkills(
                    Arrays.asList("Java", "SQL"),
                    Arrays.asList("Java")
            );
            assert analysis != null : "analysis should not be null";
            assert analysis.getRequiredSkills().size() == 2 : "required skills should keep input size";
            assert analysis.getApplicantSkills().size() == 1 : "applicant skills should keep input size";
        });

        System.out.println("========================================");
        System.out.println("MissingSkillsServiceTest Summary");
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

package com.example.authlogin.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            assert analysis.getMatchedSkills().size() == 1 : "matched skills should contain Java";
            assert analysis.getMissingSkills().size() == 1 : "missing skills should contain SQL";
            assert analysis.getMatchScore() == 50.0 : "match score should be 50";
        });

        test("Comparison should be case-insensitive and deduplicated", () -> {
            MissingSkillsService.MissingSkillsAnalysis analysis = service.analyzeMissingSkills(
                    Arrays.asList("Java", " java ", "Machine Learning"),
                    Arrays.asList("JAVA", "Python")
            );
            assert analysis.getRequiredSkills().size() == 2 : "required skills should be deduplicated";
            assert analysis.getMatchedSkills().size() == 1 : "Java should match";
            assert analysis.getMissingSkills().size() == 1 : "Machine Learning should be missing";
            assert analysis.getMatchScore() == 50.0 : "score should be 50";
        });

        test("Empty required skills should return 100 score", () -> {
            MissingSkillsService.MissingSkillsAnalysis analysis = service.analyzeMissingSkills(
                    Arrays.asList(),
                    Arrays.asList("Java")
            );
            assert analysis.getMatchScore() == 100.0 : "score should be 100 when no required skills";
        });

        test("Report should include summary and recommendations", () -> {
            MissingSkillsService.MissingSkillsReport report = service.generateMissingSkillsReport(
                    Arrays.asList("Java", "SQL", "Linux"),
                    Arrays.asList("Java")
            );
            assert report != null : "report should not be null";
            assert report.getAnalysis().getMissingSkills().size() == 2 : "missing size should be 2";
            assert report.getSummary().contains("Missing: 2") : "summary should contain missing count";
            assert !report.getRecommendations().isEmpty() : "recommendations should not be empty";
        });

        test("Report should suggest interview when no missing skills", () -> {
            MissingSkillsService.MissingSkillsReport report = service.generateMissingSkillsReport(
                    Arrays.asList("Java", "SQL"),
                    Arrays.asList("Java", "SQL")
            );
            assert report.getAnalysis().getMissingSkills().isEmpty() : "there should be no missing skills";
            assert report.getRecommendations().get(0).contains("covers all required skills")
                    : "first recommendation should indicate full coverage";
        });

        test("Boundary fix: split combined skills by separators", () -> {
            MissingSkillsService.MissingSkillsAnalysis analysis = service.analyzeMissingSkills(
                    Arrays.asList("Java, SQL", "Python；Linux"),
                    Arrays.asList("java", "sql", "linux")
            );
            assert analysis.getRequiredSkills().size() == 4 : "required skills should split into 4 items";
            assert analysis.getMatchedSkills().size() == 3 : "three skills should match";
            assert analysis.getMissingSkills().size() == 1 : "python should be missing";
            assert analysis.getMissingSkills().contains("python") : "missing skills should include python";
        });

        test("Boundary fix: ignore placeholder skill tokens", () -> {
            MissingSkillsService.MissingSkillsAnalysis analysis = service.analyzeMissingSkills(
                    Arrays.asList("Java", "N/A", "-", "none"),
                    Arrays.asList("Java")
            );
            assert analysis.getRequiredSkills().size() == 1 : "placeholder tokens should be ignored";
            assert analysis.getMissingSkills().isEmpty() : "no missing skills after placeholder filtering";
            assert analysis.getMatchScore() == 100.0 : "score should be 100";
        });

        test("Visualization data should contain gap frequencies", () -> {
            MissingSkillsService.MissingSkillsVisualizationData data = service.generateVisualizationData(
                    Arrays.asList("Java", "SQL", "Linux"),
                    Arrays.asList("Java")
            );
            assert data.getRequiredCount() == 3 : "required count should be 3";
            assert data.getMatchedCount() == 1 : "matched count should be 1";
            assert data.getMissingCount() == 2 : "missing count should be 2";
            assert data.getGapFrequency().get("sql") == 1 : "sql gap frequency should be 1";
            assert data.getGapFrequency().get("linux") == 1 : "linux gap frequency should be 1";
        });

        test("Aggregate frequency should summarize multiple applicants", () -> {
            Map<String, Integer> frequency = service.aggregateMissingSkillFrequency(
                    Arrays.asList("Java", "SQL", "Linux"),
                    List.of(
                            Arrays.asList("Java"),
                            Arrays.asList("Java", "SQL"),
                            Arrays.asList("Java", "Linux")
                    )
            );
            assert frequency.get("sql") == 2 : "sql should be missing for 2 applicants";
            assert frequency.get("linux") == 2 : "linux should be missing for 2 applicants";
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

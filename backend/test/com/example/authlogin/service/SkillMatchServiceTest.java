package com.example.authlogin.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * SkillMatchService 自动化测试
 * 运行方式：
 *   javac -encoding UTF-8 -d out -cp backend/src backend/src/com/example/authlogin/service/SkillMatchService.java backend/test/com/example/authlogin/service/SkillMatchServiceTest.java
 *   java -ea -cp out com.example.authlogin.service.SkillMatchServiceTest
 */
public class SkillMatchServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        SkillMatchService service = new SkillMatchService();

        test("Exact match should return 100", () -> {
            SkillMatchService.SkillMatchResult result = service.matchSkills(
                    Arrays.asList("Java", "Spring", "SQL"),
                    Arrays.asList("java", "spring", "sql", "docker")
            );
            assert result.getScore() == 100.0 : "score should be 100";
            assert result.getLevel() == SkillMatchService.MatchLevel.HIGH : "level should be HIGH";
            assert result.getMatchedSkills().size() == 3 : "matched size should be 3";
            assert result.getMissingSkills().isEmpty() : "missing should be empty";
        });

        test("Partial match should return expected score", () -> {
            SkillMatchService.SkillMatchResult result = service.matchSkills(
                    Arrays.asList("Java", "Spring", "SQL", "Linux"),
                    Arrays.asList("Java", "SQL")
            );
            assert result.getScore() == 50.0 : "score should be 50";
            assert result.getLevel() == SkillMatchService.MatchLevel.LOW : "level should be LOW";
            assert result.getMatchedSkills().size() == 2 : "matched size should be 2";
            assert result.getMissingSkills().size() == 2 : "missing size should be 2";
        });

        test("Duplicate skills should not be over-counted", () -> {
            SkillMatchService.SkillMatchResult result = service.matchSkills(
                    Arrays.asList("Java", "Java", "SQL"),
                    Arrays.asList("java", "java", "sql", "sql")
            );
            assert result.getRequiredSkillCount() == 2 : "required count should deduplicate to 2";
            assert result.getApplicantSkillCount() == 2 : "applicant count should deduplicate to 2";
            assert result.getScore() == 100.0 : "score should be 100";
        });

        test("No required skills should return full score", () -> {
            SkillMatchService.SkillMatchResult result = service.matchSkills(
                    Collections.emptyList(),
                    Arrays.asList("java")
            );
            assert result.getScore() == 100.0 : "score should be 100 when no required skill";
            assert result.getLevel() == SkillMatchService.MatchLevel.HIGH : "level should be HIGH";
        });

        test("No matched skills should return NONE level", () -> {
            SkillMatchService.SkillMatchResult result = service.matchSkills(
                    Arrays.asList("python", "ml"),
                    Arrays.asList("java", "sql")
            );
            assert result.getScore() == 0.0 : "score should be 0";
            assert result.getLevel() == SkillMatchService.MatchLevel.NONE : "level should be NONE";
            assert result.getMatchedSkills().isEmpty() : "matched should be empty";
            assert result.getMissingSkills().size() == 2 : "missing size should be 2";
        });

        test("Keyword match should include contextual overlap", () -> {
            SkillMatchService.SkillMatchResult result = service.matchByKeywords(
                    Arrays.asList("Java", "Machine Learning"),
                    "Need neural network modeling and python data analysis experience.",
                    Arrays.asList("Java", "Python"),
                    "Built neural network projects for data analysis and learning systems."
            );
            assert result.getSkillScore() == 50.0 : "skill score should be 50";
            assert result.getKeywordScore() > result.getSkillScore() : "keyword score should be higher than skill score";
            assert result.getMatchedKeywords().contains("neural") : "matched keywords should include neural";
            assert result.getMatchedKeywords().contains("analysis") : "matched keywords should include analysis";
        });

        test("Keyword extraction should ignore stop words", () -> {
            SkillMatchService.SkillMatchResult result = service.matchByKeywords(
                    Arrays.asList("Java"),
                    "the and of with",
                    Arrays.asList("Java"),
                    "the with and"
            );
            assert result.getKeywordScore() == 100.0 : "keyword score should rely on real keywords only";
            assert !result.getMatchedKeywords().contains("the") : "stop words should not appear in matched keywords";
        });

        test("AI integration should blend AI score", () -> {
            SkillMatchService aiService = new SkillMatchService((required, applicant) ->
                    Optional.of(new AiSkillMatchClient.AiScoreResult(90.0, "mock-ai"))
            );

            SkillMatchService.SkillMatchResult baseline = aiService.matchByKeywords(
                    Arrays.asList("Java", "Machine Learning"),
                    "Need neural network and python data analysis.",
                    Arrays.asList("Java", "Python"),
                    "Neural network data analysis projects."
            );

            SkillMatchService.SkillMatchResult aiResult = aiService.matchWithAi(
                    Arrays.asList("Java", "Machine Learning"),
                    "Need neural network and python data analysis.",
                    Arrays.asList("Java", "Python"),
                    "Neural network data analysis projects."
            );

            double expected = roundTo2(baseline.getScore() * 0.6 + 90.0 * 0.4);
            assert aiResult.isAiEnhanced() : "result should mark ai enhanced";
            assert aiResult.getAiScore() == 90.0 : "ai score should be 90";
            assert expected == aiResult.getScore() : "final score should follow blend formula";
        });

        test("AI integration should fallback when AI unavailable", () -> {
            SkillMatchService fallbackService = new SkillMatchService((required, applicant) -> Optional.empty());

            SkillMatchService.SkillMatchResult baseline = fallbackService.matchByKeywords(
                    Arrays.asList("Java", "Spring"),
                    "Need restful api and backend development.",
                    Arrays.asList("Java"),
                    "backend api experience."
            );
            SkillMatchService.SkillMatchResult aiResult = fallbackService.matchWithAi(
                    Arrays.asList("Java", "Spring"),
                    "Need restful api and backend development.",
                    Arrays.asList("Java"),
                    "backend api experience."
            );

            assert !aiResult.isAiEnhanced() : "ai flag should be false when unavailable";
            assert baseline.getScore() == aiResult.getScore() : "fallback score should equal baseline";
        });

        System.out.println("========================================");
        System.out.println("SkillMatchServiceTest Summary");
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

    private static double roundTo2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

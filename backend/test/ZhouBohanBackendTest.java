import com.example.tarecruitment.ai.client.DeepSeekAiConfig;
import com.example.tarecruitment.ai.client.DeepSeekApplicantSearchClient;
import com.example.tarecruitment.ai.client.DeepSeekTaJobSearchClient;
import com.example.tarecruitment.ai.service.MoApplicantAiSearchService;
import com.example.tarecruitment.ai.service.TaJobAiSearchService;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.common.storage.CsvCodec;
import com.example.tarecruitment.common.storage.StoragePaths;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.model.Applicant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Zhou Bohan backend test entry point.
 *
 * Zhou Bohan's responsibilities focus on TA profiles, file/path data, CSV storage, and DeepSeek recommendation search configuration.
 * This test also uses main method direct execution, without depending on JUnit.
 *
 * scripts/test/test-zhou-bohan.sh will point TA_HIRING_DATA_DIR to the temporary directory,
 * so ApplicantDao's applicants.csv will only appear under build/contributor-tests/zhou-bohan/data.
 */
public class ZhouBohanBackendTest {

    private static int passed;

    public static void main(String[] args) {
        // First verify underlying CSV and paths, then verify profile model/DAO, and finally verify AI config fallback.
        testCsvCodec();
        testStoragePaths();
        testApplicantCsvRoundTrip();
        testApplicantDaoDuplicateRules();
        testDeepSeekConfigFallbacks();
        testAiRecommendationLanguageRules();
        System.out.println("[Zhou Bohan] PASS total=" + passed);
    }

    /**
     * Validate CSV escaping utility.
     *
     * Applicant names, departments, experiences and other fields may all contain commas or quotes.
     * If CSV escaping fails, the columns read back from DAO will be misaligned, and subsequent page displays will also be incorrect.
     */
    private static void testCsvCodec() {
        String line = CsvCodec.escape("Alice, TA") + "," + CsvCodec.escape("He said \"hello\"");
        String[] parts = CsvCodec.split(line);
        assertEquals(2, parts.length, "csv part count");
        assertEquals("Alice, TA", CsvCodec.unescape(parts[0]), "comma value");
        assertEquals("He said \"hello\"", CsvCodec.unescape(parts[1]), "quote value");
        pass("CsvCodec preserves commas and quotes in CSV fields");
    }

    /**
     * Validate StoragePaths interpretation of runtime data directory.
     *
     * The project convention is that runtime data is not written into the repository, but to TA_HIRING_DATA_DIR.
     * Here, confirm that applicants, resumes, photos, and other directories are all derived from the same data root directory.
     */
    private static void testStoragePaths() {
        String dataDir = StoragePaths.getDataDir();
        assertTrue(!dataDir.isBlank(), "data dir configured by test script");
        assertTrue(StoragePaths.getApplicantsDir().endsWith("applicants"), "applicant dir suffix");
        assertTrue(StoragePaths.getResumeDir().endsWith("resumes"), "resume dir suffix");
        assertTrue(StoragePaths.getPhotoDir().endsWith("photos"), "photo dir suffix");
        pass("StoragePaths uses TA_HIRING_DATA_DIR and module subdirectories");
    }

    /**
     * Validate Applicant model CSV round-trip.
     *
     * TA profile has many fields, and includes skills list, resume path, and photo path.
     * This test ensures that after these fields are written to CSV and read back, the core information is not lost or misaligned.
     */
    private static void testApplicantCsvRoundTrip() {
        Applicant applicant = new Applicant("user-2", "Alice Zhang", "20260001");
        applicant.setApplicantId("applicant-001");
        applicant.setDepartment("School, Engineering");
        applicant.setProgram("MSc Software Engineering");
        applicant.setGpa("3.8");
        applicant.setSkills(Arrays.asList("Java", "CSV", "AI"));
        applicant.setResumePath("resumes/alice.pdf");
        applicant.setPhotoPath("photos/alice.png");
        applicant.setPhone("123456");
        applicant.setAddress("Campus A");
        applicant.setExperience("TA for Java, CSV tools");
        applicant.setMotivation("Help students learn.");

        Applicant parsed = Applicant.fromCsv(applicant.toCsv());
        assertNotNull(parsed, "parsed applicant");
        assertEquals("School, Engineering", parsed.getDepartment(), "department csv escaping");
        assertEquals("Java;CSV;AI", parsed.getSkillsAsString(), "skills serialization");
        assertEquals("photos/alice.png", parsed.getPhotoPath(), "photo path");
        pass("Applicant CSV round-trip preserves profile and asset fields");
    }

    /**
     * Validate ApplicantDao uniqueness rules.
     *
     * A TA user can only have one profile, and the same student ID cannot be reused by multiple profiles.
     * These constraints are placed in the DAO layer to prevent bypassing the frontend form and directly writing duplicate data.
     */
    private static void testApplicantDaoDuplicateRules() {
        ApplicantDao dao = ApplicantDao.getInstance();
        dao.deleteAll();

        Applicant first = new Applicant("user-2-a", "Alice Zhang", "S20260001");
        first.setDepartment("Computing");
        first.setProgram("MSc");
        dao.create(first);

        assertTrue(dao.findByUserId("user-2-a").isPresent(), "find by user id");
        assertEquals(1L, dao.count(), "applicant count");
        assertThrows(IllegalArgumentException.class,
                () -> dao.create(new Applicant("user-2-a", "Duplicate User", "S20260002")),
                "duplicate user profile rejected");
        assertThrows(IllegalArgumentException.class,
                () -> dao.create(new Applicant("user-2-b", "Duplicate Student", "S20260001")),
                "duplicate student id rejected");
        pass("ApplicantDao creates profiles and rejects duplicate user/student records");
    }

    /**
     * Validate DeepSeek config safe fallback.
     *
     * During defense or local runtime, a real API key may not be configured.
     * When the key is a placeholder like replace_me, isApiKeyConfigured() must return false,
     * so the service layer can return “AI temporarily unavailable” instead of generating local fake recommendations.
     */
    private static void testDeepSeekConfigFallbacks() {
        String oldKey = System.getProperty("deepseek.api.key");
        String oldBaseUrl = System.getProperty("deepseek.base-url");
        String oldModel = System.getProperty("deepseek.model");
        String oldTimeout = System.getProperty("deepseek.timeout-ms");
        try {
            System.setProperty("deepseek.api.key", "replace_me");
            System.setProperty("deepseek.base-url", "https://api.example.test///");
            System.setProperty("deepseek.model", "deepseek-test-model");
            System.setProperty("deepseek.timeout-ms", "-1");

            DeepSeekAiConfig config = DeepSeekAiConfig.load(null);
            assertFalse(config.isApiKeyConfigured(), "placeholder key is not configured");
            assertEquals("https://api.example.test", config.getBaseUrl(), "base url normalization");
            assertEquals("deepseek-test-model", config.getModel(), "model property");
            assertEquals(8000L, config.getTimeoutMillis(), "invalid timeout fallback");
            pass("DeepSeekAiConfig falls back safely without a real API key");
        } finally {
            restoreProperty("deepseek.api.key", oldKey);
            restoreProperty("deepseek.base-url", oldBaseUrl);
            restoreProperty("deepseek.model", oldModel);
            restoreProperty("deepseek.timeout-ms", oldTimeout);
        }
    }

    /**
     * Validate recommendation prompt language control.
     *
     * The two AI recommendation flows must follow the user's input language:
     * - Chinese query -> Chinese recommendation message and reason;
     * - English query -> English recommendation message and reason.
     *
     * This uses capturing fake clients, so the test verifies service-layer prompt construction
     * and response fallback logic without calling the real DeepSeek endpoint.
     */
    private static void testAiRecommendationLanguageRules() {
        Job job = new Job("mo-language", "MO Language", "Teaching Assistant - Software Engineering Studio", "DEMO-SE601");
        job.setJobId("job-language-001");
        job.setDescription("Support labs, review Java code, and guide testing.");
        job.setRequiredSkills(Arrays.asList("Java", "Testing"));
        job.setPositions(1);

        Applicant taProfile = new Applicant("ta-language", "Alice Language", "S20261234");
        taProfile.setProgram("MSc Software Engineering");
        taProfile.setSkills(Arrays.asList("Java", "Testing"));
        taProfile.setExperience("Helped classmates debug Java assignments.");

        CapturingTaJobClient taChineseClient = new CapturingTaJobClient("", "这个职位匹配你的 Java 和测试经验。");
        TaJobAiSearchService.SearchResult taChineseResult = new TaJobAiSearchService(taChineseClient)
                .search(taProfile, List.of(job), "请推荐适合我的岗位");
        assertContains(taChineseClient.systemPrompt, "Simplified Chinese", "TA Chinese system prompt");
        assertContains(taChineseClient.userPrompt, "Target output language: Simplified Chinese", "TA Chinese user prompt");
        assertContains(taChineseClient.userPrompt, "not applied to yet", "TA prompt explains unapplied candidate pool");
        assertEquals("已为你生成 1 个尚未申请的开放职位推荐。", taChineseResult.getMessage(), "TA Chinese generated message");
        assertEquals("这个职位匹配你的 Java 和测试经验。",
                taChineseResult.getRecommendations().get(0).getRecommendation(),
                "TA Chinese recommendation reason");

        CapturingTaJobClient taBlankChineseClient = new CapturingTaJobClient("", "这个职位匹配你的 Java 和测试经验。");
        TaJobAiSearchService.SearchResult taBlankChineseResult = new TaJobAiSearchService(taBlankChineseClient)
                .search(taProfile, List.of(job), "", "zh-CN");
        assertContains(taBlankChineseClient.userPrompt, "Target output language: Simplified Chinese", "TA blank query uses locale hint");
        assertEquals("已为你生成 1 个尚未申请的开放职位推荐。", taBlankChineseResult.getMessage(), "TA blank Chinese generated message");

        CapturingTaJobClient taEnglishClient = new CapturingTaJobClient("", "This role matches your Java and testing experience.");
        TaJobAiSearchService.SearchResult taEnglishResult = new TaJobAiSearchService(taEnglishClient)
                .search(taProfile, List.of(job), "Recommend a role for my testing skills");
        assertContains(taEnglishClient.systemPrompt, "English", "TA English system prompt");
        assertContains(taEnglishClient.userPrompt, "Target output language: English", "TA English user prompt");
        assertEquals("Generated 1 AI recommendation from open positions you have not applied to yet.", taEnglishResult.getMessage(), "TA English generated message");
        assertEquals("This role matches your Java and testing experience.",
                taEnglishResult.getRecommendations().get(0).getRecommendation(),
                "TA English recommendation reason");

        Application application = new Application("job-language-001", "ta-language", "Alice Language", "alice@example.test");
        application.setApplicationId("application-language-001");
        application.setCoverLetter("I can support Java labs and testing feedback.");
        Map<String, Applicant> applicantsByUserId = Map.of("ta-language", taProfile);

        CapturingApplicantClient moChineseClient = new CapturingApplicantClient("", "该申请人具备 Java 和测试经验，适合支持实验课。");
        MoApplicantAiSearchService.SearchResult moChineseResult = new MoApplicantAiSearchService(moChineseClient)
                .search(job, List.of(application), applicantsByUserId, "请推荐最合适的申请人");
        assertContains(moChineseClient.systemPrompt, "Simplified Chinese", "MO Chinese system prompt");
        assertContains(moChineseClient.userPrompt, "Target output language: Simplified Chinese", "MO Chinese user prompt");
        assertEquals("已生成 AI 推荐结果。", moChineseResult.getMessage(), "MO Chinese fallback message");
        assertEquals("该申请人具备 Java 和测试经验，适合支持实验课。",
                moChineseResult.getRecommendations().get(0).getRecommendation(),
                "MO Chinese recommendation reason");

        CapturingApplicantClient moEnglishClient = new CapturingApplicantClient("", "This applicant has Java and testing experience for lab support.");
        MoApplicantAiSearchService.SearchResult moEnglishResult = new MoApplicantAiSearchService(moEnglishClient)
                .search(job, List.of(application), applicantsByUserId, "Recommend the best applicant");
        assertContains(moEnglishClient.systemPrompt, "English", "MO English system prompt");
        assertContains(moEnglishClient.userPrompt, "Target output language: English", "MO English user prompt");
        assertEquals("AI recommendation results have been generated.", moEnglishResult.getMessage(), "MO English fallback message");
        assertEquals("This applicant has Java and testing experience for lab support.",
                moEnglishResult.getRecommendations().get(0).getRecommendation(),
                "MO English recommendation reason");

        pass("AI recommendation prompts and fallback messages follow the query language");
    }

    /**
     * System Properties were temporarily changed during the test and must be restored afterward.
     * Otherwise, when continuing to run other tests in the same JVM, the configuration left by Zhou Bohan test may be read.
     */
    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    /**
     * Output current test point pass information for easy step-by-step explanation during each contributor's defense.
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[Zhou Bohan] PASS - " + message);
    }

    // Below are lightweight assertion tools to avoid introducing additional frameworks for testing.
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertNotNull(Object value, String message) {
        assertTrue(value != null, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertContains(String text, String expectedPart, String message) {
        if (text == null || !text.contains(expectedPart)) {
            throw new AssertionError(message + " expected to contain=" + expectedPart + " actual=" + text);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (expectedType.isInstance(thrown)) {
                return;
            }
            throw new AssertionError(message + " wrong exception=" + thrown);
        }
        throw new AssertionError(message + " expected exception=" + expectedType.getSimpleName());
    }

    private static final class CapturingTaJobClient extends DeepSeekTaJobSearchClient {
        private final String responseMessage;
        private final String recommendation;
        private String systemPrompt;
        private String userPrompt;

        private CapturingTaJobClient(String responseMessage, String recommendation) {
            super(DeepSeekAiConfig.load(null));
            this.responseMessage = responseMessage;
            this.recommendation = recommendation;
        }

        @Override
        public boolean isConfigured() {
            return true;
        }

        @Override
        public SearchAttempt search(String systemPrompt, String userPrompt) {
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
            return SearchAttempt.success(new SearchPayload(
                    "recommend",
                    responseMessage,
                    List.of(new SearchRecommendation("J1", recommendation))
            ));
        }
    }

    private static final class CapturingApplicantClient extends DeepSeekApplicantSearchClient {
        private final String responseMessage;
        private final String recommendation;
        private String systemPrompt;
        private String userPrompt;

        private CapturingApplicantClient(String responseMessage, String recommendation) {
            super(DeepSeekAiConfig.load(null));
            this.responseMessage = responseMessage;
            this.recommendation = recommendation;
        }

        @Override
        public boolean isConfigured() {
            return true;
        }

        @Override
        public SearchAttempt search(String systemPrompt, String userPrompt) {
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
            return SearchAttempt.success(new SearchPayload(
                    "recommend",
                    responseMessage,
                    List.of(new SearchRecommendation("C1", recommendation))
            ));
        }
    }
}

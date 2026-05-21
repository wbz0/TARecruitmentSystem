import com.example.tarecruitment.ai.client.DeepSeekAiConfig;
import com.example.tarecruitment.common.storage.CsvCodec;
import com.example.tarecruitment.common.storage.StoragePaths;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.model.Applicant;

import java.util.Arrays;

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
}

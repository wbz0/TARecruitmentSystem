import com.example.tarecruitment.ai.client.DeepSeekAiConfig;
import com.example.tarecruitment.common.storage.CsvCodec;
import com.example.tarecruitment.common.storage.StoragePaths;
import com.example.tarecruitment.profile.dao.ApplicantDao;
import com.example.tarecruitment.profile.model.Applicant;

import java.util.Arrays;

/**
 * member2 后端测试入口。
 *
 * member2 的职责重点是 TA 档案、文件/路径数据、CSV 存储，以及 DeepSeek 推荐搜索配置。
 * 本测试同样采用 main 方法直接运行，不依赖 JUnit。
 *
 * scripts/test/test-member2.sh 会把 TA_HIRING_DATA_DIR 指向临时目录，
 * 因此 ApplicantDao 创建的 applicants.csv 只会出现在 build/member-tests/member2/data 下。
 */
public class Member2BackendTest {

    private static int passed;

    public static void main(String[] args) {
        // 先验证底层 CSV 和路径，再验证档案模型/DAO，最后验证 AI 配置降级。
        testCsvCodec();
        testStoragePaths();
        testApplicantCsvRoundTrip();
        testApplicantDaoDuplicateRules();
        testDeepSeekConfigFallbacks();
        System.out.println("[member2] PASS total=" + passed);
    }

    /**
     * 验证 CSV 转义工具。
     *
     * 申请人姓名、院系、经历等字段都可能包含逗号或引号。
     * 如果 CSV 转义失败，DAO 读回来的列会错位，后续页面显示也会出错。
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
     * 验证 StoragePaths 对运行时数据目录的解释。
     *
     * 项目约定运行时数据不写进仓库，而是写到 TA_HIRING_DATA_DIR 下。
     * 这里确认 applicants、resumes、photos 等目录都从同一个数据根目录派生。
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
     * 验证 Applicant 模型的 CSV 往返。
     *
     * TA 档案字段较多，并且包含技能列表、简历路径、照片路径。
     * 该测试确保这些字段写成 CSV 后再读回来，核心信息没有丢失或错列。
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
     * 验证 ApplicantDao 的唯一性规则。
     *
     * 一个 TA 用户只能有一个档案，同一个学号也不能被多个档案复用。
     * 这些约束放在 DAO 层，可以防止绕过前端表单直接写入重复数据。
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
     * 验证 DeepSeek 配置的安全降级。
     *
     * 答辩或本地运行时不一定配置真实 API key。
     * 当 key 是 replace_me 这类占位符时，isApiKeyConfigured() 必须返回 false，
     * 这样服务层可以返回“AI 暂不可用”，而不是生成本地假推荐。
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
     * 测试过程中临时改了 System Property，结束后必须还原。
     * 否则同一个 JVM 里继续跑别的测试时，可能读到 member2 测试留下的配置。
     */
    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    /**
     * 输出当前测试点通过信息，方便每个成员答辩时逐条解释。
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[member2] PASS - " + message);
    }

    // 下面是极简断言工具，避免为了测试引入额外框架。
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

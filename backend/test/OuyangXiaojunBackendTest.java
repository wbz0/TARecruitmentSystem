import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.service.ServiceResult;

/**
 * Ouyang Xiaojun backend test entry point.
 *
 * This is not a JUnit test, but a lightweight main test program that can be run directly via javac/java.
 * This approach aligns with the project's lightweight tech stack of “Servlet + JSP + native JS + CSV + scripts”,
 * without introducing additional Maven, JUnit, or testing framework dependencies.
 *
 * Test scope corresponds to Ouyang Xiaojun's defense responsibilities:
 * 1. Service layer unified result object ServiceResult;
 * 2. User model User CSV storage format;
 * 3. User DAO demo accounts, login verification, password hashing, and duplicate username protection.
 *
 * scripts/test/test-ouyang-xiaojun.sh will set a temporary TA_HIRING_DATA_DIR for this test,
 * so UserDao writes to the temporary CSV under build/contributor-tests/ouyang-xiaojun/data,
 * which will not affect Tomcat's real demo data.
 */
public class OuyangXiaojunBackendTest {

    private static int passed;

    public static void main(String[] args) {
        // main method is organized in defense presentation order: first test common return structure, then test user storage and login.
        testServiceResult();
        testUserCsvRoundTrip();
        testUserDaoDemoAccountsAndLogin();
        System.out.println("[Ouyang Xiaojun] PASS total=" + passed);
    }

    /**
     * Validate ServiceResult basic contract.
     *
     * Servlet layer ultimately converts service results to HTTP responses, so this confirms:
     * - created() corresponds to 201 and success=true;
     * - forbidden() corresponds to 403 and success=false;
     * - data/message will not be lost in the result object.
     */
    private static void testServiceResult() {
        ServiceResult created = ServiceResult.created("created", "payload");
        assertEquals(201, created.getStatusCode(), "created status");
        assertTrue(created.isSuccess(), "created success flag");
        assertEquals("payload", created.getData(), "created data");

        ServiceResult forbidden = ServiceResult.forbidden("blocked");
        assertEquals(403, forbidden.getStatusCode(), "forbidden status");
        assertFalse(forbidden.isSuccess(), "forbidden success flag");
        pass("ServiceResult keeps service-layer status/message/data contract");
    }

    /**
     * Validate User model CSV serialization compatibility.
     *
     * The project uses CSV as a lightweight data layer, and User.toCsv()/fromCsv() is the persistence protocol for account data.
     * Here, specifically include a displayName with comma to confirm CSV escaping does not break the fields.
     */
    private static void testUserCsvRoundTrip() {
        User user = new User("ouyang_xiaojun_user", "secret", "ouyang-xiaojun@example.test", User.Role.TA);
        user.setUserId("user-001");
        user.setDisplayName("Member One, TA");
        user.setRealName("Alice Zhang");
        user.setProfessionalTitle("Teaching Assistant");
        user.setAvatarPath("account-avatars/alice.png");

        User parsed = User.fromCsv(user.toCsv());
        assertNotNull(parsed, "parsed user");
        assertEquals("user-001", parsed.getUserId(), "user id");
        assertEquals("Member One, TA", parsed.getDisplayName(), "display name csv escaping");
        assertEquals("account-avatars/alice.png", parsed.getAvatarPath(), "avatar path");
        pass("User CSV round-trip preserves account profile fields");
    }

    /**
     * Validate UserDao core authentication behavior.
     *
     * This test first clears the user CSV in the temporary test directory, then re-populates fixed demo accounts.
     * The focus is not on testing pages, but on whether the backend data layer meets basic login/registration constraints:
     * - Three demo accounts can be initialized;
     * - Both username and email can be used for login;
     * - New user passwords are hashed, not stored in plaintext;
     * - Duplicate usernames are rejected.
     */
    private static void testUserDaoDemoAccountsAndLogin() {
        UserDao dao = UserDao.getInstance();
        dao.deleteAll();
        dao.ensureDefaultDemoAccounts();

        assertEquals(3L, dao.count(), "demo account count");
        assertTrue(dao.verifyLogin("ta_demo", "Pass1234").isPresent(), "demo login by username");
        assertTrue(dao.verifyLogin("mo_demo@local.test", "Pass1234").isPresent(), "demo login by email");

        User created = dao.create(new User("ouyang_xiaojun_extra", "Pass1234", "ouyang-xiaojun-extra@example.test", User.Role.TA));
        assertTrue(dao.findByUsername("OUYANG_XIAOJUN_EXTRA").isPresent(), "case-insensitive username lookup");
        assertTrue(!"Pass1234".equals(created.getPassword()), "password is hashed before storage");
        assertThrows(IllegalArgumentException.class,
                () -> dao.create(new User("ouyang_xiaojun_extra", "Pass1234", "ouyang-xiaojun-dup@example.test", User.Role.TA)),
                "duplicate username rejected");
        pass("UserDao initializes demo accounts, verifies login, and rejects duplicates");
    }

    /**
     * Record a passing test point for easy direct viewing of each verification result from terminal output during defense.
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[Ouyang Xiaojun] PASS - " + message);
    }

    // Below are minimalist assertion tools. Custom assertions are used to avoid introducing JUnit dependency.
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

    /**
     * Verify that a piece of logic must throw the specified exception.
     *
     * For example, duplicate username registration must throw IllegalArgumentException;
     * If no exception is thrown, the backend uniqueness protection has failed.
     */
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

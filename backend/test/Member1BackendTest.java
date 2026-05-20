import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.service.ServiceResult;

/**
 * member1 后端测试入口。
 *
 * 这不是 JUnit 测试，而是一个可以直接通过 javac/java 运行的轻量 main 测试程序。
 * 这样做是为了符合当前项目“Servlet + JSP + 原生 JS + CSV + 脚本”的轻量技术栈，
 * 不额外引入 Maven、JUnit 或测试框架。
 *
 * 测试范围对应 member1 的答辩职责：
 * 1. service 层统一结果对象 ServiceResult；
     * 2. 用户模型 User 的 CSV 存储格式；
     * 3. 用户 DAO 的演示账号、登录验证、密码哈希和重复用户名保护。
 *
 * scripts/test/test-member1.sh 会给本测试设置临时 TA_HIRING_DATA_DIR，
 * 所以 UserDao 写入的是 build/member-tests/member1/data 下的临时 CSV，
 * 不会影响 Tomcat 真实演示数据。
 */
public class Member1BackendTest {

    private static int passed;

    public static void main(String[] args) {
        // main 方法按答辩展示顺序组织：先测公共返回结构，再测用户存储和登录。
        testServiceResult();
        testUserCsvRoundTrip();
        testUserDaoDemoAccountsAndLogin();
        System.out.println("[member1] PASS total=" + passed);
    }

    /**
     * 验证 ServiceResult 的基本契约。
     *
     * Servlet 层最终会把 service 结果转成 HTTP 响应，因此这里确认：
     * - created() 对应 201 和 success=true；
     * - forbidden() 对应 403 和 success=false；
     * - data/message 不会在结果对象中丢失。
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
     * 验证 User 模型的 CSV 序列化兼容性。
     *
     * 项目使用 CSV 作为轻量数据层，User.toCsv()/fromCsv() 就是账号数据的持久化协议。
     * 这里专门放入带逗号的 displayName，确认 CSV 转义不会把字段拆坏。
     */
    private static void testUserCsvRoundTrip() {
        User user = new User("member1_user", "secret", "member1@example.test", User.Role.TA);
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
     * 验证 UserDao 的核心认证行为。
     *
     * 这个测试会先清空临时测试目录中的用户 CSV，然后重新补齐固定演示账号。
     * 重点不是测页面，而是测后端数据层是否满足登录注册的基础约束：
     * - 三个演示账号能被初始化；
     * - 用户名和邮箱都可以登录；
     * - 新用户密码会被哈希，不明文保存；
     * - 重复用户名会被拒绝。
     */
    private static void testUserDaoDemoAccountsAndLogin() {
        UserDao dao = UserDao.getInstance();
        dao.deleteAll();
        dao.ensureDefaultDemoAccounts();

        assertEquals(3L, dao.count(), "demo account count");
        assertTrue(dao.verifyLogin("ta_demo", "Pass1234").isPresent(), "demo login by username");
        assertTrue(dao.verifyLogin("mo_demo@local.test", "Pass1234").isPresent(), "demo login by email");

        User created = dao.create(new User("member1_extra", "Pass1234", "member1-extra@example.test", User.Role.TA));
        assertTrue(dao.findByUsername("MEMBER1_EXTRA").isPresent(), "case-insensitive username lookup");
        assertTrue(!"Pass1234".equals(created.getPassword()), "password is hashed before storage");
        assertThrows(IllegalArgumentException.class,
                () -> dao.create(new User("member1_extra", "Pass1234", "member1-dup@example.test", User.Role.TA)),
                "duplicate username rejected");
        pass("UserDao initializes demo accounts, verifies login, and rejects duplicates");
    }

    /**
     * 记录一个通过的测试点，方便答辩时从终端输出直接看到每一步验证结果。
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[member1] PASS - " + message);
    }

    // 下面是极简断言工具。使用自定义断言是为了避免引入 JUnit 依赖。
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
     * 验证某段逻辑必须抛出指定异常。
     *
     * 例如重复用户名注册必须抛 IllegalArgumentException；
     * 如果没有抛异常，说明后端唯一性保护失效。
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

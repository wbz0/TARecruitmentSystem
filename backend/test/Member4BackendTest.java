import com.example.tarecruitment.admin.service.InviteCodeService;
import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.application.validator.ApplicationValidator;
import com.example.tarecruitment.notification.model.Notification;

/**
 * member4 后端测试入口。
 *
 * member4 的职责重点是申请流程、状态流转、通知和管理员邀请码。
 * 本测试不启动 Servlet 容器，而是直接测试 validator、model 和 DAO，
 * 因为这些类承载了答辩时最容易讲清楚的核心业务规则。
 */
public class Member4BackendTest {

    private static int passed;

    public static void main(String[] args) {
        // 按“输入校验 -> 数据格式 -> 状态流转 -> 通知/短邀请码”的顺序组织输出。
        testApplicationValidation();
        testApplicationCsvRoundTrip();
        testApplicationDaoTransitions();
        testNotificationCsvRoundTrip();
        testInviteCodeValidation();
        System.out.println("[member4] PASS total=" + passed);
    }

    /**
     * 验证申请接口的输入校验。
     *
     * ApplicationValidator 只负责 HTTP 参数层面的安全和格式：
     * - jobId/applicationId 不能为空；
     * - coverLetter 不能包含明显 HTML/JS 注入；
     * - transition action 只能是 accept/reject/withdraw。
     */
    private static void testApplicationValidation() {
        assertNull(ApplicationValidator.validateJobId("job-001"), "valid job id");
        assertEquals("Job ID is required", ApplicationValidator.validateJobId(" "), "blank job id");
        assertEquals("Cover letter contains unsupported characters",
                ApplicationValidator.validateCoverLetter("<img src=x onerror=alert(1)>"),
                "unsafe cover letter");
        assertNull(ApplicationValidator.validateTransitionAction(" ACCEPT "), "accept action");
        assertEquals("Invalid action. Use 'accept', 'reject', or 'withdraw'",
                ApplicationValidator.validateTransitionAction("approve"),
                "invalid transition action");
        pass("ApplicationValidator checks IDs, cover letters, and transition actions");
    }

    /**
     * 验证 Application 模型的 CSV 格式。
     *
     * 申请数据需要保存申请人、职位、状态和进度阶段。
     * 这里把 applicantName 设成带逗号的值，确认 CSV 转义不会导致错列。
     */
    private static void testApplicationCsvRoundTrip() {
        Application application = new Application("job-4", "applicant-4", "Alice, TA", "alice@example.test");
        application.setApplicationId("application-004");
        application.setJobTitle("Software Engineering TA");
        application.setCourseCode("SE604");
        application.setMoId("mo-4");
        application.setMoName("MO Four");
        application.setCoverLetter("I can support labs, testing, and feedback.");

        Application parsed = Application.fromCsv(application.toCsv());
        assertNotNull(parsed, "parsed application");
        assertEquals("Alice, TA", parsed.getApplicantName(), "applicant name csv escaping");
        assertEquals(Application.Status.PENDING, parsed.getStatus(), "default status");
        assertEquals(Application.ProgressStage.UNDER_REVIEW, parsed.getProgressStage(), "default review stage");
        pass("Application CSV round-trip preserves applicant and progress fields");
    }

    /**
     * 验证申请 DAO 的状态流转。
     *
     * 这里模拟两个核心场景：
     * - MO 接受申请：状态变为 ACCEPTED，进度变为 COMPLETED；
     * - TA 撤回申请：状态变为 WITHDRAWN。
     *
     * 这些变化都会写入临时 applications.csv，再通过 findById 读回验证。
     */
    private static void testApplicationDaoTransitions() {
        ApplicationDao dao = ApplicationDao.getInstance();
        dao.deleteAll();

        Application first = new Application("job-4", "applicant-4-a", "Alice", "alice@example.test");
        first.setCourseCode("SE604");
        dao.create(first);
        assertTrue(dao.hasApplied("job-4", "applicant-4-a"), "has applied");
        assertEquals(1L, dao.countPendingByJobId("job-4"), "pending count");
        assertTrue(dao.accept(first.getApplicationId()), "accept transition");

        Application accepted = dao.findById(first.getApplicationId()).get();
        assertEquals(Application.Status.ACCEPTED, accepted.getStatus(), "accepted status");
        assertEquals(Application.ProgressStage.COMPLETED, accepted.getProgressStage(), "accepted completed stage");
        assertEquals(1L, dao.countAcceptedByJobId("job-4"), "accepted count");

        Application second = new Application("job-4", "applicant-4-b", "Bob", "bob@example.test");
        dao.create(second);
        assertTrue(dao.withdraw(second.getApplicationId()), "withdraw transition");
        assertEquals(Application.Status.WITHDRAWN, dao.findById(second.getApplicationId()).get().getStatus(),
                "withdrawn status");
        pass("ApplicationDao stores applications and applies accept/withdraw status transitions");
    }

    /**
     * 验证系统公告 Notification 的 CSV 往返。
     *
     * 通知页依赖标题、正文、发布者快照和发布时间。
     * 这里确认含逗号的标题不会破坏 CSV 字段边界。
     */
    private static void testNotificationCsvRoundTrip() {
        Notification notification = new Notification();
        notification.setNotificationId("notice-004");
        notification.setTitle("Interview update, week 4");
        notification.setContent("Please check your application status.");
        notification.setPublishedByUserId("admin-4");
        notification.setPublishedByUsername("admin_demo");

        Notification parsed = Notification.fromCsv(notification.toCsv());
        assertNotNull(parsed, "parsed notification");
        assertEquals("Interview update, week 4", parsed.getTitle(), "notification title csv escaping");
        assertEquals("admin_demo", parsed.getPublishedByUsername(), "publisher snapshot");
        pass("Notification CSV round-trip preserves published message fields");
    }

    /**
     * 验证当前管理员短邀请码流程。
     *
     * InviteCodeService 使用服务端密钥和时间窗口生成 8 位短码。
     * 测试只校验当前可见流程：当前码可用、大小写/空格可容忍、明显错误码会被拒绝。
     */
    private static void testInviteCodeValidation() {
        InviteCodeService service = InviteCodeService.getInstance();
        String currentCode = service.getCurrentCode();

        assertEquals(8, currentCode.length(), "invite code length");
        assertTrue(service.isValidCode(currentCode), "current invite code");
        assertTrue(service.isValidCode(" " + currentCode.toLowerCase() + " "), "normalized invite code");
        assertTrue(!service.isValidCode("INVALID1"), "invalid invite code");
        assertTrue(service.getSecondsRemaining() >= 0 && service.getSecondsRemaining() <= 600,
                "invite code countdown range");
        pass("InviteCodeService validates the visible short-code invitation flow");
    }

    /**
     * 输出当前测试点通过信息，便于答辩时逐条讲解测试过程。
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[member4] PASS - " + message);
    }

    // 自定义断言工具。失败时抛 AssertionError，shell 脚本会立即判定该成员测试失败。
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNull(Object value, String message) {
        if (value != null) {
            throw new AssertionError(message + " expected null actual=" + value);
        }
    }

    private static void assertNotNull(Object value, String message) {
        assertTrue(value != null, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}

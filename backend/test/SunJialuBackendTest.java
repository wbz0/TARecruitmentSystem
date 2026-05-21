import com.example.tarecruitment.admin.service.InviteCodeService;
import com.example.tarecruitment.application.dao.ApplicationDao;
import com.example.tarecruitment.application.model.Application;
import com.example.tarecruitment.application.validator.ApplicationValidator;
import com.example.tarecruitment.notification.model.Notification;

/**
 * Sun Jialu backend test entry point.
 *
 * Sun Jialu's responsibilities focus on application flow, status transitions, notifications, and admin invite codes.
 * This test does not start a Servlet container, but directly tests validators, models, and DAOs,
 * because these classes carry the core business rules that are easiest to explain during defense.
 */
public class SunJialuBackendTest {

    private static int passed;

    public static void main(String[] args) {
        // Organized in the order of “input validation -> data format -> status transition -> notification/short invite code”.
        testApplicationValidation();
        testApplicationCsvRoundTrip();
        testApplicationDaoTransitions();
        testNotificationCsvRoundTrip();
        testInviteCodeValidation();
        System.out.println("[Sun Jialu] PASS total=" + passed);
    }

    /**
     * Validate application interface input.
     *
     * ApplicationValidator is only responsible for HTTP parameter-level security and format:
     * - jobId/applicationId cannot be empty;
     * - coverLetter cannot contain obvious HTML/JS injection;
     * - transition action can only be accept/reject/withdraw.
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
     * Validate Application model CSV format.
     *
     * Application data needs to save applicant, job, status, and progress stage.
     * Here, applicantName is set to a value with comma to confirm CSV escaping does not cause column misalignment.
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
     * Validate Application DAO status transitions.
     *
     * This simulates two core scenarios:
     * - MO accepts application: status becomes ACCEPTED, progress becomes COMPLETED;
     * - TA withdraws application: status becomes WITHDRAWN.
     *
     * These changes are written to the temporary applications.csv, then verified by reading back through findById.
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
     * Validate system notification Notification CSV round-trip.
     *
     * Notification page depends on title, content, publisher snapshot, and publish time.
     * Here, we confirm that a title with comma does not break CSV field boundaries.
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
     * Validate current admin short invite code flow.
     *
     * InviteCodeService uses server-side key and time window to generate an 8-character short code.
     * This test only validates the currently visible flow: current code is usable, case sensitivity/spaces are tolerated, and obviously wrong codes are rejected.
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
     * Output current test point pass information for easy step-by-step explanation during defense.
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[Sun Jialu] PASS - " + message);
    }

    // Custom assertion tool. Throws AssertionError on failure, and shell script will immediately determine test failure for this contributor.
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

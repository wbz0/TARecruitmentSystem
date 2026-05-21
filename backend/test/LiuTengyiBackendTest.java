import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.job.mapper.JobRequestMapper;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.job.validator.JobValidator;
import com.example.tarecruitment.profile.validator.AccountProfileValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Liu Tengyi backend test entry point.
 *
 * Liu Tengyi's responsibilities focus on job posting/query/validation and account profile synchronization.
 * This uses lightweight main tests to cover these business rules instead of starting Tomcat for page tests.
 */
public class LiuTengyiBackendTest {

    private static int passed;

    public static void main(String[] args) {
        // Starting from form validation, then testing job status/DAO, and finally testing account profile.
        testJobValidationRules();
        testJobEffectiveStatus();
        testJobDaoSearchAndStatus();
        testAccountProfileValidation();
        System.out.println("[Liu Tengyi] PASS total=" + passed);
    }

    /**
     * Validate job creation form backend validation.
     *
     * Here, construct a complete valid job to confirm validateCreate returns null;
     * Also add several typical invalid inputs:
     * - skills using semicolons will be rejected;
     * - duplicate skills will be rejected;
     * - title containing HTML/JS fragments will be rejected.
     */
    private static void testJobValidationRules() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        LocalDate start = deadline.toLocalDate().plusDays(1);
        LocalDate end = start.plusWeeks(8);
        String deadlineText = deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        String validError = JobValidator.validateCreate(
                "Teaching Assistant",
                "SE601",
                "Software Engineering",
                "Support tutorials and labs",
                "Java, Testing, CSV",
                "3",
                "8.5",
                start.toString(),
                end.toString(),
                "20 GBP/hour",
                deadlineText
        );
        assertNull(validError, "valid job payload");
        assertEquals("Please use English commas or Chinese commas to separate skills",
                JobValidator.validateSkills("Java;Testing", true),
                "skill separator validation");
        assertEquals("Duplicate skills found. Please keep each skill only once",
                JobValidator.validateSkills("Java, java", true),
                "duplicate skill validation");
        assertEquals("Job title contains unsupported characters",
                JobValidator.validateTitle("<script>alert(1)</script>", true),
                "dangerous title validation");
        assertEquals(8.5, JobRequestMapper.parseWeeklyHours("8.5"), "weekly hours parser");
        pass("JobValidator accepts valid jobs and rejects unsafe or ambiguous fields");
    }

    /**
     * Validate job “effective status” rules.
     *
     * The status saved in data may still be OPEN, but if the deadline has passed,
     * the page list and statistics should treat it as CLOSED.
     */
    private static void testJobEffectiveStatus() {
        Job job = new Job("mo-3", "MO Three", "Tutor", "SE602");
        job.setStatus(Job.Status.OPEN);
        job.setDeadline(LocalDateTime.now().minusDays(1));
        assertEquals(Job.Status.CLOSED, job.getEffectiveStatus(LocalDateTime.now()), "past deadline closes job");

        job.setStatus(Job.Status.FILLED);
        assertEquals(Job.Status.FILLED, job.getEffectiveStatus(LocalDateTime.now()), "filled status is preserved");
        pass("Job effective status handles deadlines and final states");
    }

    /**
     * Validate JobDao basic data flow.
     *
     * This test creates jobs in the temporary data directory, then verifies:
     * - open job count;
     * - keyword search covers description field;
     * - status update can be written back to CSV and read out again.
     */
    private static void testJobDaoSearchAndStatus() {
        JobDao dao = JobDao.getInstance();
        dao.deleteAll();

        Job job = new Job("mo-3", "MO Three", "Software Engineering TA", "SE603");
        job.setCourseName("Software Engineering");
        job.setDescription("Support architecture and testing labs");
        job.setRequiredSkillsFromString("Java, Testing");
        job.setPositions(2);
        job.setWeeklyHours(6.0);
        job.setWorkStartDate(LocalDate.now().plusDays(3));
        job.setWorkEndDate(LocalDate.now().plusWeeks(8));
        job.setSalary("20 GBP/hour");
        job.setDeadline(LocalDateTime.now().plusDays(2));
        dao.create(job);

        assertEquals(1L, dao.countOpenJobs(), "open job count");
        assertEquals(1, dao.search("architecture").size(), "fuzzy search finds description");
        assertTrue(dao.updateStatus(job.getJobId(), Job.Status.CLOSED), "status update returns true");
        assertEquals(Job.Status.CLOSED, dao.findById(job.getJobId()).get().getStatus(), "stored status updated");
        pass("JobDao stores jobs, searches fields, and updates status");
    }

    /**
     * Validate account profile validator.
     *
     * Account profile is displayed in sidebar, top bar, and TA profile link locations, so this tests:
     * username format, real name requirement when TA already has a profile, and uploaded file name sanitization.
     */
    private static void testAccountProfileValidation() {
        assertNull(AccountProfileValidator.validateUsernameFormat("liu_tengyi"), "valid username");
        assertEquals("Username cannot end with an underscore",
                AccountProfileValidator.validateUsernameFormat("liu_tengyi_"),
                "username trailing underscore");
        assertEquals("Full name is required.",
                AccountProfileValidator.validateTaSharedRealName(" ", true),
                "TA profile real name required");
        assertEquals("resume_file", AccountProfileValidator.sanitizeBaseName("../resume file.pdf", "fallback"),
                "safe file base name");
        pass("AccountProfileValidator protects account names and uploaded file names");
    }

    /**
     * Output current test point pass information.
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[Liu Tengyi] PASS - " + message);
    }

    // Custom lightweight assertion tool, throws AssertionError on failure to make script exit with non-zero.
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

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}

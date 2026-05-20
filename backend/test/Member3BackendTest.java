import com.example.tarecruitment.job.dao.JobDao;
import com.example.tarecruitment.job.mapper.JobRequestMapper;
import com.example.tarecruitment.job.model.Job;
import com.example.tarecruitment.job.validator.JobValidator;
import com.example.tarecruitment.profile.validator.AccountProfileValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * member3 后端测试入口。
 *
 * member3 的职责重点是职位发布/查询/校验，以及账号资料同步。
 * 这里用轻量 main 测试覆盖这些业务规则，而不是启动 Tomcat 做页面测试。
 */
public class Member3BackendTest {

    private static int passed;

    public static void main(String[] args) {
        // 从表单校验开始，再测试职位状态/DAO，最后测试账号资料。
        testJobValidationRules();
        testJobEffectiveStatus();
        testJobDaoSearchAndStatus();
        testAccountProfileValidation();
        System.out.println("[member3] PASS total=" + passed);
    }

    /**
     * 验证职位创建表单的后端校验。
     *
     * 这里构造一个完整合法职位，确认 validateCreate 返回 null；
     * 同时补充几个典型错误输入：
     * - 技能使用分号会被拒绝；
     * - 重复技能会被拒绝；
     * - 标题包含 HTML/JS 片段会被拒绝。
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
     * 验证职位的“有效状态”规则。
     *
     * 数据里保存的 status 可能还是 OPEN，但如果截止时间已过，
     * 页面列表和统计应当把它视为 CLOSED。
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
     * 验证 JobDao 的基本数据流程。
     *
     * 测试会在临时数据目录中创建职位，随后验证：
     * - 开放职位数量；
     * - 关键词搜索覆盖描述字段；
     * - 状态更新可以写回 CSV 并再次读出。
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
     * 验证账号资料校验器。
     *
     * 账号资料会显示在侧边栏、顶栏和 TA 档案联动位置，因此这里测试：
     * 用户名格式、TA 已有档案后的实名要求，以及上传文件名清洗。
     */
    private static void testAccountProfileValidation() {
        assertNull(AccountProfileValidator.validateUsernameFormat("member_3"), "valid username");
        assertEquals("Username cannot end with an underscore",
                AccountProfileValidator.validateUsernameFormat("member3_"),
                "username trailing underscore");
        assertEquals("Full name is required.",
                AccountProfileValidator.validateTaSharedRealName(" ", true),
                "TA profile real name required");
        assertEquals("resume_file", AccountProfileValidator.sanitizeBaseName("../resume file.pdf", "fallback"),
                "safe file base name");
        pass("AccountProfileValidator protects account names and uploaded file names");
    }

    /**
     * 输出当前测试点通过信息。
     */
    private static void pass(String message) {
        passed++;
        System.out.println("[member3] PASS - " + message);
    }

    // 自定义轻量断言工具，失败时抛 AssertionError 让脚本退出非 0。
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

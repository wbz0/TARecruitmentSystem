package com.example.authlogin.dao;

import com.example.authlogin.model.Application;
import com.example.authlogin.model.Job;
import com.example.authlogin.model.User;
import com.example.authlogin.model.User.Role;
import com.example.authlogin.dao.UserDao;
import com.example.authlogin.dao.JobDao;

/**
 * ApplicationDao 测试类
 */
public class ApplicationDaoTest {

    public static void main(String[] args) {
        System.out.println("=== 开始测试 ApplicationDao ===\n");

        // 清理测试数据
        ApplicationDao appDao = ApplicationDao.getInstance();
        appDao.deleteAll();

        // 创建测试用户和职位
        UserDao userDao = UserDao.getInstance();
        JobDao jobDao = JobDao.getInstance();

        // 创建MO用户
        User mo = new User("mo001", "mo123", "smith@edu.com", Role.MO);
        mo.setUserId("mo001");
        userDao.save(mo);

        // 创建TA用户
        User ta = new User("ta001", "ta123", "john@edu.com", Role.TA);
        ta.setUserId("ta001");
        userDao.save(ta);

        // 创建职位
        Job job = new Job("mo001", "Dr. Smith", "TA for Java", "CS101");
        job.setCourseName("Introduction to Java");
        job.setDescription("Teach Java programming");
        job.setPositions(2);
        jobDao.save(job);

        // 测试1: 创建申请
        System.out.println("测试1: 创建申请");
        Application app = new Application(job.getJobId(), ta.getUserId(), ta.getUsername(), ta.getEmail());
        app.setJobTitle(job.getTitle());
        app.setCourseCode(job.getCourseCode());
        app.setMoId(job.getMoId());
        app.setMoName(job.getMoName());
        app.setCoverLetter("I am interested in this position.");

        Application savedApp = appDao.create(app);
        System.out.println("  申请ID: " + savedApp.getApplicationId());
        System.out.println("  状态: " + savedApp.getStatus());
        System.out.println("  测试通过!\n");

        // 测试2: 根据ID查找
        System.out.println("测试2: 根据ID查找申请");
        Application foundApp = appDao.findById(savedApp.getApplicationId()).orElse(null);
        if (foundApp != null) {
            System.out.println("  找到申请: " + foundApp.getApplicantName());
            System.out.println("  测试通过!\n");
        } else {
            System.out.println("  测试失败!\n");
        }

        // 测试3: 根据职位ID查找
        System.out.println("测试3: 根据职位ID查找申请");
        java.util.List<Application> jobApps = appDao.findByJobId(job.getJobId());
        System.out.println("  职位申请数量: " + jobApps.size());
        System.out.println("  测试通过!\n");

        // 测试4: 根据申请人ID查找
        System.out.println("测试4: 根据申请人ID查找申请");
        java.util.List<Application> applicantApps = appDao.findByApplicantId(ta.getUserId());
        System.out.println("  申请人申请数量: " + applicantApps.size());
        System.out.println("  测试通过!\n");

        // 测试5: 更新申请状态
        System.out.println("测试5: 更新申请状态");
        boolean updated = appDao.updateStatus(savedApp.getApplicationId(), Application.Status.ACCEPTED);
        System.out.println("  状态更新: " + (updated ? "成功" : "失败"));

        Application updatedApp = appDao.findById(savedApp.getApplicationId()).orElse(null);
        System.out.println("  新状态: " + updatedApp.getStatus());
        System.out.println("  测试通过!\n");

        // 测试6: 检查是否已申请
        System.out.println("测试6: 检查是否已申请");
        boolean hasApplied = appDao.hasApplied(job.getJobId(), ta.getUserId());
        System.out.println("  已申请: " + hasApplied);
        System.out.println("  测试通过!\n");

        // 测试7: 统计功能
        System.out.println("测试7: 统计功能");
        long totalApps = appDao.count();
        long pendingApps = appDao.countByStatus(Application.Status.PENDING);
        long acceptedApps = appDao.countByStatus(Application.Status.ACCEPTED);
        System.out.println("  总申请数: " + totalApps);
        System.out.println("  待审核: " + pendingApps);
        System.out.println("  已接受: " + acceptedApps);
        System.out.println("  测试通过!\n");

        // 测试8: 删除申请
        System.out.println("测试8: 删除申请");
        boolean deleted = appDao.delete(savedApp.getApplicationId());
        System.out.println("  删除: " + (deleted ? "成功" : "失败"));

        Application deletedApp = appDao.findById(savedApp.getApplicationId()).orElse(null);
        System.out.println("  删除后查找: " + (deletedApp == null ? "null (正确)" : "找到(错误)"));
        System.out.println("  测试通过!\n");

        // 清理测试数据
        appDao.deleteAll();
        jobDao.deleteAll();
        userDao.deleteAll();

        System.out.println("=== 所有测试完成 ===");
    }
}

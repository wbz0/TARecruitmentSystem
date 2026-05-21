package com.example.tarecruitment.demo;

import com.example.tarecruitment.auth.dao.UserDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * 应用启动时预热演示数据，确保欢迎页访问前已补齐固定测试账号和示例业务数据。
 *
 * 这不是生产迁移脚本：它只在本地演示数据缺失时补齐，不会清空用户已有 CSV。
 */
@WebListener
public class DemoAccountBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            UserDao.getInstance();
            DemoDataSeeder.SeedSummary summary = DemoDataSeeder.createDefault().seed();
            sce.getServletContext().log("Demo data initialized: " + summary);
        } catch (RuntimeException e) {
            sce.getServletContext().log("Demo data initialization will retry on first request", e);
        }
    }
}

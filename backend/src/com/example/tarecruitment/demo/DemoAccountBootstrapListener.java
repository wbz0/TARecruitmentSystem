package com.example.tarecruitment.demo;

import com.example.tarecruitment.auth.dao.UserDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Warms up demo data on application startup, ensuring fixed test accounts and sample business data are populated before welcome page access.
 *
 * This is not a production migration script: it only supplements missing local demo data, does not clear existing user CSV.
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

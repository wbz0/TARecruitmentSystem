package com.example.authlogin.integration;

import com.example.authlogin.dao.UserDao;
import com.example.authlogin.model.User;

/**
 * 登录注册模块集成测试
 */
public class LoginRegisterIntegrationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        UserDao userDao = UserDao.getInstance();
        userDao.deleteAll();

        try {
            test("Register user should persist and hash password", () -> {
                User user = new User("lr_user", "Pass1234", "lr_user@example.com", User.Role.TA);
                User saved = userDao.create(user);
                assert saved.getUserId() != null : "user id should not be null";
                assert !"Pass1234".equals(saved.getPassword()) : "password should be hashed";
            });

            test("Login by username should succeed", () -> {
                assert userDao.verifyLogin("lr_user", "Pass1234").isPresent()
                        : "login with username should succeed";
            });

            test("Login by email should succeed", () -> {
                assert userDao.verifyLogin("lr_user@example.com", "Pass1234").isPresent()
                        : "login with email should succeed";
            });

            test("Duplicate username should be rejected", () -> {
                try {
                    userDao.create(new User("lr_user", "Another123", "another@example.com", User.Role.TA));
                    throw new AssertionError("duplicate username should throw");
                } catch (IllegalArgumentException expected) {
                    assert expected.getMessage().contains("Username already exists");
                }
            });

            test("Wrong password should fail login", () -> {
                assert userDao.verifyLogin("lr_user", "WrongPass").isEmpty()
                        : "wrong password should fail login";
            });
        } finally {
            userDao.deleteAll();
        }

        System.out.println("========================================");
        System.out.println("LoginRegisterIntegrationTest Summary");
        System.out.println("========================================");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));
        System.out.println("========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, Runnable runnable) {
        try {
            runnable.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + name + " - " + t.getMessage());
        }
    }
}

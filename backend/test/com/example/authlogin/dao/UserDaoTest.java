package com.example.authlogin.dao;

import com.example.authlogin.model.User;

/**
 * UserDao Manual Test Runner
 * Run this class to test UserDao functionality
 */
public class UserDaoTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       UserDao Manual Tests");
        System.out.println("========================================\n");

        UserDao userDao = UserDao.getInstance();
        userDao.deleteAll(); // Clean up before tests

        try {
            // Test 1: Create User
            test("Create User", () -> {
                User user = new User("testuser", "password123", "test@example.com", User.Role.TA);
                User saved = userDao.create(user);
                assert saved.getUserId() != null : "User ID should not be null";
                assert "testuser".equals(saved.getUsername()) : "Username mismatch";
                assert "test@example.com".equals(saved.getEmail()) : "Email mismatch";
                assert saved.getPassword() != null && !saved.getPassword().equals("password123") : "Password should be hashed";
            });

            // Test 2: Username Uniqueness
            test("Username Uniqueness", () -> {
                try {
                    User user = new User("testuser", "password456", "test2@example.com", User.Role.TA);
                    userDao.create(user);
                    throw new AssertionError("Should throw exception for duplicate username");
                } catch (IllegalArgumentException e) {
                    assert e.getMessage().contains("Username already exists");
                }
            });

            // Test 3: Find by Username
            test("Find by Username", () -> {
                User user = new User("findme", "pass", "findme@example.com", User.Role.MO);
                userDao.create(user);

                User found = userDao.findByUsername("findme")
                        .orElseThrow(() -> new AssertionError("User not found"));
                assert "findme@example.com".equals(found.getEmail());
            });

            // Test 4: Find by Role
            test("Find by Role", () -> {
                userDao.create(new User("ta1", "p", "ta1@e.com", User.Role.TA));
                userDao.create(new User("ta2", "p", "ta2@e.com", User.Role.TA));
                userDao.create(new User("mo1", "p", "mo1@e.com", User.Role.MO));

                assert userDao.findByRole(User.Role.TA).size() == 2 : "Should have 2 TAs";
                assert userDao.findByRole(User.Role.MO).size() == 1 : "Should have 1 MO";
            });

            // Test 5: Verify Login
            test("Verify Login", () -> {
                User user = new User("loginuser", "mypassword", "login@example.com", User.Role.TA);
                userDao.create(user);

                assert userDao.verifyLogin("loginuser", "mypassword").isPresent() : "Valid login should succeed";
                assert !userDao.verifyLogin("loginuser", "wrongpass").isPresent() : "Wrong password should fail";
                assert !userDao.verifyLogin("nonexistent", "pass").isPresent() : "Nonexistent user should fail";
            });

            // Test 6: Update User
            test("Update User", () -> {
                User user = new User("updateuser", "pass", "old@email.com", User.Role.TA);
                userDao.create(user);

                user.setEmail("new@email.com");
                user.setRole(User.Role.MO);
                User updated = userDao.update(user);

                assert "new@email.com".equals(updated.getEmail()) : "Email should be updated";
                assert updated.getRole() == User.Role.MO : "Role should be updated";
            });

            // Test 7: Delete User
            test("Delete User", () -> {
                User user = new User("deleteuser", "pass", "del@email.com", User.Role.TA);
                userDao.create(user);

                assert userDao.delete(user.getUserId()) : "Delete should return true";
                assert !userDao.findById(user.getUserId()).isPresent() : "User should be deleted";
            });

            // Test 8: Password Hashing
            test("Password Hashing", () -> {
                User user = new User("hashuser", "secret123", "hash@example.com", User.Role.TA);
                userDao.create(user);

                User found = userDao.findByUsername("hashuser").get();
                assert !found.getPassword().equals("secret123") : "Password should be hashed";
            });

            // Test 9: Admin Role
            test("Admin Role", () -> {
                userDao.create(new User("admin", "adminpass", "admin@example.com", User.Role.ADMIN));
                assert userDao.findByRole(User.Role.ADMIN).size() == 1 : "Should have 1 admin";
            });

            // Test 10: Get All Users
            test("Get All Users", () -> {
                long count = userDao.count();
                assert count > 0 : "Should have users";
            });

        } finally {
            // Cleanup
            userDao.deleteAll();
        }

        // Print summary
        System.out.println("\n========================================");
        System.out.println("           Test Summary");
        System.out.println("========================================");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));
        System.out.println("========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, Runnable testCode) {
        try {
            testCode.run();
            System.out.println("[PASS] " + name);
            passed++;
        } catch (Throwable e) {
            System.out.println("[FAIL] " + name + " - " + e.getMessage());
            failed++;
        }
    }
}

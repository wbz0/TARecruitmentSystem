package com.example.authlogin.service;

import com.example.authlogin.model.Application;

import java.util.Arrays;

/**
 * WorkloadStatsService 自动化测试
 */
public class WorkloadStatsServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        WorkloadStatsService service = new WorkloadStatsService();

        test("Service class should provide counts object", () -> {
            Application a1 = new Application();
            Application a2 = new Application();

            WorkloadStatsService.ApplicationCounts counts = service.calculateApplicationCounts(Arrays.asList(a1, a2));
            assert counts != null : "counts should not be null";
            assert counts.getTotal() == 2 : "total should equal input size";
        });

        System.out.println("========================================");
        System.out.println("WorkloadStatsServiceTest Summary");
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

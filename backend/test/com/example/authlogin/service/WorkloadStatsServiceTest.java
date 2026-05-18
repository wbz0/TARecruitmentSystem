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
            a1.setStatus(Application.Status.PENDING);
            a2.setStatus(Application.Status.ACCEPTED);

            WorkloadStatsService.ApplicationCounts counts = service.calculateApplicationCounts(Arrays.asList(a1, a2));
            assert counts != null : "counts should not be null";
            assert counts.getTotal() == 2 : "total should equal input size";
            assert counts.getPending() == 1 : "pending count should be 1";
            assert counts.getAccepted() == 1 : "accepted count should be 1";
        });

        test("Count stats should include all statuses", () -> {
            Application p = new Application();
            Application a = new Application();
            Application r = new Application();
            Application w = new Application();

            p.setStatus(Application.Status.PENDING);
            a.setStatus(Application.Status.ACCEPTED);
            r.setStatus(Application.Status.REJECTED);
            w.setStatus(Application.Status.WITHDRAWN);

            WorkloadStatsService.ApplicationCounts counts = service.calculateApplicationCounts(Arrays.asList(p, a, r, w));
            assert counts.getTotal() == 4 : "total should be 4";
            assert counts.getPending() == 1 : "pending should be 1";
            assert counts.getAccepted() == 1 : "accepted should be 1";
            assert counts.getRejected() == 1 : "rejected should be 1";
            assert counts.getWithdrawn() == 1 : "withdrawn should be 1";
        });

        test("MO workload stats should aggregate processed work", () -> {
            Application app1 = new Application();
            app1.setMoId("mo-1");
            app1.setMoName("Dr. A");
            app1.setStatus(Application.Status.PENDING);

            Application app2 = new Application();
            app2.setMoId("mo-1");
            app2.setMoName("Dr. A");
            app2.setStatus(Application.Status.ACCEPTED);

            Application app3 = new Application();
            app3.setMoId("mo-2");
            app3.setMoName("Dr. B");
            app3.setStatus(Application.Status.REJECTED);

            java.util.List<WorkloadStatsService.MoWorkloadStats> stats = service.calculateMoWorkloadStats(
                    Arrays.asList(app1, app2, app3)
            );

            assert stats.size() == 2 : "should aggregate into 2 MOs";
            WorkloadStatsService.MoWorkloadStats mo1 = stats.get(0);
            WorkloadStatsService.MoWorkloadStats mo2 = stats.get(1);

            assert mo1.getMoId().equals("mo-1") : "first MO id should be mo-1";
            assert mo1.getTotalApplications() == 2 : "mo-1 total should be 2";
            assert mo1.getPending() == 1 : "mo-1 pending should be 1";
            assert mo1.getProcessed() == 1 : "mo-1 processed should be 1";

            assert mo2.getMoId().equals("mo-2") : "second MO id should be mo-2";
            assert mo2.getRejected() == 1 : "mo-2 rejected should be 1";
            assert mo2.getProcessed() == 1 : "mo-2 processed should be 1";
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

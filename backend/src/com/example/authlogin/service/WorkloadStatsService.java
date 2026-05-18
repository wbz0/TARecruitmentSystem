package com.example.authlogin.service;

import com.example.authlogin.model.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WorkloadStatsService - 管理员工作量统计服务（阶段1基础类）
 */
public class WorkloadStatsService {

    public static class ApplicationCounts {
        private final int total;
        private final int pending;
        private final int accepted;
        private final int rejected;
        private final int withdrawn;

        public ApplicationCounts(int total, int pending, int accepted, int rejected, int withdrawn) {
            this.total = total;
            this.pending = pending;
            this.accepted = accepted;
            this.rejected = rejected;
            this.withdrawn = withdrawn;
        }

        public int getTotal() {
            return total;
        }

        public int getPending() {
            return pending;
        }

        public int getAccepted() {
            return accepted;
        }

        public int getRejected() {
            return rejected;
        }

        public int getWithdrawn() {
            return withdrawn;
        }
    }

    public static class MoWorkloadStats {
        private final String moId;
        private final String moName;
        private final int totalApplications;
        private final int pending;
        private final int processed;
        private final int accepted;
        private final int rejected;
        private final int withdrawn;

        public MoWorkloadStats(String moId,
                               String moName,
                               int totalApplications,
                               int pending,
                               int processed,
                               int accepted,
                               int rejected,
                               int withdrawn) {
            this.moId = moId;
            this.moName = moName;
            this.totalApplications = totalApplications;
            this.pending = pending;
            this.processed = processed;
            this.accepted = accepted;
            this.rejected = rejected;
            this.withdrawn = withdrawn;
        }

        public String getMoId() {
            return moId;
        }

        public String getMoName() {
            return moName;
        }

        public int getTotalApplications() {
            return totalApplications;
        }

        public int getPending() {
            return pending;
        }

        public int getProcessed() {
            return processed;
        }

        public int getAccepted() {
            return accepted;
        }

        public int getRejected() {
            return rejected;
        }

        public int getWithdrawn() {
            return withdrawn;
        }
    }

    /**
     * 阶段2：实现申请数量统计逻辑。
     */
    public ApplicationCounts calculateApplicationCounts(List<Application> applications) {
        List<Application> safeApplications = applications != null ? applications : Collections.emptyList();
        int total = 0;
        int pending = 0;
        int accepted = 0;
        int rejected = 0;
        int withdrawn = 0;

        for (Application application : safeApplications) {
            if (application == null) {
                continue;
            }
            total++;
            Application.Status status = application.getStatus();
            if (status == null) {
                pending++;
                continue;
            }
            switch (status) {
                case PENDING:
                    pending++;
                    break;
                case ACCEPTED:
                    accepted++;
                    break;
                case REJECTED:
                    rejected++;
                    break;
                case WITHDRAWN:
                    withdrawn++;
                    break;
                default:
                    break;
            }
        }

        return new ApplicationCounts(total, pending, accepted, rejected, withdrawn);
    }

    /**
     * 阶段3：按MO统计处理工作量。
     */
    public List<MoWorkloadStats> calculateMoWorkloadStats(List<Application> applications) {
        List<Application> safeApplications = applications != null ? applications : Collections.emptyList();
        Map<String, MutableMoStats> grouped = new LinkedHashMap<>();

        for (Application application : safeApplications) {
            if (application == null) {
                continue;
            }
            String moId = safeText(application.getMoId(), "UNKNOWN_MO");
            String moName = safeText(application.getMoName(), "Unknown MO");
            String key = moId + "|" + moName;

            MutableMoStats stats = grouped.computeIfAbsent(key, ignored -> new MutableMoStats(moId, moName));
            stats.totalApplications++;

            Application.Status status = application.getStatus();
            if (status == null || status == Application.Status.PENDING) {
                stats.pending++;
                continue;
            }
            switch (status) {
                case ACCEPTED:
                    stats.accepted++;
                    stats.processed++;
                    break;
                case REJECTED:
                    stats.rejected++;
                    stats.processed++;
                    break;
                case WITHDRAWN:
                    stats.withdrawn++;
                    break;
                default:
                    break;
            }
        }

        List<MoWorkloadStats> results = new ArrayList<>();
        for (MutableMoStats value : grouped.values()) {
            results.add(new MoWorkloadStats(
                    value.moId,
                    value.moName,
                    value.totalApplications,
                    value.pending,
                    value.processed,
                    value.accepted,
                    value.rejected,
                    value.withdrawn
            ));
        }
        return results;
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static class MutableMoStats {
        private final String moId;
        private final String moName;
        private int totalApplications;
        private int pending;
        private int processed;
        private int accepted;
        private int rejected;
        private int withdrawn;

        private MutableMoStats(String moId, String moName) {
            this.moId = moId;
            this.moName = moName;
        }
    }
}

package com.example.authlogin.service;

import com.example.authlogin.model.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * WorkloadStatsService - 管理员工作量统计服务（阶段1基础类）
 */
public class WorkloadStatsService {

    private static final long CACHE_TTL_MS = 30_000L;
    private static final int CACHE_MAX_ENTRIES = 256;
    private final ConcurrentMap<String, SnapshotCacheEntry> snapshotCache = new ConcurrentHashMap<>();

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
        return calculateApplicationCounts(applications, null, null);
    }

    /**
     * 阶段4：支持时间段筛选。
     */
    public ApplicationCounts calculateApplicationCounts(List<Application> applications,
                                                        LocalDateTime start,
                                                        LocalDateTime end) {
        return getOrBuildSnapshot(applications, start, end).counts;
    }

    /**
     * 阶段3：按MO统计处理工作量。
     */
    public List<MoWorkloadStats> calculateMoWorkloadStats(List<Application> applications) {
        return calculateMoWorkloadStats(applications, null, null);
    }

    public List<MoWorkloadStats> calculateMoWorkloadStats(List<Application> applications,
                                                          LocalDateTime start,
                                                          LocalDateTime end) {
        return getOrBuildSnapshot(applications, start, end).moWorkloads;
    }

    public String exportMoWorkloadCsv(List<Application> applications, LocalDateTime start, LocalDateTime end) {
        List<MoWorkloadStats> stats = getOrBuildSnapshot(applications, start, end).moWorkloads;
        StringBuilder csv = new StringBuilder();
        csv.append("moId,moName,totalApplications,pending,processed,accepted,rejected,withdrawn\n");
        for (MoWorkloadStats stat : stats) {
            csv.append(escapeCsv(stat.getMoId())).append(",")
                    .append(escapeCsv(stat.getMoName())).append(",")
                    .append(stat.getTotalApplications()).append(",")
                    .append(stat.getPending()).append(",")
                    .append(stat.getProcessed()).append(",")
                    .append(stat.getAccepted()).append(",")
                    .append(stat.getRejected()).append(",")
                    .append(stat.getWithdrawn())
                    .append("\n");
        }
        return csv.toString();
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

    private static class StatsSnapshot {
        private final ApplicationCounts counts;
        private final List<MoWorkloadStats> moWorkloads;

        private StatsSnapshot(ApplicationCounts counts, List<MoWorkloadStats> moWorkloads) {
            this.counts = counts;
            this.moWorkloads = moWorkloads;
        }
    }

    private static class SnapshotCacheEntry {
        private final StatsSnapshot snapshot;
        private final long expiresAtMillis;

        private SnapshotCacheEntry(StatsSnapshot snapshot, long expiresAtMillis) {
            this.snapshot = snapshot;
            this.expiresAtMillis = expiresAtMillis;
        }
    }

    private StatsSnapshot getOrBuildSnapshot(List<Application> applications, LocalDateTime start, LocalDateTime end) {
        List<Application> safeApplications = applications != null ? applications : Collections.emptyList();
        String key = buildSnapshotKey(safeApplications, start, end);

        SnapshotCacheEntry cached = snapshotCache.get(key);
        long now = System.currentTimeMillis();
        if (cached != null && cached.expiresAtMillis > now) {
            return cached.snapshot;
        }

        StatsSnapshot snapshot = buildSnapshot(safeApplications, start, end);
        if (snapshotCache.size() >= CACHE_MAX_ENTRIES) {
            evictExpiredCacheEntries(now);
            if (snapshotCache.size() >= CACHE_MAX_ENTRIES) {
                snapshotCache.clear();
            }
        }
        snapshotCache.put(key, new SnapshotCacheEntry(snapshot, now + CACHE_TTL_MS));
        return snapshot;
    }

    private StatsSnapshot buildSnapshot(List<Application> applications, LocalDateTime start, LocalDateTime end) {
        int total = 0;
        int pending = 0;
        int accepted = 0;
        int rejected = 0;
        int withdrawn = 0;

        Map<String, MutableMoStats> grouped = new LinkedHashMap<>();

        for (Application application : applications) {
            if (application == null) {
                continue;
            }
            if (!isInTimeRange(application, start, end)) {
                continue;
            }

            total++;
            Application.Status status = application.getStatus();
            if (status == null || status == Application.Status.PENDING) {
                pending++;
            } else if (status == Application.Status.ACCEPTED) {
                accepted++;
            } else if (status == Application.Status.REJECTED) {
                rejected++;
            } else if (status == Application.Status.WITHDRAWN) {
                withdrawn++;
            }

            String moId = safeText(application.getMoId(), "UNKNOWN_MO");
            String moName = safeText(application.getMoName(), "Unknown MO");
            String moKey = moId + "|" + moName;
            MutableMoStats moStats = grouped.computeIfAbsent(moKey, ignored -> new MutableMoStats(moId, moName));
            moStats.totalApplications++;

            if (status == null || status == Application.Status.PENDING) {
                moStats.pending++;
            } else if (status == Application.Status.ACCEPTED) {
                moStats.accepted++;
                moStats.processed++;
            } else if (status == Application.Status.REJECTED) {
                moStats.rejected++;
                moStats.processed++;
            } else if (status == Application.Status.WITHDRAWN) {
                moStats.withdrawn++;
            }
        }

        List<MoWorkloadStats> workloadList = new ArrayList<>();
        for (MutableMoStats mo : grouped.values()) {
            workloadList.add(new MoWorkloadStats(
                    mo.moId,
                    mo.moName,
                    mo.totalApplications,
                    mo.pending,
                    mo.processed,
                    mo.accepted,
                    mo.rejected,
                    mo.withdrawn
            ));
        }

        ApplicationCounts counts = new ApplicationCounts(total, pending, accepted, rejected, withdrawn);
        return new StatsSnapshot(counts, Collections.unmodifiableList(workloadList));
    }

    private String buildSnapshotKey(List<Application> applications, LocalDateTime start, LocalDateTime end) {
        int size = applications.size();
        String firstId = size > 0 && applications.get(0) != null ? safeText(applications.get(0).getApplicationId(), "null") : "null";
        String lastId = size > 0 && applications.get(size - 1) != null ? safeText(applications.get(size - 1).getApplicationId(), "null") : "null";
        return size
                + "|" + firstId
                + "|" + lastId
                + "|" + (start != null ? start.toString() : "")
                + "|" + (end != null ? end.toString() : "");
    }

    private void evictExpiredCacheEntries(long now) {
        for (Map.Entry<String, SnapshotCacheEntry> entry : snapshotCache.entrySet()) {
            SnapshotCacheEntry value = entry.getValue();
            if (value != null && value.expiresAtMillis <= now) {
                snapshotCache.remove(entry.getKey(), value);
            }
        }
    }

    private boolean isInTimeRange(Application application, LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return true;
        }
        LocalDateTime ts = application.getAppliedAt();
        if (ts == null) {
            ts = application.getUpdatedAt();
        }
        if (ts == null) {
            return false;
        }
        if (start != null && ts.isBefore(start)) {
            return false;
        }
        if (end != null && ts.isAfter(end)) {
            return false;
        }
        return true;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

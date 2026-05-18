package com.example.authlogin.service;

import com.example.authlogin.model.Application;

import java.util.Collections;
import java.util.List;

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

    /**
     * 阶段1：先提供基础方法签名，后续阶段补充完整统计逻辑。
     */
    public ApplicationCounts calculateApplicationCounts(List<Application> applications) {
        List<Application> safeApplications = applications != null ? applications : Collections.emptyList();
        return new ApplicationCounts(safeApplications.size(), 0, 0, 0, 0);
    }
}

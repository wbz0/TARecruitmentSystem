# 管理员 TA 工作量统计模块技术文档

## 1. 模块概述

管理员工作量统计模块用于查看已录用 TA 的工作量。当前前端只展示 TA workload，不再提供 MO workload 模式。

**核心组件**：

- `WorkloadStatsServlet` - `/api/admin/workload-statistics` HTTP 入口
- `WorkloadStatsService` - TA 工作量统计业务逻辑
- `frontend/webapp/jsp/admin/dashboard.jsp` - Admin 工作量页面
- `frontend/webapp/js/admin/admin-dashboard.js` - 页面请求、筛选、分页和 CSV 导出

---

## 2. 统计口径

当前统计只计入满足以下条件的记录：

- 申请状态为 `ACCEPTED`；
- 申请人是 TA 用户；
- 对应职位存在；
- 职位具备 `weeklyHours`、`workStartDate`、`workEndDate`；
- 如果传入 `start` / `end`，按职位工作期和筛选区间的交集计算周数。

响应包含：

| 字段 | 说明 |
|------|------|
| `taWorkloads` | 每位 TA 的工作量列表 |
| `invalidJobs` | 无法统计的录用记录及原因 |
| `totalTaCount` | 有统计记录的 TA 数 |
| `totalAcceptedJobs` | 被计入的录用岗位数 |
| `totalWorkWeeks` | 合计周数 |
| `totalWorkHours` | 合计工作小时 |

---

## 3. Servlet API

### GET /api/admin/workload-statistics

**权限**: Admin

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `start` | String | 开始日期或日期时间，可选 |
| `end` | String | 结束日期或日期时间，可选 |
| `mode` | String | 可选；当前仅接受 `ta` |
| `export` | String | 传 `csv` 时导出 TA 工作量 CSV |

示例响应：

```json
{
  "success": true,
  "message": "TA workload stats generated",
  "data": {
    "taWorkloads": [
      {
        "taId": "ta-001",
        "taName": "Alice",
        "acceptedJobCount": 2,
        "totalWorkWeeks": 18,
        "totalWorkHours": 144,
        "jobs": []
      }
    ],
    "invalidJobs": [],
    "totalTaCount": 1,
    "totalAcceptedJobs": 2,
    "totalWorkWeeks": 18,
    "totalWorkHours": 144
  }
}
```

### CSV 导出

`GET /api/admin/workload-statistics?export=csv` 返回 `text/csv`，文件名为 `ta-workload-stats.csv`。

---

## 4. 前端页面

Admin dashboard 调用：

```javascript
TARecruitment.api.request(TARecruitment.routes.admin.workloadStatistics(query), {
  headers: { "X-Requested-With": "XMLHttpRequest" }
});
```

页面功能：

- TA 工作量汇总卡片；
- 搜索、排序和分页；
- 日期范围筛选；
- 无效录用记录提示；
- CSV 导出。

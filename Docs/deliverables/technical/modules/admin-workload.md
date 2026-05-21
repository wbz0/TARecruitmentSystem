# Admin TA Workload Statistics Module Technical Documentation

## 1. Module Overview

Admin workload statistics module is used to view workload of hired TAs. Currently, the frontend only displays TA workload; MO workload mode is no longer provided.

**Core Components**:

- `WorkloadStatsServlet` - `/api/admin/workload-statistics` HTTP entry
- `WorkloadStatsService` - TA workload statistics business logic
- `frontend/webapp/jsp/admin/dashboard.jsp` - Admin workload page
- `frontend/webapp/js/admin/admin-dashboard.js` - Page request, filtering, pagination, and CSV export

---

## 2. Statistics Scope

Current statistics only include records meeting the following conditions:

- Application status is `ACCEPTED`;
- Applicant is a TA user;
- Corresponding job exists;
- Job has `weeklyHours`, `workStartDate`, `workEndDate`;
- If `start` / `end` is passed, calculate weeks based on intersection of job work period and filter range.

Response contains:

| Field | Description |
|------|-------------|
| `taWorkloads` | Workload list for each TA |
| `invalidJobs` | Hired records that cannot be counted and reasons |
| `totalTaCount` | Number of TAs with statistics records |
| `totalAcceptedJobs` | Number of hired jobs counted |
| `totalWorkWeeks` | Total weeks |
| `totalWorkHours` | Total work hours |

---

## 3. Servlet API

### GET /api/admin/workload-statistics

**Permission**: Admin

**Query Parameters**:

| Parameter | Type | Description |
|------|------|------|
| `start` | String | Start date or datetime, optional |
| `end` | String | End date or datetime, optional |
| `mode` | String | Optional; currently only accepts `ta` |
| `export` | String | When set to `csv`, exports TA workload CSV |

Example response:

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

### CSV Export

`GET /api/admin/workload-statistics?export=csv` returns `text/csv`, filename is `ta-workload-stats.csv`.

---

## 4. Frontend Page

Admin dashboard calls:

```javascript
TARecruitment.api.request(TARecruitment.routes.admin.workloadStatistics(query), {
  headers: { "X-Requested-With": "XMLHttpRequest" }
});
```

Page features:

- TA workload summary cards;
- Search, sort, and pagination;
- Date range filtering;
- Invalid hired records notification;
- CSV export.
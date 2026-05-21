# Job Management Module Technical Documentation

## 1. Module Overview

Job management module supports MO in publishing, editing, deleting jobs; TA and MO in browsing job list/details; and provides job data for application process and AI recommendation.

**Core Components**:
- `Job` - Job model, status enum, and CSV serialization
- `JobDao` - Job CSV read/write, query, search, and statistics
- `JobRequestMapper` - pathInfo, form fields, skills list, deadline and other request conversion
- `JobResponseMapper` - Job list/detail payload assembly
- `JobValidator` - Field validation for creating and updating jobs
- `JobService` - Job list filtering, detail, create, update, delete, and permission-related business validation
- `JobServlet` - `/api/jobs` HTTP thin entry

---

## 2. Layer Responsibilities

```text
JobServlet
    -> JobRequestMapper
    -> JobService
        -> JobValidator
        -> JobDao / ApplicationDao / UserDao
        -> JobResponseMapper
```

| Layer | Responsibility |
|-------|----------------|
| `web` | Parse HTTP method, pathInfo, current user, and request parameters; call `JobService` |
| `service` | Handle filtering, permission validation, default values, status rules, and cross-DAO collaboration |
| `dao` | Only responsible for CSV read/write and query |
| `mapper` | Convert HTTP parameters and response payload |
| `validator` | Validate required fields, length, date, dangerous input, and numeric ranges |
| `model` | Express job fields, status enum, and CSV format |

---

## 3. Data Model

### 3.1 Job

**Path**: `backend/src/com/example/tarecruitment/job/model/Job.java`

Main fields include:

| Field | Description |
|------|-------------|
| `jobId` | Job ID |
| `moId` / `moName` | MO who published this job |
| `title` | Job title |
| `courseCode` / `courseName` | Course code and course name |
| `description` | Job description |
| `requiredSkills` | Skills list |
| `positions` | Number of positions to hire |
| `weeklyHours` | Weekly work hours |
| `workStartDate` / `workEndDate` | Work period |
| `salary` | Salary description |
| `deadline` | Application deadline |
| `status` | `OPEN` / `CLOSED` / `FILLED` |

`getEffectiveStatus(...)` calculates display status based on explicit status and deadline: filled takes priority, then manually closed, then deadline auto-close.

### 3.2 CSV

Job data is written by `JobDao` to jobs CSV file under `TA_HIRING_DATA_DIR`. Model is responsible for CSV field serialization/deserialization; DAO is responsible for file read/write.

---

## 4. API

**Servlet**: `backend/src/com/example/tarecruitment/job/web/JobServlet.java`

| Function | Method | Path | Permission |
|------|--------|------|------|
| Job list | GET | `/api/jobs` | Public |
| Job detail | GET | `/api/jobs/{jobId}` | Public |
| Create job | POST | `/api/jobs` | MO |
| Update job | PUT | `/api/jobs/{jobId}` | Job's MO |
| Delete job | DELETE | `/api/jobs/{jobId}` | Job's MO |

List query parameters:

| Parameter | Description |
|------|-------------|
| `keyword` | Keyword fuzzy search |
| `courseCode` | Course code filter |
| `status` | Valid status filter |
| `moId` | MO's own jobs filter |

Create/update fields are read by `JobRequestMapper` whitelist to avoid unknown request parameters directly entering business objects.

---

## 5. Business Flow

### 5.1 MO Creates Job

```text
MO submits job form
  -> JobServlet reads whitelist parameters
  -> JobService validates current user is MO
  -> JobValidator validates fields
  -> JobService assembles Job model and default values
  -> JobDao writes to CSV
  -> JobResponseMapper returns jobId
```

### 5.2 MO Updates Job

Update interface uses HTTP `PUT`, but business adopts partial update style of "only overwrite fields carried in request". PUT form body is manually parsed by `JobRequestMapper.formParameters(...)` to be compatible with some Servlet containers not automatically parsing PUT form parameters.

### 5.3 Delete Job

Before deletion, first read job and confirm `job.moId` equals current MO's user ID. Admin does not delete jobs on behalf of others through this business interface.

---

## 6. Frontend Call Method

Frontend generates URLs via `frontend/webapp/js/common/ta-recruitment.js`; do not hand-write API strings.

```javascript
const url = TARecruitment.routes.jobs.list({
    keyword: keyword,
    status: status,
    courseCode: courseCode
});

TARecruitment.api.request(url, {
    headers: { "X-Requested-With": "XMLHttpRequest" }
}).then(function (result) {
    renderJobs(result.payload.data);
});
```

Job detail uses:

```javascript
const detailUrl = TARecruitment.routes.jobs.detail(jobId);
```

---

## 7. Error Handling

| Scenario | Status Code | Example Message |
|------|--------|----------|
| Not logged in when creating job | 401 | `Please login first` |
| Non-MO creates job | 403 | `Only MO can post jobs` |
| Operate on others' jobs | 403 | `You can only update your own jobs` |
| Missing job ID | 400 | `Job ID is required` |
| Job does not exist | 404 | `Job not found` |
| Field validation failure | 400 | Specific field error |

All JSON responses are output by `ApiResponses` with `{ success, message, data }` structure.

---

## 8. Testing

Recommendation check:

```bash
bash -n scripts/dev.sh scripts/javadocs.sh
find frontend/webapp/js -name "*.js" -print0 | xargs -0 -n1 node --check
./scripts/javadocs.sh
```
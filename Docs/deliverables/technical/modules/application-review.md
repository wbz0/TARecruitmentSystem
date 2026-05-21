# Application Review Module Technical Documentation

## 1. Module Overview

Application review module handles TA's application process for jobs, including application submission, progress tracking, and MO review.

**Core Components**:
- `Application` - Application entity
- `ApplicationDao` - Data access layer
- `ApplicationServlet` - Application operation handling
- Frontend pages: `jsp/ta/application-status.jsp`, `jsp/ta/application-detail.jsp`, `jsp/mo/dashboard.jsp` applicant subview

---

## 2. Entity Design

### 2.1 Application

**Path**: `backend/src/com/example/tarecruitment/application/model/Application.java`

```java
public class Application {
    private String applicationId;        // UUID
    private String jobId;                 // Applied job ID
    private String applicantId;           // Applicant ID
    private String applicantName;        // Applicant name
    private String applicantEmail;       // Applicant email
    private String jobTitle;             // Job title (redundant)
    private String courseCode;            // Course code (redundant)
    private String moId;                 // MO ID who posted the job
    private String moName;               // MO name (redundant)
    private Status status;               // PENDING / ACCEPTED / REJECTED / WITHDRAWN
    private String coverLetter;          // Cover letter
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private ProgressStage progressStage; // UNDER_REVIEW / INTERVIEW_SCHEDULED / COMPLETED
    private LocalDateTime reviewStartedAt;
    private LocalDateTime interviewScheduledAt;
    private LocalDateTime finalDecisionAt;

    public enum Status { PENDING, ACCEPTED, REJECTED, WITHDRAWN }
    public enum ProgressStage { UNDER_REVIEW, INTERVIEW_SCHEDULED, COMPLETED }
}
```

### 2.2 Progress Stage

```
UNDER_REVIEW ──▶ INTERVIEW_SCHEDULED ──▶ COMPLETED
   (Under Review)     (Interview Scheduled)     (Completed)
      │
      └──▶ WITHDRAWN
```

### 2.3 CSV Format

**File**: `data/applications/applications.csv`

**Header**:
```csv
applicationId,jobId,applicantId,applicantName,applicantEmail,jobTitle,courseCode,moId,moName,status,coverLetter,appliedAt,updatedAt,reviewedAt,progressStage,reviewStartedAt,interviewScheduledAt,finalDecisionAt
```

---

## 3. Data Access Layer

### 3.1 ApplicationDao

**Path**: `backend/src/com/example/tarecruitment/application/dao/ApplicationDao.java`

**Singleton**: Yes

**Core Methods**:

| Method | Description |
|------|------|
| `findById(String applicationId)` | Find by ID |
| `findByJobId(String jobId)` | Find all applications for a job |
| `findByApplicantId(String applicantId)` | Find all applications for an applicant |
| `findByMoId(String moId)` | Find all applications received by MO |
| `findByStatus(Status status)` | Filter by status |
| `findByProgressStage(ProgressStage stage)` | Filter by stage |
| `existsByJobIdAndApplicantId(String jobId, String applicantId)` | Check if already applied (prevent duplicates) |
| `save(Application application)` | Save application |
| `updateStatus(String applicationId, Status status)` | Update status |
| `updateProgressStage(String applicationId, ProgressStage stage)` | Update stage |

**Prevent duplicate applications**:
```java
public boolean existsByJobIdAndApplicantId(String jobId, String applicantId) {
    return readAllApplications().stream()
        .anyMatch(app ->
            app.getJobId().equals(jobId) &&
            app.getApplicantId().equals(applicantId) &&
            app.getStatus() != Status.WITHDRAWN
        );
}
```

---

## 4. Servlet Implementation

### 4.1 ApplicationServlet

**Path**: `backend/src/com/example/tarecruitment/application/web/ApplicationServlet.java`

**Endpoint**: `/api/applications`

**Supported Operations**:

| Operation | Method | Description |
|------|------|------|
| Submit application | POST `/api/applications` | TA submits new application |
| Cancel application | POST `/api/applications/{applicationId}/transition` | TA withdraws application |
| Accept application | POST `/api/applications/{applicationId}/transition` | MO accepts application |
| Reject application | POST `/api/applications/{applicationId}/transition` | MO rejects application |

#### POST /api/applications (Submit Application)

**Request Parameters**:
| Parameter | Type | Required | Description |
|------|------|------|------|
| jobId | String | Yes | Job ID |
| coverLetter | String | No | Cover letter |

**Permission**: TA only

**Processing Flow**:
```
POST /api/applications
    │
    ▼
Validate TA login status
    │
    ▼
Check if TA has applicant profile
    │
    ▼
Check if job exists
    │
    ▼
Check if job is open (effectiveStatus == OPEN)
    │
    ▼
Check for duplicate application
    │
    ▼
Create Application object
    │
    ▼
Save to CSV
    │
    ▼
Return success
```

#### POST /api/applications/{applicationId}/transition (Status Transition)

**Request Parameters**:
| Parameter | Type | Required | Description |
|------|------|------|------|
| action | String | Yes | `accept`, `reject`, `withdraw` |

**Rules**:
- TA can withdraw their own PENDING applications.
- MO can accept or reject PENDING applications for their jobs.
- Admin maintains current system view capability; no new review business capability added.

---

## 5. Frontend Pages

### 5.1 TA My Applications Page

**Path**: `frontend/webapp/jsp/ta/application-status.jsp`

**Features**:
- Application list display (status, job, time)
- Application detail view
- Cancel application button (PENDING status only)

```javascript
// Load my applications
async function loadMyApplications() {
    return TARecruitment.api.request(TARecruitment.routes.applications.list(), {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    });
}

// Cancel application
async function cancelApplication(applicationId) {
    return TARecruitment.api.request(TARecruitment.routes.applications.transition(applicationId), {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'action=withdraw'
    });
}
```

### 5.2 TA Application Detail Page

**Path**: `frontend/webapp/jsp/ta/application-detail.jsp`

**Features**:
- Application detail display
- Progress timeline
- Review result display

### 5.3 MO Dashboard Applicant Subview

**Path**: `frontend/webapp/jsp/mo/dashboard.jsp`

**Features**:
- Received application list
- View applicant profile
- View resume
- Search and AI recommend candidates
- Review operations (accept/reject)

---

## 6. Permission Control

| Operation | TA | MO | ADMIN |
|------|----|----|-------|
| Submit application | ✓ (own) | ✗ | ✗ |
| View my applications | ✓ (own) | ✗ | ✗ |
| Cancel application | ✓ (own, PENDING) | ✗ | ✗ |
| View received applications | ✗ | ✓ (own jobs) | ✗ |
| Review application | ✗ | ✓ (own jobs) | ✗ |
| Advance progress | ✗ | ✓ (own jobs) | ✗ |

---

## 7. Business Flow

### 7.1 TA Application Flow

```
TA browses job list
    │
    ▼
Click job detail
    │
    ▼
Click "Apply for this job"
    │
    ▼
Fill in cover letter (optional)
    │
    ▼
Submit → POST /api/applications
    │
    ▼
View my application status
```

### 7.2 MO Review Flow

```
MO views received applications
    │
    ▼
Click application to view details
    │
    ▼
Select candidates
    │
    ▼
Advance progress stage
    │
    ▼
Make final decision (accept/reject)
    │
    ▼
TA receives notification (optional)
```

---

## 8. Status and Stage Linkage

### 8.1 Stage to Status Mapping

| Stage | Possible Status |
|------|----------|
| UNDER_REVIEW | PENDING |
| INTERVIEW_SCHEDULED | PENDING |
| COMPLETED | ACCEPTED / REJECTED / WITHDRAWN |

### 8.2 Status Change Rules

```java
// On review decision
if ("ACCEPTED".equals(status)) {
    application.setStatus(Status.ACCEPTED);
    application.setProgressStage(ProgressStage.COMPLETED);
    application.setFinalDecisionAt(LocalDateTime.now());
} else if ("REJECTED".equals(status)) {
    application.setStatus(Status.REJECTED);
    application.setProgressStage(ProgressStage.COMPLETED);
    application.setFinalDecisionAt(LocalDateTime.now());
}
```

---

## 9. Error Handling

| Error Scenario | Response Code | Message |
|----------|--------|------|
| Job does not exist | 400 | "Job not found" |
| Job is closed | 400 | "Job is no longer accepting applications" |
| Duplicate application | 400 | "You have already applied for this job" |
| No applicant profile | 400 | "Please complete your profile first" |
| Insufficient permission | 403 | "Access denied" |
| Application does not exist | 404 | "Application not found" |
| Status cannot be changed | 400 | "Cannot change status at this stage" |

---

## 10. Test Cases

**Validation method**: After starting local environment with `scripts/dev.sh`, manually verify according to the following scenarios; before committing, you can run `./scripts/javadocs.sh` and frontend `node --check`.

**Test scenarios**:
1. TA submits application → Application saved correctly
2. TA duplicates application → Returns error
3. TA cancels PENDING application → Success
4. TA cancels non-PENDING application → Returns error
5. MO views received applications → Correct list
6. MO accepts application → Status updated
7. MO rejects application → Status updated
8. Non-MO reviews application → Returns 403
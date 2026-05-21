# Servlet API Documentation

## 1. Overview

TA Hiring System's JSON and file resource interfaces are uniformly placed under `/api/...`. JSP page paths are retained, e.g., `/login.jsp`, `/jsp/ta/job-list.jsp`.

Common response structure:

```json
{ "success": true, "message": "OK", "data": {} }
```

Failure responses maintain the same structure:

```json
{ "success": false, "message": "Error message" }
```

## 2. Auth

| Function | Method | Path | Permission |
|---|---|---|---|
| Login | POST | `/api/auth/login` | Public |
| Register | POST | `/api/auth/register` | Public |
| Logout | POST/GET | `/api/auth/logout` | Logged in users |
| Username/Email availability | GET | `/api/auth/availability?type=&value=` | Public |

## 3. Jobs

| Function | Method | Path | Permission |
|---|---|---|---|
| Job list | GET | `/api/jobs` | Public |
| Job detail | GET | `/api/jobs/{jobId}` | Public |
| Create job | POST | `/api/jobs` | MO |
| Update job | PUT | `/api/jobs/{jobId}` | Job's MO |
| Delete job | DELETE | `/api/jobs/{jobId}` | Job's MO |

Common query parameters: `keyword`, `courseCode`, `status`, `moId`.

## 4. Applications

| Function | Method | Path | Permission |
|---|---|---|---|
| Application list | GET | `/api/applications` | TA/MO/Admin filtered by role |
| Application detail | GET | `/api/applications/{applicationId}` | Applicant, job's MO, Admin |
| Create application | POST | `/api/applications` | TA |
| Status transition | POST | `/api/applications/{applicationId}/transition` | TA/MO/Admin determined by action |

Create application parameters: `jobId`, `coverLetter`.

Status transition parameters: `action=accept|reject|withdraw`.

## 5. Current User Profile

| Function | Method | Path | Permission |
|---|---|---|---|
| Current account info | GET/POST | `/api/me/account` | TA/MO |
| Current account avatar | GET | `/api/me/avatar` | TA/MO |
| Current TA profile | GET/POST/PUT | `/api/me/applicant-profile` | TA |
| Current TA profile avatar | GET | `/api/me/applicant-profile/photo` | TA |
| Current TA profile resume | GET | `/api/me/applicant-profile/resume` | TA |
| Draft resume | POST/DELETE | `/api/me/applicant-profile/resume-draft` | TA |

## 6. Application Applicant Materials

| Function | Method | Path | Permission |
|---|---|---|---|
| Application's applicant materials | GET | `/api/applications/{applicationId}/applicant` | Applicant, job's MO, Admin |
| Application's resume | GET | `/api/applications/{applicationId}/applicant/resume` | Applicant, job's MO, Admin |
| Application's avatar | GET | `/api/applications/{applicationId}/applicant/photo` | Applicant, job's MO, Admin |

## 7. Admin

| Function | Method | Path | Permission |
|---|---|---|---|
| TA workload statistics | GET | `/api/admin/workload-statistics` | Admin |
| Accept admin short invitation code registration | POST | `/api/admin/invitations/acceptance` | Public |
| Current short invitation code | GET/POST | `/api/admin/invitations/current-code` | Admin |

## 8. Notifications

| Function | Method | Path | Permission |
|---|---|---|---|
| Notification list | GET | `/api/notifications` | Logged in users |
| Publish notification | POST | `/api/notifications` | Admin |
| Delete notification | DELETE | `/api/notifications?notificationId=` | Admin |

## 9. AI

| Function | Method | Path | Permission |
|---|---|---|---|
| MO applicant recommendations | POST | `/api/mo/applicant-recommendations` | MO |
| TA job recommendations | POST | `/api/ta/job-recommendations` | TA |

AI recommendation configuration template is `frontend/webapp/WEB-INF/ai/deepseek.properties.template`; local key file is `deepseek.local.properties`, which should not be committed to git.
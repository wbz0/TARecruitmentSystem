# Sun Jialu Division and Current Code Files

[Back to Overview](Overview.md)

## Basic Information

| Item | Content |
| --- | --- |
| Contributor | Sun Jialu |
| Standard commit count | 23 |
| Division overview | Application flow, status transitions, TA withdrawal, MO selection, notifications and invite code service, integration testing/user manual early work |

## Division Overview

Sun Jialu primarily took on the application business flow: position application, application status query, MO selection/hiring, process stage convergence, TA withdrawal and review status sync. Later also contributed notifications and admin short invite code service, and continued to improve position application and account profile service boundaries. Early on took on integration testing, packaging, and user manual type work.

## Current Corresponding Code Files

Position application, application status, MO review, and TA withdrawal:

- `backend/src/com/example/tarecruitment/application/model/Application.java`
- `backend/src/com/example/tarecruitment/application/dao/ApplicationDao.java`
- `backend/src/com/example/tarecruitment/application/mapper/ApplicationRequestMapper.java`
- `backend/src/com/example/tarecruitment/application/mapper/ApplicationResponseMapper.java`
- `backend/src/com/example/tarecruitment/application/service/ApplicationApplicantService.java`
- `backend/src/com/example/tarecruitment/application/service/ApplicationService.java`
- `backend/src/com/example/tarecruitment/application/validator/ApplicationValidator.java`
- `backend/src/com/example/tarecruitment/application/web/ApplicationServlet.java`

Notification business:

- `backend/src/com/example/tarecruitment/notification/model/Notification.java`
- `backend/src/com/example/tarecruitment/notification/dao/NotificationDao.java`
- `backend/src/com/example/tarecruitment/notification/web/NotificationServlet.java`

Admin invite code business:

- `backend/src/com/example/tarecruitment/admin/service/InviteCodeService.java`
- `backend/src/com/example/tarecruitment/admin/web/AdminCurrentInviteCodeServlet.java`
- `backend/src/com/example/tarecruitment/admin/web/AdminInviteAcceptServlet.java`

Workload statistics rules collaboration files (**Overlap note: Liu Tengyi primary; Sun Jialu explains how application status affects statistics calculation**):

- `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java` (overlap file, Liu Tengyi defense primary)
- `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java` (overlap file, Liu Tengyi defense primary)

Related common capabilities:

- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java` (overlap file, Liu Tengyi defense primary)

## File Overlap and Defense Attribution

- `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java` and `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java` also appear in Liu Tengyi's documentation. During defense, the workload statistics interface is presented by Liu Tengyi. Sun Jialu only explains how application status, hiring, and withdrawal flows affect statistics data.
- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java` also appears in Liu Tengyi's documentation. During defense, the search utility is presented by Liu Tengyi. Sun Jialu only explains how filtering capability is used in application/stats scenarios.

## Test Presentation

Run command:

```bash
./scripts/test/test-sun-jialu.sh
```

Test code:

- `backend/test/SunJialuBackendTest.java`

Test coverage points:

- `ApplicationValidator` validates application ID, position ID, cover letter, and status transition actions.
- `Application` CSV serialization/deserialization preserves applicant, status, and progress stage.
- `ApplicationDao` can create applications and complete status transitions like accept and withdraw.
- `Notification` can save and read system announcement fields.
- `InviteCodeService` can generate and validate current short invite codes.

For defense, you can explain:

Sun Jialu's test focus is on application flow and status transitions. The script creates temporary application data to simulate key flows like TA has applied, MO accepted application, TA withdrew application. Passing the tests proves that application status, progress stages, notifications, and admin short invite codes can be stably saved and read.
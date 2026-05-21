# Sheng Yuhan Division and Current Code Files

[Back to Overview](Overview.md)

## Basic Information

| Item | Content |
| --- | --- |
| Contributor | Sheng Yuhan |
| Standard commit count | 34 |
| Division overview | Frontend pages and interactions, covering login/register, TA/MO/Admin pages, frontend API routing unification |

## Division Overview

Sheng Yuhan primarily took on frontend pages, interactions, and styles. Scope covers login/register, TA profile, TA position list/detail/application status, MO posting and applicant review within dashboard, Admin page request adaptation, and later unified how page JS calls `TARecruitment.routes` and common request utilities. The old MO skill matching page, old MO standalone applicant page, and old Admin registration instruction page have been taken offline, so the current file list only includes pages and scripts that still exist.

## Current Corresponding Code Files

Authentication, registration, and admin invite frontend:

- `frontend/webapp/login.jsp`
- `frontend/webapp/register.jsp`
- `frontend/webapp/admin-invite.jsp`
- `frontend/webapp/css/auth/login.css`
- `frontend/webapp/css/auth/register.css`
- `frontend/webapp/js/auth/login.js`
- `frontend/webapp/js/auth/register.js`
- `frontend/webapp/js/auth/admin-invite.js`

TA pages, scripts, and styles:

- `frontend/webapp/jsp/ta/dashboard.jsp`
- `frontend/webapp/jsp/ta/job-list.jsp`
- `frontend/webapp/jsp/ta/job-detail.jsp`
- `frontend/webapp/jsp/ta/application-status.jsp`
- `frontend/webapp/jsp/ta/application-detail.jsp`
- `frontend/webapp/jsp/ta/notifications.jsp`
- `frontend/webapp/css/ta/ta-dashboard.css`
- `frontend/webapp/css/ta/ta-job-list.css`
- `frontend/webapp/css/ta/ta-job-detail.css`
- `frontend/webapp/css/ta/ta-application-status.css`
- `frontend/webapp/css/ta/ta-application-detail.css`
- `frontend/webapp/js/ta/ta-dashboard.js`
- `frontend/webapp/js/ta/ta-job-list.js`
- `frontend/webapp/js/ta/ta-job-detail.js`
- `frontend/webapp/js/ta/ta-application-status.js`
- `frontend/webapp/js/ta/ta-application-detail.js`
- `frontend/webapp/js/ta/ta-notifications.js`

MO pages, scripts, and styles:

- `frontend/webapp/jsp/mo/dashboard.jsp`
- `frontend/webapp/jsp/mo/notifications.jsp`
- `frontend/webapp/css/mo/mo-dashboard.css`
- `frontend/webapp/js/mo/mo-dashboard.js`
- `frontend/webapp/js/mo/mo-notifications.js`

Admin frontend page request adaptation:

- `frontend/webapp/jsp/admin/dashboard.jsp`
- `frontend/webapp/jsp/admin/invite.jsp`
- `frontend/webapp/jsp/admin/notifications.jsp`
- `frontend/webapp/css/admin/admin-dashboard.css`
- `frontend/webapp/css/admin/admin-invite-management.css`
- `frontend/webapp/js/admin/admin-dashboard.js`
- `frontend/webapp/js/admin/admin-invite-management.js`
- `frontend/webapp/js/admin/admin-notifications.js`

Frontend common API route calls:

- `frontend/webapp/js/common/ta-recruitment.js`
- `frontend/webapp/WEB-INF/jsp/fragments/portal-sidebar.jspf`

## File Overlap and Defense Attribution

Sheng Yuhan's current code files have no direct overlap with Ouyang Xiaojun, Zhou Bohan, Liu Tengyi, and Sun Jialu. Sheng Yuhan's defense focus is on specific frontend pages, page interactions, styles, and page JS API call methods. If public portal shell and full-site architecture frontend files overlap with Wang Bangzhen's documentation, they should be handled according to Wang Bangzhen's leader/architecture explanation.

## Test Presentation

Run command:

```bash
./scripts/test/test-sheng-yuhan.sh
```

Test code:

- `frontend/test/sheng-yuhan-frontend-test.js`

Test coverage points:

- All `frontend/webapp/js/**/*.js` files pass `node --check` syntax validation.
- Page JS must not directly hardcode API addresses or old root paths, must use `TARecruitment.routes` to generate them.
- Login, register, TA, MO, Admin key pages and shared `TARecruitment.routes` file exist.
- The taken offline old MO skill matching page, old MO standalone applicant page, and old Admin registration instruction page no longer appear in the current frontend directory.

For defense, you can explain:

Sheng Yuhan's test focus is on whether frontend pages can be normally parsed by the browser and whether page requests uniformly go through the common routing utility. This proves that the frontend is not scattered with hand-written interface addresses, but uses `TARecruitment.routes` to adapt deployment paths like `/groupproject`.
# TA Hiring System User Manual

## 1. Document Overview

This manual is intended for three types of users: TA, MO, and Administrator. It explains how to access the system, register and log in to accounts, and how to complete the main operations for each role.

The system name is uniformly referred to as **TA Hiring System**. The default access address for the local demo environment is:

```text
http://localhost:8080/groupproject/
```

If the Tomcat port or application name on this machine differs, please refer to the actual configuration in the local `scripts/config.sh` or `scripts/config.bat`.

## 2. Common Entry Points and Accounts

### 2.1 Portal Home Page

The portal home page provides a system overview, role entry points, login entry, and registration entry. Users can switch languages from the upper right corner, or directly navigate to the login or registration page.

![Portal Home Page](user-manual-images/home.png)

### 2.2 Login Page

During login, you need to fill in the username or email, password, and select the corresponding role. The account role and the login role must match, otherwise the system will deny the login.

![Login Page](user-manual-images/login.png)

### 2.3 TA/MO Registration Page

The regular registration page is used to create TA or MO accounts. During registration, you need to fill in the username, email, password, and select the TA or MO role.

![TA/MO Registration Page](user-manual-images/register.png)

### 2.4 Administrator Invite Registration Page

Administrator accounts are not created through the regular registration entry. New administrators must first obtain the current 8-digit invitation code, then fill in the email, invitation code, username, and password on the administrator invite registration page. The first administrator account is automatically generated with a default invitation code when the system is deployed. Subsequent administrators obtain their invitation codes from the current administrator.

![Administrator Invite Registration Page](user-manual-images/admin-invite-register.png)

### 2.5 Demo Accounts

When the local demo environment starts, fixed demo accounts and sample data are automatically populated. Common accounts are as follows:

| Role | Username | Password |
| ---- | -------- | -------- |
| TA | `ta_demo` | `Pass1234` |
| TA | `ta_demo_mia` | `Pass1234` |
| TA | `ta_demo_noah` | `Pass1234` |
| TA | `ta_demo_olivia` | `Pass1234` |
| TA | `ta_demo_liam` | `Pass1234` |
| MO | `mo_demo` | `Pass1234` |
| MO | `mo_demo_alice` | `Pass1234` |
| MO | `mo_demo_brian` | `Pass1234` |
| Administrator | `admin_demo` | `Pass1234` |

If the demo data is not displayed on the page, please confirm that the application has been redeployed via the project scripts and that the current run used the correct `TA_HIRING_DATA_DIR`.

## 3. TA User Guide

TA users primarily manage personal profiles, browse positions, apply for positions, and track application status.

### 3.1 TA Personal Profile

After entering the TA section, the system displays the personal profile page. TAs can maintain their name, student ID, department, project type, GPA, contact information, skills, relevant experience, application motivation, avatar, and resume.

![TA Personal Profile Page](user-manual-images/ta-dashboard.png)

Key operation notes:

- Avatars support JPG, PNG, WEBP, with a maximum size of 5MB.
- Resumes support PDF, DOC, DOCX, with a maximum size of 10MB.
- Skills are recommended to be separated by English commas or Chinese commas.
- After saving the profile, MOs can view the relevant information when reviewing applications.

TAs can navigate to the job list or application status page via the left sidebar.

### 3.2 TA Job List

The job list displays the current TA positions in the system. TAs can use the search box to filter by job title, course code, or keywords, and can also navigate to the job detail page to view job requirements.

![TA Job List](user-manual-images/ta-job-list.png)

Key operation notes:

- Job cards display the course code, course name, MO, deadline, salary, workload, and required skills.
- After entering the job details, you can fill in a cover letter and submit an application.
- Positions that have been closed or filled may still appear in the list, but cannot continue to be treated as open applications.

### 3.3 TA Application Status

The application status page displays the TA's submitted application records and shows application progress via a timeline.

![TA Application Status Page](user-manual-images/ta-application-status.png)

Common statuses include:

| Status | Meaning |
| ------ | ------- |
| `PENDING` | Submitted, waiting for MO review |
| `ACCEPTED` | Application accepted by MO |
| `REJECTED` | Application rejected by MO |
| `WITHDRAWN` | Application withdrawn by TA |

## 4. MO User Guide

MO users primarily manage job postings, maintain positions, view applicants, use AI recommendations for applicants, and analyze application details with AI.

### 4.1 MO Job Management

After logging in, MOs enter the job management page by default, where they can view their published positions and switch to the job posting form.

![MO Job Management Page](user-manual-images/mo-dashboard.png)

When posting a job, the following fields are typically required:

- Job title, course code, course name;
- Job description and required skills;
- Number of positions to fill, application deadline, weekly working hours;
- Work start date, work end date, and salary description.

### 4.2 MO Applicant Review Sub-View

The applicant review is integrated into the MO job management page. After MOs enter a specific position from "My Postings," they can view applications received for that position and search by applicant name, email, or job title.

![MO Applicant Review Sub-View](user-manual-images/mo-dashboard-applicants.png)

Key operation notes:

- When there are application records, the page displays applicant cards.
- MOs can view applicant information, applied positions, and cover letters.
- For pending applications, MOs can accept or reject them.

### 4.3 MO AI Recommendations

MOs can use AI recommendations to search for candidates on the job management page.

The page displays:

- AI-recommended candidate list;
- Recommendation reasons and basic candidate information;
- You can further navigate to the applicant detail page to view materials and process applications.

If AI configuration is unavailable, the page displays an unavailability notice and does not generate local fake recommendations.

## 5. Administrator User Guide

Administrators are primarily responsible for viewing TA workloads, managing administrator invitation codes, and publishing notifications.

### 5.1 TA Workload Dashboard

The administrator dashboard displays TA workload statistics from accepted positions. The page supports searching by TA name or related keywords.

![Administrator TA Workload Dashboard](user-manual-images/admin-dashboard.png)

Key information on the page includes:

- TAs included in the statistics;
- Number of accepted positions per TA;
- Cumulative working hours per TA;
- Clicking a card allows you to expand and view specific position workloads.

### 5.2 Administrator Invitation Code Management

The invitation code management page displays the currently available 8-digit administrator invitation codes and a countdown. Administrators can reply the current invitation code to applicants who need to create an administrator account.

![Administrator Invitation Code Management Page](user-manual-images/admin-invite-management.png)

Operation process:

1. The applicant contacts the team using the email they plan to register with.
2. The administrator goes to the invitation code management page to view the current invitation code.
3. The administrator replies the invitation code to the applicant.
4. The applicant completes the account creation on the administrator invite registration page.

### 5.3 Administrator Notifications Page

The notifications page is used to publish announcements for system users. Administrators can fill in the notification title and body and publish.

![Administrator Notifications Page](user-manual-images/admin-notifications.png)

Before publishing, it is recommended to confirm:

- The title is concise and clear;
- The body contains specific matters, target audience, and time;
- Do not include unnecessary personal sensitive information in the announcement.

## 6. Frequently Asked Questions

### 6.1 Login Failure

- Confirm that the username or email is filled in correctly.
- Confirm that the password is correct.
- Confirm that the role selected on the login page matches the account role.
- If using a demo account fails, please restart or redeploy the local application and confirm that the demo data has been initialized.

### 6.2 TA Cannot Upload Avatar or Resume

- Check whether the file format complies with the page prompts.
- Check whether the file size exceeds the limit.
- Check whether the current session has expired; log in again if necessary.

### 6.3 MO Cannot See Applicants

- Confirm that the TA has submitted an application for a position published by this MO.
- Confirm that the currently logged-in account is the MO to whom the position belongs.
- You can first check the job management page to confirm whether the position exists.

### 6.4 AI Recommendation or Analysis Unavailable

- Confirm that the API Key, model, and network access in the AI configuration file are available.
- Confirm that the current position, application, or TA profile data is complete.
- If the AI service is temporarily unavailable, the page displays a failure notice and does not generate local regular matching results.

### 6.5 Administrator Cannot See Workloads

- Confirm that the current account is logged in with the administrator role.
- Confirm that there are accepted TA applications in the system.
- Confirm that the position has valid weekly hours and work cycle data.

## 7. Screenshot Index

All screenshots in this manual are stored in the `user-manual-images/` subdirectory at the same level as this Markdown file.

| File Name | Corresponding Page | Description |
| -------- | ----------------- | ----------- |
| `home.png` | `/` | Portal Home Page |
| `login.png` | `/login.jsp` | Login Page |
| `register.png` | `/register.jsp` | TA/MO Registration Page |
| `admin-invite-register.png` | `/admin-invite.jsp` | Administrator Invite Registration Page |
| `ta-dashboard.png` | `/jsp/ta/dashboard.jsp` | TA Personal Profile Page |
| `ta-job-list.png` | `/jsp/ta/job-list.jsp` | TA Job List |
| `ta-application-status.png` | `/jsp/ta/application-status.jsp` | TA Application Status |
| `mo-dashboard.png` | `/jsp/mo/dashboard.jsp` | MO Job Management |
| `mo-dashboard-applicants.png` | `/jsp/mo/dashboard.jsp` | MO Applicant Review Sub-View |
| `admin-dashboard.png` | `/jsp/admin/dashboard.jsp` | Administrator TA Workload |
| `admin-invite-management.png` | `/jsp/admin/invite.jsp` | Administrator Invitation Code Management |
| `admin-notifications.png` | `/jsp/admin/notifications.jsp` | Administrator Notifications Page |

## 8. Maintenance Suggestions

- After adding a new major page, supplement this manual with operation instructions and screenshots.
- After adjusting routes, role permissions, or demo accounts, synchronize the updates to the "Common Entry Points and Accounts" chapter.
- After adjusting the core pages for Admin, MO, or TA, recapture the corresponding screenshots to avoid inconsistencies between the manual content and the actual interface.
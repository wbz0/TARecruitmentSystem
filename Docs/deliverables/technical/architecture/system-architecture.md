# System Architecture Design

## 1. Architecture Overview

TA Hiring System adopts the traditional **Three-Tier Architecture**, built on Java Servlet + JSP technology stack. The system is divided into Presentation Layer, Business Logic Layer, and Data Access Layer.

### 1.1 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Client Browser                              │
│                  (HTML + JSP + JavaScript)                        │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼ HTTP / AJAX
┌─────────────────────────────────────────────────────────────────┐
│                    Apache Tomcat 11.x                           │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                   Web Container                              ││
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  ││
│  │  │ AuthFilter  │  │   JSPs      │  │   Static Resources  │  ││
│  │  │ (Security)  │  │ (Views)     │  │   (CSS/JS/Images)   │  ││
│  │  └─────────────┘  └─────────────┘  └─────────────────────┘  ││
│  │                                                              ││
│  │  ┌─────────────────────────────────────────────────────────┐││
│  │  │                    Servlets                             │││
│  │  │  LoginServlet, RegisterServlet, JobServlet, ApplicationServlet│││
│  │  │  ApplicantProfileServlet, AdminInviteAcceptServlet, WorkloadStats... │││
│  │  └─────────────────────────────────────────────────────────┘││
│  │                            │                                 ││
│  │  ┌─────────────────────────┴───────────────────────────────┐││
│  │  │                   Service Layer                          │││
│  │  │  WorkloadStatsService, InviteCodeService,                │││
│  │  │  MoApplicantAiSearchService, TaJobAiSearchService        │││
│  │  └─────────────────────────────────────────────────────────┘││
│  │                            │                                 ││
│  │  ┌─────────────────────────┴───────────────────────────────┐││
│  │  │                    DAO Layer                            │││
│  │  │  UserDao, JobDao, ApplicantDao, ApplicationDao,          │││
│  │  │  NotificationDao                                         │││
│  │  └─────────────────────────────────────────────────────────┘││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    File System (CSV Storage)                      │
│  ${TA_HIRING_DATA_DIR}/  (configured in config.bat)                      │
│  ├── users/           # User data (split by role)                   │
│  │   ├── users_ta.csv                                         │
│  │   ├── users_mo.csv                                         │
│  │   └── users_admin.csv                                      │
│  ├── jobs/jobs.csv     # Job data                               │
│  ├── applicants/       # TA applicant profiles                            │
│  ├── applications/     # Application records                                │
│  ├── invites/          # Invitation records                                │
│  ├── resumes/          # Resume files                                │
│  └── photos/          # Avatar files                                │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Technology Selection Rationale

| Component | Selection | Rationale |
|-----------|-----------|-----------|
| **Servlet** | Jakarta Servlet 6 | Standard Java web component, lightweight and controllable |
| **JSP** | JSP 2.x | Suitable for server-side rendering, works well with Servlet |
| **Tomcat** | Apache Tomcat 11.x | Mainstream lightweight Servlet container |
| **Storage** | CSV files | Simplified deployment, suitable for small projects, no DB required |
| **AI** | DeepSeek compatible API | Used for front-end visible AI recommendation search |

---

## 2. Module Division

### 2.1 Core Modules

| Module | Package Path | Responsibility |
|--------|--------------|----------------|
| **Common Module** | `common/api`, `common/web`, `common/storage`, `common/search`, `common/util` | API route constants, JSON responses, request reading, CSV encoding, storage paths, common search |
| **Auth Module** | `auth/web`, `auth/dao`, `auth/model`, `auth/service`, `auth/validator` | User login, registration, session management, access policy |
| **TA Profile Module** | `profile/web`, `profile/dao`, `profile/model`, `profile/service`, `profile/mapper`, `profile/validator` | TA applicant information, resume and avatar management |
| **Job Module** | `job/web`, `job/service`, `job/mapper`, `job/validator`, `job/dao`, `job/model` | Job CRUD operations |
| **Application Module** | `application/web`, `application/service`, `application/mapper`, `application/validator`, `application/dao`, `application/model` | Application submission, material reading and review workflow |
| **AI Module** | `ai/web`, `ai/service`, `ai/client` | MO applicant recommendations and TA job recommendations |
| **Admin Module** | `admin/web`, `admin/service`, `admin/dao`, `admin/model` | Invitation and statistics functionality |

### 2.2 Layer Dependency Relationship

```
┌──────────────────┐
│   JSP / Filter   │  ← depends on →  ┌─────────────┐
└──────────────────┘           │   Servlets  │
        │                       └──────┬──────┘
        │                              │
        │                       ┌──────▼──────┐
        └──────────────────────▶│  Services   │
                                 └──────┬──────┘
                                        │
                                 ┌──────▼──────┐
                                 │    DAO      │
                                 └──────┬──────┘
                                        │
                                 ┌──────▼──────┐
                                 │ CSV Files   │
                                 └─────────────┘
```

---

## 3. Request Processing Flow

### 3.1 Standard Request Flow

```
HTTP Request
    │
    ▼
AuthFilter.doFilter()
    │ Check Session / Permission
    │
    ▼
Servlet.service()
    │ Parse request parameters
    │
    ▼
Service Layer
    │ Business logic processing
    │
    ▼
DAO Layer
    │ Data persistence
    │
    ▼
CSV File
```

### 3.2 Authentication Flow

```
Unauthenticated Request
    │
    ▼
AuthFilter → Check if public path?
    │
    ├─ Yes → Pass through directly
    │
    └─ No → Check Session
              │
              ├─ No Session → AJAX? Return 401 : Redirect to /login.jsp
              │
              └─ Has Session → Check role permission
                               │
                               ├─ Insufficient permission → AJAX? Return 403 : 403 Error page
                               │
                               └─ Permission passed → Pass through
```

### 3.3 Application Flow

```
TA Submits Application
    │
    ▼
ApplicationServlet (POST)
    │
    ▼
Validate: Job exists? Job open? No duplicate application?
    │
    ▼
Create Application Record
    │
    ▼
Store to applications/applications.csv
    │
    ▼
Return Success / Error
```

---

## 4. Frontend Architecture

### 4.1 Page Structure

```
frontend/webapp/
├── index.jsp           # Homepage (job list entry)
├── login.jsp           # Login page
├── register.jsp        # TA/MO self-service registration
├── admin-invite.jsp    # Admin invitation acceptance page
│
└── jsp/
    ├── ta/             # TA role pages
    │   ├── dashboard.jsp           # TA profile dashboard
    │   ├── job-list.jsp            # Job list
    │   ├── job-detail.jsp          # Job detail
    │   ├── application-status.jsp  # My applications
    │   └── application-detail.jsp  # Application detail
    │
    ├── mo/             # MO role pages
    │   ├── dashboard.jsp           # MO management dashboard and applicant review subview
    │   └── notifications.jsp       # MO notifications
    │
    └── admin/          # Admin role pages
        ├── dashboard.jsp           # Statistics dashboard
        └── invite.jsp              # Short invitation code management
```

### 4.2 AJAX Interaction Pattern

The system extensively uses AJAX for frontend-backend data interaction:

```javascript
// Example: Get job list
window.TARecruitment.api.request(window.TARecruitment.routes.jobs.list(), {
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
}).then(result => renderJobs(result.payload));
```

**AJAX Response Format**:
- Success: `{"success":true,"message":"...","data":{...}}`
- 401: Not logged in, returns `{"success":false,"message":"Please login first"}`
- 403: No permission, returns `{"success":false,"message":"Access denied"}`
- 500: Server error, returns `{"success":false,"message":"..."}`

---

## 5. Configuration Management

### 5.1 Data Directory Configuration

Data directory is specified via `TA_HIRING_DATA_DIR` environment variable, **must** be configured in `scripts/config.bat`:

```batch
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data
```

If not configured, the application will throw an exception on startup.

### 5.2 AI Configuration

AI recommendation search uses configuration file:
```
frontend/webapp/WEB-INF/ai/deepseek.properties.template
```

Configuration items:
- `deepseek.api.key`: DeepSeek API key
- `deepseek.base-url`: OpenAI-compatible API endpoint
- `deepseek.model`: Model name to use
- `deepseek.timeout-ms`: Request timeout

---

## 6. Multi-Language Support

The system supports Chinese and English bilingual switching, controlled via URL parameter `lang`:
- `/jsp/ta/dashboard.jsp?lang=zh` → Chinese
- `/jsp/ta/dashboard.jsp?lang=en` → English

Language files are stored within JSP pages or passed via request attributes.

---

## 7. Error Handling

### 7.1 Error Handling Strategy

| Error Type | Handling |
|------------|----------|
| Parameter validation failure | Return 400 + error message |
| Not logged in | Return 401 + redirect to login page |
| No permission | Return 403 + error message |
| Resource not found | Return 404 + error message |
| Server error | Return 500 + error log |

### 7.2 Unified Error Response

```json
{
    "error": "ErrorType",
    "message": "Human readable message",
    "details": {}  // Optional additional information
}
```
# Authentication and Permission Module Technical Documentation

## 1. Module Overview

Authentication and permission module handles public registration, login, logout, username/email availability check, Session user state, and full-site access control.

**Core Components**:
- `User` - Login account model and `TA` / `MO` / `ADMIN` role enum
- `UserDao` - CSV-based account read, create, update, and login verification
- `LoginServlet` - `/api/auth/login`
- `RegisterServlet` - `/api/auth/register`
- `LogoutServlet` - `/api/auth/logout`
- `CheckAvailableServlet` - `/api/auth/availability`
- `AuthFilter` - Full-site filter
- `AccessPolicy` - Centralized maintenance of public paths and role access policies

---

## 2. Data Model and Storage

### 2.1 User

**Path**: `backend/src/com/example/tarecruitment/auth/model/User.java`

`User` stores account ID, username, password hash, email, role, display name, avatar path, and login time. Role enum only contains:

```java
public enum Role {
    TA, MO, ADMIN
}
```

### 2.2 UserDao

**Path**: `backend/src/com/example/tarecruitment/auth/dao/UserDao.java`

`UserDao` is only responsible for reading, writing, and querying `users.csv`; it does not read request/session, nor does it construct HTTP responses.

| Method | Description |
|------|------|
| `findById(String userId)` | Find account by user ID |
| `findByUsername(String username)` | Find account by username |
| `findByEmail(String email)` | Find account by email |
| `verifyLogin(String usernameOrEmail, String password)` | Verify username/email and password |
| `create(User user)` | Create account and perform uniqueness validation |
| `update(User user)` | Update account information |
| `ensureDefaultDemoAccounts()` | Ensure demo accounts exist |

---

## 3. Auth API

All authentication interfaces use `/api/...` paths; page paths remain as `/login.jsp`, `/register.jsp`, etc.

| Function | Method | Path | Permission |
|------|--------|------|------|
| Login | POST | `/api/auth/login` | Public |
| Register | POST | `/api/auth/register` | Public |
| Logout | POST/GET | `/api/auth/logout` | Logged in users or public logout entry |
| Username/Email availability | GET | `/api/auth/availability?type=&value=` | Public |

### 3.1 Login

**Path**: `backend/src/com/example/tarecruitment/auth/web/LoginServlet.java`

Request parameters:

| Parameter | Required | Description |
|------|------|------|
| `username` | Yes | Username or email |
| `password` | Yes | Password |
| `role` | No | Frontend-selected login role, used to prevent entering wrong entry |
| `rememberMe` | No | `1` means extend Session validity |

Success response uses unified JSON:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "username": "ta_demo",
    "role": "TA",
    "redirect": "/jsp/ta/dashboard.jsp"
  }
}
```

### 3.2 Register

**Path**: `backend/src/com/example/tarecruitment/auth/web/RegisterServlet.java`

Public registration only allows `TA` and `MO`. `ADMIN` must register through admin invitation flow.

| Parameter | Required | Description |
|------|------|------|
| `username` | Yes | Letter start, letters, numbers, underscores allowed |
| `email` | Yes | Unique email |
| `password` | Yes | Contains letters and numbers |
| `confirmPassword` | Yes | Must match password |
| `role` | Yes | `TA` or `MO` |

Registration success returns `201`; does not directly restore old root path to login/registration entry.

### 3.3 Logout

**Path**: `backend/src/com/example/tarecruitment/auth/web/LogoutServlet.java`

Shared sidebar calls `/api/auth/logout`. AJAX requests return JSON; normal browser requests redirect back to `login.jsp`.

---

## 4. Access Control

### 4.1 AuthFilter

**Path**: `backend/src/com/example/tarecruitment/auth/web/AuthFilter.java`

`AuthFilter` intercepts all requests but does not maintain a large hardcoded path set. It will:

1. Pass through static resources.
2. Call `AccessPolicy.isPublic(method, path)` to determine public pages and public APIs.
3. Read current `User` from Session.
4. Call `AccessPolicy.canAccess(method, path, role)` to determine role permissions.
5. For unauthenticated API requests, return unified JSON; for page requests, redirect to login page.

### 4.2 AccessPolicy

**Path**: `backend/src/com/example/tarecruitment/auth/web/AccessPolicy.java`

`AccessPolicy` is the centralized entry for permission strategy; when adding new API routes, check here synchronously.

| Role | Accessible Scope |
|------|-----------------|
| Unauthenticated | Homepage, login page, registration page, admin invitation registration page, public job browsing, public authentication API |
| TA | TA pages, TA API, application API, current user profile API, notification API |
| MO | MO pages, partial TA perspective pages, MO API, TA recommendation/analysis API, application API, account profile API, notification API, job write operations |
| ADMIN | Admin pages and Admin API |

---

## 5. Frontend Call Method

Frontend does not directly concatenate `contextPath + "/api/..."`. Login, registration, logout, and availability checks all generate paths via common route utility:

```javascript
const loginUrl = TARecruitment.routes.auth.login();
TARecruitment.api.request(loginUrl, {
    method: "POST",
    headers: {
        "X-Requested-With": "XMLHttpRequest",
        "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({
        username: username,
        password: password,
        role: role
    }).toString()
});
```

---

## 6. Error Handling

Authentication API uses unified structure:

```json
{ "success": false, "message": "Invalid username/email or password" }
```

Common status codes:

| Status Code | Scenario |
|--------|------|
| 400 | Parameter format error |
| 401 | Not logged in or login failed |
| 403 | Role mismatch or insufficient permission |
| 409 | Duplicate registration username or email |
| 500 | Server exception |

---

## 7. Testing

Current lightweight check entry is:

```bash
bash -n scripts/dev.sh scripts/javadocs.sh
find frontend/webapp/js -name "*.js" -print0 | xargs -0 -n1 node --check
./scripts/javadocs.sh
```

For complete run verification, execute `scripts/dev.sh` to deploy to local Tomcat, then manually check login, registration, logout, and role redirect flows.
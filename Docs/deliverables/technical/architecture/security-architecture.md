# Security Architecture Design

## 1. Security Design Overview

The security architecture of TA Hiring System is implemented based on **Jakarta Servlet Filter**, adopting centralized request filtering and Role-Based Access Control (RBAC) strategy.

---

## 2. Authentication Mechanism

### 2.1 Login Authentication Flow

```
User submits login request (username/email + password)
    │
    ▼
LoginServlet.doPost()
    │
    ▼
UserDao.verifyLogin(usernameOrEmail, password)
    │
    ├── User does not exist or password incorrect → Return error message
    │
    └── Verification successful → Create Session
                      │
                      ▼
                 Session.setAttribute("user", User)
                      │
                      ▼
                 Redirect to role-specific dashboard
```

### 2.2 Password Security

**Password Storage**:

- Algorithm: SHA-256
- Storage: Password hash (not plaintext)

```java
private String hashPassword(String password) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(password.getBytes("UTF-8"));
    // Convert to hex string
    return hexString.toString();
}
```

### 2.3 Session Management

**Session Storage**:

- Use HttpSession to store user information
- Session stores user object (User)

```java
// On login
HttpSession session = request.getSession(true);
session.setAttribute("user", user);

// Get current logged-in user
User user = (User) session.getAttribute("user");
```

**Session Configuration** (web.xml):

```xml
<session-config>
    <session-timeout>30</session-timeout>  <!-- 30 minutes -->
</session-config>
```

---

## 3. Authorization Control

### 3.1 Role-Based Access Control (RBAC)

The system defines three roles:

| Role | Description | Main Permissions |
|------|-------------|-------------------|
| **TA** | Teaching Assistant Applicant | View jobs, apply for jobs, manage personal profile |
| **MO** | Module Owner | Post jobs, review applications, AI recommendations and application analysis |
| **ADMIN** | System Administrator | Workload statistics, send invitations |

### 3.2 AuthFilter Permission Verification

`AuthFilter` is the core security filter, automatically intercepting all requests using `@WebFilter("/*")` annotation.

**Public Paths (no login required)**:

```java
PUBLIC_PATHS = {
    "/", "/index.jsp",
    "/login.jsp", "/register.jsp",
    "/admin-invite.jsp",
    "/api/auth/login",
    "/api/auth/register",
    "/api/auth/logout",
    "/api/auth/availability",
    "/api/admin/invitations/acceptance",
    "/api/jobs"  // Public job list
}
```

**Permission Matrix**:

| Path Pattern | TA | MO | ADMIN |
| ------------------------------------------------- | --- | --- | ----- |
| `/jsp/ta/*` | ✓ | ✗ | ✗ |
| `/jsp/mo/*` | ✗ | ✓ | ✗* |
| `/jsp/admin/*` | ✗ | ✗ | ✓ |
| `/api/ta/*` | ✓ | ✓ | ✗ |
| `/api/mo/*` | ✗ | ✓ | ✗ |
| `/api/jobs` | TA can read, MO can manage their own jobs | ✓ | ✗ |
| `/api/me/applicant-profile/*` | ✓ | ✗ | ✗ |
| `/api/applications/{applicationId}/applicant/*` | ✓ | ✓ | ✓ |

### 3.3 Permission Verification Flow

```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String path = getPath(httpRequest);

    // 1. Check if it's a public path
    if (isPublicPath(path)) {
        chain.doFilter(request, response);
        return;
    }

    // 2. Check login status
    HttpSession session = httpRequest.getSession(false);
    User user = session != null ? (User) session.getAttribute("user") : null;

    if (user == null) {
        if (isAjaxRequest(httpRequest)) {
            // AJAX request returns 401
            response.setStatus(401);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
        } else {
            // Normal request redirects to login page
            response.sendRedirect(contextPath + "/login.jsp");
        }
        return;
    }

    // 3. Verify role permission
    if (!hasPermission(path, user.getRole())) {
        if (isAjaxRequest(httpRequest)) {
            response.setStatus(403);
            response.getWriter().write("{\"error\": \"Forbidden\"}");
        } else {
            response.sendError(403, "Access denied");
        }
        return;
    }

    // 4. Pass through
    chain.doFilter(request, response);
}
```

---

## 4. CSRF Protection

### 4.1 Form Token

The system uses hidden fields to store CSRF tokens:

```jsp
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
```

### 4.2 Request Verification

```java
// Verify in key Servlets
String submittedToken = request.getParameter("csrfToken");
String sessionToken = (String) session.getAttribute("csrfToken");

if (!submittedToken.equals(sessionToken)) {
    // Reject request
    response.sendError(403, "Invalid CSRF token");
}
```

---

## 5. Input Validation

### 5.1 Server-side Validation

All user input is validated on the server:

```java
// Example: Registration validation
if (username == null || username.trim().isEmpty()) {
    return error("Username is required");
}
if (username.length() < 3 || username.length() > 50) {
    return error("Username must be 3-50 characters");
}
if (userDao.existsByUsername(username)) {
    return error("Username already exists");
}
```

### 5.2 XSS Protection

- JSP pages use EL expressions for automatic escaping
- Rich text content uses HTML encoding

```jsp
<!-- Auto-escape -->
<p>${user.username}</p>

<!-- Manual encoding -->
<p>${fn:escapeXml(user.bio)}</p>
```

### 5.3 SQL Injection Protection (CSV Scenario)

Although using CSV storage, input sanitization is still performed:

```java
// CSV escape
private static String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
}
```

---

## 6. File Upload Security

### 6.1 Resume Upload

- **Allowed types**: PDF
- **Size limit**: 5MB (frontend check)
- **Filename**: Renamed using UUID

```java
// Generate secure filename
String extension = FilenameUtils.getExtension(originalFilename);
String newFilename = userId + "_resume_" + System.currentTimeMillis() + "." + extension;
```

### 6.2 Avatar Upload

- **Allowed types**: JPG, PNG, GIF
- **Size limit**: 2MB (frontend check)
- **Storage path**: `{DATA_DIR}/photos/`

---

## 7. Session Security

### 7.1 Session Fixation Protection

Recreate Session after successful login:

```java
// Login success
HttpSession oldSession = request.getSession(false);
if (oldSession != null) {
    oldSession.invalidate();
}
HttpSession newSession = request.getSession(true);
newSession.setAttribute("user", user);
```

### 7.2 Logout Handling

```java
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();
    }
    response.sendRedirect(request.getContextPath() + "/login.jsp");
}
```

---

## 8. Sensitive Operation Audit

### 8.1 Logging

Key operations are logged to console:

```java
System.out.println("[AuthFilter] User " + user.getUsername() +
    " accessed " + path);
```

### 8.2 Short Invitation Code Security

Admin entry uses 8-character short invitation code, generated by server secret and time window:

```java
String code = InviteCodeService.getInstance().getCurrentCode();
boolean valid = InviteCodeService.getInstance().isValidCode(code);
```

---

## 9. Security Configuration Checklist

| Configuration | Recommended Value | Current Status |
|-------------|-------------------|----------------|
| Session timeout | 30 minutes | ✓ 30 minutes |
| Password hash | BCrypt/Argon2 | SHA-256 (demo) |
| HTTPS | Required in production | Optional in dev |
| CORS | Restrict origins | Not configured |
| File upload size | ≤ 5MB | ✓ Frontend check |
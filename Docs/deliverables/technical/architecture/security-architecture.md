# 安全架构设计

## 1. 安全设计概述

TA Hiring System 的安全架构基于 **Jakarta Servlet Filter** 实现，采用集中的请求过滤和基于角色的访问控制 (RBAC) 策略。

---

## 2. 认证机制

### 2.1 登录认证流程

```
用户提交登录请求 (username/email + password)
    │
    ▼
LoginServlet.doPost()
    │
    ▼
UserDao.verifyLogin(usernameOrEmail, password)
    │
    ├── 用户不存在或密码错误 → 返回错误信息
    │
    └── 验证成功 → 创建 Session
                      │
                      ▼
                 Session.setAttribute("user", User)
                      │
                      ▼
                 重定向到角色对应仪表盘
```

### 2.2 密码安全

**密码存储**：

- 算法：SHA-256
- 存储：密码哈希值（非明文）

```java
private String hashPassword(String password) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(password.getBytes("UTF-8"));
    // 转换为十六进制字符串
    return hexString.toString();
}
```

### 2.3 会话管理

**会话存储**：

- 使用 HttpSession 存储用户信息
- Session 存储用户对象 (User)

```java
// 登录时
HttpSession session = request.getSession(true);
session.setAttribute("user", user);

// 获取当前登录用户
User user = (User) session.getAttribute("user");
```

**会话配置** (web.xml)：

```xml
<session-config>
    <session-timeout>30</session-timeout>  <!-- 30分钟 -->
</session-config>
```

---

## 3. 权限控制

### 3.1 基于角色的访问控制 (RBAC)

系统定义了三种角色：

| 角色            | 说明                      | 主要权限                         |
| --------------- | ------------------------- | -------------------------------- |
| **TA**    | Teaching Assistant 申请人 | 查看职位、申请职位、管理个人档案 |
| **MO**    | Module Owner 模块负责人   | 发布职位、审核申请、AI 推荐和申请分析   |
| **ADMIN** | 系统管理员                | 工作量统计、发送邀请             |

### 3.2 AuthFilter 权限验证

`AuthFilter` 是核心的安全过滤器，使用 `@WebFilter("/*")` 注解自动拦截所有请求。

**公开路径 (无需登录)**：

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
    "/api/jobs"  // 职位公开列表
}
```

**权限矩阵**：

| 路径模式                                          | TA                         | MO | ADMIN |
| ------------------------------------------------- | -------------------------- | -- | ----- |
| `/jsp/ta/*`                                     | ✓                         | ✗ | ✗    |
| `/jsp/mo/*`                                     | ✗                         | ✓ | ✗*   |
| `/jsp/admin/*`                                  | ✗                         | ✗ | ✓    |
| `/api/ta/*`                                     | ✓                         | ✓ | ✗    |
| `/api/mo/*`                                     | ✗                         | ✓ | ✗    |
| `/api/jobs`                                     | TA 可读，MO 可管理本人职位 | ✓ | ✗    |
| `/api/me/applicant-profile/*`                   | ✓                         | ✗ | ✗    |
| `/api/applications/{applicationId}/applicant/*` | ✓                         | ✓ | ✓    |

### 3.3 权限验证流程

```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String path = getPath(httpRequest);

    // 1. 检查是否为公开路径
    if (isPublicPath(path)) {
        chain.doFilter(request, response);
        return;
    }

    // 2. 检查登录状态
    HttpSession session = httpRequest.getSession(false);
    User user = session != null ? (User) session.getAttribute("user") : null;

    if (user == null) {
        if (isAjaxRequest(httpRequest)) {
            // AJAX 请求返回 401
            response.setStatus(401);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
        } else {
            // 普通请求重定向到登录页
            response.sendRedirect(contextPath + "/login.jsp");
        }
        return;
    }

    // 3. 验证角色权限
    if (!hasPermission(path, user.getRole())) {
        if (isAjaxRequest(httpRequest)) {
            response.setStatus(403);
            response.getWriter().write("{\"error\": \"Forbidden\"}");
        } else {
            response.sendError(403, "Access denied");
        }
        return;
    }

    // 4. 放行
    chain.doFilter(request, response);
}
```

---

## 4. CSRF 防护

### 4.1 表单 Token

系统使用隐藏字段存储 CSRF Token：

```jsp
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
```

### 4.2 请求验证

```java
// 在关键 Servlet 中验证
String submittedToken = request.getParameter("csrfToken");
String sessionToken = (String) session.getAttribute("csrfToken");

if (!submittedToken.equals(sessionToken)) {
    // 拒绝请求
    response.sendError(403, "Invalid CSRF token");
}
```

---

## 5. 输入验证

### 5.1 服务端验证

所有用户输入在服务端进行验证：

```java
// 示例：注册验证
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

### 5.2 XSS 防护

- JSP 页面使用 EL 表达式自动转义
- 富文本内容使用 HTML 编码

```jsp
<!-- 自动转义 -->
<p>${user.username}</p>

<!-- 手动编码 -->
<p>${fn:escapeXml(user.bio)}</p>
```

### 5.3 SQL 注入防护 (CSV 场景)

虽然使用 CSV 存储，但仍进行输入清理：

```java
// CSV 转义
private static String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
}
```

---

## 6. 文件上传安全

### 6.1 简历上传

- **允许类型**: PDF
- **大小限制**: 5MB (前端检查)
- **文件名**: 使用 UUID 重命名

```java
// 生成安全文件名
String extension = FilenameUtils.getExtension(originalFilename);
String newFilename = userId + "_resume_" + System.currentTimeMillis() + "." + extension;
```

### 6.2 头像上传

- **允许类型**: JPG, PNG, GIF
- **大小限制**: 2MB (前端检查)
- **存储路径**: `{DATA_DIR}/photos/`

---

## 7. Session 安全

### 7.1 Session 固定防护

登录成功后重新创建 Session：

```java
// 登录成功
HttpSession oldSession = request.getSession(false);
if (oldSession != null) {
    oldSession.invalidate();
}
HttpSession newSession = request.getSession(true);
newSession.setAttribute("user", user);
```

### 7.2 登出处理

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

## 8. 敏感操作审计

### 8.1 日志记录

关键操作记录到控制台日志：

```java
System.out.println("[AuthFilter] User " + user.getUsername() +
    " accessed " + path);
```

### 8.2 短邀请码安全

管理员入口使用 8 位短邀请码，由服务端密钥和时间窗口生成：

```java
String code = InviteCodeService.getInstance().getCurrentCode();
boolean valid = InviteCodeService.getInstance().isValidCode(code);
```

---

## 9. 安全配置清单

| 配置项       | 建议值        | 当前状态       |
| ------------ | ------------- | -------------- |
| Session 超时 | 30 分钟       | ✓ 30分钟      |
| 密码哈希     | BCrypt/Argon2 | SHA-256 (演示) |
| HTTPS        | 生产必须      | 开发环境可选   |
| CORS         | 限制来源      | 未配置         |
| 文件上传大小 | ≤ 5MB        | ✓ 前端检查    |

# 认证与权限模块技术文档

## 1. 模块概述

认证与权限模块负责公开注册、登录、退出登录、用户名/邮箱可用性检查、Session 用户状态和全站访问控制。

**核心组件**：
- `User` - 登录账号模型和 `TA` / `MO` / `ADMIN` 角色枚举
- `UserDao` - 基于 CSV 的账号读取、创建、更新和登录验证
- `LoginServlet` - `/api/auth/login`
- `RegisterServlet` - `/api/auth/register`
- `LogoutServlet` - `/api/auth/logout`
- `CheckAvailableServlet` - `/api/auth/availability`
- `AuthFilter` - 全站过滤器
- `AccessPolicy` - 集中维护公开路径和角色访问策略

---

## 2. 数据模型与存储

### 2.1 User

**路径**: `backend/src/com/example/tarecruitment/auth/model/User.java`

`User` 保存账号 ID、用户名、密码散列、邮箱、角色、展示名、头像路径和登录时间等字段。角色枚举只包含：

```java
public enum Role {
    TA, MO, ADMIN
}
```

### 2.2 UserDao

**路径**: `backend/src/com/example/tarecruitment/auth/dao/UserDao.java`

`UserDao` 只负责 `users.csv` 的读写和查询，不读取 request/session，也不拼 HTTP 响应。

| 方法 | 说明 |
|------|------|
| `findById(String userId)` | 根据用户 ID 查找账号 |
| `findByUsername(String username)` | 根据用户名查找账号 |
| `findByEmail(String email)` | 根据邮箱查找账号 |
| `verifyLogin(String usernameOrEmail, String password)` | 验证用户名/邮箱和密码 |
| `create(User user)` | 创建账号并执行唯一性校验 |
| `update(User user)` | 更新账号资料 |
| `ensureDefaultDemoAccounts()` | 确保演示账号存在 |

---

## 3. Auth API

所有认证接口都使用 `/api/...` 路径，页面路径仍然是 `/login.jsp`、`/register.jsp` 等 JSP。

| 功能 | Method | Path | 权限 |
|------|--------|------|------|
| 登录 | POST | `/api/auth/login` | 公开 |
| 注册 | POST | `/api/auth/register` | 公开 |
| 登出 | POST/GET | `/api/auth/logout` | 登录用户或公开退出入口 |
| 用户名/邮箱可用性 | GET | `/api/auth/availability?type=&value=` | 公开 |

### 3.1 登录

**路径**: `backend/src/com/example/tarecruitment/auth/web/LoginServlet.java`

请求参数：

| 参数 | 必需 | 说明 |
|------|------|------|
| `username` | 是 | 用户名或邮箱 |
| `password` | 是 | 密码 |
| `role` | 否 | 前端选择的登录角色，用于防止进错入口 |
| `rememberMe` | 否 | `1` 表示延长 Session 有效期 |

成功响应使用统一 JSON：

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

### 3.2 注册

**路径**: `backend/src/com/example/tarecruitment/auth/web/RegisterServlet.java`

公开注册只允许 `TA` 和 `MO`。`ADMIN` 必须通过管理员邀请流程注册。

| 参数 | 必需 | 说明 |
|------|------|------|
| `username` | 是 | 字母开头，允许字母、数字、下划线 |
| `email` | 是 | 唯一邮箱 |
| `password` | 是 | 包含字母和数字 |
| `confirmPassword` | 是 | 与密码一致 |
| `role` | 是 | `TA` 或 `MO` |

注册成功返回 `201`，不直接把旧根路径恢复为登录/注册入口。

### 3.3 退出登录

**路径**: `backend/src/com/example/tarecruitment/auth/web/LogoutServlet.java`

共享侧边栏通过 `/api/auth/logout` 调用。AJAX 请求返回 JSON，普通浏览器请求会跳回 `login.jsp`。

---

## 4. 访问控制

### 4.1 AuthFilter

**路径**: `backend/src/com/example/tarecruitment/auth/web/AuthFilter.java`

`AuthFilter` 拦截所有请求，但不维护大型硬编码路径集合。它会：

1. 放行静态资源。
2. 调用 `AccessPolicy.isPublic(method, path)` 判断公开页面和公开 API。
3. 从 Session 读取当前 `User`。
4. 调用 `AccessPolicy.canAccess(method, path, role)` 判断角色权限。
5. 未登录时对 API 返回统一 JSON，对页面跳转登录页。

### 4.2 AccessPolicy

**路径**: `backend/src/com/example/tarecruitment/auth/web/AccessPolicy.java`

`AccessPolicy` 是权限策略的集中入口，新增 API 路由时应同步检查这里。

| 角色 | 可访问范围 |
|------|------------|
| 未登录 | 首页、登录页、注册页、管理员邀请注册页、公开职位浏览、公开认证 API |
| TA | TA 页面、TA API、申请 API、当前用户资料 API、通知 API |
| MO | MO 页面、部分 TA 视角页面、MO API、TA 推荐/分析 API、申请 API、账号资料 API、通知 API、职位写操作 |
| ADMIN | Admin 页面和 Admin API |

---

## 5. 前端调用方式

前端不直接拼 `contextPath + "/api/..."`。登录、注册、登出和可用性检查都通过公共路由工具生成路径：

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

## 6. 错误处理

认证 API 使用统一结构：

```json
{ "success": false, "message": "Invalid username/email or password" }
```

常见状态码：

| 状态码 | 场景 |
|--------|------|
| 400 | 参数格式错误 |
| 401 | 未登录或登录失败 |
| 403 | 角色不匹配或权限不足 |
| 409 | 注册用户名或邮箱重复 |
| 500 | 服务器异常 |

---

## 7. 测试

当前轻量检查入口为：

```bash
bash -n scripts/dev.sh scripts/javadocs.sh
find frontend/webapp/js -name "*.js" -print0 | xargs -0 -n1 node --check
./scripts/javadocs.sh
```

如需完整运行验证，可执行 `scripts/dev.sh` 部署到本地 Tomcat 后按登录、注册、退出和角色跳转流程手工检查。

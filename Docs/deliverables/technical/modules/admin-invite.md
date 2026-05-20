# 管理员短邀请码模块技术文档

## 1. 模块概述

管理员邀请注册现在只保留短邀请码流程。Admin 在后台查看或刷新 8 位邀请码，受邀用户打开 `/admin-invite.jsp`，填写邮箱、用户名、密码和短邀请码后创建 `ADMIN` 账号。

**核心组件**：

- `InviteCodeService` - 基于服务端密钥和时间窗口生成/校验短邀请码
- `AdminCurrentInviteCodeServlet` - 管理员读取或刷新当前短邀请码
- `AdminInviteAcceptServlet` - 公开注册入口，校验短码并创建管理员账号
- `admin-invite.jsp` / `js/auth/admin-invite.js` - 管理员邀请注册页面
- `jsp/admin/invite.jsp` / `js/admin/admin-invite-management.js` - 管理员短码管理页

---

## 2. 后端流程

### 2.1 InviteCodeService

**路径**: `backend/src/com/example/tarecruitment/admin/service/InviteCodeService.java`

`InviteCodeService` 使用 HMAC-SHA256 和服务端密钥生成 8 位短码。默认每 10 分钟一个窗口，校验时接受当前窗口以及前后相邻窗口，避免用户卡在倒计时边界失败。

服务端持久化状态位于 `TA_HIRING_DATA_DIR/invites/`：

| 文件 | 说明 |
|------|------|
| `invite_secret.bin` | 生成短码的服务端密钥 |
| `rotation_offset.txt` | 管理员主动刷新后的轮换偏移 |
| `forced_window_start.txt` | 手动刷新后新窗口的开始时间 |

### 2.2 AdminCurrentInviteCodeServlet

**路径**: `backend/src/com/example/tarecruitment/admin/web/AdminCurrentInviteCodeServlet.java`

| Method | Path | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/admin/invitations/current-code` | Admin | 返回当前短码和剩余秒数 |
| POST | `/api/admin/invitations/current-code` | Admin | 主动轮换短码并返回新短码 |

### 2.3 AdminInviteAcceptServlet

**路径**: `backend/src/com/example/tarecruitment/admin/web/AdminInviteAcceptServlet.java`

| Method | Path | 权限 | 说明 |
|---|---|---|---|
| POST | `/api/admin/invitations/acceptance` | 公开 | 校验短邀请码并创建 Admin 账号 |

请求字段：

| 字段 | 说明 |
|------|------|
| `email` | 管理员邮箱 |
| `username` | 管理员用户名 |
| `password` | 登录密码 |
| `inviteCode` | 8 位短邀请码 |

校验通过后，Servlet 通过 `UserDao` 写入 `users/users_admin.csv`。

---

## 3. 前端流程

### 3.1 管理员短码管理页

**路径**: `frontend/webapp/jsp/admin/invite.jsp`

页面通过 `TARecruitment.routes.admin.currentInvitationCode()` 调用当前短码接口。Admin 可以查看当前短码、剩余时间，并主动刷新短码。

### 3.2 管理员邀请注册页

**路径**: `frontend/webapp/admin-invite.jsp`

页面通过 `TARecruitment.routes.admin.invitationAcceptance()` 提交注册表单。当前流程不再提交 token，也不再访问旧的邀请验证接口。

---

## 4. 当前保留与已移除

保留：

- `/admin-invite.jsp`
- `/api/admin/invitations/acceptance`
- `/api/admin/invitations/current-code`

已移除：

- 邮件/token 邀请记录链路
- 旧邀请创建和验证接口
- 旧管理员注册说明页

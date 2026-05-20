# Servlet API 接口文档

## 1. 概述

TA Hiring System 的 JSON 和文件资源接口统一放在 `/api/...` 下。JSP 页面路径继续保留，例如 `/login.jsp`、`/jsp/ta/job-list.jsp`。

通用响应结构：

```json
{ "success": true, "message": "OK", "data": {} }
```

失败响应保持同一结构：

```json
{ "success": false, "message": "Error message" }
```

## 2. Auth

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| 登录 | POST | `/api/auth/login` | 公开 |
| 注册 | POST | `/api/auth/register` | 公开 |
| 登出 | POST/GET | `/api/auth/logout` | 登录用户 |
| 用户名/邮箱可用性 | GET | `/api/auth/availability?type=&value=` | 公开 |

## 3. Jobs

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| 职位列表 | GET | `/api/jobs` | 公开 |
| 职位详情 | GET | `/api/jobs/{jobId}` | 公开 |
| 创建职位 | POST | `/api/jobs` | MO |
| 更新职位 | PUT | `/api/jobs/{jobId}` | 职位所属 MO |
| 删除职位 | DELETE | `/api/jobs/{jobId}` | 职位所属 MO |

常用查询参数：`keyword`、`courseCode`、`status`、`moId`。

## 4. Applications

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| 申请列表 | GET | `/api/applications` | TA/MO/Admin 按角色过滤 |
| 申请详情 | GET | `/api/applications/{applicationId}` | 申请人、职位所属 MO、Admin |
| 创建申请 | POST | `/api/applications` | TA |
| 状态流转 | POST | `/api/applications/{applicationId}/transition` | TA/MO/Admin 按 action 判断 |

创建申请参数：`jobId`、`coverLetter`。

状态流转参数：`action=accept|reject|withdraw`。

## 5. Current User Profile

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| 当前账号信息 | GET/POST | `/api/me/account` | TA/MO |
| 当前账号头像 | GET | `/api/me/avatar` | TA/MO |
| 当前 TA 档案 | GET/POST/PUT | `/api/me/applicant-profile` | TA |
| 当前 TA 档案头像 | GET | `/api/me/applicant-profile/photo` | TA |
| 当前 TA 档案简历 | GET | `/api/me/applicant-profile/resume` | TA |
| 草稿简历 | POST/DELETE | `/api/me/applicant-profile/resume-draft` | TA |

## 6. Application Applicant Materials

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| 申请关联的申请人资料 | GET | `/api/applications/{applicationId}/applicant` | 申请人、职位所属 MO、Admin |
| 申请关联的简历 | GET | `/api/applications/{applicationId}/applicant/resume` | 申请人、职位所属 MO、Admin |
| 申请关联的头像 | GET | `/api/applications/{applicationId}/applicant/photo` | 申请人、职位所属 MO、Admin |

## 7. Admin

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| TA 工作量统计 | GET | `/api/admin/workload-statistics` | Admin |
| 接受管理员短邀请码注册 | POST | `/api/admin/invitations/acceptance` | 公开 |
| 当前短邀请码 | GET/POST | `/api/admin/invitations/current-code` | Admin |

## 8. Notifications

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| 通知列表 | GET | `/api/notifications` | 登录用户 |
| 发布通知 | POST | `/api/notifications` | Admin |
| 删除通知 | DELETE | `/api/notifications?notificationId=` | Admin |

## 9. AI

| 功能 | Method | Path | 权限 |
|---|---|---|---|
| MO 申请人推荐 | POST | `/api/mo/applicant-recommendations` | MO |
| TA 职位推荐 | POST | `/api/ta/job-recommendations` | TA |

AI 推荐配置模板为 `frontend/webapp/WEB-INF/ai/deepseek.properties.template`；本地密钥文件为 `deepseek.local.properties`，不应提交到 git。

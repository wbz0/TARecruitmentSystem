# member1 分工与当前代码文件

[返回总览](Overview.md)

## 基本信息

| 项目 | 内容 |
| --- | --- |
| Git author | `member1 <member1@edu.com>` |
| 标准提交数 | 18 |
| 分工概述 | 后端基础能力、认证流程、接口响应与日志工具、技能匹配早期实现、部分测试/统计补充 |

## 分工概述

`member1` 主要承担后端早期基础能力：用户认证、登录注册、Session/权限校验、统一响应工具、日志工具，以及技能匹配服务早期实现。后续也补过职位筛选响应修复、TA 工作量统计和脚本日志相关内容。早期技能匹配入口和旧邀请工具后续已被下线，因此不再列入当前仍存在的代码文件。

## 当前对应代码文件

后端认证与会话入口：

- `backend/src/com/example/tarecruitment/auth/model/User.java`
- `backend/src/com/example/tarecruitment/auth/dao/UserDao.java`
- `backend/src/com/example/tarecruitment/auth/web/LoginServlet.java`
- `backend/src/com/example/tarecruitment/auth/web/RegisterServlet.java`
- `backend/src/com/example/tarecruitment/auth/web/LogoutServlet.java`
- `backend/src/com/example/tarecruitment/auth/web/AuthFilter.java`
- `backend/src/com/example/tarecruitment/auth/web/AccessPolicy.java`
- `backend/src/com/example/tarecruitment/auth/web/CheckAvailableServlet.java`

后端公共响应、Session、权限和日志工具：

- `backend/src/com/example/tarecruitment/common/web/ApiResponses.java`
- `backend/src/com/example/tarecruitment/common/web/JsonResponseUtil.java`
- `backend/src/com/example/tarecruitment/common/web/SessionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/PermissionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/WebRequests.java`
- `backend/src/com/example/tarecruitment/common/service/ServiceResult.java`
- `backend/src/com/example/tarecruitment/common/util/Logger.java`

## 文件重叠与答辩归属说明

当前 member1 与 member4 不再共享旧邀请工具；管理员短邀请码由 member4 的 `InviteCodeService` 负责说明。

## 测试展示

运行命令：

```bash
./scripts/test/test-member1.sh
```

测试代码：

- `backend/test/Member1BackendTest.java`

测试覆盖点：

- `ServiceResult` 是否能稳定表达 service 层的状态码、成功标记、消息和数据。
- `User` 的 CSV 序列化/反序列化是否保留账号资料字段。
- `UserDao` 是否能初始化固定演示账号、验证登录、拒绝重复用户名。

答辩时可以这样解释：

`member1` 的测试重点是认证和公共后端基础能力。脚本会先编译后端源码，再使用临时 `TA_HIRING_DATA_DIR` 创建隔离测试数据，不会污染本机真实演示数据。测试通过说明登录账号、演示账号、密码哈希和 service 返回结构这些底层能力可以正常工作。

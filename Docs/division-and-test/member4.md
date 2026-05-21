# member4 分工与当前代码文件

[返回总览](Overview.md)

## 基本信息

| 项目 | 内容 |
| --- | --- |
| Git author | `member4 <member4@edu.com>` |
| 标准提交数 | 23 |
| 分工概述 | 申请流程、状态流转、TA 撤回、MO 选择、通知与短邀请码业务、集成测试/用户手册早期工作 |

## 分工概述

`member4` 主要承担申请业务流程：职位申请、申请状态查询、MO 选择/录用、流程阶段收敛、TA 撤回申请和审核状态同步。后期也补充通知与管理员短邀请码服务，并继续完善职位申请与账号资料相关服务边界；早期承担过集成测试、打包和用户手册类工作。

## 当前对应代码文件

职位申请、申请状态、MO 审核与 TA 撤回：

- `backend/src/com/example/tarecruitment/application/model/Application.java`
- `backend/src/com/example/tarecruitment/application/dao/ApplicationDao.java`
- `backend/src/com/example/tarecruitment/application/mapper/ApplicationRequestMapper.java`
- `backend/src/com/example/tarecruitment/application/mapper/ApplicationResponseMapper.java`
- `backend/src/com/example/tarecruitment/application/service/ApplicationApplicantService.java`
- `backend/src/com/example/tarecruitment/application/service/ApplicationService.java`
- `backend/src/com/example/tarecruitment/application/validator/ApplicationValidator.java`
- `backend/src/com/example/tarecruitment/application/web/ApplicationServlet.java`

通知业务：

- `backend/src/com/example/tarecruitment/notification/model/Notification.java`
- `backend/src/com/example/tarecruitment/notification/dao/NotificationDao.java`
- `backend/src/com/example/tarecruitment/notification/web/NotificationServlet.java`

管理员邀请码业务：

- `backend/src/com/example/tarecruitment/admin/service/InviteCodeService.java`
- `backend/src/com/example/tarecruitment/admin/web/AdminCurrentInviteCodeServlet.java`
- `backend/src/com/example/tarecruitment/admin/web/AdminInviteAcceptServlet.java`

工作量统计规则协作文件（**重叠标注：member3 主归属；member4 说明申请状态如何影响统计口径**）：

- `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java`（重叠文件，member3 答辩主归属）
- `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java`（重叠文件，member3 答辩主归属）

相关公共能力：

- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java`（重叠文件，member3 答辩主归属）

## 文件重叠与答辩归属说明

- `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java` 和 `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java` 同时出现在 member3 文档中。答辩时工作量统计接口由 member3 主讲；member4 只说明申请状态、录用和撤回流程如何影响统计数据。
- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java` 同时出现在 member3 文档中。答辩时搜索工具由 member3 主讲；member4 只说明申请/统计场景中如何使用筛选能力。

## 测试展示

运行命令：

```bash
./scripts/test/test-member4.sh
```

测试代码：

- `backend/test/Member4BackendTest.java`

测试覆盖点：

- `ApplicationValidator` 是否校验申请 ID、职位 ID、求职信和状态流转动作。
- `Application` 的 CSV 序列化/反序列化是否保留申请人、状态和进度阶段。
- `ApplicationDao` 是否能创建申请，并完成接受、撤回等状态流转。
- `Notification` 是否能保存和读取系统公告字段。
- `InviteCodeService` 是否能生成并校验当前短邀请码。

答辩时可以这样解释：

`member4` 的测试重点是申请流程和状态流转。脚本会创建临时申请数据，模拟 TA 已申请、MO 接受申请、TA 撤回申请等关键流程。测试通过说明申请状态、进度阶段、通知和管理员短邀请码这些业务对象可以稳定保存和读取。

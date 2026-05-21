# member3 分工与当前代码文件

[返回总览](Overview.md)

## 基本信息

| 项目 | 内容 |
| --- | --- |
| Git author | `member3 <member3@edu.com>` |
| 标准提交数 | 19 |
| 分工概述 | 职位发布/查询/校验、工作量统计接口、账号资料同步、AI 配置模板 |

## 分工概述

`member3` 主要承担职位模块和校验逻辑：职位创建、列表筛选、编辑删除、结构化字段校验、职位有效状态处理。同时承担管理员/MO 工作量统计的一部分后端能力，并在后期补充账号资料同步更新。

## 当前对应代码文件

职位发布、职位列表、结构化字段和校验：

- `backend/src/com/example/tarecruitment/job/model/Job.java`
- `backend/src/com/example/tarecruitment/job/dao/JobDao.java`
- `backend/src/com/example/tarecruitment/job/mapper/JobRequestMapper.java`
- `backend/src/com/example/tarecruitment/job/mapper/JobResponseMapper.java`
- `backend/src/com/example/tarecruitment/job/service/JobService.java`
- `backend/src/com/example/tarecruitment/job/validator/JobValidator.java`
- `backend/src/com/example/tarecruitment/job/web/JobServlet.java`

账号资料同步：

- `backend/src/com/example/tarecruitment/profile/mapper/AccountProfileResponseMapper.java`
- `backend/src/com/example/tarecruitment/profile/service/AccountProfileService.java`
- `backend/src/com/example/tarecruitment/profile/validator/AccountProfileValidator.java`
- `backend/src/com/example/tarecruitment/profile/web/AccountProfileServlet.java`

工作量统计：

- `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java`（**重叠标注：member3 主归属；member4 的申请流程会影响统计口径**）
- `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java`（**重叠标注：member3 主归属；member4 的申请流程会影响统计口径**）

相关公共能力：

- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java`（**重叠标注：member3 主归属；member4 的申请/统计说明会依赖搜索能力**）
- `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java`（重叠文件，member2 答辩主归属）

## 文件重叠与答辩归属说明

- `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java` 和 `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java` 同时出现在 member4 文档中，因为工作量统计需要读取申请状态和录用结果。答辩时统计接口和统计规则由 member3 主讲；member4 只说明申请状态如何影响统计结果。
- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java` 同时出现在 member4 文档中，因为申请/统计说明会提到筛选和搜索能力。答辩时该搜索工具由 member3 主讲。
- `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java` 同时出现在 member2 文档中。答辩时该数据路径基础设施由 member2 主讲；member3 只说明职位模块依赖它读取测试数据目录。

## 测试展示

运行命令：

```bash
./scripts/test/test-member3.sh
```

测试代码：

- `backend/test/Member3BackendTest.java`

测试覆盖点：

- `JobValidator` 是否接受合法职位，并拒绝危险标题、重复技能和错误分隔符。
- `Job` 的有效状态是否能根据截止时间从 `OPEN` 自动转为 `CLOSED`。
- `JobDao` 是否能创建职位、搜索职位字段并更新职位状态。
- `AccountProfileValidator` 是否能校验用户名、TA 实名和上传文件名。

答辩时可以这样解释：

`member3` 的测试重点是职位发布、结构化校验和账号资料同步。测试不仅验证“能创建职位”，也验证错误输入会被后端挡住，例如重复技能、危险 HTML 和非法用户名。这样可以说明职位模块不是只靠前端限制，而是后端也有规则兜底。

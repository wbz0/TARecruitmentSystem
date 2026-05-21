# 重叠文件与答辩归属说明

[返回总览](Overview.md)

## 说明原则

成员文档中的“当前对应代码文件”用于展示每个人参与过、依赖过或需要解释的代码范围。部分文件会同时出现在多个成员文档中，这是因为项目后期做过架构重组，业务模块之间也存在真实协作关系。

答辩时按下面规则处理：

- **主归属**：负责解释该文件的设计、核心逻辑、测试点和主要风险。
- **协作/依赖**：可以说明自己负责的业务为什么会用到该文件，但不重复讲该文件的完整实现。
- member1 到 member5 的业务答辩以“主归属”为准；member6 的重叠更多是 leader 视角的架构覆盖，不代表重复认领每个业务文件。

## member1 到 member5 的重叠文件

| 重叠文件 | 出现位置 | 答辩主归属 | 协作/依赖说明 |
| --- | --- | --- | --- |
| `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java` | member2、member3 | member2 | member3 的职位模块和配置测试依赖运行时数据目录。 |
| `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java` | member3、member4 | member3 | member4 的申请/统计说明会涉及筛选和搜索能力。 |
| `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java` | member3、member4 | member3 | member4 说明申请状态、录用和撤回如何影响统计口径。 |
| `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java` | member3、member4 | member3 | member4 说明申请流程与统计接口的业务关联。 |

## 各成员答辩重点

| 成员 | 答辩主讲重点 | 遇到重叠文件时怎么讲 |
| --- | --- | --- |
| member1 | 认证、Session、权限、统一响应和日志工具 | 认证与公共返回结构由 member1 主讲。 |
| member2 | TA 档案、文件上传、CSV/数据路径、DeepSeek 推荐搜索 | `StoragePaths.java` 由 member2 主讲；AI 推荐搜索由 member2 主讲。 |
| member3 | 职位发布/查询/校验、工作量统计、账号资料同步 | 工作量统计和搜索工具由 member3 主讲。 |
| member4 | 申请流程、状态流转、通知、管理员邀请码业务 | 工作量统计和公共工具只讲业务使用场景，不重复讲实现。 |
| member5 | 前端页面、交互、样式、页面 JS API 调用方式 | member5 与 member1 到 member4 没有直接代码路径重叠。 |

## member6 的特殊说明

member6 是项目 leader 和架构整理角色，文档中可能覆盖公共前端、公共后端、脚本、文档和架构迁移文件。member6 的答辩重点是“整体结构是否统一、旧接口是否清理、前后端路由是否同步”，不抢占 member1 到 member5 的业务实现主归属。

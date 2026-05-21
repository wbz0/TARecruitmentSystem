# member2 分工与当前代码文件

[返回总览](Overview.md)

## 基本信息

| 项目 | 内容 |
| --- | --- |
| Git author | `member2 <member2@edu.com>` |
| 标准提交数 | 20 |
| 分工概述 | TA 档案与文件上传、数据路径/初始化稳定性、AI 推荐搜索与匹配服务 |

## 分工概述

`member2` 主要承担 TA 档案、简历/头像等资源上传、数据存储路径与演示账号初始化稳定性，并在后期负责 AI 推荐搜索的后端接入，包括 DeepSeek 相关配置和客户端。旧技能匹配服务和详情页额外分析链路下线后，AI 相关后端范围收敛为推荐搜索接口。

## 当前对应代码文件

TA 档案、简历、头像与草稿资料：

- `backend/src/com/example/tarecruitment/profile/model/Applicant.java`
- `backend/src/com/example/tarecruitment/profile/dao/ApplicantDao.java`
- `backend/src/com/example/tarecruitment/profile/mapper/ApplicantProfileRequestMapper.java`
- `backend/src/com/example/tarecruitment/profile/mapper/ApplicantProfileResponseMapper.java`
- `backend/src/com/example/tarecruitment/profile/service/ApplicantProfileService.java`
- `backend/src/com/example/tarecruitment/profile/service/ProfileAssetService.java`
- `backend/src/com/example/tarecruitment/profile/validator/ApplicantProfileInput.java`
- `backend/src/com/example/tarecruitment/profile/validator/ApplicantProfileValidator.java`
- `backend/src/com/example/tarecruitment/profile/validator/ProfileAssetValidator.java`
- `backend/src/com/example/tarecruitment/profile/web/ApplicantProfileServlet.java`
- `backend/src/com/example/tarecruitment/profile/web/ApplicantAssetServlet.java`

本地数据路径、CSV 存储与演示数据：

- `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java`（**重叠标注：member2 主归属；member3 的职位/配置测试会依赖该数据路径能力**）
- `backend/src/com/example/tarecruitment/common/storage/CsvCodec.java`
- `backend/src/com/example/tarecruitment/demo/DemoAccountBootstrapListener.java`
- `backend/src/com/example/tarecruitment/demo/DemoDataSeeder.java`

DeepSeek 推荐搜索与 AI 搜索入口：

- `backend/src/com/example/tarecruitment/ai/client/DeepSeekAiConfig.java`
- `backend/src/com/example/tarecruitment/ai/client/DeepSeekApplicantSearchClient.java`
- `backend/src/com/example/tarecruitment/ai/client/DeepSeekTaJobSearchClient.java`
- `backend/src/com/example/tarecruitment/ai/client/DeepSeekChatClient.java`
- `backend/src/com/example/tarecruitment/ai/service/MoApplicantAiSearchService.java`
- `backend/src/com/example/tarecruitment/ai/service/TaJobAiSearchService.java`
- `backend/src/com/example/tarecruitment/ai/web/MoApplicantAiSearchServlet.java`
- `backend/src/com/example/tarecruitment/ai/web/TaJobAiSearchServlet.java`
- `frontend/webapp/WEB-INF/ai/deepseek.properties.template`

## 文件重叠与答辩归属说明

- `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java` 同时出现在 member3 文档中，因为职位、账号资料和配置测试都需要读取运行时数据目录。答辩时该数据路径基础设施由 member2 主讲；member3 只说明职位模块如何依赖它。

## 测试展示

运行命令：

```bash
./scripts/test/test-member2.sh
```

测试代码：

- `backend/test/Member2BackendTest.java`

测试覆盖点：

- `CsvCodec` 是否能正确处理逗号和引号，避免破坏 CSV 列。
- `StoragePaths` 是否通过 `TA_HIRING_DATA_DIR` 生成 applicants、resumes、photos 等运行时目录。
- `Applicant` 的 CSV 序列化/反序列化是否保留档案字段、技能、简历和照片路径。
- `ApplicantDao` 是否能创建档案，并拒绝重复用户档案和重复学号。
- `DeepSeekAiConfig` 在没有真实 API key 或配置为占位符时，是否安全降级为“未配置”。

答辩时可以这样解释：

`member2` 的测试重点是 TA 档案、CSV 数据层和 AI 推荐配置。脚本用临时数据目录模拟真实运行环境，证明档案可以安全写入 CSV，重复档案会被拦截；同时 AI key 没配置时不会生成假的推荐结果，而是走安全降级。

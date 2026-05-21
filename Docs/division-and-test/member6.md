# member6 分工与当前代码文件

[返回总览](Overview.md)

## 基本信息

| 项目       | 内容                                                                           |
| ---------- | ------------------------------------------------------------------------------ |
| Git author | `member6 <member6@edu.com>`                                                  |
| 标准提交数 | 41                                                                             |
| 分工概述   | 架构重组、通用配置、文档脚本整理、门户壳层、公共样式、Admin 页面与全站双语资源 |

## 分工概述

`member6` 是项目 leader 和整体结构负责人。主要负责目录整理、架构重组、通用配置、文档/脚本入口、门户统一壳层、全站通用样式、双语资源、Admin 页面重构，以及后期把后端旧大入口与旧包名迁移为当前轻量分层结构。业务模块文件虽然在架构重组提交中被批量移动或整理过，但答辩归属仍以各业务成员的功能分工为主。

## 当前对应代码文件

后端架构、路由、公共基础设施：

- `backend/src/com/example/tarecruitment/common/api/ApiRoutes.java`
- `backend/src/com/example/tarecruitment/common/service/ServiceResult.java`
- `backend/src/com/example/tarecruitment/common/storage/CsvCodec.java`
- `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java`
- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java`
- `backend/src/com/example/tarecruitment/common/util/Logger.java`
- `backend/src/com/example/tarecruitment/common/web/ApiResponses.java`
- `backend/src/com/example/tarecruitment/common/web/JsonResponseUtil.java`
- `backend/src/com/example/tarecruitment/common/web/PermissionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/SessionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/WebRequests.java`
- `backend/src/com/example/tarecruitment/auth/web/AccessPolicy.java`
- `backend/src/com/example/tarecruitment/package-info.java`
- `frontend/webapp/WEB-INF/web.xml`

后端业务模块架构重组说明：

`member6` 在 `4abe022 refactor: 统一后端模块架构与 API 路由` 中整理过后端业务模块的包结构、路由和分层边界。该提交覆盖了多个业务域文件，但这些文件的功能归属仍分别记录在 `member1` 到 `member4` 的文档中；`member6` 的测试展示应重点说明架构规则、公共入口和文档脚本，而不是重复认领所有业务实现文件。

门户壳层、公共样式、双语资源和首页：

- `frontend/webapp/index.jsp`
- `frontend/webapp/WEB-INF/jsp/fragments/portal-sidebar.jspf`
- `frontend/webapp/WEB-INF/jsp/fragments/portal-topbar.jspf`
- `frontend/webapp/css/common/components.css`
- `frontend/webapp/css/common/forms.css`
- `frontend/webapp/css/common/motion.css`
- `frontend/webapp/css/common/notifications.css`
- `frontend/webapp/css/common/tokens.css`
- `frontend/webapp/css/portal/portal-home.css`
- `frontend/webapp/css/portal/portal-shell.css`
- `frontend/webapp/js/common/i18n.js`
- `frontend/webapp/js/common/locale-bootstrap.js`
- `frontend/webapp/js/common/portal-i18n.js`
- `frontend/webapp/js/common/ta-recruitment.js`

Admin 页面整体重构：

- `frontend/webapp/jsp/admin/dashboard.jsp`
- `frontend/webapp/jsp/admin/invite.jsp`
- `frontend/webapp/jsp/admin/notifications.jsp`
- `frontend/webapp/css/admin/admin-dashboard.css`
- `frontend/webapp/css/admin/admin-invite-management.css`
- `frontend/webapp/js/admin/admin-dashboard.js`
- `frontend/webapp/js/admin/admin-invite-management.js`
- `frontend/webapp/js/admin/admin-notifications.js`

AI 配置模板：

- `frontend/webapp/WEB-INF/ai/deepseek.properties.template`

脚本、运行配置和技术文档：

- `scripts/config.example.bat`
- `scripts/config.example.sh`
- `scripts/dev.bat`
- `scripts/dev.sh`
- `scripts/javadocs.bat`
- `scripts/javadocs.sh`
- `README.md`
- `docs/deliverables/technical/index.md`
- `docs/deliverables/technical/api/servlet-api.md`
- `docs/deliverables/technical/architecture/data-architecture.md`
- `docs/deliverables/technical/architecture/security-architecture.md`
- `docs/deliverables/technical/architecture/system-architecture.md`
- `docs/deliverables/technical/deployment/deployment-guide.md`
- `docs/deliverables/technical/modules/admin-invite.md`
- `docs/deliverables/technical/modules/admin-workload.md`
- `docs/deliverables/technical/modules/ai-matching.md`
- `docs/deliverables/technical/modules/application-review.md`
- `docs/deliverables/technical/modules/authentication.md`
- `docs/deliverables/technical/modules/job-management.md`
- `docs/deliverables/technical/modules/ta-profile.md`

## 测试展示

运行命令：

```bash
./scripts/test/test-member6.sh
```

测试代码：

- `frontend/test/member6-architecture-test.js`

测试覆盖点：

- 负责的架构、门户壳层、公共样式、脚本和文档文件是否存在。
- 后端源码、前端页面、脚本和分工文档中是否还残留旧包名、旧 Servlet、带版本号的旧 API 前缀或旧根路径。
- `ApiRoutes.java` 中的后端 API 常量是否保持简洁 `/api/...` 路径。
- `frontend/webapp/js/common/ta-recruitment.js` 是否同步包含后端 `ApiRoutes` 的 API 路径。
- `package-info.java` 和 `docs/division-and-test/Overview.md` 是否反映当前轻量架构与成员文档入口。

答辩时可以这样解释：

`member6` 的测试重点不是重复测试每个业务功能，而是验证项目整体结构没有回退到旧架构。脚本会检查旧包名、旧接口、旧 Servlet、大量手写路径和已删除 AI 页面是否还残留，并确认后端 API 常量和前端公共路由保持同步。

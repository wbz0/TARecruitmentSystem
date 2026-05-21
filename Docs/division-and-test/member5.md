# member5 分工与当前代码文件

[返回总览](Overview.md)

## 基本信息

| 项目 | 内容 |
| --- | --- |
| Git author | `member5 <member5@edu.com>` |
| 标准提交数 | 34 |
| 分工概述 | 前端页面与交互，覆盖登录注册、TA/MO/Admin 页面、前端 API 路由统一 |

## 分工概述

`member5` 主要承担前端页面、交互和样式。范围覆盖登录注册、TA 档案、TA 职位列表/详情/申请状态、MO 发布与 dashboard 内的申请人审核、Admin 页面请求适配，以及后期统一页面 JS 对 `TARecruitment.routes` 和公共请求工具的调用方式。旧 MO 技能匹配页、旧 MO 独立申请人页和旧 Admin 注册说明页已下线，因此当前文件清单只保留仍存在的页面与脚本。

## 当前对应代码文件

认证、注册和管理员邀请前端：

- `frontend/webapp/login.jsp`
- `frontend/webapp/register.jsp`
- `frontend/webapp/admin-invite.jsp`
- `frontend/webapp/css/auth/login.css`
- `frontend/webapp/css/auth/register.css`
- `frontend/webapp/js/auth/login.js`
- `frontend/webapp/js/auth/register.js`
- `frontend/webapp/js/auth/admin-invite.js`

TA 页面、脚本和样式：

- `frontend/webapp/jsp/ta/dashboard.jsp`
- `frontend/webapp/jsp/ta/job-list.jsp`
- `frontend/webapp/jsp/ta/job-detail.jsp`
- `frontend/webapp/jsp/ta/application-status.jsp`
- `frontend/webapp/jsp/ta/application-detail.jsp`
- `frontend/webapp/jsp/ta/notifications.jsp`
- `frontend/webapp/css/ta/ta-dashboard.css`
- `frontend/webapp/css/ta/ta-job-list.css`
- `frontend/webapp/css/ta/ta-job-detail.css`
- `frontend/webapp/css/ta/ta-application-status.css`
- `frontend/webapp/css/ta/ta-application-detail.css`
- `frontend/webapp/js/ta/ta-dashboard.js`
- `frontend/webapp/js/ta/ta-job-list.js`
- `frontend/webapp/js/ta/ta-job-detail.js`
- `frontend/webapp/js/ta/ta-application-status.js`
- `frontend/webapp/js/ta/ta-application-detail.js`
- `frontend/webapp/js/ta/ta-notifications.js`

MO 页面、脚本和样式：

- `frontend/webapp/jsp/mo/dashboard.jsp`
- `frontend/webapp/jsp/mo/notifications.jsp`
- `frontend/webapp/css/mo/mo-dashboard.css`
- `frontend/webapp/js/mo/mo-dashboard.js`
- `frontend/webapp/js/mo/mo-notifications.js`

Admin 前端页面请求适配：

- `frontend/webapp/jsp/admin/dashboard.jsp`
- `frontend/webapp/jsp/admin/invite.jsp`
- `frontend/webapp/jsp/admin/notifications.jsp`
- `frontend/webapp/css/admin/admin-dashboard.css`
- `frontend/webapp/css/admin/admin-invite-management.css`
- `frontend/webapp/js/admin/admin-dashboard.js`
- `frontend/webapp/js/admin/admin-invite-management.js`
- `frontend/webapp/js/admin/admin-notifications.js`

前端公共 API 路由调用：

- `frontend/webapp/js/common/ta-recruitment.js`
- `frontend/webapp/WEB-INF/jsp/fragments/portal-sidebar.jspf`

## 文件重叠与答辩归属说明

member5 与 member1 到 member4 的当前代码文件没有直接重叠。member5 的答辩重点放在具体前端页面、页面交互、样式和页面 JS 的 API 调用方式上；公共门户壳层和全站架构类前端文件如果与 member6 文档重叠，应按 member6 的 leader/架构说明处理。

## 测试展示

运行命令：

```bash
./scripts/test/test-member5.sh
```

测试代码：

- `frontend/test/member5-frontend-test.js`

测试覆盖点：

- 所有 `frontend/webapp/js/**/*.js` 文件都通过 `node --check` 语法检查。
- 页面 JS 不直接手写 API 地址或旧根路径，必须通过 `TARecruitment.routes` 生成。
- 登录、注册、TA、MO、Admin 关键页面和共享 `TARecruitment.routes` 文件存在。
- 已下线的旧 MO 技能匹配页、旧 MO 独立申请人页和旧 Admin 注册说明页不再出现在当前前端目录。

答辩时可以这样解释：

`member5` 的测试重点是前端页面能否被浏览器正常解析，以及页面请求是否统一走公共路由工具。这样可以证明前端不是散落手写接口地址，而是通过 `TARecruitment.routes` 适配 `/groupproject` 这类部署路径。

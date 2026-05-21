### 整体分工

| 成员        | 文件                  | Git author                    | 标准提交数 | 分工概述                                                                                   |
| ----------- | --------------------- | ----------------------------- | ---------: | ------------------------------------------------------------------------------------------ |
| `member1` | [member1.md](member1.md) | `member1 <member1@edu.com>` |         18 | 后端基础能力、认证流程、接口响应与工具类、技能匹配早期实现、部分测试/统计补充              |
| `member2` | [member2.md](member2.md) | `member2 <member2@edu.com>` |         20 | TA 档案与文件上传、数据路径/初始化稳定性、AI 推荐搜索与匹配服务                            |
| `member3` | [member3.md](member3.md) | `member3 <member3@edu.com>` |         19 | 职位发布/查询/校验、工作量统计接口、账号资料同步、AI 配置模板                              |
| `member4` | [member4.md](member4.md) | `member4 <member4@edu.com>` |         23 | 申请流程、状态流转、TA 撤回、MO 选择、通知与邀请码业务、集成测试/用户手册早期工作          |
| `member5` | [member5.md](member5.md) | `member5 <member5@edu.com>` |         34 | 前端页面与交互，覆盖登录注册、TA/MO/Admin 页面、前端 API 路由统一                          |
| `member6` | [member6.md](member6.md) | `member6 <member6@edu.com>` |         41 | 项目 leader/架构重组、通用配置、文档脚本整理、门户壳层、公共样式、Admin 页面与全站双语资源 |

### 个人测试展示入口

每个成员答辩时只需要运行自己的脚本，不需要运行统一总测试。

重叠文件和答辩主归属见：[overlap-and-defense-ownership.md](overlap-and-defense-ownership.md)。

| 成员 | 测试命令 | 测试代码 |
| --- | --- | --- |
| `member1` | `./scripts/test/test-member1.sh` | `backend/test/Member1BackendTest.java` |
| `member2` | `./scripts/test/test-member2.sh` | `backend/test/Member2BackendTest.java` |
| `member3` | `./scripts/test/test-member3.sh` | `backend/test/Member3BackendTest.java` |
| `member4` | `./scripts/test/test-member4.sh` | `backend/test/Member4BackendTest.java` |
| `member5` | `./scripts/test/test-member5.sh` | `frontend/test/member5-frontend-test.js` |
| `member6` | `./scripts/test/test-member6.sh` | `frontend/test/member6-architecture-test.js` |

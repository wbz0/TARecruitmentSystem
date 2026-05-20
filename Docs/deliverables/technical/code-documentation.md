# 代码文档与 JavaDoc 使用指南

## 1. 文档关系

本项目有两类技术文档：

| 文档类型 | 位置 | 作用 |
|----------|------|------|
| 人工技术文档 | `docs/deliverables/technical/` | 解释系统架构、模块职责、API、部署和主要业务流程 |
| 自动 JavaDoc | `docs/deliverables/technical/javadocs/index.html` | 从 Java 源码注释自动生成类、方法、参数和包级说明 |

阅读代码时建议先看 `docs/deliverables/technical/index.md`，理解系统分层和模块边界；再生成 JavaDoc，查看具体类和 public 方法的职责。

## 2. 生成 JavaDoc

macOS / Linux：

```bash
./scripts/javadocs.sh
```

Windows：

```bat
scripts\javadocs.bat
```

脚本会读取本地 `scripts/config.sh` 或 `scripts/config.bat` 中的 `TOMCAT_HOME`，并使用：

```text
TOMCAT_HOME/lib/servlet-api.jar
```

作为 Jakarta Servlet 依赖。生成结果放在：

```text
docs/deliverables/technical/javadocs/index.html
```

JavaDoc 输出目录位于技术交付材料目录内。源码、public API、包结构或注释更新后，应重新运行脚本刷新该目录。

## 3. 代码阅读顺序

后端代码按领域包组织：

```text
com.example.tarecruitment
├── auth          # 登录、注册、会话和访问控制
├── profile       # 当前账号、TA 档案、头像和简历
├── job           # 职位发布、列表、详情、编辑和删除
├── application   # 申请创建、列表、详情和状态流转
├── ai            # 职位推荐、申请人推荐和详情 AI 分析
├── admin         # 管理员邀请和工作量统计
├── notification  # 通知列表、发布和删除
├── common        # API 路由、统一响应、CSV、搜索和 Web 工具
└── demo          # 演示账号和演示数据初始化
```

每个主要业务领域优先按这个顺序阅读：

```text
web -> service -> dao/model/mapper/validator
```

| 层次 | 职责 |
|------|------|
| `web` | Servlet HTTP 入口，只解析请求、读取当前用户、调用 service、写统一响应 |
| `service` | 业务流程、权限相关业务校验、状态流转和跨 DAO 协作 |
| `dao` | CSV 读写和查询，不依赖 request、session 或 response |
| `model` | 数据结构、枚举、CSV 序列化和反序列化字段 |
| `mapper` | 请求参数到领域参数、model 到响应 payload 的转换 |
| `validator` | 必填、长度、格式、危险输入和文件约束校验 |

## 4. 维护规则

- 修改 public API、service 行为、CSV 字段或权限策略时，同步更新 JavaDoc 和对应模块文档。
- 新增包时补充 `package-info.java`，让自动 JavaDoc 有包级入口。
- 新增 Servlet 时确认路径常量来自 `common/api/ApiRoutes.java`。
- 前端示例应通过 `TARecruitment.routes` 和 `TARecruitment.api.request` 表达，不手写旧根路径接口。
- JavaDoc 中只解释非显而易见的行为；普通 getter/setter 不需要机械补注释。

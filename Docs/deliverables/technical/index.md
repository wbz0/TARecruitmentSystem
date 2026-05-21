# TA Hiring System - Technical Documentation

## 概述

本文档是 TA Hiring System 的技术文档集合，提供系统的整体架构设计和各功能模块的详细技术实现说明。

**项目背景**：这是一个面向计算机科学硕士课程的 TA (Teaching Assistant) 招聘管理系统，允许学生申请 TA 职位，模块负责人 (MO) 发布职位并审核申请，系统还集成了 AI 推荐搜索功能。

---

## 文档目录

### 代码文档

| 文档 | 说明 |
|------|------|
| [code-documentation.md](./code-documentation.md) | JavaDoc 自动生成、阅读顺序和代码文档维护规则 |

### 架构设计

| 文档 | 说明 |
|------|------|
| [system-architecture.md](./architecture/system-architecture.md) | 系统整体架构设计 |
| [data-architecture.md](./architecture/data-architecture.md) | 数据架构与存储设计 |
| [security-architecture.md](./architecture/security-architecture.md) | 安全架构与权限设计 |

### 功能模块技术文档

| 文档 | 说明 |
|------|------|
| [authentication.md](./modules/authentication.md) | 认证与权限模块 |
| [ta-profile.md](./modules/ta-profile.md) | TA 档案管理模块 |
| [job-management.md](./modules/job-management.md) | 职位管理模块 |
| [application-review.md](./modules/application-review.md) | 申请审核模块 |
| [ai-matching.md](./modules/ai-matching.md) | AI 推荐模块 |
| [admin-workload.md](./modules/admin-workload.md) | 管理员工作量统计模块 |
| [admin-invite.md](./modules/admin-invite.md) | 管理员邀请模块 |

### API 与部署

| 文档 | 说明 |
|------|------|
| [servlet-api.md](./api/servlet-api.md) | Servlet API 接口文档 |
| [deployment-guide.md](./deployment/deployment-guide.md) | 部署运维指南 |

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Java 17+, Jakarta Servlet 6 |
| 容器 | Apache Tomcat 11.x |
| 前端 | JSP, HTML5, CSS3, Vanilla JavaScript |
| 构建/运行 | 脚本直跑 |
| 持久化 | CSV 文件存储 |
| AI | DeepSeek 兼容 API (可选) |
| 多语言 | 中英文双语 |

---

## 项目结构

```
backend/src/com/example/tarecruitment/
├── common/             # JSON、请求、CSV、存储路径等公共基础设施
├── auth/               # 登录、注册、会话和权限过滤
├── profile/            # TA 档案、简历和头像访问
├── job/                # 职位模型、DAO 和 HTTP 接口
├── application/        # 申请记录与申请流程
├── ai/                 # AI 推荐客户端、服务和接口
├── admin/              # 管理员邀请和工作量统计
├── notification/       # 通知模型、DAO 和接口
└── demo/               # 演示账号和演示数据初始化

frontend/webapp/
├── index.jsp           # 门户首页
├── login.jsp           # 登录页
├── register.jsp        # 注册页
├── jsp/
│   ├── ta/             # TA 角色页面 (5个)
│   ├── mo/             # MO 角色页面 (3个)
│   └── admin/          # Admin 角色页面 (2个)
├── css/                # 样式文件
└── js/                 # 前端脚本

docs/deliverables/technical/
├── architecture/       # 架构设计文档
├── modules/            # 功能模块文档
├── api/                # API 文档
├── code-documentation.md  # 代码文档与 JavaDoc 指南
├── javadocs/           # 自动生成的 JavaDoc HTML
└── deployment/         # 部署文档
```

自动生成的 JavaDoc 作为技术交付材料的一部分，通过脚本生成到：

```text
docs/deliverables/technical/javadocs/index.html
```

---

## 角色说明

| 角色 | 说明 | 主要功能 |
|------|------|----------|
| **TA** | Teaching Assistant 申请人 | 创建档案、浏览职位、提交申请、查看申请状态 |
| **MO** | Module Owner 模块负责人 | 发布职位、管理申请、AI 推荐和申请分析 |
| **ADMIN** | 系统管理员 | TA 工作量统计、管理短邀请码 |

---

## 核心数据流

```
[TA]  --注册--> [User] --创建档案--> [Applicant]
                           |
[MO]  --发布职位--> [Job] <--申请-- [Application] --审核--> [TA]
                           |
[AI]  --推荐/分析--> [Candidate Insight] --> [MO 筛选]
```

---

## 文档更新记录

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-05-15 | 1.1.1 | 调整 JavaDoc 输出到技术交付目录 |
| 2026-05-14 | 1.1.0 | 增加 JavaDoc 自动生成说明和代码文档入口 |
| 2026-03-28 | 1.0.0 | 初始技术文档 |

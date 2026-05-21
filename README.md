# TA Hiring System

基于 `Servlet + JSP + 原生 JavaScript + CSS + CSV` 的教学场景 TA 招聘系统。项目面向三类角色：

- **TA**：维护个人档案、上传头像/简历、浏览职位、提交申请、查看申请进度和职位 AI 推荐。
- **MO**：发布与维护职位、查看候选人、处理申请、使用 AI 推荐辅助筛选。
- **Admin**：查看 TA 工作量统计、管理通知、生成管理员注册邀请码。

## 技术栈

| 层次      | 技术                                     |
| --------- | ---------------------------------------- |
| 后端      | Java 17+、Jakarta Servlet                |
| 容器      | Apache Tomcat 10.1+ 或 11.x              |
| 前端      | JSP、HTML、CSS、原生 JavaScript          |
| 构建/运行 | `scripts/dev.sh` / `scripts/dev.bat` |
| 持久化    | CSV 文件 + 本地上传文件目录              |
| AI        | AI 推荐搜索                              |
| 多语言    | 中英文双语前端文案                       |

本项目不依赖 Maven、Gradle、Spring、数据库或前端构建工具。开发脚本会直接用 `javac` 编译 `backend/src/**/*.java`，再把 `frontend/webapp` 部署到 Tomcat。

## 目录结构

| 路径                           | 作用                                                          |
| ------------------------------ | ------------------------------------------------------------- |
| `backend/src/`               | 后端 Java 源码                                                |
| `frontend/webapp/`           | JSP 页面、CSS、JavaScript、`WEB-INF/web.xml` 和 AI 配置模板 |
| `scripts/dev.sh`             | macOS / Linux 一键编译、部署、启动                            |
| `scripts/dev.bat`            | Windows 一键编译、部署、启动                                  |
| `scripts/config.example.sh`  | macOS / Linux 本地配置模板                                    |
| `scripts/config.example.bat` | Windows 本地配置模板                                          |
| `scripts/javadocs.sh`        | macOS / Linux JavaDoc 生成脚本                                |
| `scripts/javadocs.bat`       | Windows JavaDoc 生成脚本                                      |
| `docs/deliverables/technical/` | 架构、API、部署和模块说明                                   |
| `docs/deliverables/`         | 课程交付资料                                                  |

## 环境要求

| 工具    | 要求                  | 说明                                     |
| ------- | --------------------- | ---------------------------------------- |
| JDK     | `17+`               | 需要能直接使用 `javac`                 |
| Tomcat  | `10.1+` 或 `11.x` | 项目使用 Jakarta Servlet 6 API           |
| Shell   | Bash 或 Windows CMD   | 运行本地脚本                             |
| Node.js | 可选                  | 只用于 `node --check` 检查前端 JS 语法 |

## 快速启动

### macOS / Linux

```bash
cp scripts/config.example.sh scripts/config.sh
chmod +x scripts/dev.sh
```

编辑 `scripts/config.sh`：

```bash
export CATALINA_HOME="/path/to/apache-tomcat-11.0.7"
export TOMCAT_HOME="${CATALINA_HOME}"
export APP_NAME="groupproject"
export TA_HIRING_DATA_DIR="${CATALINA_HOME}/data"
```

启动：

```bash
./scripts/dev.sh
```

### Windows

```bat
copy scripts\config.example.bat scripts\config.bat
```

编辑 `scripts\config.bat`：

```bat
set CATALINA_HOME=D:\path\to\apache-tomcat-11.0.7
set TOMCAT_HOME=%CATALINA_HOME%
set APP_NAME=groupproject
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data
```

启动：

```bat
scripts\dev.bat
```

脚本会自动执行：

```text
清理 build -> 编译 backend/src -> 复制 frontend/webapp -> 部署到 Tomcat webapps -> 启动 Tomcat
```

## 访问地址

默认 `APP_NAME=groupproject`，启动后访问：

| 页面             | URL                                                 |
| ---------------- | --------------------------------------------------- |
| 门户首页         | http://localhost:8080/groupproject/                 |
| 登录页           | http://localhost:8080/groupproject/login.jsp        |
| TA/MO 注册页     | http://localhost:8080/groupproject/register.jsp     |
| Admin 邀请注册页 | http://localhost:8080/groupproject/admin-invite.jsp |

角色页面位于：

| 角色  | 主要页面                                                                                                                      |
| ----- | ----------------------------------------------------------------------------------------------------------------------------- |
| TA    | `/jsp/ta/dashboard.jsp`、`/jsp/ta/job-list.jsp`、`/jsp/ta/application-status.jsp`、`/jsp/ta/notifications.jsp`        |
| MO    | `/jsp/mo/dashboard.jsp`、`/jsp/mo/notifications.jsp` |
| Admin | `/jsp/admin/dashboard.jsp`、`/jsp/admin/invite.jsp`、`/jsp/admin/notifications.jsp`                                     |

## 演示账号

应用启动时会自动补齐固定演示账号和示例职位/申请数据，不会清空已有 CSV。

| Role  | Username           | Password     |
| ----- | ------------------ | ------------ |
| TA    | `ta_demo`        | `Pass1234` |
| TA    | `ta_demo_mia`    | `Pass1234` |
| TA    | `ta_demo_noah`   | `Pass1234` |
| TA    | `ta_demo_olivia` | `Pass1234` |
| TA    | `ta_demo_liam`   | `Pass1234` |
| MO    | `mo_demo`        | `Pass1234` |
| MO    | `mo_demo_alice`  | `Pass1234` |
| MO    | `mo_demo_brian`  | `Pass1234` |
| Admin | `admin_demo`     | `Pass1234` |

## 数据和日志

运行时数据必须由 `TA_HIRING_DATA_DIR` 指定，代码不会把运行数据写进仓库。用户、职位、申请、通知、头像和简历等演示数据都会保存在这个目录下。

后端日志文件位于项目根目录：

```text
logs/app.log
```

## 页面功能

| 角色  | 前端可见功能                                                                                                        |
| ----- | ------------------------------------------------------------------------------------------------------------------- |
| TA    | 登录/注册、维护账号和 TA 档案、上传头像/简历、浏览职位、提交申请、查看申请状态、查看职位推荐 |
| MO    | 登录/注册、发布/编辑/删除职位、查看候选人、处理申请、查看申请人推荐                                   |
| Admin | 登录、查看 TA 工作量统计、查看通知、查看/刷新当前 8 位管理员邀请码                                                  |

Admin 新账号通过 `/admin-invite.jsp` 输入 8 位邀请码创建；当前邀请码在 Admin 的 `/jsp/admin/invite.jsp` 页面查看或刷新。

## AI 配置

AI 配置只影响前端页面里的推荐功能。模板位于 `frontend/webapp/WEB-INF/ai/`，真实密钥文件使用 `*.local.properties`，已被 `.gitignore` 忽略。

### 推荐搜索

用于 MO 申请人推荐和 TA 职位推荐。

```bash
cp frontend/webapp/WEB-INF/ai/deepseek.properties.template \
   frontend/webapp/WEB-INF/ai/deepseek.local.properties
```

如果没有配置 key 或服务不可用，推荐类 AI 会提示暂不可用，不生成本地假推荐。

### 常见问题

- **提示找不到 Tomcat**：检查 `CATALINA_HOME` / `TOMCAT_HOME` 是否指向真实 Tomcat 根目录，并确认 `lib/servlet-api.jar` 存在。
- **提示数据目录未配置**：检查 `TA_HIRING_DATA_DIR` 是否已写入 `scripts/config.sh` 或 `scripts/config.bat`。
- **脚本显示 All Done 但页面打不开**：检查 8080/8005 是否被其他 Tomcat 或服务占用，并查看 Tomcat 日志。
- **页面能打开但无数据**：确认本次运行使用的是同一个 `TA_HIRING_DATA_DIR`，并确认启动时演示数据初始化没有报错。
- **Admin 注册失败**：管理员账号必须使用 `/admin-invite.jsp` 的 8 位短邀请码；当前邀请码可在 Admin 的 `/jsp/admin/invite.jsp` 查看或刷新。
- **AI 推荐不可用**：检查对应 `*.local.properties` 是否配置真实 key。

## 更多文档

- 技术文档入口：`docs/deliverables/technical/index.md`
- 部署说明：`docs/deliverables/technical/deployment/deployment-guide.md`

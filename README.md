# Software Engineering Group Project

基于 Tomcat + Servlet + JSP 的 Web 应用

## 环境要求

- JDK 17+
- Apache Tomcat 11.x

## 快速开始

### 1. 首次配置

```cmd
cd scripts
copy config.example.bat config.bat
notepad config.bat
```

修改 `config.bat`，填入你的 Tomcat 路径：

```bat
set CATALINA_HOME=C:\apache-tomcat-11.0.7
```

### 2. 运行项目

```cmd
cd scripts
build.bat
deploy.bat
startup.bat
```

### 3. 访问

- 首页: http://localhost:8080/groupproject/
- Servlet: http://localhost:8080/groupproject/hello
- JSP: http://localhost:8080/groupproject/jsp/welcome.jsp

## 日常开发工作流 (修改代码后)

当你修改了任何 `.java` 源码或 `.jsp/.html` 前端文件后，**无需重启 Tomcat**，只需要在 `scripts/` 目录下重新执行编译和部署命令：

```cmd
cd scripts
build.bat
deploy.bat
```

*(这会重新编译最新的 Java 类并把新文件覆盖到 Tomcat 的运行包中，刷新浏览器即可看到变化)*

## 项目架构与目录说明

为了完全契合《EBU6304 敏捷开发计划》中 **前端/后端分别协作** 的要求，也为了避免将来合代码时产生冲突，本项目的代码被物理隔离为两个顶级目录：

```text
carnegie_software_engineering/
├── backend/               # 👨‍💻 后端开发工作区 (成员1-4)
│   └── src/               # Java 源代码存放处 
│
├── frontend/              # 🎨 前端开发工作区 (成员5-6)
│   └── webapp/            # JSP、HTML、CSS、JS 静态资源与 web.xml
│
├── scripts/               # ⚙️ 构建与部署脚本
│   ├── build.bat          # 核心构建脚本 (将 backend 和 frontend 组装)
│   ├── deploy.bat         # 核心部署脚本
│   ├── startup.bat        # Tomcat 启动脚本
│   └── config.example.bat # 配置文件模板
│
├── data/                  # 💾 纯文本数据存储 (JSON/txt)
└── build/                 # 📦 脚本自动生成的打包产物 (被 Git 忽略)
```

## 常见问题

- **端口被占用**: 请修改您本机的 `Tomcat/conf/server.xml`，将 `<Connector port="8080" />` 改为其他端口。
- **命令行乱码**: 请确保您的终端代码页格式正确，或在执行构建脚本时确保所有的本地 `.java` 文件是以 `UTF-8` 无 BOM 形式保存的。

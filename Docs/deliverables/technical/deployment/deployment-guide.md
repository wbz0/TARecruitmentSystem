# 部署指南

## 1. 环境要求

### 1.1 软件要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| Java | 17+ | JDK 17 或更高版本 |
| Bash / Windows CMD | 系统自带 | 运行脚本 |
| Apache Tomcat | 11.x | Servlet 容器 |
| Git | 任意版本 | 代码管理 (可选) |

### 1.2 硬件要求

- CPU: 1 核+
- 内存: 2GB+
- 磁盘: 1GB+ 可用空间

---

## 2. 开发环境搭建

### 2.1 克隆代码

```bash
git clone <repository-url>
cd carnegie_software_engineering
```

### 2.2 导入 IDE

**IntelliJ IDEA**:
1. File → Open → 选择项目根目录
2. 配置 Project SDK 为 Java 17+
3. 将 `backend/src` 标记为源码目录

**Eclipse**:
1. File → Import → General → Existing Projects into Workspace
2. 选择项目根目录
3. 完成导入

### 2.3 配置 Tomcat

1. 下载 Apache Tomcat 11.x
2. 在 IDE 中添加 Tomcat Server
3. 配置 Context Path 为 `/groupproject`

---

## 3. 构建项目

### 3.1 macOS / Linux 一键运行

```bash
cp scripts/config.example.sh scripts/config.sh
# 编辑 scripts/config.sh，确认 TOMCAT_HOME 与 TA_HIRING_DATA_DIR
./scripts/dev.sh
```

脚本会自动发现 `backend/src/**/*.java`，通过 `javac @sources` 编译到 `build/WEB-INF/classes`，再把 `frontend/webapp` 与编译结果部署到 Tomcat 的 `webapps/groupproject`。

### 3.2 Windows 一键运行

```batch
copy scripts\config.example.bat scripts\config.bat
REM 编辑 scripts\config.bat，确认 TOMCAT_HOME 与 TA_HIRING_DATA_DIR
scripts\dev.bat
```

---

## 4. 部署到 Tomcat

### 4.1 脚本部署

`scripts/dev.sh` / `scripts/dev.bat` 会清理旧的 `webapps/groupproject` 目录、复制 JSP/CSS/JS/WEB-INF 资源、编译 Java 类，并启动 Tomcat。

### 4.2 context 配置 (可选)

创建 `${CATALINA_HOME}/conf/Catalina/localhost/groupproject.xml`:
```xml
<Context path="/groupproject" docBase="/path/to/webapp" reloadable="true">
    <Parameter name="ta.hiring.data.dir" value="/custom/data/path" />
</Context>
```

---

## 5. 配置

### 5.1 数据目录配置

数据目录用于存储 CSV 文件和上传的文件。**必须**在 `scripts/config.bat` 中配置 `TA_HIRING_DATA_DIR` 环境变量：

```batch
REM ==== DATA DIRECTORY ====
REM 设置数据目录路径（默认使用 Tomcat 下的 data 目录）
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data
```

**重要**：必须配置 `TA_HIRING_DATA_DIR`，否则应用启动时会抛出异常。

### 5.2 AI 服务配置

复制 AI 配置模板并填写实际值:

```bash
cp frontend/webapp/WEB-INF/ai/deepseek.properties.template \
   frontend/webapp/WEB-INF/ai/deepseek.local.properties
```

编辑配置文件:
```properties
deepseek.api.key=your-actual-api-key
deepseek.base-url=https://api.deepseek.com
deepseek.model=deepseek-v4-flash
deepseek.timeout-ms=8000
```

### 5.3 Session 配置

在 `web.xml` 中配置 (可选):

```xml
<session-config>
    <session-timeout>30</session-timeout>  <!-- 30 分钟 -->
</session-config>
```

---

## 6. 验证部署

### 6.1 启动 Tomcat

```bash
# Linux/Mac
${CATALINA_HOME}/bin/startup.sh

# Windows
%CATALINA_HOME%\bin\startup.bat
```

### 6.2 访问应用

打开浏览器访问:
```
http://localhost:8080/groupproject/
```

### 6.3 默认登录账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| TA | ta_demo | Pass1234 |
| MO | mo_demo | Pass1234 |
| ADMIN | admin_demo | Pass1234 |

---

## 7. 目录结构

部署后，应用目录结构如下:

```
${CATALINA_HOME}/webapps/groupproject/
├── index.jsp
├── login.jsp
├── register.jsp
├── admin-invite.jsp
├── WEB-INF/
│   ├── web.xml
│   └── ai/
│       └── deepseek.local.properties  # AI 推荐配置
├── jsp/
│   ├── ta/
│   ├── mo/
│   └── admin/
├── css/
├── js/
└── ...

${TA_HIRING_DATA_DIR}/
├── users/
│   ├── users_ta.csv
│   ├── users_mo.csv
│   └── users_admin.csv
├── jobs/
│   └── jobs.csv
├── applicants/
│   └── applicants.csv
├── applications/
│   └── applications.csv
├── invites/
│   └── invites.csv
├── resumes/       # 上传的简历文件
└── photos/        # 上传的头像文件
```

---

## 8. 日志

### 8.1 日志位置

- Tomcat 日志: `${CATALINA_HOME}/logs/`
  - `catalina.out` - 应用日志
  - `localhost.log` - 访问日志

### 8.2 日志级别

应用使用 `System.out.println` 输出日志，生产环境建议配置 Log4j/SLF4J。

---

## 9. 故障排除

### 9.1 应用无法启动

**检查**:
1. Tomcat 日志是否有错误
2. 端口 8080 是否被占用
3. Java 版本是否正确 (17+)

**解决方案**:
```bash
# 检查 Java 版本
java -version

# 检查端口占用
netstat -an | grep 8080  # Linux
netstat -ano | findstr 8080  # Windows
```

### 9.2 数据无法保存

**检查**:
1. 数据目录是否存在并有写入权限
2. CSV 文件是否被锁定

**解决方案**:
```bash
# 创建数据目录
mkdir -p ${DATA_DIR}/users
mkdir -p ${DATA_DIR}/jobs
mkdir -p ${DATA_DIR}/applicants
mkdir -p ${DATA_DIR}/applications

# 设置权限
chmod 755 ${DATA_DIR}
chmod 644 ${DATA_DIR}/*.csv
```

### 9.3 AI 功能不工作

**检查**:
1. API Key 是否正确配置
2. `deepseek.base-url` 是否能访问
3. AI 服务网络是否可达

**解决方案**:
```bash
# 检查配置文件
cat frontend/webapp/WEB-INF/ai/deepseek.local.properties

# 测试 API 连接
curl -X POST "https://api.deepseek.com/chat/completions" \
  -H "Authorization: Bearer YOUR-API-KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"deepseek-v4-flash","messages":[{"role":"user","content":"hello"}]}'
```

### 9.4 文件上传失败

**检查**:
1. 上传目录是否存在
2. 文件大小是否超过限制
3. 文件类型是否允许

**解决方案**:
```bash
# 创建上传目录
mkdir -p ${DATA_DIR}/resumes
mkdir -p ${DATA_DIR}/photos

# 设置权限
chmod 755 ${DATA_DIR}/resumes
chmod 755 ${DATA_DIR}/photos
```

---

## 10. 生产环境建议

### 10.1 安全建议

1. **启用 HTTPS**
   ```xml
   <!-- server.xml -->
   <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
              maxThreads="150" scheme="https" secure="true"
              keystoreFile="/path/to/keystore" keystorePass="password" />
   ```

2. **配置强密码策略**: 当前使用 SHA-256，生产环境建议使用 BCrypt

3. **限制文件上传**: 当前通过前端检查，生产环境应配置服务器级别限制

4. **定期备份数据**: 备份 CSV 文件和上传目录

### 10.2 性能建议

1. **启用 Gzip 压缩**
   ```xml
   <Connector port="8080" compression="on" compressionMinSize="2048"
              noCompressionUserAgents="gozilla,tomcat" compressableMimeType="text/html,text/xml,text/plain,text/css,application/javascript" />
   ```

2. **数据库迁移**: 当前使用 CSV 存储，生产环境建议迁移到 MySQL/PostgreSQL

3. **缓存**: 当前使用内存缓存，可考虑使用 Redis 分布式缓存

### 10.3 监控建议

1. 配置 Log4j/SLF4J 日志框架
2. 集成 APM 工具 (如 SkyWalking, Pinpoint)
3. 监控 JVM 内存和 GC

---

## 11. 卸载

### 11.1 停止 Tomcat

```bash
${CATALINA_HOME}/bin/shutdown.sh  # Linux/Mac
%CATALINA_HOME%\bin\shutdown.bat  # Windows
```

### 11.2 删除应用

```bash
# 删除 webapp
rm -rf ${CATALINA_HOME}/webapps/groupproject

# 删除数据 (如确定不需要)
rm -rf ${DATA_DIR}
```

---

## 12. 联系方式

如有问题，请查看:
- 项目 README: `README.md`
- Git 提交记录: `docs/git-records/commit-records.md`
- 技术文档: `docs/deliverables/technical/index.md`

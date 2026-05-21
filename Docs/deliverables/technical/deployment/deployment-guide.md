# Deployment Guide

## 1. Environment Requirements

### 1.1 Software Requirements

| Component | Version Requirement | Description |
|-----------|-------------------|-------------|
| Java | 17+ | JDK 17 or higher |
| Bash / Windows CMD | System built-in | Run scripts |
| Apache Tomcat | 11.x | Servlet container |
| Git | Any version | Code management (optional) |

### 1.2 Hardware Requirements

- CPU: 1 core+
- Memory: 2GB+
- Disk: 1GB+ available space

---

## 2. Development Environment Setup

### 2.1 Clone Code

```bash
git clone <repository-url>
cd carnegie_software_engineering
```

### 2.2 Import to IDE

**IntelliJ IDEA**:
1. File → Open → Select project root directory
2. Configure Project SDK to Java 17+
3. Mark `backend/src` as source directory

**Eclipse**:
1. File → Import → General → Existing Projects into Workspace
2. Select project root directory
3. Complete import

### 2.3 Configure Tomcat

1. Download Apache Tomcat 11.x
2. Add Tomcat Server in IDE
3. Configure Context Path to `/groupproject`

---

## 3. Build Project

### 3.1 macOS / Linux One-click Run

```bash
cp scripts/config.example.sh scripts/config.sh
# Edit scripts/config.sh, confirm TOMCAT_HOME and TA_HIRING_DATA_DIR
./scripts/dev.sh
```

The script automatically discovers `backend/src/**/*.java`, compiles to `build/WEB-INF/classes` via `javac @sources`, then deploys `frontend/webapp` with compiled results to Tomcat's `webapps/groupproject`.

### 3.2 Windows One-click Run

```batch
copy scripts\config.example.bat scripts\config.bat
REM Edit scripts\config.bat, confirm TOMCAT_HOME and TA_HIRING_DATA_DIR
scripts\dev.bat
```

---

## 4. Deploy to Tomcat

### 4.1 Script Deployment

`scripts/dev.sh` / `scripts/dev.bat` will clean old `webapps/groupproject` directory, copy JSP/CSS/JS/WEB-INF resources, compile Java classes, and start Tomcat.

### 4.2 Context Configuration (Optional)

Create `${CATALINA_HOME}/conf/Catalina/localhost/groupproject.xml`:
```xml
<Context path="/groupproject" docBase="/path/to/webapp" reloadable="true">
    <Parameter name="ta.hiring.data.dir" value="/custom/data/path" />
</Context>
```

---

## 5. Configuration

### 5.1 Data Directory Configuration

Data directory is used to store CSV files and uploaded files. **Must** configure `TA_HIRING_DATA_DIR` environment variable in `scripts/config.bat`:

```batch
REM ==== DATA DIRECTORY ====
REM Set data directory path (default uses data directory under Tomcat)
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data
```

**Important**: `TA_HIRING_DATA_DIR` must be configured, otherwise the application will throw an exception on startup.

### 5.2 AI Service Configuration

Copy AI configuration template and fill in actual values:

```bash
cp frontend/webapp/WEB-INF/ai/deepseek.properties.template \
   frontend/webapp/WEB-INF/ai/deepseek.local.properties
```

Edit configuration file:
```properties
deepseek.api.key=your-actual-api-key
deepseek.base-url=https://api.deepseek.com
deepseek.model=deepseek-v4-flash
deepseek.timeout-ms=8000
```

### 5.3 Session Configuration

Configure in `web.xml` (optional):

```xml
<session-config>
    <session-timeout>30</session-timeout>  <!-- 30 minutes -->
</session-config>
```

---

## 6. Verify Deployment

### 6.1 Start Tomcat

```bash
# Linux/Mac
${CATALINA_HOME}/bin/startup.sh

# Windows
%CATALINA_HOME%\bin\startup.bat
```

### 6.2 Access Application

Open browser to access:
```
http://localhost:8080/groupproject/
```

### 6.3 Default Login Accounts

| Role | Username | Password |
|------|----------|----------|
| TA | ta_demo | Pass1234 |
| MO | mo_demo | Pass1234 |
| ADMIN | admin_demo | Pass1234 |

---

## 7. Directory Structure

After deployment, application directory structure is as follows:

```
${CATALINA_HOME}/webapps/groupproject/
├── index.jsp
├── login.jsp
├── register.jsp
├── admin-invite.jsp
├── WEB-INF/
│   ├── web.xml
│   └── ai/
│       └── deepseek.local.properties  # AI recommendation configuration
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
├── resumes/       # Uploaded resume files
└── photos/        # Uploaded avatar files
```

---

## 8. Logging

### 8.1 Log Locations

- Tomcat logs: `${CATALINA_HOME}/logs/`
  - `catalina.out` - Application logs
  - `localhost.log` - Access logs

### 8.2 Log Level

Application uses `System.out.println` for log output; production environment should configure Log4j/SLF4J.

---

## 9. Troubleshooting

### 9.1 Application Fails to Start

**Check**:
1. Check Tomcat logs for errors
2. Check if port 8080 is occupied
3. Check if Java version is correct (17+)

**Solution**:
```bash
# Check Java version
java -version

# Check port occupancy
netstat -an | grep 8080  # Linux
netstat -ano | findstr 8080  # Windows
```

### 9.2 Data Cannot Be Saved

**Check**:
1. Check if data directory exists and has write permission
2. Check if CSV files are locked

**Solution**:
```bash
# Create data directory
mkdir -p ${DATA_DIR}/users
mkdir -p ${DATA_DIR}/jobs
mkdir -p ${DATA_DIR}/applicants
mkdir -p ${DATA_DIR}/applications

# Set permissions
chmod 755 ${DATA_DIR}
chmod 644 ${DATA_DIR}/*.csv
```

### 9.3 AI Function Not Working

**Check**:
1. Check if API Key is configured correctly
2. Check if `deepseek.base-url` is accessible
3. Check if AI service network is reachable

**Solution**:
```bash
# Check configuration file
cat frontend/webapp/WEB-INF/ai/deepseek.local.properties

# Test API connection
curl -X POST "https://api.deepseek.com/chat/completions" \
  -H "Authorization: Bearer YOUR-API-KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"deepseek-v4-flash","messages":[{"role":"user","content":"hello"}]}'
```

### 9.4 File Upload Failed

**Check**:
1. Check if upload directory exists
2. Check if file size exceeds limit
3. Check if file type is allowed

**Solution**:
```bash
# Create upload directories
mkdir -p ${DATA_DIR}/resumes
mkdir -p ${DATA_DIR}/photos

# Set permissions
chmod 755 ${DATA_DIR}/resumes
chmod 755 ${DATA_DIR}/photos
```

---

## 10. Production Environment Recommendations

### 10.1 Security Recommendations

1. **Enable HTTPS**
   ```xml
   <!-- server.xml -->
   <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
              maxThreads="150" scheme="https" secure="true"
              keystoreFile="/path/to/keystore" keystorePass="password" />
   ```

2. **Configure strong password policy**: Currently using SHA-256; BCrypt recommended for production

3. **Limit file upload**: Currently checked on frontend; server-level limits should be configured in production

4. **Regular data backup**: Backup CSV files and upload directories

### 10.2 Performance Recommendations

1. **Enable Gzip compression**
   ```xml
   <Connector port="8080" compression="on" compressionMinSize="2048"
              noCompressionUserAgents="gozilla,tomcat" compressableMimeType="text/html,text/xml,text/plain,text/css,application/javascript" />
   ```

2. **Database migration**: Currently using CSV storage; migration to MySQL/PostgreSQL recommended for production

3. **Caching**: Currently using memory cache; Redis distributed cache can be considered

### 10.3 Monitoring Recommendations

1. Configure Log4j/SLF4J logging framework
2. Integrate APM tools (e.g., SkyWalking, Pinpoint)
3. Monitor JVM memory and GC

---

## 11. Uninstallation

### 11.1 Stop Tomcat

```bash
${CATALINA_HOME}/bin/shutdown.sh  # Linux/Mac
%CATALINA_HOME%\bin\shutdown.bat  # Windows
```

### 11.2 Delete Application

```bash
# Delete webapp
rm -rf ${CATALINA_HOME}/webapps/groupproject

# Delete data (if confirmed not needed)
rm -rf ${DATA_DIR}
```

---

## 12. Contact

For questions, please check:
- Project README: `README.md`
- Git commit records: `docs/git-records/commit-records.md`
- Technical documentation: `docs/deliverables/technical/index.md`
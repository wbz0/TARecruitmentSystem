# 数据架构设计

## 1. 存储策略

### 1.1 CSV 文件存储

系统采用 **CSV 文件**作为主要数据存储方式，适用于小型项目和数据量有限的场景。

**优点**：
- 无需额外数据库软件
- 数据可读性好，便于调试
- 部署简单

**缺点**：
- 并发写入可能产生冲突
- 不适合大数据量场景
- 缺少事务支持

### 1.2 存储目录结构

```
${TA_HIRING_DATA_DIR}/           # 必须在 config.bat 中配置
├── users/                       # 用户数据
│   ├── users_ta.csv            # TA 用户
│   ├── users_mo.csv           # MO 用户
│   └── users_admin.csv        # 管理员用户
├── jobs/
│   └── jobs.csv               # 职位数据
├── applicants/
│   └── applicants.csv         # TA 申请人档案
├── applications/
│   └── applications.csv       # 申请记录
├── invites/
│   └── invites.csv            # 邀请记录
├── resumes/                    # 简历文件
└── photos/                     # 头像文件
```

### 1.3 数据目录配置

通过 `StoragePaths` 工具类统一管理：

```java
StoragePaths.getDataDir();       // 根数据目录
StoragePaths.getUsersDir();     // 用户目录
StoragePaths.getJobsDir();      // 职位目录
// ...
```

**配置方式**：在 `scripts/config.bat` 中设置 `TA_HIRING_DATA_DIR` 环境变量：

```batch
REM ==== DATA DIRECTORY ====
REM 设置数据目录路径
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data
```

**重要**：必须配置 `TA_HIRING_DATA_DIR`，否则应用启动时会抛出异常。

---

## 2. 数据模型

### 2.1 实体关系图

```
┌──────────────┐     1:N      ┌──────────────┐
│     User     │─────────────▶│  Applicant   │
│  (userId)    │   "关联"      │ (applicantId) │
└──────────────┘              └──────────────┘
       │
       │ 1:N
       ▼
┌──────────────┐     N:1      ┌──────────────┐
│  Application │─────────────▶│     Job      │
│ (applicationId)             │   (jobId)    │
└──────────────┘              └──────────────┘
       │
       │ N:1 (moId)
       ▼
┌──────────────┐
│     User     │ (作为 MO)
└──────────────┘
       │
       │ 1:N
       ▼
┌──────────────┐
│   Job        │
└──────────────┘

Admin 短邀请码不再保存邀请实体，只在 `invites/` 目录保存服务端密钥和轮换窗口状态。
```

### 2.2 User 实体

**文件**: `users/users_{role}.csv`

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String (UUID) | 用户唯一标识 |
| username | String | 用户名 (唯一) |
| password | String (SHA-256) | 密码哈希 |
| email | String | 邮箱 (唯一) |
| role | Enum | TA / MO / ADMIN |
| createdAt | DateTime | 创建时间 |
| lastLoginAt | DateTime | 最后登录时间 |

**CSV 示例**：
```csv
userId,username,password,email,role,createdAt,lastLoginAt
a1b2c3d4,ta_demo,5e884898...da3c,ta_demo@local.test,TA,2026-03-28T10:00:00,2026-03-28T14:30:00
```

### 2.3 Job 实体

**文件**: `jobs/jobs.csv`

| 字段 | 类型 | 说明 |
|------|------|------|
| jobId | String (UUID) | 职位唯一标识 |
| moId | String | 发布职位的 MO 用户ID |
| moName | String | MO 姓名 |
| title | String | 职位标题 |
| courseCode | String | 课程代码 |
| courseName | String | 课程名称 |
| description | String | 职位描述 |
| requiredSkills | String (分号分隔) | 必需技能列表 |
| positions | Integer | 职位数量 |
| workload | String | 工作量 (如 "10小时/周") |
| salary | String | 薪资 |
| deadline | DateTime | 申请截止日期 |
| status | Enum | OPEN / CLOSED / FILLED |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

**状态有效性规则**：
```java
public Status getEffectiveStatus(LocalDateTime now) {
    if (status == Status.FILLED) return FILLED;
    if (status == Status.CLOSED) return CLOSED;
    if (deadline != null && deadline.isBefore(now)) return CLOSED;  // 过期自动关闭
    return OPEN;
}
```

### 2.4 Applicant 实体

**文件**: `applicants/applicants.csv`

| 字段 | 类型 | 说明 |
|------|------|------|
| applicantId | String (UUID) | 档案唯一标识 |
| userId | String | 关联的用户ID |
| fullName | String | 姓名 |
| studentId | String | 学号 |
| department | String | 院系 |
| program | String | 项目 (本科/硕士/博士) |
| gpa | String | GPA |
| skills | String (分号分隔) | 技能列表 |
| resumePath | String | 简历文件路径 |
| photoPath | String | 头像文件路径 |
| phone | String | 电话 |
| address | String | 地址 |
| experience | String | 相关经验 |
| motivation | String | 申请动机 |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

### 2.5 Application 实体

**文件**: `applications/applications.csv`

| 字段 | 类型 | 说明 |
|------|------|------|
| applicationId | String (UUID) | 申请唯一标识 |
| jobId | String | 申请的职位ID |
| applicantId | String | 申请人ID |
| applicantName | String | 申请人姓名 |
| applicantEmail | String | 申请人邮箱 |
| jobTitle | String | 职位标题 (冗余) |
| courseCode | String | 课程代码 (冗余) |
| moId | String | 发布职位的 MO ID |
| moName | String | MO 姓名 (冗余) |
| status | Enum | PENDING / ACCEPTED / REJECTED / WITHDRAWN |
| coverLetter | String | 求职信 |
| appliedAt | DateTime | 申请时间 |
| updatedAt | DateTime | 更新时间 |
| reviewedAt | DateTime | 审核时间 |
| progressStage | Enum | UNDER_REVIEW / INTERVIEW_SCHEDULED / COMPLETED |
| reviewStartedAt | DateTime | 材料审核开始时间 |
| interviewScheduledAt | DateTime | 面试安排时间 |
| finalDecisionAt | DateTime | 最终决定时间 |

**进度阶段说明**：

```
UNDER_REVIEW → INTERVIEW_SCHEDULED → COMPLETED
   (审核中)       (已安排面试)         (完成)
      ↓
 WITHDRAWN
```

### 2.6 Admin 短邀请码状态

**目录**: `invites/`

| 文件 | 说明 |
|------|------|
| `invite_secret.bin` | 生成短邀请码的服务端密钥 |
| `rotation_offset.txt` | 管理员主动刷新后的轮换偏移 |
| `forced_window_start.txt` | 手动刷新后新窗口的开始时间 |

---

## 3. CSV 格式规范

### 3.1 通用规则

- **分隔符**: 逗号 `,`
- **引号字符**: 双引号 `"`
- **换行符**: `\n` (LF)
- **编码**: UTF-8
- **表头**: 每文件第一行为表头行
- **空行**: 忽略
- **转义规则**:
  - 字段包含逗号、引号或换行时，使用双引号包裹
  - 内部双引号转义为两个双引号 `""`

### 3.2 格式解析

```java
// CSV 解析示例 (来自 User.fromCsv)
public static User fromCsv(String csvLine) {
    // 使用正则分割，支持带引号的字段
    String[] parts = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    // ...
}

// 转义处理
private static String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
}
```

---

## 4. 文件操作规范

### 4.1 写入安全

为防止并发写入导致数据损坏，采用以下策略：

```java
// 1. 写入临时文件
Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempPath))) {
    // 写入数据
}

// 2. 原子移动
Files.move(tempPath, targetPath,
           StandardCopyOption.REPLACE_EXISTING,
           StandardCopyOption.ATOMIC_MOVE);

// 3. 重试机制 (如果原子移动失败)
for (int attempt = 0; attempt < FILE_WRITE_RETRY_COUNT; attempt++) {
    try {
        Files.move(tempPath, targetPath, ...);
        return;
    } catch (IOException e) {
        // 重试
    }
}
```

### 4.2 读取策略

- 每次读取都加载完整文件到内存
- 不使用文件锁
- 适合读多写少场景

---

## 5. 二进制文件存储

### 5.1 简历存储

- **目录**: `{DATA_DIR}/resumes/`
- **命名**: `{userId}_resume_{timestamp}.pdf`
- **上传限制**: 通过前端检查文件类型和大小

### 5.2 头像存储

- **目录**: `{DATA_DIR}/photos/`
- **命名**: `{userId}_photo_{timestamp}.{ext}`
- **格式支持**: JPG, PNG, GIF
- **大小限制**: 2MB (通过前端检查)

### 5.3 文件访问

二进制文件通过 Servlet 路径访问：
```
/file/resume/{filename}
/file/photo/{filename}
```

---

## 6. 数据初始化

### 6.1 启动时初始化

通过 `DemoAccountBootstrapListener` 在应用启动时执行：

```java
@WebListener
public class DemoAccountBootstrapListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 确保演示账号存在
        UserDao.getInstance().ensureDefaultDemoAccounts();
        // 初始化示例数据
        DemoDataSeeder.seedIfEmpty();
    }
}
```

### 6.2 默认演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| TA | ta_demo | Pass1234 |
| MO | mo_demo | Pass1234 |
| ADMIN | admin_demo | Pass1234 |

---

## 7. 数据备份与恢复

### 7.1 手动备份

```bash
# 备份数据目录
cp -r ${DATA_DIR} ${DATA_DIR}_backup_$(date +%Y%m%d)

# 或打包
tar -czf data_backup_$(date +%Y%m%d).tar.gz ${DATA_DIR}
```

### 7.2 恢复

```bash
# 停止 Tomcat
# 恢复数据
cp -r ${DATA_DIR}_backup_*/. ${DATA_DIR}/
# 启动 Tomcat
```

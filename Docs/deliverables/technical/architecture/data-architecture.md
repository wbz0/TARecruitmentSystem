# Data Architecture Design

## 1. Storage Strategy

### 1.1 CSV File Storage

The system uses **CSV files** as the primary data storage method, suitable for small projects and scenarios with limited data volume.

**Advantages**:
- No additional database software required
- Good data readability, easy to debug
- Simple deployment

**Disadvantages**:
- Concurrent writes may cause conflicts
- Not suitable for large data volume scenarios
- No transaction support

### 1.2 Storage Directory Structure

```
${TA_HIRING_DATA_DIR}/           # Must be configured in config.bat
├── users/                       # User data
│   ├── users_ta.csv            # TA users
│   ├── users_mo.csv           # MO users
│   └── users_admin.csv        # Admin users
├── jobs/
│   └── jobs.csv               # Job data
├── applicants/
│   └── applicants.csv         # TA applicant profiles
├── applications/
│   └── applications.csv       # Application records
├── invites/
│   └── invites.csv            # Invitation records
├── resumes/                    # Resume files
└── photos/                     # Avatar files
```

### 1.3 Data Directory Configuration

Managed uniformly through `StoragePaths` utility class:

```java
StoragePaths.getDataDir();       // Root data directory
StoragePaths.getUsersDir();     // Users directory
StoragePaths.getJobsDir();      // Jobs directory
// ...
```

**Configuration**: Set `TA_HIRING_DATA_DIR` environment variable in `scripts/config.bat`:

```batch
REM ==== DATA DIRECTORY ====
REM Set data directory path
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data
```

**Important**: `TA_HIRING_DATA_DIR` must be configured, otherwise the application will throw an exception on startup.

---

## 2. Data Model

### 2.1 Entity Relationship Diagram

```
┌──────────────┐     1:N      ┌──────────────┐
│     User     │─────────────▶│  Applicant   │
│  (userId)    │   "related"  │ (applicantId) │
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
│     User     │ (as MO)
└──────────────┘
       │
       │ 1:N
       ▼
┌──────────────┐
│   Job        │
└──────────────┘

Admin short invitation codes no longer save invitation entities; only server secret and rotation window state are saved in the `invites/` directory.
```

### 2.2 User Entity

**File**: `users/users_{role}.csv`

| Field | Type | Description |
|------|------|-------------|
| userId | String (UUID) | User unique identifier |
| username | String | Username (unique) |
| password | String (SHA-256) | Password hash |
| email | String | Email (unique) |
| role | Enum | TA / MO / ADMIN |
| createdAt | DateTime | Creation time |
| lastLoginAt | DateTime | Last login time |

**CSV Example**:
```csv
userId,username,password,email,role,createdAt,lastLoginAt
a1b2c3d4,ta_demo,5e884898...da3c,ta_demo@local.test,TA,2026-03-28T10:00:00,2026-03-28T14:30:00
```

### 2.3 Job Entity

**File**: `jobs/jobs.csv`

| Field | Type | Description |
|------|------|-------------|
| jobId | String (UUID) | Job unique identifier |
| moId | String | MO user ID who posted the job |
| moName | String | MO name |
| title | String | Job title |
| courseCode | String | Course code |
| courseName | String | Course name |
| description | String | Job description |
| requiredSkills | String (semicolon-separated) | Required skills list |
| positions | Integer | Number of positions |
| workload | String | Workload (e.g., "10 hours/week") |
| salary | String | Salary |
| deadline | DateTime | Application deadline |
| status | Enum | OPEN / CLOSED / FILLED |
| createdAt | DateTime | Creation time |
| updatedAt | DateTime | Update time |

**Status validity rules**:
```java
public Status getEffectiveStatus(LocalDateTime now) {
    if (status == Status.FILLED) return FILLED;
    if (status == Status.CLOSED) return CLOSED;
    if (deadline != null && deadline.isBefore(now)) return CLOSED;  // Auto-close on expiration
    return OPEN;
}
```

### 2.4 Applicant Entity

**File**: `applicants/applicants.csv`

| Field | Type | Description |
|------|------|-------------|
| applicantId | String (UUID) | Profile unique identifier |
| userId | String | Related user ID |
| fullName | String | Full name |
| studentId | String | Student ID |
| department | String | Department |
| program | String | Program (Bachelor/Master/PhD) |
| gpa | String | GPA |
| skills | String (semicolon-separated) | Skills list |
| resumePath | String | Resume file path |
| photoPath | String | Avatar file path |
| phone | String | Phone |
| address | String | Address |
| experience | String | Related experience |
| motivation | String | Application motivation |
| createdAt | DateTime | Creation time |
| updatedAt | DateTime | Update time |

### 2.5 Application Entity

**File**: `applications/applications.csv`

| Field | Type | Description |
|------|------|-------------|
| applicationId | String (UUID) | Application unique identifier |
| jobId | String | Applied job ID |
| applicantId | String | Applicant ID |
| applicantName | String | Applicant name |
| applicantEmail | String | Applicant email |
| jobTitle | String | Job title (redundant) |
| courseCode | String | Course code (redundant) |
| moId | String | MO ID who posted the job |
| moName | String | MO name (redundant) |
| status | Enum | PENDING / ACCEPTED / REJECTED / WITHDRAWN |
| coverLetter | String | Cover letter |
| appliedAt | DateTime | Application time |
| updatedAt | DateTime | Update time |
| reviewedAt | DateTime | Review time |
| progressStage | Enum | UNDER_REVIEW / INTERVIEW_SCHEDULED / COMPLETED |
| reviewStartedAt | DateTime | Material review start time |
| interviewScheduledAt | DateTime | Interview scheduling time |
| finalDecisionAt | DateTime | Final decision time |

**Progress stage explanation**:

```
UNDER_REVIEW → INTERVIEW_SCHEDULED → COMPLETED
   (Under Review)     (Interview Scheduled)     (Completed)
      ↓
 WITHDRAWN
```

### 2.6 Admin Short Invitation Code Status

**Directory**: `invites/`

| File | Description |
|------|-------------|
| `invite_secret.bin` | Server secret for generating short invitation codes |
| `rotation_offset.txt` | Rotation offset after admin manually refreshes |
| `forced_window_start.txt` | New window start time after manual refresh |

---

## 3. CSV Format Specification

### 3.1 General Rules

- **Delimiter**: Comma `,`
- **Quote character**: Double quote `"`
- **Line ending**: `\n` (LF)
- **Encoding**: UTF-8
- **Header**: First row of each file is the header row
- **Empty lines**: Ignored
- **Escape rules**:
  - When field contains comma, quote, or newline, wrap with double quotes
  - Internal double quotes are escaped as two double quotes `""`

### 3.2 Format Parsing

```java
// CSV parsing example (from User.fromCsv)
public static User fromCsv(String csvLine) {
    // Regex split, supports quoted fields
    String[] parts = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    // ...
}

// Escape handling
private static String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
}
```

---

## 4. File Operation Specification

### 4.1 Write Safety

To prevent data corruption from concurrent writes, the following strategy is used:

```java
// 1. Write to temporary file
Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempPath))) {
    // Write data
}

// 2. Atomic move
Files.move(tempPath, targetPath,
           StandardCopyOption.REPLACE_EXISTING,
           StandardCopyOption.ATOMIC_MOVE);

// 3. Retry mechanism (if atomic move fails)
for (int attempt = 0; attempt < FILE_WRITE_RETRY_COUNT; attempt++) {
    try {
        Files.move(tempPath, targetPath, ...);
        return;
    } catch (IOException e) {
        // Retry
    }
}
```

### 4.2 Read Strategy

- Each read loads the entire file into memory
- No file locking used
- Suitable for read-heavy, write-light scenarios

---

## 5. Binary File Storage

### 5.1 Resume Storage

- **Directory**: `{DATA_DIR}/resumes/`
- **Naming**: `{userId}_resume_{timestamp}.pdf`
- **Upload limit**: File type and size checked on frontend

### 5.2 Avatar Storage

- **Directory**: `{DATA_DIR}/photos/`
- **Naming**: `{userId}_photo_{timestamp}.{ext}`
- **Format support**: JPG, PNG, GIF
- **Size limit**: 2MB (checked on frontend)

### 5.3 File Access

Binary files are accessed via Servlet paths:
```
/file/resume/{filename}
/file/photo/{filename}
```

---

## 6. Data Initialization

### 6.1 Initialization on Startup

Executed via `DemoAccountBootstrapListener` when application starts:

```java
@WebListener
public class DemoAccountBootstrapListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Ensure demo accounts exist
        UserDao.getInstance().ensureDefaultDemoAccounts();
        // Initialize sample data
        DemoDataSeeder.seedIfEmpty();
    }
}
```

### 6.2 Default Demo Accounts

| Role | Username | Password |
|------|----------|----------|
| TA | ta_demo | Pass1234 |
| MO | mo_demo | Pass1234 |
| ADMIN | admin_demo | Pass1234 |

---

## 7. Data Backup and Recovery

### 7.1 Manual Backup

```bash
# Backup data directory
cp -r ${DATA_DIR} ${DATA_DIR}_backup_$(date +%Y%m%d)

# Or package
tar -czf data_backup_$(date +%Y%m%d).tar.gz ${DATA_DIR}
```

### 7.2 Recovery

```bash
# Stop Tomcat
# Restore data
cp -r ${DATA_DIR}_backup*/. ${DATA_DIR}/
# Start Tomcat
```
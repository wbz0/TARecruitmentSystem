# 申请审核模块技术文档

## 1. 模块概述

申请审核模块处理 TA 对职位的申请流程，包括申请提交、进度跟踪和 MO 审核。

**核心组件**：
- `Application` - 申请实体
- `ApplicationDao` - 数据访问层
- `ApplicationServlet` - 申请操作处理
- 前端页面: `jsp/ta/application-status.jsp`, `jsp/ta/application-detail.jsp`, `jsp/mo/dashboard.jsp` 申请人子视图

---

## 2. 实体设计

### 2.1 Application

**路径**: `backend/src/com/example/tarecruitment/application/model/Application.java`

```java
public class Application {
    private String applicationId;        // UUID
    private String jobId;                 // 申请的职位 ID
    private String applicantId;           // 申请人 ID
    private String applicantName;        // 申请人姓名
    private String applicantEmail;       // 申请人邮箱
    private String jobTitle;             // 职位标题 (冗余)
    private String courseCode;            // 课程代码 (冗余)
    private String moId;                 // 发布职位的 MO ID
    private String moName;               // MO 姓名 (冗余)
    private Status status;               // PENDING / ACCEPTED / REJECTED / WITHDRAWN
    private String coverLetter;          // 求职信
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private ProgressStage progressStage; // UNDER_REVIEW / INTERVIEW_SCHEDULED / COMPLETED
    private LocalDateTime reviewStartedAt;
    private LocalDateTime interviewScheduledAt;
    private LocalDateTime finalDecisionAt;

    public enum Status { PENDING, ACCEPTED, REJECTED, WITHDRAWN }
    public enum ProgressStage { UNDER_REVIEW, INTERVIEW_SCHEDULED, COMPLETED }
}
```

### 2.2 进度阶段

```
UNDER_REVIEW ──▶ INTERVIEW_SCHEDULED ──▶ COMPLETED
   (审核中)             (已安排面试)           (完成)
      │
      └──▶ WITHDRAWN
```

### 2.3 CSV 格式

**文件**: `data/applications/applications.csv`

**表头**:
```csv
applicationId,jobId,applicantId,applicantName,applicantEmail,jobTitle,courseCode,moId,moName,status,coverLetter,appliedAt,updatedAt,reviewedAt,progressStage,reviewStartedAt,interviewScheduledAt,finalDecisionAt
```

---

## 3. 数据访问层

### 3.1 ApplicationDao

**路径**: `backend/src/com/example/tarecruitment/application/dao/ApplicationDao.java`

**单例模式**: 是

**核心方法**:

| 方法 | 说明 |
|------|------|
| `findById(String applicationId)` | 根据 ID 查找 |
| `findByJobId(String jobId)` | 查找职位的所有申请 |
| `findByApplicantId(String applicantId)` | 查找申请人的所有申请 |
| `findByMoId(String moId)` | 查找 MO 收到的所有申请 |
| `findByStatus(Status status)` | 根据状态筛选 |
| `findByProgressStage(ProgressStage stage)` | 根据阶段筛选 |
| `existsByJobIdAndApplicantId(String jobId, String applicantId)` | 检查是否已申请 (防重复) |
| `save(Application application)` | 保存申请 |
| `updateStatus(String applicationId, Status status)` | 更新状态 |
| `updateProgressStage(String applicationId, ProgressStage stage)` | 更新阶段 |

**防重复申请**:
```java
public boolean existsByJobIdAndApplicantId(String jobId, String applicantId) {
    return readAllApplications().stream()
        .anyMatch(app ->
            app.getJobId().equals(jobId) &&
            app.getApplicantId().equals(applicantId) &&
            app.getStatus() != Status.WITHDRAWN
        );
}
```

---

## 4. Servlet 实现

### 4.1 ApplicationServlet

**路径**: `backend/src/com/example/tarecruitment/application/web/ApplicationServlet.java`

**端点**: `/api/applications`

**支持的操作**:

| 操作 | 方法 | 说明 |
|------|------|------|
| 提交申请 | POST `/api/applications` | TA 提交新申请 |
| 取消申请 | POST `/api/applications/{applicationId}/transition` | TA 撤回申请 |
| 接受申请 | POST `/api/applications/{applicationId}/transition` | MO 接受申请 |
| 拒绝申请 | POST `/api/applications/{applicationId}/transition` | MO 拒绝申请 |

#### POST /api/applications (提交申请)

**请求参数**:
| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| jobId | String | 是 | 职位 ID |
| coverLetter | String | 否 | 求职信 |

**权限**: 仅 TA

**处理流程**:
```
POST /api/applications
    │
    ▼
验证 TA 登录状态
    │
    ▼
检查 TA 是否有申请人档案
    │
    ▼
检查职位是否存在
    │
    ▼
检查职位是否开放 (effectiveStatus == OPEN)
    │
    ▼
检查是否重复申请
    │
    ▼
创建 Application 对象
    │
    ▼
保存到 CSV
    │
    ▼
返回成功
```

#### POST /api/applications/{applicationId}/transition (状态流转)

**请求参数**:
| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| action | String | 是 | `accept`、`reject`、`withdraw` |

**规则**:
- TA 可以撤回自己的 PENDING 申请。
- MO 可以接受或拒绝自己职位下的 PENDING 申请。
- Admin 保持当前系统既有查看能力，不新增审核业务能力。

---

## 5. 前端页面

### 5.1 TA 我的申请页

**路径**: `frontend/webapp/jsp/ta/application-status.jsp`

**功能**:
- 申请列表展示 (状态、职位、时间)
- 申请详情查看
- 取消申请按钮 (仅 PENDING 状态)

```javascript
// 加载我的申请
async function loadMyApplications() {
    return TARecruitment.api.request(TARecruitment.routes.applications.list(), {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    });
}

// 取消申请
async function cancelApplication(applicationId) {
    return TARecruitment.api.request(TARecruitment.routes.applications.transition(applicationId), {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'action=withdraw'
    });
}
```

### 5.2 TA 申请详情页

**路径**: `frontend/webapp/jsp/ta/application-detail.jsp`

**功能**:
- 申请详情展示
- 进度时间线
- 审核结果展示

### 5.3 MO dashboard 申请人子视图

**路径**: `frontend/webapp/jsp/mo/dashboard.jsp`

**功能**:
- 收到的申请列表
- 查看申请人档案
- 查看简历
- 搜索和 AI 推荐候选人
- 审核操作 (接受/拒绝)

---

## 6. 权限控制

| 操作 | TA | MO | ADMIN |
|------|----|----|-------|
| 提交申请 | ✓ (本人) | ✗ | ✗ |
| 查看我的申请 | ✓ (本人) | ✗ | ✗ |
| 取消申请 | ✓ (本人, PENDING) | ✗ | ✗ |
| 查看收到的申请 | ✗ | ✓ (本人职位) | ✗ |
| 审核申请 | ✗ | ✓ (本人职位) | ✗ |
| 推进进度 | ✗ | ✓ (本人职位) | ✗ |

---

## 7. 业务流程

### 7.1 TA 申请流程

```
TA 浏览职位列表
    │
    ▼
点击职位详情
    │
    ▼
点击 "申请此职位"
    │
    ▼
填写求职信 (可选)
    │
    ▼
提交 → POST /api/applications
    │
    ▼
查看我的申请状态
```

### 7.2 MO 审核流程

```
MO 查看收到的申请
    │
    ▼
点击申请查看详情
    │
    ▼
选择候选人
    │
    ▼
推进进度阶段
    │
    ▼
做出最终决定 (接受/拒绝)
    │
    ▼
TA 收到通知 (可选)
```

---

## 8. 状态与阶段联动

### 8.1 阶段到状态的映射

| 阶段 | 可能状态 |
|------|----------|
| UNDER_REVIEW | PENDING |
| INTERVIEW_SCHEDULED | PENDING |
| COMPLETED | ACCEPTED / REJECTED / WITHDRAWN |

### 8.2 状态变更规则

```java
// 审核决定时
if ("ACCEPTED".equals(status)) {
    application.setStatus(Status.ACCEPTED);
    application.setProgressStage(ProgressStage.COMPLETED);
    application.setFinalDecisionAt(LocalDateTime.now());
} else if ("REJECTED".equals(status)) {
    application.setStatus(Status.REJECTED);
    application.setProgressStage(ProgressStage.COMPLETED);
    application.setFinalDecisionAt(LocalDateTime.now());
}
```

---

## 9. 错误处理

| 错误场景 | 响应码 | 消息 |
|----------|--------|------|
| 职位不存在 | 400 | "Job not found" |
| 职位已关闭 | 400 | "Job is no longer accepting applications" |
| 重复申请 | 400 | "You have already applied for this job" |
| 无申请人档案 | 400 | "Please complete your profile first" |
| 权限不足 | 403 | "Access denied" |
| 申请不存在 | 404 | "Application not found" |
| 状态不可变更 | 400 | "Cannot change status at this stage" |

---

## 10. 测试用例

**验证方式**: 使用 `scripts/dev.sh` 启动本地环境后，按以下场景手工验证；提交前可运行 `./scripts/javadocs.sh` 和前端 `node --check`。

**测试场景**:
1. TA 提交申请 → 申请正确保存
2. TA 重复申请 → 返回错误
3. TA 取消 PENDING 申请 → 成功
4. TA 取消非 PENDING 申请 → 返回错误
5. MO 查看收到的申请 → 正确列表
6. MO 接受申请 → 状态更新
7. MO 拒绝申请 → 状态更新
8. 非 MO 审核申请 → 返回 403

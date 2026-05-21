# TA Profile Management Module Technical Documentation

## 1. Module Overview

TA profile management module allows TA applicants to create and manage personal profiles, including basic information, skills, resume, and avatar.

**Core Components**:
- `Applicant` - Applicant profile entity
- `ApplicantDao` - Data access layer
- `ApplicantProfileServlet` - Current TA profile JSON entry
- `ApplicantAssetServlet` - Current TA avatar, resume, draft resume entry
- `ApplicantProfileService` - Profile creation, update, and sync flow
- `ProfileAssetService` - Avatar, resume, draft resume file processing
- `ApplicantProfileResponseMapper` / `ApplicantProfileValidator` - Response assembly and form validation
- Frontend page: `jsp/ta/dashboard.jsp`

---

## 2. Entity Design

### 2.1 Applicant

**Path**: `backend/src/com/example/tarecruitment/profile/model/Applicant.java`

```java
public class Applicant {
    private String applicantId;       // UUID
    private String userId;            // Related User ID
    private String fullName;          // Full name
    private String studentId;         // Student ID
    private String department;        // Department
    private String program;           // Bachelor/Master/PhD
    private String gpa;              // GPA
    private List<String> skills;     // Skills list (semicolon-separated)
    private String resumePath;        // Resume file path
    private String photoPath;         // Avatar file path
    private String phone;             // Phone
    private String address;           // Address
    private String experience;        // Related experience
    private String motivation;        // Application motivation
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2.2 CSV Format

**File**: `data/applicants/applicants.csv`

**Header**:
```csv
applicantId,userId,fullName,studentId,department,program,gpa,skills,resumePath,photoPath,phone,address,experience,motivation,createdAt,updatedAt
```

**Example**:
```csv
uuid-123,user-456,John Doe,2020123456,Computer Science,Master,3.8,Java;Python;Machine Learning,,photo.jpg,1234567890,London,2 years TA experience,Want to help students
```

---

## 3. Data Access Layer

### 3.1 ApplicantDao

**Path**: `backend/src/com/example/tarecruitment/profile/dao/ApplicantDao.java`

**Singleton**: Yes

**Core Methods**:

| Method | Description |
|------|------|
| `findById(String applicantId)` | Find by ID |
| `findByUserId(String userId)` | Find by user ID (association query) |
| `findAll()` | Get all profiles |
| `save(Applicant applicant)` | Save profile (new or update) |
| `create(Applicant applicant)` | Create profile |
| `update(Applicant applicant)` | Update profile |
| `delete(String applicantId)` | Delete profile |

**Association query implementation**:
```java
public Optional<Applicant> findByUserId(String userId) {
    return readAllApplicants().stream()
        .filter(a -> a.getUserId().equals(userId))
        .findFirst();
}

public Optional<Applicant> findByUserIdOrCreate(String userId) {
    Optional<Applicant> existing = findByUserId(userId);
    if (existing.isPresent()) {
        return existing;
    }
    // Does not auto-create, returns empty
    return Optional.empty();
}
```

---

## 4. Web and Service Layer Implementation

### 4.1 ApplicantProfileServlet

**Path**: `backend/src/com/example/tarecruitment/profile/web/ApplicantProfileServlet.java`

**Endpoint**: `/api/me/applicant-profile`

**Supported Methods**:
- `GET` - Get current user profile
- `POST` - Create profile
- `PUT` - Update profile

#### GET Handling

```
GET /api/me/applicant-profile
    │
    ▼
Get current logged-in user
    │
    ▼
Call ApplicantProfileService.get(...)
    │
    ├── Profile exists → ApplicantProfileResponseMapper assembles JSON
    │
    └── Profile does not exist → Returns 404 and draft resume status
```

**Response format** (JSON):
```json
{
    "applicantId": "uuid-123",
    "userId": "user-456",
    "fullName": "John Doe",
    "studentId": "2020123456",
    "department": "Computer Science",
    "program": "Master",
    "gpa": "3.8",
    "skills": ["Java", "Python", "Machine Learning"],
    "resumePath": "/file/resume/resume.pdf",
    "photoPath": "/file/photo/photo.jpg",
    "phone": "1234567890",
    "address": "London",
    "experience": "2 years TA experience",
    "motivation": "Want to help students"
}
```

#### POST/PUT Handling

**Request Parameters**:
| Parameter | Type | Required | Description |
|------|------|------|------|
| fullName | String | Yes | Full name |
| studentId | String | Yes | Student ID |
| department | String | Yes | Department |
| program | String | Yes | Program |
| gpa | String | No | GPA |
| skills | String | No | Skills (comma-separated) |
| phone | String | No | Phone |
| address | String | No | Address |
| experience | String | No | Experience |
| motivation | String | No | Motivation |

**Processing Flow**:
```
POST /api/me/applicant-profile
    │
    ▼
ApplicantProfileRequestMapper reads parameters
    │
    ▼
ApplicantProfileValidator validates fields
    │
    ▼
ApplicantProfileService saves profile and syncs account/application name
    │
    ▼
Returns unified JSON response
```

---

## 5. Avatar and Resume Upload

### 5.1 File Storage

**Avatar directory**: `{DATA_DIR}/photos/`
**Resume directory**: `{DATA_DIR}/resumes/`

**Filename generation**:
```java
String extension = FilenameUtils.getExtension(originalFilename);
String newFilename = userId + "_photo_" + System.currentTimeMillis() + "." + extension;
String newFilename = userId + "_resume_" + System.currentTimeMillis() + "." + extension;
```

### 5.2 ApplicantAssetServlet

**Path**: `backend/src/com/example/tarecruitment/profile/web/ApplicantAssetServlet.java`

**Endpoints**:
- `GET /api/me/applicant-profile/photo`
- `GET /api/me/applicant-profile/resume`
- `POST /api/me/applicant-profile/resume-draft`
- `DELETE /api/me/applicant-profile/resume-draft`

**Implementation boundary**:
- Servlet only checks current user, dispatches resource path, and writes file response.
- `ProfileAssetService` is responsible for file location, saving, copying draft, and deleting old files.
- `ProfileAssetValidator` is responsible for file size, Content-Type, and extension validation.

**Security checks**:
- Verify user is logged in
- Verify file belongs to current user or corresponding application

### 5.3 Application-Related Material Access

MO/TA/Admin access applicant materials through application:

- `GET /api/applications/{applicationId}/applicant`
- `GET /api/applications/{applicationId}/applicant/resume`
- `GET /api/applications/{applicationId}/applicant/photo`

### 5.4 File Type Validation

```java
// Avatar validation
private boolean isValidImage(String filename) {
    String ext = FilenameUtils.getExtension(filename).toLowerCase();
    return Arrays.asList("jpg", "jpeg", "png", "gif").contains(ext);
}

// Resume validation
private boolean isValidResume(String filename) {
    String ext = FilenameUtils.getExtension(filename).toLowerCase();
    return "pdf".equals(ext);
}
```

---

## 6. Frontend Pages

### 6.1 dashboard.jsp

**Path**: `frontend/webapp/jsp/ta/dashboard.jsp`

**Functional areas**:
1. **Profile information display/edit**
   - Basic information form
   - Skills tags
2. **Resume upload**
   - PDF file upload
   - Upload status display
3. **Avatar upload**
   - Image preview
   - Cropping function (optional)

### 6.2 Frontend Interaction

```javascript
// Save profile
async function saveProfile(formData) {
    return TARecruitment.api.request(TARecruitment.routes.me.applicantProfile(), {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: formData
    });
}

// Upload avatar
async function uploadPhoto(file) {
    const formData = new FormData();
    formData.append('photo', file);

    return TARecruitment.api.request(TARecruitment.routes.me.applicantPhoto(), {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: formData
    });
}
```

---

## 7. Permission Control

| Operation | TA | MO | ADMIN |
|------|----|----|-------|
| View own profile | ✓ | ✓ | ✓ |
| Edit own profile | ✓ | ✓ | ✓ |
| View others' profiles | ✗ | ✗ | ✗ |
| Upload resume | ✓ | ✗ | ✗ |
| Upload avatar | ✓ | ✗ | ✗ |

---

## 8. Error Handling

| Error Scenario | Response Code | Message |
|----------|--------|------|
| Profile does not exist | 404 | "Applicant not found" |
| Upload file too large | 400 | "File too large" |
| File type not supported | 400 | "Invalid file type" |
| Insufficient permission | 403 | "Access denied" |
| Server error | 500 | "Internal server error" |

---

## 9. Test Cases

**Validation method**: After starting local environment with `scripts/dev.sh`, manually verify according to the following scenarios; before committing, you can run `./scripts/javadocs.sh` and frontend `node --check`.

**Test scenarios**:
1. TA creates profile → Profile saved correctly
2. TA updates profile → Update takes effect
3. TA uploads avatar → File saved correctly
4. TA uploads resume → File saved correctly
5. Unauthenticated access → Returns 401
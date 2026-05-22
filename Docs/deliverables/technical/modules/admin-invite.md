# Admin Short Invitation Code Module Technical Documentation

## 1. Module Overview

Admin invitation registration now only retains the short invitation code flow. Admin views or refreshes the 8-character invitation code in the backend; invited users open `/admin-invite.jsp`, fill in email, username, password, and short invitation code to create `ADMIN` account.

**Core Components**:

- `InviteCodeService` - Generates/validates short invitation code based on server secret and time window
- `AdminCurrentInviteCodeServlet` - Admin reads or refreshes current short invitation code
- `AdminInviteAcceptServlet` - Public registration entry, validates short code and creates admin account
- `admin-invite.jsp` / `js/auth/admin-invite.js` - Admin invitation registration page
- `jsp/admin/invite.jsp` / `js/admin/admin-invite-management.js` - Admin short code management page

---

## 2. Backend Flow

### 2.1 InviteCodeService

**Path**: `backend/src/com/example/tarecruitment/admin/service/InviteCodeService.java`

`InviteCodeService` uses HMAC-SHA256 and server secret to generate 8-character short codes. Default is one window every 10 minutes; when validating, accepts current window and adjacent windows to prevent users from failing at countdown boundary.

Server-side persisted state is in `TA_HIRING_DATA_DIR/invites/`:

| File | Description |
|------|-------------|
| `invite_secret.bin` | Server secret for generating short codes |
| `rotation_offset.txt` | Rotation offset after admin manually refreshes |
| `forced_window_start.txt` | New window start time after manual refresh |

### 2.2 AdminCurrentInviteCodeServlet

**Path**: `backend/src/com/example/tarecruitment/admin/web/AdminCurrentInviteCodeServlet.java`

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/admin/invitations/current-code` | Admin | Returns current short code and remaining seconds |
| POST | `/api/admin/invitations/current-code` | Admin | Actively rotates short code and returns new short code |

### 2.3 AdminInviteAcceptServlet

**Path**: `backend/src/com/example/tarecruitment/admin/web/AdminInviteAcceptServlet.java`

| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/api/admin/invitations/acceptance` | Public | Validates short invitation code and creates Admin account |

Request fields:

| Field | Description |
|------|-------------|
| `email` | Admin email |
| `username` | Admin username |
| `password` | Login password |
| `inviteCode` | 8-character short invitation code |

After validation passes, Servlet writes to `users/users_admin.csv` via `UserDao`.

---

## 3. Frontend Flow

### 3.1 Admin Short Code Management Page

**Path**: `frontend/webapp/jsp/admin/invite.jsp`

Page calls current short code interface via `TARecruitment.routes.admin.currentInvitationCode()`. Admin can view current short code, remaining time, and actively refresh short code.

### 3.2 Admin Invitation Registration Page

**Path**: `frontend/webapp/admin-invite.jsp`

Page submits registration form via `TARecruitment.routes.admin.invitationAcceptance()`. Current flow no longer submits token, nor accesses old invitation validation interface.

---

## 4. Current Retention and Removal

Retained:

- `/admin-invite.jsp`
- `/api/admin/invitations/acceptance`
- `/api/admin/invitations/current-code`

Removed:

- Email/token invitation record chain
- Old invitation creation and validation interfaces
- Old admin registration instruction page
# Ouyang Xiaojun Division and Current Code Files

[Back to Overview](Overview.md)

## Basic Information

| Item | Content |
| --- | --- |
| Contributor | Ouyang Xiaojun |
| Standard commit count | 18 |
| Division overview | Backend foundational capabilities, authentication flow, API response and logging utilities, early skill matching implementation, partial testing/stats contribution |

## Division Overview

Ouyang Xiaojun primarily took on backend early foundational capabilities: user authentication, login/register, Session/permission validation, unified response utilities, logging utilities, and early skill matching service implementation. Later also contributed to position filtering response fixes, TA workload statistics, and script logging. The early skill matching entry and old invite tool were later taken offline, so they are no longer listed in the current existing code files.

## Current Corresponding Code Files

Backend authentication and session entry:

- `backend/src/com/example/tarecruitment/auth/model/User.java`
- `backend/src/com/example/tarecruitment/auth/dao/UserDao.java`
- `backend/src/com/example/tarecruitment/auth/web/LoginServlet.java`
- `backend/src/com/example/tarecruitment/auth/web/RegisterServlet.java`
- `backend/src/com/example/tarecruitment/auth/web/LogoutServlet.java`
- `backend/src/com/example/tarecruitment/auth/web/AuthFilter.java`
- `backend/src/com/example/tarecruitment/auth/web/AccessPolicy.java`
- `backend/src/com/example/tarecruitment/auth/web/CheckAvailableServlet.java`

Backend common response, session, permission, and logging utilities:

- `backend/src/com/example/tarecruitment/common/web/ApiResponses.java`
- `backend/src/com/example/tarecruitment/common/web/JsonResponseUtil.java`
- `backend/src/com/example/tarecruitment/common/web/SessionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/PermissionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/WebRequests.java`
- `backend/src/com/example/tarecruitment/common/service/ServiceResult.java`
- `backend/src/com/example/tarecruitment/common/util/Logger.java`

## File Overlap and Defense Attribution

Currently Ouyang Xiaojun no longer shares the old invite tool with Sun Jialu. The admin short invite code is explained by Sun Jialu's `InviteCodeService`.

## Test Presentation

Run command:

```bash
./scripts/test/test-ouyang-xiaojun.sh
```

Test code:

- `backend/test/OuyangXiaojunBackendTest.java`

Test coverage points:

- `ServiceResult` can stably express status codes, success flags, messages, and data from the service layer.
- `User` CSV serialization/deserialization preserves account profile fields.
- `UserDao` can initialize fixed demo accounts, validate login, and reject duplicate usernames.

For defense, you can explain:

Ouyang Xiaojun's test focus is on authentication and common backend foundational capabilities. The script compiles the backend source code first, then uses a temporary `TA_HIRING_DATA_DIR` to create isolated test data that won't pollute the real demo data on the machine. Passing the tests proves that login accounts, demo accounts, password hashing, and service return structures work correctly.
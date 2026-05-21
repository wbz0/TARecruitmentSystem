# TA Hiring System

A teaching scenario TA recruitment system based on `Servlet + JSP + vanilla JavaScript + CSS + CSV`. The project serves three roles:

- **TA**: Maintain personal profile, upload avatar/resume, browse positions, submit applications, view application progress, and receive AI job recommendations.
- **MO**: Post and maintain positions, view candidates, process applications, and use AI recommendations to assist filtering.
- **Admin**: View TA workload statistics, manage notifications, and generate admin registration invite codes.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17+, Jakarta Servlet |
| Container | Apache Tomcat 10.1+ or 11.x |
| Frontend | JSP, HTML, CSS, vanilla JavaScript |
| Build/Run | `scripts/dev.sh` / `scripts/dev.bat` |
| Persistence | CSV files + local upload directory |
| AI | AI recommendation search |
| i18n | Chinese/English bilingual frontend |

This project has no dependencies on Maven, Gradle, Spring, databases, or frontend build tools. The dev scripts directly use `javac` to compile `backend/src/**/*.java`, then deploy `frontend/webapp` to Tomcat.

## Directory Structure

| Path | Purpose |
| --- | --- |
| `backend/src/` | Backend Java source code |
| `frontend/webapp/` | JSP pages, CSS, JavaScript, `WEB-INF/web.xml`, and AI config templates |
| `scripts/dev.sh` | macOS / Linux one-click compile, deploy, start |
| `scripts/dev.bat` | Windows one-click compile, deploy, start |
| `scripts/config.example.sh` | macOS / Linux local config template |
| `scripts/config.example.bat` | Windows local config template |
| `scripts/javadocs.sh` | macOS / Linux JavaDoc generation script |
| `scripts/javadocs.bat` | Windows JavaDoc generation script |
| `docs/deliverables/technical/` | Architecture, API, deployment, and module documentation |
| `docs/deliverables/` | Course delivery materials |

## Environment Requirements

| Tool | Requirement | Notes |
| --- | --- | --- |
| JDK | `17+` | Must be able to use `javac` directly |
| Tomcat | `10.1+` or `11.x` | Project uses Jakarta Servlet 6 API |
| Shell | Bash or Windows CMD | Run local scripts |
| Node.js | Optional | Only used for `node --check` to validate frontend JS syntax |

## Quick Start

### macOS / Linux

```bash
cp scripts/config.example.sh scripts/config.sh
chmod +x scripts/dev.sh
```

Edit `scripts/config.sh`:

```bash
export CATALINA_HOME="/path/to/apache-tomcat-11.0.7"
export TOMCAT_HOME="${CATALINA_HOME}"
export APP_NAME="groupproject"
export TA_HIRING_DATA_DIR="${CATALINA_HOME}/data"
```

Start:

```bash
./scripts/dev.sh
```

### Windows

```bat
copy scripts\config.example.bat scripts\config.bat
```

Edit `scripts\config.bat`:

```bat
set CATALINA_HOME=D:\path\to\apache-tomcat-11.0.7
set TOMCAT_HOME=%CATALINA_HOME%
set APP_NAME=groupproject
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data
```

Start:

```bat
scripts\dev.bat
```

The script automatically executes:

```text
Clean build -> Compile backend/src -> Copy frontend/webapp -> Deploy to Tomcat webapps -> Start Tomcat
```

## Access URLs

With default `APP_NAME=groupproject`, after startup access:

| Page | URL |
| --- | --- |
| Portal Home | http://localhost:8080/groupproject/ |
| Login Page | http://localhost:8080/groupproject/login.jsp |
| TA/MO Registration | http://localhost:8080/groupproject/register.jsp |
| Admin Invite Registration | http://localhost:8080/groupproject/admin-invite.jsp |

Role pages are located at:

| Role | Key Pages |
| --- | --- |
| TA | `/jsp/ta/dashboard.jsp`, `/jsp/ta/job-list.jsp`, `/jsp/ta/application-status.jsp`, `/jsp/ta/notifications.jsp` |
| MO | `/jsp/mo/dashboard.jsp`, `/jsp/mo/notifications.jsp` |
| Admin | `/jsp/admin/dashboard.jsp`, `/jsp/admin/invite.jsp`, `/jsp/admin/notifications.jsp` |

## Demo Accounts

Demo accounts and sample positions/applications are automatically populated on startup. Existing CSV data will not be cleared.

| Role | Username | Password |
| --- | --- | --- |
| TA | `ta_demo` | `Pass1234` |
| TA | `ta_demo_mia` | `Pass1234` |
| TA | `ta_demo_noah` | `Pass1234` |
| TA | `ta_demo_olivia` | `Pass1234` |
| TA | `ta_demo_liam` | `Pass1234` |
| MO | `mo_demo` | `Pass1234` |
| MO | `mo_demo_alice` | `Pass1234` |
| MO | `mo_demo_brian` | `Pass1234` |
| Admin | `admin_demo` | `Pass1234` |

## Data and Logs

Runtime data must be specified by `TA_HIRING_DATA_DIR`. The code will not write runtime data into the repository. Users, positions, applications, notifications, avatars, and resumes are all stored under this directory.

Backend log files are located in the project root:

```text
logs/app.log
```

## Page Features

| Role | Frontend Visible Features |
| --- | --- |
| TA | Login/Register, maintain account and TA profile, upload avatar/resume, browse positions, submit applications, view application status, view job recommendations |
| MO | Login/Register, post/edit/delete positions, view candidates, process applications, view applicant recommendations |
| Admin | Login, view TA workload statistics, view notifications, view/refresh current 8-digit admin invite code |

New Admin accounts are created via `/admin-invite.jsp` by entering an 8-digit invite code. The current invite code can be viewed or refreshed on the Admin page `/jsp/admin/invite.jsp`.

## AI Configuration

AI configuration only affects the recommendation feature on frontend pages. Templates are located in `frontend/webapp/WEB-INF/ai/`. Real key files use `*.local.properties` and are ignored by `.gitignore`.

### Recommendation Search

Used for MO applicant recommendations and TA job recommendations.

```bash
cp frontend/webapp/WEB-INF/ai/deepseek.properties.template \
   frontend/webapp/WEB-INF/ai/deepseek.local.properties
```

AI recommendation search does not provide local fallback data. If the key is not configured or the service is unavailable, the frontend displays the error message "AI recommendations are temporarily unavailable, please try again later." No fake recommendation results will be generated.

## FAQ

- **Tomcat not found**: Check if `CATALINA_HOME` / `TOMCAT_HOME` points to the real Tomcat root directory and confirm `lib/servlet-api.jar` exists.
- **Data directory not configured**: Check if `TA_HIRING_DATA_DIR` is written to `scripts/config.sh` or `scripts/config.bat`.
- **Script shows All Done but page won't open**: Check if 8080/8005 is occupied by another Tomcat or service, and check Tomcat logs.
- **Page opens but no data**: Confirm the same `TA_HIRING_DATA_DIR` is used for this run, and confirm the demo data initialization had no errors during startup.
- **Admin registration failed**: Admin accounts must use the 8-digit short invite code from `/admin-invite.jsp`. The current invite code can be viewed or refreshed on Admin's `/jsp/admin/invite.jsp` page.
- **AI recommendation unavailable**: Check if the corresponding `*.local.properties` has a real key configured.

## More Documentation

- Technical documentation entry: `docs/deliverables/technical/index.md`
- Deployment guide: `docs/deliverables/technical/deployment/deployment-guide.md`
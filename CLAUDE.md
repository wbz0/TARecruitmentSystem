# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **TA (Teaching Assistant) Recruitment System** for a university course (EBU6304). It's a traditional Java web application using Tomcat + Servlet + JSP with CSV-based data storage.

## Tech Stack

- **Language**: Java 17+
- **Web Framework**: Jakarta EE (Servlet 6.0)
- **Server**: Apache Tomcat 11.x
- **Frontend**: JSP, HTML, CSS, JavaScript
- **Data Storage**: CSV files (no database)

## Commands

```cmd
# Configure (first time only)
cd scripts
copy config.example.bat config.bat
# Edit config.bat to set CATALINA_HOME path

# Build and run
build.bat
deploy.bat
startup.bat

# After code changes (no restart needed)
build.bat
deploy.bat
```

Access: http://localhost:8080/groupproject/

## Architecture

```
backend/src/com/example/authlogin/
├── model/          # Entity classes (User, Applicant)
├── dao/            # Data access (UserDao, ApplicantDao)
├── *Servlet.java  # Servlets (LoginServlet, RegisterServlet, etc.)
├── filter/        # Filters (AuthFilter)
└── util/          # Utilities (SessionUtil, PermissionUtil)

frontend/webapp/   # JSP, HTML, CSS, JS files
scripts/           # Build & deployment scripts
data/              # CSV data storage
```

## Key Files

- `scripts/config.bat` - Tomcat path configuration
- `frontend/webapp/WEB-INF/web.xml` - Web app deployment descriptor
- `data/users.csv` - User data storage

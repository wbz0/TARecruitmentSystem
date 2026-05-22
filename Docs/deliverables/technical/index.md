# TA Hiring System - Technical Documentation

## Overview

This document is the technical documentation collection for the TA Hiring System, providing the overall system architecture design and detailed technical implementation descriptions for each functional module.

**Project Background**: This is a TA (Teaching Assistant) recruitment management system for computer science master's courses, allowing students to apply for TA positions, module owners (MO) to post positions and review applications, and integrating AI recommendation search functionality.

---

## Documentation Index

### Code Documentation

| Document | Description |
|----------|-------------|
| [code-documentation.md](./code-documentation.md) | JavaDoc auto-generation, reading order, and code documentation maintenance rules |

### Architecture Design

| Document | Description |
|----------|-------------|
| [system-architecture.md](./architecture/system-architecture.md) | Overall system architecture design |
| [data-architecture.md](./architecture/data-architecture.md) | Data architecture and storage design |
| [security-architecture.md](./architecture/security-architecture.md) | Security architecture and permission design |

### Functional Module Technical Documentation

| Document | Description |
|----------|-------------|
| [authentication.md](./modules/authentication.md) | Authentication and permission module |
| [ta-profile.md](./modules/ta-profile.md) | TA profile management module |
| [job-management.md](./modules/job-management.md) | Job management module |
| [application-review.md](./modules/application-review.md) | Application review module |
| [ai-matching.md](./modules/ai-matching.md) | AI recommendation module |
| [admin-workload.md](./modules/admin-workload.md) | Admin workload statistics module |
| [admin-invite.md](./modules/admin-invite.md) | Admin invitation module |

### API and Deployment

| Document | Description |
|----------|-------------|
| [servlet-api.md](./api/servlet-api.md) | Servlet API documentation |
| [deployment-guide.md](./deployment/deployment-guide.md) | Deployment and operations guide |

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17+, Jakarta Servlet 6 |
| Container | Apache Tomcat 11.x |
| Frontend | JSP, HTML5, CSS3, Vanilla JavaScript |
| Build/Run | Script direct execution |
| Persistence | CSV file storage |
| AI | DeepSeek compatible API (optional) |
| i18n | Chinese and English bilingual |

---

## Project Structure

```
backend/src/com/example/tarecruitment/
├── common/             # JSON, requests, CSV, storage paths and other common infrastructure
├── auth/               # Login, registration, session, and permission filtering
├── profile/            # TA profile, resume, and avatar access
├── job/                # Job model, DAO, and HTTP interface
├── application/        # Application records and application workflow
├── ai/                 # AI recommendation client, service, and interface
├── admin/              # Admin invitation and workload statistics
├── notification/       # Notification model, DAO, and interface
└── demo/               # Demo accounts and demo data initialization

frontend/webapp/
├── index.jsp           # Portal homepage
├── login.jsp           # Login page
├── register.jsp        # Registration page
├── jsp/
│   ├── ta/             # TA role pages (5 pages)
│   ├── mo/             # MO role pages (3 pages)
│   └── admin/          # Admin role pages (2 pages)
├── css/                # Style files
└── js/                 # Frontend scripts

docs/deliverables/technical/
├── architecture/       # Architecture design documents
├── modules/            # Functional module documents
├── api/                # API documentation
├── code-documentation.md  # Code documentation and JavaDoc guide
├── javadocs/           # Auto-generated JavaDoc HTML
└── deployment/         # Deployment documentation
```

Auto-generated JavaDoc is part of the technical delivery materials, generated via script to:

```text
docs/deliverables/technical/javadocs/index.html
```

---

## Role Description

| Role | Description | Main Functions |
|------|-------------|----------------|
| **TA** | Teaching Assistant Applicant | Create profile, browse jobs, submit applications, view application status |
| **MO** | Module Owner | Post jobs, manage applications, AI recommendations and application analysis |
| **ADMIN** | System Administrator | TA workload statistics, manage short invitation codes |

---

## Core Data Flow

```
[TA]  --register--> [User] --create profile--> [Applicant]
                           |
[MO]  --post job--> [Job] <--apply-- [Application] --review--> [TA]
                           |
[AI]  --recommend/analyze--> [Candidate Insight] --> [MO filtering]
```

---

## Documentation Changelog

| Date | Version | Description |
|------|---------|-------------|
| 2026-05-15 | 1.1.1 | Adjusted JavaDoc output to technical delivery directory |
| 2026-05-14 | 1.1.0 | Added JavaDoc auto-generation instructions and code documentation entry |
| 2026-03-28 | 1.0.0 | Initial technical documentation |
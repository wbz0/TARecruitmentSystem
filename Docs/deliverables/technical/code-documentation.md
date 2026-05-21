# Code Documentation and JavaDoc Usage Guide

## 1. Document Relationship

This project has two types of technical documents:

| Document Type | Location | Purpose |
|---------------|----------|---------|
| Human-written technical documents | `docs/deliverables/technical/` | Explain system architecture, module responsibilities, APIs, deployment, and main business processes |
| Auto-generated JavaDoc | `docs/deliverables/technical/javadocs/index.html` | Auto-generated from Java source comments for classes, methods, parameters, and package-level descriptions |

When reading code, it is recommended to first read `docs/deliverables/technical/index.md` to understand system layers and module boundaries; then generate JavaDoc to view specific class and public method responsibilities.

## 2. Generate JavaDoc

macOS / Linux:

```bash
./scripts/javadocs.sh
```

Windows:

```bat
scripts\javadocs.bat
```

The script reads `TOMCAT_HOME` from local `scripts/config.sh` or `scripts/config.bat`, and uses:

```text
TOMCAT_HOME/lib/servlet-api.jar
```

As the Jakarta Servlet dependency. Generated output is placed at:

```text
docs/deliverables/technical/javadocs/index.html
```

JavaDoc output directory is within the technical delivery materials directory. After source code, public API, package structure, or comments are updated, the script should be rerun to refresh that directory.

## 3. Code Reading Order

Backend code is organized by domain packages:

```text
com.example.tarecruitment
├── auth          # Login, registration, session, and access control
├── profile       # Current account, TA profile, avatar and resume
├── job           # Job posting, list, detail, edit, and delete
├── application   # Application creation, list, detail, and status transitions
├── ai            # Job recommendations, applicant recommendations, and detail AI analysis
├── admin         # Admin invitation and workload statistics
├── notification  # Notification list, publish, and delete
├── common        # API routes, unified responses, CSV, search, and Web utilities
└── demo          # Demo accounts and demo data initialization
```

For each major business domain, read in this order:

```text
web -> service -> dao/model/mapper/validator
```

| Layer | Responsibility |
|-------|----------------|
| `web` | Servlet HTTP entry, only parses requests, reads current user, calls service, writes unified response |
| `service` | Business process, permission-related business validation, status transitions, and cross-DAO collaboration |
| `dao` | CSV read/write and queries, does not depend on request, session, or response |
| `model` | Data structures, enums, CSV serialization and deserialization fields |
| `mapper` | Request parameter to domain parameter, model to response payload conversion |
| `validator` | Required field, length, format, dangerous input, and file constraint validation |

## 4. Maintenance Rules

- When modifying public API, service behavior, CSV fields, or permission strategies, synchronously update JavaDoc and corresponding module documents.
- When adding new packages, add `package-info.java` to provide package-level entry for auto JavaDoc.
- When adding new Servlets, confirm that route constants come from `common/api/ApiRoutes.java`.
- Frontend examples should be expressed via `TARecruitment.routes` and `TARecruitment.api.request`, not hand-written old root path interfaces.
- In JavaDoc, only explain non-obvious behaviors; ordinary getter/setter does not need mechanical comments.
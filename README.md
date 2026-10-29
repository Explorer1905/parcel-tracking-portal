# Parcel Tracking Portal

**Student:** Shravani Chavan | **Division:** CMPN A | **Subject:** DevOps  
**GitHub:** [Explorer1905/parcel-tracking-portal](https://github.com/Explorer1905/parcel-tracking-portal)  
**Viva:** 30 October 2026

---

## What This Project Is

A production-grade **Parcel Tracking Portal** built using Java 17 / Tomcat 10.1 / PostgreSQL 16 / Docker. It demonstrates a full DevOps lifecycle: agile planning → code → CI/CD → containerisation → configuration management → monitoring.

**Live endpoints (local Docker Compose):**

| URL | Purpose |
|-----|---------|
| `http://localhost:8090/parcel-tracking-portal/` | Home page |
| `http://localhost:8090/parcel-tracking-portal/track` | Public parcel tracker (no login) |
| `http://localhost:8090/parcel-tracking-portal/parcels` | Register / list parcels |
| `http://localhost:8090/parcel-tracking-portal/dashboard` | Ops dashboard with summary cards |
| `http://localhost:8090/parcel-tracking-portal/alerts` | Delayed / failed parcel alerts |
| `http://localhost:8090/parcel-tracking-portal/api/summary` | JSON summary (REST API) |
| `http://localhost:8090/parcel-tracking-portal/api/parcels` | Paginated parcel list (REST API) |
| `http://localhost:8090/parcel-tracking-portal/export/parcels.csv` | CSV export |
| `http://localhost:8090/parcel-tracking-portal/health` | Health check → `OK` |

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Web | Jakarta Servlet 6.0 + JSP on Tomcat 10.1 |
| Build | Apache Maven 3 (WAR packaging) |
| Database | PostgreSQL 16 (HikariCP 5.1.0 connection pool) |
| Migrations | Flyway 10.20.1 (V1: schema, V2: parcel details) |
| Containers | Docker + Docker Compose |
| CI/CD | Jenkins (Windows service) |
| Config Mgmt | Ansible (WSL Ubuntu) |
| Testing | JUnit 5 (39 tests) + Selenium 4 WebDriver (ChromeDriver) |
| Registry | Local Docker registry `localhost:5000` |

---

## Quick Start

### Prerequisites
- Docker Desktop (Windows) with WSL 2 integration
- Java 17, Maven 3.9+

### Run locally

```powershell
# 1. Clone
git clone https://github.com/Explorer1905/parcel-tracking-portal.git
cd parcel-tracking-portal

# 2. Build WAR
mvn -B clean package -DskipTests

# 3. Start database + app containers
docker compose up -d --build

# 4. Wait for health
curl http://localhost:8090/parcel-tracking-portal/health  # → OK

# 5. Open browser
start http://localhost:8090/parcel-tracking-portal/
```

### Run tests

```powershell
# Unit tests only (no Docker needed)
mvn test "-Dtest=ParcelServiceValidationTest,ApiHelperTest"

# Full suite (Docker must be up)
mvn test "-Dselenium.baseUrl=http://localhost:8090/parcel-tracking-portal"
```

**Test results:** Tests run: 39, Failures: 0, Errors: 0, Skipped: 0

---

## Database Schema

```sql
-- V1: base schema
parcels(parcel_id PK, sender, receiver, origin, destination, created_at)
events(event_id PK, parcel_id FK, status, location, event_time, remarks)

-- V2: parcel details (Flyway migration)
ALTER TABLE parcels ADD COLUMN weight_kg     NUMERIC(8,2) NOT NULL DEFAULT 1.0
                                                CHECK (weight_kg BETWEEN 0.01 AND 1000);
ALTER TABLE parcels ADD COLUMN priority      VARCHAR(10)  NOT NULL DEFAULT 'Standard'
                                                CHECK (priority IN ('Standard','Express','Urgent'));
ALTER TABLE parcels ADD COLUMN expected_delivery DATE;
ALTER TABLE parcels ADD COLUMN assigned_agent    VARCHAR(100);
```

Flyway applies migrations automatically on startup. Existing rows receive safe defaults (weight=1.0, priority=Standard).

---

## Project Structure

```
parcel-tracking-portal/
├── src/main/java/com/parcel/
│   ├── controller/         # Servlets (Parcels, Parcel, Dashboard, Alerts, Track,
│   │                       #          ApiParcels, ApiSummary, Export, Health, Search)
│   ├── dao/                # ParcelDao (JDBC + HikariCP)
│   ├── model/              # Parcel (record), ParcelEvent, DashboardSummary
│   ├── service/            # ParcelService (all validation, injectable)
│   └── util/               # Database, Html, Json
├── src/main/resources/db/
│   ├── migration/          # V1__create_schema.sql, V2__add_parcel_details.sql
│   └── demo/               # R__demo_data.sql (repeatable, 10 parcels)
├── src/main/webapp/
│   ├── WEB-INF/views/      # parcels.jsp, parcel.jsp, dashboard.jsp,
│   │                       # alerts.jsp, track.jsp
│   └── assets/styles.css   # Responsive CSS
├── src/test/java/com/parcel/
│   ├── ParcelServiceValidationTest.java  # 17 unit tests (no DB)
│   ├── ApiHelperTest.java                # 16 unit tests (no DB)
│   └── selenium/ParcelPortalSeleniumTest.java  # 6 Selenium tests
├── ansible/                # 5 playbooks + 7 roles + README.md
├── Dockerfile              # Multi-stage: Maven build → Tomcat runtime
├── docker-compose.yml      # PostgreSQL 16 + Tomcat app stack
├── Jenkinsfile             # CI/CD pipeline (build → test → push → deploy)
└── docs/                   # 15 task .docx files, screenshots, API.md, VIVA_QA.md
```

---

## DevOps Tasks Completed

| # | Task | Branch | Evidence |
|---|------|--------|---------|
| 1 | Problem definition | task/01-problem-definition | `docs/task-01-problem-definition.docx` |
| 2 | Agile plan + DevOps lifecycle | task/02-agile-planning | `docs/task-02-agile-planning.docx` |
| 3 | Architecture (SRS, diagrams, API) | task/03-architecture-setup | `docs/task-03-architecture-setup.docx`, `docs/API.md` |
| 4 | GitHub repo setup | task/04-repo-init | `docs/task-04-repo-init.docx` |
| 5 | Feature branch + PR + code review | feature/parcel-entry | `docs/task-05-deployment-testing.docx` |
| 6 | MVP + merge conflict + v1.0 tag | task/06-mvp-completion | `docs/task-06-mvp-completion.docx` |
| 7 | Jenkins CI job | task/07-jenkins-ci | `docs/task-07-jenkins-ci.docx` |
| 8 | Jenkinsfile pipeline-as-code | task/08-pipeline-as-code | `docs/task-08-pipeline-as-code.docx`, `Jenkinsfile` |
| 9 | Selenium test suite | task/09-selenium-tests | `docs/task-09-selenium-tests.docx`, 6 Selenium tests |
| 10 | Selenium quality gate + defect cycle | task/10-continuous-testing | `docs/task-10-continuous-testing.docx` |
| 11 | Docker containerisation | task/11-docker | `docs/task-11-docker.docx`, `Dockerfile` |
| 12 | Jenkins-Docker CD (versioned images) | task/12-jenkins-docker-cd | `docs/task-12-jenkins-docker-cd.docx` |
| 13 | Ansible configuration management | ops/ansible-postgres | `ansible/`, `docs/task-13-configuration-management.docx` |
| 14 | Provisioning, idempotency, rollback | ops/ansible-postgres | `ansible/rollback.yml`, `docs/task-14-provisioning-reliability.docx` |
| 15 | Final end-to-end + viva | docs/phase-d | `docs/VIVA_QA.md`, `docs/final-release-runbook.md` |

---

## CI/CD Pipeline (Jenkinsfile)

```
Checkout → mvn clean package → mvn test (Selenium) → docker build →
docker push localhost:5000/parcel-tracking-portal:BUILD_NUMBER →
docker compose up -d → /health probe → archive artefacts
```

Parameterised build: `DEPLOY_ENV` (dev / staging / production).

---

## Ansible (Configuration Management)

```bash
# From WSL Ubuntu:
cd ansible/

# Full provision + deploy
ansible-playbook -i inventory.ini site.yml -e release_tag=1.1.42 -e postgres_password=...

# Deploy new release
ansible-playbook -i inventory.ini deploy.yml -e release_tag=1.1.50 -e postgres_password=...

# Rollback
ansible-playbook -i inventory.ini rollback.yml -e rollback_tag=1.0.5 -e postgres_password=...

# Backup DB
ansible-playbook -i inventory.ini backup.yml

# Restore DB
ansible-playbook -i inventory.ini restore.yml -e backup_file=...
```

See [ansible/README.md](ansible/README.md) for exact expected output of each command.

---

## REST API

See [docs/API.md](docs/API.md) for the full API reference.

Quick summary:

| Method | Endpoint | Description |
|--------|---------|-------------|
| GET | `/api/summary` | Dashboard counts (JSON) |
| GET | `/api/parcels?q=&page=&size=` | Paginated parcel list |
| GET | `/api/parcels/{id}` | Single parcel with events |
| POST | `/api/parcels` | Create parcel (JSON body) |
| POST | `/api/parcels/{id}/events` | Add tracking event |
| GET | `/export/parcels.csv` | Export all parcels as CSV |
| GET | `/health` | Health probe → `OK` |

---

## Viva Preparation

See [docs/VIVA_QA.md](docs/VIVA_QA.md) — 40 Q&A pairs covering all 15 tasks.
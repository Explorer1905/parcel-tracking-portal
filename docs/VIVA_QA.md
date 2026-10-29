# Viva Q&A – Parcel Tracking Portal (DevOps)

**Student:** Shravani Chavan | **Viva:** 30 October 2026  
**40 questions covering all 15 DevOps tasks**

---

## Task 1 – Problem Definition

**Q1. What problem does your project solve?**  
Courier companies track thousands of parcels manually using spreadsheets. When a parcel is delayed or lost, there is no systematic way to identify it or alert operations staff. The Parcel Tracking Portal automates registration, status tracking, delay detection, and alerting. Parcels are stored in a PostgreSQL database with full event history, and delayed/failed parcels appear automatically on an Alerts dashboard.

**Q2. Who are the users of this system?**  
Two types: (1) **Operations staff** — register parcels, add tracking events, monitor the dashboard; (2) **Public / customers** — use the `/track` page to check their parcel status by ID without logging in.

---

## Task 2 – Agile Plan + DevOps Lifecycle

**Q3. What agile artefacts did you produce?**  
A product backlog with 15 user stories mapped to the 15 DevOps tasks, a sprint plan (3 sprints of 1 week each), and a DevOps lifecycle diagram showing the continuous loop: Plan → Code → Build → Test → Release → Deploy → Monitor → Feedback.

**Q4. Draw or describe your DevOps lifecycle.**  
Plan (GitHub issues + backlog) → Code (feature branches) → Build (Maven WAR via Jenkins) → Test (JUnit 5 unit tests + Selenium integration tests) → Release (Docker image tagged and pushed to local registry `localhost:5000`) → Deploy (Jenkins triggers `docker compose up` or Ansible playbook) → Monitor (`/health` endpoint + Alerts dashboard) → Feedback (GitHub PR reviews, Jenkins failure notifications).

---

## Task 3 – Architecture

**Q5. Describe the system architecture.**  
Three-tier: (1) **Presentation** — JSP/HTML pages served by Tomcat, plus REST API JSON endpoints; (2) **Business Logic** — Java service layer (`ParcelService`) with all validation; (3) **Data** — PostgreSQL 16 with Flyway-managed schema, accessed via HikariCP connection pool. Everything runs in Docker containers orchestrated by Docker Compose. A local Docker registry at `localhost:5000` stores versioned images.

**Q6. What is Flyway and why did you use it?**  
Flyway is a database migration tool. Instead of running SQL manually, you write numbered migration files (`V1__create_schema.sql`, `V2__add_parcel_details.sql`). Flyway applies them in order on startup and records which ones ran in `flyway_schema_history`. This means the database schema evolves safely with the code — no manual SQL needed, and old databases are upgraded automatically.

**Q7. What does V2 migration add?**  
Four columns on the `parcels` table: `weight_kg` (numeric 0.01–1000 kg), `priority` (Standard/Express/Urgent), `expected_delivery` (date), `assigned_agent` (varchar). Existing rows get safe defaults (1.0 kg, Standard, null). The columns have CHECK constraints enforced both in the database and in `ParcelService.addParcel()`.

**Q8. Explain the data model.**  
Two tables: `parcels` (one row per shipment, primary key `parcel_id`) and `events` (one row per status scan, foreign key back to `parcels`). A parcel's current status is always the most recent event. Alerts are identified by a JOIN that finds parcels whose latest event is `Failed` or has had no update in 48+ hours.

**Q9. What APIs does the system expose?**  
Seven endpoints — see `docs/API.md`. Key ones: `GET /api/summary` (JSON counts), `GET /api/parcels?q=&page=&size=` (paginated search), `GET /api/parcels/{id}` (with event timeline), `POST /api/parcels` (create), `POST /api/parcels/{id}/events` (add event), `GET /export/parcels.csv` (CSV download), `GET /health` (probe).

---

## Task 4 – GitHub Repository Setup

**Q10. How did you set up the repository?**  
Created `Explorer1905/parcel-tracking-portal` on GitHub with `main` as the default branch, added `.gitignore` (excludes `target/`, `*.class`, secrets), branch protection on `main` (no direct push), and a `develop` integration branch. All work happens on feature branches merged via Pull Requests.

**Q11. What is your branching strategy?**  
`main` → `develop` → feature branches (`feature/`, `task/`, `ops/`, `docs/`). Feature branches are merged into `develop` via PR with at least one review. `develop` is merged to `main` only for releases. We never push directly to `main`.

---

## Task 5 – Feature Branch + PR + Code Review

**Q12. Walk through a pull request in this project.**  
Example: `feature/parcel-entry` branched off `develop`. Changes made: `Parcel.java`, `ParcelServlet.java`, `parcels.jsp`. PR opened → reviewer left comments → one change requested (validation moved to service layer) → change made → approved → merged (squash commit). The merge commit triggers the Jenkins CI job automatically via webhook.

---

## Task 6 – MVP + Merge Conflict + Tag

**Q13. How did you resolve the merge conflict?**  
Two developers edited `parcels.jsp` concurrently — one added a search box, the other updated the table headers. Git flagged the conflict with `<<<<<<`/`>>>>>>` markers. I opened the file, kept both changes (search box above the table, new headers), removed the markers, ran `mvn compile` to verify, then committed the resolution. The commit message references the conflict: `fix: resolve merge conflict in parcels.jsp`.

**Q14. What are your release tags?**  
`v1.0` — first working MVP (H2 in-memory DB). Later Docker images are tagged with the Jenkins build number: `localhost:5000/parcel-tracking-portal:1.0.5`, `1.0.7` (pre-PostgreSQL, kept for rollback demo), `1.1.x` (PostgreSQL releases).

---

## Task 7 – Jenkins CI Job

**Q15. How is Jenkins set up?**  
Jenkins runs as a Windows service (LocalSystem). A Freestyle job called `parcel-continuous-testing` polls GitHub every 5 minutes (`H/5 * * * *`). Steps: checkout → `mvn clean package -DskipTests` → `docker compose up -d --build` → `mvn test -Dselenium.baseUrl=...` → archive test reports + screenshots.

**Q16. What happens when a test fails in Jenkins?**  
The build is marked FAILED (red ball). Selenium failure screenshots are saved to `docs/screenshots/failures/` and archived as build artefacts. The next build attempt either fixes the failure or produces another screenshot. This was demonstrated in Task 10 — a deliberate bug in `ParcelService` caused the Selenium test to fail, the bug was then fixed, and the pipeline went green again.

---

## Task 8 – Jenkinsfile Pipeline-as-Code

**Q17. What is the structure of your Jenkinsfile?**  
Five stages: (1) **Checkout** — `git checkout`; (2) **Build** — `mvn -B clean package -DskipTests`; (3) **Test** — `mvn test -Dselenium.baseUrl=...` (fails the build if any test fails); (4) **Docker Push** — `docker tag` + `docker push localhost:5000/parcel-tracking-portal:${BUILD_NUMBER}`; (5) **Deploy** — `docker compose up -d --no-build` using the just-pushed image. Post section archives JUnit XML and screenshots on both success and failure.

**Q18. What is the DEPLOY_ENV parameter and why?**  
A string parameter with values `dev`, `staging`, `production`. It controls which `.env` file is loaded by Docker Compose, so the same Jenkinsfile deploys to different environments without code changes. For the viva demo, `dev` is used (port 8090).

---

## Task 9 – Selenium Tests

**Q19. How many Selenium tests are there and what do they test?**  
Six tests in `ParcelPortalSeleniumTest.java`:
1. `homePageLoads` — home page has title "Parcel Tracking Portal"
2. `addParcelAndVerify` — register a parcel via form; verify it appears in the table
3. `trackParcelViaDetailPage` — navigate to parcel detail; add an event; verify timeline
4. `searchParcelsFiltersList` — search for a parcel ID; verify the result is shown
5. `failedParcelAppearsOnAlertsPage` — create a parcel with a Failed event; verify it appears in the Alerts tab
6. `publicTrackPageFindsParcel` — use the public `/track` page; verify status + input pre-filled

**Q20. Where are failure screenshots stored?**  
`docs/screenshots/failures/` (relative to the repo root). The `@AfterEach` method in the test class calls `captureFailure(testName)` which uses `TakesScreenshot` to save a PNG named after the failing test method. Jenkins archives this directory as a build artefact.

---

## Task 10 – Selenium Quality Gate + Defect Cycle

**Q21. What was the deliberate defect you introduced and fixed?**  
In `ParcelService.addParcel()`, the validation for a duplicate parcel ID was removed — so the DAO's `INSERT` was called even when the ID already existed. The Selenium test `addParcelAndVerify` passed (the parcel was registered), but the unit test `duplicateIdRejected` failed because `IllegalArgumentException` was no longer thrown. Jenkins went red. The fix was to restore the `dao.exists(id)` check. Jenkins went green. This demonstrates the quality gate: CI catches regressions immediately.

---

## Task 11 – Docker Containerisation

**Q22. Explain your Dockerfile.**  
Multi-stage build: Stage 1 (`builder`) uses `maven:3.9-eclipse-temurin-17` to run `mvn clean package -DskipTests` and produce the WAR. Stage 2 (`runtime`) uses `tomcat:10.1-jre17-temurin` and copies only the WAR into `$CATALINA_HOME/webapps/`. No Maven or build tools in the final image — smaller, more secure. A `HEALTHCHECK` using `curl` probes `/health` every 30s.

**Q23. What does `docker compose up -d --build` do?**  
`--build` forces Docker to rebuild the image even if one already exists. `-d` runs containers in detached (background) mode. Compose starts two containers: `parcel-db` (PostgreSQL 16, named volume `pgdata`) and `parcel-app` (Tomcat), with `parcel-app` depending on `parcel-db` being healthy (via `healthcheck`). The `depends_on: condition: service_healthy` ensures Tomcat never starts before PostgreSQL is ready.

**Q24. How do you verify the container lifecycle?**  
```bash
docker compose ps                              # shows both containers Running
docker logs parcel-app --tail 50               # Flyway migration log + Tomcat started
docker exec parcel-db psql -U parcel -d parcels -c "SELECT COUNT(*) FROM parcels;"
curl http://localhost:8090/parcel-tracking-portal/health  # → OK
```

---

## Task 12 – Jenkins-Docker CD with Versioned Images

**Q25. How does Jenkins push a versioned image?**  
After tests pass, the Jenkinsfile runs:
```bash
docker tag parcel-tracking-portal:latest localhost:5000/parcel-tracking-portal:${BUILD_NUMBER}
docker push localhost:5000/parcel-tracking-portal:${BUILD_NUMBER}
```
The local registry at `localhost:5000` stores every build. You can inspect all available tags:
```bash
curl http://localhost:5000/v2/parcel-tracking-portal/tags/list
```

**Q26. How do you deploy a specific version?**  
```bash
# Via Ansible (recommended):
ansible-playbook -i ansible/inventory.ini ansible/deploy.yml \
  -e release_tag=1.1.42 -e postgres_password=parcel_pass

# Or manually:
docker pull localhost:5000/parcel-tracking-portal:1.1.42
APP_IMAGE=localhost:5000/parcel-tracking-portal:1.1.42 docker compose up -d --no-build
```

---

## Task 13 – Ansible Configuration Management

**Q27. What does the Ansible `site.yml` playbook do?**  
Two plays: (1) **Provision** — runs 5 roles: `packages` (installs Docker prereqs), `service-user` (creates `parcel` system user/group), `app-dirs` (creates 4 directories), `app-config` (renders `.env` from Jinja2 template), `firewall` (opens ports 8082, 5000, 5434). (2) **Deploy** — runs `deploy` role (pulls image, `docker compose up`) and `health-check` role (polls `/health`).

**Q28. How do you prove idempotency?**  
Run `site.yml` a second time with the same variables. Every task should report `ok` (no changes). Ansible's `apt`, `user`, `group`, `file`, `template`, and `copy` modules are all idempotent by design. The deploy role uses `docker compose up -d` which is also idempotent (Compose only recreates containers if the image changed). Expected output second run:
```
PLAY RECAP  localhost : ok=N  changed=0  unreachable=0  failed=0
```

**Q29. How are database credentials handled?**  
They are **never** stored in Git. The `.env` file is rendered from a Jinja2 template (`ansible/roles/app-config/templates/env.j2`) using the `ansible.builtin.template` module. The `postgres_password` variable is supplied at runtime via `-e postgres_password=...` or from an Ansible Vault-encrypted file (`--ask-vault-pass`). The render task uses `no_log: true` to prevent the password appearing in Ansible output.

---

## Task 14 – Provisioning, Idempotency, Health Check, Rollback

**Q30. Explain the rollback process step by step.**  
```bash
# 1. Check current running tag
cat /opt/parcel-tracking-portal/active_release  # e.g. 1.1.42

# 2. Run rollback playbook with an OLDER tag
ansible-playbook -i ansible/inventory.ini ansible/rollback.yml \
  -e rollback_tag=1.0.5 -e postgres_password=parcel_pass
```
The playbook: reads `active_release` → asserts `rollback_tag ≠ current_release` → re-renders `.env` with `release_tag=1.0.5` → `docker pull localhost:5000/parcel-tracking-portal:1.0.5` → `docker compose up -d` → health check. Database volume is **not touched**. `active_release` is updated to `1.0.5`.

**Q31. How does the Ansible health check role work?**  
It uses `ansible.builtin.uri` to make an HTTP GET to `{{ health_url }}` and waits until `status == 200` and `content == 'OK'`. It retries up to 20 times with a 5-second delay between attempts (total wait: up to 100 seconds). If the app doesn't start in time, the entire play fails — this is the deployment gate.

**Q32. How does the backup playbook work?**  
It runs `pg_dump` inside the running `parcel-production-db` container via `docker exec`, pipes the output through `gzip`, and saves it to `/var/backups/parcel-tracking-portal/parcels_YYYYMMDD_HHMMSS.sql.gz`. After creating the backup, it deletes any `.sql.gz` files older than 7 days using `ansible.builtin.find` + `ansible.builtin.file state=absent`.

---

## Task 15 – Final End-to-End Run

**Q33. Walk through the complete end-to-end demo sequence for the viva.**

```bash
# 1. Build WAR
mvn -B clean package -DskipTests

# 2. Start stack (rebuilds image)
docker compose up -d --build

# 3. Wait for health
curl http://localhost:8090/parcel-tracking-portal/health  # → OK

# 4. Open browser: http://localhost:8090/parcel-tracking-portal/

# 5. Track public page: /track?id=DEMO-1001

# 6. API: GET /api/summary
curl http://localhost:8090/parcel-tracking-portal/api/summary

# 7. API: GET /api/parcels?size=3
curl "http://localhost:8090/parcel-tracking-portal/api/parcels?size=3"

# 8. Run full test suite
mvn test "-Dselenium.baseUrl=http://localhost:8090/parcel-tracking-portal"
# → Tests run: 39, Failures: 0

# 9. Export CSV
curl -o parcels.csv http://localhost:8090/parcel-tracking-portal/export/parcels.csv

# 10. Show Flyway migration history
docker exec parcel-db psql -U parcel -d parcels \
  -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"

# 11. Show Ansible syntax check (WSL)
wsl ansible-playbook -i ansible/inventory.ini ansible/site.yml --syntax-check

# 12. Trigger Jenkins build: http://localhost:8080
```

---

## General DevOps Questions

**Q34. What is the difference between CI and CD?**  
**CI (Continuous Integration):** every code push triggers an automated build and test run. Catches bugs within minutes. In this project, Jenkins polls GitHub every 5 minutes, builds the WAR, and runs 39 tests.  
**CD (Continuous Delivery/Deployment):** after CI passes, the artefact (Docker image) is automatically pushed to the registry and deployed. In this project, Jenkins pushes a versioned image and runs `docker compose up`, so every green build is live.

**Q35. What is idempotency and why is it important in DevOps?**  
An operation is idempotent if running it once or ten times produces the same result. In Ansible, `state: present` for packages and users means the task does nothing if the resource already exists. Idempotency is critical because it makes automation safe to re-run (no accidental duplicates, no drift), and it makes `changed=0` a reliable indicator that the target is already in the desired state.

**Q36. What is Docker's role vs Ansible's role in this project?**  
**Docker:** packages the application and its dependencies into an image; Docker Compose manages the multi-container stack (app + database) at runtime.  
**Ansible:** manages the *infrastructure* — creating system users, directories, config files, firewall rules, and orchestrating which image version is running. Ansible ensures every server is configured identically regardless of who set it up.

**Q37. How do you prevent secrets from leaking into Git?**  
Three layers: (1) `.gitignore` excludes `.env` files; (2) The `.env` is rendered by Ansible from a template — the password variable is supplied at runtime, never in a file that's committed; (3) `no_log: true` on the Ansible template task prevents the password appearing in logs. For production, Ansible Vault encrypts the password file.

**Q38. What happens to data when you roll back the application?**  
The PostgreSQL data volume (`pgdata`) is a Docker named volume, separate from the application container. Rolling back only replaces the Tomcat container image — the volume is mounted at the same path as before. Existing parcel records and events are preserved. The only risk is a schema mismatch if the old code doesn't understand V2 columns — in this project, `weight_kg` has a default so V1 code ignores it safely.

**Q39. Explain the test pyramid in this project.**  
- **Unit tests (33):** `ParcelServiceValidationTest` and `ApiHelperTest` — test validation logic and JSON/CSV helpers with an in-memory fake DAO. No database, no servlet container, runs in under 0.3 seconds.
- **Integration tests (6):** `ParcelPortalSeleniumTest` — drives a real Chrome browser against the running Docker stack. Tests the full stack: browser → Tomcat → PostgreSQL.
- **CI gate:** Jenkins runs the full pyramid. If a unit test fails (fastest feedback), Selenium tests don't run. If a Selenium test fails, the image is not pushed.

**Q40. If the professor asks "Your project was 'too basic' and 'no database' — what changed?"**  
Previously the application used H2 (in-memory, wiped on restart) and had only 5 fields per parcel. Now:
- **Database:** PostgreSQL 16 in Docker, data persists across restarts, Flyway manages schema evolution
- **Richer data model:** parcels now have weight, priority, expected delivery date, assigned agent — validated both in the DB (CHECK constraints) and in Java (service layer, 17 unit tests)
- **REST API:** 5 JSON endpoints + CSV export — the app is now a real service, not just HTML forms
- **Public tracking page:** customers can track parcels without login
- **39 automated tests** (was ~3) covering unit, integration, and Selenium layers
- **Full CI/CD pipeline:** Jenkins builds → tests → versions → pushes → deploys automatically
- **Configuration management:** Ansible provisions infrastructure, deploys versioned images, rolls back, backs up and restores the database

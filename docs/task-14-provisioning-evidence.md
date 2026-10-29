# Task 14 – Provisioning, Idempotency, Health Check, Rollback
## Evidence Document

**Branch:** `ops/ansible-postgres`  
**Date:** 29 October 2026

---

## 1. Provisioning

The `site.yml` master playbook provisions the complete infrastructure before deploying:

| Step | Role | What It Creates |
|------|------|----------------|
| 1 | packages | Docker + curl + python3-docker |
| 2 | service-user | `parcel` system user + group |
| 3 | app-dirs | `/opt/parcel-tracking-portal/`, `/etc/parcel-tracking-portal/`, `/var/lib/parcel-tracking-portal/`, `/var/backups/parcel-tracking-portal/` |
| 4 | app-config | `/etc/parcel-tracking-portal/.env` (mode 640, secrets from vault) |
| 5 | firewall | ufw allow 8082, 5000, 5434 |
| 6 | deploy | Pull image + docker compose up |
| 7 | health-check | Poll /health until OK |

---

## 2. Idempotency Proof

**Run 1 (changes expected):**
```
PLAY RECAP
localhost : ok=14  changed=8  unreachable=0  failed=0
```

**Run 2 (no changes – idempotency confirmed):**
```
PLAY RECAP
localhost : ok=14  changed=0  unreachable=0  failed=0
```

Modules used that guarantee idempotency:
- `ansible.builtin.apt` with `state: present`
- `ansible.builtin.user` / `ansible.builtin.group` with `state: present`
- `ansible.builtin.file` with `state: directory`
- `ansible.builtin.template` (only writes if content changes – checksums)
- `ansible.builtin.copy` (only writes if content differs)
- `docker compose up -d` (Compose compares image digests)

---

## 3. Health Check

The `health-check` role uses `ansible.builtin.uri` with retry logic:

```yaml
- name: Wait for application to be healthy
  ansible.builtin.uri:
    url: "http://localhost:{{ app_port }}/parcel-tracking-portal/health"
    return_content: true
    status_code: 200
  register: health_result
  until: health_result.status == 200 and health_result.content | trim == 'OK'
  retries: 20
  delay: 5
```

- Up to 20 retries × 5s = 100 seconds maximum wait
- Fails the entire play if the app does not become healthy → **deployment gate**
- `/health` endpoint in `HealthServlet.java` returns HTTP 200 `OK` only when the DB connection pool is active

---

## 4. Rollback Procedure

### Prerequisites
Tags `1.0.5` and `1.0.7` are preserved in `localhost:5000` for the rollback demo:
```bash
curl http://localhost:5000/v2/parcel-tracking-portal/tags/list
# {"name":"parcel-tracking-portal","tags":["1.0.5","1.0.7","1.1.42","1.1.50"]}
```

### Rollback Command
```bash
ansible-playbook -i ansible/inventory.ini ansible/rollback.yml \
  -e rollback_tag=1.0.5 \
  -e postgres_password=parcel_pass
```

### Rollback Play Sequence
1. **Read** `active_release` → e.g. `1.1.50`
2. **Assert** `rollback_tag (1.0.5) ≠ current_release (1.1.50)` → passes
3. **Show plan:** "Rolling back from 1.1.50 to 1.0.5"
4. **Render** `.env` with `release_tag=1.0.5`
5. **Pull** `localhost:5000/parcel-tracking-portal:1.0.5`
6. **docker compose up** → Compose recreates `parcel-app` with old image
7. **Health check** → polls until `/health` returns OK
8. **Update** `/opt/parcel-tracking-portal/active_release` → `1.0.5`
9. **Show:** "Rollback successful. DB volume was NOT modified."

### Expected Output
```
TASK [Show rollback plan] **********
ok: [localhost] => {"msg": "Rolling back from 1.1.50 to 1.0.5"}

TASK [deploy : Pull exact image tag 1.0.5] **********
ok: [localhost]

TASK [deploy : Bring up Docker Compose stack] **********
changed: [localhost]

TASK [health-check : Wait for application to be healthy] **********
ok: [localhost]

TASK [Rollback complete] **********
ok: [localhost] => {"msg": "Rollback successful. Running tag: 1.0.5. Previous: 1.1.50. DB NOT modified."}

PLAY RECAP
localhost : ok=8  changed=1  unreachable=0  failed=0
```

---

## 5. Database Backup and Restore

### Backup
```bash
ansible-playbook -i ansible/inventory.ini ansible/backup.yml
# Creates: /var/backups/parcel-tracking-portal/parcels_20261029_143000.sql.gz
# Deletes: backups older than 7 days
```

### Restore
```bash
ansible-playbook -i ansible/inventory.ini ansible/restore.yml \
  -e backup_file=/var/backups/parcel-tracking-portal/parcels_20261029_143000.sql.gz
```

---

## Screenshots Required for Viva

Capture and add to `docs/screenshots/task-14/`:
- Rollback: terminal showing the full rollback play (green output)
- After rollback: `curl /health` → OK + `cat active_release` → 1.0.5
- Backup: terminal showing backup created + file size
- Restore: terminal showing restore completed

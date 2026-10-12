# Task 14: Provisioning and Reliability Runbook

Task 14 uses the Task 13 Ansible configuration as the prerequisite layer and
adds release deployment, health validation, idempotence and rollback.

## Provision a clean target

Run the prerequisite playbook first:

```bash
sudo ansible-playbook -i inventory.ini site.yml -e ansible_become=false
```

The target must have Docker Desktop/WSL integration available. For a normal
Linux host, set `docker_executable=/usr/bin/docker`; for Docker Desktop CLI
integration, pass the executable path appropriate to that host.

## Deploy a release

```bash
sudo ansible-playbook -i inventory.ini deploy.yml \
  -e ansible_become=false \
  -e docker_executable=/usr/bin/docker \
  -e release_tag=1.0.7
```

The playbook pulls the exact image, reconciles the container only when its
image differs, waits for the health endpoint to return `OK`, and records the active image in
`/etc/parcel-tracking-portal/active-release`.

## Idempotence evidence

Run the same deployment twice. The second run should complete successfully
with the container replacement task skipped because the requested image is
already running. The prerequisite resources and active-release file should
also remain unchanged.

## Rollback and recovery

```bash
sudo ansible-playbook -i inventory.ini rollback.yml \
  -e ansible_become=false \
  -e docker_executable=/usr/bin/docker \
  -e rollback_tag=1.0.7
```

Verify the recovery:

```bash
docker ps --filter name=parcel-portal-cd
cat /etc/parcel-tracking-portal/active-release
curl http://127.0.0.1:8082/parcel-tracking-portal/health
```

Expected health output is `OK`, and the active release should be the stable
rollback image.

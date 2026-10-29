# Task 13: Configuration Specification

The target node is an Ubuntu/Debian Linux host running the Parcel Tracking
Portal container workload. The Ansible playbook is intentionally idempotent:
re-running it should report no changes after the first successful execution.

| Category | Required state |
|---|---|
| Packages | `ca-certificates`, `curl`, `docker.io`, `maven`, `openjdk-17-jre-headless` |
| User and group | System user/group `parcel`; shell `/usr/sbin/nologin` |
| Application folder | `/opt/parcel-tracking-portal`, owned by `parcel:parcel`, mode `0755` |
| Configuration folder | `/etc/parcel-tracking-portal`, owned by `parcel:parcel`, mode `0750` |
| Data folder | `/var/lib/parcel-tracking-portal`, owned by `parcel:parcel`, mode `0750` |
| Configuration file | `/etc/parcel-tracking-portal/application.properties`, mode `0640` |
| Application port | TCP `8082` |
| Tomcat port | TCP `8080` |
| Registry port | TCP `5000` |
| Service | Docker enabled at boot and started when the target exposes `docker.service`; WSL/Docker Desktop targets use the already-running Docker Desktop engine |
| Firewall | TCP ports `8082`, `8080`, and `5000` allowed on Debian-family hosts |

## Inventory and execution

The local inventory targets `127.0.0.1` so the playbook can be demonstrated
inside Ubuntu on WSL. For a remote node, replace `ansible_host` and provide
the SSH user/key in the inventory or an Ansible Vault.

```bash
cd ansible
ansible-playbook -i inventory.ini site.yml --check --diff
ansible-playbook -i inventory.ini site.yml
ansible-playbook -i inventory.ini site.yml
```

The first run is the configuration execution evidence. The second normal run
should show the resources are already present and demonstrate idempotence.

On WSL with Docker Desktop integration, Ubuntu may not expose a native
`docker.service`. The playbook still installs the Docker package and reports
that the external Docker Desktop engine is being used. On a normal Ubuntu
server, the playbook enables and starts `docker.service`.

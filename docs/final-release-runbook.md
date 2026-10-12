# Final End-to-End Release and Viva Runbook

## 1. Release objective

Demonstrate the complete Parcel Tracking Portal workflow from a committed
change through Jenkins, Maven, Selenium, Docker, Ansible provisioning,
deployment, health validation and recovery.

## 2. Architecture summary

The Java 17 Jakarta Servlet/JSP WAR is built with Maven and deployed to
Tomcat 10.1. Jenkins checks out the repository, builds the WAR, runs the
Selenium quality gate, builds a versioned Docker image, publishes it to the
local registry and deploys a fresh container. Ansible provisions the target
identity, packages, directories and configuration, then reconciles the
versioned container and validates `/parcel-tracking-portal/health`.

The normal local ports are Jenkins `8080`, Tomcat `8081`, the containerized
portal `8082`, and the local registry `5000`.

## 3. Final demonstration sequence

1. Show the GitHub repository, `develop` branch and the task branches.
2. Create or select a final commit and push it to GitHub.
3. Run the Jenkins job with `local-tomcat`, registry `localhost:5000`,
   repository `parcel-tracking-portal`, container `parcel-portal-cd` and host
   port `8082`.
4. Show successful Maven build, Selenium tests, Docker build, registry
   publication and container deployment stages.
5. Show the archived WAR and the versioned registry tag.
6. Verify:

   ```powershell
   curl.exe http://localhost:8082/parcel-tracking-portal/health
   ```

   Expected response: `OK`.
7. In Ubuntu/WSL, run the Ansible provisioning playbook, deployment playbook
   and a second deployment to show the skipped replacement task.
8. Run the rollback playbook and show the recovered `OK` health response.

## 4. Evidence checklist

Capture screenshots or a short screen recording of:

- Git commit and pushed branch
- Jenkins successful pipeline overview
- Selenium test results and published Surefire report
- Docker image tag, registry tags and running container
- Portal browser page and health endpoint
- Ansible first execution and idempotent rerun
- Failed candidate release and rollback recovery
- Final GitHub pull request and merged `develop` history

Store final screenshots in `docs/screenshots/task-15/` and the recording or
presentation source in the course submission location. Do not commit large
video files unless repository policy explicitly permits them.

## 5. Troubleshooting guide

| Symptom | Check | Resolution |
|---|---|---|
| Jenkins cannot find Docker | Jenkins service PATH | Set the Docker executable parameter to the installed Docker Desktop CLI path |
| Health probe returns an empty response | Tomcat startup logs | Use the pipeline retry probe and inspect `docker logs parcel-portal-cd` |
| Selenium cannot connect | Tomcat and Chrome | Start Tomcat, verify the base URL and keep the browser driver compatible |
| Ansible asks for sudo credentials | WSL local connection | Use the documented `sudo ansible-playbook ... -e ansible_become=false` command |
| `docker.service` is unavailable in WSL | `systemctl status docker` | Use Docker Desktop/WSL integration; do not expect a native service |
| Registry tag is missing | Registry tag list | Confirm Jenkins published the exact `1.0.<build>` tag before deployment |
| Port is already allocated | `docker ps` and `Get-NetTCPConnection` | Stop the conflicting process/container or use the documented port mapping |

## 6. Current limitations

- The local registry is not a production artifact repository.
- The WSL demonstration depends on Docker Desktop integration.
- The deployment uses a single container and has no traffic switch or replica
  set.
- Secrets and production TLS are outside the coursework environment.
- Selenium coverage focuses on the principal parcel workflows rather than
  full browser and accessibility compatibility.

## 7. Future enhancements

1. Replace the local registry with a secured artifact repository.
2. Add image signing, vulnerability scanning and SBOM publication.
3. Use a managed database with backups and migration automation.
4. Deploy multiple replicas behind a reverse proxy with zero-downtime rollout.
5. Add centralized logs, metrics, alerting and distributed tracing.
6. Add role-based access control, audit trails and production TLS.
7. Run the pipeline on Linux agents and test a matrix of supported browsers.

## 8. Viva questions to rehearse

- Why is the WAR packaged separately from the Docker image?
- What is the difference between CI, continuous testing and continuous
  deployment in this project?
- Why are versioned image tags safer than deploying `latest`?
- How does the Selenium stage act as a deployment gate?
- What makes the Ansible configuration repeatable and idempotent?
- Why is Docker service management skipped on this WSL target?
- How does the rollback playbook restore a stable release?
- What would change before deploying this system to production?

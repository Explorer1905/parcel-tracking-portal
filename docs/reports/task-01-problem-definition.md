# Task 1: Problem Definition and Scope

**Project:** CI/CD Pipeline for a Parcel Tracking Portal
**Name:** Shravani Chavan
**Roll No:** 23102A0055
**Class:** CMPN A
**Branch:** `task/01-problem-definition`
**Date:** <fill in today's date>

---

## 1. Objective

To study the real-time need for a Parcel Tracking Portal, identify its users, pain points, stakeholders, constraints and measurable success criteria, and freeze a small 15-task MVP scope that will be delivered through a complete CI/CD pipeline.

## 2. Problem Statement

Customers and courier staff often have no single, reliable place to see the live status of a parcel. Customers repeatedly contact support to ask "where is my parcel?", while staff update statuses manually in scattered sheets or messages. Delayed, stuck or failed deliveries are noticed late, which leads to poor customer experience and extra support workload.

The Parcel Tracking Portal solves this by giving one web application where parcel events are recorded, searched and monitored, with delayed or exceptional parcels flagged automatically.

## 3. Target Users

| User | Need |
|---|---|
| Customer | Check the current status and history of a parcel quickly |
| Delivery agent | Update parcel events (picked up, in transit, delivered, failed) |
| Warehouse staff | Record arrival and dispatch of parcels |
| Admin / support team | Monitor all parcels, see delays and handle exceptions |

## 3.1 Existing Pain Points

- No real-time visibility of parcel status.
- Status updates are manual and inconsistent.
- No automatic alert for delayed or failed deliveries.
- Parcel data is scattered across sheets, messages and calls.
- High number of repeated status queries to support.
- No quick summary of how many parcels are delivered, in transit or delayed.

## 4. Stakeholders

- Customers (end users of tracking)
- Courier company management
- Warehouse and delivery teams
- Customer support team
- Developers / DevOps team (project team)
- Course instructor / evaluator

## 5. Constraints

- Academic timeline: 15 tasks to be completed in sequence.
- Only free and open-source tools (Git, GitHub, Maven, Jenkins, Docker, Selenium, Ansible).
- Development and testing on a single laptop environment.
- No integration with a real courier API or live GPS; data is entered manually or simulated.
- Small MVP: focus is on the DevOps pipeline, not on a large application.

## 6. Measurable Success Criteria

| # | Criterion | How it is measured |
|---|---|---|
| 1 | Parcel search returns a result in under 2 seconds | Manual timing / Selenium test |
| 2 | Full event history of a parcel is visible on one screen | Status drill-down page check |
| 3 | Delayed or failed parcels are flagged on the dashboard automatically | Alert/exception view check |
| 4 | Build, test and deploy run automatically on every commit | Jenkins pipeline run |
| 5 | At least 80% of Selenium tests pass on each build | Jenkins test report |
| 6 | Application runs identically in a Docker container | Container run and health check |
| 7 | Environment can be rebuilt using a Puppet/Ansible script | Re-run shows idempotent result |

## 7. Frozen MVP Scope

### In scope
- Parcel data / event entry
- Searchable dashboard
- Summary indicators (total, in transit, delivered, delayed)
- Status drill-down (event history per parcel)
- Alert / exception view for delayed or failed parcels

### Out of scope
- Real GPS or live map tracking
- Payments and billing
- Mobile application
- Real courier API integration
- Email/SMS notifications

## 8. Technology Stack (planned)

| Area | Tool |
|---|---|
| Application | Java Servlets/JSP |
| Build | Maven |
| Version control | Git and GitHub |
| CI / CD | Jenkins (Jenkinsfile) |
| Server | Apache Tomcat |
| Testing | Selenium WebDriver with JUnit |
| Containers | Docker |
| Configuration management | Ansible |

## 9. 15-Task Plan

| Task | Title | Branch |
|---|---|---|
| 1 | Problem Definition and Scope | `task/01-problem-definition` |
| 2 | Agile Planning and DevOps Workflow | `task/02-agile-planning` |
| 3 | Requirements, Architecture and Technology Setup | `task/03-architecture-setup` |
| 4 | Git and GitHub Repository Initialization | `task/04-repo-init` |
| 5 | Feature Development with Branching | `task/05-feature-branching` |
| 6 | MVP Completion and Git Collaboration | `task/06-mvp-completion` |
| 7 | Jenkins Installation and Continuous Integration Job | `task/07-jenkins-ci` |
| 8 | Pipeline as Code and Server Deployment | `task/08-pipeline-as-code` |
| 9 | Selenium Test Design and Local Execution | `task/09-selenium-tests` |
| 10 | Continuous Testing in Jenkins | `task/10-continuous-testing` |
| 11 | Docker Image and Container Lifecycle | `task/11-docker` |
| 12 | Jenkins-Docker Continuous Deployment | `task/12-jenkins-docker-cd` |
| 13 | Configuration Management Script | `task/13-config-management` |
| 14 | Automated Provisioning and Reliability Validation | `task/14-provisioning` |
| 15 | Final End-to-End Release, Documentation and Viva | `task/15-final-release` |

## 10. Steps Performed

1. Studied the real-time tracking need and listed users and pain points.
2. Identified stakeholders and constraints.
3. Defined measurable success criteria.
4. Froze the MVP scope and the 15-task plan.
5. Created branch `task/01-problem-definition` from `develop`.
6. Wrote this report, committed it and raised a pull request into `develop`.

## 11. Screenshots

- Branch list showing `main`, `develop` and `task/01-problem-definition`
- Commit history for this task
- Merged pull request on GitHub

(Saved in `docs/screenshots/task-01/`)

## 12. Deliverables Checklist

- [x] Problem statement
- [x] Target users and pain points
- [x] Stakeholders and constraints
- [x] Measurable success criteria
- [x] Frozen 15-task MVP scope

## 13. Issues Faced and Fixes

- Linux-style `mkdir -p` failed in Windows PowerShell; used `mkdir docs\reports` instead.
- Clone command was entered with a placeholder username; corrected with the real GitHub username.

## 14. Conclusion

The problem, users, scope and success criteria of the Parcel Tracking Portal are now clearly defined and frozen. This gives a stable base for Agile planning in Task 2 and for building the CI/CD pipeline in the later tasks.
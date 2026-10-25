import sys, glob, os
from datetime import date
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

NAME, ROLL, CLASS = "Shravani Chavan", "23102A0055", "CMPN A"
PROJECT = "CI/CD Pipeline for a Parcel Tracking Portal"

# ---------- helpers ----------
def shade(cell, color):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear"); shd.set(qn("w:color"), "auto"); shd.set(qn("w:fill"), color)
    tcPr.append(shd)

def add_table(doc, header, rows):
    t = doc.add_table(rows=1, cols=len(header))
    t.style = "Table Grid"
    for i, h in enumerate(header):
        c = t.rows[0].cells[i]; c.text = ""
        r = c.paragraphs[0].add_run(h); r.bold = True
        shade(c, "D9E2F3")
    for row in rows:
        cells = t.add_row().cells
        for i, v in enumerate(row):
            cells[i].text = str(v)
    doc.add_paragraph()

def add_screenshots(doc, task_no):
    folder = f"docs/screenshots/task-{task_no:02d}"
    files = sorted(glob.glob(folder + "/*.png") + glob.glob(folder + "/*.jpg"))
    if not files:
        doc.add_paragraph(f"(No screenshots found in {folder}. Add images there and re-run.)")
        return
    for i, f in enumerate(files, 1):
        doc.add_picture(f, width=Inches(6))
        doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
        cap = doc.add_paragraph(f"Figure {i}: {os.path.basename(f)}")
        cap.alignment = WD_ALIGN_PARAGRAPH.CENTER

def build(task_no, title, branch, blocks, filename):
    doc = Document()
    st = doc.styles["Normal"]; st.font.name = "Calibri"; st.font.size = Pt(11)

    h = doc.add_heading(f"Task {task_no}: {title}", 0)
    h.alignment = WD_ALIGN_PARAGRAPH.CENTER

    add_table(doc, ["Field", "Details"], [
        ["Project", PROJECT], ["Name", NAME], ["Roll No", ROLL], ["Class", CLASS],
        ["Branch", branch], ["Date", date.today().strftime("%d %B %Y")],
    ])

    for b in blocks:
        kind = b[0]
        if kind == "h":
            doc.add_heading(b[1], 1)
        elif kind == "h2":
            doc.add_heading(b[1], 2)
        elif kind == "p":
            doc.add_paragraph(b[1])
        elif kind == "bullets":
            for x in b[1]: doc.add_paragraph(x, style="List Bullet")
        elif kind == "numbered":
            for x in b[1]: doc.add_paragraph(x, style="List Number")
        elif kind == "check":
            for x in b[1]: doc.add_paragraph("\u2611 " + x)
        elif kind == "table":
            add_table(doc, b[1], b[2])
        elif kind == "code":
            p = doc.add_paragraph(); r = p.add_run(b[1])
            r.font.name = "Consolas"; r.font.size = Pt(9.5)
        elif kind == "screenshots":
            add_screenshots(doc, task_no)

    os.makedirs("docs/reports", exist_ok=True)
    out = f"docs/reports/{filename}.docx"
    doc.save(out)
    print("Created:", out)

# ---------- TASK CONTENT ----------
TASKS = {}

TASKS[1] = dict(
    title="Problem Definition and Scope",
    branch="task/01-problem-definition",
    filename="task-01-problem-definition",
    blocks=[
        ("h", "1. Objective"),
        ("p", "To study the real-time need for a Parcel Tracking Portal, identify its users, pain points, stakeholders, constraints and measurable success criteria, and freeze a small 15-task MVP scope that will be delivered through a complete CI/CD pipeline."),
        ("h", "2. Problem Statement"),
        ("p", "Customers and courier staff often have no single, reliable place to see the live status of a parcel. Customers repeatedly contact support to ask where their parcel is, while staff update statuses manually in scattered sheets or messages. Delayed, stuck or failed deliveries are noticed late, which leads to poor customer experience and extra support workload."),
        ("p", "The Parcel Tracking Portal solves this by giving one web application where parcel events are recorded, searched and monitored, with delayed or exceptional parcels flagged automatically."),
        ("h", "3. Target Users"),
        ("table", ["User", "Need"], [
            ["Customer", "Check the current status and history of a parcel quickly"],
            ["Delivery agent", "Update parcel events (picked up, in transit, delivered, failed)"],
            ["Warehouse staff", "Record arrival and dispatch of parcels"],
            ["Admin / support team", "Monitor all parcels, see delays and handle exceptions"],
        ]),
        ("h2", "3.1 Existing Pain Points"),
        ("bullets", [
            "No real-time visibility of parcel status.",
            "Status updates are manual and inconsistent.",
            "No automatic alert for delayed or failed deliveries.",
            "Parcel data is scattered across sheets, messages and calls.",
            "High number of repeated status queries to support.",
            "No quick summary of delivered, in-transit and delayed parcels.",
        ]),
        ("h", "4. Stakeholders"),
        ("bullets", [
            "Customers (end users of tracking)",
            "Courier company management",
            "Warehouse and delivery teams",
            "Customer support team",
            "Developers / DevOps team (project team)",
            "Course instructor / evaluator",
        ]),
        ("h", "5. Constraints"),
        ("bullets", [
            "Academic timeline: 15 tasks to be completed in sequence.",
            "Only free and open-source tools (Git, GitHub, Maven, Jenkins, Docker, Selenium, Ansible).",
            "Development and testing on a single laptop environment.",
            "No integration with a real courier API or live GPS; data is entered manually or simulated.",
            "Small MVP: the focus is on the DevOps pipeline, not on a large application.",
        ]),
        ("h", "6. Measurable Success Criteria"),
        ("table", ["#", "Criterion", "How it is measured"], [
            ["1", "Parcel search returns a result in under 2 seconds", "Manual timing / Selenium test"],
            ["2", "Full event history of a parcel visible on one screen", "Status drill-down page check"],
            ["3", "Delayed or failed parcels flagged on the dashboard automatically", "Alert/exception view check"],
            ["4", "Build, test and deploy run automatically on every commit", "Jenkins pipeline run"],
            ["5", "At least 80% of Selenium tests pass on each build", "Jenkins test report"],
            ["6", "Application runs identically in a Docker container", "Container run and health check"],
            ["7", "Environment can be rebuilt using an Ansible/Puppet script", "Re-run shows idempotent result"],
        ]),
        ("h", "7. Frozen MVP Scope"),
        ("h2", "In scope"),
        ("bullets", [
            "Parcel data / event entry",
            "Searchable dashboard",
            "Summary indicators (total, in transit, delivered, delayed)",
            "Status drill-down (event history per parcel)",
            "Alert / exception view for delayed or failed parcels",
        ]),
        ("h2", "Out of scope"),
        ("bullets", [
            "Real GPS or live map tracking",
            "Payments and billing",
            "Mobile application",
            "Real courier API integration",
            "Email/SMS notifications",
        ]),
        ("h", "8. Technology Stack (planned)"),
        ("table", ["Area", "Tool"], [
            ["Application", "Java Servlets/JSP"],
            ["Build", "Maven"],
            ["Version control", "Git and GitHub"],
            ["CI / CD", "Jenkins (Jenkinsfile)"],
            ["Server", "Apache Tomcat"],
            ["Testing", "Selenium WebDriver with JUnit"],
            ["Containers", "Docker"],
            ["Configuration management", "Ansible"],
        ]),
        ("h", "9. 15-Task Plan"),
        ("table", ["Task", "Title", "Branch"], [
            ["1", "Problem Definition and Scope", "task/01-problem-definition"],
            ["2", "Agile Planning and DevOps Workflow", "task/02-agile-planning"],
            ["3", "Requirements, Architecture and Technology Setup", "task/03-architecture-setup"],
            ["4", "Git and GitHub Repository Initialization", "task/04-repo-init"],
            ["5", "Feature Development with Branching", "task/05-feature-branching"],
            ["6", "MVP Completion and Git Collaboration", "task/06-mvp-completion"],
            ["7", "Jenkins Installation and Continuous Integration Job", "task/07-jenkins-ci"],
            ["8", "Pipeline as Code and Server Deployment", "task/08-pipeline-as-code"],
            ["9", "Selenium Test Design and Local Execution", "task/09-selenium-tests"],
            ["10", "Continuous Testing in Jenkins", "task/10-continuous-testing"],
            ["11", "Docker Image and Container Lifecycle", "task/11-docker"],
            ["12", "Jenkins-Docker Continuous Deployment", "task/12-jenkins-docker-cd"],
            ["13", "Configuration Management Script", "task/13-config-management"],
            ["14", "Automated Provisioning and Reliability Validation", "task/14-provisioning"],
            ["15", "Final End-to-End Release, Documentation and Viva", "task/15-final-release"],
        ]),
        ("h", "10. Steps Performed"),
        ("numbered", [
            "Studied the real-time tracking need and listed users and pain points.",
            "Identified stakeholders and constraints.",
            "Defined measurable success criteria.",
            "Froze the MVP scope and the 15-task plan.",
            "Created branch task/01-problem-definition from develop.",
            "Wrote the report, committed it and raised a pull request into develop.",
        ]),
        ("h", "11. Git Commands Used"),
        ("code", "git checkout develop\ngit checkout -b task/01-problem-definition\ngit add docs\ngit commit -m \"docs: add Task 1 problem definition and scope report\"\ngit push -u origin task/01-problem-definition"),
        ("h", "12. Screenshots"),
        ("screenshots",),
        ("h", "13. Deliverables Checklist"),
        ("check", [
            "Problem statement",
            "Target users and pain points",
            "Stakeholders and constraints",
            "Measurable success criteria",
            "Frozen 15-task MVP scope",
        ]),
        ("h", "14. Issues Faced and Fixes"),
        ("bullets", [
            "Linux-style mkdir -p failed in Windows PowerShell; used mkdir docs\\reports instead.",
            "Clone command was entered with a placeholder username; corrected with the real GitHub username.",
        ]),
        ("h", "15. Conclusion"),
        ("p", "The problem, users, scope and success criteria of the Parcel Tracking Portal are now clearly defined and frozen. This gives a stable base for Agile planning in Task 2 and for building the CI/CD pipeline in the later tasks."),
    ],
)

TASKS[2] = dict(
    title="Agile Planning and DevOps Workflow",
    branch="task/02-agile-planning",
    filename="task-02-agile-planning",
    blocks=[
        ("h", "1. Objective"),
        ("p", "To create user stories and acceptance criteria for the Parcel Tracking Portal, prepare a product backlog, define a 15-task Kanban plan and a Definition of Done, and show the DevOps lifecycle from development to operations."),
        ("h", "2. Agile Approach"),
        ("p", "Kanban is used because the project is a fixed sequence of 15 tasks delivered by one person. Work is visualised on a GitHub Projects board with the columns Backlog, To Do, In Progress, In Review and Done. A work-in-progress (WIP) limit of one task In Progress at a time keeps the flow steady."),
        ("h", "3. User Stories and Acceptance Criteria"),
        ("table", ["ID", "User story", "Acceptance criteria"], [
            ["US-01", "As a warehouse staff member, I want to add a new parcel so that it can be tracked.", "Form accepts parcel ID, sender, receiver, origin and destination. Duplicate parcel IDs are rejected. A success message is shown after saving."],
            ["US-02", "As a delivery agent, I want to add a status event to a parcel so that its progress is recorded.", "Status can be Picked Up, In Transit, Out for Delivery, Delivered or Failed. Each event stores a timestamp and location. The latest status updates on the dashboard."],
            ["US-03", "As a customer, I want to search a parcel by ID so that I can see its current status.", "Search returns the result in under 2 seconds. An unknown ID shows a clear 'not found' message."],
            ["US-04", "As a support agent, I want a dashboard listing all parcels so that I can monitor them.", "Dashboard lists all parcels with ID, status and last update. The list can be filtered by status."],
            ["US-05", "As a manager, I want summary indicators so that I can see overall performance.", "Counts of total, in transit, delivered and delayed parcels are shown and match the stored data."],
            ["US-06", "As a customer, I want to open a parcel and see its full history so that I know what happened.", "Drill-down page shows all events in time order on one screen."],
            ["US-07", "As a support agent, I want delayed or failed parcels flagged so that I can act quickly.", "A parcel with no update beyond the set limit, or with status Failed, appears in the alert view."],
            ["US-08", "As a developer, I want every commit built and tested automatically so that defects are found early.", "A Jenkins build is triggered on commit. A failed test stops deployment. Reports are archived."],
            ["US-09", "As an operations engineer, I want the portal deployed in a container and provisioned by script so that environments are repeatable.", "The Docker image runs the portal. The Ansible/Puppet script builds the environment and a second run changes nothing."],
        ]),
        ("h", "4. Product Backlog"),
        ("table", ["ID", "Backlog item", "Priority", "Effort", "Linked task"], [
            ["B-01", "Problem definition and scope", "High", "S", "Task 1"],
            ["B-02", "Agile plan, backlog and workflow", "High", "S", "Task 2"],
            ["B-03", "Architecture and technology setup", "High", "M", "Task 3"],
            ["B-04", "Repository initialization and conventions", "High", "S", "Task 4"],
            ["B-05", "Parcel entry and event entry (US-01, US-02)", "High", "M", "Task 5"],
            ["B-06", "Search, dashboard, summary, drill-down, alerts (US-03 to US-07)", "High", "L", "Task 6"],
            ["B-07", "Jenkins installation and CI job (US-08)", "High", "M", "Task 7"],
            ["B-08", "Jenkinsfile and deployment to Tomcat", "High", "M", "Task 8"],
            ["B-09", "Selenium test cases for three user journeys", "High", "M", "Task 9"],
            ["B-10", "Selenium tests integrated in Jenkins", "Medium", "M", "Task 10"],
            ["B-11", "Dockerfile and container lifecycle (US-09)", "Medium", "M", "Task 11"],
            ["B-12", "Jenkins to Docker continuous deployment", "Medium", "L", "Task 12"],
            ["B-13", "Ansible/Puppet configuration script", "Medium", "M", "Task 13"],
            ["B-14", "Automated provisioning and health check", "Medium", "M", "Task 14"],
            ["B-15", "Final release, documentation and viva", "High", "M", "Task 15"],
        ]),
        ("p", "Effort key: S = small, M = medium, L = large."),
        ("h", "5. 15-Task Kanban Plan"),
        ("table", ["Task", "Title", "Phase", "Board column (at start)"], [
            ["1", "Problem Definition and Scope", "Plan", "Done"],
            ["2", "Agile Planning and DevOps Workflow", "Plan", "In Progress"],
            ["3", "Requirements, Architecture and Technology Setup", "Plan", "To Do"],
            ["4", "Git and GitHub Repository Initialization", "Code", "To Do"],
            ["5", "Feature Development with Branching", "Code", "Backlog"],
            ["6", "MVP Completion and Git Collaboration", "Code", "Backlog"],
            ["7", "Jenkins Installation and CI Job", "Build", "Backlog"],
            ["8", "Pipeline as Code and Server Deployment", "Build / Deploy", "Backlog"],
            ["9", "Selenium Test Design and Local Execution", "Test", "Backlog"],
            ["10", "Continuous Testing in Jenkins", "Test", "Backlog"],
            ["11", "Docker Image and Container Lifecycle", "Release", "Backlog"],
            ["12", "Jenkins-Docker Continuous Deployment", "Deploy", "Backlog"],
            ["13", "Configuration Management Script", "Operate", "Backlog"],
            ["14", "Automated Provisioning and Reliability Validation", "Operate / Monitor", "Backlog"],
            ["15", "Final End-to-End Release, Documentation and Viva", "Release / Monitor", "Backlog"],
        ]),
        ("h2", "Kanban rules"),
        ("bullets", [
            "Only one task is In Progress at a time (WIP limit = 1).",
            "A task moves to In Review when its pull request is opened.",
            "A task moves to Done only when it meets the Definition of Done.",
            "Each task has its own branch and its own report.",
        ]),
        ("h", "6. Definition of Done"),
        ("p", "A task is Done only when all of the following are true:"),
        ("check", [
            "Work is committed on its own task branch with meaningful commit messages.",
            "Code or configuration builds and runs without errors.",
            "Acceptance criteria of the related user stories are met.",
            "Tests (where applicable) pass.",
            "Pull request is raised, reviewed and merged into develop.",
            "The branch is kept for evidence and not deleted.",
            "Task report (Word) is written with commands, output and screenshots.",
            "The Kanban card is moved to Done.",
        ]),
        ("h", "7. DevOps Lifecycle: Development to Operations"),
        ("p", "The Parcel Tracking Portal follows a continuous loop. Code changes flow from planning through build, test, release and deployment into operations, and monitoring feedback returns to planning."),
        ("table", ["Stage", "Activity in this project", "Tool"], [
            ["1. Plan", "User stories, backlog, Kanban board", "GitHub Projects, Issues"],
            ["2. Code", "Feature branches, commits, pull requests", "Git, GitHub, VS Code"],
            ["3. Build", "Compile and package the WAR file", "Maven, Jenkins"],
            ["4. Test", "Automated UI tests on every build", "Selenium, JUnit, Jenkins"],
            ["5. Release", "Versioned Docker image", "Docker, Docker Hub"],
            ["6. Deploy", "Automatic deployment to Tomcat or container", "Jenkins, Docker"],
            ["7. Operate", "Provision and configure servers", "Ansible / Puppet"],
            ["8. Monitor", "Health check, test reports, feedback to Plan", "Jenkins reports, curl health check"],
        ]),
        ("code", "PLAN -> CODE -> BUILD -> TEST -> RELEASE -> DEPLOY -> OPERATE -> MONITOR\n  ^                                                                |\n  +------------------------- feedback loop ------------------------+"),
        ("h", "8. Steps Performed"),
        ("numbered", [
            "Created branch task/02-agile-planning from develop.",
            "Wrote user stories with acceptance criteria.",
            "Prepared the product backlog and mapped it to the 15 tasks.",
            "Created the Kanban board on GitHub Projects with five columns.",
            "Defined the Definition of Done and the DevOps lifecycle.",
            "Committed the report, raised a pull request into develop and merged it.",
        ]),
        ("h", "9. Git Commands Used"),
        ("code", "git checkout develop\ngit pull origin develop\ngit checkout -b task/02-agile-planning\ngit add docs\ngit commit -m \"docs: add Task 2 agile planning report\"\ngit push -u origin task/02-agile-planning"),
        ("h", "10. Screenshots"),
        ("screenshots",),
        ("h", "11. Deliverables Checklist"),
        ("check", [
            "User stories with acceptance criteria",
            "Product backlog",
            "15-task Kanban plan",
            "Definition of Done",
            "DevOps lifecycle diagram",
        ]),
        ("h", "12. Issues Faced and Fixes"),
        ("bullets", [
            "Script initially run from the wrong folder; fixed by running it inside the repository.",
            "Report file was open in Word and caused a permission error; closed it before regenerating.",
        ]),
        ("h", "13. Conclusion"),
        ("p", "Agile planning for the Parcel Tracking Portal is complete. The user stories, backlog, Kanban board, Definition of Done and DevOps lifecycle give a clear path for building the application and its CI/CD pipeline in the next tasks."),
    ],
)

TASKS[3] = dict(
    title="Requirements, Architecture and Technology Setup",
    branch="task/03-architecture-setup",
    filename="task-03-architecture-setup",
    blocks=[
        ("h", "1. Objective"),
        ("p", "To define the requirements of the Parcel Tracking Portal, design the minimum application architecture for the MVP, select the technology stack and verify that the required tools are installed."),
        ("h", "2. Functional Requirements"),
        ("table", ["ID", "Requirement", "User story"], [
            ["FR-01", "Add a new parcel with ID, sender, receiver, origin and destination", "US-01"],
            ["FR-02", "Add a status event (Picked Up, In Transit, Out for Delivery, Delivered, Failed) with timestamp and location", "US-02"],
            ["FR-03", "Search a parcel by ID", "US-03"],
            ["FR-04", "Dashboard listing all parcels with status filter", "US-04"],
            ["FR-05", "Summary indicators: total, in transit, delivered, delayed", "US-05"],
            ["FR-06", "Status drill-down showing the full event history of a parcel", "US-06"],
            ["FR-07", "Alert view listing delayed or failed parcels", "US-07"],
        ]),
        ("h", "3. Non-Functional Requirements"),
        ("table", ["ID", "Requirement", "Target"], [
            ["NFR-01", "Performance", "Search result in under 2 seconds"],
            ["NFR-02", "Reliability", "Build, test and deploy run automatically without manual steps"],
            ["NFR-03", "Portability", "Same application runs on Tomcat and inside a Docker container"],
            ["NFR-04", "Maintainability", "Layered code structure and Maven standard layout"],
            ["NFR-05", "Testability", "Core user journeys covered by Selenium tests"],
            ["NFR-06", "Cost", "Only free and open-source tools"],
        ]),
        ("h", "4. Minimum Application Architecture"),
        ("p", "The portal is a layered Java web application. The browser sends requests to servlets running on Apache Tomcat. Servlets call a service layer that holds the business rules, and the service layer uses a DAO layer to read and write an embedded H2 database. JSP pages render the results."),
        ("table", ["Layer", "Responsibility", "Components"], [
            ["Presentation", "Display pages and forms", "JSP: add parcel, dashboard, parcel details, alerts"],
            ["Controller", "Handle HTTP requests and choose the view", "ParcelServlet, EventServlet, SearchServlet, DashboardServlet"],
            ["Service", "Business rules such as delay detection and summary counts", "ParcelService, EventService"],
            ["Data access", "Database operations through JDBC", "ParcelDao, EventDao"],
            ["Storage", "Persist parcels and events", "H2 embedded database"],
        ]),
        ("h2", "Architecture diagram"),
        ("code", "Browser\n   |\n   v\nJSP Views\n   |\n   v\nServlets (Controllers)\n   |\n   v\nService Layer\n   |\n   v\nDAO Layer (JDBC)\n   |\n   v\nH2 Database\n\nAll components run inside Apache Tomcat and are built by Maven."),
        ("p", "A drawn version of this diagram is included in the Screenshots section."),
        ("h2", "Data model"),
        ("table", ["Table", "Columns"], [
            ["parcels", "parcel_id (PK), sender, receiver, origin, destination, created_at"],
            ["events", "event_id (PK), parcel_id (FK), status, location, event_time, remarks"],
        ]),
        ("h2", "Delay rule"),
        ("p", "A parcel is flagged as delayed when its latest status is not Delivered and no event has been recorded for more than 48 hours. A parcel whose latest status is Failed is always shown in the alert view."),
        ("h2", "Planned project structure"),
        ("code", "parcel-tracking-portal/\n  pom.xml\n  src/main/java/.../controller\n  src/main/java/.../service\n  src/main/java/.../dao\n  src/main/java/.../model\n  src/main/webapp/*.jsp\n  src/test/java/.../selenium\n  Jenkinsfile\n  Dockerfile\n  config-management/\n  docs/"),
        ("h", "5. Technology Selection"),
        ("table", ["Area", "Choice", "Reason"], [
            ["Language", "Java (compiled for Java 17)", "Matches Maven, Tomcat and Jenkins tooling"],
            ["Web layer", "Servlets and JSP (Jakarta)", "Simple, lightweight, ideal for a WAR deployment"],
            ["Database", "H2 embedded", "No separate database server needed"],
            ["Build", "Maven", "Standard build, dependency and test management"],
            ["Server", "Apache Tomcat 10.1", "Supports Jakarta Servlet 6"],
            ["Version control", "Git and GitHub", "Branching and pull requests"],
            ["CI / CD", "Jenkins", "Pipeline as code with a Jenkinsfile"],
            ["Testing", "Selenium WebDriver with JUnit 5", "Browser-level automated tests"],
            ["Containers", "Docker", "Repeatable runtime environment"],
            ["Configuration management", "Ansible (through WSL)", "Agentless and simple YAML playbooks"],
        ]),
        ("h", "6. Tool Verification"),
        ("table", ["Tool", "Version found", "Status"], [
            ["Java JDK", "22.0.2 (target release 17)", "Installed"],
            ["Apache Maven", "3.9.16", "Installed"],
            ["Git", "2.46.2.windows.1", "Installed"],
            ["Operating system", "Windows 11 (amd64)", "Ready"],
            ["Apache Tomcat", "10.1.x", "Installed and welcome page verified"],
            ["Jenkins, Docker, Selenium, Ansible", "To be installed in their own tasks", "Planned"],
        ]),
        ("h", "7. Steps Performed"),
        ("numbered", [
            "Created branch task/03-architecture-setup from develop.",
            "Wrote functional and non-functional requirements.",
            "Designed the layered architecture and the data model.",
            "Selected the technology stack.",
            "Verified Java, Maven and Git with version commands.",
            "Downloaded Apache Tomcat 10.1, started it and opened http://localhost:8080.",
            "Committed the report, raised a pull request into develop and merged it.",
        ]),
        ("h", "8. Commands Used"),
        ("code", "java -version\nmvn -v\ngit --version\nstartup.bat\ngit add docs\ngit commit -m \"docs: add Task 3 requirements, architecture and setup report\"\ngit push -u origin task/03-architecture-setup"),
        ("h", "9. Screenshots"),
        ("screenshots",),
        ("h", "10. Deliverables Checklist"),
        ("check", [
            "Functional and non-functional requirements",
            "Minimum application architecture and data model",
            "Technology stack selected",
            "Tools verified",
        ]),
        ("h", "11. Issues Faced and Fixes"),
        ("bullets", [
            "Installed JDK is version 22. The project compiles for Java 17, and Jenkins runs on its own Java 21 runtime.",
            "Jenkins and Tomcat both use port 8080, so Tomcat was moved to port 8081.",
            "Ansible does not run natively on Windows, so WSL will be used in the configuration management task.",
        ]),
        ("h", "12. Conclusion"),
        ("p", "The requirements, architecture and technology stack for the Parcel Tracking Portal are defined and the required tools are verified. The next task creates the Git and GitHub repository structure and the initial application skeleton."),
    ],
)

TASKS[4] = dict(
    title="Git and GitHub Repository Initialization",
    branch="task/04-repo-init",
    filename="task-04-repo-init",
    blocks=[
        ("h", "1. Objective"),
        ("p", "To initialise the GitHub repository for the Parcel Tracking Portal with a README, .gitignore, folder structure, issue templates and branch naming rules, and to commit the initial application skeleton using meaningful commit messages."),
        ("h", "2. Repository Details"),
        ("table", ["Item", "Value"], [
            ["Repository", "parcel-tracking-portal"],
            ["Owner", "Explorer1905"],
            ["Visibility", "Public"],
            ["Default branch", "main"],
            ["Integration branch", "develop"],
        ]),
        ("h", "3. Folder Structure"),
        ("code", "parcel-tracking-portal/\n  pom.xml\n  README.md\n  CONTRIBUTING.md\n  .gitignore\n  generate_report1.py\n  .github/\n    ISSUE_TEMPLATE/ (bug_report, feature_request, task)\n    pull_request_template.md\n  src/main/java/com/parcel/{controller,service,dao,model}\n  src/main/webapp/index.jsp\n  src/test/java/com/parcel\n  docs/reports\n  docs/screenshots"),
        ("h", "4. Branch Naming Rules"),
        ("table", ["Branch type", "Pattern", "Example"], [
            ["Stable", "main", "main"],
            ["Integration", "develop", "develop"],
            ["Task", "task/NN-short-name", "task/04-repo-init"],
            ["Feature", "feature/short-name", "feature/parcel-entry"],
            ["Fix", "fix/short-name", "fix/search-error"],
        ]),
        ("h", "5. Commit Message Convention"),
        ("p", "Commits use the format type: summary, where type is one of feat, fix, docs, test, build, ci or chore. Each commit contains one logical change."),
        ("h", "6. Initial Application Skeleton"),
        ("bullets", [
            "Maven WAR project compiled for Java 17 with the Jakarta Servlet 6.0 API.",
            "Layered package structure: controller, service, dao and model.",
            "index.jsp landing page and a /health servlet returning OK.",
            "Build verified with mvn clean package, producing parcel-tracking-portal.war.",
        ]),
        ("h", "7. Templates Added"),
        ("bullets", [
            "Issue templates: bug report, feature request and task.",
            "Pull request template with a checklist.",
            "CONTRIBUTING.md with branching and commit rules.",
        ]),
        ("h", "8. Steps Performed"),
        ("numbered", [
            "Created branch task/04-repo-init from develop.",
            "Created the Maven project skeleton and verified the build.",
            "Added README, CONTRIBUTING.md and the .github templates.",
            "Extended .gitignore for Maven and IDE files.",
            "Made separate commits with meaningful messages.",
            "Pushed the branch, raised a pull request into develop and merged it.",
        ]),
        ("h", "9. Commands Used"),
        ("code", "git checkout -b task/04-repo-init\nmvn clean package\ngit add pom.xml src\ngit commit -m \"build: add Maven WAR project skeleton\"\ngit commit -m \"feat: add health check servlet\"\ngit commit -m \"docs: add branch naming and commit message rules\"\ngit commit -m \"chore: add issue and pull request templates\"\ngit push -u origin task/04-repo-init\ngit log --oneline"),
        ("h", "10. Screenshots"),
        ("screenshots",),
        ("h", "11. Deliverables Checklist"),
        ("check", [
            "GitHub repository created",
            "README and .gitignore added",
            "Folder structure created",
            "Issue templates added",
            "Branch naming rules documented",
            "Initial skeleton committed with meaningful messages",
        ]),
        ("h", "12. Issues Faced and Fixes"),
        ("bullets", [
            "Git commands were first run in the wrong folder and used a stray repository; fixed by running them inside the project folder and verifying with git rev-parse --show-toplevel.",
            "The Task 2 pull request had not been merged; merged it before starting later tasks.",
        ]),
        ("h", "13. Conclusion"),
        ("p", "The repository is fully initialised with documentation, templates, branching rules and a building application skeleton. The next task develops the first feature on a feature branch using the pull request workflow."),
    ],
)

TASKS[5] = dict(
    title="Application Deployment and Testing",
    branch="develop",
    filename="task-05-deployment-testing",
    blocks=[
        ("h", "1. Objective"),
        ("p", "To build the Parcel Tracking Portal, deploy the generated WAR file to Apache Tomcat, verify the application in a browser and confirm the health-check endpoint responds successfully."),
        ("h", "2. Environment"),
        ("table", ["Item", "Value"], [
            ["Operating system", "Windows"],
            ["Java", "JDK 22"],
            ["Build tool", "Apache Maven"],
            ["Application server", "Apache Tomcat 10.1.60"],
            ["Tomcat port", "8081"],
            ["Application URL", "http://localhost:8081/parcel-tracking-portal/"],
        ]),
        ("h", "3. Build and Deployment"),
        ("numbered", [
            "Updated the local develop branch after merging the parcel-entry feature.",
            "Ran mvn clean package successfully.",
            "Verified that target/parcel-tracking-portal.war was generated.",
            "Copied the WAR file into Tomcat's webapps directory.",
            "Configured Tomcat to use port 8081 because Jenkins was already using port 8080.",
            "Set JAVA_HOME to the installed JDK 22 directory and started Tomcat.",
        ]),
        ("h", "4. Functional Verification"),
        ("table", ["Test", "Result"], [
            ["Tomcat listener on port 8081", "Pass"],
            ["Portal home page", "HTTP 200"],
            ["Health endpoint /health", "HTTP 200; response OK"],
            ["Parcel management link", "Available from the home page"],
        ]),
        ("h", "5. Commands Used"),
        ("code", "git checkout develop\ngit pull origin develop\nmvn clean package\nCopy-Item target\\parcel-tracking-portal.war <tomcat>\\webapps\\ -Force\n$env:JAVA_HOME = 'C:\\Program Files\\Java\\jdk-22'\n.\\startup.bat\nhttp://localhost:8081/parcel-tracking-portal/\nhttp://localhost:8081/parcel-tracking-portal/health"),
        ("h", "6. Issues Faced and Fixes"),
        ("bullets", [
            "Tomcat initially could not start because JAVA_HOME was not defined; fixed by setting JAVA_HOME to C:\\Program Files\\Java\\jdk-22.",
            "Port 8080 was already occupied by Jenkins, so the Tomcat HTTP connector was changed to port 8081.",
            "Requests sent to port 8080 returned Jenkins responses and 404 errors; testing the configured Tomcat port 8081 returned the application successfully.",
        ]),
        ("h", "7. Screenshots"),
        ("screenshots",),
        ("h", "8. Deliverables Checklist"),
        ("check", [
            "Maven build completed successfully",
            "WAR file generated",
            "WAR deployed to Apache Tomcat",
            "Portal home page verified",
            "Health endpoint verified with response OK",
            "Deployment screenshots added to the repository",
        ]),
        ("h", "9. Conclusion"),
        ("p", "The Parcel Tracking Portal was built successfully, deployed to Apache Tomcat 10.1.60 and tested on port 8081. The home page returned HTTP 200 and the health endpoint returned OK, confirming that the application was running correctly."),
    ],
)

TASKS[6] = dict(
    title="MVP Completion and Git Collaboration",
    branch="task/06-mvp-completion",
    filename="task-06-mvp-completion",
    blocks=[
        ("h", "1. Objective"),
        ("p", "To complete the essential Parcel Tracking Portal MVP functions: event entry, searchable dashboard, summary indicators, status drill-down and alert/exception view, while demonstrating feature branches, conflict resolution, tagging and a release-ready source baseline."),
        ("h", "2. Features Delivered"),
        ("bullets", [
            "Event entry supports Picked Up, In Transit, Out for Delivery, Delivered and Failed statuses.",
            "Events include location, optional remarks and an optional event time for backdated testing.",
            "The dashboard supports parcel search and status filtering with total, in-transit, delivered and delayed indicators.",
            "The parcel detail page at /parcel?id=... shows the full event history.",
            "The alerts page lists Failed parcels and parcels whose latest non-delivered event is more than 48 hours old.",
        ]),
        ("h", "3. HTML Test ID Contract"),
        ("table", ["Page", "IDs"], [
            ["Add parcel", "parcelId, sender, receiver, origin, destination, addBtn, successMsg, errorMsg, parcelTable"],
            ["Dashboard", "searchBox, statusFilter, searchBtn, parcelTable, sumTotal, sumTransit, sumDelivered, sumDelayed"],
            ["Parcel detail", "eventStatus, eventLocation, eventTime, addEventBtn, eventTable"],
            ["Alerts", "alertTable"],
        ]),
        ("h", "4. Git Collaboration"),
        ("numbered", [
            "Created feature/event-entry and feature/dashboard from the develop baseline.",
            "Merged the event-entry feature into develop.",
            "Created the required conflict when the dashboard branch changed the same index.jsp navigation line.",
            "Resolved the conflict by retaining both parcel registration and dashboard navigation.",
            "Committed the resolution with a merge-specific commit message.",
        ]),
        ("h", "5. Validation"),
        ("table", ["Check", "Result"], [
            ["Maven build", "BUILD SUCCESS"],
            ["Compiled sources", "11 Java source files"],
            ["WAR artifact", "target/parcel-tracking-portal.war"],
            ["Release tag", "v1.0.0-mvp"],
        ]),
        ("h", "6. Commands Used"),
        ("code", "git checkout develop\ngit checkout -b feature/event-entry\ngit checkout -b feature/dashboard\ngit merge feature/event-entry\ngit merge feature/dashboard\ngit add src/main/webapp/index.jsp\ngit commit -m \"merge: resolve index.jsp conflict between event-entry and dashboard\"\nmvn clean package\ngit tag -a v1.0.0-mvp -m \"Release-ready Parcel Tracking Portal MVP\"\ngit log --oneline --graph --all -15"),
        ("h", "7. Screenshots"),
        ("screenshots",),
        ("h", "8. Deliverables Checklist"),
        ("check", [
            "Functional MVP completed",
            "Second feature branch demonstrated",
            "Merge conflict created and resolved",
            "Collaboration graph and blame evidence captured",
            "Release-ready source baseline tagged",
            "Task 6 backlog item moved to Done",
        ]),
        ("h", "9. Conclusion"),
        ("p", "The Parcel Tracking Portal MVP is complete and release-ready. The application supports event capture, search, status summaries, drill-down history and operational alerts. The Git history demonstrates parallel feature work, conflict resolution and a tagged release baseline."),
    ],
)

TASKS[7] = dict(
    title="Jenkins Installation and Continuous Integration Job",
    branch="task/07-jenkins-ci",
    filename="task-07-jenkins-ci",
    blocks=[
        ("h", "1. Objective"),
        ("p", "To configure Jenkins for the Parcel Tracking Portal, create a Maven continuous-integration job triggered by SCM polling, archive the WAR artifact and verify successful and failed build evidence."),
        ("h", "2. Jenkins Configuration"),
        ("table", ["Item", "Value"], [
            ["Jenkins URL", "http://localhost:8080"],
            ["Job name", "parcel-ci-build"],
            ["Job type", "Freestyle project"],
            ["Repository", "https://github.com/Explorer1905/parcel-tracking-portal.git"],
            ["Branch", "*/develop"],
            ["SCM trigger", "Poll SCM: H/2 * * * *"],
            ["Archived artifact", "target/*.war"],
        ]),
        ("h", "3. Tool Configuration"),
        ("table", ["Tool", "Configured value"], [
            ["JDK", "JDK22 - C:\\Program Files\\Java\\jdk-22"],
            ["Maven", "Maven3 - C:\\DevTools\\apache-maven-3.9.16"],
            ["Git", "C:\\Program Files\\Git\\cmd\\git.exe"],
        ]),
        ("h", "4. Build Step"),
        ("code", "set \"JAVA_HOME=C:\\Program Files\\Java\\jdk-22\"\nset \"PATH=C:\\DevTools\\apache-maven-3.9.16\\bin;%JAVA_HOME%\\bin;%PATH%\"\nmvn -B clean package"),
        ("h", "5. Verification"),
        ("bullets", [
            "Build Now completed successfully with BUILD SUCCESS and Finished: SUCCESS.",
            "The parcel-tracking-portal.war file was archived.",
            "The job was configured to poll the develop branch for SCM changes.",
        ]),
        ("h", "6. Screenshots"),
        ("screenshots",),
        ("h", "7. Deliverables Checklist"),
        ("check", [
            "Jenkins job created",
            "JDK, Maven and Git tools configured",
            "Successful Maven build verified",
            "WAR artifact archived",
            "Task 7 backlog item moved to Done",
        ]),
        ("h", "8. Conclusion"),
        ("p", "The Jenkins continuous-integration job builds the develop branch with Maven and archives the generated WAR artifact."),
    ],
)

TASKS[8] = dict(
    title="Pipeline as Code and Server Deployment",
    branch="task/08-pipeline-as-code",
    filename="task-08-pipeline-as-code",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Create a Jenkins pipeline as code for the Parcel Tracking Portal. The pipeline checks out the repository, builds and packages the WAR, archives the artifact, and deploys it to Apache Tomcat using a parameterized environment setting."),
        ("h", "2. Jenkinsfile"),
        ("p", "The repository root contains a declarative Jenkinsfile with Checkout, Build, Package and Deploy to Tomcat stages. The pipeline uses the DEPLOY_ENV choice and TOMCAT_HOME string parameters so the deployment target is configurable without editing the pipeline."),
        ("code", "pipeline {\n    agent any\n    parameters {\n        choice(name: 'DEPLOY_ENV', choices: ['local-tomcat', 'staging-tomcat'])\n        string(name: 'TOMCAT_HOME', defaultValue: '<Tomcat installation>')\n    }\n    stages {\n        stage('Checkout') { steps { checkout scm } }\n        stage('Build') { steps { bat 'mvn -B clean test' } }\n        stage('Package') {\n            steps {\n                bat 'mvn -B package -DskipTests'\n                archiveArtifacts artifacts: 'target/*.war', fingerprint: true\n            }\n        }\n        stage('Deploy to Tomcat') {\n            steps {\n                bat 'copy /Y target\\\\parcel-tracking-portal.war %TOMCAT_HOME%\\\\webapps\\\\'\n            }\n        }\n    }\n}"),
        ("h", "3. Pipeline Configuration"),
        ("table", ["Item", "Value"], [
            ["Pipeline definition", "Pipeline script from SCM"],
            ["Jenkinsfile location", "Jenkinsfile"],
            ["Build tool", "Maven"],
            ["Deployment server", "Apache Tomcat 10.1.60"],
            ["Application URL", "http://localhost:8081/parcel-tracking-portal/"],
            ["Health URL", "http://localhost:8081/parcel-tracking-portal/health"],
            ["Parameterized settings", "DEPLOY_ENV and TOMCAT_HOME"],
        ]),
        ("h", "4. Verification"),
        ("bullets", [
            "The Jenkins pipeline run completes successfully through Checkout, Build, Package and Deploy to Tomcat.",
            "The generated parcel-tracking-portal.war is archived by the pipeline.",
            "The WAR is copied into Tomcat's webapps directory.",
            "The deployed home page returns HTTP 200 at the application URL.",
            "The health endpoint returns OK after deployment.",
        ]),
        ("h", "5. Screenshots"),
        ("screenshots",),
        ("h", "6. Deliverables Checklist"),
        ("check", [
            "Jenkinsfile committed to the repository",
            "Checkout, build, package and deploy stages visible",
            "At least one deployment parameter configured",
            "Successful pipeline run captured",
            "WAR artifact archived",
            "Deployed application URL and health check verified",
            "Task 8 backlog item moved to Done",
        ]),
        ("h", "7. Conclusion"),
        ("p", "The Jenkinsfile makes the build and Tomcat deployment repeatable from source control. A successful pipeline run produces and archives the WAR, deploys it to Tomcat and verifies the running Parcel Tracking Portal."),
    ],
)

TASKS[9] = dict(
    title="Selenium Test Design and Local Execution",
    branch="task/09-selenium-tests",
    filename="task-09-selenium-tests",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Identify critical Parcel Tracking Portal user journeys, automate them with Selenium WebDriver and JUnit 5, execute the suite locally through Maven, and capture screenshots when a test fails."),
        ("h", "2. Test Plan"),
        ("table", ["ID", "User journey", "Test data", "Assertions"], [
            ["S-01", "Register a parcel", "Unique parcel ID; Mumbai to Pune", "Success message contains the parcel ID"],
            ["S-02", "Search the dashboard", "Registered unique parcel ID", "Dashboard table contains the parcel"],
            ["S-03", "Add a tracking event", "In Transit at Nashik", "Event table contains status and location"],
            ["S-04", "Open alerts and exceptions", "Existing portal data", "Alert table is displayed"],
        ]),
        ("h", "3. Selenium Implementation"),
        ("p", "ParcelPortalSeleniumTest.java uses Selenium WebDriver with Chrome, JUnit 5 lifecycle methods, explicit waits and stable element IDs from the application views. The base URL is configurable with selenium.baseUrl."),
        ("code", "mvn test -Dselenium.baseUrl=http://localhost:8081/parcel-tracking-portal\n\n# Optional visible-browser execution\nmvn test -Dselenium.headless=false"),
        ("h", "4. Failure Screenshot Mechanism"),
        ("p", "A JUnit TestWatcher captures the current browser screen after a test failure. Screenshots are written to target/selenium-failures/<test-name>.png. Maven Surefire XML and text reports are written to target/surefire-reports."),
        ("h", "5. Local Execution Evidence"),
        ("screenshots",),
        ("h", "6. Deliverables Checklist"),
        ("check", [
            "Four critical user journeys identified",
            "Selenium WebDriver tests with assertions and test data added",
            "Failure screenshot mechanism implemented",
            "Suite executed locally through Maven",
            "Surefire local test report generated",
            "Task 9 backlog item moved to Done",
        ]),
        ("h", "7. Conclusion"),
        ("p", "The local Selenium suite verifies the highest-value parcel workflows through the browser and preserves diagnostic screenshots for failures, providing a repeatable baseline for later Jenkins test integration."),
    ],
)

TASKS[10] = dict(
    title="Continuous Testing in Jenkins",
    branch="task/10-continuous-testing",
    filename="task-10-continuous-testing",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Integrate the Selenium user-journey suite into Jenkins, publish Maven Surefire results, stop deployment when tests fail, correct a deliberately introduced defect and verify a successful rerun."),
        ("h", "2. Pipeline Changes"),
        ("table", ["Stage or setting", "Implementation"], [
            ["Build", "mvn -B clean compile"],
            ["Selenium Tests", "mvn -B test -Dselenium.baseUrl=http://localhost:8081/parcel-tracking-portal"],
            ["Test report publisher", "junit target/surefire-reports/*.xml"],
            ["Package", "mvn -B package -DskipTests"],
            ["Deployment gate", "Package and Deploy stages execute only after tests pass"],
        ]),
        ("h", "3. Failure and Recovery"),
        ("p", "A deliberately failing Selenium assertion is introduced on the test branch and causes the Jenkins Selenium Tests stage to fail. Jenkins publishes the Surefire result and skips Package and Deploy. The assertion is then corrected in a follow-up commit, and the pipeline is rerun successfully."),
        ("h", "4. Verification"),
        ("bullets", [
            "Failed pipeline evidence shows a red Selenium Tests stage and no deployment.",
            "Jenkins displays the published Surefire test report.",
            "The defect correction commit restores the intended assertion.",
            "The successful rerun completes Selenium Tests, Package and Deploy.",
            "The deployed application remains available at http://localhost:8081/parcel-tracking-portal/.",
        ]),
        ("h", "5. Screenshots"),
        ("screenshots",),
        ("h", "6. Deliverables Checklist"),
        ("check", [
            "Selenium suite integrated into Jenkins",
            "Surefire test report published",
            "Failed tests prevent deployment",
            "Deliberate defect and correction commit recorded",
            "Successful pipeline rerun captured",
            "Task 10 backlog item moved to Done",
        ]),
        ("h", "7. Conclusion"),
        ("p", "Jenkins now treats the Selenium suite as a deployment gate. Test results are published for every run, failures stop the release flow, and the corrected pipeline provides evidence of failure detection and recovery."),
    ],
)

TASKS[11] = dict(
    title="Docker Image and Container Lifecycle",
    branch="task/11-docker",
    filename="task-11-docker",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Create a Docker image for the Parcel Tracking Portal, run it as a container, map the application port, inspect image and runtime details, and document the complete container lifecycle."),
        ("h", "2. Dockerfile"),
        ("p", "The Dockerfile uses Apache Tomcat 10.1 with JDK 17, removes the default web applications, copies the Maven WAR into the application context and starts Tomcat in the foreground."),
        ("code", "FROM tomcat:10.1-jdk17-temurin\nENV CATALINA_OPTS=\"-Dfile.encoding=UTF-8\"\nRUN rm -rf /usr/local/tomcat/webapps/*\nCOPY target/parcel-tracking-portal.war /usr/local/tomcat/webapps/parcel-tracking-portal.war\nEXPOSE 8080\nCMD [\"catalina.sh\", \"run\"]"),
        ("h", "3. Image and Container Commands"),
        ("code", "mvn -B clean package -DskipTests\ndocker build -t parcel-tracking-portal:1.0.0 .\ndocker images parcel-tracking-portal\ndocker run -d --name parcel-portal -p 8082:8080 parcel-tracking-portal:1.0.0\ndocker ps\ndocker logs parcel-portal\ndocker inspect parcel-portal\ndocker stop parcel-portal\ndocker start parcel-portal\ndocker rm -f parcel-portal"),
        ("h", "4. Verification"),
        ("table", ["Check", "Expected result"], [
            ["Image tag", "parcel-tracking-portal:1.0.0"],
            ["Container mapping", "Host 8082 to container 8080"],
            ["Application URL", "http://localhost:8082/parcel-tracking-portal/"],
            ["Health URL", "http://localhost:8082/parcel-tracking-portal/health"],
            ["Health response", "OK"],
            ["Lifecycle", "Run, inspect, stop, restart and remove demonstrated"],
        ]),
        ("h", "5. Screenshots"),
        ("screenshots",),
        ("h", "6. Deliverables Checklist"),
        ("check", [
            "Dockerfile committed",
            "Docker image built and tagged",
            "Container started with mapped port",
            "Image details and container logs inspected",
            "Container stopped and restarted",
            "Container removed after verification",
            "Running container and health endpoint captured",
            "Task 11 backlog item moved to Done",
        ]),
        ("h", "7. Conclusion"),
        ("p", "The Parcel Tracking Portal is packaged as a reproducible Tomcat container. The recorded commands demonstrate image creation, runtime verification and the full container lifecycle from start through removal."),
    ],
)

TASKS[12] = dict(
    title="Jenkins-Docker Continuous Deployment",
    branch="task/12-jenkins-docker-cd",
    filename="task-12-jenkins-docker-cd",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Extend the Jenkins pipeline so that successful build and Selenium test stages produce a versioned Docker image, publish it to a local registry, and deploy a fresh container automatically."),
        ("h", "2. Pipeline Stages"),
        ("table", ["Stage", "Implementation"], [
            ["Build and test", "Maven compile and Selenium tests; failures stop all later stages"],
            ["Package", "Maven WAR package with archived artifact"],
            ["Build Docker Image", "Builds localhost:5000/parcel-tracking-portal:1.0.<Jenkins build number>"],
            ["Publish to Local Registry", "Pushes the versioned and latest tags to registry:2"],
            ["Deploy Fresh Container", "Pulls the exact versioned tag, removes the old container and starts a new one"],
            ["Health verification", "Checks /parcel-tracking-portal/health and expects OK"],
        ]),
        ("h", "3. Local Registry and Jenkins Configuration"),
        ("code", "docker run -d --restart=always --name parcel-registry -p 5000:5000 registry:2\n\n# Jenkins parameters\nREGISTRY_HOST=localhost:5000\nIMAGE_REPOSITORY=parcel-tracking-portal\nCONTAINER_NAME=parcel-portal-cd\nCONTAINER_PORT=8082"),
        ("h", "4. Versioned Image and Container Evidence"),
        ("code", "docker image ls localhost:5000/parcel-tracking-portal\ndocker pull localhost:5000/parcel-tracking-portal:1.0.<BUILD_NUMBER>\ndocker ps --filter name=parcel-portal-cd\ndocker logs parcel-portal-cd\ncurl.exe http://localhost:8082/parcel-tracking-portal/health"),
        ("h", "5. Verification"),
        ("bullets", [
            "A successful Jenkins run shows Build, Selenium Tests, Package, Docker build, registry publish and fresh container deployment stages.",
            "The local registry tag list contains the versioned image tag.",
            "The deployed container was created from the exact versioned image tag.",
            "The health endpoint returns OK after deployment.",
            "A failed test run prevents image publication and container deployment.",
        ]),
        ("h", "6. Screenshots"),
        ("screenshots",),
        ("h", "7. Deliverables Checklist"),
        ("check", [
            "Jenkinsfile updated with Docker build and publish stages",
            "Versioned image created",
            "Local registry evidence captured",
            "Fresh container deployed by Jenkins",
            "Health endpoint verified",
            "End-to-end successful pipeline run captured",
            "Task 12 backlog item moved to Done",
        ]),
        ("h", "8. Conclusion"),
        ("p", "Jenkins now provides continuous delivery from a tested commit to a versioned registry image and a fresh running container. The image tag, registry response, container logs and health check make the deployment reproducible and auditable."),
    ],
)

TASKS[13] = dict(
    title="Configuration Management Script",
    branch="task/13-configuration-management",
    filename="task-13-configuration-management",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Identify the Parcel Tracking Portal server prerequisites and configure an Ubuntu/Debian target node with an idempotent Ansible playbook."),
        ("h", "2. Configuration Specification"),
        ("table", ["Category", "Required state"], [
            ["Packages", "ca-certificates, curl, docker.io, maven, openjdk-17-jre-headless"],
            ["Identity", "System group and nologin user: parcel"],
            ["Folders", "/opt/parcel-tracking-portal, /etc/parcel-tracking-portal, /var/lib/parcel-tracking-portal"],
            ["Files", "Managed application.properties with ownership and mode 0640"],
            ["Ports", "8082 application, 8080 Tomcat, 5000 registry"],
            ["Service", "Docker enabled and started"],
            ["Firewall", "Required TCP ports allowed on Debian-family targets"],
        ]),
        ("h", "3. Ansible Inventory and Playbook"),
        ("p", "ansible/inventory.ini targets the local Ubuntu/WSL node for a reproducible first execution. ansible/site.yml installs packages, creates the service identity and directories, writes configuration, starts Docker and configures firewall ports."),
        ("code", "cd ansible\nansible-playbook -i inventory.ini site.yml --check --diff\nansible-playbook -i inventory.ini site.yml\nansible-playbook -i inventory.ini site.yml"),
        ("h", "4. First Execution and Idempotence"),
        ("p", "The first normal execution log is saved as docs/reports/task-13-ansible-first-execution.log. A second normal execution is run to demonstrate idempotence; resources remain present and no unnecessary changes are made."),
        ("h", "5. Screenshots"),
        ("screenshots",),
        ("h", "6. Deliverables Checklist"),
        ("check", [
            "Configuration specification added",
            "Ansible inventory added",
            "Ansible playbook configures packages, identity, folders, files, ports and service",
            "First execution log captured",
            "Idempotent second execution verified",
            "Task 13 backlog item moved to Done",
        ]),
        ("h", "7. Conclusion"),
        ("p", "The target node prerequisites are expressed as repeatable Ansible resources rather than manual setup steps. The inventory supports local WSL demonstration and can be changed to an SSH target without changing the playbook."),
    ],
)
TASKS[14] = dict(
    title="Automated Provisioning and Reliability Validation",
    branch="task/14-provisioning-reliability",
    filename="task-14-provisioning-reliability",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Provision a clean target with Ansible, deploy a known release, rerun automation to demonstrate repeatability, verify application health and recover by rolling back to a previous stable image."),
        ("h", "2. Provisioning and Deployment Design"),
        ("table", ["Area", "Automation"], [
            ["Prerequisites", "site.yml installs packages and creates the service identity and directories"],
            ["Release deployment", "deploy.yml pulls an exact image tag and reconciles parcel-portal-cd"],
            ["Health gate", "Ansible URI retries until HTTP 200 and body OK"],
            ["Idempotence", "A repeat deployment skips container replacement when the requested tag is already running"],
            ["Rollback", "rollback.yml restores a known stable image and repeats the health check"],
        ]),
        ("h", "3. Execution Commands"),
        ("code", "sudo ansible-playbook -i inventory.ini site.yml -e ansible_become=false\nsudo ansible-playbook -i inventory.ini deploy.yml -e ansible_become=false -e release_tag=1.0.7\nsudo ansible-playbook -i inventory.ini deploy.yml -e ansible_become=false -e release_tag=1.0.7\nsudo ansible-playbook -i inventory.ini rollback.yml -e ansible_become=false -e rollback_tag=1.0.7"),
        ("h", "4. Reliability Verification"),
        ("code", "docker ps --filter name=parcel-portal-cd\ncat /etc/parcel-tracking-portal/active-release\ncurl http://127.0.0.1:8082/parcel-tracking-portal/health"),
        ("h", "5. Evidence"),
        ("p", "Capture the provisioning recap, repeat execution recap, deployed version and health response, then capture the rollback container and recovered health response. Save the first execution output as docs/reports/task-14-provisioning-first-execution.log."),
        ("h", "6. Screenshots"),
        ("screenshots",),
        ("h", "7. Deliverables Checklist"),
        ("check", [
            "Clean target provisioned with Ansible",
            "Release container deployed from an exact versioned image",
            "Automation rerun successfully",
            "Health endpoint returned OK",
            "Rollback to the previous stable release completed",
            "Recovery health check returned OK",
            "Task 14 backlog item moved to Done",
        ]),
        ("h", "8. Conclusion"),
        ("p", "The portal environment can be provisioned and recovered through repeatable automation. Exact image tags, health gating and an explicit stable rollback make the container deployment auditable and recoverable."),
    ],
)

TASKS[15] = dict(
    title="Final End-to-End Release, Documentation and Viva",
    branch="task/15-final-release-viva",
    filename="task-15-final-release-viva",
    blocks=[
        ("h", "1. Objective"),
        ("p", "Run and document the complete Parcel Tracking Portal workflow from Git commit through Jenkins, Maven, Selenium, Docker deployment and Ansible provisioning. Prepare the final technical report, evidence and viva material."),
        ("h", "2. Architecture and Release Flow"),
        ("p", "A Java 17 Jakarta Servlet/JSP WAR is built with Maven, tested with Selenium in Jenkins, packaged into a versioned Docker image, published to a local registry and deployed as parcel-portal-cd. Ansible provisions the target and validates the health endpoint."),
        ("table", ["Stage", "Evidence"], [
            ["Source control", "Final commit, branch and merged pull request"],
            ["Continuous integration", "Jenkins checkout, Maven build and WAR archive"],
            ["Quality gate", "Selenium journeys and published Surefire results"],
            ["Continuous delivery", "Versioned registry image and running container"],
            ["Operations", "Ansible provisioning, idempotence, health check and rollback"],
        ]),
        ("h", "3. Demonstration Commands"),
        ("code", "mvn -B clean package\ncurl.exe http://localhost:8082/parcel-tracking-portal/health\ncd ansible\nsudo ansible-playbook -i inventory.ini site.yml -e ansible_become=false\nsudo ansible-playbook -i inventory.ini deploy.yml -e ansible_become=false -e release_tag=1.0.7\nsudo ansible-playbook -i inventory.ini deploy.yml -e ansible_become=false -e release_tag=1.0.7\nsudo ansible-playbook -i inventory.ini rollback.yml -e ansible_become=false -e rollback_tag=1.0.7"),
        ("h", "4. Troubleshooting, Limitations and Future Work"),
        ("p", "The final runbook documents Jenkins Docker path issues, delayed Tomcat startup, Selenium connectivity, WSL Docker service limitations, registry tag failures and port conflicts. It also records the single-container, local-registry and non-production security limitations and proposes artifact signing, scanning, observability, replicas and production TLS."),
        ("h", "5. Evidence"),
        ("p", "Save final screenshots under docs/screenshots/task-15 and record the Jenkins run, Selenium gate, registry image, container health, Ansible idempotence and rollback recovery. Keep large video files outside the repository unless the submission rules permit them."),
        ("h", "6. Screenshots"),
        ("screenshots",),
        ("h", "7. Viva Preparation"),
        ("check", [
            "Final repository and merged history shown",
            "Jenkins end-to-end pipeline demonstrated",
            "Selenium quality gate explained",
            "Versioned Docker deployment verified",
            "Ansible provisioning and idempotence explained",
            "Rollback and health recovery demonstrated",
            "Troubleshooting, limitations and future plan reviewed",
            "Final presentation and viva questions prepared",
        ]),
        ("h", "8. Conclusion"),
        ("p", "The project demonstrates a traceable DevOps release path with automated quality validation, versioned deployment, infrastructure configuration, health gating and recovery evidence."),
    ],
)

# ---------- RUN ----------
if __name__ == "__main__":
    nums = [int(a) for a in sys.argv[1:]] or sorted(TASKS)
    for n in nums:
        if n not in TASKS:
            print(f"Task {n} content not added yet."); continue
        t = TASKS[n]
        build(n, t["title"], t["branch"], t["blocks"], t["filename"])
# Strix

Strix is a behaviour-based Intrusion Detection System for web applications.

This repository contains my final year project implementation. The project explores whether a web application can be monitored at the application layer by learning normal request behaviour and then identifying suspicious deviations from that baseline.

Unlike a simple rule-based system, Strix observes patterns of HTTP request activity over time. It uses machine learning to help detect abnormal behaviour such as brute force login attempts, endpoint scanning and suspicious request patterns.

## Project Overview

Strix is made up of three main applications:

- `target-app` 
  A monitored Spring Boot web application with normal, login, user and admin endpoints.

- `strix-ids` 
  The main Strix Intrusion Detection System service. It receives request events from the target application, extracts behavioural features, applies anomaly detection and provides the dashboard.

- `traffic-generator` 
  A Java application that simulates normal users and suspicious traffic so the IDS can be trained, tested and demonstrated.

The basic flow of the system is:

```text
Traffic Generator -> Target Spring Boot App -> Strix IDS
                                      <- Mitigation Actions
```

The traffic generator sends request traffic to the target application. The target application records each request and forwards structured request events to the IDS. The IDS analyses the behaviour and raises alerts or mitigation recommendations when suspicious activity is detected.

## Repository Structure

```text
strix/
├── strix-ids/           # Main intrusion detection service
├── target-app/          # Monitored Spring Boot target application
├── traffic-generator/   # Normal and suspicious traffic simulation tool
├── docs/                # Supporting documentation and project evidence
└── .github/workflows/   # GitHub Actions CI/CD workflows
```

## Technologies Used

The project is primarily built with:

- Java
- Spring Boot
- Gradle
- Machine learning based anomaly detection
- Docker
- GitHub Actions
- GitHub Container Registry
- Azure Container Apps

## Running Strix Locally

The easiest way to run the project locally is through IntelliJ IDEA or VS Code.

### Prerequisites

Make sure the following are installed:

- Java
- IntelliJ IDEA or VS Code
- Gradle support enabled in the IDE
- Port `8080` free for the target application
- Port `8081` free for the IDS service

## Startup Order

The applications should be started in this order.

### 1. Run the target app

Open the `target-app` module and run the Spring Boot main class:

```text
org.example.StrixAppScratch
```

This starts the monitored web application on:

```text
http://localhost:8080
```

### 2. Run the Strix IDS

Open the `strix-ids` module and run the Spring Boot main class:

```text
org.example.StrixIds
```

This starts the IDS service on:

```text
http://localhost:8081
```

### 3. Open the dashboard

Once the IDS is running, open the dashboard in a browser:

```text
http://localhost:8081/dashboard.html
```

### 4. Run the traffic generator

Open the `traffic-generator` module and run the main class:

```text
org.example.StrixGenerator
```

The traffic generator sends both normal and suspicious traffic to the target application. This allows the IDS to show alerts and detection behaviour through the dashboard.

## Expected Demo Flow

A normal local demonstration should look like this:

```text
1. Start target-app
2. Start strix-ids
3. Open the dashboard
4. Start traffic-generator
5. Watch the IDS receive events and raise alerts
```

## DevOps and Pipeline Work

This repository also includes the DevOps pipeline implementation for the project.

The GitHub Actions workflows support a pull request based development process. The pipeline checks the project before changes are merged and supports build, test, formatting, linting, container image creation, security scanning and deployment evidence.

The aim is not only to build the Strix application, but also to show how a small software project can be supported by a realistic DevOps workflow using issues, branches, pull requests, code review, automated checks and deployment stages.

## Development Workflow

Development work should be completed through:

```text
Issue -> Feature Branch -> Pull Request -> Review -> Merge
```

Changes should not be pushed directly to `main`. Pull requests should be reviewed and automated checks should pass before merging.

## Troubleshooting

If the target app does not start, check that port `8080` is free.

If the IDS does not start, check that port `8081` is free.

If the dashboard does not load, make sure the IDS service is running.

If the traffic generator fails to connect, make sure both the target application and the IDS service are running first.

## Project Status

Strix is an academic prototype developed as part of a final year software project. It is intended to demonstrate behaviour-based intrusion detection, anomaly detection and DevOps pipeline implementation. It is not intended to be used as a production security system without further development, testing and hardening.

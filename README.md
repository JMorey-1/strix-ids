# Strix

Strix is a behaviour-based intrusion detection system for web applications.

The project is being developed as part of a DevOps pipeline implementation. The repository is structured as a Gradle monorepo so the main IDS service, target application and traffic generator can be built and managed together.

## Repository Structure

- `strix-ids` - the main intrusion detection service
- `target-app` - the monitored Spring Boot web application used to generate request activity
- `traffic-generator` - a utility for simulating normal and suspicious traffic
- `docs` - supporting documentation and pipeline evidence

## Development Workflow

Work should be completed through issues, feature branches and pull requests. Changes should be reviewed before being merged into `main`.
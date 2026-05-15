# Release Governance

Strix uses a staged release model to separate normal development work from environment promotion. This is intended to support the DevOps pipeline for the project rather than represent a full production hosting setup.

## Environments

The `dev` environment represents the first controlled stage after code has passed pull request review and CI checks. It is used for low-risk validation of the current main branch.

The `staging` environment represents a more controlled pre-release stage. Promotion to staging should require approval so the system can be checked before it is treated as release-ready.

The `production` environment represents the final demo or production-style stage for the prototype. Promotion to this environment should be deliberate and approved.

## Review Ownership

Code ownership is used to make pull request review more focused. DevOps, pipeline, Docker and release files are owned by the platform-focused reviewer, Niamh Kavanagh (Niamh-Kav-1). Application and backend files are owned by the backend-focused reviewer, James Smith (James-Smith-1).

Reviewers are expected to check whether a change works, but also whether it affects release behaviour, environment configuration or ownership boundaries.

## Promotion Approach

## Image-Based Promotion

The release workflow promotes published Docker images rather than rebuilding the application at each environment stage. The Docker image workflow builds, scans, generates SBOMs and publishes SHA-tagged images to GitHub Container Registry.

The environment promotion workflow then accepts an image tag and uses that same tag through `dev`, `staging` and `production`. This supports the build-once, promote-same-artifact principle and improves traceability because each promoted image can be linked back to the commit that created it.

A separate `docker-compose.images.yml` file is provided for running the published image set locally or during demonstrations without rebuilding the images from source.


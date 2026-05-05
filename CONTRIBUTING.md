# Contributing to Strix

Strix uses a pull request based workflow. All planned work should begin with a GitHub issue, move onto a short-lived branch and then be reviewed before it is merged into `main`.

## Team Roles

Although Strix is implemented as an individual academic project, the repository workflow simulates a small cross-functional DevOps team. The team model follows the CA1 strategy, with responsibilities spread across backend development, machine learning, platform engineering and product ownership.

- Jamie acts as Product Owner, lead developer and repository owner.
- James acts as backend and machine learning reviewer, with focus on the IDS service, feature extraction and anomaly detection logic.
- Niamh acts as backend and DevOps reviewer, with focus on the target application, GitHub Actions, branch protection, workflow rules and deployment process.

The Product Owner is responsible for prioritising the backlog, clarifying scope, accepting completed work and making release scope decisions. Technical quality remains a shared responsibility through pull request review, CI checks and team documentation. Because Strix is a small academic project, some responsibilities are combined rather than represented by separate GitHub accounts.

## Branching Workflow

All work should branch from `main`. Branches should be named according to the type of work being completed.

Examples:

- `feature/add-target-app-bootstrap`
- `ci/add-basic-github-actions-workflow`
- `docs/team-workflow-guidelines`
- `fix/gradle-build-failure`

Branches should be short-lived. For this project, a branch should normally be completed, reviewed and merged within two to three working days. If a branch becomes too large or remains open for longer than expected, the work should be split into smaller issues or reviewed with the Product Owner.

## Pull Request Workflow

Every change to `main` must go through a pull request. Pull requests should link to the relevant issue using `Closes #issue-number`.

Before requesting review, the author should check that:

- the pull request has a clear summary
- the related issue is linked
- the change is focused and not unnecessarily large
- the Gradle build passes locally where practical
- any known limitations are noted for the reviewer

Pull requests must receive at least one approval before merging. Conversation threads should be resolved before merge so that review comments are not ignored.

## CI Expectations

The GitHub Actions CI workflow runs automatically on pull requests into `main`. The required `Build and test` check must pass before a pull request can be merged.

If CI fails, the author is responsible for investigating the failure, pushing a fix and explaining the cause in the pull request where appropriate.

## After Merge

After a pull request is merged, the branch should be deleted to keep the repository tidy. The issue linked to the pull request should close automatically if the PR uses the correct `Closes #issue-number` syntax.
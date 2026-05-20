# Strix Team Agreement

This team agreement defines how the Strix team agrees to work together. It is intended to support a DevOps culture based on shared ownership, respect, visibility, fast feedback and continuous improvement.

The agreement is not only a list of repository rules. It describes the behaviours the team expects from each member when planning work, reviewing changes, responding to failures and improving the delivery process.

## 1. Purpose

The purpose of this agreement is to make the team’s working expectations clear before major project code is added to the repository. Strix is being developed through a simulated cross-functional DevOps workflow, so the team needs shared principles for how work is planned, reviewed, integrated and improved.

This agreement ideally should help the team avoid unclear ownership, hidden work, slow reviews and blame-based responses to failure.

## 2. Team Culture

The Strix team will treat DevOps as a shared way of working rather than just a set of tools or pipeline stages. Development, testing, review, deployment and reliability are shared responsibilities.

Successes and failures belong to the team. A successful release is treated as a team achievement, while a failed build, defect or deployment problem is treated as feedback about the system. The team will focus on learning and improvement rather than blame.

Team members are expected to show respect, ask for help early, give feedback constructively and document decisions that affect future work. This helps reduce knowledge silos and supports a healthier review process where pull requests are used for learning as well as quality control.

## 3. Shared Working Principles

The team agrees to keep work visible and traceable. Planned work should begin as a GitHub issue, move onto a short-lived branch and then be reviewed through a pull request before it is merged into `main`.

Each team member should work on their own branch and avoid pushing directly to `main`. Pull requests should be linked to their related GitHub issue so that the reason for each change is clear. Large features should be broken into smaller issues where possible so that work is easier to review and integrate.

Team members should use clear commit messages, follow the agreed PR and issue templates and keep the project board updated as work progresses.

## 4. Communication and Blockers

The team should communicate blockers early. If a task becomes unclear, too large or technically blocked, the issue or pull request should be updated so the rest of the team can see the problem.

The team should avoid leaving blocked work hidden on long-running branches. If needed, the work should be split into smaller tasks, reviewed with the Product Owner or moved back into the backlog until the scope can be made clearer.

Important decisions should be recorded in the relevant issue or pull request so that the reasoning remains available later.

## 5. Review Culture

Pull request review is used for both quality control and knowledge sharing. Reviewers should check that the change is understandable, focused and aligned with the related issue. They should also consider whether the change affects tests, configuration, deployment, security or future maintainability.

At least one other team member should review a pull request before it is merged. Approvals should not be treated as a rubber stamp. Reviewers should leave clear and constructive comments where they see a concern or possible improvement.

The author should respond to review comments and resolve conversations before merging. The aim is not to “win” a review, but to improve the change and share understanding across the team.

## 6. Build Responsibility

Each team member is responsible for the impact of their changes on the full project, not only the specific class, feature or test they worked on. If a pull request causes existing tests to fail or breaks the CI pipeline, the contributor must investigate and fix the issue before the work can be merged.

The team agrees that a passing local test is not enough if the full CI build fails. The repository should remain in a working state and broken builds should be treated as a priority.

## 7. Handling Failures and Post-Mortems

Build failures, failed checks, defects and deployment problems should be handled in a blameless way. The focus should be on identifying what happened, why it happened, how it was resolved and what can be improved.

Where a failure reveals a weakness in the workflow, the team should consider whether a new test, check, document or automation step is needed. Failures should be treated as useful feedback about the system rather than as personal mistakes.

For significant failures the team should hold a short blameless post-mortem. The outcome should be practical actions rather than blame. Any follow-up work should be captured as GitHub issues so it can be tracked.

## 8. Continuous Improvement

The team will use short retrospectives after major milestones or repeated issues. A retrospective should cover what worked well, what caused friction and what should be improved in the next sprint.

The agreement itself can change as the project develops. If the team identifies a better way to work, the agreement should be updated through the same issue, branch, pull request and review process as the rest of the repository.

## Agreement

By contributing to this repository the team agrees to follow the working principles described in this document.

| Name | Role |
|---|---|
| Jamie | Product Owner, Lead Developer |
| James | Backend and Machine Learning |
| Niamh | Backend and DevOps |

Date Created: 19th April 2026

# Code Review Guidelines

This document defines how pull request reviews should be carried out in the Strix repository. Code review is used as both a quality control step and a knowledge sharing practice.

## 1. Purpose of Code Review

Code review is not only a gate before merging. It is a way for the team to improve code quality, to share knowledge across the project and to catch issues before they reach `main`.

Reviewers should focus on whether the change is understandable, safe, maintainable and aligned with the related issue.

## 2. Review Expectations

A reviewer should check that:

- the pull request links to a GitHub issue
- the change matches the purpose of the issue
- the code is readable and reasonably simple
- the change is not larger than necessary
- relevant tests have been added or updated where appropriate
- the CI build passes
- configuration, security or deployment changes are clearly explained
- the README or project documentation is updated if the change affects usage or workflow

## 3. Review Behaviour

Review comments should always be clear, respectful and constructive. The aim is to improve the work not to criticise the person who wrote it.

Reviewers should explain why they are requesting a change where possible. Authors should respond to comments openly and avoid treating feedback as a personal criticism.

Approvals should not be used as a rubber stamp. If a reviewer is unsure about something they should ask a question rather than approve silently.

## 4. Required Review Before Merge

Every pull request into `main` must receive at least one approval before it can be merged.

The required reviewer may depend on the area being changed:

- IDS, feature extraction or anomaly detection changes should involve James.
- Target application, GitHub Actions, workflow or deployment changes should involve Niamh.
- Product scope, documentation or release decisions should involve Jamie.

This ownership model is intended to guide review routing but it does not prevent other team members from reading, commenting on or learning from any pull request.

## 5. Handling Review Comments

If a reviewer leaves a comment the author should either make the requested change or reply with a clear explanation.

Conversation threads should be resolved before merge. If the discussion reveals a larger issue a follow-up GitHub issue should be created rather than leaving the concern hidden in the pull request.

## 6. CI and Build Failures

A pull request should not be merged while the CI build is failing.

If the build fails the author should investigate the cause, push a fix and briefly explain the issue in the pull request. Reviewers should check that the fix is sensible before approving.

A failed build should be treated as useful feedback from the pipeline not as a personal mistake.

## 7. Review Checklist

Before approving a pull request reviewers should consider:

- Does the change solve the linked issue?
- Is the change small enough to review properly?
- Is the code understandable and maintainable?
- Are tests or manual checks appropriate for this change?
- Does the CI build pass?
- Are any security, configuration or deployment impacts explained?
- Is follow-up work needed?

## 8. Merging

Once the pull request has an approval, passing CI and all conversations are resolved it can be merged into `main`.

After merge the source branch should be deleted and any linked issue should be closed automatically through the pull request.
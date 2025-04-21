# Monora - Budget Better, Live Freer

![Repo Logo](./assets/monora_canvas.png)

Monora is a smart budgeting and expense tracking app with AI-powered financial suggestions.
This app is developed as a Software Engineering project by Group 35.

## Screenshots

1. User login & register page.
<p align="center">
  <img src="./assets/screenshot_login.png" width="45%" />
  <img src="./assets/screenshot_register.png" width="45%" />
</p>

2. Placeholder for other screenshots.

## Project Structure

```
├─ .github
│  └─ workflows
├─ assets
├─ misc
└─ se_app
    ├─ .idea
    │   └─ runConfigurations
    ├─ .mvn
    │   └─ wrapper
    ├─ app_data (persistent data folder, not present in repo)
    │   ├─ data
    │   └─ logs
    ├─ src
    │   ├─ main (main app)
    │   └─ test/java
    └─ target (build output)
```

## Prerequisites

- Java Development Kit (JDK) 24 or higher
- Apache Maven 3.8 or higher
- IntelliJ IDEA (required for streamlined compilation and running)

## Setup & Run

1. Clone the repository and navigate to the project directory:

```bash
git clone <repository-url>
cd se_app
```

2. Open the `se_app` directory in IntelliJ IDEA and import it as a Maven project.
3. Select the **Standard Run** configuration (Maven goal: `javafx:run`).
4. Run the configuration to launch the application with the project JDK.

## Development Guidelines

### Commit Messages

Use the following commit message structure:

```
<type>: <concise summary in imperative mood with first letter capitalized>

<optional detailed explanation>
```

Supported commit types:

| Type       | Description                                               |
|------------|-----------------------------------------------------------|
| `feat`     | New feature                                               |
| `fix`      | Bug fix                                                   |
| `docs`     | Documentation updates                                     |
| `refactor` | Refactoring (no behavior changes)                         |
| `test`     | Adding or modifying tests                                 |
| `chore`    | Routine tasks (build process or configuration)            |

Example commit messages:

```bash
feat: Add global logging utility
fix: Resolve login hash mismatch
refactor: Extract validation logic
docs: Update logger setup guide
```

### Branching & Workflow

**1. Branch Name:**

All development is done on separate branches based on `main`. Branch naming convention:

```
<category>/<short-descriptive-name>
```

Supported categories are the same with commit types. Example branch names:

```bash
feature/logger-integration
fix/login-validation
```

**2. Workflow:**

1. **Branch, Commit, Push**: Create a feature branch off `main`, commit changes, and push to remote.
2. **Update with Main**: Update your branch with latest `main` before opening PR. (Or do so after PR is opened)
3. **Open a PR**: Create a PR to merge your branch into `main`.
4. **Review & CI**: Request a reviewer from the team. The PR must pass CI and receive at least one approval.
5. **Auto-Merge & Cleanup**: Approved PRs merge automatically, and the head branch deletes automatically. To restore it, use the restore option in the PR.

### Pull Requests, Reviews & Branch Protection

To ensure high-quality contributions and maintain project stability, adhere to the following guidelines:

**1. Pull Requests:**
- The PR title must clearly summarize the changes, following the commit summary conventions.
- Include a detailed description outlining the purpose of the changes and referencing any related issues, if applicable.
- All PRs must be reviewed and approved by at least one other team member before merging.

**2. Branch Protection & Continuous Integration:**
- The `main` branch is protected with the following enforced rules:
  - Mandatory pull request reviews before merging.
  - Required status checks passing successfully.
  - Force pushes and deletions are prohibited.

- Continuous Integration automatically validates project compilation on every push or pull request targeting the `main` branch, reinforcing code stability before merging.


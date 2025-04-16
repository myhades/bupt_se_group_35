# Monora - Budget Better, Live Freer
Monora is a smart budgeting and expense tracking app with AI-powered financial suggestions.
This app is developed as the Software Engineering project by Group 35.

## Usage

### 1. Placeholder

## Build

### 1. Placeholder

## Workflow and Conventions

### 1. Branching Strategy

All development work must be done on separate branches created from the main or develop branch. Branches must be named according to the following convention:

```
<category>/<short-descriptive-name>
```

#### 1.1. Supported Categories

| Category   | Description                                                  | Example                            |
|------------|--------------------------------------------------------------|------------------------------------|
| `feature`  | For implementing new features                                | `feature/logger-integration`       |
| `bugfix`   | For fixing bugs discovered during development or testing     | `bugfix/login-validation`          |
| `hotfix`   | For critical fixes to be applied immediately on production   | `hotfix/crash-on-launch`           |
| `refactor` | For structural code changes without altering functionality   | `refactor/settings-handler`        |
| `test`     | For adding or modifying test cases                           | `test/user-authentication`         |
| `docs`     | For documentation-only changes                               | `docs/setup-instructions`          |
| `chore`    | For maintenance tasks, build script updates, or dependencies | `chore/gradle-version-update`      |

#### 1.2. Rules

- Always branch from the latest `main` or `develop`.
- Never commit directly to `main`.
- Merge must occur via pull requests.

---

### 2. Commit Message Guidelines

Each commit message must follow a consistent structure to ensure readability and maintainability.

#### 2.1. Format

```
<type>: <concise summary in imperative mood>

<optional body for detailed explanation>
```

#### 2.2. Supported Types

| Type       | Usage Description                                      |
|------------|--------------------------------------------------------|
| `feat`     | A new feature                                           |
| `fix`      | A bug fix                                               |
| `docs`     | Documentation updates                                   |
| `style`    | Changes related to formatting and style (no code logic)|
| `refactor` | Code refactoring (no behavior change)                  |
| `perf`     | Performance improvements                                |
| `test`     | Adding or modifying tests                               |
| `chore`    | Routine tasks such as build process or configuration   |

#### 2.3. Examples

```bash
feat: implement global logging utility
fix: resolve password hash mismatch during login
refactor: extract user validation logic to separate method
docs: update README with logger setup instructions
```

**Note:** The summary line should be written in the imperative mood, as if giving a command (e.g., “add” instead of “added”).

---

### 3. Pull Requests and Code Review

All changes must be submitted via pull requests. The following rules apply:

- The branch must be up-to-date with the target branch before merging.
- The pull request title should be clear and follow the same format as commit messages.
- Include a description of the change, purpose, and any relevant issue references.
- Pull requests must be reviewed and approved by at least one other team member before merging.
- Squash merge is preferred unless otherwise specified.
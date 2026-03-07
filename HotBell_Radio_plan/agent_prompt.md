# Agent Prompt: HotBell Radio

**Project:** HotBell Radio
**Mode:** Android Native (`mobile` - Pure Kotlin)

## Available Tools & Capabilities
*   **Browser:** Used strictly for reading external Android/Kotlin official documentation if an API is unknown. Do NOT browse randomly.
*   **Terminal:** Used for executing Gradle tasks (`./gradlew build`, `./gradlew lint`, `./gradlew test`), adb commands, and git operations.
*   **File System:** Used for scaffolding the project, writing Android XML/Compose Kotlin files, and managing resources.
*   **Code Inspection:** Read existing codebase before modifying any file. Always review the previous module's implementation to ensure compatibility and reuse.

## Context Links
*   **PRD:** `../PRD.md` (Product rules and requirements)
*   **Architecture:** `../modules.md` (Global styling, data models, tech stack)
*   **Module Specs:** `../modules/` (Implement these sequentially)
*   **API Refs:** `../reference/api_radio-browser.md`

## Data Initialization Strategy
*   **Incremental:** Initialize Room Database entities module-by-module. Module 0 sets up the abstract `AppDatabase`, Module 2 creates `AlarmEntity`, Module 3 creates `FavoriteStationEntity`.
*   Ensure Room schema exports are enabled in `build.gradle` for migration safety.

## File & Folder Conventions
*   Android standard package structure: `com.hotbell.radio`
*   Features grouped by package (e.g., `com.hotbell.radio.alarms`, `com.hotbell.radio.player`, `com.hotbell.radio.ui`).
*   Compose screen files should be suffixed with `Screen.kt` (e.g., `HomeScreen.kt`).
*   Database entities in `com.hotbell.radio.data`.

## Environment Variables
*   None required for the Radio Browser API (it is fully open without API keys).
*   If testing against a specific test network, define it in `local.properties`.

## Validation Commands
*   `./gradlew assembleDebug` (Must pass without errors before user review).
*   `./gradlew lintDebug` (Must not have fatal errors).
*   `./gradlew ktlintCheck` (If configured, ensure code style compliance).

## Automated Acceptance Gate
After running validation commands and before requesting human review:
1.  Check ALL binary pass/fail criteria from the current module's Testing section.
2.  Run dependency vulnerability scan (Android Studio lint or Gradle dependency check). **Block on HIGH severity findings.**
3.  Confirm no exact alarm leaks (alarms remain set indefinitely without a device).
4.  Verify all Radio API inputs (Search text) are sanitized.
*Only request user review if all above checks pass.*

## Self-Reflection Step (Post-Module)
Before calling `notify_user` for review, append findings to `../execution_log.md`:
1.  Were any security risks introduced?
2.  Is there any code duplication from previous modules?
3.  Are there performance concerns with the Compose UI or ExoPlayer buffering?

## Testing Requirements
*   Execute the module-specific test scenarios detailed in each `modules/*.md` Testing section. Ensure you test edge cases, especially network failure for the radio stream.

## Git Strategy
*   Commit prefix: `feat(module-[X]): [description]` or `fix(module-[X]): [description]`.
*   Commit after every successfully validated module. Wait for user review before proceeding to the next.

## Failure Protocol & Recovery State
*   Diagnose & fix up to **3 attempts**.
*   If unresolved after 3 attempts, **rollback** via `git stash` or `git reset --hard` to the last successful commit.
*   Log the complete error in `progress.json` and request user guidance. **NEVER proceed with a broken build.**
*   Update `../progress.json` after each completed module: `{"last_completed": "module-X", "status": "success"}`.

## Execution Log & Decisions
*   **Log:** Update `../execution_log.md` with components built and new library dependencies.
*   **Decisions:** Document significant architectural choices (like choosing MediaSessionService vs MediaBrowserService) in `../decisions.md` using ADR format (Context -> Options -> Decision -> Consequences).

## Evaluation Loop (Every 3 Modules)
*   Pause for a mid-project health check after Module 2.
*   Evaluate: Is Room DB structured correctly? Is the app handling background execution efficiently?
*   Suggest refactors if needed in `../decisions.md`.

## Token Budget Awareness
*   Keep the current `module-X.md` and `modules.md` in full context.
*   Summarize older modules to `{module, files_created, exports}` when context grows too large.

## Anti-Patterns
*   **NEVER** modify `agent_prompt.md`, `PRD.md`, `modules.md`, or any file in `[project]_plan/`! These are read-only execution blueprints.
*   **NEVER** leave TODO or FIXME comments in completed modules.
*   **NEVER** use generic colors; use the exact Hex codes defined in `modules.md`.
*   **NEVER** upgrade dependencies without explicit instruction.

## User Review Checkpoint
*   Strict pause. Call the `notify_user` tool and wait for explicit approval before moving to the next module.

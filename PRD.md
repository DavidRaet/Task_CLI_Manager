# CLI Task Manager — Minimal PRD

## Goal

Build a small command-line app for managing tasks. It should demonstrate records, enums, collections, streams, `Optional`, and file I/O without adding features beyond the core workflow.

## Minimal acceptance criteria

- A task stores an ID, title, priority, completion status, and creation time. Model the task as a record and priority as an enum with at least three levels.
- From the CLI, a user can add a task, list tasks, mark a task complete by ID, and delete a task by ID.
- Listing can filter by completion status and priority, and sort by priority or creation time. Filtering and sorting operate on the in-memory collection using streams.
- Looking up a task by ID handles a missing task safely with `Optional`: the command reports that no matching task exists and does not crash.
- Tasks are saved to a local file. On startup, the app loads saved tasks; after a change, it saves them so they remain available after restart.
- Invalid input (such as an unknown priority or malformed ID) produces a clear message and leaves saved data intact.

## CLI contract used by the tests

- `add <PRIORITY> <TITLE>` creates a task. Priorities are `LOW`, `MEDIUM`, and `HIGH`; output includes `ID: <id>`.
- `list` accepts `--status open|completed`, `--priority LOW|MEDIUM|HIGH`, and `--sort priority|created`.
- Priority sorting puts `HIGH` before `MEDIUM` before `LOW`; creation sorting puts older tasks first.
- `complete <ID>` and `delete <ID>` act on one task. An unknown ID reports that no task matched.
- Run one command per process. The app reads and writes its task file in the process working directory, so a fresh process can reload it.

## Out of scope

Due dates, tags, recurring tasks, editing task titles, interactive menus, multiple users, and remote or database storage.

## Done when

A user can add tasks, view them with a filter and sort, complete or delete one, exit, restart the app, and see the remaining tasks preserved.

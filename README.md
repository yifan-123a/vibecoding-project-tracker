# VibeTracker — Vibecoding Project Tracker

A Kanban tool that tracks vibecoding tasks **and the context that produced them**. Started as the
**UXD Vibecoding Hackathon 2026** starter; this fork is a solo build on a different stack.

What this fork has:

- **PRD** (`PRD.md`) — scope, data model, priorities, definition of done (filled in, v2).
- **DESIGN.md** — palette, typography, component rules, voice (filled in, v2).
- **One Spring Boot app** — Thymeleaf on the server, plain CSS/JS in the browser, H2 on disk.
- The task model as a JPA entity, with the due-state logic derived in `TaskService`.
- **User-editable state colours** — the six semantic colours are CSS variables, changed from a drawer.

Built so far: the board, the colour drawer, seeding, persistence. Next: the add/edit/delete modal, the
Context field, and the Copy Context button — the demo's hero moment.

## Quick start

```bash
# 1. Clone your fork
git clone https://github.com/<your-team>/<your-fork>.git
cd <your-fork>

# 2. Run it — needs a JDK. The build targets Java 25 (Boot 3.5 works on 21+).
mvn spring-boot:run
```

Open `http://localhost:8080`. The board renders on the server from an embedded H2 database that is seeded
with six demo tasks on first start, so there is nothing to install and no frontend build.

> This repo was originally a Vite + React scaffold. It is now a single Spring Boot application with
> Thymeleaf templates — see `PRD.md` §6 for why. The old frontend files are gone; `git log` has them if you
> are curious.

## Commit rhythm

Milestone tags are **not graded** in this run (confirmed by the course lead), so they are kept only as a
progress log. What still matters is one commit per working slice:

```bash
git add . && git commit -m "M5 crud-modal: add/edit/delete + persistence"
git push
```

| # | Tag (optional) | What it meant | Status here |
|---|---|---|---|
| 1 | `git-linked` | Repo registered on the portal | done |
| 2 | `prd-done` | PRD reviewed and filled in | done |
| 3 | `design-done` | DESIGN.md filled in, tokens defined | done |
| 4 | `data-model` | Four-column board renders with demo tasks | done |
| 5 | `crud-modal` | Add / edit / delete via a modal, persisted | next |
| 6 | `tag-style` | Feature vs. bug visual distinction | done |
| 7 | `task-owner` | Assignee badge + reassign | partial (badge only) |
| 8 | `due-tint` | Cards tint by deadline | done |
| 9 | `context` | Context field on the task modal | next |
| 10 | `copy-prompt` | One-click copy of the Markdown briefing | next |
| 11 | `project-deliverables` | Deliverable anchor banner | later |
| 12 | `secret-sauce` | The one thing that makes it yours | later |
| 13 | `pitch-ready` | Demo dataset + rehearsed pitch | later |

## Repo map

```
.
├── PRD.md                     ← scope, data model, priorities (v2, solo)
├── DESIGN.md                  ← palette, type, component rules, voice
├── README.md                  ← you are here
├── pom.xml                    ← Spring Boot 3.5 · Java 25 · web, thymeleaf, data-jpa, h2
├── docs/
│   ├── design-spec.md         ← FR/NFR, routes, module layout, build order
│   └── shots/                 ← screenshots of the current board and drawer
└── src/main/
    ├── java/de/uxd/vibetracker/
    │   ├── task/              Task, statuses, TaskService (board + due states)
    │   ├── settings/          the six user-editable colours
    │   ├── web/               BoardController, SettingsController
    │   └── seed/              six demo tasks on first start
    └── resources/
        ├── application.yml    port 8080, H2 file db at ./data
        ├── templates/index.html
        └── static/css/app.css · static/js/app.js
```

## Deploy

There is nothing to deploy separately: `mvn package` produces one executable JAR.

```bash
java -jar target/vibetracker-0.1.0.jar
```

For the live demo, run it locally — predictable latency, no cold start, and the seeded database is already
in `./data`. A public host works too (Render, Fly, Railway); warm it a few minutes before the pitch.

## A word about prompts

Once M9 (`context`) is done, keep the Context field of each real task up to date as you work. By the end of the hackathon you should have a handful of curated context blocks on real tasks — those become part of your Module 5 "Vibe Coding History" deliverable. Free credit, just by using your own tool.

---

*UXD Vibecoding Hackathon 2026 · Prof. Dr. Ignacio Alvarez*

# Vibecoding Project Tracker — Design Spec

> Status: draft v1 · derived from the 13 milestone slides (M1–M13), `PRD.md` and `DESIGN.md`.
> Scope decision: milestone tags are treated as optional bookkeeping. What follows is driven by
> **functional requirements (FR)** and **non-functional requirements (NFR)** only.

---

## 1. Product in one paragraph

A single-page Kanban tool for a 3-person vibecoding team. It tracks *tasks* (what to build) **and**
the *AI context* behind each task (the prompt, the tool, the outcome), so a teammate — or another AI —
can pick a card up and continue without re-deriving everything. The hero interaction is
**"Copy Context"**: one click turns a card into a paste-ready Markdown briefing.

---

## 2. Functional requirements

| ID | Requirement | Source | Priority |
|---|---|---|---|
| FR-1 | Board renders exactly four columns: To Do, In Progress, Review, Done. | M4 | Must |
| FR-2 | A task has `id, title, description, type, status, assignee, dueDate, createdDate` and is stored server-side. | M4 | Must |
| FR-3 | Board is pre-populated with 3–4 demo tasks on a fresh database. | M4 | Must |
| FR-4 | Create task via a "+" button that opens a modal containing every task field. | M5 | Must |
| FR-5 | Edit task by clicking its card — same modal, pre-filled. | M5 | Must |
| FR-6 | Delete task from the modal, behind a confirm step. | M5 | Must |
| FR-7 | Status is changed inside the modal (dropdown). No drag-and-drop. | M5 | Must |
| FR-8 | All mutations survive a page reload (persistence). | M5 | Must |
| FR-9 | `feature` vs `bug` tasks are visually distinct everywhere a task renders (color + icon). | M6 | Must |
| FR-10 | Each card shows the assignee; the modal offers "Hand off to…" which reassigns and confirms visually. | M7 | Must |
| FR-11 | Cards are tinted by due date: >48 h calm, <24 h warning, past-due alarm, Done neutral. Auto-updates, no user action. | M8 | Must |
| FR-12 | Modal has a `context` textarea, an optional AI-tool dropdown, and an auto-set "last updated N ago" label. | M9 | Must |
| FR-13 | "Copy Context" copies the task as formatted Markdown to the clipboard, with a toast. | M10 | Must |
| FR-14 | Four deliverable slots (Presentation, Demo, Report, Documentation) pinned above the board, each with a URL field and an empty/filled indicator, persisted. | M11 | Should |
| FR-15 | One self-chosen differentiator that is visible in the demo and breaks nothing else. | M12 | Should |
| FR-16 | A curated demo dataset and a rehearsed 5-minute pitch. | M13 | Must |
| FR-17 | The user recolours the six semantic states (feature, bug, due ×4) from a settings panel; the choice persists and can be reset. | mid-hack added | Should |

**Out of scope** (deliberately): auth, real GitHub integration, mobile layout, multi-team, real-time collaboration.

---

## 3. Non-functional requirements

| ID | Requirement | Target / how it is measured |
|---|---|---|
| NFR-1 | **Persistence & durability** | Every mutation is committed before the UI confirms it; reload restores identical state. Data lives in the Spring Boot DB, not only in the browser. |
| NFR-2 | **Demo resilience** | If the API is unreachable, the board still renders from a local cache and shows an "offline" badge. No blank screen during the pitch. |
| NFR-3 | **Latency** | Read API p95 < 150 ms locally; board first paint < 1 s; modal open < 100 ms. |
| NFR-4 | **Accessibility** | Full keyboard path (Tab order, Esc closes modal, focus trapped in modal, focus returns to the invoking card), `aria-label` on icon buttons, contrast ≥ 4.5:1 for body text. |
| NFR-5 | **Consistency** | Every color/spacing value comes from `tailwind.config.js` tokens. Same status/type/due semantics on board, card and modal. |
| NFR-6 | **Maintainability** | One data-access interface for the whole UI; components never call `fetch` directly. No magic strings for statuses or types. |
| NFR-7 | **Security (minimum viable)** | Server-side validation of every payload, no secrets in the frontend bundle, CORS restricted to known origins. |
| NFR-8 | **Deployability** | Frontend: Vercel static build. Backend: single executable JAR with an embedded DB; API base URL injected at build time via `VITE_API_URL`. |
| NFR-9 | **Time budget** | Everything above reachable in ~3 h by 1–2 people. Anything that cannot be cut in 5 minutes is a risk and needs a fallback (see §9). |
| NFR-10 | **Desktop-first layout** | Optimised for 1280–1920 px; degrades without breaking at 1024 px. |
| NFR-11 | **User-chosen colours stay readable** | Every editable colour is measured against white; < 4.5:1 warns, < 3:1 warns harder, none are blocked. Text on solid fills picks its own ink (`readableInk`). Colour is never a hard-coded literal in a component. |

---

## 4. Data model

```
Task
  id                 UUID (string, server-generated)
  title              string, required, 1..120
  description        string, required, ≤ 2000
  type               enum FEATURE | BUG                     (FR-9)
  status             enum TODO | IN_PROGRESS | REVIEW | DONE (FR-1/FR-7)
  assignee           string, required, one of TEAM
  dueDate            LocalDate | null                        (FR-11)
  createdDate        LocalDate, server defaults to today
  context            text, default ""                        (FR-12)
  contextTool        enum CLAUDE | CHATGPT | CURSOR | LOVABLE | REPLIT | OTHER | null
  contextUpdatedAt   Instant | null, set by the server on save

AnchorSlot                                           (FR-14)
  key                enum PRESENTATION | DEMO | REPORT | DOCUMENTATION  (primary key)
  url                string | null   → empty state when null/blank
  updatedAt          Instant
```

Invariants: `status = DONE` always renders due-neutral regardless of `dueDate`; a blank/whitespace
`url` counts as *empty*, not *filled*.

---

## 5. Architecture

```
browser  ──HTTP(forms)──►  Spring Boot 3.5 (Java 25)
                              controller → service → repository (JPA) ──► H2 (file)
                              │
                              └── Thymeleaf template + inline CSS variables + app.css
```

One process, one port, no client state. Every interaction is a normal request:

| Route | Method | Purpose |
|---|---|---|
| `/` | GET | Render the board: four columns, cards, colour rows, theme variables |
| `/settings` | POST | Save the six colours (form fields), redirect back to `/` |
| `/settings/reset` | POST | Restore DESIGN.md defaults, redirect back to `/` |
| `/tasks/new`, `/tasks/{id}`, `/tasks/{id}/delete` | GET/POST | M5 CRUD modal — next milestone |
| `/css/app.css`, `/js/app.js` | GET | Static assets |

Deliberately **not** built: a JSON API. The server renders HTML; adding a REST surface nobody consumes is
extra contract to maintain. If it is ever needed, the service layer is already the seam.

Rationale for dropping the SPA: the board is four filtered lists. A client app would add a build step, a
fetch layer, a loading state, a cache, and a second copy of the state — for a screen that the server can
render in one pass.

---

## 6. Frontend design

### 6.1 Page structure (`templates/index.html`)

```
<header class="topbar">       wordmark · tagline · [Colours] · [+ Task]
<main class="board">          4 × <section class="column">
  <header class="column-head">  label · count
  <div class="cards">          <article class="card …"> × n  |  <p class="empty">
      .card-meta                 chip (type) · due label
      .card-title
      .card-desc                 (2-line clamp)
      .card-foot                 avatar · assignee · "Hand off" disclosure
<footer class="legend">       4 colour-key dots: 2+ days / 24 h / past due / done
<aside class="drawer">        6 × .color-row (label · hint · swatch · hex · note) · Save · Reset
<div class="modal-layer">     ?create / ?edit={id} → the task form
<div class="toast">           flash message after save, reset, delete, hand-off
```

The card's state lives in its class list — `card type-bug is-overdue` — and the CSS resolves the colour.
That keeps `SettingsService` free of presentation strings and makes the template readable six months later.

### 6.2 No client store (the important decision)

There is no state layer, no fetch wrapper and no cache: the database is the only source of truth and the
HTML is the only copy. Consequences, accepted knowingly:

- Reload = fresh read. No optimistic update, no stale cache, nothing to invalidate.
- Two browser tabs stay consistent by accident, not by design.
- The colour preview *is* client state, but it is write-only (it mutates a CSS variable) and the server
  overwrites it on save. Nothing reads it back.
- The demo has one dependency: the JAR must be running. Since it runs on the laptop with an embedded H2
  file, that dependency has no network hop and no cold start.

### 6.3 Derived logic (single source of truth, Java side)

```java
// TaskService — computed per request, never stored
DueState dueState(Task t, LocalDate today) {
    if (t.status == DONE || t.dueDate == null) return NEUTRAL;
    long days = DAYS.between(today, t.dueDate);
    if (days < 0) return OVERDUE;
    if (days == 0) return WARNING;   // "inside the next 24 h"
    return SAFE;                     // 1..n days; the 24–48 h gap folds here
}
```

`TaskService.toView()` turns that into a `TaskView` record (title, type, assignee, initials, dueState,
dueLabel) so the template renders strings and never does date arithmetic.

`TaskService.serializeContext(task)` will produce the M10 Markdown block:

```markdown
## Task
ID-101 · Implement User Authentication · feature · To Do · assigned to Liam Chen

## Context
**Background**  …
**Constraints** …
**Tried so far** …
**Pick up**     …
```

---

## 7. Design tokens (CSS variables)

| Token | Role | User-editable |
|---|---|---|
| `--c-brand-primary` | header, primary button, focus ring | no |
| `--c-brand-accent` | wordmark dot, links | no |
| `--c-surface-page`, `--c-surface-card`, `--c-surface-sunken` | backgrounds | no |
| `--c-ink-primary`, `--c-ink-muted` | body, captions | no |
| `--c-line-hairline` | 1 px borders | no |
| `--c-type-feature`, `--c-type-bug` | card stripe + chip | **yes** |
| `--c-due-safe`, `--c-due-warning`, `--c-due-overdue`, `--c-due-neutral` | card tint + due label | **yes** |

```css
/* app.css — defaults, once */
:root { --c-type-bug: #DC2626; }
```

```html
<!-- index.html — the user's values, inline, before the stylesheet paints -->
<style th:utext="${themeCss}"></style>
```

`SettingsService.themeCss()` builds that block from the database row; `SettingsService.rows()` supplies the
editor's labels and the contrast note. Because the override arrives in the HTML head, there is no flash of
default colours and no client-side theme store. Tints use `color-mix(in srgb, var(--c-due-warning) 8%, #fff)`,
so any user-chosen colour produces a readable surface without pre-computing RGBA values.

---

## 8. Module layout (as built)

```
pom.xml                                      web, thymeleaf, data-jpa, validation, h2, devtools
src/main/java/de/uxd/vibetracker/
├── VibeTrackerApplication.java
├── task/    Task · TaskType · TaskStatus · DueState · TaskRepository
│            TaskService · TaskView · ColumnView
├── settings/ Settings · SettingsRepository · SettingsService · SettingsForm · ColorRow
├── web/     BoardController (GET /) · SettingsController (POST /settings, /settings/reset)
└── seed/    SeedData — 6 demo tasks, dates relative to today
src/main/resources/
├── application.yml                          port 8080, H2 file db, ddl-auto=update
├── templates/index.html                     the whole page, one template
└── static/css/app.css · static/js/app.js    ~600 lines CSS, ~100 lines JS
```

Notes for Java 25: Spring Boot 3.5.x, no Lombok (annotation processing on a brand-new JDK is pure friction),
`ddl-auto=update` so a fresh clone needs no migration, and `context`/`description` are stored as plain
strings rather than a JSON blob because nothing needs to query inside them.

---

## 9. Why Spring Boot — and what it costs

| For | Against |
|---|---|
| Shared state across 3 laptops — the real reason to have a backend at all | +40–60 min scaffolding, entities, CORS, DTO mapping |
| Persistence survives a cleared browser, unlike localStorage (NFR-1) | Free Java hosting is weak: Vercel cannot run a JAR; Render free tier sleeps → 30–60 s cold start |
| "Full-stack" credibility in the grading; FR-2 says the task model is the contract | More moving parts to break on stage; JSON date/enum mismatches cost debugging time |

**Verdict: Spring Boot, server-rendered, one process.** The cost that mattered was not the backend but the
second runtime: a SPA would have meant two builds, an API contract, CORS, and a client copy of the state.
Thymeleaf removes all four, and the colour picker still works because the theme is a CSS variable either way.

Deployment: the JAR on the laptop is the demo. A public deployment is optional and, if attempted, must be
warmed a few minutes before the pitch.

---

## 10. Build order and time budget

| Slot | Work | Exit criterion |
|---|---|---|
| done | PRD v2 + DESIGN v2 + tokens | no placeholder markers left |
| done | Spring Boot scaffold: entities, services, controllers, seeder, H2 | `mvn package` runs, board renders 6 seeds |
| done | FR-1/2/3/9/11/17: four columns, cards, type colour, due tint, colour drawer | board + live recolour verified by screenshot |
| done | FR-4..FR-7 (M5): add / edit / delete via a modal, form POST + redirect | create, edit, delete survive a reload |
| done | FR-10 (M7): assignee + reassign | badge on card, assignment saved from the modal |
| later | FR-12/13 (M9/M10): context field + Copy Context Markdown | pasted block reads cleanly in an AI chat |
| later | FR-14/15/16 (M11–M13): anchors, differentiator, demo data | rehearsed 5-minute talk track |

---

## 11. Open questions

1. ~~Team name + tagline + teammate names~~ — settled: solo, VibeTracker, `TEAM = [Yifan, AI]`.
2. ~~Palette~~ — settled in DESIGN.md §2, and now user-editable anyway.
3. FR-15 differentiator: collapsible columns, drag-to-move, or something else?
4. Does the "+ Task" button open a centred modal or a full-page form? Modal matches M5's wording.

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
React 18 + Vite + Tailwind (SPA)
        │  fetch, JSON, camelCase
        ▼
Spring Boot 3.5 (REST, Java 25)  ──►  H2 (file-based, demo)
   controller → service → repository (JPA)
        │
        ▼
write-through cache in localStorage  (NFR-2 fallback)
```

Local dev: Vite on `:5173` proxies `/api` → Spring Boot on `:8080` (avoids CORS entirely in dev).
Deployed: `VITE_API_URL` points at the hosted API; CORS allowlist holds the Vercel origin.

### 5.1 API contract

| Method | Path | Body | Response |
|---|---|---|---|
| GET | `/api/tasks` | – | `200 [TaskDTO]` |
| POST | `/api/tasks` | `TaskCreateDTO` | `201 TaskDTO` · `400` validation |
| PUT | `/api/tasks/{id}` | `TaskCreateDTO` | `200 TaskDTO` · `404` |
| PATCH | `/api/tasks/{id}` | partial (`status`, `assignee`, `context`) | `200 TaskDTO` |
| DELETE | `/api/tasks/{id}` | – | `204` |
| GET | `/api/anchors` | – | `200 [AnchorDTO]` (always 4 rows) |
| PUT | `/api/anchors/{key}` | `{ "url": "…" }` | `200 AnchorDTO` |
| GET | `/api/settings` | – | `200 { colors: { token: "#RRGGBB" }, updatedAt }` |
| PUT | `/api/settings` | `{ colors: { … } }` | `200` · `400` on a malformed hex |
| GET | `/api/health` | – | `200 {"status":"UP"}` |

`TaskDTO` mirrors the entity with `dueDate` as `"YYYY-MM-DD"` and `contextUpdatedAt` as ISO-8601.
Errors use the Spring problem shape `{timestamp, status, error, message, path}`; the UI maps
`message` to a toast.

---

## 6. Frontend design

### 6.1 Component tree

```
App
├── Header            product name · tagline · [+ Task]
├── DeliverableBanner 4 × AnchorCard (label, url input, empty/filled dot)      FR-14
├── Board             4 × Column
│   └── Column        title · count · EmptyState
│       └── TaskCard  type chip · title · assignee avatar · due pill           FR-9/10/11
├── TaskModal         create | edit                                            FR-4..7/12/13
│   ├── fields: title, description, type, status, assignee, dueDate
│   ├── ContextSection  textarea · AI-tool select · "updated 2 h ago"
│   └── footer: Copy Context · Delete (confirm) · Cancel · Save
├── SettingsDrawer    6 × ColorRow (label · swatch · hex · contrast note) · Reset   FR-17
└── ToastStack        copy / handoff / error feedback
```

### 6.2 Data access seam (the important decision)

One module, two implementations, identical async interface:

```js
// src/data/store.js
export const store = import.meta.env.VITE_DATA_SOURCE === 'api'
  ? createHttpStore(import.meta.env.VITE_API_URL)   // Spring Boot
  : createLocalStore();                             // localStorage only
```

Both expose `listTasks / createTask / updateTask / deleteTask / getAnchors / setAnchor / getSettings / setSettings`.
The HTTP store writes every successful response through to `localStorage`; on network failure it
returns the cached snapshot and sets an `offline` flag. Consequence: **the whole UI can be built
against the local store first and switched to Spring Boot with a one-line env change** — and the
demo survives a dead backend (NFR-2).

### 6.3 Derived logic (single source of truth)

```js
dueState(task)  → 'overdue' | 'warning' | 'safe' | 'neutral'
    done                → neutral
    no dueDate          → neutral-less (no tint)
    daysUntil < 0       → overdue
    hoursUntil < 24     → warning
    else                → safe        // 24–48 h gap documented in DESIGN.md
```

`serializeContext(task)` produces the M10 Markdown block:

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

## 7. Design tokens (CSS variables → Tailwind)

| Token | Role | User-editable |
|---|---|---|
| `brand-primary` | header, primary button, focus ring | no |
| `brand-accent` | wordmark dot, copy affordance, links | no |
| `surface-page`, `surface-card` | backgrounds | no |
| `ink-primary`, `ink-muted` | body, captions | no |
| `line-hairline` | 1 px borders | no |
| `type-feature`, `type-bug` | card accent stripe + icon | **yes** |
| `due-safe`, `due-warning`, `due-overdue`, `due-neutral` | card tint | **yes** |

Implementation, decided once so M6 and M8 never need a rewrite:

```css
/* src/index.css */
:root { --c-type-bug: 220 38 38; }        /* space-separated RGB */
```

```js
// tailwind.config.js
const token = (name) => `rgb(var(--c-${name}) / <alpha-value>)`;
```

`src/theme.js` owns the defaults, validation, `applyTheme()`, and the contrast helpers. A settings change
sets one property on `document.documentElement`; every Tailwind utility (`bg-type-bug`, `border-due-warning/30`)
follows, and the `/opacity` modifiers keep working because of `<alpha-value>`.

---

## 8. Backend module layout

```
backend/
├── pom.xml                                  spring-boot-starter-web, -data-jpa, -validation, h2
└── src/main/
    ├── java/de/uxd/vibetracker/
    │   ├── VibetrackerApplication.java
    │   ├── config/WebConfig.java            CORS allowlist
    │   ├── task/  Task, TaskType, TaskStatus, TaskRepository, TaskService,
    │   │          TaskController, TaskDto, TaskMapper, TaskNotFoundException
    │   ├── anchor/ AnchorKey, AnchorSlot, AnchorRepository, AnchorController
    │   └── seed/   DemoData.java            CommandLineRunner: 4 tasks + 4 anchor slots
    └── resources/application.yml            port 8080, H2 file db, ddl-auto=update
```

Notes for Java 25: use Spring Boot 3.5.x, skip Lombok (annotation processing on the newest JDK is
pure friction — write the getters or use a `record` for DTOs), and set
`spring.jpa.hibernate.ddl-auto=update` so a fresh clone needs no migration.

---

## 9. Why Spring Boot — and what it costs

| For | Against |
|---|---|
| Shared state across 3 laptops — the real reason to have a backend at all | +40–60 min scaffolding, entities, CORS, DTO mapping |
| Persistence survives a cleared browser, unlike localStorage (NFR-1) | Free Java hosting is weak: Vercel cannot run a JAR; Render free tier sleeps → 30–60 s cold start |
| "Full-stack" credibility in the grading; FR-2 says the task model is the contract | More moving parts to break on stage; JSON date/enum mismatches cost debugging time |

**Verdict: yes, use Spring Boot — but never as a single point of failure.** Order of work:
build and green the UI against the local store, *then* switch `VITE_DATA_SOURCE=api`. If the backend
misbehaves at minute 150, the flip back is one env line, not a rewrite.

Deployment recommendation: keep the API on the laptop for the live demo (predictable latency),
push a Render deployment only if there is spare time; pre-warm it 5 minutes before the pitch.

---

## 10. Build order and time budget

| Slot | Work | Exit criterion |
|---|---|---|
| 0:00–0:25 | PRD §8/§9/§11 + DESIGN.md §1–5 + tokens into `tailwind.config.js` | no `<TODO>` left; M2/M3 taggable |
| 0:25–0:55 | Backend scaffold: entity, repository, controller, seeder, CORS | `curl /api/tasks` returns the 4 seeds |
| 0:55–1:35 | FR-1..FR-8: board + modal + CRUD against the local store, then API | create/edit/delete survives reload |
| 1:35–2:05 | FR-9..FR-11: type styling, owner + handoff, due tint | four due states visibly distinct |
| 2:05–2:25 | FR-12/FR-13: context field + Copy Context Markdown | paste into a text editor reads cleanly |
| 2:25–2:40 | FR-14: deliverable banner | four slots persist |
| 2:40–2:55 | FR-15/FR-16: differentiator + demo dataset | curated board, rehearsed talk track |
| 2:55–3:00 | Deploy, smoke test, buffer | reachable URL, no console errors |

---

## 11. Open questions

1. Team name + tagline + the three teammate names (needed for `TEAM` and PRD §11).
2. Brand palette — or accept a default and adjust in DESIGN.md.
3. FR-15 differentiator: drag-and-drop, sounds, collapsible columns, or something else?
4. Hosting: laptop-only demo, or a public Render deployment?

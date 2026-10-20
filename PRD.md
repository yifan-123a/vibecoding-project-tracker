# Vibecoding Project Tracker — PRD

**Status:** v2 · solo edition · every section filled in, zero placeholder markers · 2026-10-20

**What changed from the starter v1:**
1. Scope is now a **one-person build** (confirmed with the course lead) — no three-way split.
2. Persistence moved from localStorage-only to a **Spring Boot API + H2**, with localStorage kept as a
   write-through cache so the demo never depends on the backend being up.
3. Milestone tags are **optional bookkeeping**, not a grading mechanism. See §13.

---

## 1. Problem

Vibecoding projects have two recurring pains, and going solo makes both worse rather than better:

1. **Tasks get lost.** A hundred small things, no shared view, and no record of what state each one is
   actually in. Solo, the "shared view" is the inside of your head — which does not survive a weekend.
2. **Prompts get lost.** The AI produced working code two days ago, but nobody remembers the prompt
   that produced it. When it is time to iterate, you start from zero and pay for the same reasoning twice.

A spreadsheet doesn't solve this. A real project tool (Linear, Jira, Notion) is overkill, knows nothing
about prompts, and takes hours to set up. Solo, the third pain is **thread loss**: every new chat window
starts amnesiac, and you re-explain the same constraints every single time.

## 2. Target users

- **Primary:** the builder — one UXD student using this daily for the rest of the semester, across many
  AI sessions per day.
- **Secondary:** future cohorts of vibecoders taking the same class, and any teammate who joins later.

We are designing the tool we ourselves will use. The pitch demo is also our first real user test.

## 3. Product overview

A single-page web app with two persistent surfaces:

- A **Kanban board** where vibecoding tasks move through four states.
- An **Anchor Board** above it that pins the four final-project deliverables (Presentation, Demo, Report,
  Documentation).

Each task carries the context that produced it, so the builder — or any AI model, or a future teammate —
can click one button and copy the task plus its briefing back into a chat to continue the work.

## 4. Primary user flow

1. Open the web app. The board loads from the API; if the API is unreachable it renders the last cached
   snapshot and shows an offline badge.
2. Click "+" to add a task: title, type, assignee, due date, description, and the context briefing.
3. Change status in the modal as work progresses (no drag-and-drop — see DESIGN.md §4).
4. When stuck, click **Copy Context** on a task and paste it into Claude / ChatGPT / Cursor to continue.
5. Reassign a task to `AI` or back to yourself — the solo version of "hand off to a teammate".
6. Drop deliverable links into the Anchor Board as they are produced.

## 5. Data model

Locked. Do not change without a deliberate decision (and a migration note).

```js
// A single vibecoding task.
Task = {
  id: string,                    // uuid, server-generated
  title: string,                 // 1..120 chars
  description: string,           // <= 2000 chars
  type: 'feature' | 'bug',       // visual color coding (M6)
  status: 'todo' | 'in-progress' | 'review' | 'done',
  assignee: string,              // one of the values in §8
  dueDate: string | null,        // ISO date 'YYYY-MM-DD'
  createdDate: string,           // ISO date 'YYYY-MM-DD', defaults to today

  // Added in later milestones:
  context: string,               // M9 — curated briefing for the next AI / teammate
  contextTool: string | null,    // M9 — 'Claude' | 'ChatGPT' | 'Cursor' | 'Lovable' | 'Replit' | 'Other'
  contextUpdatedAt: string|null, // M9 — ISO timestamp, set by the server on save
}

// One of four pinned deliverable slots — always four rows, never fewer.
Anchor = {
  id: 'presentation' | 'demo' | 'report' | 'documentation',  // primary key
  label: string,                 // human-readable
  url: string | null,            // the link pasted in; blank counts as empty
  updatedAt: string,             // ISO timestamp
}
```

Wire format is JSON with camelCase keys; dates are `YYYY-MM-DD` strings, timestamps are ISO-8601.
Invariants: a task with `status = 'done'` always renders due-neutral regardless of `dueDate`; a whitespace
`url` is treated as empty, not filled.

## 6. Tech stack

| Layer | Choice | Why |
|---|---|---|
| UI | React 18 + Vite | What the scaffold ships with. Don't migrate. |
| Styling | Tailwind, tokens in `tailwind.config.js` | Already configured. Utility classes only, no separate CSS files. |
| State | `useState` + custom hooks | One `useTasks` hook owns the collection; no Redux for this size. |
| Data access | A single `store` module with two implementations (`local`, `http`) | Components never call `fetch`. One env var switches backends. |
| API | **Spring Boot 3.5**, Java 25, Spring Web + Data JPA + Validation | Real persistence, real CRUD contract, and it doubles as the M5 data model. |
| Database | H2, file-based (`jdbc:h2:file:./data/vibetracker`) | Zero-install, survives restarts, no migration scripts. |
| Cache | localStorage, write-through | If the API dies, the board still renders. Demo safety. |
| Deploy | Vercel (static frontend) · JAR on the laptop for the live demo | Vercel cannot host a JVM; a free-tier JVM host cold-starts in 30–60 s, which is a pitch risk. |

> **Decision:** the backend is deliberate, not scope creep — it is what makes `assignee`, `context` and
> `contextUpdatedAt` real shared state instead of browser-local trivia. But the **demo must never depend on
> it**: `VITE_DATA_SOURCE=local` is the one-line fallback.

## 7. Stage names

Four columns, in this order, with these exact labels in the UI:

1. **To Do**
2. **In Progress**
3. **Review**
4. **Done**

## 8. Team

Solo build — confirmed with the course lead. One human, plus the AI as a working partner.

| Slot | Name | GitHub handle |
|---|---|---|
| Builder | Yifan | `yifan-123a` |
| AI partner (pseudo-assignee) | AI | — |

These are the **only** allowed values for `Task.assignee`. No auth — just a dropdown. `AI` exists so the
ownership and hand-off behaviour (M7) is still demonstrable without a second human in the room: a task can
be handed to the model and back.

## 9. Build order (solo)

Priority is the order to cut, not the order to build: **P0 ships or there is no demo**, P1 is the visual
layer that makes it feel finished, P2 is only if the clock is generous.

| # | Tag (optional) | Module | Priority | Done when |
|---|---|---|---|---|
| 1 | `git-linked` | Repo + portal registration | done | tag exists, webhook ping green |
| 2 | `prd-done` | This document | P0 | zero placeholder markers, team + identity filled |
| 3 | `design-done` | DESIGN.md + tokens in `tailwind.config.js` | P0 | palette and type in the config, no hex literals in components |
| 4 | `data-model` | Task entity + four-column board | P0 | board renders 4 seeded tasks from `/api/tasks` |
| 5 | `crud-modal` | Add / edit / delete modal | P0 | create, edit, delete all survive a reload |
| 6 | `tag-style` | Feature / bug distinction | P1 | color + icon everywhere a task renders |
| 7 | `task-owner` | Assignee badge + reassign | P1 | badge on card, reassignment acknowledged with a toast |
| 8 | `due-tint` | Due-date tinting | P1 | four states visibly distinct, updates without user action |
| 9 | `context` | Context field on the modal | P0 | textarea + AI-tool select + "updated N ago", persisted |
| 10 | `copy-prompt` | Copy Context button | P0 | Markdown block pastes cleanly into an AI chat |
| 11 | `project-deliverables` | Deliverable anchor banner | P2 | four slots accept a URL and survive reload |
| 12 | `secret-sauce` | One differentiator | P2 | visible in the demo, breaks nothing else |
| 13 | `pitch-ready` | Demo dataset + rehearsal | P0 | 5-minute talk track timed and rehearsed |

**Dependency chain.** M2 + M3 are documents — do them first because M6 and M8 consume the colors and M4
consumes the names from §8. M4 unblocks everything in M5–M8. M9 must land before M10 (Copy Context has
nothing to copy otherwise). M11 and M12 are the first things to cut when the clock runs out.

## 10. AI / vibe coding angle

Where AI is heavily used:
- M4, M5: scaffold the board and the modal from a single prompt each.
- M9, M10: write the Markdown serialization helper and the clipboard fallback.
- M3: generate palette variants to react to — the choice stays human.

Where AI will **not** save time:
- M2 (this document) — read it yourself.
- M3 (DESIGN.md) — taste is yours, not the model's.
- M12 (secret-sauce) — the whole point is that it isn't predictable.
- M13 (pitch) — write the script in your own voice.

Keep the Context field of every real task up to date as you work. By the end of the hackathon there should
be a handful of curated context blocks on real tasks — those become part of the Module 5 "Vibe Coding
History" deliverable, at zero extra cost.

## 11. Team identity

- **Team name:** VibeTracker
- **Tagline (one sentence):** Track the task, keep the context.
- **Tracker URL after deploy:** `http://localhost:5173` during the hackathon (Vercel URL added after the
  first deploy).

## 12. Out of scope (do not build)

- Authentication / accounts / user management.
- Real GitHub integration (branch, PR status) — visual mock at most, and only under M12.
- Mobile responsive layout — desktop-first, 1280 px and up.
- Multi-team / multi-tenant support.
- Real-time collaboration, websockets, presence.
- Notifications, email, file uploads.
- Dark mode (see DESIGN.md §7).

## 13. Definition of done

Milestone tags are **not graded** and not required; they stay in this document only as a progress log.
The hackathon is done when:

- All **P0** modules in §9 work end to end on the deployed build, with no console errors.
- The board survives a reload, and survives the API being down (cached snapshot + offline badge).
- The "Copy Context" demo works on stage, pasted into a real AI chat.
- The 5-minute pitch is rehearsed and timed.

---

*PRD version: v2 (solo, backend-enabled) · adapted from hackathon-starter v1 by Prof. Dr. Ignacio Alvarez*

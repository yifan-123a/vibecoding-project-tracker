# Vibecoding Project Tracker — Design

**Status:** v2 · filled in · 2026-10-20 · owner: the builder (solo)

**Why this file exists.** This tracker is your tool. It should look like *your* tool — not a generic Kanban
with default Tailwind blue. Twenty minutes of design decisions here are visible on every screen for the next
six weeks of Module 5.

**Who owns this.** Solo build: the same person designs and builds. That removes the hand-off risk and raises
the risk of "generic default" — so §2 and §4 below are binding: nobody types a hex literal into a component.

---

## 1. Mood / vibe

*A quiet instrument, not a dashboard: the board should read like a well-kept notebook — high contrast where
it matters, silent everywhere else.*

References (not links to copy, but the qualities to steal):

- **Linear** — density without clutter; colour used only as a state signal, never as decoration.
- **Things 3** — generous whitespace and one accent, so the eye knows where to land.
- **Editorial print** — strong hierarchy from weight and spacing rather than from boxes and borders.

Anti-references — what we are explicitly **not** trying to look like:

- A Jira screenshot: grey on grey, twelve borders per card, everything shouting at once.
- A "productivity startup" landing page: gradients, glassmorphism, emoji in headings, purple-to-pink anything.
- A spreadsheet: no colour semantics, no hierarchy, dates as raw strings.

## 2. Color palette

These are the only colors allowed. They are declared once in `tailwind.config.js`; components consume Tailwind
classes such as `bg-surface-card` or `border-due-warning`. **No hex literals outside the config.**

Rule of use: the four *state* colours (type + due) are the only saturated things on screen. Everything else
is slate. If a screen has more than three saturated elements, it is wrong.

### Brand

| Token | Hex | Where it shows up |
|---|---|---|
| `brand-primary` | `#1D4ED8` | Wordmark, "+ Task" button, focus rings, active column header |
| `brand-accent` | `#F59E0B` | The dot in the wordmark, "Copy Context" affordance, hover underline on links |
| `surface-page` | `#F8FAFC` | Page background (one step off white, so cards can be pure white) |
| `surface-card` | `#FFFFFF` | Card background, modal background, anchor slots |
| `ink-primary` | `#0F172A` | Body text, card titles |
| `ink-muted` | `#64748B` | Captions, dates, counts, placeholders |

### Task type (M6 `tag-style`)

| Token | Hex | When used |
|---|---|---|
| `type-feature` | `#10B981` | Cards tagged `feature` — 3 px left stripe + a small `Sparkles` icon |
| `type-bug` | `#DC2626` | Cards tagged `bug` — 3 px left stripe + a small `Bug` icon |

### Due-date states (M8 `due-tint`)

Accent hex below; card backgrounds use the same hex at 10 % opacity with a 30 % opacity border, so tinting
stays readable at card size.

| Token | Hex | When used |
|---|---|---|
| `due-safe` | `#64748B` | More than 2 days out — calm, reads as "not now" |
| `due-warning` | `#F59E0B` | Less than 24 hours — same amber as the accent, deliberately familiar |
| `due-overdue` | `#DC2626` | Past due — no new red, reuse the bug red so the system stays small |
| `due-neutral` | `#94A3B8` | Done (always overrides the date), and tasks with no due date |

Known gap inherited from the milestone spec: the 24–48 h window is not defined. Decision: fold it into
`due-safe`; revisit only if a real task ever feels mis-signalled there.

## 3. Typography

| Role | Font | Why |
|---|---|---|
| Heading | **Inter**, 600 | Neutral grotesque; keeps the board businesslike and matches the UI text so there is one voice. |
| Body | **Inter**, 400 | Same family — hierarchy comes from size and weight, not from a second typeface. |
| Monospace (IDs, tags, the copied Markdown preview) | **JetBrains Mono**, 400 | Distinguishes machine-readable text from prose at a glance; also what the copied block will look like. |

Loaded from Google Fonts in `index.html` with `display=swap`; if the network blocks it, the stack falls back
to `system-ui` / `ui-monospace` and nothing breaks (NFR-2 applies to fonts too).

Sizes (do not invent new ones):

- Page title: 24 px / 600
- Section header: 12 px / 600 / 0.08 em tracking / uppercase / `ink-muted`
- Card title: 14 px / 500
- Body: 14 px / 400
- Caption, dates, counts: 12 px / 400 / `ink-muted`
- Code / IDs: 12 px / 400 / mono

## 4. Component principles

One sentence per element; these are the rules the build phase is checked against.

- **Cards:** flat white, 1 px `#E2E8F0` border, 8 px radius, no shadow at rest — a 2 px shadow lift on hover is
  the only motion, and the left stripe carries the type.
- **Buttons:** solid `brand-primary`, 8 px radius, 10 px × 16 px padding, no gradients, 150 ms colour
  transition; destructive actions are outline-red, never solid red.
- **Modal:** centred, max-width 640 px, backdrop `#0F172A` at 50 %, focus trapped, `Esc` closes, focus returns
  to the card that opened it.
- **Empty states:** dashed 1 px border, `ink-muted` text, one encouraging line, never an illustration.
- **Drag affordance:** none — status changes through the modal dropdown. Dragging a card is M12 territory, and
  only if it can be done without breaking the board.
- **Toasts:** bottom-centre, `ink-primary` background, white text, 200 ms fade, auto-dismiss at 2.5 s, one at a
  time.

## 5. Voice / microcopy

The tone: a competent colleague, short sentences, no exclamation marks, no cutesy filler.

| Where | Text |
|---|---|
| "+" button label | `+ Task` |
| Empty column placeholder | `Nothing here yet — keep going.` |
| Toast after "Copy Context" | `Copied. Paste it into your AI.` |
| Confirm-delete message | `Delete this task? It won't come back.` |
| Reassign toast (M7 `task-owner`) | `Reassigned to {name}.` |
| Offline badge (when the API is down) | `Offline — showing the last saved board.` |
| Context "last updated" caption | `Updated 2 h ago` |

## 6. Logo / wordmark

The tracker doesn't need a logo, but it needs a name and a wordmark style.

- **Product name:** VibeTracker (matches PRD §11)
- **Wordmark style:** "Vibe" in Inter 400 and "Tracker" in Inter 600, both `brand-primary`, followed by a
  single `brand-accent` full stop — `Vibe**Tracker**.` No icon, no box, no gradient. The full stop is the only
  ornament in the entire UI.

## 7. Out of scope (this hackathon)

To keep design tight, the following are explicitly not part of `design-done`:

- A dark mode toggle. Pick one mode and ship it.
- Multiple themes. One brand, applied consistently.
- Animations beyond a 200 ms fade on toasts and a 150 ms hover on cards.
- A custom icon set. Use [Lucide](https://lucide.dev) icons if needed — three or four of them, maximum.
- Illustration or empty-state artwork.

---

*DESIGN.md version: v2 (solo, token-bound) · adapted from hackathon-starter v1*

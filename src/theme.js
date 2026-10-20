/**
 * Runtime theme layer.
 *
 * The user picks the colours for the semantic states (feature / bug / the four
 * due states) themselves — DESIGN.md §2 lists the defaults, not the law.
 *
 * How it works: every colour is a CSS custom property (`--c-<token>`), declared
 * in `src/index.css` and mapped into Tailwind in `tailwind.config.js`. Changing a
 * variable repaints the whole app instantly — no rebuild, no class swapping.
 *
 * Contract for components: never write a hex literal, never read a hex literal
 * to compute a colour; use `bg-type-bug`, `border-due-warning/30` and let the
 * variable do the work.
 */

/** Defaults — must match the `:root` block in src/index.css. */
export const DEFAULT_THEME = {
  'brand-primary': '#1D4ED8',
  'brand-accent': '#F59E0B',
  'surface-page': '#F8FAFC',
  'surface-card': '#FFFFFF',
  'ink-primary': '#0F172A',
  'ink-muted': '#64748B',
  'line-hairline': '#E2E8F0',
  'type-feature': '#10B981',
  'type-bug': '#DC2626',
  'due-safe': '#64748B',
  'due-warning': '#F59E0B',
  'due-overdue': '#DC2626',
  'due-neutral': '#94A3B8',
};

/** The six tokens the settings panel may rewrite. Everything else is structural. */
export const EDITABLE_TOKENS = [
  'type-feature',
  'type-bug',
  'due-safe',
  'due-warning',
  'due-overdue',
  'due-neutral',
];

/** Human labels for the settings panel. */
export const THEME_LABELS = {
  'type-feature': 'Feature task',
  'type-bug': 'Bug task',
  'due-safe': 'Due in more than 2 days',
  'due-warning': 'Due within 24 hours',
  'due-overdue': 'Past due',
  'due-neutral': 'Done / no due date',
};

export const STORAGE_KEY = 'vibetracker.theme';

/** '#abc' | 'abc' | '#AABBCC' -> '#AABBCC', anything else -> null. */
export function normalizeHex(value) {
  if (typeof value !== 'string') return null;
  let v = value.trim().replace(/^#/, '');
  if (v.length === 3) v = v.split('').map((c) => c + c).join('');
  if (!/^[0-9a-f]{6}$/i.test(v)) return null;
  return `#${v.toUpperCase()}`;
}

/** '#1D4ED8' -> '29 78 216' (the form `rgb(var(--x) / <alpha>)` needs). */
export function hexToRgbTriplet(hex) {
  const normalized = normalizeHex(hex);
  if (!normalized) return null;
  return [1, 3, 5]
    .map((i) => parseInt(normalized.slice(i, i + 2), 16))
    .join(' ');
}

/** Merge an untrusted object over the defaults; invalid values fall back. */
export function resolveTheme(candidate) {
  const theme = { ...DEFAULT_THEME };
  if (!candidate || typeof candidate !== 'object') return theme;
  for (const token of EDITABLE_TOKENS) {
    const hex = normalizeHex(candidate[token]);
    if (hex) theme[token] = hex;
  }
  return theme;
}

/** Bind the theme to the document root. Safe to call with junk. */
export function applyTheme(theme, root) {
  const target = root ?? (typeof document !== 'undefined' ? document.documentElement : null);
  if (!target) return;
  const resolved = resolveTheme(theme);
  for (const [token, hex] of Object.entries(resolved)) {
    target.style.setProperty(`--c-${token}`, hexToRgbTriplet(hex));
  }
}

export function readStoredTheme() {
  try {
    return resolveTheme(JSON.parse(window.localStorage.getItem(STORAGE_KEY)));
  } catch {
    return resolveTheme(null);
  }
}

export function storeTheme(theme) {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(resolveTheme(theme)));
  } catch {
    /* quota / private mode — the theme still applies for this session */
  }
}

// --- Contrast guard -------------------------------------------------------
// A user-chosen colour can be unreadable. Rather than forbid it, we measure it
// and let the UI warn; text on solid fills picks its own ink automatically.

function channelToLinear(channel) {
  const c = channel / 255;
  return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
}

export function relativeLuminance(hex) {
  const normalized = normalizeHex(hex);
  if (!normalized) return 0;
  const [r, g, b] = [1, 3, 5].map((i) => parseInt(normalized.slice(i, i + 2), 16));
  return (
    0.2126 * channelToLinear(r) +
    0.7152 * channelToLinear(g) +
    0.0722 * channelToLinear(b)
  );
}

export function contrastRatio(a, b) {
  const [lighter, darker] = [relativeLuminance(a), relativeLuminance(b)].sort((x, y) => y - x);
  return (lighter + 0.05) / (darker + 0.05);
}

/** Black or white text on top of `backgroundHex`, whichever reads better. */
export function readableInk(backgroundHex) {
  return contrastRatio(backgroundHex, '#FFFFFF') >= contrastRatio(backgroundHex, '#0F172A')
    ? '#FFFFFF'
    : '#0F172A';
}

/**
 * @returns {{level:'warn'|'error', message:string}|null} null when the colour is
 * safe for text, a warning when it is only safe for stripes/icons.
 */
export function contrastWarning(hex, against = '#FFFFFF') {
  const ratio = contrastRatio(hex, against);
  if (ratio >= 4.5) return null;
  const rounded = ratio.toFixed(1);
  if (ratio >= 3) {
    return {
      level: 'warn',
      message: `Contrast ${rounded}:1 — fine for stripes and icons, weak for text.`,
    };
  }
  return {
    level: 'error',
    message: `Contrast ${rounded}:1 — too low to read. Pick a darker or lighter colour.`,
  };
}

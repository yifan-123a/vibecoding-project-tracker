/** @type {import('tailwindcss').Config} */

// Every colour is a CSS variable (see src/index.css), so the palette can be
// re-bound at runtime without rebuilding Tailwind. `<alpha-value>` keeps the
// `/10`, `/30` opacity modifiers working: bg-due-warning/10 etc.
const token = (name) => `rgb(var(--c-${name}) / <alpha-value>)`;

export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Brand
        'brand-primary': token('brand-primary'),
        'brand-accent': token('brand-accent'),
        'surface-page': token('surface-page'),
        'surface-card': token('surface-card'),
        'ink-primary': token('ink-primary'),
        'ink-muted': token('ink-muted'),
        'line-hairline': token('line-hairline'),

        // Task type — M6 tag-style (user-editable)
        'type-feature': token('type-feature'),
        'type-bug': token('type-bug'),

        // Due-date states — M8 due-tint (user-editable)
        'due-safe': token('due-safe'),
        'due-warning': token('due-warning'),
        'due-overdue': token('due-overdue'),
        'due-neutral': token('due-neutral'),
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'monospace'],
      },
      borderRadius: {
        card: '0.5rem',
      },
      transitionDuration: {
        fast: '150ms',
      },
    },
  },
  plugins: [],
};

/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      // Every colour in the app comes from here — see DESIGN.md §2.
      // No hex literals inside components.
      colors: {
        // Brand
        'brand-primary': '#1D4ED8',
        'brand-accent': '#F59E0B',
        'surface-page': '#F8FAFC',
        'surface-card': '#FFFFFF',
        'ink-primary': '#0F172A',
        'ink-muted': '#64748B',
        'line-hairline': '#E2E8F0',

        // Task type — M6 tag-style
        'type-feature': '#10B981',
        'type-bug': '#DC2626',

        // Due-date states — M8 due-tint (use with /10 background, /30 border)
        'due-safe': '#64748B',
        'due-warning': '#F59E0B',
        'due-overdue': '#DC2626',
        'due-neutral': '#94A3B8',
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

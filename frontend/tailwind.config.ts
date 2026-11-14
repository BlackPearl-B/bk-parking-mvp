import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        surface: {
          app: 'var(--surface-app)',
          card: 'var(--surface-card)',
          subtle: 'var(--surface-subtle)',
          inverse: 'var(--surface-inverse)',
          hover: 'var(--surface-hover)',
        },
        border: {
          default: 'var(--border-default)',
          muted: 'var(--border-muted)',
          subtle: 'var(--border-subtle)',
          strong: 'var(--border-strong)',
        },
        text: {
          primary: 'var(--text-primary)',
          secondary: 'var(--text-secondary)',
          muted: 'var(--text-muted)',
          inverse: 'var(--text-inverse)',
        },
        success: {
          DEFAULT: 'var(--success-default)',
          hover: 'var(--success-hover)',
          surface: 'var(--success-surface)',
          border: 'var(--success-border)',
          text: 'var(--success-text)',
        },
        warning: {
          DEFAULT: 'var(--warning-default)',
          hover: 'var(--warning-hover)',
          surface: 'var(--warning-surface)',
          border: 'var(--warning-border)',
          text: 'var(--warning-text)',
        },
        danger: {
          DEFAULT: 'var(--danger-default)',
          hover: 'var(--danger-hover)',
          surface: 'var(--danger-surface)',
          border: 'var(--danger-border)',
          text: 'var(--danger-text)',
        },
        info: {
          DEFAULT: 'var(--info-default)',
          hover: 'var(--info-hover)',
          surface: 'var(--info-surface)',
          border: 'var(--info-border)',
          text: 'var(--info-text)',
        },
      },
      width: {
        sidebar: 'var(--size-sidebar)',
      },
      height: {
        topbar: 'var(--size-topbar)',
        actionbar: 'var(--size-actionbar)',
      },
      minHeight: {
        touch: 'var(--size-touch)',
      },
      fontSize: {
        display: ['4.5rem', { lineHeight: '4.875rem', fontWeight: '700' }],
      },
    },
  },
  plugins: [],
} satisfies Config;


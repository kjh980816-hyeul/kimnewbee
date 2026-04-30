/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        pepper: 'var(--color-pepper)',
        'pepper-deep': 'var(--color-pepper-deep)',
        corn: 'var(--color-corn)',
        cheek: 'var(--color-cheek)',
        paper: 'var(--color-paper)',
        surface: 'var(--color-surface)',
        elevated: 'var(--color-elevated)',
        border: 'var(--color-border)',
        ink: 'var(--color-ink)',
        'ink-muted': 'var(--color-ink-muted)',
        chzzk: 'var(--color-chzzk)',
        naver: 'var(--color-naver)',
      },
      fontFamily: {
        sans: ['Pretendard', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
};

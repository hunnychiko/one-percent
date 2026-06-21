import type { Config } from 'tailwindcss'

const config: Config = {
  content: ['./src/**/*.{js,ts,jsx,tsx,mdx}'],
  theme: {
    extend: {
      colors: {
        bg:        '#0E1420',
        card:      '#1A2230',
        'card-lt': '#202D40',
        surface:   '#253045',
        primary:   '#5B6BF8',
        'primary-dark': '#4355D8',
        secondary: '#7C5CBF',
        gold:      '#FFD700',
        success:   '#4CAF50',
        error:     '#E53E3E',
        warning:   '#FF9800',
        't-primary':   '#FFFFFF',
        't-secondary': '#8A9BB5',
        't-muted':     '#4A5568',
        divider:   '#253045',
      },
      fontFamily: {
        sans: ['Pretendard', 'Apple SD Gothic Neo', 'sans-serif'],
      },
      animation: {
        'pulse-slow': 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow': 'spin 3s linear infinite',
      },
    },
  },
  plugins: [],
}

export default config

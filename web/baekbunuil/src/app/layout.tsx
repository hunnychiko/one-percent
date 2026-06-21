import type { Metadata } from 'next'
import './globals.css'
import { Header }    from '@/components/Header'
import { BottomNav } from '@/components/BottomNav'

export const metadata: Metadata = {
  title:       '1/100 백분의일 — 연승 추첨',
  description: '광고 보고 티켓 획득 → 가위바위보 연승 → 1/100 추첨 당첨!',
  icons:       { icon: '/favicon.ico' },
  openGraph: {
    title:       '1/100 백분의일',
    description: '연승하면 경품에 도전하세요!',
    type:        'website',
  },
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <Header />
        <main className="max-w-screen-md mx-auto px-4 pt-4 pb-24">
          {children}
        </main>
        <BottomNav />
      </body>
    </html>
  )
}

'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

const LINKS = [
  { href: '/admin',              label: '대시보드' },
  { href: '/admin/products',     label: '상품 관리' },
  { href: '/admin/affiliate',    label: '제휴 배너' },
  { href: '/admin/draws',        label: '추첨 관리' },
  { href: '/admin/winners',      label: '당첨자 관리' },
]

export function AdminSidebar() {
  const pathname = usePathname()

  return (
    <aside className="w-48 shrink-0 bg-card rounded-2xl p-3 h-fit sticky top-20">
      <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide mb-3 px-2">관리자</p>
      <nav className="space-y-1">
        {LINKS.map(({ href, label }) => {
          const active = pathname === href
          return (
            <Link
              key={href}
              href={href}
              className={`block px-3 py-2 rounded-xl text-sm font-medium transition-colors ${
                active
                  ? 'bg-primary/20 text-primary'
                  : 'text-gray-400 hover:bg-card-lt hover:text-white'
              }`}
            >
              {label}
            </Link>
          )
        })}
      </nav>
      <div className="mt-4 pt-4 border-t border-card-lt">
        <Link href="/" className="block px-3 py-2 rounded-xl text-xs text-gray-500 hover:text-white transition-colors">
          ← 홈으로
        </Link>
      </div>
    </aside>
  )
}

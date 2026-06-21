'use client'

import { useState } from 'react'
import { useProducts } from '@/hooks/useProducts'
import { ProductCard }  from '@/components/ProductCard'
import { GRADE_CONFIG, type ProductRoom } from '@/lib/types'

type GradeFilter = 'ALL' | ProductRoom['grade']

const GRADE_FILTERS: GradeFilter[] = ['ALL', 'C', 'B', 'A', 'S', 'SS']

export default function HomePage() {
  const { products, loading }           = useProducts()
  const [filter, setFilter]             = useState<GradeFilter>('ALL')
  const [statusFilter, setStatusFilter] = useState<'all' | 'open'>('open')

  const filtered = products
    .filter((p) => filter === 'ALL' || p.grade === filter)
    .filter((p) => statusFilter === 'all' || p.drawStatus === 'open')

  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div
        className="rounded-2xl p-5 relative overflow-hidden"
        style={{
          background: 'linear-gradient(135deg, #1A2230 0%, #202D40 50%, #1A2230 100%)',
          border: '1px solid rgba(91,107,248,0.3)',
        }}
      >
        <div className="relative z-10">
          <p className="text-xs text-primary font-semibold mb-1">1/100 추첨 시스템</p>
          <h1 className="text-2xl font-black text-white mb-2">
            연승하면<br />
            <span className="text-gradient">경품이 내 것!</span>
          </h1>
          <p className="text-sm text-gray-400 leading-relaxed">
            광고 보기 → 승부권 획득 → 가위바위보 연승<br />
            → 100명 추첨 → 당첨
          </p>
        </div>
        <div
          className="absolute -right-6 -top-6 w-32 h-32 rounded-full opacity-10"
          style={{ background: 'radial-gradient(circle, #5B6BF8, transparent)' }}
        />
      </div>

      {/* Filters */}
      <div className="space-y-3">
        <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
          {GRADE_FILTERS.map((g) => (
            <button
              key={g}
              onClick={() => setFilter(g)}
              className={`shrink-0 px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                filter === g
                  ? 'bg-primary text-white'
                  : 'bg-card-lt text-gray-400 hover:text-white'
              }`}
              style={
                filter === g || g === 'ALL' ? undefined :
                { borderColor: GRADE_CONFIG[g as ProductRoom['grade']].color + '44', border: '1px solid' }
              }
            >
              {g === 'ALL' ? '전체' : GRADE_CONFIG[g as ProductRoom['grade']].label}
            </button>
          ))}
        </div>

        <div className="flex gap-2">
          {(['open', 'all'] as const).map((s) => (
            <button
              key={s}
              onClick={() => setStatusFilter(s)}
              className={`px-3 py-1 rounded-lg text-xs font-medium transition-colors ${
                statusFilter === s ? 'text-white bg-card-lt' : 'text-gray-500'
              }`}
            >
              {s === 'open' ? '진행중' : '전체'}
            </button>
          ))}
        </div>
      </div>

      {/* Product Grid */}
      {loading ? (
        <div className="grid grid-cols-2 gap-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="card h-52 shimmer" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          <p className="text-3xl mb-2">🎁</p>
          <p>해당 조건의 상품이 없습니다</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3">
          {filtered.map((p) => (
            <ProductCard key={p.roomId} product={p} />
          ))}
        </div>
      )}

      {/* App Download Banner */}
      <div className="card text-center py-6">
        <p className="text-2xl mb-2">📱</p>
        <p className="font-bold text-white mb-1">앱으로 도전하기</p>
        <p className="text-xs text-gray-400 mb-3">가위바위보 배틀은 앱에서만 가능해요</p>
        <button className="btn-primary text-sm px-6 py-2">
          앱 다운로드 (준비 중)
        </button>
      </div>
    </div>
  )
}

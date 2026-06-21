'use client'

import { use } from 'react'
import Link from 'next/link'
import { useProduct } from '@/hooks/useProducts'
import { useAuth }    from '@/hooks/useAuth'
import { GradeBadge }    from '@/components/GradeBadge'
import { StreakStars }   from '@/components/StreakStars'
import { ParticipantBar } from '@/components/ParticipantBar'
import { productEmoji, GRADE_CONFIG } from '@/lib/types'

export default function ProductDetailPage({ params }: { params: Promise<{ roomId: string }> }) {
  const { roomId }       = use(params)
  const { product, loading } = useProduct(roomId)
  const { user }         = useAuth()

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="h-52 rounded-2xl shimmer" />
        <div className="h-8 w-2/3 rounded-xl shimmer" />
        <div className="h-4 w-1/2 rounded-xl shimmer" />
      </div>
    )
  }

  if (!product) {
    return (
      <div className="text-center py-20">
        <p className="text-4xl mb-3">😕</p>
        <p className="text-white font-bold mb-2">상품을 찾을 수 없어요</p>
        <Link href="/" className="text-primary text-sm">← 홈으로 돌아가기</Link>
      </div>
    )
  }

  const emoji    = productEmoji(product.productName)
  const gradeCfg = GRADE_CONFIG[product.grade]
  const isFull   = product.currentCount >= product.capacity
  const pct      = Math.min((product.currentCount / product.capacity) * 100, 100)

  return (
    <div className="space-y-4 pb-4">
      {/* Back */}
      <Link href="/" className="flex items-center gap-1 text-sm text-gray-400 hover:text-white transition-colors">
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        상품 목록
      </Link>

      {/* Hero Image */}
      <div
        className="h-52 rounded-2xl flex items-center justify-center text-7xl"
        style={{ background: `linear-gradient(135deg, ${gradeCfg.color}22, ${gradeCfg.color}11)`, border: `1px solid ${gradeCfg.color}33` }}
      >
        {product.imageUrl
          ? <img src={product.imageUrl} alt={product.productName} className="h-full w-full object-cover rounded-2xl" />
          : emoji
        }
      </div>

      {/* Title */}
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <GradeBadge grade={product.grade} />
            <span className="text-xs text-gray-500">Round {product.round}</span>
          </div>
          <h1 className="text-xl font-black text-white">{product.productName}</h1>
          <p className="text-sm text-gray-400 mt-1">{product.description}</p>
        </div>
      </div>

      {/* Participant bar */}
      <div className="card">
        <ParticipantBar current={product.currentCount} capacity={product.capacity} />
        <div className="mt-3 h-2 bg-card-lt rounded-full overflow-hidden">
          <div
            className="h-full rounded-full"
            style={{ width: `${pct}%`, backgroundColor: gradeCfg.color, transition: 'width 0.5s ease' }}
          />
        </div>
      </div>

      {/* Rules */}
      <div className="card space-y-3">
        <h2 className="font-bold text-white">도전 방법</h2>
        <ol className="space-y-2">
          {[
            '앱에서 광고를 시청하면 승부권 1개를 획득해요',
            `승부권을 사용해 가위바위보 배틀에 입장해요`,
            `${product.requiredStreak}연승을 달성하면 이 추첨방에 입장할 수 있어요`,
            `100명이 모이면 자동으로 1명을 추첨해요`,
            '당첨자에게는 앱 알림으로 안내드려요',
          ].map((step, i) => (
            <li key={i} className="flex gap-3 text-sm text-gray-300">
              <span
                className="shrink-0 w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold"
                style={{ backgroundColor: gradeCfg.color + '33', color: gradeCfg.color }}
              >
                {i + 1}
              </span>
              {step}
            </li>
          ))}
        </ol>
      </div>

      {/* Required streak */}
      <div className="card">
        <p className="text-sm text-gray-400 mb-2">필요 연승</p>
        <div className="flex items-center gap-3">
          <StreakStars required={product.requiredStreak} />
          <span className="text-white font-bold">{product.requiredStreak}연승</span>
        </div>
      </div>

      {/* CTA */}
      {isFull ? (
        <div className="btn-primary text-center opacity-50 cursor-not-allowed">
          추첨 마감 (100/100)
        </div>
      ) : !user ? (
        <div className="space-y-2">
          <p className="text-xs text-center text-gray-400">도전하려면 로그인이 필요해요</p>
          <div className="card text-center py-4">
            <p className="text-2xl mb-2">📱</p>
            <p className="text-sm text-white font-bold mb-1">앱에서 도전하기</p>
            <p className="text-xs text-gray-400">가위바위보 배틀은 앱에서만 가능해요</p>
          </div>
        </div>
      ) : (
        <div className="card text-center py-4">
          <p className="text-2xl mb-2">📱</p>
          <p className="text-sm text-white font-bold mb-1">앱에서 도전하기</p>
          <p className="text-xs text-gray-400">가위바위보 배틀은 앱에서만 가능해요</p>
          <button className="btn-primary mt-3 text-sm px-6 py-2">
            앱 다운로드 (준비 중)
          </button>
        </div>
      )}
    </div>
  )
}

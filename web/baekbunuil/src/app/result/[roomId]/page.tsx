'use client'

import { use, useEffect, useState } from 'react'
import Link from 'next/link'
import { ref, onValue, off } from 'firebase/database'
import { db } from '@/lib/firebase'
import { useAuth }  from '@/hooks/useAuth'
import { useProduct } from '@/hooks/useProducts'
import { productEmoji, type DrawResult } from '@/lib/types'

export default function ResultPage({ params }: { params: Promise<{ roomId: string }> }) {
  const { roomId }           = use(params)
  const { product }          = useProduct(roomId)
  const { user }             = useAuth()
  const [result, setResult]  = useState<DrawResult | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const drawRef = ref(db, `drawResults/${roomId}`)
    const handler = onValue(drawRef, (snap) => {
      if (snap.exists()) {
        setResult(snap.val() as DrawResult)
      }
      setLoading(false)
    })
    return () => off(drawRef, 'value', handler)
  }, [roomId])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center space-y-3">
          <div className="w-16 h-16 rounded-full bg-primary/20 mx-auto shimmer" />
          <p className="text-gray-400">추첨 결과 확인 중...</p>
        </div>
      </div>
    )
  }

  const emoji      = product ? productEmoji(product.productName) : '🎁'
  const isWinner   = result && user && result.winnerUserId === user.userId
  const isDrawn    = result !== null
  const isCurrent  = product?.drawStatus === 'open' || product?.drawStatus === 'drawing'

  return (
    <div className="space-y-5 pb-4">
      <Link href="/" className="flex items-center gap-1 text-sm text-gray-400 hover:text-white transition-colors">
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        홈으로
      </Link>

      <div className="card text-center py-8">
        <div className="text-6xl mb-4">{emoji}</div>
        <h1 className="text-xl font-black text-white mb-1">{product?.productName ?? '상품'}</h1>
        <p className="text-sm text-gray-400">{product?.description}</p>
      </div>

      {!isDrawn && (
        <div className="card text-center py-8 space-y-3">
          {isCurrent ? (
            <>
              <div className="relative mx-auto w-16 h-16">
                <div className="w-16 h-16 rounded-full bg-primary/20 flex items-center justify-center text-2xl">🎲</div>
              </div>
              <p className="font-bold text-white">추첨 대기 중</p>
              <p className="text-sm text-gray-400">
                {product?.currentCount ?? 0}/{product?.capacity ?? 100}명 모집 중<br />
                100명이 모이면 자동으로 추첨이 진행됩니다
              </p>
            </>
          ) : (
            <>
              <p className="text-3xl">📋</p>
              <p className="font-bold text-white">추첨 결과 없음</p>
              <p className="text-sm text-gray-400">아직 추첨이 진행되지 않았습니다</p>
            </>
          )}
        </div>
      )}

      {isDrawn && isWinner && (
        <div
          className="card text-center py-8 space-y-3"
          style={{ background: 'linear-gradient(135deg, #FFD70022, #FFD70011)', border: '1px solid #FFD70044' }}
        >
          <p className="text-5xl">🏆</p>
          <p className="text-2xl font-black text-gold">당첨!</p>
          <p className="text-white font-bold">{user?.nickname}님이 당첨되셨습니다!</p>
          <p className="text-xs text-gray-400">앱 알림 또는 이메일로 수령 방법을 안내드릴게요</p>
          <p className="text-xs text-gray-500 mt-2">
            추첨 시각: {new Date(result.drawnAt).toLocaleString('ko-KR')}
          </p>
        </div>
      )}

      {isDrawn && !isWinner && (
        <div className="card text-center py-8 space-y-3">
          <p className="text-5xl">😢</p>
          <p className="text-xl font-black text-white">미당첨</p>
          {result.winnerNickname && (
            <p className="text-sm text-gray-400">
              당첨자: <span className="text-white font-bold">{result.winnerNickname}</span>님
            </p>
          )}
          <p className="text-xs text-gray-500">
            추첨 시각: {new Date(result.drawnAt).toLocaleString('ko-KR')}
          </p>
          <Link href="/" className="block mt-4">
            <button className="btn-primary w-full">다른 상품 도전하기</button>
          </Link>
        </div>
      )}

      {/* Transparency info */}
      {isDrawn && result && (
        <div className="card space-y-2">
          <p className="text-xs font-bold text-gray-300">추첨 투명성</p>
          <p className="text-xs text-gray-500 break-all">
            Seed Hash: {result.seedHash}
          </p>
          <p className="text-xs text-gray-500">
            서버에서 암호학적 난수(crypto.randomBytes)로 공정하게 추첨됩니다
          </p>
        </div>
      )}
    </div>
  )
}

'use client'

import { useEffect, useState } from 'react'
import { ref, onValue, off }   from 'firebase/database'
import { getFunctions, httpsCallable } from 'firebase/functions'
import { db }         from '@/lib/firebase'
import { GradeBadge } from '@/components/GradeBadge'
import { ParticipantBar } from '@/components/ParticipantBar'
import type { ProductRoom } from '@/lib/types'

interface DrawResult {
  drawId:          string
  winnerUserId:    string
  winnerNickname:  string
  totalEntries:    number
}

export default function AdminDrawsPage() {
  const [products, setProducts] = useState<ProductRoom[]>([])
  const [loading,  setLoading]  = useState(true)
  const [drawing,  setDrawing]  = useState<string | null>(null)
  const [results,  setResults]  = useState<Record<string, DrawResult>>({})
  const [error,    setError]    = useState<string | null>(null)

  useEffect(() => {
    const r = ref(db, 'productRooms')
    const h = onValue(r, (snap) => {
      if (snap.exists()) {
        setProducts(Object.values(snap.val() as Record<string, ProductRoom>))
      }
      setLoading(false)
    })
    return () => off(r, 'value', h)
  }, [])

  async function triggerDraw(roomId: string, productName: string) {
    if (!confirm(`"${productName}" 추첨을 실행하시겠습니까?\n현재 참여자 중 공정한 난수로 1명을 선정합니다.`)) return
    setDrawing(roomId)
    setError(null)
    try {
      const fn = httpsCallable<{ roomId: string }, DrawResult>(getFunctions(), 'executeDraw')
      const res = await fn({ roomId })
      setResults((prev) => ({ ...prev, [roomId]: res.data }))
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message ?? '추첨 실패'
      setError(msg)
    } finally {
      setDrawing(null)
    }
  }

  const openRooms  = products.filter((p) => p.drawStatus === 'open')
  const drawingRooms = products.filter((p) => p.drawStatus === 'drawing')
  const drawnRooms = products.filter((p) => p.drawStatus === 'drawn')

  return (
    <div className="space-y-5">
      <h1 className="text-xl font-black text-white">추첨 관리</h1>

      {error && (
        <div className="card border border-red-500/30 bg-red-500/10">
          <p className="text-sm text-red-400">{error}</p>
        </div>
      )}

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => <div key={i} className="card h-28 shimmer" />)}
        </div>
      ) : (
        <>
          <div className="space-y-2">
            <p className="text-sm font-bold text-gray-300">진행중 상품 ({openRooms.length})</p>
            {openRooms.length === 0 ? (
              <div className="card text-center py-8 text-gray-500 text-sm">진행중인 추첨방이 없습니다</div>
            ) : (
              openRooms.map((p) => (
                <div key={p.roomId} className="card space-y-3">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <GradeBadge grade={p.grade} />
                        <span className="text-xs text-gray-500">Round {p.round}</span>
                        <span className="text-xs text-gray-500">{p.productType ?? 'coupon'}</span>
                      </div>
                      <p className="font-bold text-white text-sm">{p.productName}</p>
                      <p className="text-xs text-gray-400">{p.description}</p>
                    </div>
                    <button
                      onClick={() => triggerDraw(p.roomId, p.productName)}
                      disabled={drawing === p.roomId}
                      className="shrink-0 px-3 py-1.5 bg-primary/20 text-primary text-xs font-bold rounded-xl hover:bg-primary/30 transition-colors disabled:opacity-50"
                    >
                      {drawing === p.roomId ? '추첨중...' : '추첨 실행'}
                    </button>
                  </div>
                  <ParticipantBar current={p.currentCount} capacity={p.capacity} />
                  <p className="text-xs text-gray-500">
                    {p.currentCount >= p.capacity
                      ? '✓ 정원 달성 — 즉시 추첨 가능'
                      : `${p.capacity - p.currentCount}명 부족 (강제 추첨 가능)`}
                  </p>
                </div>
              ))
            )}
          </div>

          {drawingRooms.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-bold text-yellow-400">추첨 진행중 ({drawingRooms.length})</p>
              {drawingRooms.map((p) => (
                <div key={p.roomId} className="card border border-yellow-500/30 animate-pulse">
                  <div className="flex items-center gap-2">
                    <GradeBadge grade={p.grade} />
                    <p className="text-sm text-white">{p.productName}</p>
                    <span className="ml-auto text-xs text-yellow-400 font-semibold">추첨중...</span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {Object.entries(results).map(([roomId, result]) => {
            const product = products.find(p => p.roomId === roomId)
            return (
              <div key={roomId} className="card border border-green-500/30 bg-green-500/5 space-y-2">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-bold text-green-400">🎉 추첨 완료</p>
                  <button
                    onClick={() => setResults(prev => { const n = { ...prev }; delete n[roomId]; return n })}
                    className="text-xs text-gray-500 hover:text-white"
                  >
                    닫기
                  </button>
                </div>
                <p className="text-white font-bold">{product?.productName}</p>
                <div className="bg-card-lt rounded-xl p-3 space-y-1">
                  <p className="text-xs text-gray-400">당첨자</p>
                  <p className="text-white font-bold">{result.winnerNickname}</p>
                  <p className="text-xs text-gray-500">ID: {result.winnerUserId}</p>
                  <p className="text-xs text-gray-500">전체 참여자: {result.totalEntries}명 중 1명 선정</p>
                </div>
                <p className="text-xs text-gray-400">당첨자에게 FCM 알림이 발송됐습니다. 수령 관리 탭에서 처리하세요.</p>
              </div>
            )
          })}

          {drawnRooms.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-bold text-gray-300">추첨 완료 ({drawnRooms.length})</p>
              {drawnRooms.map((p) => (
                <div key={p.roomId} className="card opacity-60">
                  <div className="flex items-center gap-2">
                    <GradeBadge grade={p.grade} />
                    <p className="text-sm text-white">{p.productName}</p>
                    <span className="ml-auto text-xs text-green-400 font-semibold">완료</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}

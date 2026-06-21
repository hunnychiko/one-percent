'use client'

import { useEffect, useState } from 'react'
import { ref, onValue, off, update } from 'firebase/database'
import { db } from '@/lib/firebase'
import { GradeBadge }    from '@/components/GradeBadge'
import { ParticipantBar } from '@/components/ParticipantBar'
import type { ProductRoom } from '@/lib/types'

export default function AdminDrawsPage() {
  const [products, setProducts] = useState<ProductRoom[]>([])
  const [loading,  setLoading]  = useState(true)
  const [drawing,  setDrawing]  = useState<string | null>(null)

  useEffect(() => {
    const r = ref(db, 'productRooms')
    const h = onValue(r, (snap) => {
      if (snap.exists()) {
        const all = Object.values(snap.val() as Record<string, ProductRoom>)
        setProducts(all)
      }
      setLoading(false)
    })
    return () => off(r, 'value', h)
  }, [])

  async function triggerDraw(roomId: string) {
    if (!confirm(`추첨을 강제 실행하시겠습니까? (현재 참여자로 즉시 추첨)`)) return
    setDrawing(roomId)
    try {
      await update(ref(db, `productRooms/${roomId}`), { drawStatus: 'drawing' })
    } finally {
      setDrawing(null)
    }
  }

  const openRooms  = products.filter((p) => p.drawStatus === 'open')
  const drawnRooms = products.filter((p) => p.drawStatus === 'drawn')

  return (
    <div className="space-y-5">
      <h1 className="text-xl font-black text-white">추첨 관리</h1>

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => <div key={i} className="card h-28 shimmer" />)}
        </div>
      ) : (
        <>
          <div className="space-y-2">
            <p className="text-sm font-bold text-gray-300">진행중 상품 ({openRooms.length})</p>
            {openRooms.length === 0 ? (
              <div className="card text-center py-8 text-gray-500">진행중인 추첨방이 없습니다</div>
            ) : (
              openRooms.map((p) => (
                <div key={p.roomId} className="card space-y-3">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <GradeBadge grade={p.grade} />
                        <span className="text-xs text-gray-500">Round {p.round}</span>
                      </div>
                      <p className="font-bold text-white text-sm">{p.productName}</p>
                    </div>
                    <button
                      onClick={() => triggerDraw(p.roomId)}
                      disabled={drawing === p.roomId}
                      className="shrink-0 px-3 py-1.5 bg-primary/20 text-primary text-xs font-bold rounded-xl hover:bg-primary/30 transition-colors disabled:opacity-50"
                    >
                      {drawing === p.roomId ? '추첨중...' : '강제 추첨'}
                    </button>
                  </div>
                  <ParticipantBar current={p.currentCount} capacity={p.capacity} />
                  {p.currentCount < p.capacity && (
                    <p className="text-xs text-gray-500">
                      {p.capacity - p.currentCount}명 추가 필요 (강제 추첨 가능)
                    </p>
                  )}
                  {p.currentCount >= p.capacity && (
                    <p className="text-xs text-green-400 font-semibold">
                      ✓ 100명 달성 — 자동 추첨 대기 중
                    </p>
                  )}
                </div>
              ))
            )}
          </div>

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

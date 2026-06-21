'use client'

import { useEffect, useState } from 'react'
import { ref, onValue, off } from 'firebase/database'
import { db } from '@/lib/firebase'
import type { DrawResult, ProductRoom } from '@/lib/types'

interface WinnerRow extends DrawResult {
  productName?: string
}

export default function AdminWinnersPage() {
  const [winners,  setWinners]  = useState<WinnerRow[]>([])
  const [loading,  setLoading]  = useState(true)

  useEffect(() => {
    let products: Record<string, ProductRoom> = {}

    const roomsRef = ref(db, 'productRooms')
    const drawsRef = ref(db, 'drawResults')

    const roomsHandler = onValue(roomsRef, (snap) => {
      if (snap.exists()) products = snap.val()
    })

    const drawsHandler = onValue(drawsRef, (snap) => {
      if (snap.exists()) {
        const rows: WinnerRow[] = Object.values(snap.val() as Record<string, DrawResult>)
          .map((r) => ({ ...r, productName: products[r.roomId]?.productName }))
          .sort((a, b) => b.drawnAt - a.drawnAt)
        setWinners(rows)
      }
      setLoading(false)
    })

    return () => {
      off(roomsRef, 'value', roomsHandler)
      off(drawsRef, 'value', drawsHandler)
    }
  }, [])

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-black text-white">당첨자 관리</h1>
        <span className="text-xs text-gray-400">{winners.length}건</span>
      </div>

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => <div key={i} className="card h-20 shimmer" />)}
        </div>
      ) : winners.length === 0 ? (
        <div className="card text-center py-12 text-gray-500">
          <p className="text-3xl mb-2">🏆</p>
          <p>당첨 내역이 없습니다</p>
        </div>
      ) : (
        <div className="space-y-2">
          {winners.map((w) => (
            <div key={w.drawId} className="card space-y-2">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="font-bold text-white text-sm">{w.winnerNickname ?? w.winnerUserId}</p>
                  <p className="text-xs text-gray-400">{w.productName ?? w.roomId}</p>
                </div>
                <p className="text-xs text-gray-500 shrink-0">
                  {new Date(w.drawnAt).toLocaleDateString('ko-KR')}
                </p>
              </div>
              <div className="text-xs text-gray-600 break-all">
                UID: {w.winnerUserId}
              </div>
              <div className="text-xs text-gray-600 break-all">
                Hash: {w.seedHash.slice(0, 20)}…
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

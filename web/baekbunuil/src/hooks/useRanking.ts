'use client'

import { useState, useEffect } from 'react'
import { ref, query, orderByChild, limitToLast, onValue, off } from 'firebase/database'
import { db } from '@/lib/firebase'
import type { RankingEntry } from '@/lib/types'

const SAMPLE_RANKING: RankingEntry[] = [
  { userId: 'u1', nickname: '연승왕', bestStreak: 23, totalWins: 5 },
  { userId: 'u2', nickname: '행운아',  bestStreak: 18, totalWins: 3 },
  { userId: 'u3', nickname: '도전자',  bestStreak: 15, totalWins: 2 },
  { userId: 'u4', nickname: '승부사',  bestStreak: 12, totalWins: 1 },
  { userId: 'u5', nickname: '럭키가이', bestStreak: 10, totalWins: 1 },
]

export function useRanking(mode: 'streak' | 'wins' = 'streak') {
  const [rankings, setRankings] = useState<RankingEntry[]>(SAMPLE_RANKING)
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    const rankRef = query(
      ref(db, 'users'),
      orderByChild(mode === 'streak' ? 'bestStreak' : 'totalWins'),
      limitToLast(50)
    )

    const handler = onValue(
      rankRef,
      (snap) => {
        if (snap.exists()) {
          const entries: RankingEntry[] = []
          snap.forEach((child) => {
            const u = child.val()
            entries.push({
              userId:     child.key!,
              nickname:   u.nickname ?? '알수없음',
              bestStreak: u.bestStreak ?? 0,
              totalWins:  u.totalWins  ?? 0,
            })
          })
          setRankings(
            entries
              .sort((a, b) =>
                mode === 'streak'
                  ? b.bestStreak - a.bestStreak
                  : b.totalWins  - a.totalWins
              )
              .slice(0, 30)
          )
        } else {
          setRankings(SAMPLE_RANKING)
        }
        setLoading(false)
      },
      () => {
        setRankings(SAMPLE_RANKING)
        setLoading(false)
      }
    )

    return () => off(rankRef, 'value', handler)
  }, [mode])

  return { rankings, loading }
}

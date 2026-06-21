'use client'

import { useState } from 'react'
import { useRanking } from '@/hooks/useRanking'

export default function RankingPage() {
  const [mode, setMode]     = useState<'streak' | 'wins'>('streak')
  const { rankings, loading } = useRanking(mode)

  const top3  = rankings.slice(0, 3)
  const rest  = rankings.slice(3)

  const podiumOrder = top3.length >= 3
    ? [top3[1], top3[0], top3[2]]
    : top3

  const PODIUM_HEIGHTS = ['h-20', 'h-28', 'h-16']
  const MEDALS         = ['🥈', '🥇', '🥉']

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-black text-white">랭킹</h1>
        <div className="flex gap-1 bg-card-lt rounded-xl p-1">
          {(['streak', 'wins'] as const).map((m) => (
            <button
              key={m}
              onClick={() => setMode(m)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-colors ${
                mode === m ? 'bg-primary text-white' : 'text-gray-400'
              }`}
            >
              {m === 'streak' ? '연승' : '당첨'}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="space-y-2">
          {Array.from({ length: 10 }).map((_, i) => (
            <div key={i} className="card h-14 shimmer" />
          ))}
        </div>
      ) : (
        <>
          {/* Podium */}
          {top3.length === 3 && (
            <div className="card pb-0 overflow-hidden">
              <div className="flex items-end justify-center gap-3 px-4 pt-6">
                {podiumOrder.map((entry, i) => (
                  <div key={entry.userId} className="flex flex-col items-center gap-1 flex-1">
                    <span className="text-xl">{MEDALS[i]}</span>
                    <p className="text-xs font-bold text-white truncate max-w-full px-1">{entry.nickname}</p>
                    <p className="text-xs text-gray-400">
                      {mode === 'streak' ? `${entry.bestStreak}연승` : `${entry.totalWins}회`}
                    </p>
                    <div
                      className={`w-full ${PODIUM_HEIGHTS[i]} rounded-t-xl flex items-center justify-center`}
                      style={{
                        background: i === 1
                          ? 'linear-gradient(180deg, #FFD700, #FFA000)'
                          : i === 0
                          ? 'linear-gradient(180deg, #B0BEC5, #78909C)'
                          : 'linear-gradient(180deg, #CD7F32, #A05C1A)',
                      }}
                    >
                      <span className="text-2xl font-black text-white">{[2,1,3][i]}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* List */}
          <div className="space-y-2">
            {rest.map((entry, i) => (
              <div key={entry.userId} className="card flex items-center gap-3">
                <span className="w-6 text-center text-sm font-bold text-gray-400">{i + 4}</span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-bold text-white truncate">{entry.nickname}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-bold text-primary">
                    {mode === 'streak' ? `${entry.bestStreak}연승` : `${entry.totalWins}회 당첨`}
                  </p>
                  {mode === 'streak'
                    ? <p className="text-xs text-gray-500">{entry.totalWins}회 당첨</p>
                    : <p className="text-xs text-gray-500">최고 {entry.bestStreak}연승</p>
                  }
                </div>
              </div>
            ))}

            {rankings.length === 0 && (
              <div className="text-center py-12 text-gray-500">
                <p className="text-3xl mb-2">🏆</p>
                <p>랭킹 데이터가 없어요</p>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  )
}

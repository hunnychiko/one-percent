'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { ref, onValue, off } from 'firebase/database'
import { db } from '@/lib/firebase'
import { useAuth } from '@/hooks/useAuth'
import type { Challenge, DrawEntry } from '@/lib/types'

export default function MyPage() {
  const { user, firebaseUser, signIn, signOut, loading } = useAuth()
  const [challenges, setChallenges] = useState<Challenge[]>([])
  const [entries,    setEntries]    = useState<DrawEntry[]>([])

  useEffect(() => {
    if (!firebaseUser) return

    const chalRef  = ref(db, 'challenges')
    const entryRef = ref(db, 'drawEntries')

    const chalHandler = onValue(chalRef, (snap) => {
      if (!snap.exists()) return
      const all = Object.values(snap.val() as Record<string, Challenge>)
      setChallenges(all.filter((c) => c.userId === firebaseUser.uid))
    })

    const entryHandler = onValue(entryRef, (snap) => {
      if (!snap.exists()) return
      const all = Object.values(snap.val() as Record<string, DrawEntry>)
      setEntries(all.filter((e) => e.userId === firebaseUser.uid))
    })

    return () => {
      off(chalRef,  'value', chalHandler)
      off(entryRef, 'value', entryHandler)
    }
  }, [firebaseUser])

  if (loading) {
    return <div className="h-40 card shimmer" />
  }

  if (!user) {
    return (
      <div className="text-center py-20 space-y-4">
        <p className="text-5xl">👤</p>
        <p className="text-white font-bold">로그인이 필요해요</p>
        <p className="text-sm text-gray-400">내 기록을 확인하려면 로그인해 주세요</p>
        <button onClick={signIn} className="btn-primary px-8">
          Google로 로그인
        </button>
      </div>
    )
  }

  const activeChals    = challenges.filter((c) => c.state === 'active')
  const completedChals = challenges.filter((c) => c.state === 'completed')

  return (
    <div className="space-y-5 pb-4">
      <h1 className="text-xl font-black text-white">마이페이지</h1>

      {/* Profile Card */}
      <div
        className="card flex items-center gap-4"
        style={{ border: '1px solid rgba(91,107,248,0.3)' }}
      >
        <div className="w-14 h-14 rounded-full bg-primary/20 flex items-center justify-center text-2xl font-black text-primary">
          {user.nickname.charAt(0)}
        </div>
        <div className="flex-1">
          <p className="font-bold text-white text-lg">{user.nickname}</p>
          <p className="text-xs text-gray-400">{firebaseUser?.email}</p>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-3">
        {[
          { label: '최고 연승', value: user.bestStreak, unit: '연승', color: '#FFD700' },
          { label: '총 당첨',   value: user.totalWins,  unit: '회',   color: '#5B6BF8' },
          { label: '승부권',    value: user.ticketCount, unit: '개',  color: '#7C5CBF' },
        ].map(({ label, value, unit, color }) => (
          <div key={label} className="card text-center">
            <p className="text-2xl font-black" style={{ color }}>{value}</p>
            <p className="text-xs text-gray-500 mt-0.5">{unit}</p>
            <p className="text-xs text-gray-400 mt-1">{label}</p>
          </div>
        ))}
      </div>

      {/* Active Challenges */}
      {activeChals.length > 0 && (
        <div className="space-y-2">
          <p className="text-sm font-bold text-gray-300">진행 중인 도전</p>
          {activeChals.map((c) => (
            <Link key={c.challengeId} href={`/product/${c.roomId}`}>
              <div className="card flex items-center justify-between">
                <div>
                  <p className="text-xs text-gray-400">{c.roomId}</p>
                  <p className="text-sm font-bold text-white">
                    {c.currentStreak}/{c.targetStreak} 연승
                  </p>
                </div>
                <div
                  className="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold"
                  style={{ background: 'rgba(91,107,248,0.2)', color: '#5B6BF8' }}
                >
                  {Math.round((c.currentStreak / c.targetStreak) * 100)}%
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Draw Entries */}
      {entries.length > 0 && (
        <div className="space-y-2">
          <p className="text-sm font-bold text-gray-300">추첨 참여 내역</p>
          {entries.map((e) => (
            <Link key={e.entryId} href={`/result/${e.roomId}`}>
              <div className="card flex items-center justify-between">
                <div>
                  <p className="text-xs text-gray-400">{e.roomId}</p>
                  <p className="text-xs text-gray-500">
                    {new Date(e.enteredAt).toLocaleDateString('ko-KR')}
                  </p>
                </div>
                <span className="text-xs text-primary font-semibold">결과 보기 →</span>
              </div>
            </Link>
          ))}
        </div>
      )}

      {activeChals.length === 0 && entries.length === 0 && completedChals.length === 0 && (
        <div className="card text-center py-10">
          <p className="text-3xl mb-2">🎯</p>
          <p className="text-gray-400 text-sm">아직 도전 내역이 없어요</p>
          <Link href="/" className="block mt-3">
            <button className="btn-primary text-sm px-6 py-2">상품 둘러보기</button>
          </Link>
        </div>
      )}

      {/* Links */}
      <div className="card space-y-0 divide-y divide-card-lt">
        {[
          { href: '/privacy', label: '개인정보처리방침' },
          { href: '/terms',   label: '이용약관' },
        ].map(({ href, label }) => (
          <Link key={href} href={href} className="flex items-center justify-between py-3 text-sm text-gray-400 hover:text-white transition-colors">
            {label}
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        ))}
      </div>

      {/* Sign Out */}
      <button
        onClick={signOut}
        className="w-full py-3 rounded-xl text-sm text-gray-400 border border-card-lt hover:text-red-400 hover:border-red-400/40 transition-colors"
      >
        로그아웃
      </button>
    </div>
  )
}

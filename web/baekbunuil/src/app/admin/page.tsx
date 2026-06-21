'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { ref, get } from 'firebase/database'
import { db } from '@/lib/firebase'

interface Stats {
  totalUsers:    number
  totalProducts: number
  openRooms:     number
  totalDrawn:    number
}

export default function AdminDashboard() {
  const [stats, setStats]     = useState<Stats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      const [usersSnap, roomsSnap, drawsSnap] = await Promise.all([
        get(ref(db, 'users')),
        get(ref(db, 'productRooms')),
        get(ref(db, 'drawResults')),
      ])

      const rooms     = roomsSnap.exists() ? Object.values(roomsSnap.val()) as { drawStatus: string }[] : []
      const openRooms = rooms.filter((r) => r.drawStatus === 'open').length

      setStats({
        totalUsers:    usersSnap.exists() ? Object.keys(usersSnap.val()).length : 0,
        totalProducts: rooms.length,
        openRooms,
        totalDrawn:    drawsSnap.exists() ? Object.keys(drawsSnap.val()).length : 0,
      })
      setLoading(false)
    }
    load()
  }, [])

  const QUICK_LINKS = [
    { href: '/admin/products', label: '상품 관리', desc: '상품 등록 및 수정',     icon: '📦' },
    { href: '/admin/draws',    label: '추첨 관리', desc: '추첨 진행 및 강제 실행', icon: '🎲' },
    { href: '/admin/winners',  label: '당첨자 관리', desc: '당첨자 조회 및 처리',  icon: '🏆' },
  ]

  return (
    <div className="space-y-5">
      <h1 className="text-xl font-black text-white">관리자 대시보드</h1>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-3">
        {loading ? (
          Array.from({ length: 4 }).map((_, i) => <div key={i} className="card h-20 shimmer" />)
        ) : stats ? (
          [
            { label: '총 사용자',  value: stats.totalUsers,    color: '#5B6BF8' },
            { label: '총 상품',    value: stats.totalProducts, color: '#7C5CBF' },
            { label: '진행중 방',  value: stats.openRooms,     color: '#4CAF50' },
            { label: '완료 추첨',  value: stats.totalDrawn,    color: '#FFD700' },
          ].map(({ label, value, color }) => (
            <div key={label} className="card text-center">
              <p className="text-3xl font-black" style={{ color }}>{value}</p>
              <p className="text-xs text-gray-400 mt-1">{label}</p>
            </div>
          ))
        ) : null}
      </div>

      {/* Quick Links */}
      <div className="space-y-2">
        <p className="text-sm font-bold text-gray-300">빠른 이동</p>
        {QUICK_LINKS.map(({ href, label, desc, icon }) => (
          <Link key={href} href={href}>
            <div className="card flex items-center gap-4 hover:bg-card-lt transition-colors cursor-pointer">
              <span className="text-2xl">{icon}</span>
              <div>
                <p className="font-bold text-white text-sm">{label}</p>
                <p className="text-xs text-gray-400">{desc}</p>
              </div>
              <svg className="w-4 h-4 text-gray-500 ml-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}

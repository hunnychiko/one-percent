'use client'

import { useAuth }       from '@/hooks/useAuth'
import { AdminSidebar }  from '@/components/AdminSidebar'

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { user, admin, loading, signIn } = useAuth()

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 rounded-full border-2 border-primary border-t-transparent animate-spin" />
      </div>
    )
  }

  if (!user || !admin) {
    return (
      <div className="text-center py-20 space-y-4">
        <p className="text-5xl">🔒</p>
        <p className="text-white font-bold text-lg">접근 권한이 없습니다</p>
        <p className="text-sm text-gray-400">관리자 계정으로 로그인해 주세요</p>
        {!user && (
          <button onClick={signIn} className="btn-primary px-8">
            Google로 로그인
          </button>
        )}
      </div>
    )
  }

  return (
    <div className="flex gap-5 items-start">
      <AdminSidebar />
      <div className="flex-1 min-w-0">{children}</div>
    </div>
  )
}

'use client'

import { useEffect, useState } from 'react'
import { ref, onValue, off, push, set, remove } from 'firebase/database'
import { db } from '@/lib/firebase'

interface AffiliateBanner {
  bannerId:    string
  companyName: string
  imageUrl:    string
  ticketReward: number
  isActive:    boolean
  order:       number
  createdAt:   number
}

const EMPTY_FORM = {
  companyName:  '',
  imageUrl:     '',
  ticketReward: 10,
  isActive:     true,
  order:        0,
}

export default function AdminAffiliatePage() {
  const [banners,  setBanners]  = useState<AffiliateBanner[]>([])
  const [form,     setForm]     = useState({ ...EMPTY_FORM })
  const [editId,   setEditId]   = useState<string | null>(null)
  const [loading,  setLoading]  = useState(true)
  const [saving,   setSaving]   = useState(false)

  useEffect(() => {
    const r = ref(db, 'affiliateBanners')
    const h = onValue(r, (snap) => {
      if (snap.exists()) {
        const list = Object.values(snap.val() as Record<string, AffiliateBanner>)
        setBanners(list.sort((a, b) => a.order - b.order))
      } else {
        setBanners([])
      }
      setLoading(false)
    })
    return () => off(r, 'value', h)
  }, [])

  async function handleSave() {
    if (!form.companyName.trim()) return
    setSaving(true)
    try {
      if (editId) {
        await set(ref(db, `affiliateBanners/${editId}`), {
          ...form,
          bannerId:  editId,
          createdAt: banners.find((b) => b.bannerId === editId)?.createdAt ?? Date.now(),
        })
      } else {
        const newRef = push(ref(db, 'affiliateBanners'))
        await set(newRef, {
          ...form,
          bannerId:  newRef.key!,
          createdAt: Date.now(),
        })
      }
      setForm({ ...EMPTY_FORM })
      setEditId(null)
    } finally {
      setSaving(false)
    }
  }

  function handleEdit(b: AffiliateBanner) {
    setEditId(b.bannerId)
    setForm({
      companyName:  b.companyName,
      imageUrl:     b.imageUrl,
      ticketReward: b.ticketReward,
      isActive:     b.isActive,
      order:        b.order,
    })
  }

  async function handleDelete(bannerId: string) {
    if (!confirm('정말 삭제하시겠습니까?')) return
    await remove(ref(db, `affiliateBanners/${bannerId}`))
  }

  async function toggleActive(b: AffiliateBanner) {
    await set(ref(db, `affiliateBanners/${b.bannerId}/isActive`), !b.isActive)
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-black text-white">제휴 배너 관리</h1>
        {editId && (
          <button
            onClick={() => { setEditId(null); setForm({ ...EMPTY_FORM }) }}
            className="text-xs text-gray-400 hover:text-white transition-colors"
          >
            취소
          </button>
        )}
      </div>

      {/* Form */}
      <div className="card space-y-4">
        <h2 className="font-bold text-white text-sm">{editId ? '배너 수정' : '새 배너 등록'}</h2>

        <div className="space-y-3">
          <div>
            <label className="text-xs text-gray-400 mb-1 block">제휴사명 *</label>
            <input
              className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
              value={form.companyName}
              onChange={(e) => setForm((f) => ({ ...f, companyName: e.target.value }))}
              placeholder="예: 스타벅스"
            />
          </div>

          <div>
            <label className="text-xs text-gray-400 mb-1 block">배너 이미지 URL</label>
            <input
              className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
              value={form.imageUrl}
              onChange={(e) => setForm((f) => ({ ...f, imageUrl: e.target.value }))}
              placeholder="https://..."
            />
            {form.imageUrl && (
              <img
                src={form.imageUrl}
                alt="미리보기"
                className="mt-2 rounded-xl h-24 object-cover"
                onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
              />
            )}
          </div>

          <div>
            <label className="text-xs text-gray-400 mb-1 block">
              도전권 지급 개수: <span className="text-primary font-bold">{form.ticketReward}개</span>
            </label>
            <input
              type="range" min={5} max={50} step={5}
              className="w-full accent-primary"
              value={form.ticketReward}
              onChange={(e) => setForm((f) => ({ ...f, ticketReward: Number(e.target.value) }))}
            />
            <div className="flex justify-between text-xs text-gray-500 mt-1">
              <span>5개</span>
              <span>50개</span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-gray-400 mb-1 block">표시 순서</label>
              <input
                type="number" min={0}
                className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                value={form.order}
                onChange={(e) => setForm((f) => ({ ...f, order: Number(e.target.value) }))}
              />
            </div>
            <div>
              <label className="text-xs text-gray-400 mb-1 block">활성화</label>
              <div className="flex items-center h-[38px]">
                <button
                  onClick={() => setForm((f) => ({ ...f, isActive: !f.isActive }))}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    form.isActive ? 'bg-primary' : 'bg-gray-600'
                  }`}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                      form.isActive ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
                <span className="ml-2 text-sm text-gray-300">{form.isActive ? '활성' : '비활성'}</span>
              </div>
            </div>
          </div>
        </div>

        <button
          onClick={handleSave}
          disabled={saving || !form.companyName.trim()}
          className="btn-primary w-full"
        >
          {saving ? '저장 중...' : editId ? '수정하기' : '등록하기'}
        </button>
      </div>

      {/* Banner list */}
      <div className="space-y-2">
        <p className="text-sm font-bold text-gray-300">등록된 배너 ({banners.length})</p>
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => <div key={i} className="card h-20 shimmer" />)
        ) : banners.length === 0 ? (
          <div className="card text-center py-8 text-gray-500">
            <p>등록된 배너가 없습니다</p>
          </div>
        ) : (
          banners.map((b) => (
            <div key={b.bannerId} className="card flex items-center gap-3">
              {/* Thumbnail */}
              <div className="w-14 h-14 rounded-xl bg-card-lt flex items-center justify-center shrink-0 overflow-hidden">
                {b.imageUrl ? (
                  <img src={b.imageUrl} alt={b.companyName} className="w-full h-full object-cover" />
                ) : (
                  <span className="text-2xl">🏢</span>
                )}
              </div>

              <div className="flex-1 min-w-0">
                <p className="font-bold text-white text-sm">{b.companyName}</p>
                <p className="text-xs text-primary">도전권 +{b.ticketReward}개 지급</p>
                <p className="text-xs text-gray-500">순서: {b.order}</p>
              </div>

              <div className="flex flex-col items-end gap-2 shrink-0">
                <button
                  onClick={() => toggleActive(b)}
                  className={`text-xs px-2 py-1 rounded-full font-bold ${
                    b.isActive
                      ? 'bg-green-900/40 text-green-400'
                      : 'bg-gray-700 text-gray-400'
                  }`}
                >
                  {b.isActive ? '활성' : '비활성'}
                </button>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleEdit(b)}
                    className="text-xs text-primary hover:underline"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDelete(b.bannerId)}
                    className="text-xs text-red-400 hover:underline"
                  >
                    삭제
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

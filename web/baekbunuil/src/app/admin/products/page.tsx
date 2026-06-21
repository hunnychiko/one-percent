'use client'

import { useEffect, useState } from 'react'
import { ref, onValue, off, push, set, remove, serverTimestamp } from 'firebase/database'
import { db } from '@/lib/firebase'
import { GRADE_CONFIG, gradeFromStreak, type ProductRoom } from '@/lib/types'
import { GradeBadge }    from '@/components/GradeBadge'
import { ParticipantBar } from '@/components/ParticipantBar'

const EMPTY_FORM: Omit<ProductRoom, 'roomId' | 'currentCount' | 'drawStatus'> = {
  productName:    '',
  imageUrl:       '',
  requiredStreak: 3,
  capacity:       100,
  grade:          'C',
  description:    '',
  round:          1,
  productType:    'coupon',
  directBuyLabel: '',
}

export default function AdminProductsPage() {
  const [products, setProducts] = useState<ProductRoom[]>([])
  const [form,     setForm]     = useState({ ...EMPTY_FORM })
  const [editId,   setEditId]   = useState<string | null>(null)
  const [loading,  setLoading]  = useState(true)
  const [saving,   setSaving]   = useState(false)

  useEffect(() => {
    const r = ref(db, 'productRooms')
    const h = onValue(r, (snap) => {
      if (snap.exists()) {
        setProducts(Object.values(snap.val() as Record<string, ProductRoom>))
      } else {
        setProducts([])
      }
      setLoading(false)
    })
    return () => off(r, 'value', h)
  }, [])

  function handleStreakChange(v: number) {
    setForm((f) => ({ ...f, requiredStreak: v, grade: gradeFromStreak(v) }))
  }

  async function handleSave() {
    if (!form.productName.trim()) return
    setSaving(true)
    try {
      if (editId) {
        await set(ref(db, `productRooms/${editId}`), {
          ...form,
          roomId: editId,
          currentCount: products.find((p) => p.roomId === editId)?.currentCount ?? 0,
          drawStatus:   products.find((p) => p.roomId === editId)?.drawStatus ?? 'open',
        })
      } else {
        const newRef = push(ref(db, 'productRooms'))
        await set(newRef, {
          ...form,
          roomId:       newRef.key!,
          currentCount: 0,
          drawStatus:   'open',
          createdAt:    serverTimestamp(),
        })
      }
      setForm({ ...EMPTY_FORM })
      setEditId(null)
    } finally {
      setSaving(false)
    }
  }

  function handleEdit(p: ProductRoom) {
    setEditId(p.roomId)
    setForm({
      productName:    p.productName,
      imageUrl:       p.imageUrl,
      requiredStreak: p.requiredStreak,
      capacity:       p.capacity,
      grade:          p.grade,
      description:    p.description,
      round:          p.round,
      productType:    p.productType ?? 'coupon',
      directBuyLabel: p.directBuyLabel ?? '',
    })
  }

  async function handleDelete(roomId: string) {
    if (!confirm('정말 삭제하시겠습니까?')) return
    await remove(ref(db, `productRooms/${roomId}`))
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-black text-white">상품 관리</h1>
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
        <h2 className="font-bold text-white text-sm">{editId ? '상품 수정' : '새 상품 등록'}</h2>

        <div className="space-y-3">
          <div>
            <label className="text-xs text-gray-400 mb-1 block">상품명 *</label>
            <input
              className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
              value={form.productName}
              onChange={(e) => setForm((f) => ({ ...f, productName: e.target.value }))}
              placeholder="예: 치킨 1마리"
            />
          </div>

          <div>
            <label className="text-xs text-gray-400 mb-1 block">설명</label>
            <input
              className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
              value={form.description}
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              placeholder="예: BBQ 황금올리브 치킨"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-gray-400 mb-1 block">필요 연승: {form.requiredStreak}</label>
              <input
                type="range" min={1} max={20}
                className="w-full accent-primary"
                value={form.requiredStreak}
                onChange={(e) => handleStreakChange(Number(e.target.value))}
              />
            </div>
            <div>
              <label className="text-xs text-gray-400 mb-1 block">정원</label>
              <input
                type="number" min={10} max={1000}
                className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                value={form.capacity}
                onChange={(e) => setForm((f) => ({ ...f, capacity: Number(e.target.value) }))}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-gray-400 mb-1 block">등급 (자동)</label>
              <div className="py-2 px-3 bg-card-lt rounded-xl">
                <GradeBadge grade={form.grade} />
              </div>
            </div>
            <div>
              <label className="text-xs text-gray-400 mb-1 block">라운드</label>
              <input
                type="number" min={1}
                className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                value={form.round}
                onChange={(e) => setForm((f) => ({ ...f, round: Number(e.target.value) }))}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-gray-400 mb-1 block">상품 유형</label>
              <select
                className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                value={form.productType}
                onChange={(e) => setForm((f) => ({ ...f, productType: e.target.value as 'coupon' | 'physical' | 'premium' }))}
              >
                <option value="coupon">쿠폰/링크</option>
                <option value="physical">실물 배송</option>
                <option value="premium">고가 실물</option>
              </select>
            </div>
            <div>
              <label className="text-xs text-gray-400 mb-1 block">도전 포기 직구 가격</label>
              <input
                className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                value={form.directBuyLabel}
                onChange={(e) => setForm((f) => ({ ...f, directBuyLabel: e.target.value }))}
                placeholder="예: ₩9,900 (빈칸=비활성)"
              />
            </div>
          </div>

          <div>
            <label className="text-xs text-gray-400 mb-1 block">이미지 URL</label>
            <input
              className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
              value={form.imageUrl}
              onChange={(e) => setForm((f) => ({ ...f, imageUrl: e.target.value }))}
              placeholder="https://..."
            />
          </div>
        </div>

        <button
          onClick={handleSave}
          disabled={saving || !form.productName.trim()}
          className="btn-primary w-full"
        >
          {saving ? '저장 중...' : editId ? '수정하기' : '등록하기'}
        </button>
      </div>

      {/* Product List */}
      <div className="space-y-2">
        <p className="text-sm font-bold text-gray-300">등록된 상품 ({products.length})</p>
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => <div key={i} className="card h-24 shimmer" />)
        ) : products.length === 0 ? (
          <div className="card text-center py-8 text-gray-500">
            <p>등록된 상품이 없습니다</p>
          </div>
        ) : (
          products.map((p) => (
            <div key={p.roomId} className="card space-y-2">
              <div className="flex items-start justify-between gap-2">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <GradeBadge grade={p.grade} />
                    <span className="text-xs text-gray-500">{p.drawStatus}</span>
                  </div>
                  <p className="font-bold text-white text-sm">{p.productName}</p>
                  <p className="text-xs text-gray-400">{p.description}</p>
                </div>
                <div className="flex gap-2 shrink-0">
                  <button
                    onClick={() => handleEdit(p)}
                    className="text-xs text-primary hover:underline"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDelete(p.roomId)}
                    className="text-xs text-red-400 hover:underline"
                  >
                    삭제
                  </button>
                </div>
              </div>
              <ParticipantBar current={p.currentCount} capacity={p.capacity} />
            </div>
          ))
        )}
      </div>
    </div>
  )
}

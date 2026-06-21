'use client'

import { useEffect, useState } from 'react'
import { ref, onValue, off, update } from 'firebase/database'
import { db } from '@/lib/firebase'

interface WinnerClaim {
  claimId:            string
  drawId:             string
  roomId:             string
  userId:             string
  productName:        string
  productType:        string   // coupon | physical | premium
  status:             string
  couponCode:         string
  affiliateUrl:       string
  shippingName:       string
  shippingPhone:      string
  shippingPostcode:   string
  shippingAddress:    string
  shippingDetail:     string
  trackingNumber:     string
  trackingCarrier:    string
  verificationStatus: string
  verificationNote:   string
  createdAt:          number
  updatedAt:          number
}

const STATUS_LABEL: Record<string, string> = {
  unclaimed:          '미수령',
  coupon_issued:      '쿠폰 발급 완료',
  address_pending:    '배송지 대기',
  address_submitted:  '배송지 접수',
  verifying:          '검증 중',
  verified:           '검증 완료',
  shipped:            '배송 중',
  delivered:          '배송 완료',
  rejected:           '지급 거절',
}

const STATUS_COLOR: Record<string, string> = {
  unclaimed:          'bg-gray-700 text-gray-300',
  coupon_issued:      'bg-green-900/40 text-green-400',
  address_pending:    'bg-yellow-900/40 text-yellow-400',
  address_submitted:  'bg-blue-900/40 text-blue-400',
  verifying:          'bg-orange-900/40 text-orange-400',
  verified:           'bg-purple-900/40 text-purple-400',
  shipped:            'bg-cyan-900/40 text-cyan-400',
  delivered:          'bg-green-900/40 text-green-400',
  rejected:           'bg-red-900/40 text-red-400',
}

const TYPE_LABEL: Record<string, string> = {
  coupon:   '쿠폰/링크',
  physical: '실물 배송',
  premium:  '고가 실물',
}

export default function AdminWinnersPage() {
  const [claims,   setClaims]   = useState<WinnerClaim[]>([])
  const [loading,  setLoading]  = useState(true)
  const [expanded, setExpanded] = useState<string | null>(null)
  const [saving,   setSaving]   = useState(false)

  // Per-claim edit state
  const [couponInput,   setCouponInput]   = useState('')
  const [affiliateInput, setAffiliateInput] = useState('')
  const [trackingInput,  setTrackingInput]  = useState('')
  const [carrierInput,   setCarrierInput]   = useState('')
  const [verifyNote,     setVerifyNote]     = useState('')

  useEffect(() => {
    const r = ref(db, 'winnerClaims')
    const h = onValue(r, (snap) => {
      if (snap.exists()) {
        const list = Object.values(snap.val() as Record<string, WinnerClaim>)
          .sort((a, b) => b.createdAt - a.createdAt)
        setClaims(list)
      } else {
        setClaims([])
      }
      setLoading(false)
    })
    return () => off(r, 'value', h)
  }, [])

  function onExpand(claim: WinnerClaim) {
    if (expanded === claim.claimId) { setExpanded(null); return }
    setExpanded(claim.claimId)
    setCouponInput(claim.couponCode ?? '')
    setAffiliateInput(claim.affiliateUrl ?? '')
    setTrackingInput(claim.trackingNumber ?? '')
    setCarrierInput(claim.trackingCarrier ?? '')
    setVerifyNote(claim.verificationNote ?? '')
  }

  async function updateClaim(claimId: string, updates: Record<string, unknown>) {
    setSaving(true)
    try {
      await update(ref(db, `winnerClaims/${claimId}`), { ...updates, updatedAt: Date.now() })
    } finally {
      setSaving(false)
    }
  }

  async function issueCoupon(claim: WinnerClaim) {
    if (!couponInput.trim() && !affiliateInput.trim()) return
    await updateClaim(claim.claimId, {
      couponCode:   couponInput.trim(),
      affiliateUrl: affiliateInput.trim(),
      status:       'coupon_issued',
    })
  }

  async function markShipped(claim: WinnerClaim) {
    if (!trackingInput.trim()) return
    await updateClaim(claim.claimId, {
      trackingNumber:  trackingInput.trim(),
      trackingCarrier: carrierInput.trim(),
      status:          'shipped',
    })
  }

  async function markDelivered(claimId: string) {
    await updateClaim(claimId, { status: 'delivered' })
  }

  async function verifyPass(claim: WinnerClaim) {
    await updateClaim(claim.claimId, {
      verificationStatus: 'passed',
      verificationNote:   verifyNote,
      status:             'address_pending',
    })
  }

  async function verifyFail(claim: WinnerClaim) {
    await updateClaim(claim.claimId, {
      verificationStatus: 'failed',
      verificationNote:   verifyNote,
      status:             'rejected',
    })
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-black text-white">당첨자 수령 관리</h1>
        <span className="text-xs text-gray-400">{claims.length}건</span>
      </div>

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => <div key={i} className="card h-20 shimmer" />)}
        </div>
      ) : claims.length === 0 ? (
        <div className="card text-center py-12 text-gray-500">
          <p className="text-3xl mb-2">🏆</p>
          <p>수령 신청 내역이 없습니다</p>
        </div>
      ) : (
        <div className="space-y-2">
          {claims.map((c) => (
            <div key={c.claimId} className="card">
              {/* Header row */}
              <button
                onClick={() => onExpand(c)}
                className="w-full flex items-center justify-between gap-2 text-left"
              >
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1 flex-wrap">
                    <span className={`text-xs px-2 py-0.5 rounded-full font-bold ${STATUS_COLOR[c.status] ?? 'bg-gray-700 text-gray-300'}`}>
                      {STATUS_LABEL[c.status] ?? c.status}
                    </span>
                    <span className="text-xs text-gray-500">{TYPE_LABEL[c.productType] ?? c.productType}</span>
                  </div>
                  <p className="font-bold text-white text-sm truncate">{c.productName}</p>
                  <p className="text-xs text-gray-500">UID: {c.userId.slice(0, 12)}…</p>
                </div>
                <span className="text-gray-500 text-sm shrink-0">{expanded === c.claimId ? '▲' : '▼'}</span>
              </button>

              {/* Expanded detail */}
              {expanded === c.claimId && (
                <div className="mt-4 pt-4 border-t border-card-lt space-y-4">

                  {/* Coupon type */}
                  {c.productType === 'coupon' && (
                    <div className="space-y-3">
                      <p className="text-xs font-bold text-gray-400 uppercase tracking-wide">쿠폰 / 제휴 링크 발급</p>
                      <div>
                        <label className="text-xs text-gray-400 mb-1 block">쿠폰 코드</label>
                        <input
                          className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                          value={couponInput}
                          onChange={(e) => setCouponInput(e.target.value)}
                          placeholder="ABCD-1234-EFGH"
                        />
                      </div>
                      <div>
                        <label className="text-xs text-gray-400 mb-1 block">또는 제휴 URL</label>
                        <input
                          className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                          value={affiliateInput}
                          onChange={(e) => setAffiliateInput(e.target.value)}
                          placeholder="https://..."
                        />
                      </div>
                      <button
                        onClick={() => issueCoupon(c)}
                        disabled={saving || (!couponInput.trim() && !affiliateInput.trim())}
                        className="btn-primary w-full text-sm"
                      >
                        {saving ? '처리 중...' : '쿠폰 발급 완료 처리'}
                      </button>
                    </div>
                  )}

                  {/* Physical / premium: verification (premium only) */}
                  {c.productType === 'premium' && c.verificationStatus !== 'passed' && c.verificationStatus !== 'failed' && (
                    <div className="space-y-3">
                      <p className="text-xs font-bold text-gray-400 uppercase tracking-wide">부정 참여 검증</p>
                      <div>
                        <label className="text-xs text-gray-400 mb-1 block">검증 메모 (사유)</label>
                        <textarea
                          className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none resize-none"
                          rows={2}
                          value={verifyNote}
                          onChange={(e) => setVerifyNote(e.target.value)}
                          placeholder="검증 결과 메모..."
                        />
                      </div>
                      <div className="grid grid-cols-2 gap-2">
                        <button
                          onClick={() => verifyPass(c)}
                          disabled={saving}
                          className="py-2 rounded-xl text-sm font-bold bg-green-900/40 text-green-400 hover:bg-green-900/60 transition-colors"
                        >
                          검증 통과 ✅
                        </button>
                        <button
                          onClick={() => verifyFail(c)}
                          disabled={saving}
                          className="py-2 rounded-xl text-sm font-bold bg-red-900/40 text-red-400 hover:bg-red-900/60 transition-colors"
                        >
                          지급 거절 ❌
                        </button>
                      </div>
                    </div>
                  )}

                  {/* Shipping address display */}
                  {(c.productType === 'physical' || c.productType === 'premium') && c.shippingName && (
                    <div className="space-y-1">
                      <p className="text-xs font-bold text-gray-400 uppercase tracking-wide">배송지 정보</p>
                      <p className="text-sm text-white">{c.shippingName} · {c.shippingPhone}</p>
                      <p className="text-xs text-gray-400">({c.shippingPostcode}) {c.shippingAddress} {c.shippingDetail}</p>
                    </div>
                  )}

                  {/* Shipping tracking input */}
                  {(c.productType === 'physical' || (c.productType === 'premium' && c.verificationStatus === 'passed')) &&
                    c.status === 'address_submitted' && (
                    <div className="space-y-3">
                      <p className="text-xs font-bold text-gray-400 uppercase tracking-wide">운송장 입력</p>
                      <div className="grid grid-cols-2 gap-2">
                        <div>
                          <label className="text-xs text-gray-400 mb-1 block">택배사</label>
                          <input
                            className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                            value={carrierInput}
                            onChange={(e) => setCarrierInput(e.target.value)}
                            placeholder="CJ대한통운"
                          />
                        </div>
                        <div>
                          <label className="text-xs text-gray-400 mb-1 block">운송장 번호</label>
                          <input
                            className="w-full bg-card-lt text-white text-sm px-3 py-2 rounded-xl border border-transparent focus:border-primary outline-none"
                            value={trackingInput}
                            onChange={(e) => setTrackingInput(e.target.value)}
                            placeholder="1234567890"
                          />
                        </div>
                      </div>
                      <button
                        onClick={() => markShipped(c)}
                        disabled={saving || !trackingInput.trim()}
                        className="btn-primary w-full text-sm"
                      >
                        {saving ? '처리 중...' : '배송 처리 완료'}
                      </button>
                    </div>
                  )}

                  {/* Mark delivered */}
                  {c.status === 'shipped' && (
                    <div className="space-y-2">
                      <p className="text-xs text-gray-400">운송장: {c.trackingCarrier} {c.trackingNumber}</p>
                      <button
                        onClick={() => markDelivered(c.claimId)}
                        disabled={saving}
                        className="w-full py-2 rounded-xl text-sm font-bold bg-cyan-900/40 text-cyan-400 hover:bg-cyan-900/60 transition-colors"
                      >
                        배송 완료 확인 ✅
                      </button>
                    </div>
                  )}

                  {/* Metadata */}
                  <div className="pt-2 border-t border-card-lt text-xs text-gray-600 space-y-1">
                    <p>claimId: {c.claimId}</p>
                    <p>drawId: {c.drawId}</p>
                    <p>신청일: {new Date(c.createdAt).toLocaleString('ko-KR')}</p>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

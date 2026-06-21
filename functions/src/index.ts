import * as admin from 'firebase-admin'
import * as functions from 'firebase-functions'
import * as crypto from 'crypto'

admin.initializeApp()
const db  = admin.database()
const fcm = admin.messaging()

export const enterMatchQueue = functions.https.onCall(async (data) => {
  const { userId, roomId, streak } = data as { userId: string; roomId: string; streak: number }
  if (!userId || !roomId) throw new functions.https.HttpsError('invalid-argument', 'userId, roomId 필수')

  const queueRef = db.ref(`matchQueue/${roomId}/${streak}`)
  const snap = await queueRef.once('value')
  let opponentId: string | null = null
  snap.forEach(child => { if (child.key !== userId && !opponentId) opponentId = child.key; return false })

  if (opponentId) {
    const matchId = `match_${roomId}_s${streak}_${Date.now()}`
    await queueRef.child(opponentId).remove()
    await db.ref(`matches/${matchId}`).set({ matchId, player1: opponentId, player2: userId, roomId, streak, createdAt: admin.database.ServerValue.TIMESTAMP })
    await sendToUser(opponentId, { title: '매칭 완료!', body: `${streak}연승 상대를 찾았습니다!`, data: { type: 'match_found', matchId, roomId } })
    await sendToUser(userId,     { title: '매칭 완료!', body: `${streak}연승 상대를 찾았습니다!`, data: { type: 'match_found', matchId, roomId } })
    return { matchId, matched: true }
  } else {
    await queueRef.child(userId).set(admin.database.ServerValue.TIMESTAMP)
    return { matchId: `waiting_${Date.now()}`, matched: false }
  }
})

export const executeDraw = functions.https.onCall(async (data) => {
  const { roomId } = data as { roomId: string }
  if (!roomId) throw new functions.https.HttpsError('invalid-argument', 'roomId 필수')

  const roomSnap = await db.ref(`productRooms/${roomId}`).once('value')
  const room = roomSnap.val()
  if (!room) throw new functions.https.HttpsError('not-found', '상품방 없음')
  if (room.drawStatus === 'drawn') throw new functions.https.HttpsError('already-exists', '이미 추첨됨')

  const entriesSnap = await db.ref('drawEntries').orderByChild('roomId').equalTo(roomId).once('value')
  const entries: Array<{ entryId: string; userId: string; roomId: string }> = []
  entriesSnap.forEach(child => { entries.push(child.val()); return false })
  if (entries.length === 0) throw new functions.https.HttpsError('failed-precondition', '참여자 없음')

  const seed      = `${roomId}_${Date.now()}_${crypto.randomBytes(16).toString('hex')}`
  const seedHash  = crypto.createHash('sha256').update(seed).digest('hex')
  const winnerIdx = parseInt(seedHash.slice(0, 8), 16) % entries.length
  const winner    = entries[winnerIdx]
  const userSnap  = await db.ref(`users/${winner.userId}`).once('value')
  const winnerNickname = userSnap.val()?.nickname ?? '알 수 없음'
  const drawId = `draw_${roomId}_${Date.now()}`

  await Promise.all([
    db.ref(`drawResults/${drawId}`).set({ drawId, roomId, winnerUserId: winner.userId, winnerNickname, seedHash, totalEntries: entries.length, drawnAt: admin.database.ServerValue.TIMESTAMP }),
    db.ref(`productRooms/${roomId}`).update({ drawStatus: 'drawn' }),
    db.ref(`winnerClaims/${drawId}`).set({ claimId: drawId, drawId, roomId, userId: winner.userId, productName: room.productName, productType: room.productType ?? 'coupon', status: 'unclaimed', couponCode: '', affiliateUrl: '', shippingName: '', shippingPhone: '', shippingPostcode: '', shippingAddress: '', shippingDetail: '', trackingNumber: '', trackingCarrier: '', verificationStatus: 'none', verificationNote: '', createdAt: admin.database.ServerValue.TIMESTAMP, updatedAt: admin.database.ServerValue.TIMESTAMP })
  ])

  await sendToUser(winner.userId, { title: '🎉 당첨되셨습니다!', body: `${room.productName} 추첨 당첨! 마이페이지에서 수령하세요.`, data: { type: 'draw_win', roomId, drawId } })
  return { drawId, winnerUserId: winner.userId, winnerNickname, totalEntries: entries.length }
})

export const issueCoupon = functions.https.onCall(async (data) => {
  const { claimId, couponCode, affiliateUrl } = data as { claimId: string; couponCode?: string; affiliateUrl?: string }
  if (!claimId) throw new functions.https.HttpsError('invalid-argument', 'claimId 필수')
  const claim = (await db.ref(`winnerClaims/${claimId}`).once('value')).val()
  if (!claim) throw new functions.https.HttpsError('not-found', '수령 정보 없음')
  await db.ref(`winnerClaims/${claimId}`).update({ couponCode: couponCode ?? '', affiliateUrl: affiliateUrl ?? '', status: 'coupon_issued', updatedAt: admin.database.ServerValue.TIMESTAMP })
  await sendToUser(claim.userId, { title: '쿠폰이 도착했습니다!', body: `${claim.productName} 쿠폰이 발급됐습니다.`, data: { type: 'coupon_issued', claimId, roomId: claim.roomId } })
  return { success: true }
})

export const updateShipping = functions.https.onCall(async (data) => {
  const { claimId, trackingNumber, trackingCarrier, status } = data as { claimId: string; trackingNumber?: string; trackingCarrier?: string; status?: string }
  if (!claimId) throw new functions.https.HttpsError('invalid-argument', 'claimId 필수')
  const claim = (await db.ref(`winnerClaims/${claimId}`).once('value')).val()
  if (!claim) throw new functions.https.HttpsError('not-found', '수령 정보 없음')
  await db.ref(`winnerClaims/${claimId}`).update({ trackingNumber: trackingNumber ?? claim.trackingNumber, trackingCarrier: trackingCarrier ?? claim.trackingCarrier, status: status ?? claim.status, updatedAt: admin.database.ServerValue.TIMESTAMP })
  if (status === 'shipped') await sendToUser(claim.userId, { title: '상품 발송!', body: `${claim.productName} 발송. ${trackingCarrier} ${trackingNumber}`, data: { type: 'shipped', claimId } })
  if (status === 'delivered') await sendToUser(claim.userId, { title: '배송 완료!', body: `${claim.productName} 배송 완료됐습니다.`, data: { type: 'delivered', claimId } })
  return { success: true }
})

export const verifyPremiumClaim = functions.https.onCall(async (data) => {
  const { claimId, verified, note } = data as { claimId: string; verified: boolean; note?: string }
  if (!claimId) throw new functions.https.HttpsError('invalid-argument', 'claimId 필수')
  const claim = (await db.ref(`winnerClaims/${claimId}`).once('value')).val()
  if (!claim) throw new functions.https.HttpsError('not-found', '수령 정보 없음')
  const verificationStatus = verified ? 'verified' : 'rejected'
  const status = verified ? 'verification_passed' : 'verification_failed'
  await db.ref(`winnerClaims/${claimId}`).update({ verificationStatus, verificationNote: note ?? '', status, updatedAt: admin.database.ServerValue.TIMESTAMP })
  const title = verified ? '✅ 검증 통과' : '❌ 검증 실패'
  const body  = verified ? `${claim.productName} 검증 완료. 배송지를 입력해주세요.` : `${claim.productName} 검증 결과: ${note ?? '부정 참여 의심'}`
  await sendToUser(claim.userId, { title, body, data: { type: 'verification_result', claimId, verified: String(verified) } })
  return { success: true }
})

export const applyInviteCode = functions.https.onCall(async (data) => {
  const { userId, inviteCode } = data as { userId: string; inviteCode: string }
  if (!userId || !inviteCode) throw new functions.https.HttpsError('invalid-argument', '파라미터 필수')
  const me = (await db.ref(`users/${userId}`).once('value')).val()
  if (me?.inviteApplied) throw new functions.https.HttpsError('already-exists', '이미 사용한 초대 코드')
  const inviterSnap = await db.ref('users').orderByChild('inviteCode').equalTo(inviteCode).once('value')
  if (!inviterSnap.exists()) throw new functions.https.HttpsError('not-found', '유효하지 않은 초대 코드')
  let inviterId = ''
  inviterSnap.forEach(child => { inviterId = child.key ?? ''; return true })
  if (inviterId === userId) throw new functions.https.HttpsError('invalid-argument', '자신의 코드 사용 불가')
  await Promise.all([
    db.ref(`users/${userId}`).update({ ticketCount: admin.database.ServerValue.increment(3), inviteApplied: true }),
    db.ref(`users/${inviterId}`).update({ ticketCount: admin.database.ServerValue.increment(3) })
  ])
  await sendToUser(inviterId, { title: '친구가 초대 코드를 사용했습니다!', body: '도전권 3장 지급!', data: { type: 'invite_reward' } })
  return { success: true, ticketsEarned: 3 }
})

async function sendToUser(userId: string, payload: { title: string; body: string; data?: Record<string, string> }): Promise<void> {
  const token = (await db.ref(`users/${userId}/fcmToken`).once('value')).val() as string | null
  if (!token) return
  try {
    await fcm.send({ token, notification: { title: payload.title, body: payload.body }, data: payload.data ?? {}, android: { priority: 'high', notification: { channelId: 'baekbunuil_main', sound: 'default' } } })
  } catch {
    await db.ref(`users/${userId}/fcmToken`).remove().catch(() => {})
  }
}

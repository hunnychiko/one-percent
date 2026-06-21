export interface User {
  userId: string
  nickname: string
  adConsent: boolean
  ticketCount: number
  bestStreak: number
  totalWins: number
  status: string
}

export interface ProductRoom {
  roomId: string
  productName: string
  imageUrl: string
  requiredStreak: number
  capacity: number
  currentCount: number
  drawStatus: 'open' | 'drawing' | 'drawn'
  grade: 'C' | 'B' | 'A' | 'S' | 'SS'
  description: string
  round: number
  productType?: 'coupon' | 'physical' | 'premium'
  directBuyLabel?: string   // e.g. "₩9,900" — 빈 문자열이면 도전 포기 옵션 비활성
  createdAt?: number
}

export interface Challenge {
  challengeId: string
  userId: string
  roomId: string
  currentStreak: number
  targetStreak: number
  state: 'active' | 'completed' | 'failed'
}

export interface DrawEntry {
  entryId: string
  roomId: string
  userId: string
  enteredAt: number
}

export interface DrawResult {
  drawId: string
  roomId: string
  winnerUserId: string
  winnerNickname?: string
  seedHash: string
  drawnAt: number
}

export interface RankingEntry {
  userId: string
  nickname: string
  bestStreak: number
  totalWins: number
}

export const GRADE_CONFIG = {
  C:  { label: 'C급',  color: '#4CAF50', min: 3,  max: 3  },
  B:  { label: 'B급',  color: '#2196F3', min: 5,  max: 5  },
  A:  { label: 'A급',  color: '#9C27B0', min: 7,  max: 10 },
  S:  { label: 'S급',  color: '#FF9800', min: 10, max: 15 },
  SS: { label: 'SS급', color: '#E91E63', min: 15, max: 20 },
} as const

export function gradeFromStreak(streak: number): keyof typeof GRADE_CONFIG {
  if (streak <= 3)  return 'C'
  if (streak <= 5)  return 'B'
  if (streak <= 10) return 'A'
  if (streak <= 15) return 'S'
  return 'SS'
}

export function productEmoji(name: string): string {
  if (/청소기|Roborock/i.test(name)) return '🤖'
  if (/이어폰|AirPods|Buds/i.test(name)) return '🎧'
  if (/치킨/i.test(name)) return '🍗'
  if (/커피/i.test(name)) return '☕'
  if (/아이스크림/i.test(name)) return '🍦'
  if (/영화/i.test(name)) return '🎬'
  if (/피자/i.test(name)) return '🍕'
  if (/공기청정|다이슨/i.test(name)) return '💨'
  return '🎁'
}

export const SAMPLE_PRODUCTS: ProductRoom[] = [
  { roomId: 'room_1', productName: '아이스크림 쿠폰', imageUrl: '', requiredStreak: 3,  capacity: 100, currentCount: 42, drawStatus: 'open', grade: 'C',  description: '편의점 아이스크림 교환권',     round: 1 },
  { roomId: 'room_2', productName: '커피 쿠폰',        imageUrl: '', requiredStreak: 3,  capacity: 100, currentCount: 71, drawStatus: 'open', grade: 'C',  description: '스타벅스 아메리카노',           round: 1 },
  { roomId: 'room_3', productName: '치킨 1마리',        imageUrl: '', requiredStreak: 5,  capacity: 100, currentCount: 28, drawStatus: 'open', grade: 'B',  description: 'BBQ 황금올리브 치킨',           round: 1 },
  { roomId: 'room_4', productName: '영화 관람권 2매',   imageUrl: '', requiredStreak: 5,  capacity: 100, currentCount: 55, drawStatus: 'open', grade: 'B',  description: 'CGV 주말 영화 2매',             round: 1 },
  { roomId: 'room_5', productName: '무선 이어폰',       imageUrl: '', requiredStreak: 7,  capacity: 100, currentCount: 19, drawStatus: 'open', grade: 'A',  description: 'Galaxy Buds2 Pro',             round: 1 },
  { roomId: 'room_6', productName: '로봇청소기 특가',   imageUrl: '', requiredStreak: 10, capacity: 100, currentCount: 37, drawStatus: 'open', grade: 'S',  description: 'Roborock S8 Pro Ultra',        round: 1 },
  { roomId: 'room_7', productName: '에어팟 프로 2세대', imageUrl: '', requiredStreak: 10, capacity: 100, currentCount: 8,  drawStatus: 'open', grade: 'S',  description: 'Apple AirPods Pro 2',          round: 1 },
  { roomId: 'room_8', productName: '프리미엄 공기청정기', imageUrl: '', requiredStreak: 15, capacity: 100, currentCount: 3, drawStatus: 'open', grade: 'SS', description: '다이슨 공기청정기 한정 특가', round: 1 },
]

'use client'

import Link from 'next/link'
import { productEmoji, type ProductRoom } from '@/lib/types'
import { GradeBadge } from './GradeBadge'
import { StreakStars } from './StreakStars'
import { ParticipantBar } from './ParticipantBar'

export function ProductCard({ product }: { product: ProductRoom }) {
  const emoji  = productEmoji(product.productName)
  const isFull = product.currentCount >= product.capacity

  return (
    <Link href={`/product/${product.roomId}`} className="block">
      <div className="card hover:bg-card-lt transition-colors cursor-pointer">
        {/* Image / Emoji area */}
        <div className="h-28 rounded-xl bg-card-lt flex items-center justify-center text-5xl mb-3">
          {product.imageUrl
            ? <img src={product.imageUrl} alt={product.productName} className="h-full w-full object-cover rounded-xl" />
            : emoji
          }
        </div>

        {/* Grade + Name */}
        <div className="flex items-center gap-2 mb-1">
          <GradeBadge grade={product.grade} />
          {isFull && (
            <span className="text-xs text-red-400 font-semibold">마감</span>
          )}
        </div>

        <p className="font-bold text-sm text-white leading-tight mb-1">{product.productName}</p>
        <p className="text-xs text-gray-400 mb-3">{product.description}</p>

        <StreakStars required={product.requiredStreak} />

        <div className="mt-3">
          <ParticipantBar current={product.currentCount} capacity={product.capacity} />
        </div>
      </div>
    </Link>
  )
}

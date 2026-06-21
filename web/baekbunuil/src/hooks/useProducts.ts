'use client'

import { useState, useEffect } from 'react'
import { ref, onValue, off } from 'firebase/database'
import { db } from '@/lib/firebase'
import { SAMPLE_PRODUCTS, type ProductRoom } from '@/lib/types'

export function useProducts() {
  const [products, setProducts] = useState<ProductRoom[]>(SAMPLE_PRODUCTS)
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    const roomsRef = ref(db, 'productRooms')

    const handler = onValue(
      roomsRef,
      (snap) => {
        if (snap.exists()) {
          const data = snap.val() as Record<string, ProductRoom>
          setProducts(Object.values(data).sort((a, b) => (a.requiredStreak - b.requiredStreak)))
        } else {
          setProducts(SAMPLE_PRODUCTS)
        }
        setLoading(false)
      },
      () => {
        setProducts(SAMPLE_PRODUCTS)
        setLoading(false)
      }
    )

    return () => off(roomsRef, 'value', handler)
  }, [])

  return { products, loading }
}

export function useProduct(roomId: string) {
  const [product, setProduct] = useState<ProductRoom | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const roomRef = ref(db, `productRooms/${roomId}`)

    const handler = onValue(
      roomRef,
      (snap) => {
        if (snap.exists()) {
          setProduct(snap.val() as ProductRoom)
        } else {
          const sample = SAMPLE_PRODUCTS.find((p) => p.roomId === roomId) ?? null
          setProduct(sample)
        }
        setLoading(false)
      },
      () => {
        setLoading(false)
      }
    )

    return () => off(roomRef, 'value', handler)
  }, [roomId])

  return { product, loading }
}

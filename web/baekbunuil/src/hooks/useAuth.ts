'use client'

import { useState, useEffect } from 'react'
import { onAuthStateChanged, signInWithPopup, signOut as firebaseSignOut, User as FirebaseUser } from 'firebase/auth'
import { ref, get, set, serverTimestamp } from 'firebase/database'
import { auth, db, googleProvider, isAdmin } from '@/lib/firebase'
import type { User } from '@/lib/types'

export function useAuth() {
  const [firebaseUser, setFirebaseUser] = useState<FirebaseUser | null>(null)
  const [user, setUser]                 = useState<User | null>(null)
  const [loading, setLoading]           = useState(true)
  const [admin, setAdmin]               = useState(false)

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, async (fbUser) => {
      setFirebaseUser(fbUser)
      setAdmin(fbUser ? isAdmin(fbUser.uid) : false)

      if (fbUser) {
        const userRef = ref(db, `users/${fbUser.uid}`)
        const snap    = await get(userRef)

        if (snap.exists()) {
          setUser(snap.val() as User)
        } else {
          const newUser: User = {
            userId:      fbUser.uid,
            nickname:    fbUser.displayName ?? `유저${fbUser.uid.slice(0, 4)}`,
            adConsent:   false,
            ticketCount: 0,
            bestStreak:  0,
            totalWins:   0,
            status:      'active',
          }
          await set(userRef, { ...newUser, createdAt: serverTimestamp() })
          setUser(newUser)
        }
      } else {
        setUser(null)
      }

      setLoading(false)
    })

    return unsub
  }, [])

  const signIn = () => signInWithPopup(auth, googleProvider)

  const signOut = async () => {
    await firebaseSignOut(auth)
    setUser(null)
  }

  return { firebaseUser, user, loading, admin, signIn, signOut }
}

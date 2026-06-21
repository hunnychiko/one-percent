package com.hunnychiko.baekbunuil.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.hunnychiko.baekbunuil.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GameRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    val currentUserId get() = auth.currentUser?.uid ?: ""

    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: error("익명 로그인 실패")
    }

    suspend fun signInWithGoogle(idToken: String): String {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.uid ?: error("Google 로그인 실패")
    }

    suspend fun getOrCreateUser(userId: String, nickname: String): User {
        val ref = db.getReference("users/$userId")
        val snapshot = ref.get().await()
        return if (snapshot.exists()) {
            snapshot.getValue(User::class.java) ?: User(userId = userId, nickname = nickname)
        } else {
            val user = User(userId = userId, nickname = nickname, ticketCount = 1)
            ref.setValue(user).await()
            user
        }
    }

    fun observeUser(userId: String): Flow<User> = callbackFlow {
        val ref = db.getReference("users/$userId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(User::class.java)?.let { trySend(it) }
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeProductRooms(): Flow<List<ProductRoom>> = callbackFlow {
        val ref = db.getReference("productRooms")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { it.getValue(ProductRoom::class.java) }
                trySend(rooms.ifEmpty { sampleProducts })
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeProductRoom(roomId: String): Flow<ProductRoom?> = callbackFlow {
        val ref = db.getReference("productRooms/$roomId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(ProductRoom::class.java))
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeChallenge(userId: String, roomId: String): Flow<Challenge?> = callbackFlow {
        val ref = db.getReference("challenges/${userId}_$roomId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Challenge::class.java))
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun claimAdReward(userId: String): Boolean {
        return try {
            val data = hashMapOf("userId" to userId)
            val result = functions.getHttpsCallable("claimAdReward").call(data).await()
            (result.data as? Map<*, *>)?.get("success") as? Boolean ?: false
        } catch (e: Exception) {
            // Firebase Function 없을 때 로컬 처리
            val userRef = db.getReference("users/$userId")
            val snapshot = userRef.get().await()
            val user = snapshot.getValue(User::class.java) ?: return false
            val adLogRef = db.getReference("adLogs/$userId/today")
            val logSnapshot = adLogRef.get().await()
            val todayCount = logSnapshot.getValue(Int::class.java) ?: 0
            if (todayCount >= 10) return false
            userRef.child("ticketCount").setValue(user.ticketCount + 1).await()
            adLogRef.setValue(todayCount + 1).await()
            true
        }
    }

    suspend fun getTodayAdCount(userId: String): Int {
        return try {
            val snapshot = db.getReference("adLogs/$userId/today").get().await()
            snapshot.getValue(Int::class.java) ?: 0
        } catch (e: Exception) { 0 }
    }

    suspend fun enterMatchQueue(userId: String, roomId: String): String {
        val data = hashMapOf("userId" to userId, "roomId" to roomId)
        return try {
            val result = functions.getHttpsCallable("enterMatchQueue").call(data).await()
            (result.data as? Map<*, *>)?.get("matchId") as? String ?: ""
        } catch (e: Exception) {
            val matchId = "match_${System.currentTimeMillis()}"
            db.getReference("matchQueue/$roomId/$userId").setValue(true).await()
            matchId
        }
    }

    fun observeMatch(matchId: String): Flow<Match?> = callbackFlow {
        val ref = db.getReference("matches/$matchId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Match::class.java))
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun submitChoice(matchId: String, userId: String, choice: RpsChoice): Unit {
        db.getReference("matches/$matchId/choices/$userId").setValue(choice.name).await()
    }

    suspend fun enterRaffle(userId: String, roomId: String): Boolean {
        return try {
            val entryRef = db.getReference("drawEntries/$roomId/$userId")
            val entry = DrawEntry(
                entryId = "${roomId}_$userId",
                roomId = roomId,
                userId = userId,
                enteredAt = System.currentTimeMillis()
            )
            entryRef.setValue(entry).await()
            val roomRef = db.getReference("productRooms/$roomId/currentCount")
            roomRef.get().await().getValue(Int::class.java)?.let { count ->
                roomRef.setValue(count + 1).await()
            }
            true
        } catch (e: Exception) { false }
    }

    suspend fun checkDrawResult(userId: String, roomId: String): DrawResult? {
        return try {
            val snapshot = db.getReference("drawResults/$roomId").get().await()
            val result = snapshot.getValue(DrawResult::class.java) ?: return null
            result.copy(isWinner = result.winnerUserId == userId)
        } catch (e: Exception) { null }
    }

    fun isSignedIn() = auth.currentUser != null
    fun signOut() = auth.signOut()
}

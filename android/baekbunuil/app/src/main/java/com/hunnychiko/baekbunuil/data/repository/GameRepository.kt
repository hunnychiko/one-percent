package com.hunnychiko.baekbunuil.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.hunnychiko.baekbunuil.data.model.*
import com.hunnychiko.baekbunuil.viewmodel.ChallengeHistoryItem
import com.hunnychiko.baekbunuil.viewmodel.RankingEntry
import com.hunnychiko.baekbunuil.viewmodel.WinHistoryItem
import com.google.firebase.database.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    // 연승 수 기준 매칭: matchQueue/$roomId/$streak/$userId
    suspend fun enterMatchQueue(userId: String, roomId: String, streak: Int): String {
        val data = hashMapOf("userId" to userId, "roomId" to roomId, "streak" to streak)
        return try {
            val result = functions.getHttpsCallable("enterMatchQueue").call(data).await()
            (result.data as? Map<*, *>)?.get("matchId") as? String ?: ""
        } catch (e: Exception) {
            val queueRef = db.getReference("matchQueue/$roomId/$streak")
            val snapshot = queueRef.get().await()
            val waiting = snapshot.children.firstOrNull { it.key != userId }
            if (waiting != null) {
                val opponentId = waiting.key ?: ""
                val matchId = "match_${roomId}_s${streak}_${System.currentTimeMillis()}"
                queueRef.child(opponentId).removeValue().await()
                db.getReference("matches/$matchId").setValue(
                    mapOf("matchId" to matchId, "player1" to opponentId, "player2" to userId,
                        "roomId" to roomId, "streak" to streak)
                ).await()
                matchId
            } else {
                val matchId = "match_${System.currentTimeMillis()}"
                queueRef.child(userId).setValue(System.currentTimeMillis()).await()
                matchId
            }
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

    suspend fun getRankings(): List<RankingEntry> {
        val snapshot = db.getReference("users")
            .orderByChild("bestStreak")
            .limitToLast(30)
            .get()
            .await()
        return snapshot.children
            .mapNotNull { child ->
                val u = child.getValue(User::class.java) ?: return@mapNotNull null
                RankingEntry(
                    userId = u.userId,
                    nickname = u.nickname,
                    bestStreak = u.bestStreak,
                    totalWins = u.totalWins
                )
            }
            .sortedByDescending { it.bestStreak }
            .mapIndexed { i, e -> e.copy(rank = i + 1) }
    }

    suspend fun getUserHistory(
        userId: String,
        products: List<ProductRoom>
    ): Pair<List<ChallengeHistoryItem>, List<WinHistoryItem>> {
        val dateFormat = SimpleDateFormat("yy.MM.dd", Locale.KOREA)

        val challengeSnap = db.getReference("challenges")
            .orderByChild("userId")
            .equalTo(userId)
            .get()
            .await()

        val challenges = challengeSnap.children.mapNotNull { child ->
            val c = child.getValue(Challenge::class.java) ?: return@mapNotNull null
            val product = products.find { it.roomId == c.roomId }
            ChallengeHistoryItem(
                challengeId = c.challengeId,
                roomId      = c.roomId,
                productName = product?.productName ?: c.roomId,
                emoji       = productEmoji(product?.productName ?: ""),
                currentStreak = c.currentStreak,
                targetStreak  = c.targetStreak,
                state         = c.state,
                timeAgo       = ""
            )
        }.sortedByDescending { it.state == "active" }

        val drawSnap = db.getReference("drawEntries")
            .get()
            .await()

        val winHistory = mutableListOf<WinHistoryItem>()
        drawSnap.children.forEach { roomNode ->
            val roomId = roomNode.key ?: return@forEach
            if (roomNode.child(userId).exists()) {
                val resultSnap = db.getReference("drawResults/$roomId").get().await()
                val result = resultSnap.getValue(DrawResult::class.java)
                if (result != null && result.winnerUserId == userId) {
                    val product = products.find { it.roomId == roomId }
                    winHistory.add(
                        WinHistoryItem(
                            drawId      = result.drawId,
                            roomId      = roomId,
                            productName = product?.productName ?: roomId,
                            round       = product?.round ?: 1,
                            wonAt       = dateFormat.format(Date(result.drawnAt))
                        )
                    )
                }
            }
        }

        return challenges to winHistory.sortedByDescending { it.wonAt }
    }

    private fun productEmoji(name: String): String = when {
        name.contains("청소기", ignoreCase = true) || name.contains("Roborock", ignoreCase = true) -> "🤖"
        name.contains("이어폰", ignoreCase = true) || name.contains("AirPods", ignoreCase = true) || name.contains("Buds", ignoreCase = true) -> "🎧"
        name.contains("치킨", ignoreCase = true) -> "🍗"
        name.contains("커피", ignoreCase = true) -> "☕"
        name.contains("아이스크림", ignoreCase = true) -> "🍦"
        name.contains("영화", ignoreCase = true) -> "🎬"
        name.contains("피자", ignoreCase = true) -> "🍕"
        name.contains("공기청정", ignoreCase = true) || name.contains("다이슨", ignoreCase = true) -> "💨"
        else -> "🎁"
    }

    fun observeAffiliateBanners(): Flow<List<AffiliateBanner>> = callbackFlow {
        val ref = db.getReference("affiliateBanners")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val banners = snapshot.children
                    .mapNotNull { it.getValue(AffiliateBanner::class.java) }
                    .filter { it.isActive }
                    .sortedBy { it.order }
                trySend(banners)
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun claimAffiliateReward(userId: String, bannerId: String, ticketReward: Int): Boolean {
        return try {
            val claimRef = db.getReference("affiliateClaims/$bannerId/$userId")
            val snap = claimRef.get().await()
            if (snap.exists()) return false
            claimRef.setValue(System.currentTimeMillis()).await()
            val userRef = db.getReference("users/$userId/ticketCount")
            val current = userRef.get().await().getValue(Int::class.java) ?: 0
            userRef.setValue(current + ticketReward).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun getOrCreateInviteCode(userId: String): String {
        val ref = db.getReference("inviteCodes")
        // 기존 코드 검색
        val existing = ref.orderByChild("ownerUserId").equalTo(userId).get().await()
        if (existing.exists()) {
            return existing.children.first().getValue(InviteCode::class.java)?.code ?: generateCode(userId)
        }
        // 신규 생성
        val code = generateCode(userId)
        val invite = InviteCode(
            code = code,
            ownerUserId = userId,
            usedCount = 0,
            rewardPerInvite = 3,
            createdAt = System.currentTimeMillis()
        )
        ref.child(code).setValue(invite).await()
        return code
    }

    suspend fun applyInviteCode(userId: String, code: String): String {
        return try {
            val codeRef  = db.getReference("inviteCodes/$code")
            val snap     = codeRef.get().await()
            if (!snap.exists()) return "invalid"
            val invite   = snap.getValue(InviteCode::class.java) ?: return "invalid"
            if (invite.ownerUserId == userId) return "self"
            val usedRef  = db.getReference("inviteUsed/$userId")
            if (usedRef.get().await().exists()) return "used"
            usedRef.setValue(code).await()
            codeRef.child("usedCount").setValue(invite.usedCount + 1).await()
            // 초대받은 사람 티켓 +3
            val myTickets = db.getReference("users/$userId/ticketCount")
            myTickets.setValue((myTickets.get().await().getValue(Int::class.java) ?: 0) + 3).await()
            // 초대한 사람 티켓 +3
            val ownerTickets = db.getReference("users/${invite.ownerUserId}/ticketCount")
            ownerTickets.setValue((ownerTickets.get().await().getValue(Int::class.java) ?: 0) + 3).await()
            "success"
        } catch (e: Exception) { "error" }
    }

    private fun generateCode(userId: String): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val seed = userId.hashCode().toLong() + System.currentTimeMillis()
        val rng  = java.util.Random(seed)
        return (1..6).map { chars[rng.nextInt(chars.length)] }.joinToString("")
    }

    fun observeWinnerClaim(userId: String, roomId: String): Flow<WinnerClaim?> = callbackFlow {
        val ref = db.getReference("winnerClaims").orderByChild("userId").equalTo(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val claim = snapshot.children
                    .mapNotNull { it.getValue(WinnerClaim::class.java) }
                    .firstOrNull { it.roomId == roomId }
                trySend(claim)
            }
            override fun onCancelled(error: DatabaseError) = close(error.toException())
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // 수령 신청 최초 생성 또는 기존 클레임 반환
    suspend fun createOrGetClaim(
        userId: String, roomId: String, productName: String, productType: String
    ): WinnerClaim {
        val ref = db.getReference("winnerClaims")
        val existing = ref.orderByChild("userId").equalTo(userId).get().await()
        val found = existing.children
            .mapNotNull { it.getValue(WinnerClaim::class.java) }
            .firstOrNull { it.roomId == roomId }
        if (found != null) return found

        val drawResult = db.getReference("drawResults/$roomId").get().await()
            .getValue(DrawResult::class.java)
        val claimId = ref.push().key ?: "claim_${System.currentTimeMillis()}"
        val initialStatus = when (productType) {
            "coupon"   -> "unclaimed"
            "physical" -> "address_pending"
            "premium"  -> "verifying"
            else       -> "unclaimed"
        }
        val claim = WinnerClaim(
            claimId     = claimId,
            drawId      = drawResult?.drawId ?: "",
            roomId      = roomId,
            userId      = userId,
            productName = productName,
            productType = productType,
            status      = initialStatus,
            createdAt   = System.currentTimeMillis(),
            updatedAt   = System.currentTimeMillis()
        )
        ref.child(claimId).setValue(claim).await()
        return claim
    }

    suspend fun submitShippingAddress(
        claimId: String,
        name: String, phone: String,
        postcode: String, address: String, detail: String
    ): Boolean {
        return try {
            val updates = mapOf(
                "shippingName"     to name,
                "shippingPhone"    to phone,
                "shippingPostcode" to postcode,
                "shippingAddress"  to address,
                "shippingDetail"   to detail,
                "status"           to "address_submitted",
                "updatedAt"        to System.currentTimeMillis()
            )
            db.getReference("winnerClaims/$claimId").updateChildren(updates).await()
            true
        } catch (e: Exception) { false }
    }

    fun isSignedIn() = auth.currentUser != null
    fun signOut() = auth.signOut()
}

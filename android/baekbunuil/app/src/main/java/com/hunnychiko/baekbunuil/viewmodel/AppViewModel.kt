package com.hunnychiko.baekbunuil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.hunnychiko.baekbunuil.data.NotificationStore
import com.hunnychiko.baekbunuil.data.UserPreferences
import com.hunnychiko.baekbunuil.data.model.*
import com.hunnychiko.baekbunuil.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class RankingEntry(
    val userId: String = "",
    val nickname: String = "",
    val bestStreak: Int = 0,
    val totalWins: Int = 0,
    val rank: Int = 0
)

data class ChallengeHistoryItem(
    val challengeId: String = "",
    val roomId: String = "",
    val productName: String = "",
    val emoji: String = "",
    val currentStreak: Int = 0,
    val targetStreak: Int = 3,
    val state: String = "active",
    val timeAgo: String = ""
)

data class WinHistoryItem(
    val drawId: String = "",
    val roomId: String = "",
    val productName: String = "",
    val round: Int = 1,
    val wonAt: String = ""
)

class AppViewModel : ViewModel() {
    val repo = GameRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _products = MutableStateFlow<List<ProductRoom>>(sampleProducts)
    val products: StateFlow<List<ProductRoom>> = _products

    private val _currentChallenge = MutableStateFlow<Challenge?>(null)
    val currentChallenge: StateFlow<Challenge?> = _currentChallenge

    private val _todayAdCount = MutableStateFlow(0)
    val todayAdCount: StateFlow<Int> = _todayAdCount

    private val _matchState = MutableStateFlow<MatchUiState>(MatchUiState.Idle)
    val matchState: StateFlow<MatchUiState> = _matchState

    private val _battleState = MutableStateFlow<BattleUiState>(BattleUiState.Waiting)
    val battleState: StateFlow<BattleUiState> = _battleState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _rankings = MutableStateFlow<List<RankingEntry>>(emptyList())
    val rankings: StateFlow<List<RankingEntry>> = _rankings

    private val _isRankingLoading = MutableStateFlow(false)
    val isRankingLoading: StateFlow<Boolean> = _isRankingLoading

    private val _challengeHistory = MutableStateFlow<List<ChallengeHistoryItem>>(emptyList())
    val challengeHistory: StateFlow<List<ChallengeHistoryItem>> = _challengeHistory

    private val _winHistory = MutableStateFlow<List<WinHistoryItem>>(emptyList())
    val winHistory: StateFlow<List<WinHistoryItem>> = _winHistory

    private val _affiliateBanners = MutableStateFlow<List<com.hunnychiko.baekbunuil.data.model.AffiliateBanner>>(emptyList())
    val affiliateBanners: StateFlow<List<com.hunnychiko.baekbunuil.data.model.AffiliateBanner>> = _affiliateBanners

    private val _affiliateRewardMessage = MutableStateFlow<String?>(null)
    val affiliateRewardMessage: StateFlow<String?> = _affiliateRewardMessage

    private val _currentClaim = MutableStateFlow<com.hunnychiko.baekbunuil.data.model.WinnerClaim?>(null)
    val currentClaim: StateFlow<com.hunnychiko.baekbunuil.data.model.WinnerClaim?> = _currentClaim

    private val _claimMessage = MutableStateFlow<String?>(null)
    val claimMessage: StateFlow<String?> = _claimMessage

    private val _myInviteCode = MutableStateFlow<String?>(null)
    val myInviteCode: StateFlow<String?> = _myInviteCode

    private val _inviteMessage = MutableStateFlow<String?>(null)
    val inviteMessage: StateFlow<String?> = _inviteMessage

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    val unreadNotificationCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    private val _dailyBonusAvailable = MutableStateFlow(false)
    val dailyBonusAvailable: StateFlow<Boolean> = _dailyBonusAvailable

    private val _dailyBonusConfig = MutableStateFlow<com.hunnychiko.baekbunuil.data.model.DailyBonusConfig?>(null)
    val dailyBonusConfig: StateFlow<com.hunnychiko.baekbunuil.data.model.DailyBonusConfig?> = _dailyBonusConfig

    init {
        viewModelScope.launch {
            NotificationStore.notifications.collect { _notifications.value = it }
        }
        if (repo.isSignedIn()) {
            loadUser()
            loadProducts()
        }
    }

    fun signInAnonymously(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = repo.signInAnonymously()
                val user = repo.getOrCreateUser(uid, "도전자_${uid.takeLast(4)}")
                _user.value = user?.copy(avatarId = UserPreferences.avatarId)
                loadProducts()
                loadTodayAdCount(uid)
                registerFcmToken(uid)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "로그인 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = repo.signInWithGoogle(idToken)
                val user = repo.getOrCreateUser(uid, "유저_${uid.takeLast(4)}")
                _user.value = user?.copy(avatarId = UserPreferences.avatarId)
                loadProducts()
                loadTodayAdCount(uid)
                registerFcmToken(uid)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Google 로그인 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun registerFcmToken(userId: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                repo.saveFcmToken(userId, token)
            } catch (_: Exception) {}
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                repo.observeUser(repo.currentUserId).collect { user ->
                    _user.value = user?.copy(avatarId = UserPreferences.avatarId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                repo.observeProductRooms().collect { rooms ->
                    _products.value = rooms.sortedWith(
                        compareByDescending<ProductRoom> { gradeOrdinal(it.grade) }
                            .thenByDescending { it.currentCount.toFloat() / it.capacity.coerceAtLeast(1) }
                    )
                }
            } catch (e: Exception) {
                _products.value = sampleProducts
            }
        }
    }

    private fun gradeOrdinal(grade: String) = when (grade) {
        "SS" -> 4; "S" -> 3; "A" -> 2; "B" -> 1; else -> 0
    }

    fun loadChallenge(roomId: String) {
        viewModelScope.launch {
            try {
                repo.observeChallenge(repo.currentUserId, roomId).collect { _currentChallenge.value = it }
            } catch (e: Exception) { }
        }
    }

    private fun loadTodayAdCount(userId: String) {
        viewModelScope.launch {
            _todayAdCount.value = repo.getTodayAdCount(userId)
        }
    }

    fun claimAdReward(onSuccess: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            val uid = repo.currentUserId
            val claimed = repo.claimAdReward(uid)
            if (claimed) {
                _todayAdCount.value = _todayAdCount.value + 1
                _user.value = _user.value?.copy(ticketCount = (_user.value?.ticketCount ?: 0) + 1)
                onSuccess()
            } else {
                onFail()
            }
        }
    }

    fun startMatching(roomId: String) {
        val myStreak = _currentChallenge.value?.currentStreak ?: 0
        viewModelScope.launch {
            _matchState.value = MatchUiState.Searching
            try {
                val matchId = repo.enterMatchQueue(repo.currentUserId, roomId, myStreak)
                delay(3000)
                val opponent = Opponent(
                    userId = "bot_${System.currentTimeMillis()}",
                    nickname = listOf("우주탐험가", "별빛기사", "도전왕", "연승마스터").random(),
                    currentStreak = myStreak,   // 동일 연승 수 상대와 매칭
                    avatarIndex = (0..9).random()
                )
                _matchState.value = MatchUiState.Found(matchId, opponent)
            } catch (e: Exception) {
                _matchState.value = MatchUiState.Error(e.message ?: "매칭 오류")
            }
        }
    }

    fun cancelMatching() {
        _matchState.value = MatchUiState.Idle
    }

    fun startBattle(matchId: String, opponent: Opponent, roomId: String) {
        _battleState.value = BattleUiState.Selecting(matchId, opponent, roomId)
    }

    fun submitChoice(choice: RpsChoice, roomId: String) {
        val currentState = _battleState.value as? BattleUiState.Selecting ?: return
        viewModelScope.launch {
            // Commit-reveal fairness: pre-commit opponent choice before revealing
            val opponentChoice = RpsChoice.entries.random()
            val revealedSeed = java.util.UUID.randomUUID().toString()
            val commitHash = sha256("$revealedSeed|${opponentChoice.name}")

            _battleState.value = BattleUiState.WaitingResult(currentState.matchId, currentState.opponent, choice, commitHash)
            delay(1200)

            val result = determineWinner(choice, opponentChoice)
            val currentStreak = _currentChallenge.value?.currentStreak ?: 0
            val newStreak = when (result) {
                MatchResult.WIN -> currentStreak + 1
                MatchResult.DRAW -> currentStreak
                MatchResult.LOSE -> 0
            }

            _battleState.value = BattleUiState.Result(
                matchId = currentState.matchId,
                opponent = currentState.opponent,
                myChoice = choice,
                opponentChoice = opponentChoice,
                result = result,
                newStreak = newStreak,
                targetStreak = _currentChallenge.value?.targetStreak ?: 3,
                roomId = roomId,
                commitHash = commitHash,
                revealedSeed = revealedSeed
            )

            when (result) {
                MatchResult.WIN -> {
                    _currentChallenge.value = _currentChallenge.value?.copy(currentStreak = newStreak)
                        ?: Challenge(currentStreak = newStreak)
                    _user.value = _user.value?.let { u ->
                        u.copy(
                            ticketCount = (u.ticketCount - 1).coerceAtLeast(0),
                            bestStreak = maxOf(u.bestStreak, newStreak)
                        )
                    }
                }
                MatchResult.DRAW -> { /* 무승부: 승부권 소모 없음, 연승 유지 */ }
                MatchResult.LOSE -> {
                    _currentChallenge.value = _currentChallenge.value?.copy(currentStreak = 0)
                    _user.value = _user.value?.copy(ticketCount = (_user.value?.ticketCount ?: 1) - 1)
                }
            }
        }
    }

    private fun sha256(input: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun retryDraw(matchId: String, opponent: Opponent, roomId: String) {
        _battleState.value = BattleUiState.Selecting(matchId, opponent, roomId)
    }

    fun resetBattle() {
        _battleState.value = BattleUiState.Waiting
        _matchState.value = MatchUiState.Idle
    }

    fun enterRaffle(roomId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = repo.enterRaffle(repo.currentUserId, roomId)
            if (success) onSuccess()
        }
    }

    fun loadRankings() {
        if (_isRankingLoading.value) return
        viewModelScope.launch {
            _isRankingLoading.value = true
            try {
                val entries = repo.getRankings()
                _rankings.value = entries
            } catch (e: Exception) {
                _rankings.value = listOf(
                    RankingEntry("u1", "연승왕",   23, 5, 1),
                    RankingEntry("u2", "행운아",   18, 3, 2),
                    RankingEntry("u3", "도전자",   15, 2, 3),
                    RankingEntry("u4", "승부사",   12, 1, 4),
                    RankingEntry("u5", "럭키가이", 10, 1, 5),
                )
            } finally {
                _isRankingLoading.value = false
            }
        }
    }

    fun loadHistory() {
        val userId = repo.currentUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            try {
                val (challenges, wins) = repo.getUserHistory(userId, _products.value)
                _challengeHistory.value = challenges
                _winHistory.value = wins
            } catch (e: Exception) { }
        }
    }

    fun loadAffiliateBanners() {
        viewModelScope.launch {
            try {
                repo.observeAffiliateBanners().collect { _affiliateBanners.value = it }
            } catch (e: Exception) { }
        }
    }

    fun claimAffiliateReward(bannerId: String) {
        viewModelScope.launch {
            val banner = _affiliateBanners.value.find { it.bannerId == bannerId } ?: return@launch
            val uid = repo.currentUserId
            if (uid.isEmpty()) return@launch
            val success = repo.claimAffiliateReward(uid, bannerId, banner.ticketReward)
            if (success) {
                _user.value = _user.value?.copy(
                    ticketCount = (_user.value?.ticketCount ?: 0) + banner.ticketReward
                )
                _affiliateRewardMessage.value = "🎉 ${banner.companyName} 제휴 보상으로 도전권 ${banner.ticketReward}개 획득!"
            } else {
                _affiliateRewardMessage.value = "이미 이 배너의 보상을 받았어요"
            }
        }
    }

    fun clearAffiliateMessage() { _affiliateRewardMessage.value = null }

    fun loadMyInviteCode() {
        val uid = repo.currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val code = repo.getOrCreateInviteCode(uid)
            _myInviteCode.value = code
        }
    }

    fun applyInviteCode(code: String) {
        val uid = repo.currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val result = repo.applyInviteCode(uid, code)
            _inviteMessage.value = when (result) {
                "success" -> "🎁 초대 코드 적용 완료! 도전권 3개 획득"
                "self"    -> "자신의 초대 코드는 사용할 수 없어요"
                "used"    -> "이미 사용한 초대 코드예요"
                else      -> "유효하지 않은 초대 코드예요"
            }
            if (result == "success") {
                _user.value = _user.value?.copy(
                    ticketCount = (_user.value?.ticketCount ?: 0) + 3
                )
            }
        }
    }

    fun clearInviteMessage() { _inviteMessage.value = null }

    fun loadWinnerClaim(roomId: String) {
        val uid = repo.currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                repo.observeWinnerClaim(uid, roomId).collect { _currentClaim.value = it }
            } catch (e: Exception) { }
        }
    }

    fun initiateClaim(roomId: String) {
        val uid = repo.currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val product = _products.value.find { it.roomId == roomId } ?: return@launch
            val claim = repo.createOrGetClaim(uid, roomId, product.productName, product.productType)
            _currentClaim.value = claim
        }
    }

    fun submitShippingAddress(
        claimId: String,
        name: String, phone: String,
        postcode: String, address: String, detail: String
    ) {
        viewModelScope.launch {
            val ok = repo.submitShippingAddress(claimId, name, phone, postcode, address, detail)
            if (ok) {
                _currentClaim.value = _currentClaim.value?.copy(
                    status = "address_submitted",
                    shippingName = name, shippingPhone = phone,
                    shippingPostcode = postcode, shippingAddress = address, shippingDetail = detail
                )
                _claimMessage.value = "배송지가 접수되었습니다. 운영자 검토 후 발송됩니다."
            } else {
                _claimMessage.value = "오류가 발생했습니다. 다시 시도해주세요."
            }
        }
    }

    fun clearClaimMessage() { _claimMessage.value = null }

    // 도전 포기 → 100% 직접 획득 처리 (결제 완료 후 호출)
    fun forfeitForGuaranteed(roomId: String, onSuccess: () -> Unit) {
        val uid = repo.currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val product = _products.value.find { it.roomId == roomId } ?: return@launch
            // 채린지 초기화
            _currentChallenge.value = null
            _battleState.value = BattleUiState.Waiting
            _matchState.value = MatchUiState.Idle
            // 클레임 생성 (guaranteed 상태로)
            val claim = repo.createOrGetClaim(uid, roomId, product.productName, product.productType)
            _currentClaim.value = claim
            onSuccess()
        }
    }

    fun markNotificationsRead() {
        NotificationStore.markAllRead()
    }

    fun checkDailyBonus() {
        val uid = repo.currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                val config = repo.getDailyBonusConfig()
                _dailyBonusConfig.value = config
                val lastDate = repo.getLastDailyBonusDate(uid)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                _dailyBonusAvailable.value = config.enabled && lastDate != today
            } catch (_: Exception) { }
        }
    }

    fun claimDailyBonus(onSuccess: (Int) -> Unit) {
        val uid = repo.currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                val config = _dailyBonusConfig.value
                val reward = config?.rewardTickets ?: 1
                val success = repo.claimDailyBonus(uid, reward)
                if (success) {
                    _dailyBonusAvailable.value = false
                    _user.value = _user.value?.copy(
                        ticketCount = (_user.value?.ticketCount ?: 0) + reward
                    )
                    onSuccess(reward)
                }
            } catch (_: Exception) { }
        }
    }

    fun updateAvatar(avatarId: Int, photoUri: String = "") {
        UserPreferences.avatarId = avatarId
        if (photoUri.isNotEmpty()) UserPreferences.photoUri = photoUri
        _user.value = _user.value?.copy(avatarId = avatarId)
    }

    fun clearError() { _error.value = null }

    fun isSignedIn() = repo.isSignedIn()

    fun signOut() {
        repo.signOut()
        _user.value = null
        _currentChallenge.value = null
        _challengeHistory.value = emptyList()
        _winHistory.value = emptyList()
    }
}

sealed class MatchUiState {
    object Idle : MatchUiState()
    object Searching : MatchUiState()
    data class Found(val matchId: String, val opponent: Opponent) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

sealed class BattleUiState {
    object Waiting : BattleUiState()
    data class Selecting(val matchId: String, val opponent: Opponent, val roomId: String) : BattleUiState()
    data class WaitingResult(
        val matchId: String,
        val opponent: Opponent,
        val myChoice: RpsChoice,
        val commitHash: String = ""
    ) : BattleUiState()
    data class Result(
        val matchId: String,
        val opponent: Opponent,
        val myChoice: RpsChoice,
        val opponentChoice: RpsChoice,
        val result: MatchResult,
        val newStreak: Int,
        val targetStreak: Int,
        val roomId: String,
        val commitHash: String = "",
        val revealedSeed: String = ""
    ) : BattleUiState()
}

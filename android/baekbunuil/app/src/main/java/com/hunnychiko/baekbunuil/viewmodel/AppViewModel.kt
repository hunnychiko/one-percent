package com.hunnychiko.baekbunuil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hunnychiko.baekbunuil.data.model.*
import com.hunnychiko.baekbunuil.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    // Ranking
    private val _rankings = MutableStateFlow<List<RankingEntry>>(emptyList())
    val rankings: StateFlow<List<RankingEntry>> = _rankings

    private val _isRankingLoading = MutableStateFlow(false)
    val isRankingLoading: StateFlow<Boolean> = _isRankingLoading

    // History
    private val _challengeHistory = MutableStateFlow<List<ChallengeHistoryItem>>(emptyList())
    val challengeHistory: StateFlow<List<ChallengeHistoryItem>> = _challengeHistory

    private val _winHistory = MutableStateFlow<List<WinHistoryItem>>(emptyList())
    val winHistory: StateFlow<List<WinHistoryItem>> = _winHistory

    init {
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
                _user.value = user
                loadProducts()
                loadTodayAdCount(uid)
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
                _user.value = user
                loadProducts()
                loadTodayAdCount(uid)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Google 로그인 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                repo.observeUser(repo.currentUserId).collect { _user.value = it }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                repo.observeProductRooms().collect { _products.value = it }
            } catch (e: Exception) {
                _products.value = sampleProducts
            }
        }
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
        viewModelScope.launch {
            _matchState.value = MatchUiState.Searching
            try {
                val matchId = repo.enterMatchQueue(repo.currentUserId, roomId)
                // 실제 환경에서는 Firebase로 상대방을 기다림
                // MVP에서는 5초 후 가상 상대와 매칭
                delay(3000)
                val opponent = Opponent(
                    userId = "bot_${System.currentTimeMillis()}",
                    nickname = listOf("우주탐험가", "별빛기사", "도전왕", "연승마스터").random(),
                    currentStreak = (0..5).random(),
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
            _battleState.value = BattleUiState.WaitingResult(currentState.matchId, currentState.opponent, choice)
            delay(1000)
            val opponentChoice = RpsChoice.entries.random()
            val result = determineWinner(choice, opponentChoice)
            val newStreak = if (result == MatchResult.WIN) {
                (_currentChallenge.value?.currentStreak ?: 0) + 1
            } else 0
            _battleState.value = BattleUiState.Result(
                matchId = currentState.matchId,
                opponent = currentState.opponent,
                myChoice = choice,
                opponentChoice = opponentChoice,
                result = result,
                newStreak = newStreak,
                targetStreak = _currentChallenge.value?.targetStreak ?: 3,
                roomId = roomId
            )
            if (result == MatchResult.WIN) {
                _currentChallenge.value = _currentChallenge.value?.copy(currentStreak = newStreak)
                    ?: Challenge(currentStreak = newStreak)
                _user.value = _user.value?.let { u ->
                    u.copy(
                        ticketCount = (u.ticketCount - 1).coerceAtLeast(0),
                        bestStreak = maxOf(u.bestStreak, newStreak)
                    )
                }
            } else {
                _currentChallenge.value = _currentChallenge.value?.copy(currentStreak = 0)
                _user.value = _user.value?.copy(ticketCount = (_user.value?.ticketCount ?: 1) - 1)
            }
        }
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
    data class WaitingResult(val matchId: String, val opponent: Opponent, val myChoice: RpsChoice) : BattleUiState()
    data class Result(
        val matchId: String,
        val opponent: Opponent,
        val myChoice: RpsChoice,
        val opponentChoice: RpsChoice,
        val result: MatchResult,
        val newStreak: Int,
        val targetStreak: Int,
        val roomId: String
    ) : BattleUiState()
}

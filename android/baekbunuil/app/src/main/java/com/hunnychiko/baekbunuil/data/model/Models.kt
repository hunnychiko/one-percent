package com.hunnychiko.baekbunuil.data.model

data class User(
    val userId: String = "",
    val nickname: String = "",
    val deviceIdHash: String = "",
    val adConsent: Boolean = false,
    val ticketCount: Int = 0,
    val bestStreak: Int = 0,
    val totalWins: Int = 0,
    val status: String = "active",
    val avatarId: Int = 0,
    val lastDailyBonus: String = ""
)

data class DailyBonusConfig(
    val enabled: Boolean = true,
    val dday: String = "",
    val ddayLabel: String = "",
    val rewardTickets: Int = 1
)

data class ProductRoom(
    val roomId: String = "",
    val productName: String = "",
    val imageUrl: String = "",
    val requiredStreak: Int = 3,
    val capacity: Int = 100,
    val currentCount: Int = 0,
    val drawStatus: String = "open",
    val grade: String = "C",
    val description: String = "",
    val round: Int = 1,
    // "coupon" = 쿠폰/제휴링크, "physical" = 실물 배송, "premium" = 고가 실물 (검증 필요)
    val productType: String = "coupon",
    // 도전 포기 시 직접 획득 가격 표시 (예: "₩9,900" / "티켓 30개" / "" = 비활성)
    val directBuyLabel: String = "",
    // "timestamp" = 기본 타임스탬프 추첨, "lotto" = 동행복권 API 결과 연동 (SS급)
    val drawMethod: String = "timestamp"
)

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val roomId: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false
)

data class Challenge(
    val challengeId: String = "",
    val userId: String = "",
    val roomId: String = "",
    val currentStreak: Int = 0,
    val targetStreak: Int = 3,
    val state: String = "active"
)

enum class RpsChoice(val emoji: String, val label: String) {
    SCISSORS("✌️", "가위"),
    ROCK("✊", "바위"),
    PAPER("🖐", "보")
}

fun determineWinner(mine: RpsChoice, opponent: RpsChoice): MatchResult {
    if (mine == opponent) return MatchResult.DRAW
    return when (mine) {
        RpsChoice.SCISSORS -> if (opponent == RpsChoice.PAPER) MatchResult.WIN else MatchResult.LOSE
        RpsChoice.ROCK -> if (opponent == RpsChoice.SCISSORS) MatchResult.WIN else MatchResult.LOSE
        RpsChoice.PAPER -> if (opponent == RpsChoice.ROCK) MatchResult.WIN else MatchResult.LOSE
    }
}

enum class MatchResult { WIN, LOSE, DRAW }

data class Match(
    val matchId: String = "",
    val userA: String = "",
    val userB: String = "",
    val choiceA: String = "",
    val choiceB: String = "",
    val result: String = "",
    val createdAt: Long = 0L
)

data class Opponent(
    val userId: String = "",
    val nickname: String = "",
    val currentStreak: Int = 0,
    val avatarIndex: Int = 0
)

data class AdRewardLog(
    val logId: String = "",
    val userId: String = "",
    val adUnitId: String = "",
    val rewardStatus: String = "",
    val serverVerifiedAt: Long = 0L,
    val todayCount: Int = 0,
    val maxDailyCount: Int = 10
)

data class DrawEntry(
    val entryId: String = "",
    val roomId: String = "",
    val userId: String = "",
    val enteredAt: Long = 0L
)

data class DrawResult(
    val drawId: String = "",
    val roomId: String = "",
    val winnerUserId: String = "",
    val winnerNickname: String = "",
    val seedHash: String = "",
    val drawnAt: Long = 0L,
    val isWinner: Boolean = false
)

// 당첨 수령 처리
data class WinnerClaim(
    val claimId: String = "",
    val drawId: String = "",
    val roomId: String = "",
    val userId: String = "",
    val productName: String = "",
    // "coupon" | "physical" | "premium"
    val productType: String = "coupon",
    // unclaimed / coupon_issued / address_pending / address_submitted / verifying / verified / shipped / delivered / rejected
    val status: String = "unclaimed",
    // 쿠폰/제휴형
    val couponCode: String = "",
    val affiliateUrl: String = "",
    // 실물 배송
    val shippingName: String = "",
    val shippingPhone: String = "",
    val shippingPostcode: String = "",
    val shippingAddress: String = "",
    val shippingDetail: String = "",
    val trackingNumber: String = "",
    val trackingCarrier: String = "",
    // 고가 검증
    val verificationStatus: String = "none",   // none / pending / passed / failed
    val verificationNote: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class ProductGrade(val label: String, val minStreak: Int, val maxStreak: Int, val color: Long) {
    C("데일리찬스", 3, 3, 0xFF4CAF50),
    B("위클리찬스", 5, 5, 0xFF2196F3),
    A("프라임찬스", 7, 10, 0xFF9C27B0),
    S("스페셜찬스", 10, 15, 0xFFFF9800),
    SS("프리미엄찬스", 15, 20, 0xFFE91E63)
}

fun gradeFromStreak(streak: Int): ProductGrade = when {
    streak <= 3 -> ProductGrade.C
    streak <= 5 -> ProductGrade.B
    streak <= 10 -> ProductGrade.A
    streak <= 15 -> ProductGrade.S
    else -> ProductGrade.SS
}

data class AffiliateBanner(
    val bannerId: String = "",
    val companyName: String = "",
    val imageUrl: String = "",
    val ticketReward: Int = 10,
    val isActive: Boolean = true,
    val order: Int = 0,
    val createdAt: Long = 0L
)

data class InviteCode(
    val code: String = "",
    val ownerUserId: String = "",
    val usedCount: Int = 0,
    val rewardPerInvite: Int = 3,
    val createdAt: Long = 0L
)

val sampleProducts = listOf(
    ProductRoom("room_1", "아이스크림 쿠폰", "", 3, 100, 42, "open", "C", "편의점 아이스크림 교환권", 1, "coupon", "₩1,500"),
    ProductRoom("room_2", "커피 쿠폰", "", 3, 100, 71, "open", "C", "스타벅스 아메리카노", 1, "coupon", "₩4,500"),
    ProductRoom("room_3", "치킨 1마리", "", 5, 100, 28, "open", "B", "BBQ 황금올리브 치킨", 1, "physical", "₩19,900"),
    ProductRoom("room_4", "영화 관람권 2매", "", 5, 100, 55, "open", "B", "CGV 주말 영화 2매", 1, "coupon", "₩22,000"),
    ProductRoom("room_5", "무선 이어폰", "", 7, 100, 19, "open", "A", "Galaxy Buds2 Pro", 1, "physical", "₩89,000"),
    ProductRoom("room_6", "로봇청소기 특가", "", 10, 100, 37, "open", "S", "Roborock S8 Pro Ultra", 1, "premium", "₩590,000"),
    ProductRoom("room_7", "에어팟 프로 2세대", "", 10, 100, 8, "open", "S", "Apple AirPods Pro 2", 1, "premium", "₩289,000"),
    ProductRoom("room_8", "프리미엄 공기청정기", "", 15, 100, 3, "open", "SS", "다이슨 공기청정기 한정 특가", 1, "premium", "₩890,000"),
)

package com.hunnychiko.baekbunuil.ui.screens.ticket

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hunnychiko.baekbunuil.R
import com.hunnychiko.baekbunuil.data.model.sampleProducts
import com.hunnychiko.baekbunuil.ui.components.StreakStars
import com.hunnychiko.baekbunuil.ui.components.TicketBadge
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import kotlinx.coroutines.delay

// 개발 테스트용 AdMob 보상형 광고 ID — 출시 전 실제 ID로 교체
private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
private const val MAX_DAILY_ADS = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    roomId: String,
    viewModel: AppViewModel,
    onAdComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val products by viewModel.products.collectAsState()
    val user by viewModel.user.collectAsState()
    val todayAdCount by viewModel.todayAdCount.collectAsState()
    val challenge by viewModel.currentChallenge.collectAsState()

    val product = products.find { it.roomId == roomId } ?: sampleProducts.first()
    val currentStreak = challenge?.currentStreak ?: 0

    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var isAdLoading by remember { mutableStateOf(false) }
    var showMaxAdMessage by remember { mutableStateOf(false) }
    var adConsent by remember { mutableStateOf(user?.adConsent ?: false) }
    var showTicketEarnedAnim by remember { mutableStateOf(false) }

    LaunchedEffect(showTicketEarnedAnim) {
        if (showTicketEarnedAnim) {
            delay(1800)
            showTicketEarnedAnim = false
        }
    }

    // 광고 로드
    LaunchedEffect(Unit) {
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isAdLoading = false
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                isAdLoading = false
            }
        })
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("승부권 충전소", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 현재 도전 상황 카드
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("선택한 상품 & 진행 상황", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(product.productName, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "목표 ${product.requiredStreak}연승",
                                style = MaterialTheme.typography.bodySmall.copy(color = Primary)
                            )
                        }
                        Text("🎯", fontSize = 32.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    StreakStars(current = currentStreak, target = product.requiredStreak)
                    Text(
                        text = "$currentStreak / ${product.requiredStreak} 연승 진행 중",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 보유 승부권
            TicketBadge(count = user?.ticketCount ?: 0, modifier = Modifier.fillMaxWidth())

            // 광고 시청 카드
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Secondary.copy(alpha = 0.2f)
                        ) {
                            Text("▶", modifier = Modifier.padding(12.dp), fontSize = 24.sp)
                        }
                        Column {
                            Text("광고 시청", style = MaterialTheme.typography.titleMedium)
                            Text("완료 시 승부권 +1", style = MaterialTheme.typography.bodySmall.copy(color = Primary))
                        }
                    }

                    Button(
                        onClick = {
                            if (todayAdCount >= MAX_DAILY_ADS) {
                                showMaxAdMessage = true
                                return@Button
                            }
                            val ad = rewardedAd
                            if (ad != null && activity != null) {
                                ad.show(activity) { _ ->
                                    viewModel.claimAdReward(
                                        onSuccess = {
                                            showTicketEarnedAnim = true
                                            onAdComplete()
                                        },
                                        onFail = {}
                                    )
                                }
                                rewardedAd = null
                            } else {
                                // 테스트 환경: 광고 없이 보상 지급
                                viewModel.claimAdReward(
                                    onSuccess = {
                                        showTicketEarnedAnim = true
                                        onAdComplete()
                                    },
                                    onFail = {}
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (todayAdCount >= MAX_DAILY_ADS) SurfaceVariant else Primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = todayAdCount < MAX_DAILY_ADS
                    ) {
                        if (isAdLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                        } else {
                            Text(
                                "▶ 광고 보고 승부권 1장 받기",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (showMaxAdMessage) {
                        Text(
                            "오늘 최대 광고 보상 횟수(10회)를 달성했습니다.\n내일 다시 이용하세요.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Warning),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 오늘 보상 현황
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("오늘 광고 보상", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$todayAdCount / $MAX_DAILY_ADS",
                            style = MaterialTheme.typography.titleMedium.copy(color = Primary)
                        )
                    }
                    LinearProgressIndicator(
                        progress = { todayAdCount / MAX_DAILY_ADS.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Primary,
                        trackColor = SurfaceVariant
                    )
                    Text(
                        "하루 최대 10회 보상 가능",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 맞춤형 광고 설정
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("개인화 광고 허용", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "관심사 기반 광고로 더 관련성 높은 광고가 표시될 수 있어요",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = adConsent,
                        onCheckedChange = { adConsent = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = Primary)
                    )
                }
            }

            // 신뢰/안전 요소
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrustBadge("🛡️", "중복 시청 방지")
                TrustBadge("✅", "서버 검증")
                TrustBadge("🔒", "안전한 보상 지급")
            }
        }

        // 승부권 획득 애니메이션 오버레이
        if (showTicketEarnedAnim) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.anim_ticket_earned),
                        contentDescription = "승부권 획득",
                        modifier = Modifier.size(160.dp).clip(RoundedCornerShape(20.dp))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "승부권 +1 획득!",
                        style = MaterialTheme.typography.headlineMedium.copy(color = Primary, fontWeight = FontWeight.Black)
                    )
                }
            }
        }
        } // Box
    }
}

@Composable
private fun TrustBadge(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

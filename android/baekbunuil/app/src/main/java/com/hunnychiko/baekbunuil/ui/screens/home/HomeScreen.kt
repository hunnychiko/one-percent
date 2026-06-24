package com.hunnychiko.baekbunuil.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.data.model.DailyBonusConfig
import com.hunnychiko.baekbunuil.data.model.ProductGrade
import com.hunnychiko.baekbunuil.data.model.gradeFromStreak
import com.hunnychiko.baekbunuil.ui.components.*
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import kotlinx.coroutines.delay

// MainScreen 탭 0에서 직접 사용하는 콘텐츠 (Scaffold/BottomBar 없음)
@Composable
fun HomeContent(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnread: Boolean = false
) {
    val user by viewModel.user.collectAsState()
    val products by viewModel.products.collectAsState()
    val dailyBonusAvailable by viewModel.dailyBonusAvailable.collectAsState()
    val dailyBonusConfig by viewModel.dailyBonusConfig.collectAsState()
    var bonusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.checkDailyBonus() }

    val grouped = products.groupBy { gradeFromStreak(it.requiredStreak) }
    val hero = products.maxByOrNull { it.currentCount }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                TopBar(
                    ticketCount = user?.ticketCount ?: 0,
                    bestStreak = user?.bestStreak ?: 0,
                    hasUnread = hasUnread,
                    onNotificationClick = onNotificationClick
                )
            }

            if (dailyBonusAvailable) {
                item {
                    DailyBonusBanner(
                        config = dailyBonusConfig,
                        onClaim = {
                            viewModel.claimDailyBonus { reward ->
                                bonusMessage = "🎁 데일리 보너스 승부권 ${reward}장 획득!"
                            }
                        }
                    )
                }
            }

            if ((user?.bestStreak ?: 0) > 0) {
                item { CurrentChallengeBanner(streak = user?.bestStreak ?: 0) }
            }

            hero?.let { h ->
                item {
                    SectionHeader(title = "오늘의 히어로")
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        HeroProductCard(
                            product = h,
                            myCurrentStreak = 0,
                            onClick = { onProductClick(h.roomId) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            val sections = listOf(
                ProductGrade.C  to "데일리찬스",
                ProductGrade.B  to "위클리찬스",
                ProductGrade.A  to "프라임찬스",
                ProductGrade.S  to "스페셜찬스",
                ProductGrade.SS to "프리미엄찬스"
            )
            if (products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = "준비 중인 상품이 없습니다",
                            color = androidx.compose.ui.graphics.Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            sections.forEach { (grade, label) ->
                val sectionProducts = grouped[grade] ?: return@forEach
                val streakDesc = if (grade.minStreak == grade.maxStreak) "${grade.minStreak}연승" else "${grade.minStreak}~${grade.maxStreak}연승"
                item {
                    SectionHeader(title = label, subtitle = "목표 $streakDesc 달성 후 1/100 추첨 참여")
                }
                if (sectionProducts.size == 1) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            ProductCard(
                                product = sectionProducts[0],
                                onClick = { onProductClick(sectionProducts[0].roomId) }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sectionProducts) { product ->
                                Box(modifier = Modifier.width(280.dp)) {
                                    ProductCard(
                                        product = product,
                                        onClick = { onProductClick(product.roomId) }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        bonusMessage?.let { msg ->
            LaunchedEffect(msg) {
                delay(3000)
                bonusMessage = null
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 20.dp, end = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Success.copy(alpha = 0.95f)
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyBonusBanner(config: DailyBonusConfig?, onClaim: () -> Unit) {
    val isDday = !config?.dday.isNullOrEmpty()
    val label = if (isDday && !config?.ddayLabel.isNullOrEmpty()) config!!.ddayLabel else "데일리 보너스"
    val reward = config?.rewardTickets ?: 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isDday) Gold.copy(alpha = 0.15f) else Primary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, if (isDday) Gold.copy(alpha = 0.5f) else Primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.CardGiftcard,
                contentDescription = null,
                tint = if (isDday) Gold else Primary,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (isDday) Gold else Primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    "승부권 ${reward}장 무료 지급",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            Button(
                onClick = onClaim,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDday) Gold else Primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("받기", style = MaterialTheme.typography.labelLarge.copy(color = if (isDday) Background else TextPrimary))
            }
        }
    }
}

@Composable
fun CurrentChallengeBanner(streak: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(7.dp),
        color = Primary.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("👑", fontSize = 24.sp)
            Column {
                Text(
                    text = "최고 연승 ${streak}회",
                    style = MaterialTheme.typography.titleMedium.copy(color = Primary)
                )
                Text(
                    text = "계속 도전해서 더 높은 연승을 달성하세요!",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

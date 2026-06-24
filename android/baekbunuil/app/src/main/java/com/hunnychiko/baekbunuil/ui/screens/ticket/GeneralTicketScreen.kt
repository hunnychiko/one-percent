package com.hunnychiko.baekbunuil.ui.screens.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.data.model.ProductRoom
import com.hunnychiko.baekbunuil.ui.components.GradeBadge
import com.hunnychiko.baekbunuil.ui.components.ParticipantBar
import com.hunnychiko.baekbunuil.ui.components.TopBar
import com.hunnychiko.baekbunuil.ui.components.productEmoji
import com.hunnychiko.baekbunuil.ui.screens.affiliate.AffiliateBannerSection
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@Composable
fun GeneralTicketScreen(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnread: Boolean = false
) {
    val user     by viewModel.user.collectAsState()
    val products by viewModel.products.collectAsState()
    val todayAds by viewModel.todayAdCount.collectAsState()

    val maxDailyAds = 10
    val remaining   = maxDailyAds - todayAds

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TopBar(ticketCount = user?.ticketCount ?: 0, bestStreak = user?.bestStreak ?: 0, hasUnread = hasUnread, onNotificationClick = onNotificationClick)
        }

        item {
            Text(
                "충전소",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        // 티켓 현황 카드
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                color = CardBackground
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.horizontalGradient(
                            listOf(Primary.copy(alpha = 0.25f), Secondary.copy(alpha = 0.15f))
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("보유 승부권", style = MaterialTheme.typography.titleSmall.copy(color = TextSecondary))
                                Text(
                                    "${user?.ticketCount ?: 0}개",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        color = Primary,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                            Surface(shape = CircleShape, color = Primary.copy(alpha = 0.15f), modifier = Modifier.size(64.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🎟️", fontSize = 28.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        // 오늘 광고 시청 현황
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "오늘 광고 시청: $todayAds/$maxDailyAds",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Text(
                                "남은 횟수: ${remaining}회",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (remaining > 0) Primary else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { todayAds.toFloat() / maxDailyAds },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Primary,
                            trackColor = CardBackgroundLight
                        )
                    }
                }
            }
        }

        // 제휴 배너
        item {
            AffiliateBannerSection(viewModel)
        }

        // 광고 시청 안내
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = CardBackgroundLight
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("광고 보고 승부권 받기", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "상품을 선택한 뒤 광고를 시청하면\n승부권 1개를 받을 수 있어요",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }
        }

        // 사용 방법
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text("사용 방법", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                listOf(
                    "원하는 상품을 선택하세요" to "📦",
                    "광고를 시청하면 승부권 1개 획득" to "📺",
                    "승부권으로 가위바위보 대결 참여" to "✊",
                    "목표 연승 달성 시 추첨방 입장" to "🎯",
                ).forEach { (text, emoji) ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(emoji, fontSize = 20.sp, modifier = Modifier.size(32.dp).wrapContentSize())
                        Text(text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // 상품 목록 헤더
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("도전 가능한 상품", style = MaterialTheme.typography.titleLarge)
                Text(
                    "상품을 탭해서 광고 시청",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // 상품 카드 목록
        items(products.filter { it.drawStatus == "open" }) { product ->
            TicketProductRow(
                product = product,
                onClick = { onProductClick(product.roomId) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = DividerColor
            )
        }

        if (products.none { it.drawStatus == "open" }) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "현재 진행 중인 상품이 없습니다\n잠시 후 다시 확인해 주세요",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketProductRow(product: ProductRoom, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji / image
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = CardBackgroundLight,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(productEmoji(product.productName), fontSize = 24.sp)
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val (gradeLabel, gradeColor) = gradeInfo(product.grade)
                GradeBadge(label = gradeLabel, color = gradeColor)
                Text(
                    product.productName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${product.requiredStreak}연승 필요",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(6.dp))
            ParticipantBar(
                current     = product.currentCount,
                total       = product.capacity,
                fillPercent = (product.currentCount.toFloat() / product.capacity).coerceIn(0f, 1f)
            )
        }

        Spacer(Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(5.dp),
            color = Primary.copy(alpha = 0.15f)
        ) {
            Text(
                "광고\n보기",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

private fun gradeInfo(grade: String): Pair<String, Color> = when (grade) {
    "C"  -> "C급"  to GradeC
    "B"  -> "B급"  to GradeB
    "A"  -> "A급"  to GradeA
    "S"  -> "S급"  to GradeS
    "SS" -> "SS급" to GradeSS
    else -> grade  to Color.Gray
}

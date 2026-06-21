package com.hunnychiko.baekbunuil.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.ui.components.*
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

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

    val grouped = products.groupBy { it.requiredStreak }
    val hero = products.maxByOrNull { it.currentCount }

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

        if ((user?.bestStreak ?: 0) > 0) {
            item { CurrentChallengeBanner(streak = user?.bestStreak ?: 0) }
        }

        hero?.let { h ->
            item {
                SectionHeader(title = "오늘의 히어로 🔥")
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
            3 to "3연승 🌱",
            5 to "5연승 🔥",
            7 to "7연승 ⚡",
            10 to "10연승 💎",
            15 to "15연승 👑"
        )
        sections.forEach { (streak, label) ->
            val sectionProducts = grouped[streak] ?: return@forEach
            item {
                SectionHeader(title = label, subtitle = "목표 ${streak}연승 달성 후 1/100 추첨 참여")
            }
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

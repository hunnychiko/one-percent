package com.hunnychiko.baekbunuil.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.data.model.ProductRoom
import com.hunnychiko.baekbunuil.ui.components.*
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit,
    onMyPageClick: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val products by viewModel.products.collectAsState()

    val grouped = products.groupBy { it.requiredStreak }
    val hero = products.maxByOrNull { it.currentCount }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(selected = 0, onMyPageClick = onMyPageClick)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                TopBar(
                    ticketCount = user?.ticketCount ?: 0,
                    bestStreak = user?.bestStreak ?: 0
                )
            }

            // 현재 도전 배너
            if ((user?.bestStreak ?: 0) > 0) {
                item {
                    CurrentChallengeBanner(streak = user?.bestStreak ?: 0)
                }
            }

            // 히어로 상품
            hero?.let { h ->
                item {
                    SectionHeader(title = "오늘의 히어로 🔥")
                    HeroProductCard(
                        product = h,
                        myCurrentStreak = 0,
                        onClick = { onProductClick(h.roomId) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // 연승별 섹션
            val sections = listOf(3 to "3연승 🌱", 5 to "5연승 🔥", 7 to "7연승 ⚡", 10 to "10연승 💎", 15 to "15연승 👑")
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
}

@Composable
fun CurrentChallengeBanner(streak: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
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

@Composable
fun BottomNavBar(selected: Int, onMyPageClick: () -> Unit) {
    NavigationBar(
        containerColor = CardBackground,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple(Icons.Default.Home, "홈", 0),
            Triple(Icons.Default.EmojiEvents, "랭킹", 1),
            Triple(Icons.Default.PlayCircle, "충전소", 2),
            Triple(Icons.Default.History, "내기록", 3),
            Triple(Icons.Default.Person, "마이", 4)
        )
        items.forEach { (icon, label, index) ->
            NavigationBarItem(
                selected = selected == index,
                onClick = {
                    if (index == 4) onMyPageClick()
                },
                icon = {
                    Icon(icon, contentDescription = label,
                        tint = if (selected == index) Primary else TextSecondary)
                },
                label = {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected == index) Primary else TextSecondary
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    indicatorColor = Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

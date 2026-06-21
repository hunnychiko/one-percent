package com.hunnychiko.baekbunuil.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.ui.components.StreakStars
import com.hunnychiko.baekbunuil.ui.components.TopBar
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import com.hunnychiko.baekbunuil.viewmodel.ChallengeHistoryItem
import com.hunnychiko.baekbunuil.viewmodel.WinHistoryItem

@Composable
fun HistoryScreen(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnread: Boolean = false
) {
    val user by viewModel.user.collectAsState()
    val challengeHistory by viewModel.challengeHistory.collectAsState()
    val winHistory by viewModel.winHistory.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TopBar(ticketCount = user?.ticketCount ?: 0, bestStreak = user?.bestStreak ?: 0, hasUnread = hasUnread, onNotificationClick = onNotificationClick)
        }
        item {
            Text(
                "내 기록",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        // 요약 카드
        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⭐",
                    value = "${user?.bestStreak ?: 0}",
                    label = "최고 연승"
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🏆",
                    value = "${winHistory.size}",
                    label = "당첨"
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🎯",
                    value = "${challengeHistory.size}",
                    label = "도전 횟수"
                )
            }
        }

        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Background,
                contentColor = Primary,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp)
                            .background(Primary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    )
                }
            ) {
                listOf("도전 기록", "당첨 기록").forEachIndexed { i, label ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = {
                            Text(
                                label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (selectedTab == i) Primary else TextSecondary
                                )
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (selectedTab == 0) {
            // 도전 기록
            if (challengeHistory.isEmpty()) {
                item { EmptyHistoryState(message = "아직 도전 기록이 없어요.\n상품을 선택하고 첫 대결에 참여해보세요!") }
            } else {
                items(challengeHistory) { item ->
                    ChallengeHistoryCard(
                        item = item,
                        onClick = { onProductClick(item.roomId) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = DividerColor)
                }
            }
        } else {
            // 당첨 기록
            if (winHistory.isEmpty()) {
                item { EmptyHistoryState(message = "아직 당첨 기록이 없어요.\n목표 연승을 달성해 1/100 추첨에 참여하세요!") }
            } else {
                items(winHistory) { item ->
                    WinHistoryCard(item = item)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = DividerColor)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier = Modifier, emoji: String, value: String, label: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(7.dp), color = CardBackground) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = Primary, fontWeight = FontWeight.Black))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ChallengeHistoryCard(item: ChallengeHistoryItem, onClick: () -> Unit) {
    val stateColor = when (item.state) {
        "completed" -> Success
        "active" -> Primary
        else -> TextSecondary
    }
    val stateLabel = when (item.state) {
        "completed" -> "참여 완료"
        "active" -> "진행 중"
        else -> "종료"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.emoji, fontSize = 32.sp, modifier = Modifier.size(48.dp).wrapContentSize())
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            StreakStars(current = item.currentStreak, target = item.targetStreak)
            Text(
                "${item.currentStreak} / ${item.targetStreak} 연승",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Surface(shape = RoundedCornerShape(4.dp), color = stateColor.copy(alpha = 0.15f)) {
                Text(
                    stateLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(color = stateColor, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(item.timeAgo, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun WinHistoryCard(item: WinHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🏆", fontSize = 24.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.titleMedium)
            Text(
                "${item.round}차 추첨 당첨",
                style = MaterialTheme.typography.bodySmall.copy(color = Gold)
            )
            Text(item.wonAt, style = MaterialTheme.typography.labelSmall)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
private fun EmptyHistoryState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary, textAlign = TextAlign.Center)
            )
        }
    }
}

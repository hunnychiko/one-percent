package com.hunnychiko.baekbunuil.ui.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.hunnychiko.baekbunuil.ui.components.SectionHeader
import com.hunnychiko.baekbunuil.ui.components.TopBar
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import com.hunnychiko.baekbunuil.viewmodel.RankingEntry

@Composable
fun RankingScreen(
    viewModel: AppViewModel,
    onNotificationClick: () -> Unit = {},
    hasUnread: Boolean = false
) {
    val user by viewModel.user.collectAsState()
    val rankings by viewModel.rankings.collectAsState()
    val isLoading by viewModel.isRankingLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedWinEntry by remember { mutableStateOf<RankingEntry?>(null) }

    LaunchedEffect(Unit) { viewModel.loadRankings() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TopBar(ticketCount = user?.ticketCount ?: 0, bestStreak = user?.bestStreak ?: 0, hasUnread = hasUnread, onNotificationClick = onNotificationClick)
        }

        item {
            Text(
                "랭킹",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        // 내 순위 카드
        item {
            val myRank = rankings.indexOfFirst { it.userId == (user?.userId ?: "") }
            MyRankCard(
                rank = if (myRank >= 0) myRank + 1 else null,
                nickname = user?.nickname ?: "나",
                bestStreak = user?.bestStreak ?: 0,
                totalWins = user?.totalWins ?: 0
            )
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
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(Primary)
                    )
                }
            ) {
                listOf("연승 랭킹", "당첨 랭킹").forEachIndexed { i, label ->
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

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        } else if (rankings.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "아직 랭킹 데이터가 없어요.\n첫 번째 도전자가 되어보세요!",
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary, textAlign = TextAlign.Center)
                    )
                }
            }
        } else {
            // 상위 3명 포디움
            if (rankings.size >= 3) {
                item {
                    PodiumSection(
                        first = rankings[0],
                        second = rankings.getOrNull(1),
                        third = rankings.getOrNull(2)
                    )
                }
            }

            // 4위 이하
            itemsIndexed(rankings.drop(3)) { index, entry ->
                RankingRow(
                    rank = index + 4,
                    entry = entry,
                    isMe = entry.userId == (user?.userId ?: ""),
                    showStreak = selectedTab == 0,
                    onWinClick = { selectedWinEntry = it }
                )
                if (index < rankings.size - 4) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = DividerColor
                    )
                }
            }
        }
    }

    selectedWinEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedWinEntry = null },
            title = { Text("${entry.nickname}님 당첨 내역") },
            text = {
                if (entry.totalWins == 0) {
                    Text("아직 당첨 기록이 없습니다.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("총 ${entry.totalWins}회 당첨", style = MaterialTheme.typography.bodyMedium.copy(color = Primary))
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedWinEntry = null }) {
                    Text("닫기")
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun MyRankCard(rank: Int?, nickname: String, bestStreak: Int, totalWins: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(listOf(Primary.copy(alpha = 0.2f), Color.Transparent))
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = Primary.copy(alpha = 0.2f), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("👤", fontSize = 22.sp) }
                    }
                    Column {
                        Text("나", style = MaterialTheme.typography.bodySmall.copy(color = Primary))
                        Text(nickname, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${rank ?: "-"}", style = MaterialTheme.typography.headlineMedium.copy(color = Gold, fontWeight = FontWeight.Black))
                        Text("내 순위", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$bestStreak", style = MaterialTheme.typography.headlineMedium.copy(color = Primary, fontWeight = FontWeight.Black))
                        Text("최고 연승", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(
    first: RankingEntry,
    second: RankingEntry?,
    third: RankingEntry?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2위
            second?.let {
                PodiumItem(entry = it, rank = 2, height = 100.dp, color = Color(0xFFC0C0C0))
            }
            // 1위
            PodiumItem(entry = first, rank = 1, height = 140.dp, color = Gold)
            // 3위
            third?.let {
                PodiumItem(entry = it, rank = 3, height = 70.dp, color = Color(0xFFCD7F32))
            }
        }
    }
}

@Composable
private fun PodiumItem(entry: RankingEntry, rank: Int, height: androidx.compose.ui.unit.Dp, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(if (rank == 1) "👑" else "🏅", fontSize = if (rank == 1) 28.sp else 22.sp)
        Spacer(Modifier.height(4.dp))
        Surface(shape = CircleShape, color = color.copy(alpha = 0.2f), modifier = Modifier.size(48.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("👤", fontSize = 20.sp) }
        }
        Spacer(Modifier.height(4.dp))
        Text(entry.nickname.take(6), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, maxLines = 1)
        Text("${entry.bestStreak}연승", style = MaterialTheme.typography.bodySmall.copy(color = color))
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$rank",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = color,
                    fontWeight = FontWeight.Black
                )
            )
        }
    }
}

@Composable
private fun RankingRow(rank: Int, entry: RankingEntry, isMe: Boolean, showStreak: Boolean, onWinClick: (RankingEntry) -> Unit = {}) {
    Surface(
        color = if (isMe) Primary.copy(alpha = 0.08f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$rank",
                modifier = Modifier.width(32.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (rank <= 10) Primary else TextSecondary,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.width(12.dp))
            Surface(shape = CircleShape, color = CardBackgroundLight, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("👤", fontSize = 16.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(entry.nickname, style = MaterialTheme.typography.titleMedium)
                    if (isMe) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Primary.copy(alpha = 0.2f)) {
                            Text("나", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Primary))
                        }
                    }
                }
                Text(
                    "당첨 ${entry.totalWins}회",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable { onWinClick(entry) }
                )
            }
            Text(
                if (showStreak) "${entry.bestStreak}연승" else "${entry.totalWins}당첨",
                style = MaterialTheme.typography.titleMedium.copy(color = Primary, fontWeight = FontWeight.Bold)
            )
        }
    }
}

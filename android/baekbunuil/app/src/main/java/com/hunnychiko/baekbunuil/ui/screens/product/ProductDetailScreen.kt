package com.hunnychiko.baekbunuil.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hunnychiko.baekbunuil.data.model.ProductRoom
import com.hunnychiko.baekbunuil.data.model.gradeFromStreak
import com.hunnychiko.baekbunuil.data.model.sampleProducts
import com.hunnychiko.baekbunuil.ui.components.*
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    roomId: String,
    viewModel: AppViewModel,
    onChallenge: () -> Unit,
    onWatchAd: () -> Unit,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val challenge by viewModel.currentChallenge.collectAsState()
    val user by viewModel.user.collectAsState()
    val product = products.find { it.roomId == roomId } ?: sampleProducts.first()

    LaunchedEffect(roomId) {
        viewModel.loadChallenge(roomId)
    }

    val currentStreak = challenge?.currentStreak ?: 0
    val targetStreak = product.requiredStreak
    val ticketCount = user?.ticketCount ?: 0
    val hasTicket = ticketCount > 0
    val isParticipating = challenge != null && challenge?.state == "active"
    val hasCompleted = currentStreak >= targetStreak
    val grade = gradeFromStreak(product.requiredStreak)
    var showNoTicketDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(product.productName, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 승부권 수 표시
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🎫", fontSize = 14.sp)
                        Text(
                            "승부권 ${ticketCount}장",
                            style = MaterialTheme.typography.labelLarge.copy(color = Primary)
                        )
                    }
                }
            }

            // 상품 이미지 영역 (정사각형, 좌우 20dp 마진)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.productName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(CardBackgroundLight, Background))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = productEmoji(product), fontSize = 96.sp)
                    }
                }
                // 1/100 배지
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Primary
                ) {
                    Text(
                        "1/100",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary, fontWeight = FontWeight.Black)
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // 등급 + 이름
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GradeBadge(label = grade.label, color = androidx.compose.ui.graphics.Color(grade.color))
                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(20.dp))

                // 참여 현황 카드
                Surface(shape = RoundedCornerShape(8.dp), color = CardBackground) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("추첨방 현황", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        ParticipantBar(
                            current = product.currentCount,
                            total = product.capacity,
                            fillPercent = product.currentCount / product.capacity.toFloat()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (product.currentCount < product.capacity) "${product.capacity - product.currentCount}자리 남음" else "추첨 진행 중",
                            style = MaterialTheme.typography.bodySmall.copy(color = if (product.currentCount < product.capacity) Success else Warning)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 연승 진행도 카드
                Surface(shape = RoundedCornerShape(8.dp), color = CardBackground) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("목표 연승", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "$currentStreak / $targetStreak 연승",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = if (hasCompleted) Success else Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        StreakStars(current = currentStreak, target = targetStreak, large = true)
                        if (isParticipating && !hasCompleted) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "앞으로 ${targetStreak - currentStreak}연승 더!",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Primary)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 규칙 카드
                Surface(shape = RoundedCornerShape(8.dp), color = CardBackground) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("참여 규칙", style = MaterialTheme.typography.titleMedium)
                        RuleItem("🎫", "승부권 1장 = 가위바위보 1회 대결")
                        RuleItem("⭐", "목표 ${targetStreak}연승 달성 시 추첨 슬롯 자동 입장")
                        RuleItem("👤", "1인 1계정 1슬롯 참여")
                        RuleItem("🎯", "100명 모집 완료 시 자동 추첨 · 1명 당첨")
                        RuleItem("❌", "패배 시 해당 도전 종료 (재도전 가능)")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // CTA 버튼
                when {
                    hasCompleted -> {
                        Button(
                            onClick = { /* 추첨 결과 화면으로 */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("참여 완료 · 결과 기다리는 중 ✅", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
                        }
                    }
                    hasTicket -> {
                        Button(
                            onClick = onChallenge,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (isParticipating) "이어하기 ✊" else "도전하기 ✊",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    else -> {
                        Button(
                            onClick = { showNoTicketDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "도전하기 ✊",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNoTicketDialog) {
        AlertDialog(
            onDismissRequest = { showNoTicketDialog = false },
            title = { Text("승부권이 없습니다") },
            text = { Text("광고를 시청하면 승부권 2장을 받을 수 있습니다.\n승부권 충전소로 이동하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = { showNoTicketDialog = false; onWatchAd() }) {
                    Text("이동", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoTicketDialog = false }) {
                    Text("취소", color = TextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun RuleItem(emoji: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(emoji, fontSize = 14.sp)
        Text(text, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
    }
}

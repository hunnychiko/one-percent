package com.hunnychiko.baekbunuil.ui.screens.matching

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.R
import com.hunnychiko.baekbunuil.data.model.sampleProducts
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import com.hunnychiko.baekbunuil.viewmodel.MatchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    roomId: String,
    viewModel: AppViewModel,
    onMatchFound: () -> Unit,
    onCancel: () -> Unit
) {
    val matchState by viewModel.matchState.collectAsState()
    val products by viewModel.products.collectAsState()
    val challenge by viewModel.currentChallenge.collectAsState()
    val product = products.find { it.roomId == roomId } ?: sampleProducts.first()
    val myStreak = challenge?.currentStreak ?: 0

    var waitSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.startMatching(roomId)
    }

    LaunchedEffect(matchState) {
        if (matchState is MatchUiState.Found) {
            kotlinx.coroutines.delay(1500)
            viewModel.startBattle((matchState as MatchUiState.Found).matchId, (matchState as MatchUiState.Found).opponent, roomId)
            onMatchFound()
        }
    }

    LaunchedEffect(matchState) {
        if (matchState is MatchUiState.Searching) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                waitSeconds++
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )

    Scaffold(containerColor = Background) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                when (val state = matchState) {
                    is MatchUiState.Searching, is MatchUiState.Idle -> {
                        // 애니메이션 원
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .rotate(rotation)
                                    .background(
                                        Brush.sweepGradient(listOf(Primary.copy(alpha = 0.3f), Color.Transparent, Primary)),
                                        CircleShape
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .scale(pulse)
                                    .background(CardBackground, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✊", fontSize = 48.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (myStreak == 0) "상대 찾는 중..." else "${myStreak}연승 상대 찾는 중...",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "연승 ${myStreak}인 상대와 매칭",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Primary)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "예상 대기 시간 ${waitSeconds}초",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }

                        // 도전 상품 정보
                        Surface(shape = RoundedCornerShape(16.dp), color = CardBackground) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎯", fontSize = 28.sp)
                                Column {
                                    Text(product.productName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "목표 ${product.requiredStreak}연승 · 1/100 추첨",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Primary)
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.cancelMatching()
                                onCancel()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "취소", tint = TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("취소")
                        }
                    }

                    is MatchUiState.Found -> {
                        Image(
                            painter = painterResource(R.drawable.anim_matching_found),
                            contentDescription = "매칭 완료",
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp))
                        )
                        Text(
                            "상대를 찾았습니다!",
                            style = MaterialTheme.typography.headlineLarge.copy(color = Primary, fontWeight = FontWeight.Black)
                        )
                        Surface(shape = RoundedCornerShape(16.dp), color = CardBackground) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("VS", fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextSecondary)
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PlayerChip(nickname = "나", streak = 0, isMe = true)
                                    Text("VS", style = MaterialTheme.typography.headlineSmall.copy(color = Primary))
                                    PlayerChip(nickname = state.opponent.nickname, streak = state.opponent.currentStreak)
                                }
                            }
                        }
                        CircularProgressIndicator(color = Primary)
                        Text("대결 준비 중...", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                    }

                    is MatchUiState.Error -> {
                        Text("❌", fontSize = 48.sp)
                        Text("매칭 실패", style = MaterialTheme.typography.headlineMedium)
                        Text(state.message, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                        Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                            Text("돌아가기")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerChip(nickname: String, streak: Int, isMe: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (isMe) Primary.copy(alpha = 0.2f) else CardBackgroundLight
        ) {
            Text(
                if (isMe) "👤" else "🤖",
                fontSize = 36.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(nickname, style = MaterialTheme.typography.titleMedium)
        Text("연승 $streak", style = MaterialTheme.typography.bodySmall.copy(color = Primary))
    }
}

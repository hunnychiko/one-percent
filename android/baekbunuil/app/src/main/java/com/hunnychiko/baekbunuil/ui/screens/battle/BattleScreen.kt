package com.hunnychiko.baekbunuil.ui.screens.battle

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.R
import com.hunnychiko.baekbunuil.data.model.*
import com.hunnychiko.baekbunuil.data.model.sampleProducts
import com.hunnychiko.baekbunuil.ui.components.StreakStars
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import com.hunnychiko.baekbunuil.viewmodel.BattleUiState
import kotlinx.coroutines.delay

private const val COUNTDOWN_SECONDS = 10

@Composable
fun BattleScreen(
    roomId: String,
    viewModel: AppViewModel,
    onStreakComplete: () -> Unit,
    onContinue: () -> Unit,
    onHome: () -> Unit
) {
    val battleState by viewModel.battleState.collectAsState()
    val products by viewModel.products.collectAsState()
    val product = products.find { it.roomId == roomId } ?: sampleProducts.first()
    val challenge by viewModel.currentChallenge.collectAsState()

    var countdown by remember { mutableStateOf(COUNTDOWN_SECONDS) }
    var autoSelected by remember { mutableStateOf(false) }

    LaunchedEffect(battleState) {
        if (battleState is BattleUiState.Selecting) {
            countdown = COUNTDOWN_SECONDS
            autoSelected = false
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            if (!autoSelected && battleState is BattleUiState.Selecting) {
                autoSelected = true
                viewModel.submitChoice(RpsChoice.entries.random(), roomId)
            }
        }
    }

    LaunchedEffect(battleState) {
        val state = battleState
        if (state is BattleUiState.Result && state.result == MatchResult.WIN && state.newStreak >= state.targetStreak) {
            delay(2500)
            viewModel.enterRaffle(roomId) { onStreakComplete() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when (val state = battleState) {
            is BattleUiState.Selecting -> {
                BattleSelectingContent(
                    state = state,
                    product = product,
                    currentStreak = challenge?.currentStreak ?: 0,
                    countdown = countdown,
                    onChoiceSelected = { choice ->
                        autoSelected = true
                        viewModel.submitChoice(choice, roomId)
                    }
                )
            }
            is BattleUiState.WaitingResult -> {
                WaitingResultContent(state = state)
            }
            is BattleUiState.Result -> {
                BattleResultContent(
                    state = state,
                    product = product,
                    onContinue = {
                        viewModel.resetBattle()
                        onContinue()
                    },
                    onHome = {
                        viewModel.resetBattle()
                        onHome()
                    }
                )
            }
            else -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}

@Composable
private fun BattleSelectingContent(
    state: BattleUiState.Selecting,
    product: ProductRoom,
    currentStreak: Int,
    countdown: Int,
    onChoiceSelected: (RpsChoice) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // 상단: 프로필 VS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BattlePlayerCard(nickname = "나", streak = currentStreak, isMe = true)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VS", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Primary)
                Spacer(Modifier.height(8.dp))
                CountdownCircle(countdown = countdown)
            }

            BattlePlayerCard(
                nickname = state.opponent.nickname,
                streak = state.opponent.currentStreak,
                isMe = false,
                isSelecting = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // 도전 상품
        Surface(shape = RoundedCornerShape(12.dp), color = CardBackground) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎯", fontSize = 22.sp)
                Column {
                    Text(product.productName, style = MaterialTheme.typography.titleMedium)
                    StreakStars(current = currentStreak, target = product.requiredStreak)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("당신의 선택을 해주세요", style = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center))
        Text("두 사람이 동시에 선택해야 승부가 시작됩니다", style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center))

        Spacer(Modifier.height(24.dp))

        // 가위바위보 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RpsButton(choice = RpsChoice.SCISSORS, onClick = { onChoiceSelected(RpsChoice.SCISSORS) })
            RpsButton(choice = RpsChoice.ROCK, onClick = { onChoiceSelected(RpsChoice.ROCK) })
            RpsButton(choice = RpsChoice.PAPER, onClick = { onChoiceSelected(RpsChoice.PAPER) })
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoChip("👥", "실시간 매칭")
            Spacer(Modifier.width(12.dp))
            InfoChip("🎫", "승부권 1장 사용")
            Spacer(Modifier.width(12.dp))
            InfoChip("🔄", "동시 선택 후 공개")
        }
    }
}

@Composable
private fun WaitingResultContent(state: BattleUiState.WaitingResult) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(state.myChoice.emoji, fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("선택 완료!", style = MaterialTheme.typography.headlineMedium.copy(color = Primary))
        Text("상대방 선택 중...", style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
private fun BattleResultContent(
    state: BattleUiState.Result,
    product: ProductRoom,
    onContinue: () -> Unit,
    onHome: () -> Unit
) {
    val isWin = state.result == MatchResult.WIN
    val isDraw = state.result == MatchResult.DRAW
    val isStreakComplete = isWin && state.newStreak >= state.targetStreak

    val backgroundColor = when {
        isStreakComplete -> Brush.verticalGradient(listOf(Primary.copy(alpha = 0.3f), Background))
        isWin -> Brush.verticalGradient(listOf(Success.copy(alpha = 0.2f), Background))
        isDraw -> Brush.verticalGradient(listOf(SurfaceVariant, Background))
        else -> Brush.verticalGradient(listOf(Error.copy(alpha = 0.2f), Background))
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            // 결과 헤더
            if (isStreakComplete) {
                Image(
                    painter = painterResource(R.drawable.anim_streak_complete),
                    contentDescription = "연승 달성",
                    modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp))
                )
            } else {
                Text(
                    text = when {
                        isWin -> "✅"
                        isDraw -> "🤝"
                        else -> "❌"
                    },
                    fontSize = 64.sp
                )
            }
            Text(
                text = when {
                    isStreakComplete -> "연승 달성! 추첨 참여!"
                    isWin -> "승리!"
                    isDraw -> "무승부"
                    else -> "패배"
                },
                style = MaterialTheme.typography.displayMedium.copy(
                    color = when {
                        isStreakComplete -> Gold
                        isWin -> Success
                        isDraw -> TextSecondary
                        else -> Error
                    },
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            )

            // 선택 결과
            Surface(shape = RoundedCornerShape(16.dp), color = CardBackground) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("나", style = MaterialTheme.typography.bodySmall.copy(color = Primary))
                        Text(state.myChoice.emoji, fontSize = 48.sp)
                        Text(state.myChoice.label, style = MaterialTheme.typography.labelLarge)
                    }
                    Text("VS", style = MaterialTheme.typography.headlineMedium.copy(color = TextSecondary))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.opponent.nickname.take(6), style = MaterialTheme.typography.bodySmall)
                        Text(state.opponentChoice.emoji, fontSize = 48.sp)
                        Text(state.opponentChoice.label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // 연승 진행도
            Surface(shape = RoundedCornerShape(16.dp), color = CardBackground) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("연승 진행도", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${state.newStreak} / ${state.targetStreak}",
                            style = MaterialTheme.typography.titleMedium.copy(color = if (isWin) Success else Error)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    StreakStars(current = state.newStreak, target = state.targetStreak)
                    if (!isWin && !isDraw) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "패배로 도전이 종료됩니다. 승부권을 충전하고 재도전하세요.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Error)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // 버튼
            if (isStreakComplete) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Gold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("추첨 참여 슬롯 자동 입장 중...", style = MaterialTheme.typography.bodyLarge.copy(color = Gold))
                        Text("${product.productName} 1/100 추첨 참여 완료!", style = MaterialTheme.typography.titleMedium.copy(color = Gold))
                    }
                }
            } else if (isWin) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("▶ 광고 보고 계속 도전", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("▶ 광고 보고 재도전", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
                    }
                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant)
                    ) {
                        Text("홈으로", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BattlePlayerCard(nickname: String, streak: Int, isMe: Boolean, isSelecting: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = CircleShape,
                color = if (isMe) Primary.copy(alpha = 0.2f) else CardBackgroundLight,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (isMe) "👤" else "🤖", fontSize = 28.sp)
                }
            }
            if (isMe) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = Primary
                ) {
                    Text("나", modifier = Modifier.padding(3.dp), fontSize = 9.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = nickname.take(7),
            style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
            maxLines = 1
        )
        Text("연승 $streak", style = MaterialTheme.typography.bodySmall.copy(color = Primary))
        if (isSelecting && !isMe) {
            Text("선택 중...", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        }
    }
}

@Composable
private fun RpsButton(choice: RpsChoice, onClick: () -> Unit) {
    val scale by remember { mutableStateOf(1f) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackgroundLight)
            .clickable(onClick = onClick)
            .border(2.dp, SurfaceVariant, RoundedCornerShape(20.dp))
            .scale(scale),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = choice.emoji, fontSize = 40.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = choice.label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CountdownCircle(countdown: Int) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                if (countdown <= 3) Error.copy(alpha = 0.3f) else Primary.copy(alpha = 0.2f),
                CircleShape
            )
            .border(2.dp, if (countdown <= 3) Error else Primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$countdown",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = if (countdown <= 3) Error else Primary,
                fontWeight = FontWeight.Black
            )
        )
    }
}

@Composable
private fun InfoChip(emoji: String, label: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = CardBackground) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

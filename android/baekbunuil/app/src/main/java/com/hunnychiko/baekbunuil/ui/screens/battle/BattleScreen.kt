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
import com.hunnychiko.baekbunuil.ui.components.productEmoji
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
    onHome: () -> Unit,
    onForfeit: (String) -> Unit = {}
) {
    val battleState by viewModel.battleState.collectAsState()
    val products by viewModel.products.collectAsState()
    val product = products.find { it.roomId == roomId } ?: sampleProducts.first()
    val challenge by viewModel.currentChallenge.collectAsState()

    var countdown by remember { mutableStateOf(COUNTDOWN_SECONDS) }
    var autoSelected by remember { mutableStateOf(false) }
    var showForfeitDialog by remember { mutableStateOf(false) }

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
                    },
                    onForfeitTap = { if (product.directBuyLabel.isNotEmpty()) showForfeitDialog = true }
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
                    },
                    onRetryDraw = {
                        viewModel.retryDraw(state.matchId, state.opponent, roomId)
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

    // 도전 포기 확인 다이얼로그
    if (showForfeitDialog) {
        ForfeitDialog(
            product = product,
            onConfirm = {
                showForfeitDialog = false
                viewModel.forfeitForGuaranteed(roomId) { onForfeit(roomId) }
            },
            onDismiss = { showForfeitDialog = false }
        )
    }
}

@Composable
private fun ForfeitDialog(
    product: ProductRoom,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(productEmoji(product.productName), fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "도전을 포기하고\n상품을 바로 받을까요?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Gold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯 100% 획득 보장", style = MaterialTheme.typography.titleMedium.copy(color = Gold))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            product.productName,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            product.directBuyLabel,
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = Primary,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "현재 진행 중인 대결은 취소되며\n연승 기록은 초기화됩니다.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text(
                    "${product.directBuyLabel} 직접 획득하기",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Background,
                        fontWeight = FontWeight.Black
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("계속 도전하기", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))
            }
        }
    )
}

@Composable
private fun BattleSelectingContent(
    state: BattleUiState.Selecting,
    product: ProductRoom,
    currentStreak: Int,
    countdown: Int,
    onChoiceSelected: (RpsChoice) -> Unit,
    onForfeitTap: () -> Unit = {}
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
        Surface(shape = RoundedCornerShape(6.dp), color = CardBackground) {
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

        // 도전 포기 배너 (directBuyLabel 있을 때만 표시)
        if (product.directBuyLabel.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onForfeitTap),
                shape = RoundedCornerShape(8.dp),
                color = CardBackground
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.horizontalGradient(
                            listOf(Gold.copy(alpha = 0.15f), Primary.copy(alpha = 0.1f))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(productEmoji(product.productName), fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "지금 도전을 포기하시면",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Text(
                                "해당 상품을 100% 획득 가능합니다",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Gold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                product.directBuyLabel,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Gold,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

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
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(state.myChoice.emoji, fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("선택 완료!", style = MaterialTheme.typography.headlineMedium.copy(color = Primary))
        Text("상대방 선택 중...", style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(color = Primary)
        if (state.commitHash.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🔒 공정성 커밋", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${state.commitHash.take(24)}...",
                        style = MaterialTheme.typography.bodySmall.copy(color = Primary),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text("결과 공개 후 시드로 검증 가능", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                }
            }
        }
    }
}

@Composable
private fun BattleResultContent(
    state: BattleUiState.Result,
    product: ProductRoom,
    onContinue: () -> Unit,
    onHome: () -> Unit,
    onRetryDraw: () -> Unit = {}
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
                    modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp))
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
            Surface(shape = RoundedCornerShape(8.dp), color = CardBackground) {
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
            Surface(shape = RoundedCornerShape(8.dp), color = CardBackground) {
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
                    shape = RoundedCornerShape(8.dp),
                    color = Gold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("추첨 참여 슬롯 자동 입장 중...", style = MaterialTheme.typography.bodyLarge.copy(color = Gold))
                        Text("${product.productName} 1/100 추첨 참여 완료!", style = MaterialTheme.typography.titleMedium.copy(color = Gold))
                    }
                }
            } else if (isDraw) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "무승부 재대결은 승부권을 소모하지 않습니다",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = onRetryDraw,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Text("🤝 재대결하기", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
                    }
                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(7.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant)
                    ) {
                        Text("홈으로", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))
                    }
                }
            } else if (isWin) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("▶ 광고 보고 계속 도전", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Text("▶ 광고 보고 재도전", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
                    }
                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(7.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant)
                    ) {
                        Text("홈으로", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))
                    }
                }
            }
            // 공정성 검증 카드
            if (state.revealedSeed.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🔍 공정성 검증", style = MaterialTheme.typography.titleSmall.copy(color = Primary))
                        Spacer(Modifier.height(6.dp))
                        Text("공개 시드", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                        Text(
                            state.revealedSeed,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("커밋 해시 (SHA256)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                        Text(
                            state.commitHash,
                            style = MaterialTheme.typography.bodySmall.copy(color = Primary),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "검증: SHA256(시드|${state.opponentChoice.name}) = 커밋 해시",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
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
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackgroundLight)
            .clickable(onClick = onClick)
            .border(2.dp, SurfaceVariant, RoundedCornerShape(10.dp))
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
    Surface(shape = RoundedCornerShape(10.dp), color = CardBackground) {
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

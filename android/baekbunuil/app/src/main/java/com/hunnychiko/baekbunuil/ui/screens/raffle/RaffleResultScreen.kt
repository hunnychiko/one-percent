package com.hunnychiko.baekbunuil.ui.screens.raffle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.data.model.DrawResult
import com.hunnychiko.baekbunuil.data.model.sampleProducts
import com.hunnychiko.baekbunuil.ui.components.ParticipantBar
import com.hunnychiko.baekbunuil.ui.components.productEmoji
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@Composable
fun RaffleResultScreen(
    roomId: String,
    viewModel: AppViewModel,
    onHome: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val product = products.find { it.roomId == roomId } ?: sampleProducts.first()
    val user by viewModel.user.collectAsState()

    var drawResult by remember { mutableStateOf<DrawResult?>(null) }
    var isCheckingResult by remember { mutableStateOf(true) }
    val isWaiting = product.currentCount < product.capacity

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rotation"
    )

    LaunchedEffect(roomId) {
        kotlinx.coroutines.delay(1000)
        drawResult = viewModel.repo.checkDrawResult(user?.userId ?: "", roomId)
        isCheckingResult = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Primary.copy(alpha = 0.2f), Background, Background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            when {
                isCheckingResult -> {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(64.dp))
                    Text("결과 확인 중...", style = MaterialTheme.typography.headlineMedium)
                }
                isWaiting || drawResult == null -> {
                    WaitingForDraw(product = product, onHome = onHome, rotation = rotation)
                }
                drawResult?.isWinner == true -> {
                    WinnerContent(product = product, onHome = onHome)
                }
                else -> {
                    LoserContent(product = product, onHome = onHome)
                }
            }
        }
    }
}

@Composable
private fun WaitingForDraw(
    product: com.hunnychiko.baekbunuil.data.model.ProductRoom,
    onHome: () -> Unit,
    rotation: Float
) {
    Text("🎉", fontSize = 72.sp)
    Text(
        "추첨 슬롯 입장 완료!",
        style = MaterialTheme.typography.headlineLarge.copy(color = Gold, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    )
    Text(
        "추첨 결과를 기다리고 있어요",
        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary, textAlign = TextAlign.Center)
    )

    Surface(shape = RoundedCornerShape(20.dp), color = CardBackground) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(productEmoji(product), fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text(product.productName, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            ParticipantBar(
                current = product.currentCount,
                total = product.capacity,
                fillPercent = product.currentCount / product.capacity.toFloat()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${product.capacity - product.currentCount}명 더 모이면 자동 추첨!",
                style = MaterialTheme.typography.bodyMedium.copy(color = Primary),
                textAlign = TextAlign.Center
            )
        }
    }

    Text("🔄", fontSize = 36.sp, modifier = Modifier.rotate(rotation))
    Text(
        "100명이 모두 모이면 서버에서 자동 추첨이 진행됩니다.\n알림으로 결과를 알려드립니다.",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, textAlign = TextAlign.Center)
    )

    Spacer(Modifier.weight(1f))

    Button(
        onClick = onHome,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text("다른 상품도 도전하기", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
    }
}

@Composable
private fun WinnerContent(
    product: com.hunnychiko.baekbunuil.data.model.ProductRoom,
    onHome: () -> Unit
) {
    Text("🏆", fontSize = 80.sp)
    Text(
        "당첨!",
        style = MaterialTheme.typography.displayLarge.copy(color = Gold, fontWeight = FontWeight.Black)
    )
    Text(
        "축하합니다! ${product.productName}에 당첨되셨습니다!",
        style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary, textAlign = TextAlign.Center)
    )

    Surface(shape = RoundedCornerShape(16.dp), color = Gold.copy(alpha = 0.15f)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(productEmoji(product), fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(product.productName, style = MaterialTheme.typography.headlineMedium.copy(color = Gold), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "마이페이지 > 당첨 기록에서 상품 수령 정보를 확인하세요.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, textAlign = TextAlign.Center)
            )
        }
    }

    Spacer(Modifier.weight(1f))
    Button(
        onClick = onHome,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text("홈으로", style = MaterialTheme.typography.titleMedium.copy(color = Background, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun LoserContent(
    product: com.hunnychiko.baekbunuil.data.model.ProductRoom,
    onHome: () -> Unit
) {
    Text("😔", fontSize = 72.sp)
    Text(
        "아쉽게도 이번엔 아니네요",
        style = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center)
    )
    Text(
        "다음 회차에 다시 도전해보세요!\n연습이 쌓이면 더 빠르게 연승할 수 있어요.",
        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary, textAlign = TextAlign.Center)
    )

    Surface(shape = RoundedCornerShape(16.dp), color = CardBackground) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("다음 회차", style = MaterialTheme.typography.titleMedium.copy(color = Primary))
            Text(product.productName, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                "곧 ${product.round + 1}차 추첨방이 오픈됩니다",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    Spacer(Modifier.weight(1f))
    Button(
        onClick = onHome,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text("다시 도전하기", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
    }
}

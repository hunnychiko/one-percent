package com.hunnychiko.baekbunuil.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.hunnychiko.baekbunuil.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage("🎯", "1/100 백분의일", "100명 중 1명, 연승으로 들어가는 추첨 찬스", "원하는 상품을 선택하고\n광고 시청으로 승부권을 획득하세요"),
    OnboardingPage("📺", "광고로 승부권 획득", "광고를 보면 승부권 1장!", "보상형 광고를 끝까지 시청하면\n승부권 1장을 바로 받을 수 있어요"),
    OnboardingPage("✊", "실시간 가위바위보 대결", "유저 VS 유저, 동시 선택 후 결과 공개", "전 세계 유저와 실시간으로 매칭!\n목표 연승을 달성해 추첨 방에 입장하세요"),
    OnboardingPage("🏆", "1/100 추첨 참여", "100명이 모이면 자동 추첨!", "연승 달성 시 해당 상품 추첨 슬롯에 참여\n1명이 상품을 가져갑니다")
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(page = pages[page])
        }

        // 건너뛰기
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
        ) {
            Text("건너뛰기", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 페이지 인디케이터
            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = Primary,
                inactiveColor = SurfaceVariant,
                indicatorWidth = 24.dp,
                indicatorHeight = 6.dp,
                spacing = 6.dp,
                indicatorShape = RoundedCornerShape(3.dp)
            )

            // 버튼
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "다음" else "시작하기",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = page.emoji, fontSize = 80.sp)
        Spacer(Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.displayMedium.copy(
                color = Primary,
                textAlign = TextAlign.Center
            )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        )
    }
}

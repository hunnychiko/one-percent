package com.hunnychiko.baekbunuil.ui.screens.affiliate

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.R
import com.hunnychiko.baekbunuil.data.model.AffiliateBanner
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@Composable
fun AffiliateBannerSection(viewModel: AppViewModel) {
    val banners by viewModel.affiliateBanners.collectAsState()
    val rewardMessage by viewModel.affiliateRewardMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAffiliateBanners() }

    if (banners.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "제휴 혜택",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "배너를 탭해 도전권 획득",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(banners, key = { it.bannerId }) { banner ->
                AffiliateBannerCard(
                    banner = banner,
                    onClick = { viewModel.claimAffiliateReward(banner.bannerId) }
                )
            }
        }
    }

    // 보상 스낵바
    rewardMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearAffiliateMessage()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (msg.startsWith("🎉")) Primary else CardBackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    msg,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
private fun AffiliateBannerCard(banner: AffiliateBanner, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Box {
            // 배너 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Primary.copy(alpha = 0.3f), Secondary.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (banner.imageUrl.isNotEmpty()) {
                    // 실제 이미지: Coil 등 이미지 로더로 교체 가능
                    Text("🏢", fontSize = 40.sp)
                } else {
                    // 플레이스홀더
                    Image(
                        painter = painterResource(R.drawable.anim_affiliate_reward),
                        contentDescription = banner.companyName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 티켓 뱃지
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = Primary
            ) {
                Text(
                    "+${banner.ticketReward}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            // 하단 정보
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                CardBackground
                            )
                        )
                    )
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 10.dp)
            ) {
                Text(
                    banner.companyName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "도전권 ${banner.ticketReward}개 지급",
                    style = MaterialTheme.typography.bodySmall.copy(color = Primary)
                )
            }
        }
    }
}

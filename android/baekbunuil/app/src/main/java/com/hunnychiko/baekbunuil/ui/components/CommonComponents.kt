package com.hunnychiko.baekbunuil.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hunnychiko.baekbunuil.data.model.ProductRoom
import com.hunnychiko.baekbunuil.data.model.gradeFromStreak
import com.hunnychiko.baekbunuil.ui.theme.*

@Composable
fun TopBar(
    ticketCount: Int,
    bestStreak: Int,
    hasUnread: Boolean = false,
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "1/100",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = Primary,
                fontWeight = FontWeight.Black,
                fontSize = 26.sp
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip(icon = "🎫", value = "$ticketCount")
            StatChip(icon = "👑", value = "$bestStreak")
            BadgedBox(
                badge = {
                    if (hasUnread) Badge(containerColor = Primary)
                }
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "알림",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatChip(icon: String, value: String) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = SurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 13.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductRoom,
    myCurrentStreak: Int = 0,
    onClick: () -> Unit
) {
    val grade = gradeFromStreak(product.requiredStreak)
    val gradeColor = Color(grade.color)
    val fillPercent = product.currentCount / product.capacity.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GradeBadge(label = grade.label, color = gradeColor)
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "1/100",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                ProductEmoji(product = product)
            }
            Spacer(Modifier.height(14.dp))
            StreakStars(current = myCurrentStreak, target = product.requiredStreak)
            Spacer(Modifier.height(10.dp))
            ParticipantBar(current = product.currentCount, total = product.capacity, fillPercent = fillPercent)
        }
    }
}

@Composable
fun HeroProductCard(product: ProductRoom, myCurrentStreak: Int = 0, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "오늘의 히어로",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        product.productName,
                        style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
                        maxLines = 2
                    )
                    Text(
                        product.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
                ProductEmoji(product = product, imageSize = 80.dp, emojiSize = 56.sp)
            }
            Spacer(Modifier.height(16.dp))
            StreakStars(current = myCurrentStreak, target = product.requiredStreak, large = true)
            Spacer(Modifier.height(12.dp))
            ParticipantBar(
                current = product.currentCount,
                total = product.capacity,
                fillPercent = product.currentCount / product.capacity.toFloat()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "참여하기",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun GradeBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun StreakStars(current: Int, target: Int, large: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(target.coerceAtMost(10)) { i ->
            Text(
                text = if (i < current) "⭐" else "☆",
                fontSize = if (large) 20.sp else 15.sp,
                color = if (i < current) StarActive else StarInactive
            )
        }
        if (target > 10) {
            Text(
                text = "+${target - 10}",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
fun ParticipantBar(current: Int, total: Int, fillPercent: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "참여 중",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
            Text(
                text = "$current / $total 명",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fillPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = Primary,
            trackColor = SurfaceVariant
        )
    }
}

@Composable
fun ProductEmoji(
    product: ProductRoom,
    imageSize: Dp = 52.dp,
    emojiSize: TextUnit = 42.sp
) {
    if (product.imageUrl.isNotEmpty()) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.productName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = productEmoji(product), fontSize = emojiSize * 0.7f)
        }
    }
}

fun productEmoji(product: ProductRoom): String = productEmoji(product.productName)

fun productEmoji(name: String): String = when {
    name.contains("청소기") || name.contains("Roborock") -> "🤖"
    name.contains("이어폰") || name.contains("AirPods") || name.contains("Buds") -> "🎧"
    name.contains("치킨") -> "🍗"
    name.contains("커피") -> "☕"
    name.contains("아이스크림") -> "🍦"
    name.contains("영화") -> "🎬"
    name.contains("피자") -> "🍕"
    name.contains("공기청정") || name.contains("다이슨") -> "💨"
    else -> "🎁"
}

@Composable
fun TicketBadge(count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Primary.copy(alpha = 0.08f),
        border = BorderStroke(1.5.dp, Primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🎫", fontSize = 20.sp)
            Column {
                Text(
                    text = "보유 승부권",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Text(
                    text = "${count}장",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Primary,
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    }
}

@Composable
fun PulsingDot(color: Color = Primary) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .scale(scale)
            .background(color, CircleShape)
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String = "") {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Primary)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

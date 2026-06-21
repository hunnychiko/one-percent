package com.hunnychiko.baekbunuil.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "1/100",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = Primary,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatChip(icon = "🎫", value = "$ticketCount")
            StatChip(icon = "👑", value = "$bestStreak")
            BadgedBox(
                badge = {
                    if (hasUnread) Badge(containerColor = Primary)
                }
            ) {
                IconButton(onClick = onNotificationClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = "알림", tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun StatChip(icon: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardBackgroundLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(text = value, style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary))
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GradeBadge(label = grade.label, color = gradeColor)
                        Text(
                            text = "1/100",
                            style = MaterialTheme.typography.labelSmall.copy(color = Primary)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ProductEmoji(product = product)
            }
            Spacer(Modifier.height(12.dp))
            StreakStars(
                current = myCurrentStreak,
                target = product.requiredStreak
            )
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Primary.copy(alpha = 0.15f), Color.Transparent))
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("오늘의 히어로", style = MaterialTheme.typography.bodySmall.copy(color = Primary))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            product.productName,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 2
                        )
                        Text(
                            product.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = productEmoji(product),
                        fontSize = 56.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                StreakStars(current = myCurrentStreak, target = product.requiredStreak, large = true)
                Spacer(Modifier.height(12.dp))
                ParticipantBar(current = product.currentCount, total = product.capacity,
                    fillPercent = product.currentCount / product.capacity.toFloat())
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("도전하기", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
                }
            }
        }
    }
}

@Composable
fun GradeBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.2f)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun StreakStars(current: Int, target: Int, large: Boolean = false) {
    val starSize = if (large) 28.dp else 20.dp
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(target.coerceAtMost(10)) { i ->
            Text(
                text = if (i < current) "⭐" else "☆",
                fontSize = if (large) 22.sp else 16.sp,
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
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "$current / $total 명",
                style = MaterialTheme.typography.bodySmall.copy(color = Primary, fontWeight = FontWeight.SemiBold)
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fillPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Primary,
            trackColor = SurfaceVariant
        )
    }
}

@Composable
fun ProductEmoji(product: ProductRoom) {
    Text(
        text = productEmoji(product),
        fontSize = 42.sp,
        modifier = Modifier.padding(start = 8.dp)
    )
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
        shape = RoundedCornerShape(10.dp),
        color = Primary.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🎫", fontSize = 18.sp)
            Text(
                text = "승부권 ${count}장",
                style = MaterialTheme.typography.titleMedium.copy(color = Primary)
            )
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
            .size(12.dp)
            .scale(scale)
            .background(color, CircleShape)
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String = "") {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        if (subtitle.isNotEmpty()) {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

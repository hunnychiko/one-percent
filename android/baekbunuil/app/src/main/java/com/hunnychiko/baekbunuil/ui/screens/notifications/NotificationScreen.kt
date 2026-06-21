package com.hunnychiko.baekbunuil.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.data.model.AppNotification
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(Unit) { viewModel.markNotificationsRead() }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("알림", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "받은 알림이 없어요",
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "대결 결과, 추첨, 배송 알림이 여기에 표시됩니다",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationItem(notif)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = DividerColor
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notif: AppNotification) {
    Surface(
        color = if (!notif.isRead) Primary.copy(alpha = 0.05f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(5.dp),
                color = notifAccentColor(notif.type).copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(notifEmoji(notif.type), fontSize = 18.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notif.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    notif.body,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatTime(notif.createdAt),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }
            if (!notif.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .background(Primary, CircleShape)
                )
            }
        }
    }
}

private fun notifEmoji(type: String) = when (type) {
    "draw_result" -> "🎉"
    "match_found" -> "⚔️"
    "shipping"    -> "📦"
    "win"         -> "🏆"
    else          -> "🔔"
}

private fun notifAccentColor(type: String): Color = when (type) {
    "draw_result", "win" -> Gold
    "match_found"        -> Primary
    else                 -> TextSecondary
}

private fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L      -> "방금 전"
        diff < 3_600_000L   -> "${diff / 60_000}분 전"
        diff < 86_400_000L  -> "${diff / 3_600_000}시간 전"
        else                -> SimpleDateFormat("MM/dd", Locale.KOREA).format(Date(timestamp))
    }
}

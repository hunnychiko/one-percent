package com.hunnychiko.baekbunuil.ui.screens.mypage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hunnychiko.baekbunuil.R
import com.hunnychiko.baekbunuil.data.UserPreferences
import com.hunnychiko.baekbunuil.ui.components.TicketBadge
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onInvite: () -> Unit = {},
    onClaim: (String) -> Unit = {}
) {
    val user by viewModel.user.collectAsState()
    val winHistory by viewModel.winHistory.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var adConsent by remember { mutableStateOf(user?.adConsent ?: false) }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var selectedAvatarId by remember { mutableStateOf(UserPreferences.avatarId) }
    var selectedPhotoUri by remember {
        mutableStateOf<Uri?>(UserPreferences.photoUri.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) })
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            selectedAvatarId = 5
            viewModel.updateAvatar(5, uri.toString())
        }
    }

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("마이페이지", style = MaterialTheme.typography.titleLarge) },
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 프로필 카드
            Surface(shape = RoundedCornerShape(10.dp), color = CardBackground) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { showAvatarSheet = true },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when {
                                selectedAvatarId == 5 && selectedPhotoUri != null -> {
                                    AsyncImage(
                                        model = selectedPhotoUri,
                                        contentDescription = "프로필 사진",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                }
                                selectedAvatarId in 1..4 -> {
                                    Image(
                                        painter = painterResource(avatarResId(selectedAvatarId)),
                                        contentDescription = "아바타",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                else -> {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("👤", fontSize = 36.sp)
                                    }
                                }
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Primary,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, contentDescription = "변경", tint = TextPrimary, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = user?.nickname ?: "도전자",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "ID: ${(user?.userId ?: "").take(8)}...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 현황 요약
            TicketBadge(count = user?.ticketCount ?: 0, modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "👑",
                    value = "${user?.bestStreak ?: 0}",
                    label = "최고 연승"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🏆",
                    value = "${user?.totalWins ?: 0}",
                    label = "총 승리"
                )
            }

            // 도전 기록 (더미)
            SectionCard(title = "도전 기록") {
                if ((user?.totalWins ?: 0) == 0) {
                    EmptyState(message = "아직 도전 기록이 없어요.\n광고를 보고 첫 대결에 참여해보세요!")
                } else {
                    repeat(3) { i ->
                        ChallengeHistoryItem(
                            productName = listOf("로봇청소기 특가", "커피 쿠폰", "에어팟 프로")[i],
                            streak = listOf(6, 3, 1)[i],
                            target = listOf(10, 3, 10)[i],
                            isCompleted = i == 1
                        )
                        if (i < 2) HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            // 당첨 기록
            SectionCard(title = "당첨 기록") {
                if (winHistory.isEmpty()) {
                    EmptyState(message = "아직 당첨 기록이 없어요.\n목표 연승을 달성해 추첨에 참여하세요!")
                } else {
                    winHistory.forEachIndexed { i, win ->
                        WinHistoryRow(
                            productName = win.productName,
                            wonAt = win.wonAt,
                            onClaim = { onClaim(win.roomId) }
                        )
                        if (i < winHistory.lastIndex) {
                            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }

            // 광고/개인정보 설정
            SectionCard(title = "광고 및 개인정보 설정") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("맞춤형 광고 허용", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "관심사에 가까운 광고를 제공하기 위해 광고 ID 및 앱 이용 정보가 사용될 수 있습니다.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = adConsent,
                        onCheckedChange = { adConsent = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary, checkedTrackColor = Primary,
                            uncheckedThumbColor = TextSecondary, uncheckedTrackColor = CardBackgroundLight
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(12.dp))
                SettingsItem(icon = Icons.Default.PrivacyTip, label = "개인정보처리방침", onClick = {})
                SettingsItem(icon = Icons.Default.Article, label = "이용약관", onClick = {})
            }

            // 친구 초대
            Button(
                onClick = onInvite,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "친구 초대", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("친구 초대하고 도전권 받기", style = MaterialTheme.typography.titleMedium)
            }

            // 앱 정보
            SectionCard(title = "앱 정보") {
                SettingsItem(icon = Icons.Default.Info, label = "버전 1.0.0", onClick = {})
                SettingsItem(icon = Icons.Default.Star, label = "앱 평가하기", onClick = {})
            }

            // 로그아웃
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = "로그아웃", tint = Error, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("로그아웃", style = MaterialTheme.typography.titleMedium.copy(color = Error))
            }
        }
    }

    if (showAvatarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarSheet = false },
            containerColor = CardBackground
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("프로필 캐릭터 선택", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (id in 1..4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable {
                                    selectedAvatarId = id
                                    selectedPhotoUri = null
                                    viewModel.updateAvatar(id)
                                    showAvatarSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (selectedAvatarId == id) Primary.copy(alpha = 0.25f) else CardBackgroundLight,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(avatarResId(id)),
                                    contentDescription = "캐릭터 $id",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (selectedAvatarId == id) {
                                Surface(
                                    shape = CircleShape,
                                    color = Primary,
                                    modifier = Modifier.align(Alignment.BottomEnd).size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Check, null, tint = TextPrimary, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        photoLauncher.launch("image/*")
                        showAvatarSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("사진 앨범에서 선택")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("로그아웃하면 게스트 계정의 기록이 사라질 수 있습니다.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel.signOut()
                    onSignOut()
                }) {
                    Text("로그아웃", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("취소", color = TextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }
}

private fun avatarResId(id: Int) = when (id) {
    1 -> R.drawable.ic_avatar_1
    2 -> R.drawable.ic_avatar_2
    3 -> R.drawable.ic_avatar_3
    4 -> R.drawable.ic_avatar_4
    else -> R.drawable.ic_avatar_1
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, emoji: String, value: String, label: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = CardBackground) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.displayMedium.copy(color = Primary, fontWeight = FontWeight.Black))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(8.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(color = Primary))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun ChallengeHistoryItem(productName: String, streak: Int, target: Int, isCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (isCompleted) "✅" else "⭐", fontSize = 18.sp)
            Column {
                Text(productName, style = MaterialTheme.typography.titleMedium)
                Text("$streak / $target 연승", style = MaterialTheme.typography.bodySmall.copy(color = if (isCompleted) Success else TextSecondary))
            }
        }
        if (isCompleted) {
            Surface(shape = RoundedCornerShape(4.dp), color = Success.copy(alpha = 0.2f)) {
                Text("참여 완료", modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.labelSmall.copy(color = Success))
            }
        }
    }
}

@Composable
private fun WinHistoryRow(productName: String, wonAt: String, onClaim: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🏆", fontSize = 18.sp)
            Column {
                Text(productName, style = MaterialTheme.typography.titleMedium)
                Text(wonAt, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            }
        }
        Surface(
            shape = RoundedCornerShape(5.dp),
            color = Gold.copy(alpha = 0.15f),
            modifier = Modifier.clickable(onClick = onClaim)
        ) {
            Text(
                "수령하기",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge.copy(color = Gold, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary))
        }
    }
}

package com.hunnychiko.baekbunuil.ui.screens.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val myCode      by viewModel.myInviteCode.collectAsState()
    val inviteMsg   by viewModel.inviteMessage.collectAsState()
    val user        by viewModel.user.collectAsState()
    val context     = LocalContext.current

    var inputCode   by remember { mutableStateOf("") }
    var showInput   by remember { mutableStateOf(false) }
    var copied      by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadMyInviteCode() }

    LaunchedEffect(copied) {
        if (copied) {
            kotlinx.coroutines.delay(2000)
            copied = false
        }
    }

    LaunchedEffect(inviteMsg) {
        if (inviteMsg != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearInviteMessage()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("친구 초대", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 혜택 배너
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.horizontalGradient(
                            listOf(Primary.copy(alpha = 0.25f), Secondary.copy(alpha = 0.15f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎁", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "친구 초대하고\n도전권 받기!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "초대한 친구가 가입하면\n나와 친구 모두 도전권 3개!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            // 내 초대 코드
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "내 초대 코드",
                        style = MaterialTheme.typography.titleMedium.copy(color = Primary)
                    )
                    Spacer(Modifier.height(12.dp))

                    if (myCode != null) {
                        // 코드 표시
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBackgroundLight, RoundedCornerShape(6.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                myCode!!,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Primary,
                                    letterSpacing = 6.sp
                                )
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("초대코드", myCode))
                                    copied = true
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "복사",
                                    tint = if (copied) Primary else TextSecondary
                                )
                            }
                        }
                        if (copied) {
                            Text(
                                "클립보드에 복사됨!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Primary),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // 공유 버튼
                        Button(
                            onClick = {
                                val shareText = "1/100 백분의일 앱에서 가위바위보 배틀로 경품 당첨 도전!\n초대 코드: ${myCode}\n앱 다운로드 → [링크 삽입]"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "초대 공유"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("친구에게 공유하기", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Primary
                        )
                    }
                }
            }

            // 초대 코드 입력
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "초대 코드 입력",
                        style = MaterialTheme.typography.titleMedium.copy(color = Primary)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "친구에게 받은 초대 코드를 입력하세요",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it.uppercase().take(6) },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("A1B2C3", style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextSecondary, letterSpacing = 4.sp
                                ))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = DividerColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                letterSpacing = 4.sp, fontWeight = FontWeight.Bold
                            )
                        )
                        Button(
                            onClick = {
                                if (inputCode.length == 6) {
                                    viewModel.applyInviteCode(inputCode)
                                    inputCode = ""
                                }
                            },
                            enabled = inputCode.length == 6,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("적용", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    inviteMsg?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (it.startsWith("🎁")) Primary else Error
                            )
                        )
                    }
                }
            }

            // 초대 혜택 안내
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("초대 혜택 안내", style = MaterialTheme.typography.titleSmall.copy(color = TextSecondary))
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "초대한 친구가 앱 가입 시 도전권 3개 지급",
                        "초대받은 친구도 도전권 3개 지급",
                        "초대 코드는 1회만 사용 가능",
                        "자신의 초대 코드는 사용 불가",
                    ).forEach { text ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("•", style = MaterialTheme.typography.bodySmall.copy(color = Primary))
                            Text(text, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        }
                    }
                }
            }
        }
    }
}

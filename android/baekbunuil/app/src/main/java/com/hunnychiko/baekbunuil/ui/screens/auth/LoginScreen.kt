package com.hunnychiko.baekbunuil.ui.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel
import com.hunnychiko.baekbunuil.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
            account.idToken?.let { token ->
                viewModel.signInWithGoogle(token, onLoginSuccess)
            }
        } catch (e: ApiException) {
            // Google 로그인 취소 또는 실패 — 무시
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Background, CardBackground))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로고
            Text(
                text = "1/100",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Primary
            )
            Text(
                text = "백분의일",
                style = MaterialTheme.typography.headlineLarge.copy(color = TextPrimary),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "연승으로 들어가는 추첨 찬스",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(60.dp))

            // 슬로건 카드
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardBackground
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LoginFeatureItem("📺", "광고 시청", "승부권 획득")
                        LoginFeatureItem("✊", "가위바위보", "연승 도전")
                        LoginFeatureItem("🏆", "1/100", "추첨 참여")
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // Google 로그인
            Button(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("YOUR_WEB_CLIENT_ID")
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "G  Google로 시작하기",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Background,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // 게스트 로그인
            OutlinedButton(
                onClick = { viewModel.signInAnonymously(onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Primary)
                } else {
                    Text(
                        "게스트로 시작하기",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "시작하면 개인정보처리방침 및 이용약관에 동의합니다",
                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoginFeatureItem(emoji: String, title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary))
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
    }
}

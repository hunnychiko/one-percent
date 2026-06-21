package com.hunnychiko.baekbunuil.ui.screens.claim

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
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
import com.hunnychiko.baekbunuil.data.model.WinnerClaim
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimScreen(
    roomId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val claim by viewModel.currentClaim.collectAsState()
    val products by viewModel.products.collectAsState()
    val claimMessage by viewModel.claimMessage.collectAsState()
    val context = LocalContext.current
    val product = products.find { it.roomId == roomId }

    LaunchedEffect(roomId) {
        viewModel.loadWinnerClaim(roomId)
        viewModel.initiateClaim(roomId)
    }

    LaunchedEffect(claimMessage) {
        if (claimMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearClaimMessage()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("상품 수령", style = MaterialTheme.typography.titleLarge) },
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 상품 헤더
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.horizontalGradient(
                            listOf(Gold.copy(alpha = 0.2f), Primary.copy(alpha = 0.1f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "당첨 축하드립니다!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Gold
                            )
                        )
                        Text(
                            product?.productName ?: claim?.productName ?: "",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (claim == null) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                when (claim!!.productType) {
                    "coupon"   -> CouponClaimSection(claim = claim!!, context = context)
                    "physical" -> PhysicalClaimSection(claim = claim!!, viewModel = viewModel)
                    "premium"  -> PremiumClaimSection(claim = claim!!, viewModel = viewModel)
                }
            }

            claimMessage?.let {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (it.startsWith("배송")) Primary.copy(alpha = 0.2f) else Error.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (it.startsWith("배송")) Primary else Error,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CouponClaimSection(claim: WinnerClaim, context: Context) {
    val copied = remember { mutableStateOf(false) }

    LaunchedEffect(copied.value) {
        if (copied.value) {
            kotlinx.coroutines.delay(2000)
            copied.value = false
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("쿠폰 수령", style = MaterialTheme.typography.titleMedium.copy(color = Primary))
            Spacer(Modifier.height(16.dp))

            if (claim.couponCode.isNotEmpty()) {
                Text(
                    "쿠폰 코드",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackgroundLight, RoundedCornerShape(6.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        claim.couponCode,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Gold,
                            letterSpacing = 4.sp
                        )
                    )
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("쿠폰코드", claim.couponCode))
                        copied.value = true
                    }) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "복사",
                            tint = if (copied.value) Primary else TextSecondary
                        )
                    }
                }
                if (copied.value) {
                    Text(
                        "복사되었습니다!",
                        style = MaterialTheme.typography.bodySmall.copy(color = Primary),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else if (claim.affiliateUrl.isNotEmpty()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(claim.affiliateUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("제휴 링크로 이동", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                // 관리자가 아직 쿠폰 코드를 입력하지 않은 경우
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CardBackgroundLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⏳", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "쿠폰 코드 발급 준비 중",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                        )
                        Text(
                            "운영팀이 쿠폰을 준비하고 있습니다.\n잠시 후 다시 확인해주세요.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhysicalClaimSection(claim: WinnerClaim, viewModel: AppViewModel) {
    when (claim.status) {
        "address_submitted", "shipped", "delivered" -> ShippingStatusCard(claim)
        else -> AddressInputForm(claim, viewModel)
    }
}

@Composable
private fun PremiumClaimSection(claim: WinnerClaim, viewModel: AppViewModel) {
    when (claim.verificationStatus) {
        "passed" -> {
            when (claim.status) {
                "address_submitted", "shipped", "delivered" -> ShippingStatusCard(claim)
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    VerificationPassedBanner()
                    AddressInputForm(claim, viewModel)
                }
            }
        }
        "failed" -> VerificationFailedBanner(claim.verificationNote)
        else -> VerificationPendingBanner()
    }
}

@Composable
private fun VerificationPendingBanner() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔍", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "부정 참여 검증 중",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "고가 상품은 부정 참여 여부 확인 후 지급됩니다.\n영업일 기준 1~3일 내 검토 완료 예정입니다.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Warning.copy(alpha = 0.15f)
            ) {
                Text(
                    "검증 완료 시 앱 알림으로 안내드립니다",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(color = Warning)
                )
            }
        }
    }
}

@Composable
private fun VerificationPassedBanner() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Success.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("✅", fontSize = 28.sp)
            Column {
                Text(
                    "검증 완료",
                    style = MaterialTheme.typography.titleMedium.copy(color = Success)
                )
                Text(
                    "정상 참여가 확인되었습니다. 배송지를 입력해주세요.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun VerificationFailedBanner(note: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("❌", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "수령 불가",
                style = MaterialTheme.typography.titleLarge.copy(color = Error, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "부정 참여로 판정되어 상품 지급이 제한되었습니다.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )
            if (note.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = CardBackgroundLight) {
                    Text(
                        "사유: $note",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "이의가 있으실 경우 고객센터로 문의해주세요.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun AddressInputForm(claim: WinnerClaim, viewModel: AppViewModel) {
    var name     by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var address  by remember { mutableStateOf("") }
    var detail   by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("배송지 입력", style = MaterialTheme.typography.titleMedium.copy(color = Primary))

            @Composable
            fun Field(label: String, value: String, onChange: (String) -> Unit, placeholder: String = "") {
                Column {
                    Text(label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = onChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)) },
                        singleLine = true,
                        shape = RoundedCornerShape(5.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Primary
                        )
                    )
                }
            }

            Field("수령인 이름 *", name, { name = it }, "홍길동")
            Field("연락처 *", phone, { phone = it }, "010-0000-0000")
            Field("우편번호 *", postcode, { postcode = it }, "12345")
            Field("주소 *", address, { address = it }, "서울시 강남구 테헤란로")
            Field("상세 주소", detail, { detail = it }, "101동 202호")

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || postcode.isBlank() || address.isBlank()) return@Button
                    submitting = true
                    viewModel.submitShippingAddress(claim.claimId, name, phone, postcode, address, detail)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !submitting && name.isNotBlank() && phone.isNotBlank() && postcode.isNotBlank() && address.isNotBlank()
            ) {
                Text(
                    if (submitting) "접수 중..." else "배송지 접수",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                "배송지 입력 후 운영자 검토를 거쳐 발송됩니다. (영업일 3~5일 소요)",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ShippingStatusCard(claim: WinnerClaim) {
    val (icon, title, desc) = when (claim.status) {
        "address_submitted" -> Triple("📦", "배송 준비 중", "운영자가 배송을 준비하고 있습니다.")
        "shipped"           -> Triple("🚚", "배송 중", "택배가 출발했습니다.")
        "delivered"         -> Triple("✅", "배송 완료", "상품이 배달되었습니다.")
        else                -> Triple("📋", "접수 완료", "배송지가 접수되었습니다.")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(icon, fontSize = 32.sp)
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                }
            }

            if (claim.trackingNumber.isNotEmpty()) {
                HorizontalDivider(color = DividerColor)
                Column {
                    Text("운송장 번호", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${claim.trackingCarrier.ifEmpty { "택배사" }}: ${claim.trackingNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(color = Primary)
                    )
                }
            }

            // 배송지 요약
            HorizontalDivider(color = DividerColor)
            Column {
                Text("배송지", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                Spacer(Modifier.height(4.dp))
                Text(
                    "${claim.shippingName} · ${claim.shippingPhone}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "(${claim.shippingPostcode}) ${claim.shippingAddress} ${claim.shippingDetail}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

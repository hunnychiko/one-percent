package com.hunnychiko.baekbunuil.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hunnychiko.baekbunuil.ui.screens.history.HistoryScreen
import com.hunnychiko.baekbunuil.ui.screens.home.HomeContent
import com.hunnychiko.baekbunuil.ui.screens.mypage.MyPageScreen
import com.hunnychiko.baekbunuil.ui.screens.ranking.RankingScreen
import com.hunnychiko.baekbunuil.ui.screens.ticket.GeneralTicketScreen
import com.hunnychiko.baekbunuil.ui.theme.*
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

private data class TabItem(val icon: ImageVector, val label: String)

private val tabs = listOf(
    TabItem(Icons.Default.Home, "홈"),
    TabItem(Icons.Default.EmojiEvents, "랭킹"),
    TabItem(Icons.Default.PlayCircle, "충전소"),
    TabItem(Icons.Default.History, "내기록"),
    TabItem(Icons.Default.Person, "마이"),
)

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit,
    onSignOut: () -> Unit,
    onInvite: () -> Unit = {},
    onClaim: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val hasUnread by viewModel.unreadNotificationCount.collectAsState()
    val showBadge = hasUnread > 0

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint = if (selectedTab == index) Primary else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                tab.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selectedTab == index) Primary else TextSecondary
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            indicatorColor = Primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it / 4 } + fadeIn() togetherWith
                            slideOutHorizontally { -it / 4 } + fadeOut()
                    } else {
                        slideInHorizontally { -it / 4 } + fadeIn() togetherWith
                            slideOutHorizontally { it / 4 } + fadeOut()
                    }
                },
                label = "tab_animation"
            ) { tab ->
                when (tab) {
                    0 -> HomeContent(viewModel = viewModel, onProductClick = onProductClick, onNotificationClick = onNotificationClick, hasUnread = showBadge)
                    1 -> RankingScreen(viewModel = viewModel, onNotificationClick = onNotificationClick, hasUnread = showBadge)
                    2 -> GeneralTicketScreen(viewModel = viewModel, onProductClick = onProductClick, onNotificationClick = onNotificationClick, hasUnread = showBadge)
                    3 -> HistoryScreen(viewModel = viewModel, onProductClick = onProductClick, onNotificationClick = onNotificationClick, hasUnread = showBadge)
                    4 -> MyPageScreen(
                        viewModel = viewModel,
                        onBack = { selectedTab = 0 },
                        onSignOut = onSignOut,
                        onInvite = onInvite,
                        onClaim = onClaim
                    )
                }
            }
        }
    }
}

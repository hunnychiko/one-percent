package com.hunnychiko.baekbunuil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hunnychiko.baekbunuil.ui.screens.auth.LoginScreen
import com.hunnychiko.baekbunuil.ui.screens.battle.BattleScreen
import com.hunnychiko.baekbunuil.ui.screens.claim.ClaimScreen
import com.hunnychiko.baekbunuil.ui.screens.invite.InviteScreen
import com.hunnychiko.baekbunuil.ui.screens.main.MainScreen
import com.hunnychiko.baekbunuil.ui.screens.matching.MatchingScreen
import com.hunnychiko.baekbunuil.ui.screens.mypage.MyPageScreen
import com.hunnychiko.baekbunuil.ui.screens.onboarding.OnboardingScreen
import com.hunnychiko.baekbunuil.ui.screens.product.ProductDetailScreen
import com.hunnychiko.baekbunuil.ui.screens.raffle.RaffleResultScreen
import com.hunnychiko.baekbunuil.ui.screens.ticket.TicketScreen
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRODUCT_DETAIL = "product/{roomId}"
    const val TICKET = "ticket/{roomId}"
    const val MATCHING = "matching/{roomId}"
    const val BATTLE = "battle/{roomId}"
    const val RAFFLE_RESULT = "raffle_result/{roomId}"
    const val MYPAGE = "mypage"
    const val INVITE = "invite"
    const val CLAIM = "claim/{roomId}"

    fun productDetail(roomId: String) = "product/$roomId"
    fun claim(roomId: String) = "claim/$roomId"
    fun ticket(roomId: String) = "ticket/$roomId"
    fun matching(roomId: String) = "matching/$roomId"
    fun battle(roomId: String) = "battle/$roomId"
    fun raffleResult(roomId: String) = "raffle_result/$roomId"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    viewModel: AppViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.ONBOARDING) { inclusive = true } } }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            )
        }
        composable(Routes.HOME) {
            MainScreen(
                viewModel = viewModel,
                onProductClick = { roomId -> navController.navigate(Routes.productDetail(roomId)) },
                onSignOut = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onInvite = { navController.navigate(Routes.INVITE) },
                onClaim  = { roomId -> navController.navigate(Routes.claim(roomId)) }
            )
        }
        composable(
            Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { back ->
            val roomId = back.arguments?.getString("roomId") ?: return@composable
            ProductDetailScreen(
                roomId = roomId,
                viewModel = viewModel,
                onChallenge = { navController.navigate(Routes.ticket(roomId)) },
                onWatchAd = { navController.navigate(Routes.ticket(roomId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Routes.TICKET,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { back ->
            val roomId = back.arguments?.getString("roomId") ?: return@composable
            TicketScreen(
                roomId = roomId,
                viewModel = viewModel,
                onAdComplete = { navController.navigate(Routes.matching(roomId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Routes.MATCHING,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { back ->
            val roomId = back.arguments?.getString("roomId") ?: return@composable
            MatchingScreen(
                roomId = roomId,
                viewModel = viewModel,
                onMatchFound = { navController.navigate(Routes.battle(roomId)) { popUpTo(Routes.matching(roomId)) { inclusive = true } } },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            Routes.BATTLE,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { back ->
            val roomId = back.arguments?.getString("roomId") ?: return@composable
            BattleScreen(
                roomId = roomId,
                viewModel = viewModel,
                onStreakComplete = { navController.navigate(Routes.raffleResult(roomId)) { popUpTo(Routes.HOME) } },
                onContinue = { navController.navigate(Routes.ticket(roomId)) { popUpTo(Routes.battle(roomId)) { inclusive = true } } },
                onHome = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onForfeit = { id -> navController.navigate(Routes.claim(id)) { popUpTo(Routes.HOME) } }
            )
        }
        composable(
            Routes.RAFFLE_RESULT,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { back ->
            val roomId = back.arguments?.getString("roomId") ?: return@composable
            RaffleResultScreen(
                roomId = roomId,
                viewModel = viewModel,
                onHome = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            )
        }
        composable(Routes.MYPAGE) {
            MyPageScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSignOut = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                onInvite = { navController.navigate(Routes.INVITE) }
            )
        }
        composable(Routes.INVITE) {
            InviteScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Routes.CLAIM,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { back ->
            val roomId = back.arguments?.getString("roomId") ?: return@composable
            ClaimScreen(
                roomId = roomId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

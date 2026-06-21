package com.hunnychiko.baekbunuil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.hunnychiko.baekbunuil.navigation.AppNavigation
import com.hunnychiko.baekbunuil.navigation.Routes
import com.hunnychiko.baekbunuil.ui.theme.BaekbunuilTheme
import com.hunnychiko.baekbunuil.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this)

        setContent {
            BaekbunuilTheme {
                val navController = rememberNavController()
                val viewModel: AppViewModel = viewModel()

                val startDestination = remember {
                    // 최초 실행이면 온보딩, 이미 로그인돼 있으면 홈
                    if (viewModel.isSignedIn()) Routes.HOME else Routes.ONBOARDING
                }

                AppNavigation(
                    navController = navController,
                    startDestination = startDestination,
                    viewModel = viewModel
                )
            }
        }
    }
}

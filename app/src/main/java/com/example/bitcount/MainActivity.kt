package com.example.bitcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitcount.ui.SplashScreen
import com.example.bitcount.ui.about.AboutScreen
import com.example.bitcount.ui.history.HistoryScreen
import com.example.bitcount.ui.home.BitCountViewModel
import com.example.bitcount.ui.home.HomeScreen
import com.example.bitcount.ui.navigation.Screen
import com.example.bitcount.ui.settings.SettingsScreen
import com.example.bitcount.ui.theme.BitCountTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BitCountViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            BitCountTheme(
                appTheme = state.settings.theme,
                dynamicColor = state.settings.dynamicColor
            ) {
                val navController = rememberNavController()
                val animationsEnabled = state.settings.animations

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    enterTransition = { if (animationsEnabled) fadeIn() else EnterTransition.None },
                    exitTransition = { if (animationsEnabled) fadeOut() else ExitTransition.None },
                    popEnterTransition = { if (animationsEnabled) fadeIn() else EnterTransition.None },
                    popExitTransition = { if (animationsEnabled) fadeOut() else ExitTransition.None }
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onSplashFinished = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { navController.navigate(Screen.History.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToAbout = { navController.navigate(Screen.About.route) }
                        )
                    }

                    composable(Screen.History.route) {
                        HistoryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.About.route) {
                        AboutScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

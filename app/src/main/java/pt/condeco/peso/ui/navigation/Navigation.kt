package pt.condeco.peso.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.ui.analytics.AnalyticsScreen
import pt.condeco.peso.ui.dashboard.DashboardScreen
import pt.condeco.peso.ui.ledger.LedgerScreen
import pt.condeco.peso.ui.settings.SettingsScreen

enum class Screen(val route: String) {
    Dashboard("dashboard"),
    Ledger("ledger"),
    Analytics("analytics"),
    Settings("settings");

    companion object {
        fun fromRoute(route: String?): Screen = entries.firstOrNull { it.route == route } ?: Dashboard
    }
}

/** Bottom-bar style navigation: one copy of each top-level screen, state preserved, back returns to Dashboard. */
fun NavHostController.navigateTo(screen: Screen) {
    navigate(screen.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun PesoNavHost(navController: NavHostController, viewModel: WeightViewModel) {
    val onNavigate: (Screen) -> Unit = { navController.navigateTo(it) }
    val onBack: () -> Unit = { navController.popBackStack() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = Screen.fromRoute(backStackEntry?.destination?.route)

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(300)) }
    ) {
        composable(Screen.Dashboard.route) { DashboardScreen(viewModel, current, onNavigate) }
        composable(Screen.Ledger.route) { LedgerScreen(viewModel, current, onNavigate, onBack) }
        composable(Screen.Analytics.route) { AnalyticsScreen(viewModel, current, onNavigate, onBack) }
        composable(Screen.Settings.route) { SettingsScreen(viewModel, current, onNavigate, onBack) }
    }
}

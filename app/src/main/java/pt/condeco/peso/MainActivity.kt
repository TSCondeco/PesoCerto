package pt.condeco.peso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.condeco.peso.data.SettingsRepository
import pt.condeco.peso.data.WeightDatabase
import pt.condeco.peso.ui.*
import pt.condeco.peso.ui.theme.PesoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database by lazy { WeightDatabase.getDatabase(this) }
        val weightDao by lazy { database.weightDao() }
        val settingsRepository by lazy { SettingsRepository(this) }

        setContent {
            PesoTheme {
                val viewModel: WeightViewModel = viewModel(
                    factory = WeightViewModelFactory(weightDao, settingsRepository)
                )
                val currentScreen by viewModel.currentScreen.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith
                            fadeOut(animationSpec = tween(400))
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Dashboard -> DashboardScreen(viewModel)
                            is Screen.Ledger -> LedgerScreen(viewModel)
                            is Screen.Analytics -> AnalyticsScreen(viewModel)
                            is Screen.Settings -> SettingsScreen(viewModel)
                        }
                    }

                    // Global Bottom Sheet for Adding/Editing
                    AddEntryBottomSheet(viewModel)
                }
            }
        }
    }
}

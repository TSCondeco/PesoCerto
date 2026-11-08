package pt.condeco.peso

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.ui.addentry.AddEntryBottomSheet
import pt.condeco.peso.ui.navigation.PesoNavHost
import pt.condeco.peso.ui.theme.PesoTheme
import pt.condeco.peso.ui.theme.isDarkTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result handled when the user enables reminders in Settings */ }

    // Bumped whenever a notification or widget asks to open the add-weight sheet
    private var openAddRequest by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handleIntent(intent)

        // Android 13+ requires a runtime permission for notifications to be shown
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        setContent {
            val viewModel: WeightViewModel = koinViewModel()
            val state by viewModel.uiState.collectAsState()
            val dark = isDarkTheme(state.themeMode)

            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
                    ) { dark }
                )
                onDispose { }
            }

            PesoTheme(darkTheme = dark) {
                LaunchedEffect(openAddRequest) {
                    if (openAddRequest > 0) viewModel.openAddWeight()
                }
                val navController = rememberNavController()

                Box(modifier = Modifier.fillMaxSize()) {
                    PesoNavHost(navController, viewModel)
                    AddEntryBottomSheet(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_ADD, false) == true) {
            intent.removeExtra(EXTRA_OPEN_ADD)
            openAddRequest++
        }
    }

    companion object {
        const val EXTRA_OPEN_ADD = "pt.condeco.peso.EXTRA_OPEN_ADD"
    }
}

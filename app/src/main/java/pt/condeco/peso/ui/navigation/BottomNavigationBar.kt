package pt.condeco.peso.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.ui.util.label

@Composable
fun BottomNavigationBar(current: Screen, onNavigate: (Screen) -> Unit) {
    data class Item(val screen: Screen, val label: Int, val selected: ImageVector, val unselected: ImageVector)
    val items = listOf(
        Item(Screen.Dashboard, R.string.dash, Icons.Default.Dashboard, Icons.Outlined.Dashboard),
        Item(Screen.Ledger, R.string.ledger, Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
        Item(Screen.Analytics, R.string.trends, Icons.Default.BarChart, Icons.Outlined.BarChart),
        Item(Screen.Settings, R.string.config, Icons.Default.Settings, Icons.Outlined.Settings)
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            val selected = current == item.screen
            NavigationBarItem(
                icon = { Icon(if (selected) item.selected else item.unselected, contentDescription = null) },
                label = { Text(stringResource(item.label), fontSize = 10.sp) },
                selected = selected,
                onClick = { if (!selected) onNavigate(item.screen) }
            )
        }
    }
}

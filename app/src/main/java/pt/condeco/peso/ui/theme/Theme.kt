package pt.condeco.peso.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pt.condeco.peso.domain.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0056D2),
    onPrimary = Color.White,
    secondary = Color(0xFF3F51B5),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF6B7280),
    outlineVariant = Color(0xFFE0E0E0),
    secondaryContainer = Color(0xFFE8EAF6)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5B9BFF),
    onPrimary = Color(0xFF00214F),
    secondary = Color(0xFF9FA8DA),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE3E3E6),
    surface = Color(0xFF1E1F22),
    onSurface = Color(0xFFE3E3E6),
    surfaceVariant = Color(0xFF2A2C30),
    onSurfaceVariant = Color(0xFFA6ACB5),
    outlineVariant = Color(0xFF3A3C40),
    secondaryContainer = Color(0xFF2B3050)
)

@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

@Composable
fun PesoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}

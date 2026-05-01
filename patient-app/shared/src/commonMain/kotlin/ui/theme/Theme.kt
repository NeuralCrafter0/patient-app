package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MedicalBlue = Color(0xFF0061A4)
val MedicalBlueContainer = Color(0xFFD1E4FF)
val MedicalGreen = Color(0xFF006D32)
val MedicalGreenContainer = Color(0xFF98F99B)

private val LightColorScheme = lightColorScheme(
    primary = MedicalBlue,
    onPrimary = Color.White,
    primaryContainer = MedicalBlueContainer,
    onPrimaryContainer = Color(0xFF001D36),
    secondary = MedicalGreen,
    onSecondary = Color.White,
    secondaryContainer = MedicalGreenContainer,
    onSecondaryContainer = Color(0xFF00210B),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = MedicalBlueContainer,
    secondary = Color(0xFF7DDA81),
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005225),
    onSecondaryContainer = MedicalGreenContainer,
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
)

@Composable
fun PatientAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import ui.screens.DashboardScreen
import ui.theme.PatientAppTheme
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize

expect fun getPlatformName(): String

@Composable
fun App() {
    PatientAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Navigator(DashboardScreen())
        }
    }
}

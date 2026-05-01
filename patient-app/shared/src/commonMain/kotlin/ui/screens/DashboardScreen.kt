package ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import data.model.Appointment
import data.remote.NetworkService

class DashboardScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var upcomingAppointment by remember { mutableStateOf<Appointment?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            upcomingAppointment = NetworkService.fetchUpcomingAppointment()
            isLoading = false
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("DOCEase", fontWeight = FontWeight.ExtraBold) },
                    actions = {
                        IconButton(onClick = { /* Profile */ }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Booking") },
                        label = { Text("Booking") },
                        selected = false,
                        onClick = { navigator.push(DoctorListScreen()) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Records") },
                        label = { Text("Records") },
                        selected = false,
                        onClick = { /* navigator.push(RecordsScreen()) */ }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Health Overview",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(text = "Upcoming Appointment", style = MaterialTheme.typography.titleMedium)
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
                } else if (upcomingAppointment != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.DateRange, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(upcomingAppointment!!.doctorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(upcomingAppointment!!.specialty, style = MaterialTheme.typography.bodySmall)
                                Text(upcomingAppointment!!.time, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            FilledTonalButton(onClick = { navigator.push(VideoCallScreen(upcomingAppointment!!.doctorName)) }) {
                                Text("Join")
                            }
                        }
                    }
                } else {
                    Text("No upcoming appointments found.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Quick Actions", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionItem("Book Call", Icons.Default.Add) {
                        navigator.push(DoctorListScreen())
                    }
                    QuickActionItem("Prescriptions", Icons.Default.List) { }
                    QuickActionItem("Health Card", Icons.Default.AccountBox) { }
                    QuickActionItem("Emergency", Icons.Default.Warning) { }
                }
            }
        }
    }

    @Composable
    fun QuickActionItem(label: String, icon: ImageVector, onClick: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onClick() }.padding(4.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    icon, 
                    contentDescription = label, 
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

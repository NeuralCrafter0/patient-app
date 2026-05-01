package ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import data.model.Doctor
import data.remote.NetworkService

class DoctorListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                doctors = NetworkService.fetchDoctors()
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = { Text("Available Specialists", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Connection Failed", style = MaterialTheme.typography.titleMedium)
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { /* Retry */ }) { Text("Retry") }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(doctors) { doctor ->
                            ListItem(
                                headlineContent = { Text(doctor.name, fontWeight = FontWeight.SemiBold) },
                                overlineContent = { Text(doctor.specialty.uppercase()) },
                                supportingContent = { 
                                    Text(
                                        doctor.availability, 
                                        color = if (doctor.availability.contains("10")) Color(0xFF006D32) else Color.Gray 
                                    ) 
                                },
                                leadingContent = {
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AccountCircle, 
                                            contentDescription = null, 
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                },
                                trailingContent = {
                                    OutlinedButton(onClick = { /* Book */ }) {
                                        Text("Book")
                                    }
                                },
                                modifier = Modifier
                                    .clickable { /* Select */ }
                                    .fillMaxWidth()
                            )
                            Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

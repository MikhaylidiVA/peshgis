package com.walkroute.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.walkroute.app.data.repository.RouteGenerator
import com.walkroute.app.domain.model.Route
import com.walkroute.app.domain.model.RoutePoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToNavigation: (Route) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val routeGenerator = remember { RouteGenerator() }
    
    var showStepDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedSteps by remember { mutableStateOf(1000) }
    var selectedMinutes by remember { mutableStateOf(15) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WalkRoute") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Location status
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Location",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when {
                        uiState.isLocating -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Определение местоположения…",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        uiState.currentLocation != null -> {
                            Text(
                                text = "Местоположение найдено",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Широта: ${String.format("%.6f", uiState.currentLocation!!.latitude)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Долгота: ${String.format("%.6f", uiState.currentLocation!!.longitude)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        else -> {
                            Text(
                                text = "Требуется доступ к местоположению",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Route planning options
            Text(
                text = "Планирование маршрута",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showStepDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("По шагам", fontWeight = FontWeight.Bold)
                        Text("$selectedSteps шагов", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                OutlinedButton(
                    onClick = { showTimeDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("По времени", fontWeight = FontWeight.Bold)
                        Text("$selectedMinutes мин", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    uiState.currentLocation?.let { location ->
                        val route = routeGenerator.generateRouteBySteps(
                            startPoint = RoutePoint(
                                latitude = location.latitude,
                                longitude = location.longitude
                            ),
                            targetSteps = selectedSteps
                        )
                        onNavigateToNavigation(route)
                    }
                },
                enabled = uiState.currentLocation != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Начать прогулку по шагам",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    uiState.currentLocation?.let { location ->
                        val route = routeGenerator.generateRouteByTime(
                            startPoint = RoutePoint(
                                latitude = location.latitude,
                                longitude = location.longitude
                            ),
                            durationMinutes = selectedMinutes
                        )
                        onNavigateToNavigation(route)
                    }
                },
                enabled = uiState.currentLocation != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Начать прогулку по времени",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
    
    // Диалог выбора количества шагов
    if (showStepDialog) {
        AlertDialog(
            onDismissRequest = { showStepDialog = false },
            title = { Text("Выберите количество шагов") },
            text = {
                Column {
                    Slider(
                        value = selectedSteps.toFloat(),
                        onValueChange = { selectedSteps = it.toInt() },
                        valueRange = 500f..10000f,
                        steps = 18
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$selectedSteps шагов")
                }
            },
            confirmButton = {
                Button(onClick = { showStepDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Диалог выбора времени
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Выберите длительность прогулки") },
            text = {
                Column {
                    Slider(
                        value = selectedMinutes.toFloat(),
                        onValueChange = { selectedMinutes = it.toInt() },
                        valueRange = 5f..120f,
                        steps = 22
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$selectedMinutes минут")
                }
            },
            confirmButton = {
                Button(onClick = { showTimeDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

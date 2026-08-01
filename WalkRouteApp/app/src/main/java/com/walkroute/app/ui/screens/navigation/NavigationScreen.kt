package com.walkroute.app.ui.screens.navigation

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Инициализация osmdroid
    Configuration.getInstance().load(
        LocalContext.current,
        LocalContext.current.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    )
    
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Навигация") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Карта
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidMapView(
                    modifier = Modifier.fillMaxSize(),
                    route = uiState.currentRoute,
                    currentLocation = uiState.currentLocation,
                    targetWaypoint = uiState.currentWaypoint,
                    onMapCreated = { map -> mapView = map }
                )
                
                // Информация о следующей точке
                uiState.currentWaypoint?.let { waypoint ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = waypoint.instruction ?: "Следуйте по маршруту",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = buildString {
                                    append("До точки: ")
                                    append(String.format("%.0f", uiState.distanceToNextWaypoint))
                                    append(" м")
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Панель статистики
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Пройдено",
                        value = "${uiState.stepsCount} шагов"
                    )
                    StatItem(
                        label = "Расстояние",
                        value = String.format("%.2f км", uiState.distanceTraveledMeters / 1000.0)
                    )
                    StatItem(
                        label = "Время",
                        value = formatDuration(uiState.elapsedTimeSeconds)
                    )
                    StatItem(
                        label = "Осталось",
                        value = "${uiState.remainingSteps} шагов"
                    )
                }
            }
            
            // Кнопки управления
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleVoiceGuidance() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isVoiceEnabled) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (uiState.isVoiceEnabled) "Голос: ВКЛ" else "Голос: ВЫКЛ")
                }
                
                Button(
                    onClick = { viewModel.finishWalk() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Завершить")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AndroidMapView(
    modifier: Modifier = Modifier,
    route: com.walkroute.app.domain.model.Route?,
    currentLocation: com.walkroute.app.domain.model.CurrentLocation?,
    targetWaypoint: com.walkroute.app.domain.model.RoutePoint?,
    onMapCreated: (MapView) -> Unit
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                
                // Центрируем на текущей позиции или стартовой точке маршрута
                currentLocation?.let { loc ->
                    controller.setCenter(GeoPoint(loc.latitude, loc.longitude))
                } ?: route?.startPoint?.let { point ->
                    controller.setCenter(GeoPoint(point.latitude, point.longitude))
                }
                
                onMapCreated(this)
            }
        },
        modifier = modifier,
        update = { mapView ->
            mapView.overlays.clear()
            
            // Добавляем маршрут
            route?.points?.let { points ->
                if (points.isNotEmpty()) {
                    val polyline = Polyline().apply {
                        setPoints(points.map { GeoPoint(it.latitude, it.longitude) })
                        paint.apply {
                            color = Color.Blue.hashCode()
                            strokeWidth = 8f
                        }
                    }
                    mapView.overlays.add(polyline)
                    
                    // Маркер старта
                    Marker(mapView).apply {
                        position = GeoPoint(points.first().latitude, points.first().longitude)
                        title = "Старт"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(this)
                    }
                    
                    // Маркер финиша
                    Marker(mapView).apply {
                        position = GeoPoint(points.last().latitude, points.last().longitude)
                        title = "Финиш"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(this)
                    }
                }
            }
            
            // Маркер текущей позиции
            currentLocation?.let { loc ->
                Marker(mapView).apply {
                    position = GeoPoint(loc.latitude, loc.longitude)
                    title = "Вы здесь"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    mapView.overlays.add(this)
                }
                
                // Центрируем карту на текущей позиции
                mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
            }
            
            // Маркер следующей точки
            targetWaypoint?.let { point ->
                Marker(mapView).apply {
                    position = GeoPoint(point.latitude, point.longitude)
                    title = point.instruction ?: "Следующая точка"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
            }
            
            mapView.invalidate()
        }
    )
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d мин", minutes, secs)
}

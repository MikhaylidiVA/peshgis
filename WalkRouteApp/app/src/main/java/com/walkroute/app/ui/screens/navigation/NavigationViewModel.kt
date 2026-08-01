package com.walkroute.app.ui.screens.navigation

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkroute.app.data.repository.RouteGenerator
import com.walkroute.app.data.repository.StepDetector
import com.walkroute.app.domain.model.CurrentLocation
import com.walkroute.app.domain.model.Route
import com.walkroute.app.domain.model.RoutePoint
import com.walkroute.app.domain.model.WalkSession
import com.walkroute.app.domain.usecase.WalkSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class NavigationUiState(
    val currentRoute: Route? = null,
    val currentLocation: CurrentLocation? = null,
    val currentWaypoint: RoutePoint? = null,
    val waypointIndex: Int = 0,
    val stepsCount: Int = 0,
    val distanceTraveledMeters: Double = 0.0,
    val elapsedTimeSeconds: Int = 0,
    val remainingSteps: Int = 0,
    val distanceToNextWaypoint: Double = 0.0,
    val isVoiceEnabled: Boolean = true,
    val isNavigationActive: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val routeGenerator: RouteGenerator,
    private val stepCounter: StepDetector,
    private val walkSessionRepository: WalkSessionRepository,
    private val textToSpeechManager: TextToSpeechManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()
    
    private var walkSession: WalkSession? = null
    private var startTime: Long = 0
    
    init {
        startStepCounting()
        startTime = System.currentTimeMillis()
    }
    
    /**
     * Запуск навигации по сгенерированному маршруту
     */
    fun startNavigation(route: Route) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentRoute = route,
                currentWaypoint = route.points.getOrNull(1), // Первая точка после старта
                waypointIndex = 1,
                isNavigationActive = true,
                remainingSteps = route.estimatedSteps
            )
            
            // Создаем сессию прогулки
            walkSession = WalkSession(
                routeId = route.id,
                routeName = route.name,
                startTimestamp = System.currentTimeMillis(),
                targetSteps = route.estimatedSteps,
                targetDistanceMeters = route.totalDistanceMeters
            )
            
            // Голосовое приветствие
            if (_uiState.value.isVoiceEnabled) {
                textToSpeechManager.speak("Начинаем прогулку. Следуйте по маршруту.")
            }
        }
    }
    
    /**
     * Обновление текущего местоположения
     */
    fun updateLocation(location: CurrentLocation) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val route = currentState.currentRoute ?: return@launch
            
            _uiState.value = currentState.copy(currentLocation = location)
            
            // Проверяем, достигли ли следующей точки
            val currentWaypoint = currentState.currentWaypoint ?: return@launch
            val distance = routeGenerator.calculateDistanceBetween(
                RoutePoint(location.latitude, location.longitude),
                currentWaypoint
            )
            
            _uiState.value = _uiState.value.copy(
                distanceToNextWaypoint = distance
            )
            
            // Если близко к точке (менее 5 метров), переходим к следующей
            if (distance < 5.0 && currentState.waypointIndex < route.points.size - 1) {
                val nextIndex = currentState.waypointIndex + 1
                val nextWaypoint = route.points.getOrNull(nextIndex)
                
                _uiState.value = _uiState.value.copy(
                    currentWaypoint = nextWaypoint,
                    waypointIndex = nextIndex
                )
                
                // Голосовая подсказка
                if (_uiState.value.isVoiceEnabled && nextWaypoint != null) {
                    val instruction = nextWaypoint.instruction ?: "Продолжайте движение"
                    textToSpeechManager.speak(instruction)
                }
                
                // Проверяем завершение маршрута
                if (nextIndex >= route.points.size - 1) {
                    finishWalk()
                }
            }
        }
    }
    
    /**
     * Подсчет шагов
     */
    private fun startStepCounting() {
        viewModelScope.launch {
            stepCounter.getStepFlow().collect { stepEvent ->
                val currentState = _uiState.value
                val newStepsCount = currentState.stepsCount + 1
                val newRemaining = (currentState.remainingSteps - 1).coerceAtLeast(0)
                
                // Примерное расстояние на основе шагов (средняя длина шага 0.75м)
                val newDistance = newStepsCount * 0.75
                
                _uiState.value = currentState.copy(
                    stepsCount = newStepsCount,
                    distanceTraveledMeters = newDistance,
                    remainingSteps = newRemaining,
                    elapsedTimeSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                )
                
                // Проверяем, прошел ли пользователь целевое количество шагов
                if (newRemaining == 0 && currentState.currentRoute != null) {
                    textToSpeechManager.speak("Вы прошли запланированное количество шагов!")
                }
            }
        }
    }
    
    /**
     * Переключение голосовых подсказок
     */
    fun toggleVoiceGuidance() {
        val newState = !_uiState.value.isVoiceEnabled
        _uiState.value = _uiState.value.copy(isVoiceEnabled = newState)
        
        if (newState) {
            textToSpeechManager.speak("Голосовые подсказки включены")
        } else {
            textToSpeechManager.stop()
        }
    }
    
    /**
     * Завершение прогулки и сохранение в историю
     */
    fun finishWalk() {
        viewModelScope.launch {
            val session = walkSession?.copy(
                endTimestamp = System.currentTimeMillis(),
                actualSteps = _uiState.value.stepsCount,
                actualDistanceMeters = _uiState.value.distanceTraveledMeters.toInt(),
                durationSeconds = _uiState.value.elapsedTimeSeconds
            )
            
            if (session != null) {
                walkSessionRepository.saveWalkSession(session)
            }
            
            textToSpeechManager.speak("Прогулка завершена. Отличная работа!")
            
            _uiState.value = _uiState.value.copy(
                isNavigationActive = false
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        textToSpeechManager.shutdown()
    }
}

/**
 * Менеджер для управления Text-to-Speech
 */
class TextToSpeechManager @Inject constructor(
    private val context: Context
) : TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        textToSpeech = TextToSpeech(context, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("ru", "RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Пытаемся использовать английский как запасной
                textToSpeech?.setLanguage(Locale.US)
            }
            isInitialized = true
        }
    }
    
    fun speak(text: String) {
        if (isInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    fun stop() {
        textToSpeech?.stop()
    }
    
    fun shutdown() {
        textToSpeech?.shutdown()
    }
}

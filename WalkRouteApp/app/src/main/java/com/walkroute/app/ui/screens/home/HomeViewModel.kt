package com.walkroute.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkroute.app.domain.model.CurrentLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentLocation: CurrentLocation? = null,
    val isLocating: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationTracker: LocationTracker
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        startLocationUpdates()
    }
    
    private fun startLocationUpdates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLocating = true)
            
            locationTracker.getCurrentLocation().collect { location ->
                if (location != null && !location.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        currentLocation = location,
                        isLocating = false
                    )
                }
            }
        }
    }
    
    fun requestPermission() {
        // Permission request handled in UI
    }
}

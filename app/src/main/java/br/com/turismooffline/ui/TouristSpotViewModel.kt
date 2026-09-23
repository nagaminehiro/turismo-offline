package br.com.turismooffline.ui

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.turismooffline.data.AppSettings
import br.com.turismooffline.data.SettingsRepository
import br.com.turismooffline.data.TouristSpot
import br.com.turismooffline.data.TouristSpotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

data class TouristSpotUiState(
    val spots: List<TouristSpot> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isGeocoding: Boolean = false,
    val message: String? = null
)

class TouristSpotViewModel(
    private val repository: TouristSpotRepository,
    private val settingsRepository: SettingsRepository,
    private val googleMapsApiKey: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(TouristSpotUiState())
    val uiState: StateFlow<TouristSpotUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        loadSpots()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun loadSpots() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(spots = repository.getAll(), isLoading = false)
        }
    }

    fun save(spot: TouristSpot, onSaved: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, message = null)
            repository.save(spot)
            _uiState.value = _uiState.value.copy(spots = repository.getAll(), isSaving = false)
            onSaved()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            _uiState.value = _uiState.value.copy(spots = repository.getAll())
        }
    }

    fun reverseGeocode(latitude: Double, longitude: Double, context: android.content.Context, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeocoding = true, message = null)
            val result = withContext(Dispatchers.IO) { getAddress(latitude, longitude, context) }
            _uiState.value = _uiState.value.copy(isGeocoding = false)
            onResult(result)
        }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }

    override fun onCleared() {
        settingsRepository.close()
        super.onCleared()
    }

    private suspend fun getAddress(latitude: Double, longitude: Double, context: android.content.Context): String? {
        val googleAddress = withContext(Dispatchers.IO) {
            getGoogleAddress(latitude, longitude)
        }
        if (googleAddress != null) return googleAddress

        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull()?.getAddressLine(0))
                        }
                        override fun onError(errorMessage: String?) { continuation.resume(null) }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.getAddressLine(0)
            }
        } catch (_: Exception) { null }
    }

    private fun getGoogleAddress(latitude: Double, longitude: Double): String? {
        if (googleMapsApiKey.isBlank()) return null

        return runCatching {
            val lat = String.format(Locale.US, "%.7f", latitude)
            val lon = String.format(Locale.US, "%.7f", longitude)
            val url = URL(
                "https://maps.googleapis.com/maps/api/geocode/json" +
                    "?latlng=$lat,$lon&language=pt-BR&key=$googleMapsApiKey"
            )
            val response = JSONObject(url.readText())
            if (response.optString("status") != "OK") return@runCatching null
            response.optJSONArray("results")
                ?.optJSONObject(0)
                ?.optString("formatted_address")
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}

class TouristSpotViewModelFactory(
    private val repository: TouristSpotRepository,
    private val settingsRepository: SettingsRepository,
    private val googleMapsApiKey: String
) :
    androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TouristSpotViewModel(repository, settingsRepository, googleMapsApiKey) as T
}

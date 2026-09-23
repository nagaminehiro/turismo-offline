package br.com.turismooffline.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    private val _settingsFlow = MutableStateFlow(readSettings())

    val settingsFlow: Flow<AppSettings> = _settingsFlow.asStateFlow()

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_ZOOM || key == KEY_MAP_TYPE) {
            _settingsFlow.value = readSettings()
        }
    }

    fun close() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun readSettings(): AppSettings = AppSettings(
        zoom = preferences.getInt(KEY_ZOOM, DEFAULT_ZOOM).toFloat(),
        mapType = preferences.getString(KEY_MAP_TYPE, DEFAULT_MAP_TYPE) ?: DEFAULT_MAP_TYPE
    )

    companion object {
        const val KEY_ZOOM = "default_zoom"
        const val KEY_MAP_TYPE = "map_type"
        const val DEFAULT_ZOOM = 12
        const val DEFAULT_MAP_TYPE = "NORMAL"
    }
}

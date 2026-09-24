package br.com.turismooffline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import br.com.turismooffline.data.TouristSpotDbHelper
import br.com.turismooffline.data.TouristSpotRepository
import br.com.turismooffline.data.SettingsRepository
import br.com.turismooffline.BuildConfig
import br.com.turismooffline.ui.TouristSpotViewModel
import br.com.turismooffline.ui.TouristSpotViewModelFactory
import br.com.turismooffline.ui.TurismoOfflineApp
import br.com.turismooffline.ui.theme.TurismoOfflineTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TouristSpotViewModel by viewModels {
        TouristSpotViewModelFactory(
            TouristSpotRepository(TouristSpotDbHelper(applicationContext)),
            SettingsRepository(applicationContext),
            BuildConfig.MAPS_API_KEY
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TurismoOfflineTheme { TurismoOfflineApp(viewModel) }
        }
    }
}

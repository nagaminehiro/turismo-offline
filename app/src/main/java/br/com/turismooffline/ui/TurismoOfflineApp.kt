package br.com.turismooffline.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.turismooffline.data.AppSettings
import br.com.turismooffline.data.TouristSpot
import br.com.turismooffline.ui.SettingsActivity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale

private enum class AppTab(val label: String) { LIST("Pontos"), MAP("Mapa"), SETTINGS("Configurações") }

@Composable
fun TurismoOfflineApp(viewModel: TouristSpotViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AppTab.LIST) }
    var formVisible by remember { mutableStateOf(false) }
    var editingSpot by remember { mutableStateOf<TouristSpot?>(null) }
    var spotToDelete by remember { mutableStateOf<TouristSpot?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    BackHandler(enabled = formVisible) {
        formVisible = false
        editingSpot = null
    }

    if (formVisible) {
        SpotFormScreen(
            initialSpot = editingSpot,
            isSaving = uiState.isSaving,
            isGeocoding = uiState.isGeocoding,
            viewModel = viewModel,
            onCancel = {
                formVisible = false
                editingSpot = null
            },
            onSave = { spot ->
                viewModel.save(spot) {
                    formVisible = false
                    editingSpot = null
                }
            }
        )
        return
    }

    Scaffold(
        topBar = { AppTopBar(selectedTab) },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AppTab.LIST -> Icons.Default.Place
                                    AppTab.MAP -> Icons.Default.Map
                                    AppTab.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == AppTab.LIST) {
                FloatingActionButton(onClick = {
                    editingSpot = null
                    formVisible = true
                }) { Icon(Icons.Default.Add, contentDescription = "Adicionar ponto") }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (selectedTab) {
            AppTab.LIST -> SpotListScreen(
                spots = uiState.spots,
                isLoading = uiState.isLoading,
                contentPadding = padding,
                onEdit = { editingSpot = it; formVisible = true },
                onDelete = { spotToDelete = it }
            )
            AppTab.MAP -> MapScreen(uiState.spots, uiState.settings, padding)
            AppTab.SETTINGS -> SettingsScreen(
                settings = uiState.settings,
                contentPadding = padding,
                onOpenSettings = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            )
        }
    }

    spotToDelete?.let { spot ->
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            title = { Text("Excluir ponto turístico?") },
            text = { Text("O cadastro de ${spot.name} será removido do dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(spot.id)
                    spotToDelete = null
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { spotToDelete = null }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(tab: AppTab) {
    TopAppBar(
        title = {
            Column {
                Text("Marco", fontWeight = FontWeight.Bold)
                if (tab == AppTab.LIST) Text("Seus pontos turísticos", style = MaterialTheme.typography.labelMedium)
            }
        }
    )
}

@Composable
private fun SpotListScreen(
    spots: List<TouristSpot>,
    isLoading: Boolean,
    contentPadding: PaddingValues,
    onEdit: (TouristSpot) -> Unit,
    onDelete: (TouristSpot) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (spots.isEmpty()) {
        EmptyState(Modifier.fillMaxSize().padding(contentPadding))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("${spots.size} ${if (spots.size == 1) "ponto cadastrado" else "pontos cadastrados"}", style = MaterialTheme.typography.titleMedium)
            }
            items(spots, key = { it.id }) { spot ->
                SpotCard(spot, onEdit = { onEdit(spot) }, onDelete = { onDelete(spot) })
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Nenhum ponto cadastrado", style = MaterialTheme.typography.titleLarge)
        Text("Use o botão + para adicionar seu primeiro destino.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SpotCard(spot: TouristSpot, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SpotImage(spot.imageBytes, spot.imageUri, Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(spot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(spot.address ?: "Endereço ainda não consultado", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${formatCoordinate(spot.latitude)}, ${formatCoordinate(spot.longitude)}", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(spot.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun MapScreen(spots: List<TouristSpot>, settings: AppSettings, contentPadding: PaddingValues) {
    val fallback = LatLng(-14.2350, -51.9253)
    val firstPosition = spots.firstOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(firstPosition, settings.zoom)
    }
    LaunchedEffect(firstPosition, settings.zoom) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(firstPosition, settings.zoom)
    }

    Box(Modifier.fillMaxSize().padding(contentPadding)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = mapTypeFrom(settings.mapType)),
            uiSettings = MapUiSettings(zoomControlsEnabled = true, compassEnabled = true)
        ) {
            spots.forEach { spot ->
                androidx.compose.runtime.key(spot.id) {
                    Marker(
                        state = remember { MarkerState(position = LatLng(spot.latitude, spot.longitude)) },
                        title = spot.name,
                        snippet = spot.address ?: spot.description
                    )
                }
            }
        }

        if (spots.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nenhum ponto cadastrado", fontWeight = FontWeight.Bold)
                    Text("Adicione um ponto para exibir seu marcador no mapa.")
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    contentPadding: PaddingValues,
    onOpenSettings: () -> Unit
) {
    val mapTypeLabel = when (settings.mapType) {
        "SATELLITE" -> "Satélite"
        "TERRAIN" -> "Terreno"
        "HYBRID" -> "Híbrido"
        else -> "Rodoviário"
    }

    Column(
        Modifier.fillMaxSize().padding(contentPadding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Configurações do mapa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "As preferências são mantidas pelo Jetpack Preferences e não fazem parte do banco SQLite.",
            style = MaterialTheme.typography.bodyMedium
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Preferências atuais", style = MaterialTheme.typography.titleMedium)
                Text("Zoom padrão: ${settings.zoom.toInt()}")
                Text("Tipo de mapa: $mapTypeLabel")
            }
        }
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Abrir preferências")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpotFormScreen(
    initialSpot: TouristSpot?,
    isSaving: Boolean,
    isGeocoding: Boolean,
    viewModel: TouristSpotViewModel,
    onCancel: () -> Unit,
    onSave: (TouristSpot) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember(initialSpot?.id) { mutableStateOf(initialSpot?.name.orEmpty()) }
    var description by remember(initialSpot?.id) { mutableStateOf(initialSpot?.description.orEmpty()) }
    var latitude by remember(initialSpot?.id) { mutableStateOf(initialSpot?.latitude?.toString().orEmpty()) }
    var longitude by remember(initialSpot?.id) { mutableStateOf(initialSpot?.longitude?.toString().orEmpty()) }
    var address by remember(initialSpot?.id) { mutableStateOf(initialSpot?.address.orEmpty()) }
    var imageBytes by remember(initialSpot?.id) { mutableStateOf(initialSpot?.imageBytes) }
    var imageUri by remember(initialSpot?.id) { mutableStateOf(initialSpot?.imageUri) }
    var error by remember { mutableStateOf<String?>(null) }

    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val shouldTrackLocation = initialSpot == null
    var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission()) }
    var coordinatesEditedByUser by remember(initialSpot?.id) { mutableStateOf(false) }
    var addressRequested by remember(initialSpot?.id) { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions.values.any { it }
        if (!locationPermissionGranted) error = "Permissão de localização recusada."
    }

    LaunchedEffect(Unit) {
        if (shouldTrackLocation && !locationPermissionGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(locationManager, locationPermissionGranted) {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (coordinatesEditedByUser) return
                latitude = String.format(Locale.US, "%.7f", location.latitude)
                longitude = String.format(Locale.US, "%.7f", location.longitude)
                if (!addressRequested) {
                    addressRequested = true
                    viewModel.reverseGeocode(location.latitude, location.longitude, context) { result ->
                        address = result ?: "Endereço não encontrado"
                    }
                }
            }
        }

        if (shouldTrackLocation && locationPermissionGranted) {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .filter { locationManager.isProviderEnabled(it) }
            if (providers.isEmpty()) error = "Ative a localização do celular para obter as coordenadas."
            try {
                providers.mapNotNull { locationManager.getLastKnownLocation(it) }
                    .maxByOrNull { it.time }
                    ?.let { locationListener.onLocationChanged(it) }
                providers.forEach { provider ->
                    locationManager.requestLocationUpdates(
                        provider,
                        0,
                        0f,
                        locationListener
                    )
                }
            } catch (_: SecurityException) {
                error = "Não foi possível acessar a localização."
            }
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap ?: return@rememberLauncherForActivityResult
        imageBytes = bitmapToByteArray(bitmap)
        imageUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else error = "Permissão da câmera recusada."
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }
            imageBytes = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull()
            }
            imageUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialSpot == null) "Novo ponto turístico" else "Editar ponto turístico") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Voltar") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nome do ponto turístico") }, singleLine = true)
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth().height(120.dp), label = { Text("Descrição") }, minLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(latitude, { latitude = it; coordinatesEditedByUser = true }, Modifier.weight(1f), label = { Text("Latitude") }, singleLine = true)
                OutlinedTextField(longitude, { longitude = it; coordinatesEditedByUser = true }, Modifier.weight(1f), label = { Text("Longitude") }, singleLine = true)
            }
            OutlinedButton(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lon = longitude.toDoubleOrNull()
                    if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                        error = "Informe latitude (-90 a 90) e longitude (-180 a 180) válidas."
                    } else {
                        viewModel.reverseGeocode(lat, lon, context) { result ->
                            address = result ?: "Endereço não encontrado"
                        }
                    }
                },
                enabled = !isGeocoding,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGeocoding) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Place, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Buscar endereço")
            }
            if (address.isNotBlank()) Text("Endereço: $address", style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) cameraLauncher.launch(null)
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tirar foto")
                }
                OutlinedButton(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (imageBytes == null && imageUri == null) "Escolher imagem" else "Trocar imagem")
                }
            }
            if (imageBytes != null || imageUri != null) {
                SpotImage(
                    imageBytes,
                    imageUri,
                    Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lon = longitude.toDoubleOrNull()
                    when {
                        name.isBlank() -> error = "Informe o nome do ponto."
                        description.isBlank() -> error = "Informe uma descrição."
                        lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0 -> error = "Informe coordenadas válidas."
                        else -> onSave(
                            TouristSpot(
                                id = initialSpot?.id ?: 0,
                                name = name.trim(),
                                description = description.trim(),
                                latitude = lat,
                                longitude = lon,
                                address = address.ifBlank { null },
                                imageBytes = imageBytes,
                                imageUri = imageUri
                            )
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(if (initialSpot == null) "Cadastrar ponto" else "Salvar alterações")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SpotImage(imageBytes: ByteArray?, uri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = imageBytes?.contentHashCode(),
        key2 = uri
    ) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                imageBytes?.let { bytes ->
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } ?: uri?.let {
                    context.contentResolver.openInputStream(Uri.parse(it))?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(bitmap!!.asImageBitmap(), contentDescription = "Imagem do ponto", modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

private fun formatCoordinate(value: Double) = "%.5f".format(Locale.getDefault(), value)

private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}

private fun mapTypeFrom(value: String): MapType = when (value) {
    "SATELLITE" -> MapType.SATELLITE
    "TERRAIN" -> MapType.TERRAIN
    "HYBRID" -> MapType.HYBRID
    else -> MapType.NORMAL
}

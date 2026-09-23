# Turismo Offline

Aplicativo Android para cadastro local de pontos turísticos, desenvolvido com Kotlin, Jetpack Compose e arquitetura MVVM.

## Funcionalidades

- CRUD completo de pontos turísticos.
- Cadastro de nome, descrição, latitude, longitude e imagem.
- Captura de imagem pela câmera ou seleção da galeria; a imagem é convertida para `ByteArray` e persistida no SQLite como BLOB.
- Preenchimento das coordenadas pelo GPS do aparelho, com solicitação de permissão em tempo de execução.
- Conversão de coordenadas em endereço textual usando a API Google Geocoding, com fallback para o `Geocoder` do Android.
- Persistência local com SQLite (`SQLiteOpenHelper`), funcionando sem backend.
- Tela de mapa com marcadores usando Google Maps Compose.
- Configuração persistente do zoom inicial e do tipo de mapa: rodoviário, satélite, terreno ou híbrido.

## Como abrir no Android Studio

1. Abra a pasta `turismo-offline` no Android Studio.
2. Crie um arquivo `local.properties` na raiz do projeto, caso ele ainda não exista, com a chave do Google Maps:

   ```properties
   MAPS_API_KEY=SUA_CHAVE_DO_GOOGLE_MAPS_E_GEOCODING
   ```

3. Ative o Maps SDK for Android e a Geocoding API no projeto do Google Cloud e aguarde a sincronização do Gradle.
4. Execute em um emulador ou dispositivo com Google Play Services.

O cadastro, as imagens e as configurações ficam no armazenamento local do aparelho. A consulta de endereço e os mapas dependem de conectividade no momento da consulta; os dados já salvos continuam disponíveis offline.

## Organização MVVM

- `MainActivity`: host mínimo do Compose e criação do ViewModel.
- `ui/TouristSpotViewModel.kt`: estado da tela, validações e eventos.
- `ui/TurismoOfflineApp.kt`: telas e componentes Compose.
- `data/TouristSpotRepository.kt`: acesso aos dados em background.
- `data/TouristSpotDbHelper.kt`: schema e operações SQLite.
- `data/SettingsRepository.kt`: leitura das preferências do mapa mantidas pela biblioteca Jetpack Preferences.

As configurações são editadas na tela `SettingsActivity`, usando `PreferenceFragmentCompat` e `root_preferences.xml`. Elas ficam no arquivo de preferências da aplicação e não na tabela SQLite.

As configurações de zoom e tipo de mapa não são gravadas na tabela SQLite. Elas ficam no arquivo de preferências mantido pelo Jetpack Preferences, separado do banco de pontos turísticos.

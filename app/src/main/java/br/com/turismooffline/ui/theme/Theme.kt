package br.com.turismooffline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import br.com.turismooffline.R

val VerdeMata = Color(0xFF0E6B5C)
val MataProfunda = Color(0xFF094A40)
val Sol = Color(0xFFF5A524)
val Ceu = Color(0xFF7FC4E8)
val Areia = Color(0xFFFBF6EC)
val Carvao = Color(0xFF1C2B28)
private val CarvaoSuave = Color(0xFF4A5A56)
private val Borda = Color(0xFFE6DCC8)

private val TurismoColors = lightColorScheme(
    primary = VerdeMata,
    onPrimary = Color.White,
    // O FAB usa primaryContainer: botão de cadastrar fica na cor Sol.
    primaryContainer = Sol,
    onPrimaryContainer = Carvao,
    inversePrimary = Ceu,
    secondary = MataProfunda,
    onSecondary = Color.White,
    secondaryContainer = Ceu,
    onSecondaryContainer = Carvao,
    tertiary = Sol,
    onTertiary = Carvao,
    tertiaryContainer = Color(0xFFFDE7C0),
    onTertiaryContainer = Carvao,
    background = Areia,
    onBackground = Carvao,
    surface = Areia,
    onSurface = Carvao,
    surfaceVariant = Color(0xFFF1E9D8),
    onSurfaceVariant = CarvaoSuave,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF5EEDF),
    surfaceContainerHigh = Color(0xFFF1E9D8),
    surfaceContainerHighest = Color(0xFFECE3D0),
    outline = Color(0xFF8A958F),
    outlineVariant = Borda
)

val BricolageGrotesque = FontFamily(
    Font(R.font.bricolage_grotesque_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_grotesque_bold, FontWeight.Bold)
)

val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.Bold)
)

private fun TextStyle.titulo() = copy(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold)
private fun TextStyle.subtitulo() = copy(fontFamily = BricolageGrotesque, fontWeight = FontWeight.SemiBold)
private fun TextStyle.texto() = copy(fontFamily = DmSans)

private val TurismoTypography = Typography().run {
    copy(
        displayLarge = displayLarge.titulo(),
        displayMedium = displayMedium.titulo(),
        displaySmall = displaySmall.titulo(),
        headlineLarge = headlineLarge.titulo(),
        headlineMedium = headlineMedium.titulo(),
        headlineSmall = headlineSmall.titulo(),
        titleLarge = titleLarge.titulo(),
        titleMedium = titleMedium.subtitulo(),
        titleSmall = titleSmall.subtitulo(),
        bodyLarge = bodyLarge.texto(),
        bodyMedium = bodyMedium.texto(),
        bodySmall = bodySmall.texto(),
        labelLarge = labelLarge.texto(),
        labelMedium = labelMedium.texto(),
        labelSmall = labelSmall.texto()
    )
}

@Composable
fun TurismoOfflineTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TurismoColors, typography = TurismoTypography, content = content)
}

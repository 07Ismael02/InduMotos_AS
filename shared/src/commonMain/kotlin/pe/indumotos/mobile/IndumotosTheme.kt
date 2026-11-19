package pe.indumotos.mobile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF171A1F), onPrimary = Color.White,
    secondary = Color(0xFF244F7B), onSecondary = Color.White,
    background = Color(0xFFF4F5F7), onBackground = Color(0xFF171A1F),
    surface = Color.White, onSurface = Color(0xFF171A1F),
    onSurfaceVariant = Color(0xFF505C6C),
    primaryContainer = Color(0xFFF6F3CD), onPrimaryContainer = Color(0xFF171A1F),
    secondaryContainer = Color(0xFFE6EDF5), onSecondaryContainer = Color(0xFF183C61),
    outline = Color(0xFF737D89), outlineVariant = Color(0xFFD2D8E0),
    surfaceTint = Color.Transparent
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFE4D92B), onPrimary = Color(0xFF171A1F),
    secondary = Color(0xFFA6CCF5), onSecondary = Color(0xFF123354),
    background = Color(0xFF111419), onBackground = Color(0xFFE5E8EF),
    surface = Color(0xFF1B2027), onSurface = Color(0xFFE5E8EF),
    onSurfaceVariant = Color(0xFFB9C2CF),
    primaryContainer = Color(0xFF494612), onPrimaryContainer = Color(0xFFF6F3CD),
    secondaryContainer = Color(0xFF243D58), onSecondaryContainer = Color(0xFFD3E5FC),
    outline = Color(0xFF8994A3), outlineVariant = Color(0xFF414B58),
    surfaceTint = Color.Transparent
)

@Composable
fun IndumotosTheme(darkTheme:Boolean=isSystemInDarkTheme(), content:@Composable ()->Unit) {
    MaterialTheme(
        colorScheme = if(darkTheme) DarkColors else LightColors,
        shapes = Shapes(small=RoundedCornerShape(8.dp),medium=RoundedCornerShape(12.dp),large=RoundedCornerShape(18.dp)),
        content = content
    )
}

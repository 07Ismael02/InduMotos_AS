package pe.indumotos.mobile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF194B80), onPrimary = Color.White,
    secondary = Color(0xFF193D61), onSecondary = Color.White,
    background = Color(0xFFF4F7FB), onBackground = Color(0xFF142E49),
    surface = Color.White, onSurface = Color(0xFF142E49),
    onSurfaceVariant = Color(0xFF536B85),
    primaryContainer = Color(0xFFF6F3CD), onPrimaryContainer = Color(0xFF142E49),
    secondaryContainer = Color(0xFFE6EDF5), onSecondaryContainer = Color(0xFF183C61),
    outline = Color(0xFF737D89), outlineVariant = Color(0xFFD3DFEC),
    surfaceTint = Color.Transparent
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFEEE51B), onPrimary = Color(0xFF142E49),
    secondary = Color(0xFFA8CFFF), onSecondary = Color(0xFF123354),
    background = Color(0xFF0E1823), onBackground = Color(0xFFF1F5FB),
    surface = Color(0xFF1B2A3B), onSurface = Color(0xFFF1F5FB),
    onSurfaceVariant = Color(0xFFB3C5D9),
    primaryContainer = Color(0xFF494612), onPrimaryContainer = Color(0xFFF6F3CD),
    secondaryContainer = Color(0xFF243D58), onSecondaryContainer = Color(0xFFD3E5FC),
    outline = Color(0xFF8994A3), outlineVariant = Color(0xFF3B5169),
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

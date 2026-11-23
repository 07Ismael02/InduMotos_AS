package pe.indumotos.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

// Android supplies its lifecycle-aware BackHandler; shared UI keeps navigation decisions.
typealias SystemBackInstaller = @Composable (Boolean, () -> Unit) -> Unit
internal val LocalSystemBack = staticCompositionLocalOf<SystemBackInstaller> { { _, _ -> } }
internal val LocalReturnHome = staticCompositionLocalOf<() -> Unit> { {} }

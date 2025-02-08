package net.tactware.nimbus.appwide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides spacing values for the Herc theme.
 *
 * @property default The default should provide a 0 dp value.
 * @property tiny The tiny amount of space used throughout the application.
 * @property extraSmall The extra small amount of spacing throughout the application.
 * @property small The small amount of spacing throughout the application.
 * @property medium The medium amount of spacing throughout the application.
 * @property large The large amount of spacing throughout the application.
 * @constructor Create [Spacing]
 */
data class Spacing(
    val default: Dp = SpacingDefaults.DEFAULT.dp,
    val tiny: Dp = SpacingDefaults.TINY.dp,
    val extraSmall: Dp = SpacingDefaults.EXTRA_SMALL.dp,
    val small: Dp = SpacingDefaults.SMALL.dp,
    val medium: Dp = SpacingDefaults.MEDIUM.dp,
    val large: Dp = SpacingDefaults.LARGE.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

typealias Theme = MaterialTheme

val Theme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

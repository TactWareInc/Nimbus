package net.tactware.nimbus.appwide.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides spacing values for the desktop theme.
 * Uses larger spacing values compared to the common theme to better suit desktop screens.
 *
 * @property default The default should provide a 0 dp value.
 * @property tiny The tiny amount of space used throughout the application.
 * @property extraSmall The extra small amount of spacing throughout the application.
 * @property small The small amount of spacing throughout the application.
 * @property medium The medium amount of spacing throughout the application.
 * @property large The large amount of spacing throughout the application.
 * @constructor Create [DesktopSpacing]
 */
data class DesktopSpacing(
    val default: Dp = DesktopSpacingDefaults.DEFAULT.dp,
    val tiny: Dp = DesktopSpacingDefaults.TINY.dp,
    val extraSmall: Dp = DesktopSpacingDefaults.EXTRA_SMALL.dp,
    val small: Dp = DesktopSpacingDefaults.SMALL.dp,
    val medium: Dp = DesktopSpacingDefaults.MEDIUM.dp,
    val large: Dp = DesktopSpacingDefaults.LARGE.dp,
)

val LocalDesktopSpacing = compositionLocalOf { DesktopSpacing() }
package net.tactware.nimbus.appwide.ui.theme

/**
 * Specifies amount of spacing that should be used for desktop applications.
 * Desktop applications typically use smaller spacing values because mouse input
 * is more precise than touch input, allowing for more compact UI elements.
 */
data object DesktopSpacingDefaults {
    internal const val TINY = 1       // Decreased from 2
    internal const val EXTRA_SMALL = 2  // Decreased from 4
    internal const val SMALL = 4       // Decreased from 8
    internal const val MEDIUM = 8      // Decreased from 16
    internal const val LARGE = 16       // Decreased from 32
    internal const val DEFAULT = SMALL
}

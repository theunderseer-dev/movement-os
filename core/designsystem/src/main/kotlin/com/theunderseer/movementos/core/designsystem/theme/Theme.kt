package com.theunderseer.movementos.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview

/**
 * Root theme for MovementOS.
 *
 * Wraps [MaterialTheme] with project tokens and exposes [LocalSpacing] via CompositionLocal.
 *
 * @param darkTheme follow system by default; pass explicit value for previews
 */
@Composable
fun MovementOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MovementOSTypography,
            shapes = MovementOSShapes,
            content = content,
        )
    }
}

/**
 * Preview helper rendering content in both light and dark themes.
 *
 * Use on composable previews:
 * ```
 * @ThemePreviews
 * @Composable
 * private fun MyComponentPreview() { ... }
 * ```
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews

/**
 * Wrapper for component previews — applies theme + surface background.
 */
@Composable
fun PreviewSurface(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MovementOSTheme(darkTheme = darkTheme) {
        Surface(content = content)
    }
}

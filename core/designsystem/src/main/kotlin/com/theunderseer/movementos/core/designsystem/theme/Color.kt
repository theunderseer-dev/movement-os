package com.theunderseer.movementos.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Monochrome scale — base of the system
private val White = Color(0xFFFFFFFF)
private val Stone50 = Color(0xFFFAFAF9) // page background
private val Stone100 = Color(0xFFF5F5F4) // surface
private val Stone200 = Color(0xFFE7E5E4) // borders, dividers

// private val Stone300 = Color(0xFFD6D3D1) // disabled
private val Stone400 = Color(0xFFA8A29E) // secondary icons
private val Stone500 = Color(0xFF78716C) // secondary text
private val Stone600 = Color(0xFF57534E) // body text
private val Stone700 = Color(0xFF44403C) // headlines
private val Stone800 = Color(0xFF292524) // dark surface
private val Stone900 = Color(0xFF1C1917) // dark background
private val Black = Color(0xFF0A0A0A)

// Pastel accent — soft sage for movement / wellness association
private val Sage50 = Color(0xFFF1F4F1)

// private val Sage100 = Color(0xFFE0E7DF)
private val Sage300 = Color(0xFFA8B8A4)
private val Sage500 = Color(0xFF7A8E76)
private val Sage700 = Color(0xFF4A5A48)

/**
 * Light color scheme — monochrome base with soft sage accent.
 * Inspired by Apple HIG: hierarchy via shades of gray, not color contrast.
 */
internal val LightColorScheme =
    lightColorScheme(
        primary = Stone900,
        onPrimary = White,
        primaryContainer = Stone100,
        onPrimaryContainer = Stone900,
        secondary = Sage500,
        onSecondary = White,
        secondaryContainer = Sage50,
        onSecondaryContainer = Sage700,
        tertiary = Stone500,
        onTertiary = White,
        tertiaryContainer = Stone100,
        onTertiaryContainer = Stone700,
        background = Stone50,
        onBackground = Stone900,
        surface = White,
        onSurface = Stone900,
        surfaceVariant = Stone100,
        onSurfaceVariant = Stone600,
        outline = Stone200,
        outlineVariant = Stone100,
        error = Color(0xFFB91C1C),
        onError = White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF7F1D1D),
    )

/**
 * Dark color scheme — near-black with cool gray hierarchy.
 */
internal val DarkColorScheme =
    darkColorScheme(
        primary = White,
        onPrimary = Stone900,
        primaryContainer = Stone800,
        onPrimaryContainer = Stone50,
        secondary = Sage300,
        onSecondary = Stone900,
        secondaryContainer = Sage700,
        onSecondaryContainer = Sage50,
        tertiary = Stone400,
        onTertiary = Stone900,
        tertiaryContainer = Stone800,
        onTertiaryContainer = Stone100,
        background = Black,
        onBackground = Stone50,
        surface = Stone900,
        onSurface = Stone50,
        surfaceVariant = Stone800,
        onSurfaceVariant = Stone400,
        outline = Stone700,
        outlineVariant = Stone800,
        error = Color(0xFFEF4444),
        onError = Stone900,
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFEE2E2),
    )

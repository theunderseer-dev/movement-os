package com.theunderseer.movementos.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale for MovementOS.
 *
 * Follows Material 3 type scale — use [androidx.compose.material3.MaterialTheme.typography] in UI.
 *
 * Naming guide:
 * - display* — hero numbers, splash screens (rarely used)
 * - headline* — page titles
 * - title* — section titles, card headers
 * - body* — paragraph text, descriptions
 * - label* — buttons, chips, captions
 */
internal val MovementOSTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Light,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-1.5).sp,
            ),
        displayMedium =
            TextStyle(
                fontWeight = FontWeight.Light,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = (-1.0).sp,
            ),
        headlineLarge =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.5).sp,
            ),
        headlineMedium =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.25).sp,
            ),
        titleLarge =
            TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
        bodyLarge =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp, // iOS body
                lineHeight = 24.sp,
                letterSpacing = (-0.2).sp,
            ),
        labelLarge =
            TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp, // buttons
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
            ),
    )

package com.theunderseer.movementos.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing tokens following 4dp grid.
 *
 * Access via [LocalSpacing.current] inside composables:
 * ```
 * Modifier.padding(LocalSpacing.current.md)
 * ```
 *
 * Never use raw dp values in feature modules — always reach for tokens.
 */
@Immutable
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

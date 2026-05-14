package com.theunderseer.movementos.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radius scale for MovementOS components.
 *
 * - extraSmall (4dp) — chips, small badges
 * - small (8dp) — buttons, text fields
 * - medium (12dp) — cards
 * - large (16dp) — bottom sheets, dialogs
 * - extraLarge (28dp) — hero containers
 */
internal val MovementOSShapes =
    Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(10.dp), // iOS button radius
        medium = RoundedCornerShape(14.dp),
        large = RoundedCornerShape(20.dp), // iOS card
        extraLarge = RoundedCornerShape(32.dp),
    )

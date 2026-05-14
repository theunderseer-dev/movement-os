package com.theunderseer.movementos.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theunderseer.movementos.core.designsystem.theme.LocalSpacing
import com.theunderseer.movementos.core.designsystem.theme.MovementOSTheme
import com.theunderseer.movementos.core.designsystem.theme.ThemePreviews

/**
 * Primary call-to-action button.
 *
 * Uses [MaterialTheme.colorScheme.primary] background. For secondary actions, use [OutlineButton].
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val spacing = LocalSpacing.current
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        contentPadding =
            PaddingValues(
                horizontal = spacing.xl,
                vertical = spacing.md + 2.dp,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@ThemePreviews
@Composable
private fun PrimaryButtonPreview() {
    MovementOSTheme {
        PrimaryButton(text = "Start session", onClick = {})
    }
}

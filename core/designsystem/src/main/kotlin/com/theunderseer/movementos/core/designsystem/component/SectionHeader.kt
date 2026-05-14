package com.theunderseer.movementos.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.theunderseer.movementos.core.designsystem.theme.LocalSpacing
import com.theunderseer.movementos.core.designsystem.theme.MovementOSTheme
import com.theunderseer.movementos.core.designsystem.theme.ThemePreviews

/**
 * Header for a content section — title with optional subtitle.
 *
 * Use at the top of grouped content blocks (Today's session, This week, Progress).
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val spacing = LocalSpacing.current
    Column(modifier = modifier.padding(vertical = spacing.md)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.xs),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SectionHeaderPreview() {
    MovementOSTheme {
        SectionHeader(
            title = "Today's session",
            subtitle = "Mobility for tight hips · 20 min",
        )
    }
}

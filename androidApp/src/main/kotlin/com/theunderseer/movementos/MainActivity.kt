package com.theunderseer.movementos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.theunderseer.movementos.core.designsystem.component.PrimaryButton
import com.theunderseer.movementos.core.designsystem.theme.MovementOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovementOSTheme {
                PrimaryButton(text = "MovementOS", onClick = {})
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

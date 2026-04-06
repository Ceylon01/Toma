package com.capstone.toma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.capstone.toma.ui.TomaApp
import com.capstone.toma.ui.theme.TomaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomaTheme(darkTheme = false, dynamicColor = false) {
                TomaApp()
            }
        }
    }
}

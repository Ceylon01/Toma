package com.capstone.toma.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.toma.ui.theme.*

@Composable
fun FirstLaunchIntroScreen(
    onStartEnrollment: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TomaBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "토마에 오신 것을 환영합니다! 🍅",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TomaPrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "안정적인 '헤이 토마' 호출을 위해\\n사용자의 목소리 등록이 필요합니다.",
            fontSize = 16.sp,
            color = TomaSecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartEnrollment,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TomaMainOrange),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("목소리 등록 시작하기", color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSkip) {
            Text("나중에 할게요", color = TomaSecondaryText)
        }
    }
}

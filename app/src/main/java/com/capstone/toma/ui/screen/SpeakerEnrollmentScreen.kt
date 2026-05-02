package com.capstone.toma.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.toma.VoiceUiState
import com.capstone.toma.ui.theme.*

@Composable
fun SpeakerEnrollmentScreen(
    uiState: VoiceUiState,
    onCancel: () -> Unit
) {
    val enrollingState = uiState as? VoiceUiState.Enrolling
    val currentCount = enrollingState?.currentCount ?: 0
    val totalCount = enrollingState?.totalCount ?: 30
    val progress = currentCount.toFloat() / totalCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TomaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "목소리 등록하기",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TomaPrimaryText
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "정확한 호출어 인식을 위해\n'헤이 토마'를 30번 말씀해 주세요.",
            fontSize = 16.sp,
            color = TomaSecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // 중앙 녹음 애니메이션 영역
        Box(contentAlignment = Alignment.Center) {
            EnrollmentWaveAnimation(isActive = uiState is VoiceUiState.Enrolling)
            Surface(
                shape = CircleShape,
                color = TomaMainOrange,
                modifier = Modifier.size(120.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$currentCount / $totalCount",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 하단 상태 및 진행바
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val statusText = when (uiState) {
                is VoiceUiState.Enrolling -> "지금 말씀하세요..."
                VoiceUiState.Uploading -> "데이터를 전송 중입니다..."
                VoiceUiState.Training -> "학습 중입니다. 잠시만 기다려주세요."
                else -> ""
            }

            Text(
                text = statusText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TomaMainOrange
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = TomaMainOrange,
                trackColor = TomaMainOrange.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            TextButton(onClick = onCancel) {
                Text("나중에 할래요", color = TomaSecondaryText)
            }
        }
    }
}

@Composable
fun EnrollmentWaveAnimation(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    if (isActive) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(TomaMainOrange.copy(alpha = alpha), CircleShape)
        )
    }
}

package com.capstone.toma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TomaMainRed = Color(0xFFFF5722)
val TomaLightRed = Color(0xFFFFEAEA)
val TomaBrown = Color(0xFF8D6E63)
val TomaIosGray = Color(0xFFF2F2F7)
val TomaSecondaryText = Color(0xFF8E8E93)
val TomaIosLinkBlue = Color(0xFFFF9F43)
val TomaQuickAnalysisBlue = Color(0xFFE3F2FD)
val TomaQuickAnalysisGreen = Color(0xFFE8F5E9)

val LoadingGradient = Brush.sweepGradient(
    colors = listOf(Color(0xFFFF3B30), Color(0xFF007AFF), Color(0xFFFF3B30)),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomaRecipeAnalysisScreen()
        }
    }
}

@Composable
fun TomaRecipeAnalysisScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TomaIosGray)
            .padding(vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // padding 에러가 났던 부분을 분리해서 수정했습니다!
        Text(
            text = "레시피 분석 페이지",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp)
        )

        TomaTopAppBar()
        Spacer(modifier = Modifier.height(32.dp))
        LoadingSection()
        Spacer(modifier = Modifier.height(32.dp))
        LinkInputSection()
        Spacer(modifier = Modifier.height(24.dp))
        ScanOptionsSection()
        Spacer(modifier = Modifier.height(32.dp))
        QuickAnalysisSection()
        Spacer(modifier = Modifier.height(32.dp))
        RecentAnalysisSection()
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun TomaTopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(TomaMainRed, CircleShape)
        )
        Text(
            text = "To-ma",
            color = TomaBrown,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun LoadingSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(6.dp, LoadingGradient, CircleShape)
                .shadow(2.dp, CircleShape, spotColor = Color.Black.copy(0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BrightnessAuto,
                contentDescription = "Magic Stars",
                tint = Color(0xFFFF9F43),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "요리의 마법을 분석 중입니다",
            color = Color.Black,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "제미나이가 영상에서 재료, 단계, 영양 정보를\n추출하고 있어요...",
            color = TomaSecondaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun LinkInputSection() {
    var linkText by remember { mutableStateOf("") }

    TextField(
        value = linkText,
        onValueChange = { linkText = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(56.dp)
            .shadow(
                4.dp,
                RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        placeholder = {
            Text(
                text = "유튜브 링크를 붙여넣으세요...",
                color = TomaSecondaryText,
                fontSize = 16.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Link Icon",
                tint = TomaSecondaryText,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .background(TomaIosLinkBlue, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Submit",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = TomaIosLinkBlue
        ),
        shape = RoundedCornerShape(28.dp),
        singleLine = true
    )
}

@Composable
fun ScanOptionsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScanOptionCard(
            modifier = Modifier.weight(1f),
            iconVector = Icons.Default.PhotoCamera,
            iconBackgroundColor = TomaLightRed,
            iconTintColor = TomaMainRed,
            text = "사진 스캔"
        )

        ScanOptionCard(
            modifier = Modifier.weight(1f),
            iconVector = Icons.Default.Description,
            iconBackgroundColor = TomaQuickAnalysisBlue,
            iconTintColor = Color(0xFF1E88E5),
            text = "PDF 스캔"
        )
    }
}

@Composable
fun ScanOptionCard(
    modifier: Modifier = Modifier,
    iconVector: ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    text: String
) {
    Column(
        modifier = modifier
            .height(140.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(iconBackgroundColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = text,
                tint = iconTintColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = text,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

data class QuickAnalysisItem(
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color,
    val text: String
)

@Composable
fun QuickAnalysisSection() {
    val items = listOf(
        QuickAnalysisItem(Icons.Default.LocalFireDepartment, TomaMainRed, TomaLightRed, "틱톡 트렌드"),
        QuickAnalysisItem(Icons.Default.Article, Color(0xFF1E88E5), TomaQuickAnalysisBlue, "스크린샷"),
        QuickAnalysisItem(Icons.Default.Book, Color(0xFF43A047), TomaQuickAnalysisGreen, "레시피 요약")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "빠른 분석",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                QuickAnalysisChip(item = item)
            }
        }
    }
}

@Composable
fun QuickAnalysisChip(item: QuickAnalysisItem) {
    Surface(
        onClick = { },
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.03f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(item.iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.text,
                    tint = item.iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = item.text,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

data class RecentAnalysisItem(
    val title: String,
    val timeText: String,
    val badgeText: String,
    val badgeBgColor: Color,
    val tempColor: Color
)

@Composable
fun RecentAnalysisSection() {
    val items = listOf(
        RecentAnalysisItem("Miso Glazed Salmon", "2시간 전 분석", "YOUTUBE", TomaIosLinkBlue, Color(0xFF8D6E63)),
        RecentAnalysisItem("Roasted Root Salad", "어제 분석", "IMAGE", TomaMainRed, Color(0xFF43A047))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 분석 항목",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { }) {
                Text(
                    text = "전체 보기",
                    color = TomaIosLinkBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                RecentAnalysisCard(item = item)
            }
        }
    }
}

@Composable
fun RecentAnalysisCard(item: RecentAnalysisItem) {
    Column(
        modifier = Modifier
            .size(width = 200.dp, height = 240.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f)
                .background(item.tempColor)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, end = 10.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.badgeBgColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.badgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Text(
                text = item.timeText,
                color = TomaSecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
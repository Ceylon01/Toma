package com.capstone.toma.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.toma.ui.TomaBlue
import com.capstone.toma.ui.TomaBrandBar
import com.capstone.toma.ui.TomaBrown
import com.capstone.toma.ui.TomaCard
import com.capstone.toma.ui.TomaChip
import com.capstone.toma.ui.TomaCream
import com.capstone.toma.ui.TomaGreen
import com.capstone.toma.ui.TomaInk
import com.capstone.toma.ui.TomaMuted
import com.capstone.toma.ui.TomaTomato

private val LoadingGradient = Brush.sweepGradient(
    colors = listOf(TomaTomato, TomaBlue, TomaTomato),
)

private data class QuickAnalysisItem(
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color,
    val text: String
)

private data class RecentAnalysisItem(
    val title: String,
    val timeText: String,
    val badgeText: String,
    val badgeBgColor: Color,
    val tempColor: Color
)

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onOpenStorage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.background(TomaCream),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TomaBrandBar(onMenuClick = onMenuClick)
        }
        item {
            Column {
                Text(
                    text = "홈",
                    color = TomaInk,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "영상 분석과 내 레시피 저장소를 한 곳에서 관리합니다.",
                    color = TomaMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        item {
            HeroCard(onOpenStorage = onOpenStorage)
        }
        item {
            LoadingSection()
        }
        item {
            LinkInputSection()
        }
        item {
            ScanOptionsSection()
        }
        item {
            QuickAnalysisSection()
        }
        item {
            RecentAnalysisSection()
        }
    }
}

@Composable
private fun HeroCard(onOpenStorage: () -> Unit) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = TomaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFEFE6), Color(0xFFFFF7F0))
                    )
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "새로운 저장소 모듈",
                color = TomaTomato,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "저장한 레시피를 모아보고, 검색하고, 즐겨찾기까지 관리하세요.",
                color = TomaInk,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 30.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "메뉴 > 저장소",
                        color = TomaMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "저장소 UI 틀 작업 중",
                        color = TomaMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = onOpenStorage,
                    colors = ButtonDefaults.buttonColors(containerColor = TomaTomato),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = "바로 보기")
                }
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .background(LoadingGradient, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartDisplay,
                    contentDescription = null,
                    tint = TomaTomato,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Text(
            text = "요리의 마법을 분석 중입니다",
            color = TomaInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 22.dp)
        )
        Text(
            text = "영상에서 재료, 단계, 영양 정보를 추출하고\n저장소에 붙일 수 있게 정리합니다.",
            color = TomaMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun LinkInputSection() {
    var linkText by remember { mutableStateOf("") }

    TextField(
        value = linkText,
        onValueChange = { linkText = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        placeholder = {
            Text(
                text = "유튜브 링크를 붙여넣으세요...",
                color = TomaMuted
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TomaMuted
            )
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = TomaInk,
            unfocusedTextColor = TomaInk,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = TomaTomato
        ),
        shape = RoundedCornerShape(28.dp),
        singleLine = true
    )
}

@Composable
private fun ScanOptionsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScanOptionCard(
            modifier = Modifier.weight(1f),
            iconVector = Icons.Default.PhotoCamera,
            iconBackgroundColor = Color(0xFFFFE8E0),
            iconTintColor = TomaTomato,
            text = "사진 스캔"
        )

        ScanOptionCard(
            modifier = Modifier.weight(1f),
            iconVector = Icons.Default.Article,
            iconBackgroundColor = Color(0xFFEAF4FB),
            iconTintColor = TomaBlue,
            text = "PDF 스캔"
        )
    }
}

@Composable
private fun ScanOptionCard(
    modifier: Modifier,
    iconVector: ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    text: String
) {
    Card(
        modifier = modifier.height(138.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBackgroundColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = text,
                color = TomaInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun QuickAnalysisSection() {
    val items = listOf(
        QuickAnalysisItem(Icons.Default.LocalFireDepartment, TomaTomato, Color(0xFFFFE8E0), "틱톡 트렌드"),
        QuickAnalysisItem(Icons.Default.Article, TomaBlue, Color(0xFFEAF4FB), "스크린샷"),
        QuickAnalysisItem(Icons.Default.Book, TomaGreen, Color(0xFFE9F3E8), "레시피 요약"),
        QuickAnalysisItem(Icons.Default.Bookmark, TomaTomato, TomaChip, "저장소 저장")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "빠른 분석",
            color = TomaInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            contentPadding = PaddingValues(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
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
                        Text(
                            text = item.text,
                            color = TomaInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentAnalysisSection() {
    val items = listOf(
        RecentAnalysisItem("Miso Glazed Salmon", "2시간 전 분석", "YOUTUBE", TomaTomato, TomaBrown),
        RecentAnalysisItem("Roasted Root Salad", "어제 분석", "IMAGE", TomaGreen, TomaBlue)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 분석 항목",
                color = TomaInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {}) {
                Text(text = "전체 보기", color = TomaTomato)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .width(210.dp)
                        .height(240.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.2f)
                                .background(item.tempColor)
                        ) {
                            Surface(
                                color = item.badgeBgColor,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = item.badgeText,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
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
                                color = TomaInk,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.timeText,
                                color = TomaMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
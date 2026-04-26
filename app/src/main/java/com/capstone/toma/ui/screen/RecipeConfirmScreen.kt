package com.capstone.toma.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.capstone.toma.ui.theme.TomaMainOrange
import com.capstone.toma.ui.theme.TomaPrimaryText
import com.capstone.toma.ui.theme.TomaSecondaryText
import org.json.JSONObject
import java.util.Locale

private val ConfirmBg = Color(0xFFFFFBF7)
private val ConfirmCard = Color.White
private val ConfirmChip = Color(0xFFFFEFE3)
private val ConfirmAccent = TomaMainOrange
private val ConfirmInk = TomaPrimaryText
private val ConfirmMuted = TomaSecondaryText

@Composable
fun RecipeConfirmScreen(
    keyword: String = "",
    recipeDataJson: String? = null,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    onRejectClick: () -> Unit = {}
) {
    val recipe = remember(keyword, recipeDataJson) {
        parseConfirmRecipe(keyword, recipeDataJson)
    }

    Scaffold(
        containerColor = ConfirmBg,
        bottomBar = {
            ConfirmDecisionBar(
                onConfirmClick = onConfirmClick,
                onRejectClick = onRejectClick
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ConfirmBg),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = inner.calculateTopPadding() + 12.dp,
                end = 20.dp,
                bottom = inner.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { ConfirmHeader(onBackClick) }
            item { ConfirmHero(recipe) }
            item { ConfirmSummary(recipe) }
            item { ConfirmSectionTitle("재료", "(${recipe.ingredients.size}개)") }
            items(recipe.ingredients.chunked(2)) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { ingredient ->
                        IngredientChip(
                            text = ingredient,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ConfirmHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
                tint = Color.Black
            )
        }

        Text(
            text = "레시피 확인",
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "TOMA",
            color = ConfirmAccent,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ConfirmHero(recipe: ConfirmRecipeUiData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(confirmPalette(recipe.category)))
    ) {
        if (!recipe.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = "${recipe.title} 대표 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.10f),
                            Color.Black.copy(alpha = 0.42f)
                        )
                    )
                )
        )

        Surface(
            color = ConfirmAccent,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
        ) {
            Text(
                text = recipe.category,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "선택된 메뉴",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = recipe.title,
                color = Color.White,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ConfirmSummary(recipe: ConfirmRecipeUiData) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "조리 정보",
            color = ConfirmInk,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoChip(
                label = "분류",
                value = recipe.category,
                modifier = Modifier.weight(1f)
            )
            InfoChip(
                label = "소요 시간",
                value = recipe.timeText,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ConfirmAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoChip(
                label = "난이도",
                value = recipe.difficulty,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = ConfirmAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            InfoChip(
                label = "기준 인원",
                value = recipe.servingsText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        color = ConfirmChip,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.height(72.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = ConfirmMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.invoke()
                if (leadingIcon != null) Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = value,
                    color = ConfirmInk,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ConfirmSectionTitle(title: String, trailing: String? = null) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = title,
            color = ConfirmInk,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = trailing,
                color = ConfirmAccent,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun IngredientChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ConfirmCard,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = ConfirmInk,
            lineHeight = 22.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ConfirmDecisionBar(
    onConfirmClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "위 레시피로 안내를 시작할까요?",
                color = ConfirmInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRejectClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ConfirmMuted
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NO", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConfirmAccent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("YES", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class ConfirmRecipeUiData(
    val title: String,
    val category: String,
    val ingredients: List<String>,
    val difficulty: String,
    val timeText: String,
    val servingsText: String,
    val imageUrl: String?
)

private fun parseConfirmRecipe(keyword: String, recipeDataJson: String?): ConfirmRecipeUiData {
    val json = recipeDataJson?.let {
        runCatching { JSONObject(it) }.getOrNull()
    }
    val ingredients = parseStringArray(json, "ingredients")
        .ifEmpty { listOf("재료 정보가 준비되지 않았어요.") }
    val title = json?.optString("title").orEmpty()
        .ifBlank { keyword.ifBlank { "추천 레시피" } }
    val category = json?.optString("category").orEmpty()
        .ifBlank { guessRecipeCategory(title, ingredients) }
    val difficulty = json?.optString("difficulty").orEmpty()
        .ifBlank { "보통" }
    val timeText = json?.optString("time").orEmpty()
        .ifBlank { "시간 정보 없음" }
    val servings = json?.optInt("servings", 0) ?: 0
    val servingsText = if (servings > 0) "${servings}인분" else "인분 정보 없음"
    val imageUrl = json?.optString("image_url").orEmpty()
        .takeIf { it.isNotBlank() && it != "없음" }

    return ConfirmRecipeUiData(
        title = title,
        category = category,
        ingredients = ingredients,
        difficulty = difficulty,
        timeText = timeText,
        servingsText = servingsText,
        imageUrl = imageUrl
    )
}

private fun parseStringArray(json: JSONObject?, key: String): List<String> {
    val array = json?.optJSONArray(key) ?: return emptyList()
    return List(array.length()) { index -> array.optString(index) }
        .filter { it.isNotBlank() }
}

private fun guessRecipeCategory(title: String, ingredients: List<String>): String {
    val text = (title + " " + ingredients.joinToString(" ")).lowercase(Locale.KOREAN)
    return when {
        listOf("김치", "된장", "고추장", "불고기", "비빔", "찌개", "국", "밥", "전", "나물", "떡").any(text::contains) -> "한식"
        listOf("짜장", "짬뽕", "마라", "탕수", "중화", "볶음면").any(text::contains) -> "중식"
        listOf("파스타", "스테이크", "샐러드", "리조또", "피자", "수프", "크림").any(text::contains) -> "양식"
        listOf("초밥", "라멘", "우동", "돈카츠", "가츠", "소바").any(text::contains) -> "일식"
        listOf("떡볶이", "김밥", "라볶이", "튀김", "순대").any(text::contains) -> "분식"
        listOf("케이크", "쿠키", "라떼", "스무디", "디저트", "음료", "말차").any(text::contains) -> "디저트/음료"
        else -> "기타"
    }
}

private fun confirmPalette(category: String) = when (category) {
    "한식" -> listOf(Color(0xFF2D2A24), Color(0xFF916347))
    "중식" -> listOf(Color(0xFF9E2A2B), Color(0xFFE09F3E))
    "양식" -> listOf(Color(0xFF245E5A), Color(0xFF78B7A4))
    "일식" -> listOf(Color(0xFF2C3E50), Color(0xFF8FB9A8))
    "분식" -> listOf(Color(0xFFFF6B57), Color(0xFFFFB36B))
    "디저트/음료" -> listOf(Color(0xFF796AA9), Color(0xFFE9AFA3))
    else -> listOf(Color(0xFF595F72), Color(0xFFE7A977))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecipeConfirmScreenPreview() {
    RecipeConfirmScreen(
        keyword = "김치볶음밥",
        recipeDataJson = """
            {
              "title": "김치볶음밥",
              "category": "한식",
              "ingredients": ["밥 2공기", "김치 1컵", "대파 1/2대", "계란 2개"],
              "difficulty": "쉬움",
              "time": "18분",
              "servings": 2
            }
        """.trimIndent()
    )
}

package com.capstone.toma.storage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.toma.ui.TomaCard
import com.capstone.toma.ui.TomaChip
import com.capstone.toma.ui.TomaCream
import com.capstone.toma.ui.TomaInk
import com.capstone.toma.ui.TomaMuted
import com.capstone.toma.ui.TomaTomato
import kotlinx.coroutines.launch

@Composable
fun RecipeStorageScreen(
    onMenuClick: () -> Unit
) {
    val applicationContext = LocalContext.current.applicationContext
    val repository = remember(applicationContext) {
        RecipeStorageRepository.getInstance(applicationContext)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val recipes by repository.observeRecipes().collectAsState(initial = emptyList())

    var initialLoadComplete by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var openedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(repository) {
        repository.ensureSeeded()
        initialLoadComplete = true
    }

    LaunchedEffect(recipes, openedRecipeId) {
        if (openedRecipeId != null && recipes.none { it.id == openedRecipeId }) {
            openedRecipeId = null
        }
    }

    val openedRecipe = recipes.firstOrNull { it.id == openedRecipeId }
    val filteredRecipes = remember(recipes, query) {
        val keyword = query.trim()
        if (keyword.isBlank()) {
            recipes
        } else {
            recipes.filter { recipe ->
                recipe.title.contains(keyword, ignoreCase = true) ||
                    recipe.category.contains(keyword, ignoreCase = true) ||
                    recipe.story.contains(keyword, ignoreCase = true) ||
                    recipe.ingredients.any { ingredient ->
                        ingredient.name.contains(keyword, ignoreCase = true) ||
                            ingredient.detail.contains(keyword, ignoreCase = true)
                    }
            }
        }
    }

    Scaffold(
        containerColor = TomaCream,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (openedRecipe == null && initialLoadComplete && recipes.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("추가 화면은 다음 단계에서 연결하면 됩니다.")
                        }
                    },
                    containerColor = TomaTomato,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "레시피 추가")
                }
            }
        }
    ) { innerPadding ->
        when {
            !initialLoadComplete && recipes.isEmpty() -> LoadingStorageState(innerPadding)
            recipes.isEmpty() -> EmptyStorageState(innerPadding, onMenuClick) {
                scope.launch {
                    repository.ensureSeeded(forceReplace = true)
                    snackbarHostState.showSnackbar("샘플 레시피를 다시 불러왔습니다.")
                }
            }
            openedRecipe != null -> RecipeDetailContent(
                innerPadding = innerPadding,
                recipe = openedRecipe,
                onBack = { openedRecipeId = null },
                onFavoriteToggle = {
                    scope.launch {
                        repository.updateFavorite(openedRecipe.id, !openedRecipe.isFavorite)
                    }
                }
            )
            else -> RecipeListContent(
                innerPadding = innerPadding,
                recipes = filteredRecipes,
                totalCount = recipes.size,
                query = query,
                onQueryChange = { query = it },
                onMenuClick = onMenuClick,
                onRecipeOpen = { openedRecipeId = it.id },
                onFavoriteToggle = { recipe ->
                    scope.launch {
                        repository.updateFavorite(recipe.id, !recipe.isFavorite)
                    }
                }
            )
        }
    }
}

@Composable
private fun RecipeListContent(
    innerPadding: PaddingValues,
    recipes: List<StoredRecipe>,
    totalCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onRecipeOpen: (StoredRecipe) -> Unit,
    onFavoriteToggle: (StoredRecipe) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TomaCream),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = innerPadding.calculateTopPadding() + 12.dp,
            bottom = innerPadding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { StorageHeader(onMenuClick) }
        item { StorageSearchField(query, recipes.size, totalCount, onQueryChange) }

        if (recipes.isEmpty()) {
            item { EmptySearchResultCard() }
        } else {
            items(recipes, key = StoredRecipe::id) { recipe ->
                RecipeListCard(
                    recipe = recipe,
                    onOpen = { onRecipeOpen(recipe) },
                    onFavoriteToggle = { onFavoriteToggle(recipe) }
                )
            }
        }
    }
}

@Composable
private fun StorageHeader(
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "내 레시피",
            color = TomaInk,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.background(TomaCard, CircleShape)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "메뉴", tint = TomaInk)
        }
    }
}

@Composable
private fun StorageSearchField(
    query: String,
    resultCount: Int,
    totalCount: Int,
    onQueryChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("키워드를 입력하세요.", color = TomaMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TomaMuted)
            },
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = TomaInk,
                unfocusedTextColor = TomaInk,
                focusedContainerColor = TomaCard,
                unfocusedContainerColor = TomaCard,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = TomaTomato
            )
        )
        Text(
            text = if (query.isBlank()) "${totalCount}개의 저장 레시피" else "${resultCount} / ${totalCount}개 검색됨",
            color = TomaMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RecipeListCard(
    recipe: StoredRecipe,
    onOpen: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(212.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(TomaCard)
        ) {
            RecipeArtwork(recipe = recipe, modifier = Modifier.fillMaxSize())
        }
        Text(
            text = "${recipe.totalMinutes}분 조리 · ${recipe.category}",
            color = TomaTomato,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = recipe.title,
                color = TomaInk,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "즐겨찾기",
                    tint = TomaTomato,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = "${recipe.difficulty} · ${recipe.servings}인분",
            color = TomaMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RecipeDetailContent(
    innerPadding: PaddingValues,
    recipe: StoredRecipe,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TomaCream),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = innerPadding.calculateTopPadding() + 12.dp,
            bottom = innerPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { DetailHeader(onBack) }
        item { RecipeHeroCard(recipe, onFavoriteToggle) }
        item { RecipeHeadline(recipe) }
        item { RecipeStory(recipe) }
        item { SectionHeader("Ingredients", "(${recipe.servings} Servings)") }
        item { IngredientsGrid(recipe.ingredients) }
        item { SectionHeader("How to Cook") }
        itemsIndexed(recipe.steps, key = { index, step -> "${recipe.id}-$index-${step.title}" }) { index, step ->
            RecipeStepCard(index + 1, step)
        }
    }
}

@Composable
private fun DetailHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(TomaCard, CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = TomaInk)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "레시피 상세",
            color = TomaInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecipeHeroCard(
    recipe: StoredRecipe,
    onFavoriteToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(TomaCard)
    ) {
        RecipeArtwork(recipe = recipe, modifier = Modifier.fillMaxSize())
        Surface(
            color = TomaTomato,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
        ) {
            Text(
                text = recipe.category,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.White.copy(alpha = 0.92f), CircleShape)
        ) {
            Icon(
                imageVector = if (recipe.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "즐겨찾기",
                tint = TomaTomato
            )
        }
    }
}

@Composable
private fun RecipeHeadline(recipe: StoredRecipe) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = recipe.title,
                color = TomaInk,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${recipe.totalMinutes}m",
                    color = TomaTomato,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("TOTAL TIME", color = TomaMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricPill(recipe.rating.toString(), Icons.Default.Star, TomaTomato)
            MetricPill(recipe.difficulty)
            MetricPill("${recipe.caloriesKcal} kcal")
        }
    }
}

@Composable
private fun MetricPill(
    text: String,
    icon: ImageVector? = null,
    tint: Color = TomaInk
) {
    Surface(color = TomaChip, shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            }
            Text(text = text, color = TomaInk, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun RecipeStory(recipe: StoredRecipe) {
    Text(
        text = "\"${recipe.story}\"",
        color = TomaMuted,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 28.sp
    )
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: String? = null
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = title,
            color = TomaInk,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = trailing,
                color = TomaTomato,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun IngredientsGrid(
    ingredients: List<RecipeIngredient>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ingredients.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { ingredient ->
                    IngredientCard(ingredient, Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun IngredientCard(
    ingredient: RecipeIngredient,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = ingredient.icon.toDisplaySpec()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = TomaCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(
                text = ingredient.name,
                color = TomaInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = ingredient.detail,
                color = TomaMuted,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun RecipeStepCard(
    index: Int,
    step: RecipeStep
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = TomaCard)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = index.toString().padStart(2, '0'),
                color = TomaMuted.copy(alpha = 0.12f),
                fontSize = 72.sp,
                lineHeight = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 14.dp, top = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, top = 28.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = step.title,
                    color = TomaInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = step.description,
                    color = TomaMuted,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingStorageState(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = TomaTomato)
    }
}

@Composable
private fun EmptyStorageState(
    innerPadding: PaddingValues,
    onMenuClick: () -> Unit,
    onReload: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TomaCream),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = innerPadding.calculateTopPadding() + 12.dp,
            bottom = innerPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { StorageHeader(onMenuClick) }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = TomaCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "저장된 레시피가 없습니다.",
                        color = TomaInk,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Room DB가 비어 있으면 샘플 레시피를 다시 넣어 화면을 확인할 수 있습니다.",
                        color = TomaMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onReload) {
                        Text("샘플 레시피 불러오기")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchResultCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = TomaCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "검색 결과가 없습니다.",
                color = TomaInk,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "검색어를 바꾸거나 전체 목록으로 돌아가서 다시 확인해보세요.",
                color = TomaMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RecipeArtwork(
    recipe: StoredRecipe,
    modifier: Modifier = Modifier
) {
    val colors = when (recipe.category) {
        "한식" -> listOf(Color(0xFF131518), Color(0xFF453126))
        "파스타" -> listOf(Color(0xFF74C8B7), Color(0xFF3B938B))
        "음료" -> listOf(Color(0xFFC2CC97), Color(0xFF98A761))
        "샐러드" -> listOf(Color(0xFFE5F1F8), Color(0xFFCAE0EC))
        "브런치" -> listOf(Color(0xFFF2DDBE), Color(0xFFD39D6E))
        else -> listOf(Color(0xFF273542), Color(0xFF12181F))
    }

    Canvas(modifier = modifier) {
        drawRect(brush = Brush.linearGradient(colors))
        when (recipe.category) {
            "한식" -> {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.60f),
                    size = Size(size.width * 0.68f, size.height * 0.16f)
                )
                drawOval(
                    color = Color(0xFFE7E1DA),
                    topLeft = Offset(size.width * 0.22f, size.height * 0.64f),
                    size = Size(size.width * 0.56f, size.height * 0.08f)
                )
                drawOval(
                    color = Color(0xFFDA4A24),
                    topLeft = Offset(size.width * 0.32f, size.height * 0.24f),
                    size = Size(size.width * 0.28f, size.height * 0.34f)
                )
            }
            "파스타" -> {
                drawRoundRect(
                    color = Color(0xFFF0C25D),
                    topLeft = Offset(size.width * 0.30f, size.height * 0.16f),
                    size = Size(size.width * 0.40f, size.height * 0.62f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(80f, 80f)
                )
                drawRoundRect(
                    color = Color(0xFF5AB4A6),
                    topLeft = Offset(size.width * 0.36f, size.height * 0.44f),
                    size = Size(size.width * 0.28f, size.height * 0.12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                )
            }
            "음료" -> {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.36f, size.height * 0.46f),
                    size = Size(size.width * 0.24f, size.height * 0.12f)
                )
                drawOval(
                    color = Color(0xFF9AC17A),
                    topLeft = Offset(size.width * 0.40f, size.height * 0.49f),
                    size = Size(size.width * 0.16f, size.height * 0.06f)
                )
            }
            "샐러드" -> {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.54f),
                    size = Size(size.width * 0.64f, size.height * 0.14f)
                )
                repeat(8) { index ->
                    drawCircle(
                        color = if (index % 3 == 0) Color(0xFFE04B39) else Color(0xFF78AE60),
                        radius = size.minDimension * 0.06f,
                        center = Offset(
                            size.width * (0.28f + (index % 4) * 0.12f),
                            size.height * (0.48f + (index / 4) * 0.10f)
                        )
                    )
                }
            }
            "브런치" -> {
                drawRoundRect(
                    color = Color(0xFFE4B16F),
                    topLeft = Offset(size.width * 0.24f, size.height * 0.36f),
                    size = Size(size.width * 0.42f, size.height * 0.24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26f, 26f)
                )
            }
            else -> {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.64f),
                    size = Size(size.width * 0.60f, size.height * 0.10f)
                )
                drawRoundRect(
                    color = Color(0xFF5C3527),
                    topLeft = Offset(size.width * 0.34f, size.height * 0.34f),
                    size = Size(size.width * 0.28f, size.height * 0.24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
            }
        }
    }
}

private fun IngredientIcon.toDisplaySpec(): Pair<ImageVector, Color> = when (this) {
    IngredientIcon.Pantry -> Icons.Default.Restaurant to TomaTomato
    IngredientIcon.Meat -> Icons.Default.SetMeal to TomaTomato
    IngredientIcon.Aromatics -> Icons.Default.LocalCafe to Color(0xFFB57A52)
    IngredientIcon.Sauce -> Icons.Default.Opacity to TomaTomato
    IngredientIcon.Greens -> Icons.Default.Eco to Color(0xFF7FA161)
    IngredientIcon.Broth -> Icons.Default.SoupKitchen to TomaTomato
    IngredientIcon.Dairy -> Icons.Default.LocalCafe to Color(0xFFB57A52)
    IngredientIcon.Spice -> Icons.Default.LocalFireDepartment to TomaTomato
}

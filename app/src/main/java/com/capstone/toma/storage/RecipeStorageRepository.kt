package com.capstone.toma.storage

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeStorageRepository private constructor(
    private val dao: RecipeStorageDao
) {
    fun observeRecipes(): Flow<List<StoredRecipe>> {
        return dao.observeRecipes().map { recipes ->
            recipes.map(RecipeEntity::toModel)
        }
    }

    suspend fun ensureSeeded(forceReplace: Boolean = false) {
        val shouldSeed = forceReplace || dao.countRecipes() == 0
        if (!shouldSeed) return

        if (forceReplace) {
            dao.clearAll()
        }
        dao.insertAll(seedRecipes().map(StoredRecipe::toEntity))
    }

    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean) {
        dao.updateFavorite(
            recipeId = recipeId,
            isFavorite = isFavorite,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun saveRecipe(recipe: StoredRecipe) {
        dao.upsert(recipe.toEntity())
    }

    companion object {
        @Volatile
        private var instance: RecipeStorageRepository? = null

        fun getInstance(context: Context): RecipeStorageRepository {
            return instance ?: synchronized(this) {
                instance ?: RecipeStorageRepository(
                    RecipeStorageDatabase.getInstance(context).recipeStorageDao()
                ).also { instance = it }
            }
        }
    }
}

private fun RecipeEntity.toModel(): StoredRecipe = StoredRecipe(
    id = id,
    title = title,
    category = category,
    totalMinutes = totalMinutes,
    servings = servings,
    difficulty = difficulty,
    rating = rating,
    caloriesKcal = caloriesKcal,
    story = story,
    imageUrl = imageUrl,
    ingredients = ingredients,
    steps = steps,
    isFavorite = isFavorite
)

private fun StoredRecipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    category = category,
    totalMinutes = totalMinutes,
    servings = servings,
    difficulty = difficulty,
    rating = rating,
    caloriesKcal = caloriesKcal,
    story = story,
    imageUrl = imageUrl,
    ingredients = ingredients,
    steps = steps,
    isFavorite = isFavorite,
    updatedAt = System.currentTimeMillis()
)

private fun seedRecipes(): List<StoredRecipe> = listOf(
    StoredRecipe(
        id = "mom-kimchi-jjim",
        title = "우리 엄마 김치찜",
        category = "한식",
        totalMinutes = 45,
        servings = 4,
        difficulty = "Medium Difficulty",
        rating = 4.9,
        caloriesKcal = 380,
        story = "오래 끓인 묵은지의 깊은 산미와 약간의 단맛이 돼지고기 풍미를 더 부드럽게 정리해줍니다.",
        imageUrl = null,
        ingredients = listOf(
            RecipeIngredient(
                name = "Aged Kimchi",
                detail = "1/2 Head (Well-fermented)",
                icon = IngredientIcon.Pantry
            ),
            RecipeIngredient(
                name = "Pork Belly",
                detail = "600g (Thick cut)",
                icon = IngredientIcon.Meat
            ),
            RecipeIngredient(
                name = "Aromatics",
                detail = "Onion, Scallion, Garlic",
                icon = IngredientIcon.Aromatics
            ),
            RecipeIngredient(
                name = "Broth Base",
                detail = "Anchovy stock, Gochugaru",
                icon = IngredientIcon.Broth
            )
        ),
        steps = listOf(
            RecipeStep(
                title = "Prepare the Pork & Kimchi",
                description = "냄비 바닥에 묵은지를 깔고 도톰한 돼지고기를 통째로 올린 뒤, 김치 잎으로 한 번 더 감싸 풍미가 안으로 스며들게 합니다."
            ),
            RecipeStep(
                title = "Add Aromatics & Stock",
                description = "채 썬 양파와 대파를 올리고 멸치 육수를 부어 재료의 2/3 정도만 잠기게 맞춘 뒤, 고춧가루와 다진 마늘을 풀어줍니다."
            ),
            RecipeStep(
                title = "The Slow Simmer",
                description = "중약불에서 최소 35분 이상 천천히 끓여 김치는 투명하게 부드러워지고, 돼지고기는 젓가락으로 쉽게 찢어질 정도까지 익힙니다."
            )
        ),
        isFavorite = true
    ),
    StoredRecipe(
        id = "basil-cream-pasta",
        title = "바질 크림 파스타",
        category = "파스타",
        totalMinutes = 25,
        servings = 2,
        difficulty = "Easy",
        rating = 4.7,
        caloriesKcal = 520,
        story = "생바질 향은 마지막 1분에만 넣어야 크림 소스가 무겁지 않고 상큼하게 끝납니다.",
        imageUrl = null,
        ingredients = listOf(
            RecipeIngredient(
                name = "Fettuccine",
                detail = "180g",
                icon = IngredientIcon.Pantry
            ),
            RecipeIngredient(
                name = "Cream",
                detail = "200ml + Parmesan",
                icon = IngredientIcon.Dairy
            ),
            RecipeIngredient(
                name = "Basil",
                detail = "1 handful",
                icon = IngredientIcon.Greens
            ),
            RecipeIngredient(
                name = "Garlic Butter",
                detail = "2 cloves, 20g butter",
                icon = IngredientIcon.Sauce
            )
        ),
        steps = listOf(
            RecipeStep(
                title = "Boil the Pasta",
                description = "면은 포장 시간보다 1분 덜 삶고 면수 한 컵을 남겨둡니다."
            ),
            RecipeStep(
                title = "Build the Sauce",
                description = "버터에 마늘 향을 낸 뒤 생크림과 파르메산을 넣어 약불에서 농도를 맞춥니다."
            ),
            RecipeStep(
                title = "Finish with Basil",
                description = "면과 면수를 섞어 유화시키고 마지막에 바질을 넣어 한 번만 가볍게 섞습니다."
            )
        ),
        isFavorite = false
    ),
    StoredRecipe(
        id = "apple-cinnamon-toast",
        title = "애플 시나몬 토스트",
        category = "브런치",
        totalMinutes = 15,
        servings = 1,
        difficulty = "Easy",
        rating = 4.8,
        caloriesKcal = 290,
        story = "사과는 너무 익히지 말고 가장자리만 살짝 투명해질 때 멈춰야 식감이 살아 있습니다.",
        imageUrl = null,
        ingredients = listOf(
            RecipeIngredient(
                name = "Sourdough",
                detail = "2 slices",
                icon = IngredientIcon.Pantry
            ),
            RecipeIngredient(
                name = "Apple",
                detail = "1/2, thinly sliced",
                icon = IngredientIcon.Greens
            ),
            RecipeIngredient(
                name = "Butter",
                detail = "15g",
                icon = IngredientIcon.Dairy
            ),
            RecipeIngredient(
                name = "Cinnamon Sugar",
                detail = "1 tbsp",
                icon = IngredientIcon.Spice
            )
        ),
        steps = listOf(
            RecipeStep(
                title = "Toast the Bread",
                description = "팬이나 토스터에서 사워도우를 바삭하게 구워 기본 식감을 만듭니다."
            ),
            RecipeStep(
                title = "Saute the Apple",
                description = "버터에 사과를 가볍게 볶고 시나몬 슈거를 입혀 은은한 캐러멜 향을 냅니다."
            ),
            RecipeStep(
                title = "Assemble",
                description = "토스트 위에 사과를 겹겹이 올리고 팬의 남은 시럽을 얇게 둘러 마무리합니다."
            )
        ),
        isFavorite = false
    )
)

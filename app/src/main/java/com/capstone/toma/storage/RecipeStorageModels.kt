package com.capstone.toma.storage

data class StoredRecipe(
    val id: String,
    val title: String,
    val category: String,
    val totalMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val rating: Double,
    val caloriesKcal: Int,
    val story: String,
    val imageUrl: String?,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val isFavorite: Boolean
)

data class RecipeIngredient(
    val name: String,
    val detail: String,
    val icon: IngredientIcon
)

data class RecipeStep(
    val title: String,
    val description: String
)

enum class IngredientIcon {
    Pantry,
    Meat,
    Aromatics,
    Sauce,
    Greens,
    Broth,
    Dairy,
    Spice
}

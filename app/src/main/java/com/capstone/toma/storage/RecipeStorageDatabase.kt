package com.capstone.toma.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "stored_recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
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
    val isFavorite: Boolean,
    val updatedAt: Long
)

class RecipeJsonConverters {
    @TypeConverter
    fun fromIngredients(value: List<RecipeIngredient>): String {
        return JSONArray().apply {
            value.forEach { ingredient ->
                put(
                    JSONObject()
                        .put("name", ingredient.name)
                        .put("detail", ingredient.detail)
                        .put("icon", ingredient.icon.name)
                )
            }
        }.toString()
    }

    @TypeConverter
    fun toIngredients(value: String): List<RecipeIngredient> {
        if (value.isBlank()) return emptyList()
        val array = JSONArray(value)
        return buildList(array.length()) {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    RecipeIngredient(
                        name = item.optString("name"),
                        detail = item.optString("detail"),
                        icon = item.optString("icon").toIngredientIcon()
                    )
                )
            }
        }
    }

    @TypeConverter
    fun fromSteps(value: List<RecipeStep>): String {
        return JSONArray().apply {
            value.forEach { step ->
                put(
                    JSONObject()
                        .put("title", step.title)
                        .put("description", step.description)
                )
            }
        }.toString()
    }

    @TypeConverter
    fun toSteps(value: String): List<RecipeStep> {
        if (value.isBlank()) return emptyList()
        val array = JSONArray(value)
        return buildList(array.length()) {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    RecipeStep(
                        title = item.optString("title"),
                        description = item.optString("description")
                    )
                )
            }
        }
    }

    private fun String.toIngredientIcon(): IngredientIcon {
        return runCatching { IngredientIcon.valueOf(this) }
            .getOrDefault(IngredientIcon.Pantry)
    }
}

@Database(
    entities = [RecipeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RecipeJsonConverters::class)
abstract class RecipeStorageDatabase : RoomDatabase() {
    abstract fun recipeStorageDao(): RecipeStorageDao

    companion object {
        @Volatile
        private var instance: RecipeStorageDatabase? = null

        fun getInstance(context: Context): RecipeStorageDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeStorageDatabase::class.java,
                    "recipe-storage.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { instance = it }
            }
        }
    }
}

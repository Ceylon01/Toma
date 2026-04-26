package com.capstone.toma.storage

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.capstone.toma.model.RecipeSourceType
import org.json.JSONArray

@Entity(tableName = "stored_recipes")
data class StoredRecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val story: String,
    val time: Int,
    val difficulty: String,
    val servings: Int,
    val calories: Int,
    val rating: Double,
    val favorite: Boolean,
    val ingredients: List<String>,
    val steps: List<String>,
    val sourceType: RecipeSourceType,
    val timeText: String,
    val imageUri: String? = null,
    val updatedAt: Long
)

@Entity(tableName = "recipe_drafts")
data class RecipeDraftEntity(
    @PrimaryKey val id: String,
    val keyword: String,
    val title: String,
    val recipeDataJson: String,
    val complete: Boolean,
    val updatedAt: Long
)

class RecipeStorageConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return JSONArray(value).toString()
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        val array = JSONArray(value)
        return buildList(array.length()) {
            for (index in 0 until array.length()) {
                add(array.optString(index))
            }
        }
    }

    @TypeConverter
    fun fromRecipeSourceType(value: RecipeSourceType): String = value.name

    @TypeConverter
    fun toRecipeSourceType(value: String): RecipeSourceType {
        return runCatching { RecipeSourceType.valueOf(value) }
            .getOrDefault(RecipeSourceType.TEXT)
    }
}

@Database(
    entities = [StoredRecipeEntity::class, RecipeDraftEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(RecipeStorageConverters::class)
abstract class RecipeStorageDatabase : RoomDatabase() {
    abstract fun recipeStorageDao(): RecipeStorageDao

    companion object {
        @Volatile
        private var instance: RecipeStorageDatabase? = null

        private val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stored_recipes ADD COLUMN imageUri TEXT")
            }
        }

        private val Migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recipe_drafts (
                        id TEXT NOT NULL PRIMARY KEY,
                        keyword TEXT NOT NULL,
                        title TEXT NOT NULL,
                        recipeDataJson TEXT NOT NULL,
                        complete INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): RecipeStorageDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeStorageDatabase::class.java,
                    "recipe-storage.db"
                )
                    .addMigrations(Migration2To3, Migration3To4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}

package com.capstone.toma.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeStorageDao {
    @Query(
        """
        SELECT * FROM stored_recipes
        ORDER BY isFavorite DESC, updatedAt DESC, title ASC
        """
    )
    fun observeRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipe: RecipeEntity)

    @Query("SELECT COUNT(*) FROM stored_recipes")
    suspend fun countRecipes(): Int

    @Query("DELETE FROM stored_recipes")
    suspend fun clearAll()

    @Query(
        """
        UPDATE stored_recipes
        SET isFavorite = :isFavorite, updatedAt = :updatedAt
        WHERE id = :recipeId
        """
    )
    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean, updatedAt: Long)
}

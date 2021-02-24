package com.amarchaud.estats.model.database

import androidx.room.*
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: ArtistEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(bookmark: ArtistEntity)

    @Delete
    suspend fun delete(bookmark: ArtistEntity)


    @Transaction
    @Query("SELECT * from Bookmarks WHERE id==:id LIMIT 1")
    suspend fun getOneBookmark(id: String): ArtistEntity?

    @Transaction
    @Query("SELECT * from Bookmarks WHERE id==:id LIMIT 1")
    fun getOneBookmarkFlow(id: String): Flow<ArtistEntity?> // with coroutine flow, must be in a thread

    @Transaction
    @Query("SELECT * from Bookmarks")
    suspend fun getAllBookmarks(): List<ArtistEntity>

    @Transaction
    @Query("SELECT * from Bookmarks")
    fun getAllBookmarksFlow(): Flow<List<ArtistEntity>> // with coroutine flow, must be in a thread

    @Transaction
    @Query("DELETE from Bookmarks WHERE id==:id")
    suspend fun deleteOneBookmark(id: String)
}
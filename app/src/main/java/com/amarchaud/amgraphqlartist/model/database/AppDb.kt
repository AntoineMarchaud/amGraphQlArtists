package com.amarchaud.estats.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity

@Database(entities = [ArtistEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun AppDao(): AppDao
}
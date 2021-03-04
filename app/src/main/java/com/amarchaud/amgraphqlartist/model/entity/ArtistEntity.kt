package com.amarchaud.amgraphqlartist.model.entity

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import kotlinx.parcelize.Parcelize
import javax.annotation.Nullable

@Parcelize
@Entity(tableName = "BookMarks")
data class ArtistEntity(
    @PrimaryKey @ColumnInfo(name = "id") @NonNull var id: String,
    @ColumnInfo(name = "name") @Nullable var name: String? = null,
    @ColumnInfo(name = "disambiguation") @Nullable var disambiguation: String? = null,
    @ColumnInfo(name = "photoUrl") @Nullable var photoUrl: String? = null,
    @ColumnInfo(name = "cursor") @Nullable var cursor: String? = null
) : Parcelable {
    constructor(artistApp: ArtistApp) : this(
        artistApp.id!!,
        artistApp.name,
        artistApp.disambiguation,
        artistApp.photoUrl,
        artistApp.cursor
    )
}
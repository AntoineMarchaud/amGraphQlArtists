package com.amarchaud.amgraphqlartist.model.entity

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import javax.annotation.Nullable

@Parcelize
@Entity(tableName = "BookMarks")
data class ArtistEntity(
    @PrimaryKey @ColumnInfo(name = "id") @NonNull var id: String,
    //@ColumnInfo(name = "mbid") @Nullable val mbid: String? = null,
    @ColumnInfo(name = "name") @Nullable var name: String? = null,
    @ColumnInfo(name = "disambiguation") @Nullable var disambiguation: String? = null,
    @ColumnInfo(name = "photoUrl") @Nullable var photoUrl: String? = null,
    @ColumnInfo(name = "cursor") @Nullable var cursor : String? = null
) : Parcelable {

    fun areItemsSame(artistEntity: ArtistEntity): Boolean {
        return true
    }

    fun areContentsSame(artistEntity: ArtistEntity): Boolean {
        return id == artistEntity.id &&
                name == artistEntity.name &&
                disambiguation == artistEntity.disambiguation &&
                photoUrl == artistEntity.photoUrl

    }

}
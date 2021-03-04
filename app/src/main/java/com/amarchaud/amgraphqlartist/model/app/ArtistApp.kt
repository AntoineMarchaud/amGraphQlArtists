package com.amarchaud.amgraphqlartist.model.app

import android.os.Parcelable
import com.amarchaud.amgraphqlartist.ArtistsQuery
import com.amarchaud.amgraphqlartist.fragment.ArtistBasicFragment
import com.amarchaud.amgraphqlartist.interfaces.IArtistListener
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArtistApp(
    var id: String? = null,
    var name: String? = null,
    var disambiguation: String? = null,
    var photoUrl: String? = null,
    var cursor: String? = null,
    var isFavorite: Boolean = false
) : Parcelable, IArtistListener {

    // from api to app
    constructor(edge: ArtistsQuery.Edge) : this() {

        cursor = edge.cursor

        edge.node?.let { node ->
            with(node.fragments.artistBasicFragment) {
                this@ArtistApp.id = this.id
                this@ArtistApp.name = name
                this@ArtistApp.disambiguation = disambiguation
                this@ArtistApp.photoUrl = buildIconPath(this@with)
                this@ArtistApp.isFavorite = false // by default
            }
        }
    }

    // from database to app
    constructor(artistEntity: ArtistEntity) : this() {
        id = artistEntity.id
        name = artistEntity.name
        disambiguation = artistEntity.disambiguation
        photoUrl = artistEntity.photoUrl
        cursor = artistEntity.cursor
        isFavorite = true // mandatory
    }


    override fun areItemsSame(other: IArtistListener): Boolean {
        return other is ArtistApp
    }

    override fun areContentsSame(other: IArtistListener): Boolean {
        val otherResult = other as ArtistApp
        return id == otherResult.id &&
                name == otherResult.name &&
                disambiguation == otherResult.disambiguation &&
                photoUrl == otherResult.photoUrl &&
                cursor == otherResult.cursor
    }

    private fun buildIconPath(artist: ArtistBasicFragment): String? =
        if (artist.fanArt?.backgrounds?.size!! > 0) {
            artist.fanArt.backgrounds[0]?.url
                .toString()
        } else {
            null
        }
}
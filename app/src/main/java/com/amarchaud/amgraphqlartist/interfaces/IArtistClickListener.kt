package com.amarchaud.amgraphqlartist.interfaces

import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity

interface IArtistClickListener {
    fun onArtistClicked(artistEntity: ArtistEntity)
    fun onBookmarkClicked(artistEntity: ArtistEntity)
}
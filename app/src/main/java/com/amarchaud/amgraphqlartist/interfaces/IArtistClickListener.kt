package com.amarchaud.amgraphqlartist.interfaces

import com.amarchaud.amgraphqlartist.model.app.ArtistApp

interface IArtistClickListener {
    fun onArtistClicked(artistApp: ArtistApp)
    fun onBookmarkClicked(artistApp: ArtistApp)
}
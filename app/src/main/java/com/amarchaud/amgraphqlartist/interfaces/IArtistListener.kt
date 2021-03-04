package com.amarchaud.amgraphqlartist.interfaces

interface IArtistListener {
    fun areItemsSame(other: IArtistListener): Boolean
    fun areContentsSame(other: IArtistListener): Boolean
}
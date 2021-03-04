package com.amarchaud.amgraphqlartist.viewmodel.data

import androidx.lifecycle.ViewModel
import com.amarchaud.amgraphqlartist.base.SingleLiveEvent
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity

class ArtistToDeleteViewModel : ViewModel() {
    data class ArtistToDelete(val artist: ArtistApp)
    val artistToDeleteLiveData = SingleLiveEvent<ArtistToDelete>()
}
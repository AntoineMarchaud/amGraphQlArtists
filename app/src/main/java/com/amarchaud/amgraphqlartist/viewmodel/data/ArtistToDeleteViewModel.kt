package com.amarchaud.amgraphqlartist.viewmodel.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.amarchaud.amgraphqlartist.base.SingleLiveEvent
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity

class ArtistToDeleteViewModel : ViewModel() {

    private val _artistToDeleteLiveData = SingleLiveEvent<ArtistApp?>()
    val artistToDeleteLiveData: LiveData<ArtistApp?>
        get() = _artistToDeleteLiveData

    fun setArtistToDelete(venueToDelete: ArtistApp?) {
        _artistToDeleteLiveData.value = venueToDelete
    }
}
package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.*
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.amgraphqlartist.viewmodel.paging.ArtistDataSource
import com.amarchaud.estats.model.database.AppDao
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    val app: Application,
    private val apolloClient: ApolloClient,
    private val myDao: AppDao
) : AndroidViewModel(app) {

    companion object {
        const val TAG = "ArtistsViewModel"
    }

    // given by view !
    var currentArtistSearched = MutableLiveData<String>()


    // when currentArtistSearched change...
    private var _artists: LiveData<PagingData<ArtistApp>> =
        currentArtistSearched.distinctUntilChanged().switchMap { artistName ->
            Pager(PagingConfig(pageSize = 3)) {
                ArtistDataSource(artistName, apolloClient)
            }.liveData

        }
    val artists: LiveData<PagingData<ArtistApp>>
        get() = _artists


    fun setSearchQuery(query: String) {
        currentArtistSearched.value = query
    }

    fun forceRefresh() {
        currentArtistSearched.value?.let {
            val saved = currentArtistSearched.value
            currentArtistSearched.value = ""
            currentArtistSearched.value = saved!!
        }
    }

    fun onBookmarkClicked(artistApp: ArtistApp) {

        if (artistApp.id == null)
            return

        viewModelScope.launch {
            val artistEntity = ArtistEntity(artistApp)
            val favorite = myDao.getOneBookmark(artistEntity.id)
            if (favorite == null) {
                myDao.insert(artistEntity)
            } else {
                myDao.delete(artistEntity)
            }
        }
    }
}
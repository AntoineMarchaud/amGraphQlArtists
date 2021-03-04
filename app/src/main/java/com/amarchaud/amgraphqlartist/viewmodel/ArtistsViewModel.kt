package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amarchaud.amgraphqlartist.ArtistsQuery
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.estats.model.database.AppDao
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import dagger.hilt.android.lifecycle.HiltViewModel
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

    var currentArtistSearched: String = ""


    private var _listOfArtistsLiveData = MutableLiveData<List<ArtistApp>>()
    val listOfArtistsLiveData: LiveData<List<ArtistApp>>
        get() = _listOfArtistsLiveData


    fun onRefresh(isNext: Boolean = false) {

        if (currentArtistSearched.isBlank()) {
            Log.d(TAG, "Query empty so no request will be made")
            _listOfArtistsLiveData.postValue(ArrayList())
            return
        }

        if(isNext) {
            if(listOfArtistsLiveData.value.isNullOrEmpty() || listOfArtistsLiveData.value!!.size < 15 || listOfArtistsLiveData.value!!.last().cursor == null)
                return
        }

        viewModelScope.launch {

            currentArtistSearched.let { s ->
                val response = try {

                    val after = if (isNext) {
                        if (listOfArtistsLiveData.value.isNullOrEmpty()) {
                            null
                        } else {
                            listOfArtistsLiveData.value!!.last().cursor
                        }
                    } else {
                        null
                    }

                    apolloClient.query(ArtistsQuery(s, after ?: "")).await()
                } catch (e: ApolloException) {
                    Log.d(TAG, "Failure", e)
                    Toast.makeText(app, e.message, Toast.LENGTH_LONG).show()
                    null
                }

                if (response == null) {
                    return@launch
                } else {

                    val listArtists = if(isNext) {
                        listOfArtistsLiveData.value!!.toMutableList()
                    } else {
                        mutableListOf()
                    }

                    response.data?.search?.artists?.edges?.forEach { edge ->
                        edge?.let { listArtists.add(ArtistApp(it)) }
                    }
                    _listOfArtistsLiveData.postValue(listArtists)
                }
            }
        }
    }


    fun setSearchQuery(query: String) {
        currentArtistSearched = query
        onRefresh()
    }

    fun onBookmarkClicked(artistApp: ArtistApp) {

        if(artistApp.id == null)
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
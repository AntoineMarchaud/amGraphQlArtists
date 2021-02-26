package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amarchaud.amgraphqlartist.ArtistsQuery
import com.amarchaud.amgraphqlartist.BR
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.base.BaseViewModel
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    val app: Application,
    private val apolloClient: ApolloClient
) : BaseViewModel(app) {

    companion object {
        const val TAG = "ArtistsViewModel"
    }

    var currentArtistSearched: String = ""

    @Bindable
    var loading: Boolean = false


    var listOfArtistsLiveData: MutableLiveData<List<ArtistEntity>> = MutableLiveData()

    fun onRefresh(isNext: Boolean = false) {

        if(isNext) {
            if(listOfArtistsLiveData.value.isNullOrEmpty() || listOfArtistsLiveData.value!!.size < 15 || listOfArtistsLiveData.value!!.last().cursor == null)
                return
        }

        viewModelScope.launch {

            loading = true
            notifyPropertyChanged(BR.loading)

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
                    null
                }

                if (response == null) {
                    loading = false
                    notifyPropertyChanged(BR.loading)

                    Toast.makeText(app, R.string.GraphQlError, Toast.LENGTH_LONG).show()

                } else {

                    val listArtists = if(isNext) {
                        listOfArtistsLiveData.value!!.toMutableList()
                    } else {
                        mutableListOf()
                    }

                    response.data?.search()?.artists()?.edges()?.forEach { edge ->

                        val artist = ArtistEntity("")
                        artist.cursor = edge.cursor()

                        edge.node()?.let { node ->
                            with(node.fragments().artistBasicFragment()) {

                                val imageUrl: String? =
                                    if (fanArt()?.backgrounds()?.size!! > 0) {
                                        fanArt()?.backgrounds()?.get(0)?.url()
                                            .toString()
                                    } else {
                                        null
                                    }

                                artist.id = id()
                                artist.name = name()
                                artist.disambiguation = disambiguation()
                                artist.photoUrl = imageUrl
                            }
                        }

                        listArtists.add(artist)
                    }
                    listOfArtistsLiveData.postValue(listArtists)
                }
            }

            loading = false
            notifyPropertyChanged(BR.loading)
        }
    }


    fun setSearchQuery(query: String) {
        currentArtistSearched = query
        onRefresh()
    }

}
package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amarchaud.amgraphqlartist.ArtistQuery
import com.amarchaud.amgraphqlartist.BR
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.base.BaseViewModel
import com.amarchaud.amgraphqlartist.fragment.ArtistDetailsFragment
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.estats.model.database.AppDao
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

//@HiltViewModel
class ArtistDetailViewModel @AssistedInject constructor(
    val app: Application,
    val myDao: AppDao,
    private val apolloClient: ApolloClient,
    @Assisted val artist: ArtistEntity
) : BaseViewModel(app) {

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(artist: ArtistEntity): ArtistDetailViewModel
    }

    companion object {
        const val TAG = "ArtistDetailViewModel"


        fun provideFactory(
            assistedFactory: AssistedFactory,
            artist: ArtistEntity
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(artist) as T
            }
        }
    }

    @Bindable
    var loadingGeneral: Boolean = false

    @Bindable
    var loadingRelease: Boolean = false

    @Bindable
    var countryCodeDetail: String? = null

    @Bindable
    var countryDetail: String? = null

    @Bindable
    var genderDetail: String? = null

    @Bindable
    var ratingDetail: Double? = null

    @Bindable
    var typeDetail: String? = null


    init {
        onSearch()
    }

    var nameLiveData: MutableLiveData<String> = MutableLiveData()
    var disambiguationLiveData: MutableLiveData<String> = MutableLiveData()
    var photoUrlLiveData: MutableLiveData<String> = MutableLiveData()

    var artistsRelationshipsLiveData: MutableLiveData<List<ArtistEntity>> = MutableLiveData()
    var albumsLiveData: MutableLiveData<List<ArtistDetailsFragment.Node>> = MutableLiveData()

    var isArtistInDatabase: MutableLiveData<Boolean> = MutableLiveData()

    init {
        viewModelScope.launch {
            isArtistInDatabase.postValue(myDao.getOneBookmark(artist.id) != null)
        }
    }

    fun onSearch() {

        viewModelScope.launch {

            loadingGeneral = true
            notifyPropertyChanged(BR.loadingGeneral)

            loadingRelease = true
            notifyPropertyChanged(BR.loadingRelease)

            val response = try {
                apolloClient.query(ArtistQuery(artist.id)).await()
            } catch (e: ApolloException) {
                Log.d(TAG, "Failure", e)
                null
            }

            if(response == null) {
                loadingGeneral = false
                notifyPropertyChanged(BR.loadingGeneral)

                loadingRelease = false
                notifyPropertyChanged(BR.loadingRelease)

                Toast.makeText(app, R.string.GraphQlError, Toast.LENGTH_LONG).show()

            } else {

                response.data?.node()?.fragments()?.artistDetailsFragment()?.let {

                    // common details
                    with(it.fragments().artistBasicFragment()) {

                        nameLiveData.postValue(name())
                        disambiguationLiveData.postValue(disambiguation())

                        if (fanArt()?.backgrounds()?.size!! > 0) {
                            photoUrlLiveData.postValue(
                                fanArt()?.backgrounds()?.get(0)?.url().toString()
                            )
                        }
                    }

                    // new details
                    countryCodeDetail = it.country()
                    genderDetail = it.gender()
                    ratingDetail = it.rating()?.value() // rating of the artist
                    typeDetail = it.type() // group or solo
                    countryDetail = it.area()?.name() // name of the country
                    notifyChange()

                    albumsLiveData.postValue(it.releaseGroups()?.nodes())

                    loadingRelease = false
                    notifyPropertyChanged(BR.loadingRelease)

                    // TODO relationships is always NULL.... WHY ??????
                    val listArtists = mutableListOf<ArtistEntity>()
                    it.relationships()?.artists()?.nodes()?.filterNotNull()?.forEach { node ->
                        with(node.target().fragments().artistBasicFragment()) {

                            if (this == null)
                                return@with

                            val imageUrl: String? =
                                if (fanArt()?.backgrounds()?.size!! > 0) {
                                    fanArt()?.backgrounds()?.get(0)?.url().toString()
                                } else {
                                    null
                                }

                            listArtists.add(ArtistEntity(id(), name(), disambiguation(), imageUrl))
                        }
                    }

                    artistsRelationshipsLiveData.postValue(listArtists)

                    loadingGeneral = false
                    notifyPropertyChanged(BR.loadingGeneral)
                }
            }
        }
    }

    fun onBookMarkedClick() {

        viewModelScope.launch {
            val toDelete = myDao.getOneBookmark(artist.id)
            if (toDelete == null) {
                myDao.insert(artist)
                isArtistInDatabase.postValue(true)
            } else {
                myDao.delete(artist)
                isArtistInDatabase.postValue(false)
            }
        }
    }
}
package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.amarchaud.amgraphqlartist.ArtistQuery
import com.amarchaud.amgraphqlartist.fragment.ArtistDetailsFragment
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.estats.model.database.AppDao
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    val app: Application,
    val myDao: AppDao,
    private val apolloClient: ApolloClient,
) : AndroidViewModel(app) {


    companion object {
        const val TAG = "ArtistDetailViewModel"
    }

    // given by view
    lateinit var artistApp: ArtistApp

    private var _loadingGeneral = MutableLiveData(false)
    val loadingGeneral: LiveData<Boolean>
        get() = _loadingGeneral

    private var _loadingRelease = MutableLiveData(false)
    val loadingRelease: LiveData<Boolean>
        get() = _loadingRelease


    private var _countryCodeDetail = MutableLiveData<String?>(null)
    val countryCodeDetail: LiveData<String?>
        get() = _countryCodeDetail

    private var _countryDetail = MutableLiveData<String?>(null)
    val countryDetail: LiveData<String?>
        get() = _countryDetail

    private var _genderDetail = MutableLiveData<String?>(null)
    val genderDetail: LiveData<String?>
        get() = _genderDetail

    private var _ratingDetail = MutableLiveData<String?>(null)
    val ratingDetail: LiveData<String?>
        get() = _ratingDetail

    private var _typeDetail = MutableLiveData<String?>(null)
    val typeDetail: LiveData<String?>
        get() = _typeDetail

    private var _ratingBar = MutableLiveData<Float?>(null)
    val ratingBar: LiveData<Float?>
        get() = _ratingBar


    private var _reviews = MutableLiveData<Int?>(null)
    val reviews: LiveData<Int?>
        get() = _reviews


    private var _nameLiveData = MutableLiveData<String?>()
    val nameLiveData: LiveData<String?>
        get() = _nameLiveData

    private var _disambiguationLiveData = MutableLiveData<String?>()
    val disambiguationLiveData: LiveData<String?>
        get() = _disambiguationLiveData

    private var _photoUrlLiveData = MutableLiveData<String>()
    val photoUrlLiveData: LiveData<String>
        get() = _photoUrlLiveData

    private var _artistsRelationshipsLiveData = MutableLiveData<List<ArtistApp>>()
    val artistsRelationshipsLiveData: LiveData<List<ArtistApp>>
        get() = _artistsRelationshipsLiveData

    private var _albumsLiveData = MutableLiveData<List<ArtistDetailsFragment.Node?>?>()
    val albumsLiveData: LiveData<List<ArtistDetailsFragment.Node?>?>
        get() = _albumsLiveData


    fun onSearch() {

        if(artistApp.id == null)
            return

        viewModelScope.launch {

            _loadingGeneral.postValue(true)
            _loadingRelease.postValue(true)

            val response = try {
                apolloClient.query(ArtistQuery(artistApp.id!!)).await()
            } catch (e: ApolloException) {
                Log.d(TAG, "Failure", e)
                Toast.makeText(app, e.message, Toast.LENGTH_LONG).show()
                null
            }

            if (response == null) {
                _loadingGeneral.postValue(false)
                _loadingRelease.postValue(false)
            } else {

                response.data?.node?.fragments?.artistDetailsFragment?.let {

                    // common details
                    with(it.fragments.artistBasicFragment) {

                        _nameLiveData.postValue(name)
                        _disambiguationLiveData.postValue(disambiguation)

                        if (fanArt?.backgrounds?.size!! > 0) {
                            _photoUrlLiveData.postValue(
                                fanArt.backgrounds.get(0)?.url.toString()
                            )
                        }
                    }

                    // new details
                    _countryCodeDetail.postValue(it.country)
                    _genderDetail.postValue(it.gender)
                    _typeDetail.postValue(it.type)
                    _countryDetail.postValue(it.area?.name)
                    _ratingDetail.postValue(formatRatings(it))
                    _ratingBar.postValue(ratingBar(it))
                    _reviews.postValue(it.rating?.voteCount)

                    _albumsLiveData.postValue(it.releaseGroups?.nodes)

                    _loadingRelease.postValue(false)

                    // TODO relationships is always NULL.... WHY ??????
                    val listArtists = mutableListOf<ArtistApp>()
                    it.relationships?.artists?.nodes?.filterNotNull()?.forEach { node ->
                        with(node.target.fragments.artistBasicFragment) {

                            if (this == null)
                                return@with

                            val imageUrl: String? =
                                if (fanArt?.backgrounds?.size!! > 0) {
                                    fanArt.backgrounds[0]?.url.toString()
                                } else {
                                    null
                                }

                            listArtists.add(ArtistApp(id, name, disambiguation, imageUrl, null, false))
                        }
                    }

                    _artistsRelationshipsLiveData.postValue(listArtists)

                    _loadingGeneral.postValue(false)
                }
            }
        }
    }

    private fun formatRatings(venueDetail: ArtistDetailsFragment): String {
        return DecimalFormat("#.##").format(venueDetail.rating?.value?.div(2) ?: 0) ?: "0"
    }

    private fun ratingBar(artistDetailsFragment: ArtistDetailsFragment): Float {
        return artistDetailsFragment.rating?.value?.div(2)?.toFloat() ?: 0f
    }

    fun onBookmarkClicked() {

        if (artistApp.id.isNullOrEmpty())
            return

        artistApp.isFavorite = !artistApp.isFavorite

        viewModelScope.launch {
            val toDelete = myDao.getOneBookmark(artistApp.id!!)
            if (toDelete == null) {
                myDao.insert(ArtistEntity(artistApp))
            } else {
                myDao.delete(toDelete)
            }
        }
    }
}
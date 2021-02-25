package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amarchaud.amgraphqlartist.BR
import com.amarchaud.amgraphqlartist.base.BaseViewModel
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.estats.model.database.AppDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    val app: Application,
    val myDao: AppDao
) : BaseViewModel(app) {

    @Bindable
    var loading: Boolean = false

    var artistsBookmarkedLiveData: MutableLiveData<List<ArtistEntity>> = MutableLiveData()

    init {
        viewModelScope.launch {
            loading = true
            notifyPropertyChanged(BR.loading)

            // load datastore
            artistsBookmarkedLiveData.postValue(myDao.getAllBookmarks())

            loading = false
            notifyPropertyChanged(BR.loading)
        }
    }


    fun refresh() {
        viewModelScope.launch {
            artistsBookmarkedLiveData.postValue(myDao.getAllBookmarks())
        }
    }
}
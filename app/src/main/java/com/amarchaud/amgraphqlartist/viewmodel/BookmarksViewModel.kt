package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amarchaud.amgraphqlartist.BR
import com.amarchaud.amgraphqlartist.base.BaseViewModel
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
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
) : AndroidViewModel(app) {

    private var _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean>
        get() = _loadingLiveData

    private var _artistsBookmarkedLiveData= MutableLiveData<List<ArtistApp>>()
    val artistsBookmarkedLiveData: LiveData<List<ArtistApp>>
        get() = _artistsBookmarkedLiveData

    init {
        viewModelScope.launch {
            _loadingLiveData.postValue(true)
            refresh()
            _loadingLiveData.postValue(false)
        }
    }


    fun refresh() {
        viewModelScope.launch {
            _artistsBookmarkedLiveData.postValue(myDao.getAllBookmarks().map { ArtistApp(it) })
        }
    }

    fun deleteBookmark(artistApp: ArtistApp) {
        viewModelScope.launch {
            val pos = myDao.getAllBookmarks().indexOfFirst { it.id  == artistApp.id}
            if (pos >= 0) {
                myDao.deleteOneBookmark(artistApp.id!!)
                refresh()
            }
        }
    }
}
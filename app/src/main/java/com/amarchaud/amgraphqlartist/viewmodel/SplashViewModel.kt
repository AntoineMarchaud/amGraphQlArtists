package com.amarchaud.amgraphqlartist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.amarchaud.amgraphqlartist.view.SplashFragmentDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

    val actionLiveData: MutableLiveData<NavDirections> = MutableLiveData()

    init {
        viewModelScope.launch {
            delay(1000L)
            actionLiveData.postValue(SplashFragmentDirections.actionSplashFragmentToArtistsFragment())
        }
    }
}
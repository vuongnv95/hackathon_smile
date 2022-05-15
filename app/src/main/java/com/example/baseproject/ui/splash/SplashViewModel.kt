package com.example.baseproject.ui.splash

import com.example.core.base.BaseViewModel
import com.example.core.network.AuthApiInterface
import com.example.core.pref.RxPreferences
import com.example.core.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authApiInterface: AuthApiInterface,
    private val rxPreferences: RxPreferences
) : BaseViewModel() {

    val actionSplash = SingleLiveEvent<SplashActionState>()

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    init {
        login()
    }

    private fun login() {
        ioScope.launch {
            if (rxPreferences.get("imei").isNullOrBlank()) {
                actionSplash.postValue(SplashActionState.OpenPair)
            } else {
                actionSplash.postValue(SplashActionState.OpenHome)
            }
        }
    }

    override fun onCleared() {
        ioScope.cancel()
        super.onCleared()
    }
}

sealed class SplashActionState {
    object OpenPair : SplashActionState()
    object OpenHome : SplashActionState()
}
package com.example.baseproject.ui.splash

import com.example.core.base.BaseViewModel
import com.example.core.model.network.Login
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

    val actionSPlash = SingleLiveEvent<SplashActionState>()

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    init {
        login()
    }

    private fun login() {
        ioScope.launch {
            val request = Login.Request(
                "0356573351",
                "Vht@54321"
            )
            val response = authApiInterface.login(request)
            if (response.token != null) {
                rxPreferences.setUserToken("Bearer ${response.token}")
                actionSPlash.postValue(SplashActionState.Finish)
            }
        }
    }

    override fun onCleared() {
        ioScope.cancel()
        super.onCleared()
    }
}

sealed class SplashActionState {
    object Finish : SplashActionState()
}
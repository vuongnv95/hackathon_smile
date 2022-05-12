package com.example.baseproject.ui.home

import com.example.core.base.BaseViewModel
import com.example.core.pref.RxPreferences
import com.example.core.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rxPreferences: RxPreferences
) : BaseViewModel() {

    private var webSocket: WebSocket? = null

    private var reconnectCount = 0

    val serverMessageResponse = SingleLiveEvent<String>()

    fun connect() {
        if (reconnectCount++ == 5) return
        val request = Request.Builder()
            .url("http://116.101.122.190/ws/assistant")
            .addHeader("Authorization", rxPreferences.getToken() ?: "")
            .build()
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()

        client.newWebSocket(request, object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                this@HomeViewModel.webSocket = null
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                this@HomeViewModel.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                connect()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                serverMessageResponse.postValue(text)
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                this@HomeViewModel.webSocket = webSocket
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1001, null)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }
}
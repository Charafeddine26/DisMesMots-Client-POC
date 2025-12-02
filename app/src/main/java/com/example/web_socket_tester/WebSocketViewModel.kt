package com.example.web_socket_tester

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketViewModel : ViewModel() {

    private val _url = MutableStateFlow("wss://echo.websocket.org")
    val url: StateFlow<String> = _url

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            viewModelScope.launch {
                _isConnected.value = true
                _messages.value += "Connected"
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            viewModelScope.launch {
                _messages.value += "Received: $text"
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            viewModelScope.launch {
                _messages.value += "Closing: $code / $reason"
                webSocket.close(1000, null)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            viewModelScope.launch {
                _isConnected.value = false
                _messages.value += "Closed: $code / $reason"
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            viewModelScope.launch {
                _isConnected.value = false
                _messages.value += "Error: ${t.message}"
            }
        }
    }

    fun setUrl(newUrl: String) {
        _url.value = newUrl
    }

    fun connect() {
        if (_isConnected.value) {
            disconnect()
        }
        val request = Request.Builder().url(_url.value).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun disconnect() {
        webSocket?.close(1000, "Canceled by user")
    }

    fun sendMessage(text: String) {
        webSocket?.send(text)
        _messages.value += "Sent: $text"
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.cancel()
        client.dispatcher.executorService.shutdown()
    }
}

package com.example.web_socket_tester

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.UUID

class WebSocketViewModel : ViewModel() {

    private val _url = MutableStateFlow("http://10.0.2.2:9999")
    val url: StateFlow<String> = _url

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var socket: Socket? = null

    fun setUrl(newUrl: String) {
        _url.value = newUrl
    }

    fun connect() {
        if (_isConnected.value) {
            disconnect()
        }
        try {
            // socket.io-client-java handles the connection
            val options = IO.Options()
            options.reconnection = true
            options.forceNew = true
            options.query = "type=android"

            socket = IO.socket(_url.value, options)

            socket?.on(Socket.EVENT_CONNECT, onConnect)
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socket?.on("new-user", onNewUser)

            socket?.connect()

        } catch (e: URISyntaxException) {
            viewModelScope.launch {
                _messages.value += "Error: Invalid URI"
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off() // Remove all listeners
        socket = null
        _isConnected.value = false
    }

    fun sendMessage(text: String) {
        if (socket == null || !_isConnected.value) return

        val user = User(
            id = UUID.randomUUID().toString(),
            name = text,
            email = "android@example.com",
            status = "active"
        )

        val jsonObject = JSONObject()
        try {
            jsonObject.put("id", user.id)
            jsonObject.put("name", user.name)
            jsonObject.put("email", user.email)
            jsonObject.put("status", user.status)

            socket?.emit("new-user", jsonObject)
            viewModelScope.launch {
                _messages.value += "Sent: $text"
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private val onConnect = Emitter.Listener {
        viewModelScope.launch {
            _isConnected.value = true
            _messages.value += "Connected to ${_url.value}"
        }
    }

    private val onDisconnect = Emitter.Listener {
        viewModelScope.launch {
            _isConnected.value = false
            _messages.value += "Disconnected"
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        viewModelScope.launch {
            _isConnected.value = false
            val error = if (args.isNotEmpty()) args[0] else "Unknown error"
            _messages.value += "Connection Error: $error"
        }
    }

    private val onNewUser = Emitter.Listener { args ->
        viewModelScope.launch {
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                try {
                    val name = data.getString("name")
                    _messages.value += "New User: $name"
                } catch (e: JSONException) {
                    _messages.value += "Received invalid data"
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
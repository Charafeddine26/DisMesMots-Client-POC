package com.example.web_socket_tester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.web_socket_tester.ui.theme.WebsockettesterTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WebSocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebsockettesterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebSocketTesterScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WebSocketTesterScreen(viewModel: WebSocketViewModel) {
    val url by viewModel.url.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    var messageText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = url,
                onValueChange = { viewModel.setUrl(it) },
                label = { Text("WebSocket URL") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (isConnected) viewModel.disconnect() else viewModel.connect()
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(if (isConnected) "Disconnect" else "Connect")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                Text(text = message, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Message") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
                enabled = isConnected
            ) {
                Text("Send")
            }
        }
    }
}
package com.example.picoswitcher

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.picoswitcher.ui.theme.PicoSwitcherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PicoSwitcherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PicoControlUI()
                }
            }
        }
    }
}

@Composable
fun PicoControlUI() {
    val context = LocalContext.current
    var ipAddress by remember { mutableStateOf(TextFieldValue("")) }  // Fill with default IP
    var status by remember { mutableStateOf("Unknown") }
    var isOn by remember { mutableStateOf(false) }

    fun fetchStatus(ip: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://$ip/status")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 1000
                conn.readTimeout = 1000
                conn.requestMethod = "GET"
                val result = conn.inputStream.bufferedReader().readText()
                isOn = "ON" in result
                status = "Fan is ${if (isOn) "ON" else "OFF"}"
            } catch (e: Exception) {
                status = "Error connecting"
            }
        }
    }

    fun toggleState(ip: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val next = if (isOn) "off" else "on"
                val url = URL("http://$ip/$next")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 1000
                conn.readTimeout = 1000
                conn.requestMethod = "GET"
                conn.inputStream.bufferedReader().readText()
                isOn = !isOn
                status = "Fan is ${if (isOn) "ON" else "OFF"}"
            } catch (e: Exception) {
                status = "Error toggling state"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("Enter Pico IP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = status, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (ipAddress.text.isNotBlank()) {
                toggleState(ipAddress.text)
            } else {
                Toast.makeText(context, "Please enter IP", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Toggle")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = {
            if (ipAddress.text.isNotBlank()) {
                fetchStatus(ipAddress.text)
            } else {
                Toast.makeText(context, "Please enter IP", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Check Status")
        }
    }
}
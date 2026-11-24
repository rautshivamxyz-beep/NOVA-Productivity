package com.nova.productivity

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.security.SecureRandom
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NovaApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaApp() {
    var tab by remember { mutableStateOf("Home") }
    val tabs = listOf("Home", "Network", "Privacy", "Tools")
    MaterialTheme(colorScheme = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF8B9CFF))) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("NOVA Productivity", fontWeight = FontWeight.Bold) }) },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { item ->
                        NavigationBarItem(selected = tab == item, onClick = { tab = item },
                            icon = { Icon(when(item) { "Home" -> Icons.Default.Home; "Network" -> Icons.Default.Wifi; "Privacy" -> Icons.Default.Shield; else -> Icons.Default.Build }, contentDescription = item) },
                            label = { Text(item) })
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when(tab) {
                    "Home" -> HomeScreen()
                    "Network" -> NetworkScreen()
                    "Privacy" -> PrivacyScreen()
                    else -> ToolsScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val features = listOf("Network tools", "Proxy profiles", "Private DNS helper", "Notes", "Password generator", "Hash & JSON tools", "Device information")
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("One app. Countless possibilities.", style = MaterialTheme.typography.titleMedium) }
        item { Text("Your everyday utility dashboard", style = MaterialTheme.typography.bodyMedium) }
        items(features) { FeatureCard(it) }
        item { Text("Some advanced networking features require Android system permissions or a dedicated VPN service.", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun FeatureCard(name: String, detail: String = "Open this tool from its section") {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun NetworkScreen() {
    val context = LocalContext.current
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val network = cm.activeNetwork
    val caps = cm.getNetworkCapabilities(network)
    val connection = when {
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile data"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
        else -> "Unknown / offline"
    }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8080") }
    var result by remember { mutableStateOf("No connectivity test run") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Network", style = MaterialTheme.typography.headlineSmall)
        FeatureCard("Connection type", connection)
        OutlinedTextField(host, { host = it }, label = { Text("Proxy host") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(port, { port = it }, label = { Text("Proxy port") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            result = try {
                val p = port.toInt()
                val reachable = java.net.Socket().use { socket ->
                    socket.connect(java.net.InetSocketAddress(host, p), 2500); true
                }
                if (reachable) "Host and port reachable" else "Not reachable"
            } catch (e: Exception) { "Connection failed: ${e.message ?: "check host and port"}" }
        }, enabled = host.isNotBlank() && port.toIntOrNull() in 1..65535) { Text("Test host / port") }
        Text(result)
        Text("This checks whether a TCP endpoint responds. It does not route device traffic through a proxy.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)) }) { Text("Open network settings") }
    }
}

@Composable
fun PrivacyScreen() {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Privacy", style = MaterialTheme.typography.headlineSmall)
        FeatureCard("Private DNS", "Use Android's built-in Private DNS setting")
        OutlinedButton(onClick = { context.startActivity(Intent("android.settings.PRIVATE_DNS_SETTINGS")) }) { Text("Open Private DNS settings") }
        FeatureCard("Ad blocking status", "A DNS/VPN-based blocker is not enabled in this starter build.")
        FeatureCard("Firewall", "Per-app firewall rules are not implemented yet; Android restricts this without a VPN-based firewall.")
        FeatureCard("Proxy privacy", "No traffic interception or HTTPS decryption is performed.")
    }
}

@Composable
fun ToolsScreen() {
    var note by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var hash by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tools", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(note, { note = it }, label = { Text("Quick note") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { saved = note }, enabled = note.isNotBlank()) { Text("Save note on screen") }
        if (saved.isNotBlank()) Text("Saved note: $saved")
        Button(onClick = {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%"
            password = buildString { repeat(20) { append(chars[SecureRandom().nextInt(chars.length)]) } }
        }) { Text("Generate 20-character password") }
        if (password.isNotBlank()) SelectionText(password)
        OutlinedTextField(input, { input = it }, label = { Text("Text to hash (SHA-256)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { hash = MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) } }, enabled = input.isNotEmpty()) { Text("Generate SHA-256") }
        if (hash.isNotBlank()) SelectionText(hash)
        FeatureCard("Calculator and converter", "Coming in a future update")
    }
}

@Composable
fun SelectionText(value: String) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Text(value, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
}

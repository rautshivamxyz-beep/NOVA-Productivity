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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import java.security.SecureRandom

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NovaApp() }
    }
}

private val novaBlue = androidx.compose.ui.graphics.Color(0xFF9AA8FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaApp() {
    var tab by remember { mutableStateOf("Home") }
    val tabs = listOf("Home", "Network", "Privacy", "Tools")
    MaterialTheme(colorScheme = darkColorScheme(primary = novaBlue, secondary = androidx.compose.ui.graphics.Color(0xFF80CBC4))) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("NOVA", fontWeight = FontWeight.Bold)
                            Text("PRODUCTIVITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = { tab = "Home" }) {
                            Icon(Icons.Default.Home, contentDescription = "Go to home")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = {
                                Icon(
                                    when (item) {
                                        "Home" -> Icons.Default.Home
                                        "Network" -> Icons.Default.Wifi
                                        "Privacy" -> Icons.Default.Shield
                                        else -> Icons.Default.Build
                                    },
                                    contentDescription = item
                                )
                            },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    "Home" -> HomeScreen(onOpen = { tab = it })
                    "Network" -> NetworkScreen()
                    "Privacy" -> PrivacyScreen()
                    else -> ToolsScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val caps = cm.getNetworkCapabilities(cm.activeNetwork)
    val connection = when {
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Connected to Wi-Fi"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Using mobile data"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Connected by Ethernet"
        else -> "No active connection"
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Everything you need, in one place.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Simple tools. Sensible defaults. No complicated setup.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Circle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(connection, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item { Text("Quick access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { QuickFeature(Icons.Default.Wifi, "Network tools", "Check your connection and test a proxy endpoint", "Open", onClick = { onOpen("Network") }) }
        item { QuickFeature(Icons.Default.Shield, "Privacy", "Open Android Private DNS settings and privacy tools", "Open", onClick = { onOpen("Privacy") }) }
        item { QuickFeature(Icons.Default.Build, "Everyday tools", "Save notes, create passwords and generate hashes", "Open", onClick = { onOpen("Tools") }) }
        item {
            Text(
                "NOVA works with built-in features by default. Android may still ask before granting sensitive permissions or enabling VPN-based features.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, action: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = action)
        }
    }
}

@Composable
fun FeatureCard(name: String, detail: String = "Ready to use") {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NetworkScreen() {
    val context = LocalContext.current
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val caps = cm.getNetworkCapabilities(cm.activeNetwork)
    val connection = when {
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile data"
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
        else -> "Offline / unknown"
    }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8080") }
    var result by remember { mutableStateOf("Enter a proxy host to test its connection.") }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Network", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { FeatureCard("Current connection", connection) }
        item { Text("Proxy endpoint test", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item { Text("Default port: 8080. Enter the server details supplied by your proxy provider; NOVA will not change your phone's traffic automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            OutlinedTextField(host, { host = it }, label = { Text("Proxy host or IP") }, placeholder = { Text("example.com") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(port, { port = it }, label = { Text("Port") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(
                onClick = {
                    result = try {
                        java.net.Socket().use { socket ->
                            socket.connect(java.net.InetSocketAddress(host.trim(), port.toInt()), 2500)
                        }
                        "The host and port are reachable."
                    } catch (_: Exception) { "Could not connect. Check the host, port and internet connection." }
                },
                enabled = host.isNotBlank() && (port.toIntOrNull()?.let { it in 1..65535 } == true),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Test connection") }
        }
        item { FeatureCard("Test result", result) }
        item {
            OutlinedButton(onClick = { context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Phone network settings")
            }
        }
        item { Text("This is a reachability test only. It does not create a proxy tunnel or route device traffic.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun PrivacyScreen() {
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Privacy", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { FeatureCard("Private DNS", "Use Android's built-in encrypted DNS setting. Your current system setting is left unchanged.") }
        item {
            Button(onClick = {
                val intent = Intent("android.settings.PRIVATE_DNS_SETTINGS")
                try { context.startActivity(intent) }
                catch (_: Exception) { context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)) }
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Private DNS settings")
            }
        }
        item { FeatureCard("Ad and tracker blocking", "Not active in this version. A future DNS blocker will ask before enabling its local VPN.") }
        item { FeatureCard("Firewall", "Per-app firewall controls are not active yet. Android requires a VPN-based implementation for this type of filtering.") }
        item { FeatureCard("Your traffic", "NOVA does not inspect, decrypt or intercept HTTPS traffic.") }
    }
}

@Composable
fun ToolsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nova_preferences", android.content.Context.MODE_PRIVATE) }
    var note by remember { mutableStateOf(prefs.getString("quick_note", "") ?: "") }
    var savedMessage by remember { mutableStateOf("Notes are saved on this device.") }
    var password by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var hash by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Everyday tools", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { Text("Your quick note is saved automatically on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    prefs.edit().putString("quick_note", it).apply()
                    savedMessage = "Saved on this device"
                },
                label = { Text("Quick note") },
                placeholder = { Text("Write something to remember…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        item { Text(savedMessage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
        item { HorizontalDivider() }
        item { Text("Password generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item {
            Button(onClick = {
                val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*"
                password = buildString { repeat(20) { append(chars[SecureRandom().nextInt(chars.length)]) } }
            }, modifier = Modifier.fillMaxWidth()) { Text("Generate secure password") }
        }
        if (password.isNotBlank()) {
            item { SelectionText(password) }
            item {
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(password)) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy password")
                }
            }
        }
        item { HorizontalDivider() }
        item { Text("SHA-256 hash", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item {
            OutlinedTextField(input, { input = it; hash = "" }, label = { Text("Text to hash") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
        item {
            Button(onClick = {
                hash = MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
            }, enabled = input.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Generate SHA-256") }
        }
        if (hash.isNotBlank()) {
            item { SelectionText(hash) }
            item {
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(hash)) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy hash")
                }
            }
        }
        item { FeatureCard("More tools", "Calculator, unit converter and file tools will be added in later updates.") }
    }
}

@Composable
fun SelectionText(value: String) {
    Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(value, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
}

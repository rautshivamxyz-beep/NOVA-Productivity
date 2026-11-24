package com.nova.productivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NovaApp() }
    }
}

private val novaBackground = Color(0xFF0A0F19)
private val novaSurface = Color(0xFF141D2B)
private val novaSurfaceAlt = Color(0xFF1B2739)
private val novaBlue = Color(0xFF8EABFF)
private val novaMint = Color(0xFF70D6C4)
private val novaTextMuted = Color(0xFF9EADC2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaApp() {
    var page by remember { mutableStateOf("Home") }
    val proxyRunning by rememberProxyRunning()
    val colors = darkColorScheme(
        primary = novaBlue,
        onPrimary = Color(0xFF10182B),
        secondary = novaMint,
        background = novaBackground,
        surface = novaSurface,
        surfaceVariant = novaSurfaceAlt,
        onSurface = Color(0xFFEAF0FB),
        onSurfaceVariant = novaTextMuted,
        primaryContainer = Color(0xFF26385B),
        onPrimaryContainer = Color(0xFFEAF0FF),
        secondaryContainer = Color(0xFF1E4545),
        onSecondaryContainer = Color(0xFFD8FFF7)
    )

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            containerColor = novaBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("NOVA", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                            Text("LOCAL CONTROL CENTER", style = MaterialTheme.typography.labelSmall, color = novaTextMuted, letterSpacing = 1.2.sp)
                        }
                    },
                    actions = {
                        Surface(shape = CircleShape, color = if (proxyRunning) Color(0xFF163D35) else novaSurfaceAlt) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Circle, null, modifier = Modifier.size(7.dp), tint = if (proxyRunning) novaMint else novaTextMuted)
                                Text(if (proxyRunning) "PROXY ON" else "READY", style = MaterialTheme.typography.labelSmall, color = if (proxyRunning) novaMint else novaTextMuted)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = novaBackground)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = novaSurface) {
                    val destinations = listOf("Home", "Proxy", "Browser", "Tools")
                    destinations.forEach { item ->
                        val icon = when (item) {
                            "Home" -> Icons.Default.Dashboard
                            "Proxy" -> Icons.Default.Router
                            "Browser" -> Icons.Default.Shield
                            else -> Icons.Default.Build
                        }
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            icon = { Icon(icon, contentDescription = item) },
                            label = { Text(item) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = novaBlue,
                                selectedTextColor = novaBlue,
                                indicatorColor = Color(0xFF26385B),
                                unselectedIconColor = novaTextMuted,
                                unselectedTextColor = novaTextMuted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (page) {
                    "Home" -> HomeScreen(
                        proxyRunning = proxyRunning,
                        onOpen = { page = it }
                    )
                    "Proxy" -> ProxyScreen(proxyRunning)
                    "Browser" -> BrowserScreen()
                    else -> ToolsScreen()
                }
            }
        }
    }
}

@Composable
private fun rememberProxyRunning(): State<Boolean> {
    val state = remember { mutableStateOf(ProxyState.running) }
    DisposableEffect(Unit) {
        val listener: (Boolean) -> Unit = { state.value = it }
        ProxyState.subscribe(listener)
        onDispose { ProxyState.unsubscribe(listener) }
    }
    return state
}

@Composable
private fun HomeScreen(proxyRunning: Boolean, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val connection = remember { getConnectionLabel(context) }
    val freeStorage = remember { formatBytes(StatFs(Environment.getDataDirectory().absolutePath).availableBytes) }
    val blocked = remember { NovaBlocklist.blockedTotal(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2B49)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("YOUR PHONE. YOUR TOOLS.", style = MaterialTheme.typography.labelMedium, color = novaMint, letterSpacing = 1.3.sp)
                    Text("Everything runs on this device.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("No NOVA account. No paid proxy. Your notes, tools and blocklist stay local.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD0DBF0))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Icon(Icons.Default.Wifi, null, tint = novaMint, modifier = Modifier.size(16.dp))
                        Text(connection, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
        item {
            Text("CONTROL CENTER", style = MaterialTheme.typography.labelMedium, color = novaTextMuted, letterSpacing = 1.3.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "LOCAL PROXY",
                    value = if (proxyRunning) "RUNNING" else "OFF",
                    detail = if (proxyRunning) "127.0.0.1:8787" else "One-tap start",
                    active = proxyRunning,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpen("Proxy") }
                )
                MetricCard(
                    title = "AD BLOCKER",
                    value = "ON",
                    detail = blocked.toString() + " blocked so far",
                    active = true,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpen("Browser") }
                )
            }
        }
        item { Text("READY TO USE", style = MaterialTheme.typography.labelMedium, color = novaTextMuted, letterSpacing = 1.3.sp) }
        item {
            QuickActionCard(
                icon = Icons.Default.Router,
                title = "Local proxy",
                detail = "Start an HTTP / SOCKS5 proxy on this phone",
                action = "OPEN",
                onClick = { onOpen("Proxy") }
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.Shield,
                title = "Private browser",
                detail = "Browse with NOVA's built-in offline ad and tracker blocklist",
                action = "BROWSE",
                onClick = { onOpen("Browser") }
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.Build,
                title = "Everyday tools",
                detail = "Notes, passwords, hashes, calculator and converter",
                action = "OPEN",
                onClick = { onOpen("Tools") }
            )
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = novaSurface)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, null, tint = novaBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Phone storage available", fontWeight = FontWeight.SemiBold)
                        Text(freeStorage + " free in internal storage", style = MaterialTheme.typography.bodySmall, color = novaTextMuted)
                    }
                }
            }
        }
        item {
            Text(
                "Privacy note: NOVA's blocker currently protects its own browser and requests sent through NOVA's local proxy. It does not silently filter every other app.",
                style = MaterialTheme.typography.bodySmall,
                color = novaTextMuted
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    detail: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = novaSurface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = novaTextMuted, letterSpacing = 0.7.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, color = if (active) novaMint else Color.White, fontWeight = FontWeight.Bold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = novaTextMuted)
        }
    }
}

@Composable
private fun QuickActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, action: String, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = novaSurface), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = novaSurfaceAlt) {
                Icon(icon, null, modifier = Modifier.padding(11.dp).size(22.dp), tint = novaBlue)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = novaTextMuted)
            }
            Text(action, style = MaterialTheme.typography.labelSmall, color = novaBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProxyScreen(proxyRunning: Boolean) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val endpoint = "127.0.0.1:" + LocalProxyService.PORT

    Column(
        Modifier.fillMaxSize().background(novaBackground).padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("LOCAL PROXY", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("A proxy server that runs inside your phone. No proxy subscription or remote server.", color = novaTextMuted)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = if (proxyRunning) Color(0xFF123A33) else novaSurface), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Router, null, tint = if (proxyRunning) novaMint else novaBlue, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (proxyRunning) "PROXY IS RUNNING" else "PROXY IS STOPPED", fontWeight = FontWeight.Bold)
                        Text(if (proxyRunning) "Listening on this phone only" else "Start it with one tap", style = MaterialTheme.typography.bodySmall, color = novaTextMuted)
                    }
                    Surface(shape = CircleShape, color = if (proxyRunning) novaMint else Color(0xFF657187)) {
                        Box(Modifier.size(12.dp))
                    }
                }
                Text(endpoint, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("HTTP CONNECT + SOCKS5 • LOOPBACK ONLY", style = MaterialTheme.typography.labelSmall, color = novaMint, letterSpacing = 0.6.sp)
                Button(
                    onClick = {
                        if (proxyRunning) {
                            context.stopService(android.content.Intent(context, LocalProxyService::class.java))
                        } else {
                            val intent = android.content.Intent(context, LocalProxyService::class.java)
                            try {
                                if (android.os.Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
                            } catch (_: Exception) {
                                android.widget.Toast.makeText(context, "Could not start the local proxy. Check Android's service restrictions.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (proxyRunning) Color(0xFF9D4450) else novaBlue, contentColor = Color(0xFF0A1020))
                ) {
                    Icon(if (proxyRunning) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (proxyRunning) "STOP PROXY" else "START LOCAL PROXY", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(endpoint)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy proxy address")
                }
            }
        }
        Panel("HOW TO USE IT", "NOVA sets the local server up automatically.") {
            InstructionRow("1", "Tap Start local proxy.")
            InstructionRow("2", "Keep NOVA running in the background while you use it.")
            InstructionRow("3", "In an app that supports manual proxy settings, choose HTTP or SOCKS5 and enter 127.0.0.1 with port " + LocalProxyService.PORT + ".")
        }
        Panel("IMPORTANT LIMIT", "Android does not let an ordinary app silently change every other app's proxy.") {
            Text("This server is private to this phone and is not shared over Wi-Fi. It does not automatically route all phone traffic. Apps that have no proxy option will continue using their normal connection.", color = novaTextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InstructionRow(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = CircleShape, color = novaSurfaceAlt) {
            Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                Text(number, color = novaBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BrowserScreen() {
    val context = LocalContext.current
    var address by remember { mutableStateOf("https://www.google.com") }
    var blockedCount by remember { mutableIntStateOf(NovaBlocklist.blockedTotal(context)) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = webViewRef?.canGoBack() == true) {
        webViewRef?.goBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
        }
    }

    Column(Modifier.fillMaxSize().background(novaBackground).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text("PRIVATE BROWSER", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Search or enter website") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { webViewRef?.loadUrl(normalizeAddress(address)) })
            )
            IconButton(onClick = { webViewRef?.loadUrl(normalizeAddress(address)) }) {
                Icon(Icons.Default.ArrowForward, "Go", tint = novaBlue)
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, null, tint = novaMint, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text("NOVA Shield ON", color = novaMint, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.weight(1f))
            Text("Blocked: " + blockedCount, color = novaTextMuted, style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 7.dp)) {
            OutlinedButton(onClick = { webViewRef?.goBack() }, enabled = webViewRef?.canGoBack() == true) { Icon(Icons.Default.ArrowBack, null); Text("Back") }
            OutlinedButton(onClick = { webViewRef?.reload() }) { Icon(Icons.Default.Refresh, null); Text("Reload") }
            OutlinedButton(onClick = { webViewRef?.loadUrl("https://www.google.com"); address = "https://www.google.com" }) { Icon(Icons.Default.Home, null); Text("Home") }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize().weight(1f),
            factory = { webContext ->
                WebView(webContext).apply {
                    webViewRef = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(view: WebView?, request: android.webkit.WebResourceRequest?): WebResourceResponse? {
                            val host = request?.url?.host ?: return null
                            if (NovaBlocklist.isBlocked(host)) {
                                NovaBlocklist.recordBlocked(context.applicationContext)
                                return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                            }
                            return null
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            blockedCount = NovaBlocklist.blockedTotal(context)
                            if (!url.isNullOrBlank()) address = url
                        }
                    }
                    loadUrl("https://www.google.com")
                }
            },
            update = {}
        )
        Text(
            "The offline blocklist filters requests inside this browser. Other apps are not filtered unless they explicitly use NOVA's local proxy.",
            style = MaterialTheme.typography.labelSmall,
            color = novaTextMuted,
            modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
        )
    }
}

private fun normalizeAddress(value: String): String {
    val text = value.trim()
    if (text.isBlank()) return "https://www.google.com"
    if (text.startsWith("https://", true) || text.startsWith("http://", true)) return text
    return if (text.contains(".") || text.contains(":")) "https://" + text
    else "https://www.google.com/search?q=" + Uri.encode(text)
}

@Composable
private fun ToolsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nova_local_data", Context.MODE_PRIVATE) }
    val clipboard = LocalClipboardManager.current
    var note by remember { mutableStateOf(prefs.getString("note", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var hashInput by remember { mutableStateOf("") }
    var hash by remember { mutableStateOf("") }
    var firstNumber by remember { mutableStateOf("") }
    var secondNumber by remember { mutableStateOf("") }
    var calcResult by remember { mutableStateOf("") }
    var conversionMode by remember { mutableStateOf("km → mi") }
    var conversionInput by remember { mutableStateOf("") }
    var conversionResult by remember { mutableStateOf("") }
    val storageText = remember {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        formatBytes(stat.availableBytes) + " free"
    }

    Column(
        Modifier.fillMaxSize().background(novaBackground).padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("EVERYDAY TOOLS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Works offline. Your saved note stays on this phone.", color = novaTextMuted)

        Panel("QUICK NOTE", "Saved automatically on this device.") {
            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    prefs.edit().putString("note", it).apply()
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Write a reminder…") }
            )
            Text("Saved locally", color = novaMint, style = MaterialTheme.typography.labelSmall)
        }

        Panel("PASSWORD GENERATOR", "Creates a random 20-character password.") {
            Button(
                onClick = {
                    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*"
                    password = buildString { repeat(20) { append(chars[SecureRandom().nextInt(chars.length)]) } }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Icon(Icons.Default.Password, null); Spacer(Modifier.width(8.dp)); Text("Generate password") }
            if (password.isNotBlank()) {
                SelectionResult(password)
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(password)) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text("Copy password")
                }
            }
        }

        Panel("SHA-256 HASH", "Turn text into a SHA-256 fingerprint.") {
            OutlinedTextField(hashInput, { hashInput = it; hash = "" }, modifier = Modifier.fillMaxWidth(), label = { Text("Text to hash") }, minLines = 2)
            Button(
                onClick = {
                    hash = MessageDigest.getInstance("SHA-256").digest(hashInput.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
                },
                enabled = hashInput.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Generate SHA-256") }
            if (hash.isNotBlank()) {
                SelectionResult(hash)
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(hash)) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text("Copy hash")
                }
            }
        }

        Panel("BASIC CALCULATOR", "Enter two numbers and tap an operation.") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(firstNumber, { firstNumber = it; calcResult = "" }, modifier = Modifier.weight(1f), label = { Text("First number") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(secondNumber, { secondNumber = it; calcResult = "" }, modifier = Modifier.weight(1f), label = { Text("Second number") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("+", "−", "×", "÷").forEach { operation ->
                    Button(
                        onClick = {
                            val a = firstNumber.toDoubleOrNull()
                            val b = secondNumber.toDoubleOrNull()
                            calcResult = if (a == null || b == null) "Enter two valid numbers"
                            else if (operation == "÷" && b == 0.0) "Cannot divide by zero"
                            else formatNumber(
                                when (operation) {
                                    "+" -> a + b
                                    "−" -> a - b
                                    "×" -> a * b
                                    else -> a / b
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) { Text(operation, style = MaterialTheme.typography.titleMedium) }
                }
            }
            if (calcResult.isNotBlank()) SelectionResult(calcResult)
        }

        Panel("UNIT CONVERTER", "No internet connection needed.") {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("km → mi", "kg → lb", "°C → °F").forEach { mode ->
                    FilterChip(selected = conversionMode == mode, onClick = { conversionMode = mode; conversionResult = "" }, label = { Text(mode) })
                }
            }
            OutlinedTextField(conversionInput, { conversionInput = it; conversionResult = "" }, modifier = Modifier.fillMaxWidth(), label = { Text("Value") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Button(
                onClick = {
                    val v = conversionInput.toDoubleOrNull()
                    conversionResult = if (v == null) "Enter a valid number" else {
                        val result = when (conversionMode) {
                            "km → mi" -> v * 0.621371
                            "kg → lb" -> v * 2.20462262
                            else -> (v * 9.0 / 5.0) + 32.0
                        }
                        formatNumber(result) + " " + when (conversionMode) {
                            "km → mi" -> "miles"
                            "kg → lb" -> "pounds"
                            else -> "°F"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Convert") }
            if (conversionResult.isNotBlank()) SelectionResult(conversionResult)
        }

        Panel("DEVICE STORAGE", "Read-only status. NOVA will not delete your files.") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, null, tint = novaBlue)
                Spacer(Modifier.width(10.dp))
                Text(storageText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Panel(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = novaSurface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = novaTextMuted)
            content()
        }
    }
}

@Composable
private fun SelectionResult(value: String) {
    Surface(color = novaSurfaceAlt, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(value, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = Color.White)
    }
}

private fun getConnectionLabel(context: Context): String {
    return try {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork
        val caps = manager.getNetworkCapabilities(network)
        when {
            caps == null -> "No internet connection"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Connected via Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Connected via mobile data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Connected via Ethernet"
            else -> "Network connected"
        }
    } catch (_: Exception) {
        "Connection status unavailable"
    }
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index++
    }
    return DecimalFormat("#,##0.0", DecimalFormatSymbols(Locale.US)).format(value) + " " + units[index]
}

private fun formatNumber(value: Double): String {
    if (!value.isFinite()) return "Not a finite result"
    return DecimalFormat("#,##0.########", DecimalFormatSymbols(Locale.US)).format(value)
}

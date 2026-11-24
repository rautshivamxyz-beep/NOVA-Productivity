package com.nova.productivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val lifeBg = Color(0xFF080D16)
private val lifeSurface = Color(0xFF141E2D)
private val lifeSurface2 = Color(0xFF1D2A3D)
private val lifeBlue = Color(0xFF9AB4FF)
private val lifeMint = Color(0xFF75E0C9)
private val lifeMuted = Color(0xFF9BAAC0)

class LifeOSActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NovaLifeOSApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaLifeOSApp() {
    var page by remember { mutableStateOf("Home") }
    val proxyState = remember { mutableStateOf(ProxyState.running) }
    DisposableEffect(Unit) {
        val listener: (Boolean) -> Unit = { proxyState.value = it }
        ProxyState.subscribe(listener)
        onDispose { ProxyState.unsubscribe(listener) }
    }
    val scheme = darkColorScheme(
        primary = lifeBlue,
        onPrimary = Color(0xFF10182B),
        secondary = lifeMint,
        background = lifeBg,
        surface = lifeSurface,
        surfaceVariant = lifeSurface2,
        onSurface = Color(0xFFEDF3FF),
        onSurfaceVariant = lifeMuted,
        primaryContainer = Color(0xFF26385B),
        secondaryContainer = Color(0xFF17443E)
    )

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = lifeBg,
            topBar = {
                TopAppBar(
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text("NOVA", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text("LIFE CONTROL OS", color = lifeMuted, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                        }
                    },
                    actions = {
                        Surface(shape = CircleShape, color = if (proxyState.value) Color(0xFF17443E) else lifeSurface2) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, null, tint = if (proxyState.value) lifeMint else lifeMuted, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(if (proxyState.value) "PROXY ON" else "READY", style = MaterialTheme.typography.labelSmall, color = if (proxyState.value) lifeMint else lifeMuted)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = lifeBg)
                )
            },
            bottomBar = {
                val destinations = listOf(
                    "Home" to Icons.Default.Dashboard,
                    "Life" to Icons.Default.Checklist,
                    "Money" to Icons.Default.AccountBalanceWallet,
                    "Control" to Icons.Default.Tune,
                    "More" to Icons.Default.Apps
                )
                NavigationBar(containerColor = lifeSurface) {
                    destinations.forEach { (name, icon) ->
                        NavigationBarItem(
                            selected = page == name,
                            onClick = { page = name },
                            icon = { Icon(icon, contentDescription = name) },
                            label = { Text(name) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = lifeBlue,
                                selectedTextColor = lifeBlue,
                                indicatorColor = Color(0xFF26385B),
                                unselectedIconColor = lifeMuted,
                                unselectedTextColor = lifeMuted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (page) {
                    "Home" -> LifeHomeScreen(onNavigate = { page = it })
                    "Life" -> LifeCenterScreen()
                    "Money" -> MoneyCenterScreen()
                    "Control" -> DeviceControlScreen(proxyRunning = proxyState.value, onProxyChange = {
                        val context = it
                    }, onOpenLegacy = { openLegacyNova(it) })
                    else -> MoreCenterScreen(onOpenLegacy = { openLegacyNova(it) })
                }
            }
        }
    }
}

@Composable
private fun LifeHomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    val tasks = remember(revision) { NovaLifeStore.tasks(context) }
    val goals = remember(revision) { NovaLifeStore.goals(context) }
    val entries = remember(revision) { NovaLifeStore.money(context) }
    val done = tasks.count { it.done }
    val open = tasks.count { !it.done }
    val income = entries.filter { it.income }.sumOf { it.amount }
    val expense = entries.filter { !it.income }.sumOf { it.amount }
    val storage = remember { runCatching { formatLifeBytes(StatFs(Environment.getDataDirectory().absolutePath).availableBytes) }.getOrDefault("Unavailable") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2D4D))) {
                Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("YOUR LIFE. ONE CONTROL CENTER.", color = lifeMint, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                    Text("Make today count.", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Plan your day, track your goals and understand your money — privately on this phone.", color = Color(0xFFD5DFF4), style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = lifeMint, modifier = Modifier.size(15.dp))
                        Text("LOCAL-FIRST • NO NOVA ACCOUNT", color = lifeMint, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item { SectionLabel("TODAY AT A GLANCE") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                SummaryCard("OPEN TASKS", open.toString(), "To do", Modifier.weight(1f))
                SummaryCard("COMPLETED", done.toString(), "Finished", Modifier.weight(1f))
                SummaryCard("ACTIVE GOALS", goals.count { it.progress < 100 }.toString(), "In progress", Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MONEY SNAPSHOT", color = lifeMuted, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Income", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
                            Text(formatRupees(income), color = lifeMint, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Expenses", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
                            Text(formatRupees(expense), color = Color(0xFFFFA69A), fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Balance", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
                            Text(formatRupees(income - expense), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(onClick = { onNavigate("Money") }, modifier = Modifier.fillMaxWidth()) { Text("Open money manager"); Icon(Icons.Default.ArrowForward, null) }
                }
            }
        }
        item { SectionLabel("QUICK ACCESS") }
        item { ActionCard(Icons.Default.Checklist, "Tasks, goals and habits", "Plan your day and track progress", "OPEN", { onNavigate("Life") }) }
        item { ActionCard(Icons.Default.AccountBalanceWallet, "Money manager", "Record income, spending and savings", "OPEN", { onNavigate("Money") }) }
        item { ActionCard(Icons.Default.Tune, "Phone and network control", "Proxy, browser, storage and diagnostics", "OPEN", { onNavigate("Control") }) }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, null, tint = lifeBlue, modifier = Modifier.size(23.dp))
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Available phone storage", fontWeight = FontWeight.SemiBold)
                        Text(storage + " free", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LifeCenterScreen() {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var taskInput by remember { mutableStateOf("") }
    var goalInput by remember { mutableStateOf("") }
    var habitInput by remember { mutableStateOf("") }
    val tasks = remember(revision) { NovaLifeStore.tasks(context) }
    val goals = remember(revision) { NovaLifeStore.goals(context) }
    val habits = remember(revision) { NovaLifeStore.habits(context) }
    val routines = remember(revision) { NovaLifeStore.routines(context) }
    val timeline = remember(revision) { NovaLifeStore.timeline(context) }
    val today = NovaLifeStore.todayKey()
    val q = query.trim().lowercase(Locale.getDefault())
    val searchResults = if (q.isBlank()) emptyList() else buildList {
        tasks.filter { it.title.lowercase(Locale.getDefault()).contains(q) }.forEach { add("Task • " + it.title + if (it.done) " • Done" else " • Open") }
        goals.filter { it.title.lowercase(Locale.getDefault()).contains(q) }.forEach { add("Goal • " + it.title + " • " + it.progress + "%") }
        habits.filter { it.title.lowercase(Locale.getDefault()).contains(q) }.forEach { add("Habit • " + it.title) }
        routines.filter { (it.title + " " + it.detail).lowercase(Locale.getDefault()).contains(q) }.forEach { add("Routine • " + it.title) }
        val note = context.getSharedPreferences("nova_local_data", Context.MODE_PRIVATE).getString("note", "") ?: ""
        if (note.lowercase(Locale.getDefault()).contains(q)) add("Saved note • " + note.take(110))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("LIFE COMMAND CENTER", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Tasks, goals, daily habits, routines and searchable personal notes.", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Search your NOVA life data") },
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
        }
        if (q.isNotBlank()) {
            item {
                PanelCard("SEARCH RESULTS", if (searchResults.isEmpty()) "No matches found on this device." else searchResults.size.toString() + " matches") {
                    searchResults.take(12).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        item {
            PanelCard("QUICK TASK", "Add something you need to do.") {
                OutlinedTextField(taskInput, { taskInput = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Task name") })
                Button(
                    onClick = { NovaLifeStore.addTask(context, taskInput); taskInput = ""; revision++ },
                    enabled = taskInput.isNotBlank(), modifier = Modifier.fillMaxWidth()
                ) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add task") }
            }
        }
        item { SectionLabel("TASKS • " + tasks.count { !it.done } + " OPEN") }
        if (tasks.isEmpty()) {
            item { EmptyCard("No tasks yet", "Add your first task above. Your list stays on this phone.") }
        } else {
            items(tasks, key = { it.id }) { task ->
                Card(shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = task.done, onCheckedChange = { NovaLifeStore.setTaskDone(context, task.id, it); revision++ })
                        Text(task.title, modifier = Modifier.weight(1f), color = if (task.done) lifeMuted else Color.White, fontWeight = FontWeight.Medium)
                        IconButton(onClick = { NovaLifeStore.deleteTask(context, task.id); revision++ }) { Icon(Icons.Default.Delete, "Delete task", tint = lifeMuted) }
                    }
                }
            }
        }
        item {
            PanelCard("GOAL TRACKER", "Break big ambitions into progress you can see.") {
                OutlinedTextField(goalInput, { goalInput = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Example: Learn coding") })
                Button(onClick = { NovaLifeStore.addGoal(context, goalInput); goalInput = ""; revision++ }, enabled = goalInput.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Create goal")
                }
            }
        }
        if (goals.isNotEmpty()) {
            item { SectionLabel("YOUR GOALS") }
            items(goals, key = { "g" + it.id }) { goal ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(goal.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text(goal.progress.toString() + "%", color = lifeMint, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(progress = goal.progress / 100f, modifier = Modifier.fillMaxWidth())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (goal.progress >= 100) "Completed" else "Add progress when you make a step", color = lifeMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            TextButton(onClick = { NovaLifeStore.changeGoalProgress(context, goal.id, 10); revision++ }, enabled = goal.progress < 100) { Text("+10%") }
                        }
                    }
                }
            }
        }
        item {
            PanelCard("HABIT TRACKER", "A daily check-in builds a visible record of consistency.") {
                OutlinedTextField(habitInput, { habitInput = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("New habit") })
                Button(onClick = { NovaLifeStore.addHabit(context, habitInput); habitInput = ""; revision++ }, enabled = habitInput.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add habit")
                }
                habits.forEach { habit ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = habit.doneDate == today, onCheckedChange = { NovaLifeStore.toggleHabitToday(context, habit.id); revision++ })
                        Text(habit.title, modifier = Modifier.weight(1f))
                        Text(if (habit.doneDate == today) "DONE" else "TODAY", color = if (habit.doneDate == today) lifeMint else lifeMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item {
            PanelCard("READY-MADE ROUTINES", "Simple checklists you can mark complete each day.") {
                routines.forEach { routine ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = routine.doneDate == today, onCheckedChange = { NovaLifeStore.toggleRoutineToday(context, routine.id); revision++ })
                        Column(Modifier.weight(1f)) {
                            Text(routine.title, fontWeight = FontWeight.SemiBold)
                            Text(routine.detail, color = lifeMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        if (routine.doneDate == today) Icon(Icons.Default.CheckCircle, null, tint = lifeMint)
                    }
                }
            }
        }
        item {
            PanelCard("LIFE TIMELINE", "Recent actions recorded by NOVA on this device.") {
                if (timeline.isEmpty()) Text("Your timeline will appear as you use NOVA.", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
                timeline.take(8).forEach { entry ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Icon(Icons.Default.Timeline, null, tint = lifeBlue, modifier = Modifier.size(17.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            Text(entry.detail, color = lifeMuted, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(entry.createdAt)), color = lifeMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoneyCenterScreen() {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var note by remember { mutableStateOf("") }
    var income by remember { mutableStateOf(false) }
    val entries = remember(revision) { NovaLifeStore.money(context) }
    val monthStart = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    val monthEntries = entries.filter { it.createdAt >= monthStart }
    val totalIncome = monthEntries.filter { it.income }.sumOf { it.amount }
    val totalExpense = monthEntries.filter { !it.income }.sumOf { it.amount }
    val categories = if (income) listOf("Pocket money", "Salary", "Gift", "Other") else listOf("Food", "Travel", "Study", "Shopping", "Bills", "Other")
    if (category !in categories) category = categories.first()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("MONEY COMMAND CENTER", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Simple private money tracking. No bank connection required.", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
        }
        item {
            Card(shape = RoundedCornerShape(21.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF18362F))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("THIS MONTH", color = lifeMint, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                    Text(formatRupees(totalIncome - totalExpense), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row {
                        Column(Modifier.weight(1f)) { Text("INCOME", color = Color(0xFFB5EDE0), style = MaterialTheme.typography.labelSmall); Text(formatRupees(totalIncome), color = Color.White, fontWeight = FontWeight.SemiBold) }
                        Column(Modifier.weight(1f)) { Text("EXPENSES", color = Color(0xFFFFC5BB), style = MaterialTheme.typography.labelSmall); Text(formatRupees(totalExpense), color = Color.White, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
        item {
            PanelCard("ADD TRANSACTION", "Record it now. NOVA updates your totals immediately.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !income, onClick = { income = false; category = "Food" }, label = { Text("Expense") })
                    FilterChip(selected = income, onClick = { income = true; category = "Pocket money" }, label = { Text("Income") })
                }
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Amount in ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Text("CATEGORY", color = lifeMuted, style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { item -> FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.drop(3).forEach { item -> FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) }) }
                }
                OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Note (optional)") })
                Button(
                    onClick = {
                        NovaLifeStore.addMoney(context, amountText.toDoubleOrNull() ?: 0.0, category, note, income)
                        amountText = ""; note = ""; revision++
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                    modifier = Modifier.fillMaxWidth()
                ) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text(if (income) "Save income" else "Save expense") }
            }
        }
        item { SectionLabel("RECENT TRANSACTIONS") }
        if (entries.isEmpty()) {
            item { EmptyCard("No transactions yet", "Add an income or expense above. All records remain on this device.") }
        } else {
            items(entries.take(60), key = { "m" + it.id }) { entry ->
                Card(shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = if (entry.income) Color(0xFF17443E) else Color(0xFF493039)) {
                            Icon(if (entry.income) Icons.Default.Add else Icons.Default.Delete, null, tint = if (entry.income) lifeMint else Color(0xFFFFB0A4), modifier = Modifier.padding(9.dp).size(17.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.category, fontWeight = FontWeight.SemiBold)
                            Text(entry.note.ifBlank { DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(entry.createdAt)) }, style = MaterialTheme.typography.bodySmall, color = lifeMuted)
                        }
                        Text((if (entry.income) "+" else "−") + formatRupees(entry.amount), color = if (entry.income) lifeMint else Color(0xFFFFB0A4), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { NovaLifeStore.deleteMoney(context, entry.id); revision++ }) { Icon(Icons.Default.Delete, "Delete transaction", tint = lifeMuted) }
                    }
                }
            }
        }
        item { Text("NOVA does not connect to your bank or make payments. Entered transactions are personal records, not verified bank statements.", color = lifeMuted, style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun DeviceControlScreen(proxyRunning: Boolean, onProxyChange: (Boolean) -> Unit, onOpenLegacy: (String) -> Unit) {
    val context = LocalContext.current
    val storage = remember { runCatching { formatLifeBytes(StatFs(Environment.getDataDirectory().absolutePath).availableBytes) }.getOrDefault("Unavailable") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DEVICE CONTROL", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Existing NOVA networking and utility tools are available here.", color = lifeMuted)
        Card(shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Router, null, tint = lifeBlue)
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Local HTTP / SOCKS5 proxy", fontWeight = FontWeight.SemiBold)
                        Text(if (proxyRunning) "Running on 127.0.0.1:8787" else "Stopped • starts only when requested", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(
                    onClick = {
                        if (proxyRunning) context.stopService(Intent(context, LocalProxyService::class.java))
                        else {
                            val intent = Intent(context, LocalProxyService::class.java)
                            runCatching {
                                if (android.os.Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
                            }.onFailure {
                                android.widget.Toast.makeText(context, "Android could not start the proxy. Check notification permission and try again.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (proxyRunning) Color(0xFF9C4650) else lifeBlue, contentColor = Color(0xFF0A1020))
                ) { Icon(if (proxyRunning) Icons.Default.Delete else Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(if (proxyRunning) "Stop proxy" else "Start local proxy") }
                Text("This proxy listens on this phone only. It is not a VPN and does not route every app.", color = lifeMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        ActionCard(Icons.Default.Shield, "Private browser", "Open NOVA's built-in browser and offline blocker", "OPEN", { onOpenLegacy("Browser") })
        ActionCard(Icons.Default.Apps, "Everyday tools", "Notes, password generator, SHA-256, calculator and converter", "OPEN", { onOpenLegacy("Tools") })
        ActionCard(Icons.Default.NetworkCheck, "Network diagnostics", "The next networking module will add connection tests and LAN discovery.", "ROADMAP", {})
        ActionCard(Icons.Default.Storage, "Phone storage", storage + " available in internal storage", "INFO", {})
        Text("System-wide firewalling, background automation and device-wide blocking need separate Android permissions and will be added as opt-in modules.", color = lifeMuted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MoreCenterScreen(onOpenLegacy: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("NOVA TOOLBOX", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("One place for the original utilities and future NOVA modules.", color = lifeMuted)
        ActionCard(Icons.Default.Apps, "Original NOVA tools", "Password generator, hashing, calculator, unit converter and quick note", "OPEN", { onOpenLegacy("Tools") })
        ActionCard(Icons.Default.Shield, "Private browser", "WebView browser with bundled local ad and tracker filtering", "OPEN", { onOpenLegacy("Browser") })
        ActionCard(Icons.Default.Router, "Proxy guide", "Open the original proxy and usage instructions", "OPEN", { onOpenLegacy("Proxy") })
        PanelCard("WHAT IS STORED LOCALLY", "NOVA saves your tasks, goals, habits, routines, transactions and timeline in app-private storage.") {
            Text("No NOVA account is required. The new Life OS data is not sent to a server. Backups and encrypted vault features are planned separately and are not included in this release.", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
        }
        PanelCard("VERSION 0.3.0", "Life Command Center foundation") {
            Text("This is the first integrated release. Automation triggers, local servers, encrypted vault, DNS firewall and device bridge are future modules; they are not presented as finished features.", color = lifeMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun openLegacyNova(page: String) {
    // Intentionally empty composable placeholder is not used.
}

private fun formatRupees(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value)
}

private fun formatLifeBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }
    return String.format(Locale.US, "%.1f %s", value, units[index])
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = lifeMuted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun SummaryCard(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = lifeMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = lifeMint, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(detail, color = lifeMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, action: String, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = lifeSurface2) { Icon(icon, null, tint = lifeBlue, modifier = Modifier.padding(10.dp).size(22.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(detail, color = lifeMuted, style = MaterialTheme.typography.bodySmall)
            }
            Text(action, color = lifeBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PanelCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(title, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Text(subtitle, color = lifeMuted, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

@Composable
private fun EmptyCard(title: String, detail: String) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = lifeSurface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(detail, color = lifeMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

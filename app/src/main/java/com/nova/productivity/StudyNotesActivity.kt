package com.nova.productivity

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

private val hubBg = Color(0xFF080D16)
private val hubCard = Color(0xFF141E2D)
private val hubBlue = Color(0xFF9AB4FF)
private val hubMint = Color(0xFF75E0C9)
private val hubMuted = Color(0xFF9BAAC0)

class StudyNotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudyHubScreen() }
    }
}

private data class PdfResult(val id: String, val title: String, val creator: String, val year: String, val description: String)
private data class TopicArticle(val title: String, val extract: String, val url: String)
private data class SavedStudy(val id: String, val title: String, val kind: String, val category: String, val text: String, val source: String, val downloadId: Long)

private object StudyStore {
    private const val PREF = "nova_study_hub"
    private const val KEY = "library"
    private fun array(context: Context) = runCatching {
        JSONArray(context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]")
    }.getOrDefault(JSONArray())
    private fun write(context: Context, data: JSONArray) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, data.toString()).apply()
    }
    fun all(context: Context): List<SavedStudy> {
        val a = array(context)
        return (0 until a.length()).mapNotNull { i ->
            runCatching {
                val o = a.getJSONObject(i)
                SavedStudy(o.optString("id"), o.optString("title"), o.optString("kind"), o.optString("category"),
                    o.optString("text"), o.optString("source"), o.optLong("downloadId", -1L))
            }.getOrNull()
        }.reversed()
    }
    fun saveNote(context: Context, title: String, category: String, text: String, source: String) {
        val a = array(context)
        a.put(JSONObject().put("id", UUID.randomUUID().toString()).put("title", title.ifBlank { "Study note" })
            .put("kind", "NOTE").put("category", category).put("text", text).put("source", source).put("downloadId", -1L))
        write(context, a)
    }
    fun savePdf(context: Context, result: PdfResult, category: String, downloadId: Long, url: String) {
        val a = array(context)
        a.put(JSONObject().put("id", UUID.randomUUID().toString()).put("title", result.title).put("kind", "PDF")
            .put("category", category).put("text", "").put("source", url).put("downloadId", downloadId))
        write(context, a)
    }
    fun remove(context: Context, id: String): SavedStudy? {
        val a = array(context); val kept = JSONArray(); var removed: SavedStudy? = null
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            if (o.optString("id") == id) {
                removed = SavedStudy(o.optString("id"), o.optString("title"), o.optString("kind"), o.optString("category"),
                    o.optString("text"), o.optString("source"), o.optLong("downloadId", -1L))
            } else kept.put(o)
        }
        write(context, kept); return removed
    }
}

private object StudySearch {
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15000; c.readTimeout = 20000; c.instanceFollowRedirects = true
        c.setRequestProperty("User-Agent", "NOVA-Productivity/0.4 Android study hub")
        c.setRequestProperty("Accept", "application/json")
        return try { c.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() } } finally { c.disconnect() }
    }
    fun pdfs(topic: String): List<PdfResult> {
        val safe = topic.replace(Regex("[^A-Za-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()
        if (safe.isBlank()) return emptyList()
        val q = "(title:(" + safe + ") OR subject:(" + safe + ") OR description:(" + safe +
            ")) AND mediatype:texts AND format:pdf"
        val url = "https://archive.org/advancedsearch.php?q=" + enc(q) +
            "&fl[]=identifier&fl[]=title&fl[]=creator&fl[]=year&fl[]=description&rows=15&page=1&output=json"
        val docs = JSONObject(get(url)).optJSONObject("response")?.optJSONArray("docs") ?: JSONArray()
        return (0 until docs.length()).mapNotNull { i ->
            val o = docs.optJSONObject(i) ?: return@mapNotNull null
            val id = o.optString("identifier")
            if (id.isBlank()) return@mapNotNull null
            val d = o.opt("description")
            val description = when (d) {
                is JSONArray -> (0 until d.length()).joinToString(" ") { d.optString(it) }
                is String -> d
                else -> ""
            }
            val c = o.opt("creator")
            val creator = when (c) {
                is JSONArray -> (0 until c.length()).joinToString(", ") { c.optString(it) }
                is String -> c
                else -> ""
            }
            PdfResult(id, o.optString("title", id), creator, o.optString("year"), description.take(240))
        }
    }
    fun articles(topic: String): List<TopicArticle> {
        val url = "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=" + enc(topic) +
            "&gsrlimit=4&prop=extracts&exintro=1&explaintext=1&format=json&formatversion=2"
        val pages = JSONObject(get(url)).optJSONObject("query")?.optJSONArray("pages") ?: JSONArray()
        return (0 until pages.length()).mapNotNull { i ->
            val p = pages.optJSONObject(i) ?: return@mapNotNull null
            val title = p.optString("title"); val extract = p.optString("extract")
            if (title.isBlank() || extract.isBlank()) return@mapNotNull null
            TopicArticle(title, extract.take(6000), "https://en.wikipedia.org/wiki/" + Uri.encode(title.replace(' ', '_')))
        }
    }
    fun pdfUrl(identifier: String): String? {
        val files = JSONObject(get("https://archive.org/metadata/" + enc(identifier))).optJSONArray("files") ?: return null
        val options = ArrayList<Pair<Int, String>>()
        for (i in 0 until files.length()) {
            val f = files.optJSONObject(i) ?: continue
            val name = f.optString("name")
            if (!name.endsWith(".pdf", true) || f.optString("private").equals("true", true)) continue
            options.add((if (f.optString("format").equals("Text PDF", true)) 0 else 1) to name)
        }
        val file = options.sortedBy { it.first }.firstOrNull()?.second ?: return null
        return "https://archive.org/download/" + Uri.encode(identifier) + "/" + Uri.encode(file, "/")
    }
}

private val hubExecutor = Executors.newSingleThreadExecutor()
private val hubHandler = Handler(Looper.getMainLooper())
private fun <T> background(work: () -> T, done: (Result<T>) -> Unit) {
    hubExecutor.execute { val result = runCatching(work); hubHandler.post { done(result) } }
}

private fun downloadPdf(context: Context, result: PdfResult, category: String, done: (Result<Long>) -> Unit) {
    background({
        val url = StudySearch.pdfUrl(result.id) ?: error("No public PDF file was found for this result.")
        val safeName = result.title.replace(Regex("[^A-Za-z0-9._-]"), "_").take(65).ifBlank { "study_notes" }
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(result.title).setDescription("NOVA Study Hub • Public PDF")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true).setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "NOVA_" + safeName + ".pdf")
        val id = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        StudyStore.savePdf(context, result, category, id, url)
        id
    }, done)
}

private fun openPdf(context: Context, item: SavedStudy) {
    val uri = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).getUriForDownloadedFile(item.downloadId)
    if (uri == null) {
        Toast.makeText(context, "Still downloading, or the download failed. Check notifications.", Toast.LENGTH_LONG).show()
        return
    }
    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/pdf").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    runCatching { context.startActivity(Intent.createChooser(intent, "Open study PDF")) }
        .onFailure { Toast.makeText(context, "Install a PDF viewer to open this file.", Toast.LENGTH_LONG).show() }
}

private fun pdfStatus(context: Context, id: Long): String {
    val c = runCatching { (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(DownloadManager.Query().setFilterById(id)) }.getOrNull()
        ?: return "Not found"
    c.use {
        if (!it.moveToFirst()) return "Not found"
        return when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
            DownloadManager.STATUS_SUCCESSFUL -> "Ready offline"
            DownloadManager.STATUS_RUNNING -> "Downloading"
            DownloadManager.STATUS_PENDING -> "Waiting"
            DownloadManager.STATUS_PAUSED -> "Paused"
            else -> "Download error"
        }
    }
}

private fun quickRevision(article: TopicArticle, level: String): String {
    val points = article.extract.replace(Regex("\\s+"), " ").split(Regex("(?<=[.!?])\\s+")).filter { it.length > 25 }.take(7)
    return buildString {
        append(article.title).append("\n\nQUICK REVISION • ").append(level.uppercase(Locale.getDefault())).append("\n\nKEY POINTS\n")
        if (points.isEmpty()) append(article.extract.take(1200)) else points.forEach { append("• ").append(it).append("\n") }
        append("\nSource: ").append(article.url)
        append("\n\nIntroductory revision extract only. Check your syllabus and textbook for complete coverage.")
    }
}

private fun detailedNotes(article: TopicArticle, level: String) =
    article.title + "\n\nDETAILED INTRODUCTORY NOTES • " + level + "\n\n" + article.extract +
        "\n\nSource: " + article.url + "\n\nThis is a source extract, not a complete textbook or syllabus-specific chapter."

private fun openProvider(context: Context, domain: String, topic: String) {
    val q = Uri.encode("site:" + domain + " " + topic + " free study PDF")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + q))) }
        .onFailure { Toast.makeText(context, "No browser available.", Toast.LENGTH_SHORT).show() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyHubScreen() {
    val context = LocalContext.current
    var page by remember { mutableStateOf("Search") }
    var topic by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("Class 10") }
    var subject by remember { mutableStateOf("All") }
    var busy by remember { mutableStateOf(false) }
    var busyId by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var pdfs by remember { mutableStateOf(emptyList<PdfResult>()) }
    var articles by remember { mutableStateOf(emptyList<TopicArticle>()) }
    var libraryVersion by remember { mutableStateOf(0) }
    var opened by remember { mutableStateOf<SavedStudy?>(null) }
    var ownTitle by remember { mutableStateOf("") }
    var ownText by remember { mutableStateOf("") }
    val library = remember(libraryVersion) { StudyStore.all(context) }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = hubBlue, secondary = hubMint, background = hubBg, surface = hubCard,
        onSurface = Color.White, onSurfaceVariant = hubMuted
    )) {
        Column(Modifier.fillMaxSize().background(hubBg)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { (context as? Activity)?.finish() }) { Text("‹ Back", color = hubBlue) }
                Column(Modifier.weight(1f)) {
                    Text("NOVA STUDY HUB", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                    Text("Free PDFs • Revision • Offline library", color = hubMuted, style = MaterialTheme.typography.labelSmall)
                }
                Surface(color = Color(0xFF17443E), shape = RoundedCornerShape(30.dp)) {
                    Text("FREE", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = hubMint, style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = page == "Search", onClick = { page = "Search" }, label = { Text("Find notes") })
                FilterChip(selected = page == "Library", onClick = { page = "Library" }, label = { Text("Offline library (" + library.size + ")") })
            }
            Divider(color = hubCard)
            if (page == "Search") {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text("Learn any topic", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Search public PDF collections, save revision notes and download files for offline use. No API key or account required.", color = hubMuted, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(topic, { topic = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Enter a topic") }, placeholder = { Text("e.g. Electricity, Python, photosynthesis") }, singleLine = true)
                    Text("LEVEL", color = hubMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Class 10", "Class 12", "School", "College", "Beginner", "Advanced").forEach {
                            FilterChip(selected = level == it, onClick = { level = it }, label = { Text(it) })
                        }
                    }
                    Text("SUBJECT", color = hubMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Science", "Maths", "Computing", "English", "History").forEach {
                            FilterChip(selected = subject == it, onClick = { subject = it }, label = { Text(it) })
                        }
                    }
                    Button(
                        onClick = {
                            if (topic.isBlank()) status = "Enter a topic first."
                            else {
                                val query = topic.trim() + if (subject == "All") "" else " " + subject
                                busy = true; status = ""; pdfs = emptyList(); articles = emptyList()
                                background({ Pair(StudySearch.pdfs(query), StudySearch.articles(query)) }) { result ->
                                    busy = false
                                    result.onSuccess {
                                        pdfs = it.first; articles = it.second
                                        status = if (pdfs.isEmpty() && articles.isEmpty()) "No results. Try a shorter topic or use the official source shortcuts." else "Search finished. Verify the publisher and licence before reusing a file."
                                    }.onFailure { status = "Search error: " + (it.message ?: "Network unavailable") }
                                }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (busy) "Searching…" else "Search free PDFs and notes") }
                    if (busy) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Searching public sources…", color = hubMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    if (status.isNotBlank()) Text(status, color = hubMuted, style = MaterialTheme.typography.bodySmall)
                    if (pdfs.isNotEmpty()) {
                        Text("PUBLIC PDF RESULTS • " + pdfs.size, color = hubMint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        pdfs.forEach { item ->
                            Card(colors = CardDefaults.cardColors(containerColor = hubCard), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(listOf(item.creator, item.year).filter { it.isNotBlank() }.joinToString(" • "), color = hubMuted, style = MaterialTheme.typography.labelSmall)
                                    if (item.description.isNotBlank()) Text(item.description, color = hubMuted, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Internet Archive", color = hubMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                                        Button(onClick = {
                                            busyId = item.id
                                            downloadPdf(context, item, subject) { result ->
                                                busyId = ""
                                                result.onSuccess {
                                                    libraryVersion++
                                                    Toast.makeText(context, "Download started. Find it in Downloads or the NOVA library.", Toast.LENGTH_LONG).show()
                                                }.onFailure { Toast.makeText(context, it.message ?: "Download failed", Toast.LENGTH_LONG).show() }
                                            }
                                        }, enabled = busyId.isBlank()) { Text(if (busyId == item.id) "Preparing…" else "Download PDF") }
                                    }
                                }
                            }
                        }
                    }
                    if (articles.isNotEmpty()) {
                        Text("TOPIC EXPLAINERS • " + articles.size, color = hubMint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        articles.forEach { article ->
                            Card(colors = CardDefaults.cardColors(containerColor = hubCard), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text(article.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(article.extract.take(750), color = hubMuted, style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Button(onClick = {
                                            StudyStore.saveNote(context, article.title + " • Quick revision", subject, quickRevision(article, level), article.url)
                                            libraryVersion++
                                            Toast.makeText(context, "Quick notes saved offline.", Toast.LENGTH_SHORT).show()
                                        }, modifier = Modifier.weight(1f)) { Text("Quick notes") }
                                        OutlinedButton(onClick = {
                                            StudyStore.saveNote(context, article.title + " • Detailed", subject, detailedNotes(article, level), article.url)
                                            libraryVersion++
                                            Toast.makeText(context, "Detailed notes saved offline.", Toast.LENGTH_SHORT).show()
                                        }, modifier = Modifier.weight(1f)) { Text("Detailed notes") }
                                    }
                                    TextButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url))) } }) {
                                        Text("Open source ↗", color = hubBlue)
                                    }
                                }
                            }
                        }
                    }
                    Text("OFFICIAL FREE SOURCE SHORTCUTS", color = hubMint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text("Opens a topic search on the provider's website. Availability and download rights depend on the publisher.", color = hubMuted, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        OutlinedButton(onClick = { openProvider(context, "ncert.nic.in", topic.ifBlank { "school textbook" }) }, modifier = Modifier.weight(1f)) { Text("NCERT") }
                        OutlinedButton(onClick = { openProvider(context, "diksha.gov.in", topic.ifBlank { "learning material" }) }, modifier = Modifier.weight(1f)) { Text("DIKSHA") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        OutlinedButton(onClick = { openProvider(context, "swayam.gov.in", topic.ifBlank { "course" }) }, modifier = Modifier.weight(1f)) { Text("SWAYAM") }
                        OutlinedButton(onClick = { openProvider(context, "openstax.org", topic.ifBlank { "textbook" }) }, modifier = Modifier.weight(1f)) { Text("OpenStax") }
                    }
                    Text("Wikipedia extracts are introductory source material, not complete syllabus notes. Check the edition, publisher and licence before downloading or sharing files.", color = hubMuted, style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text("YOUR OFFLINE LIBRARY", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Saved notes are stored privately by NOVA. PDFs go to your phone's Downloads folder and can be opened offline.", color = hubMuted, style = MaterialTheme.typography.bodySmall)
                    Card(colors = CardDefaults.cardColors(containerColor = hubCard), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("WRITE YOUR OWN NOTE", color = hubMint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            OutlinedTextField(ownTitle, { ownTitle = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
                            OutlinedTextField(ownText, { ownText = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Write or paste notes") }, minLines = 3)
                            Button(onClick = {
                                StudyStore.saveNote(context, ownTitle, subject, ownText.trim(), "Created in NOVA")
                                ownTitle = ""; ownText = ""; libraryVersion++
                                Toast.makeText(context, "Saved on this phone.", Toast.LENGTH_SHORT).show()
                            }, enabled = ownText.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Save note offline") }
                        }
                    }
                    if (library.isEmpty()) {
                        Text("No saved items yet. Search a topic to save notes or download a PDF.", color = hubMuted)
                    } else library.forEach { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = hubCard), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text((if (item.kind == "PDF") "PDF" else "SAVED NOTE") + " • " + item.category, color = hubMuted, style = MaterialTheme.typography.labelSmall)
                                if (item.kind == "PDF") Text(pdfStatus(context, item.downloadId), color = hubMint, style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Button(onClick = { if (item.kind == "PDF") openPdf(context, item) else opened = item }, modifier = Modifier.weight(1f)) {
                                        Text(if (item.kind == "PDF") "Open PDF" else "Read notes")
                                    }
                                    OutlinedButton(onClick = {
                                        StudyStore.remove(context, item.id)?.let { removed ->
                                            if (removed.kind == "PDF" && removed.downloadId > 0) {
                                                (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).remove(removed.downloadId)
                                            }
                                        }
                                        libraryVersion++
                                    }) { Text("Remove") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    opened?.let { item ->
        AlertDialog(
            onDismissRequest = { opened = null },
            title = { Text(item.title) },
            text = { Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())) { Text(item.text) } },
            confirmButton = { TextButton(onClick = { opened = null }) { Text("Close") } },
            containerColor = hubCard
        )
    }
}

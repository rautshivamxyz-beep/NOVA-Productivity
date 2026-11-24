package com.nova.productivity

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NovaTask(val id: Long, val title: String, val done: Boolean, val createdAt: Long)
data class NovaGoal(val id: Long, val title: String, val progress: Int, val createdAt: Long)
data class NovaHabit(val id: Long, val title: String, val doneDate: String)
data class NovaMoneyEntry(val id: Long, val amount: Double, val category: String, val note: String, val income: Boolean, val createdAt: Long)
data class NovaRoutine(val id: Long, val title: String, val detail: String, val doneDate: String)
data class NovaTimelineEntry(val id: Long, val title: String, val detail: String, val createdAt: Long)

/**
 * Small local-first persistence layer for NOVA's first Life OS release.
 * Data is stored in app-private SharedPreferences as JSON; nothing is uploaded.
 */
object NovaLifeStore {
    private const val PREFS = "nova_life_os"
    private const val TASKS = "tasks"
    private const val GOALS = "goals"
    private const val HABITS = "habits"
    private const val MONEY = "money"
    private const val ROUTINES = "routines"
    private const val TIMELINE = "timeline"

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun read(context: Context, key: String): JSONArray =
        runCatching { JSONArray(prefs(context).getString(key, "[]") ?: "[]") }.getOrDefault(JSONArray())
    private fun write(context: Context, key: String, value: JSONArray) {
        prefs(context).edit().putString(key, value.toString()).apply()
    }

    fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun event(context: Context, title: String, detail: String) {
        val all = read(context, TIMELINE)
        all.put(JSONObject().put("id", System.currentTimeMillis()).put("title", title).put("detail", detail).put("createdAt", System.currentTimeMillis()))
        while (all.length() > 80) all.remove(0)
        write(context, TIMELINE, all)
    }

    fun tasks(context: Context): List<NovaTask> {
        val data = read(context, TASKS)
        return (0 until data.length()).mapNotNull { i ->
            runCatching {
                val o = data.getJSONObject(i)
                NovaTask(o.optLong("id"), o.optString("title"), o.optBoolean("done"), o.optLong("createdAt"))
            }.getOrNull()
        }.sortedByDescending { it.createdAt }
    }

    fun addTask(context: Context, title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val all = read(context, TASKS)
        val now = System.currentTimeMillis()
        all.put(JSONObject().put("id", now).put("title", clean).put("done", false).put("createdAt", now))
        write(context, TASKS, all)
        event(context, "Task added", clean)
    }

    fun setTaskDone(context: Context, id: Long, done: Boolean) {
        val all = read(context, TASKS)
        for (i in 0 until all.length()) {
            val o = all.optJSONObject(i) ?: continue
            if (o.optLong("id") == id) o.put("done", done)
        }
        write(context, TASKS, all)
        event(context, if (done) "Task completed" else "Task reopened", tasks(context).firstOrNull { it.id == id }?.title ?: "Task")
    }

    fun deleteTask(context: Context, id: Long) {
        val all = read(context, TASKS)
        val kept = JSONArray()
        for (i in 0 until all.length()) {
            val o = all.optJSONObject(i) ?: continue
            if (o.optLong("id") != id) kept.put(o)
        }
        write(context, TASKS, kept)
    }

    fun goals(context: Context): List<NovaGoal> {
        val data = read(context, GOALS)
        return (0 until data.length()).mapNotNull { i ->
            runCatching {
                val o = data.getJSONObject(i)
                NovaGoal(o.optLong("id"), o.optString("title"), o.optInt("progress").coerceIn(0, 100), o.optLong("createdAt"))
            }.getOrNull()
        }.sortedByDescending { it.createdAt }
    }

    fun addGoal(context: Context, title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val all = read(context, GOALS)
        val now = System.currentTimeMillis()
        all.put(JSONObject().put("id", now).put("title", clean).put("progress", 0).put("createdAt", now))
        write(context, GOALS, all)
        event(context, "Goal created", clean)
    }

    fun changeGoalProgress(context: Context, id: Long, delta: Int) {
        val all = read(context, GOALS)
        for (i in 0 until all.length()) {
            val o = all.optJSONObject(i) ?: continue
            if (o.optLong("id") == id) o.put("progress", (o.optInt("progress") + delta).coerceIn(0, 100))
        }
        write(context, GOALS, all)
        event(context, "Goal progress updated", goals(context).firstOrNull { it.id == id }?.title ?: "Goal")
    }

    fun habits(context: Context): List<NovaHabit> {
        val data = read(context, HABITS)
        return (0 until data.length()).mapNotNull { i ->
            runCatching {
                val o = data.getJSONObject(i)
                NovaHabit(o.optLong("id"), o.optString("title"), o.optString("doneDate"))
            }.getOrNull()
        }.sortedBy { it.title.lowercase(Locale.getDefault()) }
    }

    fun addHabit(context: Context, title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val all = read(context, HABITS)
        val now = System.currentTimeMillis()
        all.put(JSONObject().put("id", now).put("title", clean).put("doneDate", ""))
        write(context, HABITS, all)
        event(context, "Habit added", clean)
    }

    fun toggleHabitToday(context: Context, id: Long) {
        val all = read(context, HABITS)
        val today = todayKey()
        for (i in 0 until all.length()) {
            val o = all.optJSONObject(i) ?: continue
            if (o.optLong("id") == id) o.put("doneDate", if (o.optString("doneDate") == today) "" else today)
        }
        write(context, HABITS, all)
        event(context, "Habit check-in", habits(context).firstOrNull { it.id == id }?.title ?: "Habit")
    }

    fun routines(context: Context): List<NovaRoutine> {
        var data = read(context, ROUTINES)
        if (data.length() == 0 && !prefs(context).getBoolean("routines_seeded", false)) {
            val defaults = listOf(
                "Morning reset" to "Review priorities and prepare for the day",
                "Focus sprint" to "Work on one important task without distractions",
                "Evening review" to "Check progress and plan tomorrow"
            )
            data = JSONArray()
            defaults.forEachIndexed { index, pair ->
                data.put(JSONObject().put("id", System.currentTimeMillis() + index).put("title", pair.first).put("detail", pair.second).put("doneDate", ""))
            }
            write(context, ROUTINES, data)
            prefs(context).edit().putBoolean("routines_seeded", true).apply()
        }
        return (0 until data.length()).mapNotNull { i ->
            runCatching {
                val o = data.getJSONObject(i)
                NovaRoutine(o.optLong("id"), o.optString("title"), o.optString("detail"), o.optString("doneDate"))
            }.getOrNull()
        }
    }

    fun toggleRoutineToday(context: Context, id: Long) {
        val all = read(context, ROUTINES)
        val today = todayKey()
        for (i in 0 until all.length()) {
            val o = all.optJSONObject(i) ?: continue
            if (o.optLong("id") == id) o.put("doneDate", if (o.optString("doneDate") == today) "" else today)
        }
        write(context, ROUTINES, all)
        event(context, "Routine updated", routines(context).firstOrNull { it.id == id }?.title ?: "Routine")
    }

    fun money(context: Context): List<NovaMoneyEntry> {
        val data = read(context, MONEY)
        return (0 until data.length()).mapNotNull { i ->
            runCatching {
                val o = data.getJSONObject(i)
                NovaMoneyEntry(o.optLong("id"), o.optDouble("amount"), o.optString("category"), o.optString("note"), o.optBoolean("income"), o.optLong("createdAt"))
            }.getOrNull()
        }.sortedByDescending { it.createdAt }
    }

    fun addMoney(context: Context, amount: Double, category: String, note: String, income: Boolean) {
        if (!amount.isFinite() || amount <= 0.0) return
        val all = read(context, MONEY)
        val now = System.currentTimeMillis()
        all.put(JSONObject().put("id", now).put("amount", amount).put("category", category).put("note", note.trim()).put("income", income).put("createdAt", now))
        write(context, MONEY, all)
        event(context, if (income) "Income recorded" else "Expense recorded", category + " • ₹" + amount)
    }

    fun deleteMoney(context: Context, id: Long) {
        val all = read(context, MONEY)
        val kept = JSONArray()
        for (i in 0 until all.length()) {
            val o = all.optJSONObject(i) ?: continue
            if (o.optLong("id") != id) kept.put(o)
        }
        write(context, MONEY, kept)
    }

    fun timeline(context: Context): List<NovaTimelineEntry> {
        val data = read(context, TIMELINE)
        return (0 until data.length()).mapNotNull { i ->
            runCatching {
                val o = data.getJSONObject(i)
                NovaTimelineEntry(o.optLong("id"), o.optString("title"), o.optString("detail"), o.optLong("createdAt"))
            }.getOrNull()
        }.sortedByDescending { it.createdAt }.take(12)
    }
}

package com.example.dailychallenge

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val timeLabel: String,
    val category: String,
    val isCompleted: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("DailyChallengePrefs", Context.MODE_PRIVATE)

    private val _isSetupComplete = MutableStateFlow(prefs.getBoolean("setup_complete", false))
    val isSetupComplete: StateFlow<Boolean> = _isSetupComplete

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _streak = MutableStateFlow(prefs.getInt("streak", 0))
    val streak: StateFlow<Int> = _streak

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var mInterstitialAd: InterstitialAd? = null

    init {
        checkDailyReset()
        loadInterstitial(application)
    }

    private fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        // Using Test Interstitial ID from AdMob
        InterstitialAd.load(context, "ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { mInterstitialAd = ad }
            override fun onAdFailedToLoad(error: LoadAdError) { mInterstitialAd = null }
        })
    }

    private fun showInterstitial(activityContext: android.app.Activity) {
        mInterstitialAd?.show(activityContext)
        loadInterstitial(getApplication()) // preload next instance
    }

    private fun checkDailyReset() {
        if (!prefs.getBoolean("setup_complete", false)) return

        val lastDate = prefs.getString("last_date", "")
        val today = dateFormat.format(Date())

        if (lastDate != today) {
            generateTasks()
            prefs.edit().putString("last_date", today).apply()
        } else {
            generateTasks() 
        }
    }

    fun completeSetup(count: Int, categories: Set<String>) {
        prefs.edit().apply {
            putBoolean("setup_complete", true)
            putInt("task_count", count)
            putStringSet("categories", categories)
            putString("last_date", dateFormat.format(Date()))
        }.apply()
        
        _isSetupComplete.value = true
        generateTasks()
    }

    private fun generateTasks() {
        val count = prefs.getInt("task_count", 3)
        val categories = prefs.getStringSet("categories", setOf("Productivity"))?.toList() ?: listOf("Productivity")
        
        val allPossible = listOf(
            Task("1", "Read a Chapter", "Enjoy a book of your choice.", "20 min", "Fun"),
            Task("2", "Quick Walk", "Step outside for fresh air.", "15 min", "Fitness"),
            Task("3", "Clear Inbox", "Archive or reply to emails.", "15 min", "Productivity"),
            Task("4", "Stretch", "Loosen up your muscles.", "10 min", "Fitness"),
            Task("5", "Doodle", "Draw whatever comes to mind.", "10 min", "Fun"),
            Task("6", "Plan Tomorrow", "Write down 3 goals for tomorrow.", "5 min", "Productivity")
        )
        
        val filtered = allPossible.filter { categories.contains(it.category) }.shuffled().take(count)
        _tasks.value = if (filtered.isEmpty()) allPossible.shuffled().take(count) else filtered
    }

    fun toggleTask(taskId: String, activityContext: android.app.Activity) {
        val currentTasks = _tasks.value
        val updatedTasks = currentTasks.map { task ->
            if (task.id == taskId) task.copy(isCompleted = !task.isCompleted)
            else task
        }
        _tasks.value = updatedTasks
        
        val allPassedBefore = currentTasks.all { it.isCompleted } && currentTasks.isNotEmpty()
        val allPassedNow = updatedTasks.all { it.isCompleted } && updatedTasks.isNotEmpty()
        
        if (!allPassedBefore && allPassedNow) {
            val newStreak = _streak.value + 1
            _streak.value = newStreak
            prefs.edit().putInt("streak", newStreak).apply()
            
            showInterstitial(activityContext)
            Toast.makeText(activityContext, "All tasks completed! +1 Streak \uD83D\uDD25", Toast.LENGTH_SHORT).show()
        }
    }
}

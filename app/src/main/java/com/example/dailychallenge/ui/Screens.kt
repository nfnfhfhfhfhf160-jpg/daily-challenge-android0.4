package com.example.dailychallenge.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dailychallenge.MainViewModel
import com.example.dailychallenge.Task
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(targetValue = if (startAnim) 1f else 0f, animationSpec = tween(1000))
    val scaleAnim by animateFloatAsState(targetValue = if (startAnim) 1f else 0.5f, animationSpec = tween(1000, easing = FastOutSlowInEasing))

    LaunchedEffect(Unit) {
        startAnim = true
        delay(2000)
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E2C)), contentAlignment = Alignment.Center) {
        Text("Daily Challenge", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.scale(scaleAnim).alpha(alphaAnim))
    }
}

@Composable
fun SetupScreen(viewModel: MainViewModel, onComplete: () -> Unit) {
    var selectedCount by remember { mutableStateOf(3) }
    var selectedCategories by remember { mutableStateOf(setOf("Productivity")) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF12121A)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Setup Your Day", color = Color.White, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Text("How many tasks per day?", color = Color.LightGray)
        Row(modifier = Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(1, 2, 3, 5).forEach { count ->
                Button(onClick = { selectedCount = count }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedCount == count) Color(0xFF6366F1) else Color.DarkGray)) {
                    Text("$count")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Categories", color = Color.LightGray)
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            listOf("Fun", "Fitness", "Productivity").forEach { cat ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                    val newSet = selectedCategories.toMutableSet()
                    if (newSet.contains(cat)) newSet.remove(cat) else newSet.add(cat)
                    if (newSet.isNotEmpty()) selectedCategories = newSet
                }) {
                    Checkbox(checked = selectedCategories.contains(cat), onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6366F1)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = cat, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { viewModel.completeSetup(selectedCount, selectedCategories); onComplete() }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))) {
            Text("Start Challenge", fontSize = 18.sp)
        }
    }
}

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val context = LocalContext.current

    val (completed, active) = tasks.partition { it.isCompleted }

    Column(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1E1E2C), Color(0xFF12121A)))).padding(top = 48.dp, start = 16.dp, end = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Daily Challenge", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Streak: $streak Days \uD83D\uDD25", color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.padding(bottom = 24.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(completed, key = { it.id }) { task -> TaskCard(task, onToggle = { context.findActivity()?.let { viewModel.toggleTask(task.id, it) } }) }
            items(active, key = { it.id }) { task -> TaskCard(task, onToggle = { context.findActivity()?.let { viewModel.toggleTask(task.id, it) } }) }
        }

        AdMobBanner()
    }
}

@Composable
fun TaskCard(task: Task, onToggle: () -> Unit) {
    val scale by animateFloatAsState(if (task.isCompleted) 0.95f else 1f, tween(300))
    val cardColor by animateColorAsState(if (task.isCompleted) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f), tween(300))

    Box(modifier = Modifier.fillMaxWidth().scale(scale).clip(RoundedCornerShape(20.dp)).background(cardColor).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onToggle() }.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(task.description, color = Color(0xFFAAAAAA), fontSize = 14.sp)
            }
            Text(task.timeLabel, color = Color.White, fontSize = 12.sp, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun AdMobBanner() {
    AndroidView(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), factory = { context ->
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = "ca-app-pub-3940256099942544/6300978111"
            loadAd(AdRequest.Builder().build())
        }
    })
}

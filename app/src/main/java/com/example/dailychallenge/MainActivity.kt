package com.example.dailychallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dailychallenge.ui.HomeScreen
import com.example.dailychallenge.ui.SetupScreen
import com.example.dailychallenge.ui.SplashScreen
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Google Mobile Ads SDK (AdMob)
        MobileAds.initialize(this) {}
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val isSetupComplete by viewModel.isSetupComplete.collectAsState()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onTimeout = {
                if (isSetupComplete) {
                    navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                } else {
                    navController.navigate("setup") { popUpTo("splash") { inclusive = true } }
                }
            })
        }
        composable("setup") {
            SetupScreen(viewModel = viewModel, onComplete = {
                navController.navigate("home") { popUpTo("setup") { inclusive = true } }
            })
        }
        composable("home") {
            HomeScreen(viewModel = viewModel)
        }
    }
}

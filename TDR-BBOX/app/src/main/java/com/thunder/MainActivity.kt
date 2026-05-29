package com.thunder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thunder.navigation.Screen
import com.thunder.ui.screens.HomeScreen
import com.thunder.ui.screens.LoginScreen
import com.thunder.ui.screens.PermissionScreen
import com.thunder.ui.theme.Mondoinject1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen API before super.onCreate
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            Mondoinject1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(activity = this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(activity: ComponentActivity) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Permissions.route
    ) {
        composable(Screen.Permissions.route) {
            PermissionScreen(
                navController = navController,
                activity = activity
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
    }
}

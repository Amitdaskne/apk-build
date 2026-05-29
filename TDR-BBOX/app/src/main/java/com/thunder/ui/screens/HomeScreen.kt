package com.thunder.ui.screens


import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import com.thunder.R
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.thunder.view.main.BlackBoxLoader
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thunder.navigation.Screen
import com.thunder.utils.PrefsManager
import com.thunder.utils.cloneutils
import com.thunder.kuroapi.Sapi
import com.thunder.kuroapi.KuroApi
import com.thunder.LOGS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun HomeScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Logout dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Game packages
    val game1Package = "com.pubg.imobile"
    val game2Package = "com.tencent.ig"
    
    // Social media packages
    val facebookPackage = "com.facebook.katana"
    val twitterPackage = "com.twitter.android"
    
    // Installation states
    var game1Installed by remember { mutableStateOf(cloneutils.isAppInstalled(game1Package)) }
    var game2Installed by remember { mutableStateOf(cloneutils.isAppInstalled(game2Package)) }
    var facebookCloned by remember { mutableStateOf(cloneutils.isAppInstalled(facebookPackage)) }
    var twitterCloned by remember { mutableStateOf(cloneutils.isAppInstalled(twitterPackage)) }
    
    // Loading states
    var game1Loading by remember { mutableStateOf(false) }
    var game2Loading by remember { mutableStateOf(false) }
    var facebookLoading by remember { mutableStateOf(false) }
    var twitterLoading by remember { mutableStateOf(false) }
    
    // Error states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    // Progress dialog states
    var showProgressDialog by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }
    var progressValue by remember { mutableStateOf(0) }
    
    // Animation for screen entrance
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0f) }
    
    // Background login verification
    LaunchedEffect(Unit) {
        // Check if user is logged in
        if (!PrefsManager.isLoggedIn(context)) {
            LOGS.warn("User not logged in, redirecting to login screen")
            navController?.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
            return@LaunchedEffect
        }
        
        // Get saved license key
        val savedKey = PrefsManager.getLicenseKey(context)
        if (savedKey.isNullOrBlank()) {
            LOGS.warn("No license key found, redirecting to login screen")
            PrefsManager.clearAllData(context)
            navController?.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
            return@LaunchedEffect
        }
        
        // Verify license key with API in background
        launch(Dispatchers.IO) {
            try {
                val loginurl = Sapi.getbaseurl()
                val kuroApi = KuroApi(
                    userkey = savedKey,
                    baseurl = loginurl,
                    Lisence = "Vm8Lk7Uj2JmsjCPVPVjrLa7zgfx3uz9E"
                )
                
                LOGS.info("Verifying license key in background...")
                val isValid = kuroApi.IsUSerValaid()
                
                if (!isValid) {
                    LOGS.error("License key validation failed, redirecting to login")
                    withContext(Dispatchers.Main) {
                        PrefsManager.clearAllData(context)
                        navController?.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                } else {
                    LOGS.info("License key verified successfully")
                    // Optionally refresh user data
                    val database = kuroApi.getDatabase()
                    if (database != null && database.status == true) {
                        withContext(Dispatchers.Main) {
                            PrefsManager.saveUserData(
                                context,
                                database.data?.appName,
                                database.data?.expired_date
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                LOGS.error("Error verifying license key: ${e.message}")
                e.printStackTrace()
                // On network error, allow user to continue but log the issue
                // You can choose to redirect to login on error if preferred
                // For now, we'll allow offline access if user was previously logged in
            }
        }
        
        // Animation
        launch {
            alpha.animateTo(1f, animationSpec = tween(500))
        }
        launch {
            scale.animateTo(1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ))
        }
    }
    
    // Error Dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "Unknown error occurred") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Progress Dialog
    if (showProgressDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismissing during progress */ },
            title = { Text("Processing...") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = progressMessage,
                        textAlign = TextAlign.Center
                    )
                    LinearProgressIndicator(
                        progress = { progressValue / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "$progressValue%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {}
        )
    }
    
    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout? You'll need to enter your license key again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        PrefsManager.clearAllData(context)
                        showLogoutDialog = false
                        navController?.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale.value)
                .alpha(alpha.value),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Legal Loader 4.1",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Game Mod & Clone Manager",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Logout button
                    if (navController != null) {
                        IconButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_lock_power_off),
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Game Injection Section
            item {
                SectionHeader(
                    title = "Game Injection",
                    subtitle = "Clone and inject mods into your games"
                )
            }

            // Game 1 Card
            item {
                GameInjectionCard(
                    gameName = "Battlegroundindia",
                    gamePackage = game1Package,
                    isInstalled = game1Installed,
                    isLoading = game1Loading,
                    onInstallClick = {
                        // Check if game is installed on system
                        if (!cloneutils.ispackage_installed_on_system(context, game1Package)) {
                            errorMessage = "Game not installed on system. Please install the original game first."
                            showErrorDialog = true
                            return@GameInjectionCard
                        }

                        game1Loading = true
                        coroutineScope.launch {
                            try {
                                // Check external OBB
                                if (!cloneutils.isexternalObb(game1Package)) {
                                    withContext(Dispatchers.Main) {
                                        game1Loading = false
                                        Toast.makeText(
                                            context,
                                            "OBB file not found for $game1Package",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return@launch
                                }

                                // Check internal OBB
                                if (!cloneutils.isinternalObb(game1Package)) {
                                    // Need to copy OBB
                                    withContext(Dispatchers.Main) {
                                        showProgressDialog = true
                                        progressMessage = "Copying OBB files..."
                                        progressValue = 0
                                    }

                                    cloneutils.CopyObb(
                                        packageName = game1Package,
                                        copySuccess = {
                                            // OBB copy completed, now setup loader
                                            coroutineScope.launch(Dispatchers.Main) {
                                                progressMessage = "Downloading setup files..."
                                                progressValue = 0
                                            }

                                            // Get download link from Sapi
                                            val downloadUrl = Sapi.libdownloadlink()

                                            coroutineScope.launch {
                                                cloneutils.SetupLoader(
                                                    context = context,
                                                    downloadUrl = downloadUrl,
                                                    onSuccess = {
                                                        // Setup complete, install package
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            progressMessage = "Installing game..."
                                                            progressValue = 100
                                                        }

                                                        cloneutils.installpackage(
                                                            packageName = game1Package,
                                                            sucess = {
                                                                coroutineScope.launch(Dispatchers.Main) {
                                                                    game1Installed = true
                                                                    game1Loading = false
                                                                    showProgressDialog = false
                                                                    // Launch game after installation
                                                                    cloneutils.Launchapp(game1Package)
                                                                }
                                                            },
                                                            fail = { error ->
                                                                coroutineScope.launch(Dispatchers.Main) {
                                                                    game1Loading = false
                                                                    showProgressDialog = false
                                                                    errorMessage = error?.message ?: "Failed to install game"
                                                                    showErrorDialog = true
                                                                }
                                                            }
                                                        )
                                                    },
                                                    onFailure = { errorMsg ->
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            game1Loading = false
                                                            showProgressDialog = false
                                                            errorMessage = errorMsg ?: "Failed to download setup files"
                                                            showErrorDialog = true
                                                        }
                                                    },
                                                    zippass = "4444", // No password
                                                    onCopyProcess = { progress ->
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            progressValue = progress
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        onFailure = { error ->
                                            coroutineScope.launch(Dispatchers.Main) {
                                                game1Loading = false
                                                showProgressDialog = false
                                                errorMessage = error?.message ?: "Failed to copy OBB files"
                                                showErrorDialog = true
                                            }
                                        },
                                        copyProgress = { progress ->
                                            coroutineScope.launch(Dispatchers.Main) {
                                                progressValue = progress
                                            }
                                        }
                                    )
                                } else {
                                    // Internal OBB exists, proceed with setup loader
                                    withContext(Dispatchers.Main) {
                                        showProgressDialog = true
                                        progressMessage = "Downloading setup files..."
                                        progressValue = 0
                                    }

                                    val downloadUrl = Sapi.libdownloadlink()

                                    coroutineScope.launch {
                                        cloneutils.SetupLoader(
                                            context = context,
                                            downloadUrl = downloadUrl,
                                            onSuccess = {
                                                // Setup complete, install package
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    progressMessage = "Installing game..."
                                                    progressValue = 100
                                                }

                                                cloneutils.installpackage(
                                                    packageName = game1Package,
                                                    sucess = {
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            game1Installed = true
                                                            game1Loading = false
                                                            showProgressDialog = false
                                                            // Launch game after installation
                                                            cloneutils.Launchapp(game1Package)
                                                        }
                                                    },
                                                    fail = { error ->
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            game1Loading = false
                                                            showProgressDialog = false
                                                            errorMessage = error?.message ?: "Failed to install game"
                                                            showErrorDialog = true
                                                        }
                                                    }
                                                )
                                            },
                                            onFailure = { errorMsg ->
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    game1Loading = false
                                                    showProgressDialog = false
                                                    errorMessage = errorMsg ?: "Failed to download setup files"
                                                    showErrorDialog = true
                                                }
                                            },
                                            zippass = "", // No password
                                            onCopyProcess = { progress ->
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    progressValue = progress
                                                }
                                            }
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    game1Loading = false
                                    showProgressDialog = false
                                    errorMessage = "Error: ${e.message}"
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    onRunClick = {
                        // Show progress dialog
                        coroutineScope.launch(Dispatchers.Main) {
                            showProgressDialog = true
                            progressMessage = "Setting up game..."
                            progressValue = 0
                        }
                        
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // Get download link from Sapi
                                val downloadUrl = Sapi.libdownloadlink()
                                
                                cloneutils.SetupLoader(
                                    context = context,
                                    downloadUrl = downloadUrl,
                                    onSuccess = {
                                        // Setup complete, launch game
                                        //BlackBoxLoader.loadSO(context, game1Package)
                                        coroutineScope.launch(Dispatchers.Main) {
                                            showProgressDialog = false
                                            cloneutils.Launchapp(game1Package)
                                        }
                                    },
                                    onFailure = { errorMsg ->
                                        coroutineScope.launch(Dispatchers.Main) {
                                            showProgressDialog = false
                                            errorMessage = errorMsg ?: "Failed to setup game"
                                            showErrorDialog = true
                                        }
                                    },
                                    zippass = "", // No password
                                    onCopyProcess = { progress ->
                                        coroutineScope.launch(Dispatchers.Main) {
                                            progressValue = progress
                                        }
                                    }
                                )
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    showProgressDialog = false
                                    errorMessage = "Error: ${e.message}"
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    gameicon = painterResource(
                        R.drawable.bgmi_icon
                    )
                )
            }

            // Game 2 Card
            item {
                GameInjectionCard(
                    gameName = "PUBG GLOBAL",
                    gamePackage = game2Package,
                    isInstalled = game2Installed,
                    isLoading = game2Loading,
                    onInstallClick = {
                        // Check if game is installed on system
                        if (!cloneutils.ispackage_installed_on_system(context, game2Package)) {
                            errorMessage = "Game not installed on system. Please install the original game first."
                            showErrorDialog = true
                            return@GameInjectionCard
                        }

                        game2Loading = true
                        coroutineScope.launch {
                            try {
                                // Check external OBB
                                if (!cloneutils.isexternalObb(game2Package)) {
                                    withContext(Dispatchers.Main) {
                                        game2Loading = false
                                        Toast.makeText(
                                            context,
                                            "OBB file not found for $game2Package",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return@launch
                                }

                                // Check internal OBB
                                if (!cloneutils.isinternalObb(game2Package)) {
                                    // Need to copy OBB
                                    withContext(Dispatchers.Main) {
                                        showProgressDialog = true
                                        progressMessage = "Copying OBB files..."
                                        progressValue = 0
                                    }

                                    cloneutils.CopyObb(
                                        packageName = game2Package,
                                        copySuccess = {
                                            // OBB copy completed, now setup loader
                                            coroutineScope.launch(Dispatchers.Main) {
                                                progressMessage = "Downloading setup files..."
                                                progressValue = 0
                                            }

                                            // Get download link from Sapi
                                            val downloadUrl = Sapi.libdownloadlink()

                                            coroutineScope.launch {
                                                cloneutils.SetupLoader(
                                                    context = context,
                                                    downloadUrl = downloadUrl,
                                                    onSuccess = {
                                                        // Setup complete, install package
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            progressMessage = "Installing game..."
                                                            progressValue = 100
                                                        }

                                                        cloneutils.installpackage(
                                                            packageName = game2Package,
                                                            sucess = {
                                                                coroutineScope.launch(Dispatchers.Main) {
                                                                    game2Installed = true
                                                                    game2Loading = false
                                                                    showProgressDialog = false
                                                                    // Launch game after installation
                                                                    cloneutils.Launchapp(game2Package)
                                                                }
                                                            },
                                                            fail = { error ->
                                                                coroutineScope.launch(Dispatchers.Main) {
                                                                    game2Loading = false
                                                                    showProgressDialog = false
                                                                    errorMessage = error?.message ?: "Failed to install game"
                                                                    showErrorDialog = true
                                                                }
                                                            }
                                                        )
                                                    },
                                                    onFailure = { errorMsg ->
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            game2Loading = false
                                                            showProgressDialog = false
                                                            errorMessage = errorMsg ?: "Failed to download setup files"
                                                            showErrorDialog = true
                                                        }
                                                    },
                                                    zippass = "", // No password
                                                    onCopyProcess = { progress ->
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            progressValue = progress
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        onFailure = { error ->
                                            coroutineScope.launch(Dispatchers.Main) {
                                                game2Loading = false
                                                showProgressDialog = false
                                                errorMessage = error?.message ?: "Failed to copy OBB files"
                                                showErrorDialog = true
                                            }
                                        },
                                        copyProgress = { progress ->
                                            coroutineScope.launch(Dispatchers.Main) {
                                                progressValue = progress
                                            }
                                        }
                                    )
                                } else {
                                    // Internal OBB exists, proceed with setup loader
                                    withContext(Dispatchers.Main) {
                                        showProgressDialog = true
                                        progressMessage = "Downloading setup files..."
                                        progressValue = 0
                                    }

                                    val downloadUrl = Sapi.libdownloadlink()

                                    coroutineScope.launch {
                                        cloneutils.SetupLoader(
                                            context = context,
                                            downloadUrl = downloadUrl,
                                            onSuccess = {
                                                // Setup complete, install package
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    progressMessage = "Installing game..."
                                                    progressValue = 100
                                                }

                                                cloneutils.installpackage(
                                                    packageName = game2Package,
                                                    sucess = {
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            game2Installed = true
                                                            game2Loading = false
                                                            showProgressDialog = false
                                                            // Launch game after installation
                                                            cloneutils.Launchapp(game2Package)
                                                        }
                                                    },
                                                    fail = { error ->
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            game2Loading = false
                                                            showProgressDialog = false
                                                            errorMessage = error?.message ?: "Failed to install game"
                                                            showErrorDialog = true
                                                        }
                                                    }
                                                )
                                            },
                                            onFailure = { errorMsg ->
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    game2Loading = false
                                                    showProgressDialog = false
                                                    errorMessage = errorMsg ?: "Failed to download setup files"
                                                    showErrorDialog = true
                                                }
                                            },
                                            zippass = "", // No password
                                            onCopyProcess = { progress ->
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    progressValue = progress
                                                }
                                            }
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    game2Loading = false
                                    showProgressDialog = false
                                    errorMessage = "Error: ${e.message}"
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    onRunClick = {
                        // Show progress dialog
                        coroutineScope.launch(Dispatchers.Main) {
                            showProgressDialog = true
                            progressMessage = "Setting up game..."
                            progressValue = 0
                        }
                        
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // Get download link from Sapi
                                val downloadUrl = Sapi.libdownloadlink()
                                
                                cloneutils.SetupLoader(
                                    context = context,
                                    downloadUrl = downloadUrl,
                                    onSuccess = {
                                        // Setup complete, launch game
                                       // BlackBoxLoader.loadSO(context, game2Package)
                                        coroutineScope.launch(Dispatchers.Main) {
                                            showProgressDialog = false
                                            cloneutils.Launchapp(game2Package)
                                        }
                                    },
                                    onFailure = { errorMsg ->
                                        coroutineScope.launch(Dispatchers.Main) {
                                            showProgressDialog = false
                                            errorMessage = errorMsg ?: "Failed to setup game"
                                            showErrorDialog = true
                                        }
                                    },
                                    zippass = "", // No password
                                    onCopyProcess = { progress ->
                                        coroutineScope.launch(Dispatchers.Main) {
                                            progressValue = progress
                                        }
                                    }
                                )
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    showProgressDialog = false
                                    errorMessage = "Error: ${e.message}"
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    gameicon = painterResource(
                        R.drawable.globalpubg
                    )
                )
            }

            // Social Media Clone Section
            item {
                SectionHeader(
                    title = "Social Media Clones",
                    subtitle = "Run dual instances of social apps"
                )
            }

            // Facebook Clone
            item {
                SocialMediaCard(
                    appName = "Facebook",
                    appPackage = facebookPackage,
                    isCloned = facebookCloned,
                    isLoading = facebookLoading,
                    onLaunchClick = {
                        cloneutils.Launchapp(facebookPackage)
                    },
                    onCloneClick = {
                        if (!cloneutils.ispackage_installed_on_system(context, facebookPackage)) {
                            errorMessage = "Facebook not installed on system. Please install Facebook first."
                            showErrorDialog = true
                            return@SocialMediaCard
                        }
                        
                        facebookLoading = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // Check if already cloned
                                if (cloneutils.isAppInstalled(facebookPackage)) {
                                    // Already cloned, just launch
                                    withContext(Dispatchers.Main) {
                                        facebookCloned = true
                                        facebookLoading = false
                                        cloneutils.Launchapp(facebookPackage)
                                    }
                                } else {
                                    // Clone the app
                                    cloneutils.installpackage(
                                        packageName = facebookPackage,
                                        sucess = {
                                            // Installation successful - launch coroutine to update UI
                                            launch(Dispatchers.Main) {
                                                delay(500) // Small delay for state to update
                                                facebookCloned = cloneutils.isAppInstalled(facebookPackage)
                                                facebookLoading = false
                                                // Launch after cloning
                                                cloneutils.Launchapp(facebookPackage)
                                            }
                                        },
                                        fail = { error ->
                                            // Installation failed - update UI on main thread
                                            launch(Dispatchers.Main) {
                                                facebookLoading = false
                                                errorMessage = error?.message ?: "Failed to clone Facebook"
                                                showErrorDialog = true
                                            }
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    facebookLoading = false
                                    errorMessage = "Error: ${e.message}"
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    socialmediaicon = painterResource(R.drawable.facebook)
                )
            }

            // Twitter Clone
            item {
                SocialMediaCard(
                    appName = "Twitter",
                    appPackage = twitterPackage,
                    isCloned = twitterCloned,
                    isLoading = twitterLoading,
                    onLaunchClick = {
                        cloneutils.Launchapp(twitterPackage)
                    },
                    onCloneClick = {
                        if (!cloneutils.ispackage_installed_on_system(context, twitterPackage)) {
                            errorMessage = "Twitter not installed on system. Please install Twitter first."
                            showErrorDialog = true
                            return@SocialMediaCard
                        }
                        
                        twitterLoading = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // Check if already cloned
                                if (cloneutils.isAppInstalled(twitterPackage)) {
                                    // Already cloned, just launch
                                    withContext(Dispatchers.Main) {
                                        twitterCloned = true
                                        twitterLoading = false
                                        cloneutils.Launchapp(twitterPackage)
                                    }
                                } else {
                                    // Clone the app
                                    cloneutils.installpackage(
                                        packageName = twitterPackage,
                                        sucess = {
                                            // Installation successful - launch coroutine to update UI
                                            launch(Dispatchers.Main) {
                                                delay(500) // Small delay for state to update
                                                twitterCloned = cloneutils.isAppInstalled(twitterPackage)
                                                twitterLoading = false
                                                // Launch after cloning
                                                cloneutils.Launchapp(twitterPackage)
                                            }
                                        },
                                        fail = { error ->
                                            // Installation failed - update UI on main thread
                                            launch(Dispatchers.Main) {
                                                twitterLoading = false
                                                errorMessage = error?.message ?: "Failed to clone Twitter"
                                                showErrorDialog = true
                                            }
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    twitterLoading = false
                                    errorMessage = "Error: ${e.message}"
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    socialmediaicon = painterResource(R.drawable.twitter)
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun GameInjectionCard(
    gameName: String,
    gamePackage: String,
    isInstalled: Boolean,
    isLoading: Boolean = false,
    onInstallClick: () -> Unit,
    onRunClick: () -> Unit,
    gameicon : Painter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Game Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = gameicon,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Column {
                        Text(
                            text = gameName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = gamePackage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status indicator
                if (isInstalled) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Installed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Install/Setup Button
                Button(
                    onClick = onInstallClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && !isInstalled,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInstalled) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            MaterialTheme.colorScheme.primary,
                        contentColor = if (isInstalled)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isInstalled) 0.dp else 2.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Installing...",
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = if (isInstalled) Icons.Default.CheckCircle else Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isInstalled) "Installed" else "Install",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Run Button
                Button(
                    onClick = onRunClick,
                    modifier = Modifier.weight(1f),
                    enabled = isInstalled,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Features chip list
            if (isInstalled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureChip("Mod Enabled")
                    FeatureChip("Ready")
                }
            }
        }
    }
}

@Composable
fun SocialMediaCard(
    appName: String,
    appPackage: String,
    isCloned: Boolean,
    isLoading: Boolean = false,
    onCloneClick: () -> Unit = { },
    onLaunchClick: () -> Unit = { },
    socialmediaicon: Painter = painterResource(android.R.drawable.ic_menu_share)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Social Media Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = socialmediaicon,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Column {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCloned) "Cloned ✓" else "Not cloned",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCloned) 
                            Color(0xFF4CAF50) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Clone/Launch Button
            FilledTonalButton(
                onClick = {
                    if (isCloned) {
                        onLaunchClick()
                    } else {
                        onCloneClick()
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isCloned)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isCloned)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCloned) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isCloned) "Launch" else "Clone",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun FeatureChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

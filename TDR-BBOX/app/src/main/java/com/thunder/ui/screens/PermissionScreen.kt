package com.thunder.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.thunder.R
import com.thunder.LOGS

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class PermissionItem(
    val title: String,
    val description: String,
    val icon: Painter,
    val isGranted: () -> Boolean,
    val requestAction: () -> Unit
)

@Composable
fun PermissionScreen(
    navController: NavController,
    activity: Activity
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Permission states
    var storagePermissionGranted by remember { mutableStateOf(false) }
    var manageStorageGranted by remember { mutableStateOf(false) }
    var installPermissionGranted by remember { mutableStateOf(false) }
    var currentPermissionIndex by remember { mutableStateOf(0) }
    var isCheckingPermissions by remember { mutableStateOf(true) }
    
    // Animation states
    val cardAlpha = remember { Animatable(0f) }
    val cardScale = remember { Animatable(0.9f) }
    
    // Function to check and update permissions
    fun refreshPermissions() {

        checkAllPermissions(context, activity) { storage, manage, install ->
            storagePermissionGranted = storage
            manageStorageGranted = manage
            installPermissionGranted = install
            isCheckingPermissions = false
            
            // If all granted, navigate to login after a short delay
            if (storage && manage && install) {
                coroutineScope.launch {
                    delay(1000)
                    navController.navigate("login") {
                        popUpTo("permissions") { inclusive = true }
                    }
                }
            }
        }
    }
    
    // Check permissions on start
    LaunchedEffect(Unit) {
        // Animate card entrance
        cardAlpha.animateTo(1f, animationSpec = tween(600))
        cardScale.animateTo(1f, animationSpec = tween(600, delayMillis = 100))
        
        delay(300)
        refreshPermissions()
    }
    
    // Re-check permissions when activity resumes (user returns from settings)
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        (activity as LifecycleOwner).lifecycle.addObserver(observer)
        
        onDispose {
            (activity as LifecycleOwner).lifecycle.removeObserver(observer)
        }
    }
    
    // Storage permission launcher (for Android 10 and below)
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        storagePermissionGranted = allGranted
        
        // Refresh all permissions after user returns
        coroutineScope.launch {
            delay(500)
            refreshPermissions()
        }
    }
    
    // Prepare icons (must be in composable context)
    val folderIcon = painterResource(R.drawable.folder_24px)
    val storageIcon = painterResource(R.drawable.storage_24px)
    val downloadIcon = painterResource(R.drawable.download_24px)
    
    // Build permission list
    val permissions = buildPermissionList(
        context = context,
        activity = activity,
        storageGranted = storagePermissionGranted,
        manageStorageGranted = manageStorageGranted,
        installGranted = installPermissionGranted,
        folderIcon = folderIcon,
        storageIcon = storageIcon,
        downloadIcon = downloadIcon,
        onStorageRequest = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                storagePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                // Android 11+ - request MANAGE_EXTERNAL_STORAGE
                requestManageStorage(context, activity)
                checkManageStorage(context, activity) { granted ->
                    manageStorageGranted = granted
                }
            }
        },
        onInstallRequest = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (activity.packageManager.canRequestPackageInstalls()) {
                    installPermissionGranted = true
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    }
                    activity.startActivity(intent)
                }
            }
        },
        onManageStorageRequest = {
            requestManageStorage(context, activity)
            checkManageStorage(context, activity) { granted ->
                manageStorageGranted = granted
            }
        }
    )
    
    val totalPermissions = permissions.size
    val grantedCount = permissions.count { it.isGranted() }
    val progress = if (totalPermissions > 0) grantedCount.toFloat() / totalPermissions.toFloat() else 0f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(cardScale.value)
                    .alpha(cardAlpha.value),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lock_id_lock),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Title
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "We need these permissions to install games, access OBB files, and manage apps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    // Progress indicator
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Text(
                        text = "$grantedCount / $totalPermissions permissions granted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Permission items
                    permissions.forEachIndexed { index, permission ->
                        PermissionItemCard(
                            permission = permission,
                            isLast = index == permissions.size - 1
                        )
                    }
                    
                    // Continue button (only show when all permissions granted)
                    AnimatedVisibility(
                        visible = grantedCount == totalPermissions && totalPermissions > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Button(
                            onClick = {
                                navController.navigate("login") {
                                    popUpTo("permissions") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionItemCard(
    permission: PermissionItem,
    isLast: Boolean
) {
    val isGranted = permission.isGranted()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isGranted)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isGranted)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = permission.icon,
                    contentDescription = null,
                    tint = if (isGranted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = permission.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = permission.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                TextButton(
                    onClick = { permission.requestAction() }
                ) {
                    Text(
                        text = "Grant",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

fun buildPermissionList(
    context: Context,
    activity: Activity,
    storageGranted: Boolean,
    manageStorageGranted: Boolean,
    installGranted: Boolean,
    folderIcon: Painter,
    storageIcon: Painter,
    downloadIcon: Painter,
    onStorageRequest: () -> Unit,
    onInstallRequest: () -> Unit,
    onManageStorageRequest: () -> Unit
): List<PermissionItem> {
    val permissions = mutableListOf<PermissionItem>()
    
    // Storage permission (Android 10 and below)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        permissions.add(
            PermissionItem(
                title = "Storage Access",
                description = "Read and write files on device storage",
                icon = folderIcon,
                isGranted = { storageGranted },
                requestAction = onStorageRequest
            )
        )
    } else {
        // MANAGE_EXTERNAL_STORAGE for Android 11+
        permissions.add(
            PermissionItem(
                title = "Full File Access",
                description = "Access OBB files and manage all files (Android 11+)",
                icon = storageIcon,
                isGranted = { manageStorageGranted },
                requestAction = onManageStorageRequest
            )
        )
    }
    
    // Install packages permission
    permissions.add(
        PermissionItem(
            title = "Install Apps",
            description = "Install apps from unknown sources",
            icon = downloadIcon,
            isGranted = { installGranted },
            requestAction = onInstallRequest
        )
    )
    
    return permissions
}

fun checkAllPermissions(
    context: Context,
    activity: Activity,
    onResult: (storage: Boolean, manageStorage: Boolean, install: Boolean) -> Unit
) {
    val storageGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Not needed on Android 11+
    }
    
    val manageStorageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true // Not needed on Android 10 and below
    }
    
    val installGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        activity.packageManager.canRequestPackageInstalls()
    } else {
        true // Not needed on Android 7.1 and below
    }
    
    onResult(storageGranted, manageStorageGranted, installGranted)
}

fun checkManageStorage(context: Context, activity: Activity, onResult: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        onResult(Environment.isExternalStorageManager())
    } else {
        onResult(true)
    }
}

fun requestManageStorage(context: Context, activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                LOGS.error("Error requesting manage storage: ${e.message}")
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            }
        }
    }
}


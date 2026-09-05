package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ClipStudioScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeneratedClipsScreen
import com.example.ui.screens.SchedulerScreen
import com.example.ui.screens.ServerStatusScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HdBackground
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdContainerLight
import com.example.ui.theme.HdOnPrimaryContainer
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import com.example.ui.theme.HdSurface
import com.example.ui.theme.HdTextPrimary
import com.example.ui.theme.HdTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.viewmodel.FootballViewModel

enum class ScreenTab(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tag: String,
    val stepIndex: Int
) {
    DASHBOARD("Source Video", "FOOTAGE ANALYSIS", Icons.Default.Movie, "nav_dashboard", 1),
    CLIPS("Clips", "AI VIRAL SHORTS", Icons.Default.PlayCircle, "nav_clips", 2),
    STUDIO("Studio", "VERTICAL RENDERING", Icons.Default.Crop, "nav_studio", 3),
    SCHEDULER("Schedule", "SOCIAL POSTS", Icons.Default.CalendarMonth, "nav_scheduler", 4),
    SERVER("Server", "LOCAL STREAMING", Icons.Default.Dns, "nav_server", 5),
    SETTINGS("Settings", "CLIP CONFIG", Icons.Default.Settings, "nav_settings", 6)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: FootballViewModel = viewModel()
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: FootballViewModel) {
    var navHistory by remember { mutableStateOf(listOf(ScreenTab.DASHBOARD)) }
    val currentTab = navHistory.last()

    fun navigateTo(tab: ScreenTab) {
        if (currentTab != tab) {
            navHistory = navHistory + tab
        }
    }

    fun navigateBack(): Boolean {
        return if (navHistory.size > 1) {
            navHistory = navHistory.dropLast(1)
            true
        } else false
    }

    BackHandler(enabled = navHistory.size > 1) {
        navigateBack()
    }

    val isServerRunning by viewModel.httpServer.isRunning.collectAsState()
    val serverPort by viewModel.httpServer.port.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HdBackground,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(HdBackground)) {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (navHistory.size > 1) {
                            IconButton(
                                onClick = { navigateBack() },
                                modifier = Modifier.testTag("btn_top_back")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = HdTextPrimary
                                )
                            }
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = HdPrimary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "ClipAI",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (currentTab) {
                                        ScreenTab.DASHBOARD -> "ClipAI Studio"
                                        ScreenTab.CLIPS -> "Viral Shorts"
                                        ScreenTab.STUDIO -> "Clip Studio"
                                        ScreenTab.SCHEDULER -> "Content Calendar"
                                        ScreenTab.SERVER -> "Device Server"
                                        ScreenTab.SETTINGS -> "Clip Settings"
                                    },
                                    color = HdTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = currentTab.subtitle,
                                    color = HdTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Server status badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isServerRunning) HdPrimaryContainer else Color(0xFF3F1D1D),
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isServerRunning) StatusSuccess else StatusError)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isServerRunning) ":$serverPort" else "OFFLINE",
                                        color = if (isServerRunning) HdOnPrimaryContainer else StatusError,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = HdBackground
                    ),
                    actions = {
                        IconButton(
                            onClick = { navigateTo(ScreenTab.SETTINGS) },
                            modifier = Modifier.testTag("btn_top_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (currentTab == ScreenTab.SETTINGS) HdPrimary else HdTextSecondary
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            val bottomTabs = listOf(
                ScreenTab.DASHBOARD,
                ScreenTab.CLIPS,
                ScreenTab.STUDIO,
                ScreenTab.SCHEDULER,
                ScreenTab.SERVER
            )
            NavigationBar(
                containerColor = HdSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = HdCardBorder,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
            ) {
                bottomTabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { navigateTo(tab) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = HdPrimary,
                            indicatorColor = HdPrimary,
                            unselectedIconColor = HdTextSecondary,
                            unselectedTextColor = HdTextSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    ScreenTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToClips = { navigateTo(ScreenTab.CLIPS) },
                        onNavigateToServer = { navigateTo(ScreenTab.SERVER) },
                        onNavigateToStudio = { navigateTo(ScreenTab.STUDIO) },
                        onNavigateToScheduler = { navigateTo(ScreenTab.SCHEDULER) }
                    )
                    ScreenTab.CLIPS -> GeneratedClipsScreen(
                        viewModel = viewModel,
                        onNavigateToStudio = { navigateTo(ScreenTab.STUDIO) },
                        onNavigateToServer = { navigateTo(ScreenTab.SERVER) },
                        onNavigateToScheduler = { navigateTo(ScreenTab.SCHEDULER) },
                        onNavigateToDashboard = { navigateTo(ScreenTab.DASHBOARD) }
                    )
                    ScreenTab.STUDIO -> ClipStudioScreen(
                        viewModel = viewModel,
                        onNavigateToServer = { navigateTo(ScreenTab.SERVER) },
                        onNavigateToClips = { navigateTo(ScreenTab.CLIPS) }
                    )
                    ScreenTab.SCHEDULER -> SchedulerScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.SERVER -> ServerStatusScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

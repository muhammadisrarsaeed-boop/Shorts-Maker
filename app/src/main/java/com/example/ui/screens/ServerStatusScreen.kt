package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.server.NetworkUtils
import com.example.data.server.ServerLogEntry
import com.example.ui.theme.HdBackground
import com.example.ui.theme.HdBadge
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdContainerLight
import com.example.ui.theme.HdOnPrimaryContainer
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import com.example.ui.theme.HdSurface
import com.example.ui.theme.HdSurfaceVariant
import com.example.ui.theme.HdTextPrimary
import com.example.ui.theme.HdTextSecondary
import com.example.ui.theme.HdTextTertiary
import com.example.viewmodel.FootballViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ServerStatusScreen(
    viewModel: FootballViewModel
) {
    val context = LocalContext.current
    val isRunning by viewModel.httpServer.isRunning.collectAsState()
    val port by viewModel.httpServer.port.collectAsState()
    val logs by viewModel.httpServer.serverLogs.collectAsState()
    val requestsServed by viewModel.httpServer.requestsServed.collectAsState()
    val masterClips by viewModel.masterClips.collectAsState()
    val moments by viewModel.moments.collectAsState()

    val localIp = remember { NetworkUtils.getLocalIpAddress() }
    val serverUrl = "http://$localIp:$port"
    val localhostUrl = "http://127.0.0.1:$port"

    val freeMb = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        } catch (e: Exception) {
            4096L
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "DEVICE AS LOCAL SERVER",
                color = HdPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "On-Device Media & API Server",
                color = HdTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Instead of external cloud servers, your Android device hosts and serves all video highlights directly",
                color = HdTextSecondary,
                fontSize = 12.sp
            )
        }

        // Server Power & Status Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRunning) HdPrimaryContainer else Color(0xFFF9DEDC)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isRunning) HdPrimary else Color(0xFFB3261E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_server_master_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isRunning) HdPrimary else Color(0xFFB3261E),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isRunning) "SERVER ONLINE & LISTENING" else "SERVER STOPPED",
                                    color = if (isRunning) HdOnPrimaryContainer else Color(0xFFB3261E),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isRunning) "Port :$port • Serving LAN & Localhost" else "Toggle to start listening",
                                    color = if (isRunning) HdOnPrimaryContainer.copy(alpha = 0.8f) else Color(0xFF8C1D18),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = isRunning,
                            onCheckedChange = { viewModel.toggleServer(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HdPrimary,
                                uncheckedThumbColor = HdCardBorder,
                                uncheckedTrackColor = HdContainerLight
                            ),
                            modifier = Modifier.testTag("server_toggle_switch")
                        )
                    }

                    if (isRunning) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // URL Display Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(HdSurface)
                                .border(1.dp, HdCardBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DEVICE URL (WI-FI / LAN)", color = HdTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = serverUrl,
                                    color = HdPrimary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Server URL", serverUrl))
                                        Toast.makeText(context, "URL copied: $serverUrl", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_server_url_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy URL",
                                        tint = HdTextPrimary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("open_browser_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = "Open in Browser",
                                        tint = HdPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Server Health & Hardware Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "STORAGE FREE",
                    value = "$freeMb MB",
                    subtitle = "Internal phone memory",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "REQUESTS",
                    value = "$requestsServed",
                    subtitle = "Served from device",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Endpoints Documentation
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ON-DEVICE HTTP API ROUTES",
                        color = HdPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    EndpointRow("GET /", "Web Dashboard & Highlight Player for any browser on LAN")
                    EndpointRow("GET /api/status", "JSON server health, active jobs, free disk space")
                    EndpointRow("GET /api/moments", "JSON list of ${moments.size} AI detected highlight moments")
                    EndpointRow("GET /api/clips", "JSON feed of ${masterClips.size} master videos")
                    EndpointRow("GET /api/video/{id}", "HTTP Range video streaming directly from device")
                }
            }
        }

        // Real-Time Server Request Log
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE SERVER REQUEST LOGS",
                    color = HdTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${logs.size} Events",
                    color = HdPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HdSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No server requests yet. Start the server and visit http://$localIp:$port from any browser!",
                            color = HdTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(logs) { entry ->
            ServerLogRow(entry = entry)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = HdTextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = HdPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = HdTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun EndpointRow(route: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = HdContainerLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder)
        ) {
            Text(
                text = route,
                color = HdPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = description,
            color = HdTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ServerLogRow(entry: ServerLogEntry) {
    val timeStr = remember(entry.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        color = HdTextTertiary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${entry.method} ${entry.path}",
                        color = HdTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.clientIp} • ${entry.message}",
                    color = HdTextSecondary,
                    fontSize = 11.sp
                )
            }

            Surface(
                shape = CircleShape,
                color = if (entry.statusCode == 200) HdPrimaryContainer else Color(0xFFF9DEDC)
            ) {
                Text(
                    text = "${entry.statusCode}",
                    color = if (entry.statusCode == 200) HdOnPrimaryContainer else Color(0xFFB3261E),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

package com.example.data.server

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ServerLogEntry(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val path: String,
    val statusCode: Int,
    val clientIp: String,
    val message: String
)

class DeviceHttpServer(private val context: Context) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val database = AppDatabase.getDatabase(context)

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _port = MutableStateFlow(8080)
    val port: StateFlow<Int> = _port.asStateFlow()

    private val _serverLogs = MutableStateFlow<List<ServerLogEntry>>(emptyList())
    val serverLogs: StateFlow<List<ServerLogEntry>> = _serverLogs.asStateFlow()

    private val _requestsServed = MutableStateFlow(0)
    val requestsServed: StateFlow<Int> = _requestsServed.asStateFlow()

    fun startServer(desiredPort: Int = 8080) {
        if (_isRunning.value) return

        serverJob = scope.launch {
            try {
                var currentPort = desiredPort
                var bound = false
                while (!bound && currentPort < desiredPort + 10) {
                    try {
                        serverSocket = ServerSocket(currentPort)
                        _port.value = currentPort
                        bound = true
                    } catch (e: Exception) {
                        currentPort++
                    }
                }

                if (!bound) {
                    addLog("SYSTEM", "/start", 500, "127.0.0.1", "Failed to bind to ports $desiredPort-${desiredPort + 10}")
                    return@launch
                }

                _isRunning.value = true
                val localIp = NetworkUtils.getLocalIpAddress()
                addLog("SYSTEM", "/start", 200, "127.0.0.1", "Local server listening on http://$localIp:${_port.value}")

                while (isActive && _isRunning.value) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        scope.launch {
                            handleClient(client)
                        }
                    } catch (e: Exception) {
                        if (!isActive || !_isRunning.value) break
                    }
                }
            } catch (e: Exception) {
                addLog("SYSTEM", "/error", 500, "127.0.0.1", "Server error: ${e.message}")
            } finally {
                _isRunning.value = false
            }
        }
    }

    fun stopServer() {
        _isRunning.value = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serverJob?.cancel()
        addLog("SYSTEM", "/stop", 200, "127.0.0.1", "Local server stopped")
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        val clientIp = socket.inetAddress?.hostAddress ?: "unknown"
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val firstLine = reader.readLine() ?: return@withContext
            val parts = firstLine.split(" ")
            if (parts.size < 2) return@withContext

            val method = parts[0]
            val fullPath = parts[1]
            val path = fullPath.substringBefore("?")

            _requestsServed.value += 1

            val out = socket.getOutputStream()

            when {
                path == "/" || path == "/index.html" -> {
                    serveDashboardHtml(out)
                    addLog(method, path, 200, clientIp, "Dashboard served")
                }
                path == "/api/status" -> {
                    serveStatusJson(out)
                    addLog(method, path, 200, clientIp, "Status query")
                }
                path == "/api/clips" -> {
                    serveClipsJson(out)
                    addLog(method, path, 200, clientIp, "Clips list requested")
                }
                path == "/api/moments" -> {
                    serveMomentsJson(out)
                    addLog(method, path, 200, clientIp, "Moments list requested")
                }
                path.startsWith("/api/video/") -> {
                    val idStr = path.removePrefix("/api/video/")
                    serveVideoFile(idStr, out)
                    addLog(method, path, 200, clientIp, "Video stream requested for #$idStr")
                }
                else -> {
                    sendJsonResponse(out, 404, JSONObject().put("error", "Not Found").toString())
                    addLog(method, path, 404, clientIp, "Route not found")
                }
            }
        } catch (e: Exception) {
            // Client disconnect or socket error
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private suspend fun serveDashboardHtml(out: OutputStream) {
        val clips = database.footballDao().getAllMasterClips().first()
        val moments = database.footballDao().getAllMoments().first()
        val jobs = database.footballDao().getAllRenderJobs().first()

        val statFs = StatFs(Environment.getDataDirectory().path)
        val freeBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        val freeMb = freeBytes / (1024 * 1024)

        val html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Football AI Local Media Server</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0b132b; color: #f1f5f9; margin: 0; padding: 24px; }
                    .header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #1e293b; padding-bottom: 16px; margin-bottom: 24px; }
                    .title { font-size: 24px; font-weight: 800; color: #10b981; }
                    .badge { background: #065f46; color: #34d399; padding: 6px 14px; border-radius: 9999px; font-size: 13px; font-weight: 700; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px; }
                    .card { background: #1e293b; border-radius: 12px; padding: 18px; border: 1px solid #334155; }
                    .card-title { font-size: 12px; text-transform: uppercase; color: #94a3b8; letter-spacing: 0.05em; margin-bottom: 6px; }
                    .card-value { font-size: 24px; font-weight: 700; color: #f8fafc; }
                    .section-title { font-size: 18px; font-weight: 700; margin: 24px 0 12px 0; color: #f8fafc; display: flex; align-items: center; gap: 8px; }
                    table { width: 100%; border-collapse: collapse; background: #1e293b; border-radius: 12px; overflow: hidden; }
                    th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #334155; font-size: 14px; }
                    th { background: #0f172a; color: #94a3b8; font-weight: 600; }
                    .moment-badge { padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 700; }
                    .goal { background: #78350f; color: #fde68a; }
                    .miss { background: #7c2d12; color: #fed7aa; }
                    .chance { background: #1e3a8a; color: #bfdbfe; }
                    .peak { background: #701a75; color: #f5d0fe; }
                    .api-box { background: #0f172a; padding: 12px; border-radius: 8px; font-family: monospace; font-size: 13px; color: #38bdf8; margin-top: 8px; word-break: break-all; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <div class="title">⚽ Football AI On-Device Server</div>
                        <div style="color: #94a3b8; font-size: 14px; margin-top: 4px;">User Phone Acting as Highlight Processing & Streaming Node</div>
                    </div>
                    <div class="badge">● ONLINE (Local Device)</div>
                </div>

                <div class="grid">
                    <div class="card">
                        <div class="card-title">Device Storage Free</div>
                        <div class="card-value">$freeMb MB</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Master Clips Stored</div>
                        <div class="card-value">${clips.size}</div>
                    </div>
                    <div class="card">
                        <div class="card-title">AI Moments Detected</div>
                        <div class="card-value">${moments.size}</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Clips Rendered</div>
                        <div class="card-value">${jobs.size}</div>
                    </div>
                </div>

                <div class="section-title">📡 Live Device Endpoints</div>
                <div class="card">
                    <div>Status Endpoint:</div>
                    <div class="api-box">GET /api/status</div>
                    <div style="margin-top: 12px;">Moments Feed:</div>
                    <div class="api-box">GET /api/moments</div>
                    <div style="margin-top: 12px;">Clips Output:</div>
                    <div class="api-box">GET /api/clips</div>
                </div>

                <div class="section-title">⚡ Detected Highlight Moments (${moments.size})</div>
                ${if (moments.isEmpty()) "<p style='color: #94a3b8;'>No moments detected yet. Ingest a video from your Android app to extract highlights.</p>" else """
                <table>
                    <thead>
                        <tr>
                            <th>Type</th>
                            <th>Time Window</th>
                            <th>AI Confidence</th>
                            <th>Audio Spike</th>
                            <th>Priority Score</th>
                            <th>Description</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${moments.joinToString("") { m ->
                            val badgeClass = when (m.momentType) {
                                "goal_scored" -> "goal"
                                "goal_missed" -> "miss"
                                "chance_created" -> "chance"
                                else -> "peak"
                            }
                            """
                            <tr>
                                <td><span class="moment-badge $badgeClass">${m.momentType.replace('_', ' ').uppercase()}</span></td>
                                <td>${String.format(Locale.US, "%.1fs - %.1fs", m.startSec, m.endSec)}</td>
                                <td>${(m.confidence * 100).toInt()}%</td>
                                <td>${(m.audioEnergy * 100).toInt()}%</td>
                                <td><strong>${String.format(Locale.US, "%.2f", m.priorityScore)}</strong></td>
                                <td>${m.title}: ${m.description}</td>
                            </tr>
                            """
                        }}
                    </tbody>
                </table>
                """}
            </body>
            </html>
        """.trimIndent()

        val bytes = html.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"

        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    private suspend fun serveStatusJson(out: OutputStream) {
        val clips = database.footballDao().getAllMasterClips().first()
        val moments = database.footballDao().getAllMoments().first()
        val jobs = database.footballDao().getAllRenderJobs().first()

        val statFs = StatFs(Environment.getDataDirectory().path)
        val freeBytes = statFs.availableBlocksLong * statFs.blockSizeLong

        val json = JSONObject().apply {
            put("status", "running")
            put("deviceMode", "on-device-server")
            put("localIp", NetworkUtils.getLocalIpAddress())
            put("port", _port.value)
            put("freeStorageBytes", freeBytes)
            put("masterClipsCount", clips.size)
            put("momentsCount", moments.size)
            put("renderedClipsCount", jobs.size)
            put("requestsServed", _requestsServed.value)
            put("timestamp", System.currentTimeMillis())
        }
        sendJsonResponse(out, 200, json.toString())
    }

    private suspend fun serveClipsJson(out: OutputStream) {
        val clips = database.footballDao().getAllMasterClips().first()
        val array = JSONArray()
        clips.forEach { clip ->
            array.put(JSONObject().apply {
                put("id", clip.id)
                put("fileName", clip.fileName)
                put("durationSec", clip.durationSec)
                put("resolution", clip.resolution)
                put("fps", clip.fps)
                put("fileSizeBytes", clip.fileSizeBytes)
                put("createdAt", clip.createdAt)
            })
        }
        sendJsonResponse(out, 200, array.toString())
    }

    private suspend fun serveMomentsJson(out: OutputStream) {
        val moments = database.footballDao().getAllMoments().first()
        val array = JSONArray()
        moments.forEach { m ->
            array.put(JSONObject().apply {
                put("id", m.id)
                put("masterClipId", m.masterClipId)
                put("momentType", m.momentType)
                put("startSec", m.startSec)
                put("endSec", m.endSec)
                put("confidence", m.confidence)
                put("audioEnergy", m.audioEnergy)
                put("priorityScore", m.priorityScore)
                put("title", m.title)
                put("description", m.description)
                put("status", m.status)
            })
        }
        sendJsonResponse(out, 200, array.toString())
    }

    private suspend fun serveVideoFile(idStr: String, out: OutputStream) {
        val clipId = idStr.toLongOrNull()
        if (clipId == null) {
            sendJsonResponse(out, 400, JSONObject().put("error", "Invalid ID").toString())
            return
        }

        val clip = database.footballDao().getMasterClipById(clipId)
        if (clip == null) {
            sendJsonResponse(out, 404, JSONObject().put("error", "Clip not found").toString())
            return
        }

        val file = File(clip.localUri)
        if (!file.exists()) {
            sendJsonResponse(out, 404, JSONObject().put("error", "Local video file not found on device").toString())
            return
        }

        val length = file.length()
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: video/mp4\r\n" +
                "Content-Length: $length\r\n" +
                "Accept-Ranges: bytes\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(32 * 1024)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
        }
        out.flush()
    }

    private fun sendJsonResponse(out: OutputStream, code: Int, json: String) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        val statusText = if (code == 200) "OK" else if (code == 404) "Not Found" else "Error"
        val header = "HTTP/1.1 $code $statusText\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    private fun addLog(method: String, path: String, code: Int, ip: String, msg: String) {
        val entry = ServerLogEntry(
            method = method,
            path = path,
            statusCode = code,
            clientIp = ip,
            message = msg
        )
        val current = _serverLogs.value.toMutableList()
        if (current.size >= 50) {
            current.removeAt(current.size - 1)
        }
        current.add(0, entry)
        _serverLogs.value = current
    }
}

package com.nova.productivity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object ProxyState {
    @Volatile
    var running: Boolean = false
        private set

    private val listeners = CopyOnWriteArraySet<(Boolean) -> Unit>()

    fun subscribe(listener: (Boolean) -> Unit) {
        listeners.add(listener)
        listener(running)
    }

    fun unsubscribe(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }

    fun setRunning(value: Boolean) {
        running = value
        listeners.forEach { listener -> runCatching { listener(value) } }
    }
}

class LocalProxyService : Service() {
    companion object {
        const val PORT = 8787
        private const val CHANNEL_ID = "nova_local_proxy"
        private const val NOTIFICATION_ID = 8787
    }

    private val active = AtomicBoolean(false)
    private val workers = Executors.newFixedThreadPool(12)
    @Volatile private var serverSocket: ServerSocket? = null
    private var serverThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_PROXY") {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        startServer()
        return START_STICKY
    }

    private fun startServer() {
        if (!active.compareAndSet(false, true)) return
        serverThread = Thread {
            try {
                val server = ServerSocket()
                server.reuseAddress = true
                server.bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT))
                serverSocket = server
                ProxyState.setRunning(true)
                while (active.get()) {
                    val client = server.accept()
                    client.tcpNoDelay = true
                    try {
                        workers.execute { handleClient(client) }
                    } catch (_: Exception) {
                        runCatching { client.close() }
                    }
                }
            } catch (error: Exception) {
                if (active.get()) {
                    Log.e("NOVA-Proxy", "Local proxy failed to start or accept connections", error)
                    ProxyState.setRunning(false)
                    stopSelf()
                }
            }
        }
        serverThread?.name = "NOVA-LocalProxy"
        serverThread?.isDaemon = true
        serverThread?.start()
    }

    private fun handleClient(client: Socket) {
        try {
            client.use { socket ->
                socket.soTimeout = 120000
                val input = BufferedInputStream(socket.getInputStream())
                input.mark(1)
                val firstByte = input.read()
                input.reset()
                if (firstByte == 5) {
                    handleSocks5(socket, input)
                } else {
                    handleHttp(socket, input)
                }
            }
        } catch (_: Exception) {
            runCatching { client.close() }
        }
    }

    private fun handleSocks5(client: Socket, input: BufferedInputStream) {
        val output = client.getOutputStream()
        try {
            val version = input.read()
            if (version != 5) return
            val methodCount = input.read()
            if (methodCount <= 0) return
            val methods = readExact(input, methodCount)
            if (!methods.contains(0.toByte())) {
                output.write(byteArrayOf(5, -1))
                output.flush()
                return
            }
            output.write(byteArrayOf(5, 0))
            output.flush()

            val requestVersion = input.read()
            val command = input.read()
            input.read()
            val addressType = input.read()
            if (requestVersion != 5 || command != 1) {
                writeSocksReply(output, 7)
                return
            }

            val host = when (addressType) {
                1 -> InetAddress.getByAddress(readExact(input, 4)).hostAddress
                3 -> {
                    val length = input.read()
                    if (length <= 0) throw EOFException()
                    String(readExact(input, length), Charsets.UTF_8)
                }
                4 -> InetAddress.getByAddress(readExact(input, 16)).hostAddress
                else -> {
                    writeSocksReply(output, 8)
                    return
                }
            }
            val portBytes = readExact(input, 2)
            val port = ((portBytes[0].toInt() and 0xff) shl 8) or (portBytes[1].toInt() and 0xff)
            if (port !in 1..65535) {
                writeSocksReply(output, 1)
                return
            }
            if (NovaBlocklist.isBlocked(host)) {
                NovaBlocklist.recordBlocked(applicationContext)
                writeSocksReply(output, 2)
                return
            }

            val upstream = Socket()
            try {
                upstream.connect(InetSocketAddress(host, port), 12000)
                writeSocksReply(output, 0)
                relay(client, upstream, input)
            } catch (_: Exception) {
                writeSocksReply(output, 5)
            } finally {
                runCatching { upstream.close() }
            }
        } catch (_: Exception) {
            runCatching { writeSocksReply(output, 1) }
        }
    }

    private fun handleHttp(client: Socket, input: BufferedInputStream) {
        val output = client.getOutputStream()
        val rawHeader = readHeader(input) ?: return
        val headerText = String(rawHeader, Charsets.ISO_8859_1)
        val lines = headerText.split("\r\n")
        val requestParts = lines.firstOrNull()?.trim()?.split(Regex("\\s+")) ?: emptyList()
        if (requestParts.size < 2) {
            writeHttpError(output, 400, "Bad Request")
            return
        }

        val method = requestParts[0]
        val target = requestParts[1]
        val isConnect = method.equals("CONNECT", true)
        val host: String
        val port: Int
        val forwardedHeader: String

        if (isConnect) {
            val authority = parseAuthority(target, 443)
            if (authority == null) {
                writeHttpError(output, 400, "Bad CONNECT target")
                return
            }
            host = authority.first
            port = authority.second
            forwardedHeader = ""
        } else {
            val uri = runCatching { URI(target) }.getOrNull()
            val hostHeader = lines.firstOrNull { it.startsWith("Host:", true) }?.substringAfter(":")?.trim()
            val authority = if (uri?.host != null) {
                Pair(uri.host, if (uri.port > 0) uri.port else if (uri.scheme.equals("https", true)) 443 else 80)
            } else if (!hostHeader.isNullOrBlank()) {
                parseAuthority(hostHeader, 80)
            } else null
            if (authority == null) {
                writeHttpError(output, 400, "Host header required")
                return
            }
            host = authority.first
            port = authority.second
            val path = if (uri?.isAbsolute == true) {
                val rawPath = if (uri.rawPath.isNullOrEmpty()) "/" else uri.rawPath
                rawPath + if (uri.rawQuery.isNullOrEmpty()) "" else "?" + uri.rawQuery
            } else target
            val version = if (requestParts.size >= 3) requestParts[2] else "HTTP/1.1"
            val safeHeaders = lines.drop(1).filterNot { it.startsWith("Proxy-Connection:", true) }
            forwardedHeader = method + " " + path + " " + version + "\r\n" + safeHeaders.joinToString("\r\n") + "\r\n\r\n"
        }

        if (NovaBlocklist.isBlocked(host)) {
            NovaBlocklist.recordBlocked(applicationContext)
            writeHttpError(output, 403, "Blocked by NOVA Shield")
            return
        }

        val upstream = Socket()
        try {
            upstream.connect(InetSocketAddress(host, port), 12000)
            if (isConnect) {
                output.write("HTTP/1.1 200 Connection Established\r\nProxy-Agent: NOVA\r\n\r\n".toByteArray(Charsets.ISO_8859_1))
                output.flush()
            } else {
                output.write(forwardedHeader.toByteArray(Charsets.ISO_8859_1))
                output.flush()
            }
            relay(client, upstream, input)
        } catch (_: Exception) {
            writeHttpError(output, 502, "NOVA could not reach the destination")
        } finally {
            runCatching { upstream.close() }
        }
    }

    private fun parseAuthority(value: String, defaultPort: Int): Pair<String, Int>? {
        val text = value.trim()
        if (text.isEmpty()) return null
        if (text.startsWith("[")) {
            val end = text.indexOf(']')
            if (end <= 1) return null
            val host = text.substring(1, end)
            val port = if (text.length > end + 1 && text[end + 1] == ':') text.substring(end + 2).toIntOrNull() ?: return null else defaultPort
            return if (port in 1..65535) Pair(host, port) else null
        }
        val colon = text.lastIndexOf(':')
        if (colon > 0 && text.indexOf(':') == colon) {
            val port = text.substring(colon + 1).toIntOrNull() ?: return null
            return if (port in 1..65535) Pair(text.substring(0, colon), port) else null
        }
        return Pair(text, defaultPort)
    }

    private fun readHeader(input: InputStream): ByteArray? {
        val output = ByteArrayOutputStream()
        var matched = 0
        val terminator = intArrayOf(13, 10, 13, 10)
        while (output.size() < 32768) {
            val value = input.read()
            if (value < 0) return null
            output.write(value)
            if (value == terminator[matched]) {
                matched++
                if (matched == 4) return output.toByteArray()
            } else {
                matched = if (value == 13) 1 else 0
            }
        }
        return null
    }

    private fun readExact(input: InputStream, length: Int): ByteArray {
        val result = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val count = input.read(result, offset, length - offset)
            if (count < 0) throw EOFException()
            offset += count
        }
        return result
    }

    private fun writeSocksReply(output: OutputStream, status: Int) {
        output.write(byteArrayOf(5, status.toByte(), 0, 1, 0, 0, 0, 0, 0, 0))
        output.flush()
    }

    private fun writeHttpError(output: OutputStream, code: Int, message: String) {
        val body = message.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 " + code + " " + message + "\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: " +
            body.size + "\r\nConnection: close\r\n\r\n"
        runCatching {
            output.write(header.toByteArray(Charsets.ISO_8859_1))
            output.write(body)
            output.flush()
        }
    }

    private fun relay(client: Socket, upstream: Socket, clientInput: InputStream) {
        val finished = CountDownLatch(1)
        val clientToServer = Thread {
            copyStream(clientInput, upstream.getOutputStream(), upstream)
            finished.countDown()
        }
        val serverToClient = Thread {
            copyStream(upstream.getInputStream(), client.getOutputStream(), client)
            finished.countDown()
        }
        clientToServer.isDaemon = true
        serverToClient.isDaemon = true
        clientToServer.start()
        serverToClient.start()
        finished.await(120, TimeUnit.SECONDS)
        runCatching { client.close() }
        runCatching { upstream.close() }
        runCatching { clientToServer.join(300) }
        runCatching { serverToClient.join(300) }
    }

    private fun copyStream(input: InputStream, output: OutputStream, destination: Socket) {
        try {
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                output.write(buffer, 0, count)
                output.flush()
            }
        } catch (_: Exception) {
        } finally {
            runCatching { destination.shutdownOutput() }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "NOVA local proxy", NotificationManager.IMPORTANCE_LOW)
        channel.description = "Shows when NOVA's on-device proxy is running"
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("NOVA local proxy is active")
            .setContentText("Listening privately on 127.0.0.1:" + PORT)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        active.set(false)
        ProxyState.setRunning(false)
        runCatching { serverSocket?.close() }
        workers.shutdownNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

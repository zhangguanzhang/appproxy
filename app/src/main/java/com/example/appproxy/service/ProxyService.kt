package com.example.appproxy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.appproxy.CHANNEL_ID
import com.example.appproxy.NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.appproxy.NOTIFICATION_CHANNEL_NAME
import com.example.appproxy.R
import com.example.appproxy.data.APPDataBase
import com.example.appproxy.data.AppSelectionRepository
import com.example.appproxy.data.ProxyConfig
import com.example.appproxy.data.toUri
import engine.Engine
import engine.Key
import kotlinx.coroutines.runBlocking

var isVpnServiceRunning = false

public class ProxyService() : VpnService() {

    private val TAG = "->${this.javaClass.simpleName} "

    companion object {
        const val ACTION_CONNECT = "com.example.appproxy.CONNECT"
        const val ACTION_DISCONNECT = "com.example.appproxy.DISCONNECT"

        var isVpnRunning = false
    }

    private lateinit var vpnInterface: ParcelFileDescriptor

    inner class ProxyBinder : Binder() {
        fun isRunning(): Boolean = isVpnRunning

        fun addStateChangeListener(listener: (Boolean) -> Unit) {
            stateChangeListeners.add(listener)
        }

        fun removeStateChangeListener(listener: (Boolean) -> Unit) {
            stateChangeListeners.remove(listener)
        }
    }

    private val stateChangeListeners = mutableListOf<(Boolean) -> Unit>()

    private fun notifyStateChange(isRunning: Boolean) {
        stateChangeListeners.forEach { it(isRunning) }
    }

    private val binder = ProxyBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ProxyService ")
        isVpnRunning = false
        isVpnServiceRunning = false
        // 只有在 vpnInterface 已初始化时才关闭
        if (::vpnInterface.isInitialized) {
            vpnInterface.close()
        }
        sendBroadcast(Intent(ACTION_DISCONNECT))
        Log.d(TAG, "发送断开连接广播")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_CONNECT -> {
                Log.d(TAG, "收到连接命令")
                startVpn()
            }

            ACTION_DISCONNECT -> {
                Log.d(TAG, "收到断开命令")
                stopVpn()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        Log.v(TAG, "createNotificationChannel")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            Log.e(TAG, "Notification permission denied")
            return
        }

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = NOTIFICATION_CHANNEL_NAME
            val chDescription = NOTIFICATION_CHANNEL_DESCRIPTION

            val channel = NotificationChannel(
                CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = chDescription
            }

            // Add the channel
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.createNotificationChannel(channel)
        }
    }

    // https://github.com/tailscale/tailscale-android/blob/89e7be0a46bb0d5242fd457107ace1406143f77a/android/src/main/java/com/tailscale/ipn/App.kt#L498
    private fun createNotification(): Notification {
        Log.v(TAG, "createNotification")
        // Create notification channel (if targeting Android Oreo and above)
        createNotificationChannel()

        // Build the notification using NotificationCompat.Builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN Service")
            .setContentText("VPN service is running")
            .setSmallIcon(R.drawable.ic_vpn_notification)
            .setOngoing(true)
            .setSilent(true)
            .setAutoCancel(false)

        // Add actions to the notification (e.g., stop VPN service)
        addNotificationActions(builder)

        // Return the built notification
        return builder.build()
    }

    private fun addNotificationActions(builder: NotificationCompat.Builder) {
        Log.d(TAG, "addNotificationActions")

        // 创建停止 VPN 的 PendingIntent
        val stopIntent = Intent(this, ProxyService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 添加停止 VPN 的 action
        builder.addAction(
            R.drawable.ic_stop,
            "Stop VPN",
            stopPendingIntent
        )
    }

    private fun startVpn() {
        try {
            Log.d(TAG, "开始启动 VPN")
            startForeground(1, createNotification())

            val builder = Builder()
                .addAddress("10.0.0.2", 24)  // VPN 接口的 IP 地址
                .addRoute("0.0.0.0", 0)      // 路由所有流量
                .setMtu(1500)                // 设置 MTU
                .setSession(packageName)     // 设置会话名称

            // 获取选中的配置
            val (selectedConfig, selectedApps) = runBlocking<Pair<ProxyConfig?, List<String>>> {
                val db = APPDataBase.getDatabase(this@ProxyService)
                val config = db.proxyConfigDao().getSelectedConfig()
                val apps = AppSelectionRepository(this@ProxyService).getSelectedApps()
                Pair(config, apps)
            }

            if (selectedConfig == null) {
                Log.e(TAG, "没有选中的配置")
                return
            }

            // 添加允许的应用
            selectedApps.forEach { appPackageName ->
                try {
                    builder.addAllowedApplication(appPackageName)
                    Log.d(TAG, "添加允许的应用: $appPackageName")
                } catch (e: Exception) {
                    Log.e(TAG, "添加应用 $appPackageName 失败: ${e.message}")
                }
            }

            Log.d(TAG, "尝试建立 VPN 接口")
            val vpnInterface = builder.establish()
            if (vpnInterface == null) {
                Log.e(TAG, "VPN 接口建立失败，未授予权限")
                isVpnRunning = false
                isVpnServiceRunning = false
                notifyStateChange(false)
                sendBroadcast(Intent(ACTION_DISCONNECT))
                Log.d(TAG, "VPN 启动失败，发送断开连接广播")
                return
            }

            this.vpnInterface = vpnInterface
            Log.d(TAG, "VPN 接口建立成功")
            val proxyUri = selectedConfig!!.toUri()
            Log.d(TAG, "engine.proxy=${proxyUri}")
            try {
                val key = Key()
                key.mark = 0
                key.mtu = 1500
                key.device = "fd://" + vpnInterface.fd
                key.setInterface("")
                key.logLevel = "error"
                key.proxy = proxyUri
                key.restAPI = ""
                key.tcpSendBufferSize = ""
                key.tcpReceiveBufferSize = ""
                key.tcpModerateReceiveBuffer = false
                Engine.insert(key)
                Engine.start()
                Log.d(TAG, "startEngine: $key")

                // 只有在 VPN 成功启动后才更新状态和发送广播
                isVpnRunning = true
                isVpnServiceRunning = true
                notifyStateChange(true)
                sendBroadcast(Intent(ACTION_CONNECT))
                Log.d(TAG, "VPN 启动成功，发送连接广播")
            } catch (e: Exception) {
                Log.e(TAG, "VPN 启动异常: ${e.message}")
                isVpnRunning = false
                isVpnServiceRunning = false
                notifyStateChange(false)
                sendBroadcast(Intent(ACTION_DISCONNECT))
                Log.d(TAG, "VPN 启动异常，发送断开连接广播")
            }
        } catch (e: Exception) {
            Log.e(TAG, "VPN 启动失败: ${e.message}")
            isVpnRunning = false
            isVpnServiceRunning = false
            sendBroadcast(Intent(ACTION_DISCONNECT))
        }
    }

    fun stopVpn() {
        Log.d(TAG, "stopVpn")
        isVpnRunning = false
        isVpnServiceRunning = false
        notifyStateChange(false)
        sendBroadcast(Intent(ACTION_DISCONNECT))

        // 只有在 vpnInterface 已初始化时才关闭
        if (::vpnInterface.isInitialized) {
            vpnInterface.close()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

}
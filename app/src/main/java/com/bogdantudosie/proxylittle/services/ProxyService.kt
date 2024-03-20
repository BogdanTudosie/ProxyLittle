package com.bogdantudosie.proxylittle.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.widget.Toast

class ProxyService : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // create the proxy service
    val littleProxy: LittleProxyServer = LittleProxyServer()

    private inner class ServiceHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
        super.onCreate()

        HandlerThread("ProxyService", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "LittleProxy Service Starting", Toast.LENGTH_LONG).show()
        serviceHandler?.obtainMessage()?.also {msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)

            // Start the service
            littleProxy.runService()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        TODO("Return the communication channel to the service.")
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "Service is done for", Toast.LENGTH_SHORT).show()
    }
}
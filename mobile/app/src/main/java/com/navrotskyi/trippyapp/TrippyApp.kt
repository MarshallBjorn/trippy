package com.navrotskyi.trippyapp

import android.app.Application
import com.navrotskyi.trippyapp.data.network.NetworkMonitor
import com.navrotskyi.trippyapp.data.sync.SyncManager

class
TrippyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkMonitor.init(this)
        SyncManager.init(this)
    }
}
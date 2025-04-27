package com.example.appproxy

import android.app.Application
import android.content.Context
import com.example.appproxy.data.AppContainer
import com.example.appproxy.data.AppDataContainer
import com.example.appproxy.data.AppSelectionRepository


class ProxyApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    lateinit var appSelectionRepository: AppSelectionRepository

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        appSelectionRepository = AppSelectionRepository(this)
    }
}
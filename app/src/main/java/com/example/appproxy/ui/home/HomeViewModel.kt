/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appproxy.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproxy.MainActivity
import com.example.appproxy.data.ProxyConfig
import com.example.appproxy.data.ProxyConfigRepository
import com.example.appproxy.service.ProxyService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve all items in the Room database.
 */
class HomeViewModel(
    private val itemsRepository: ProxyConfigRepository,
    private val activity: MainActivity
) : ViewModel() {

    private val TAG = "->${this.javaClass.simpleName} "

    val homeUiState: StateFlow<HomeUiState> =
        itemsRepository.getAllProxyConfigStream().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )

    private val _isVpnRunning = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isVpnRunning: StateFlow<Boolean> = _isVpnRunning

    private val serviceConnection = object : ServiceConnection {
        private var stateChangeListener: ((Boolean) -> Unit)? = null
        private var proxyBinder: ProxyService.ProxyBinder? = null

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            proxyBinder = service as? ProxyService.ProxyBinder
            // 设置状态监听器
            stateChangeListener = { isRunning ->
                Log.d(TAG, "收到状态更新: $isRunning")
                _isVpnRunning.value = isRunning
            }
            proxyBinder?.addStateChangeListener(stateChangeListener!!)
            // 获取初始状态
            _isVpnRunning.value = proxyBinder?.isRunning() ?: false
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            stateChangeListener?.let { listener ->
                proxyBinder?.removeStateChangeListener(listener)
            }
            stateChangeListener = null
            proxyBinder = null
            _isVpnRunning.value = false
        }
    }

    init {
        // 尝试绑定到服务
        val intent = Intent(activity, ProxyService::class.java)
        activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        super.onCleared()
        // 解绑服务
        activity.unbindService(serviceConnection)
    }

    /**
     * Updates the selected item in the database
     */
    fun updateSelectedItem(selectedId: Int) {
        viewModelScope.launch {
            itemsRepository.updateSelections(selectedId)
        }
    }

    /**
     * Deletes an item from the database
     */
    fun deleteItem(item: ProxyConfig) {
        viewModelScope.launch {
            itemsRepository.deleteProxyConfig(item)
        }
    }

    fun toggleVpnService() {
        Log.d(TAG, "切换 VPN 服务状态，当前状态: ${_isVpnRunning.value}")
        if (_isVpnRunning.value) {
            startProxyService(false)
        } else {
            val intent = VpnService.prepare(activity)
            if (intent != null) {
                // 需要请求 VPN 权限
                activity.requestVpnPermission(intent) { granted ->
                    if (granted) {
                        startProxyService(true)
                    } else {
                        Log.d(TAG, "VPN 权限被拒绝")
                    }
                }
            } else {
                // 已经有 VPN 权限，直接启动服务
                startProxyService(true)
            }
        }
    }

    private fun startProxyService(start: Boolean) {
        val intent = Intent(activity, ProxyService::class.java).apply {
            action = if (start) ProxyService.ACTION_CONNECT else ProxyService.ACTION_DISCONNECT
        }
        activity.startService(intent)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(val itemList: List<ProxyConfig> = listOf())

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

package com.example.appproxy.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [ProxyConfig] from a given data source.
 */
interface ProxyConfigRepository {
    /**
     * Retrieve all the ProxyConfigs from the the given data source.
     */
    fun getAllProxyConfigStream(): Flow<List<ProxyConfig>>

    /**
     * Retrieve an ProxyConfig from the given data source that matches with the [id].
     */
    fun getProxyConfigStream(id: Int): Flow<ProxyConfig?>

    /**
     * Insert ProxyConfig in the data source
     */
    suspend fun insertProxyConfig(proxyConfig: ProxyConfig)

    /**
     * Delete ProxyConfig from the data source
     */
    suspend fun deleteProxyConfig(proxyConfig: ProxyConfig)

    /**
     * Update ProxyConfig in the data source
     */
    suspend fun updateProxyConfig(proxyConfig: ProxyConfig)

    suspend fun updateSelections(selectedId: Int)
}


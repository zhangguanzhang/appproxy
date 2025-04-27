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

class OfflineProxyConfigsRepository(private val proxyConfigDao: ProxyConfigDao) : ProxyConfigRepository {
    override fun getAllProxyConfigStream(): Flow<List<ProxyConfig>> = proxyConfigDao.getAllConfig()

    override fun getProxyConfigStream(id: Int): Flow<ProxyConfig?> = proxyConfigDao.getConfig(id)

    override suspend fun insertProxyConfig(proxyConfig: ProxyConfig) = proxyConfigDao.insert(proxyConfig)

    override suspend fun deleteProxyConfig(proxyConfig: ProxyConfig) = proxyConfigDao.delete(proxyConfig)

    override suspend fun updateProxyConfig(proxyConfig: ProxyConfig) = proxyConfigDao.update(proxyConfig)
    override suspend fun updateSelections(selectedId: Int) = proxyConfigDao.updateSelections(selectedId)
}

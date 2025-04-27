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

package com.example.appproxy.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.appproxy.data.ProxyConfigRepository
import com.example.appproxy.data.ProxyType
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * ViewModel to retrieve and update an item from the [ProxyConfigRepository]'s data source.
 */
class ItemEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ProxyConfigRepository
) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val itemId: Int = checkNotNull(savedStateHandle[ItemEditDestination.itemIdArg])

    /**
     * Loads the item from the [ProxyConfigRepository]'s data source
     */
    suspend fun loadItem() {
        itemUiState = itemsRepository.getProxyConfigStream(itemId)
            .filterNotNull()
            .first()
            .toItemUiState(true)
    }

    /**
     * Update the item in the [ProxyConfigRepository]'s data source
     */
    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails)) {
            itemsRepository.updateProxyConfig(itemUiState.itemDetails.toItem())
        }
    }

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        if (uiState.name.isBlank() || uiState.type.isBlank()) {
            return false
        }

        val proxyType = ProxyType.valueOf(uiState.type.uppercase())
        return proxyType.requiredFields.all { field ->
            when (field) {
                "server" -> uiState.server.isNotBlank()
                "port" -> uiState.port in 0..65535
//                "user" -> uiState.user.isNotBlank()
//                "pass" -> uiState.pass.isNotBlank()
                else -> true
            }
        }
    }
}

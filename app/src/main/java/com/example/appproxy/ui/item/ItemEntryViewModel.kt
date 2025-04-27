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
import androidx.lifecycle.ViewModel
import com.example.appproxy.data.ProxyConfig
import com.example.appproxy.data.ProxyConfigRepository
import com.example.appproxy.data.ProxyType

/**
 * ViewModel to validate and insert ProxyConfig in the Room database.
 */
class ItemEntryViewModel(private val itemsRepository: ProxyConfigRepository) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    /**
     * Inserts an [ProxyConfig] in the Room database
     */
    suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertProxyConfig(itemUiState.itemDetails.toItem())
        }
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

/**
 * Represents Ui State for an Item.
 */
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val type: String = "http",
    val user: String = "",
    val pass: String = "",
    val server: String = "",
    val port: Int = 0,
    val selected: Boolean = false,
)


fun ItemDetails.toItem(): ProxyConfig = ProxyConfig(
    id = id,
    name = name,
    type = ProxyType.valueOf(type.uppercase()),
    user = user,
    pass = pass,
    server = server,
    port = port,
    selected = selected,
)


/**
 * Extension function to convert [ProxyConfig] to [ItemUiState]
 */
fun ProxyConfig.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [ProxyConfig] to [ItemDetails]
 */
fun ProxyConfig.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    type = type.toString(),
    user = user,
    pass = pass,
    server = server,
    port = port,
    selected = selected,
)

package com.example.appproxy.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appproxy.R
import com.example.appproxy.ui.theme.AppproxyTheme

data class FieldConfig(
    val labelRes: Int,
    val keyboardType: KeyboardType,
    val getter: (ItemDetails) -> String,
    val setter: (ItemDetails, String) -> ItemDetails,
    val validator: (String) -> Boolean = { true }
)

val fieldConfigMap = mapOf(
    "name" to FieldConfig(
        labelRes = R.string.item_name,
        keyboardType = KeyboardType.Text,
        getter = { it.name },
        setter = { item, value -> item.copy(name = value) }
    ),
    "server" to FieldConfig(
        labelRes = R.string.item_server,
        keyboardType = KeyboardType.Uri,
        getter = { it.server },
        setter = { item, value -> item.copy(server = value) },
        validator = { it.isNotBlank() }
    ),
    "port" to FieldConfig(
        labelRes = R.string.item_port,
        keyboardType = KeyboardType.Number,
        getter = { if (it.port == 0) "" else it.port.toString() },
        setter = { item, value ->
            item.copy(port = value.toIntOrNull() ?: 0)
        },
        validator = { it.toIntOrNull() in 1..65535 }
    ),
    "user" to FieldConfig(
        labelRes = R.string.item_user,
        keyboardType = KeyboardType.Ascii,
        getter = { it.user },
        setter = { item, value -> item.copy(user = value) }
    ),
    "pass" to FieldConfig(
        labelRes = R.string.item_pass,
        keyboardType = KeyboardType.Password,
        getter = { it.pass },
        setter = { item, value -> item.copy(pass = value) }
    )
)


@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    label: String,
) {
    var currentValue by remember { mutableStateOf(value) }

//    LaunchedEffect(value) {
//        currentValue = value
//    }

    OutlinedTextField(
        value = currentValue,
        onValueChange = {
            currentValue = it
            onValueChange(it)
        },
        keyboardOptions = keyboardOptions,
        label = { Text(label) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
        enabled = true,
        singleLine = true
    )
}

@Composable
fun DynamicForm(
    fields: List<String>,
    item: ItemDetails,
    onItemUpdate: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        fields.forEachIndexed { index, fieldKey ->
            val config = fieldConfigMap[fieldKey] ?: return@forEachIndexed
            val currentValue = config.getter(item)

            CustomOutlinedTextField(
                value = currentValue,
                onValueChange = { newValue ->
                    onItemUpdate(config.setter(item, newValue))
                },
                label = stringResource(config.labelRes),
                keyboardOptions = KeyboardOptions(
                    keyboardType = config.keyboardType,
                    imeAction = if (index == fields.lastIndex) {
                        ImeAction.Done
                    } else {
                        ImeAction.Next
                    }
                ),
            )
        }
    }
}

@Preview
@Composable
private fun DynamicFormPreview() {
    AppproxyTheme {
        var itemDetail by remember { mutableStateOf(ItemDetails()) }
        DynamicForm(
            fields = listOf("server", "port"),
            item = itemDetail,
            onItemUpdate = { updatedItem ->
                itemDetail = updatedItem
            }
        )
    }
}



@Preview
@Composable
private fun CustomOutlinedTextFieldPreview() {
    AppproxyTheme {
        var textValue by remember { mutableStateOf("123") }
        CustomOutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            label = "test",
        )
    }
}

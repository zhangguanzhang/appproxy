package com.example.appproxy.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproxy.ProxyTopAppBar
import com.example.appproxy.R
import com.example.appproxy.data.ProxyConfig
import com.example.appproxy.data.ProxyType
import com.example.appproxy.data.toDisplayUri
import com.example.appproxy.ui.AppViewModelProvider
import com.example.appproxy.ui.navigation.NavigationDestination
import com.example.appproxy.ui.theme.AppproxyTheme

//
object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.title_server
}


/**
 * Entry route for Home screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToItemEntry: () -> Unit,
    navigateToEditItem: (Int) -> Unit,
    navigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val isVpnRunning by viewModel.isVpnRunning.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ProxyTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior,
                navigateAdd = navigateToItemEntry,
                navigateSetting = navigateToProfile,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleVpnService() },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(LocalLayoutDirection.current)
                    )
            ) {
                Icon(
                    imageVector = if (isVpnRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = if (isVpnRunning) "停止 VPN" else "启动 VPN"
                )
            }
        }
    ) { innerPadding ->
        HomeBody(
            itemList = homeUiState.itemList,
            onItemClick = { viewModel.updateSelectedItem(it) },
            onItemDoubleClick = navigateToEditItem,
            onItemDelete = { viewModel.deleteItem(it) },
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding,
        )
    }
}


@Composable
private fun HomeBody(
    itemList: List<ProxyConfig>,
    onItemClick: (Int) -> Unit,
    onItemDoubleClick: (Int) -> Unit,
    onItemDelete: (ProxyConfig) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        ConfigList(
            itemList = itemList,
            onItemClick = onItemClick,
            onItemDoubleClick = onItemDoubleClick,
            onItemDelete = onItemDelete,
            contentPadding = contentPadding,
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small)),
        )
    }
}

@Composable
private fun ConfigList(
    itemList: List<ProxyConfig>,
    onItemClick: (Int) -> Unit,
    onItemDoubleClick: (Int) -> Unit,
    onItemDelete: (ProxyConfig) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(items = itemList, key = { it.id }) { item ->
            ConfigItem(
                item = item,
                onDelete = { onItemDelete(item) },
                onClick = { onItemClick(item.id) },
                onDoubleClick = { onItemDoubleClick(item.id) },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Composable
private fun ConfigItem(
    item: ProxyConfig,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除配置") },
            text = { Text("确定要删除配置 ${item.name} 吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    },
                    onDoubleTap = {
                        onDoubleClick()
                    },
                    onLongPress = {
                        showDeleteDialog = true
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = item.selected,
                onClick = {}
            )
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = item.toDisplayUri(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.weight(1f))
            Text(
                text = item.type.toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    AppproxyTheme {
        HomeBody(
            listOf(
                ProxyConfig(1, "test", ProxyType.HTTP, "test", "", "", 80, false),
                ProxyConfig(2, "test", ProxyType.SOCKS4, "test", "", "", 80, false),
            ),
            onItemClick = {},
            onItemDoubleClick = {},
            onItemDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryItemPreview() {
    AppproxyTheme {
        ConfigItem(
            item = ProxyConfig(1, "test", ProxyType.SOCKS5, "test", "", "", 80, false),
            onDelete = {},
            onClick = {},
            onDoubleClick = {},
        )
    }
}
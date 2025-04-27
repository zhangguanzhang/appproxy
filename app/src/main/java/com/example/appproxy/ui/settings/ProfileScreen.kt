package com.example.appproxy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.appproxy.ProxyTopAppBar
import com.example.appproxy.R
import com.example.appproxy.ui.navigation.NavigationDestination
import com.example.appproxy.ui.theme.AppproxyTheme

object ProfileDestination : NavigationDestination {
    override val route = "profile"
    override val titleRes = R.string.title_setting
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    settingsNav: SettingsNav,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            ProxyTopAppBar(
                title = stringResource(R.string.profile_title),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            // 应用选择选项
            ProfileMenuItem(
                title = stringResource(R.string.app_selection_title),
                icon = Icons.Default.Settings,
                onClick = settingsNav.onNavigateToAppSelection
            )
            HorizontalDivider()
            // 关于选项
            ProfileMenuItem(
                title = stringResource(R.string.about_title),
                icon = Icons.Default.Info,
                onClick = settingsNav.onNavigateToAbout
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.padding_medium),
                vertical = dimensionResource(R.dimen.padding_large)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(R.dimen.padding_medium))
        )

    }
}


@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    AppproxyTheme {
        ProfileScreen(
            settingsNav = SettingsNav(
                onNavigateToAbout = {},
                onNavigateToAppSelection = {},
                onNavigateBackHome = {},
            ),
            onNavigateUp = {}
        )
    }
}
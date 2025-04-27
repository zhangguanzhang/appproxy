package com.example.appproxy.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

import com.example.appproxy.MainActivity
import com.example.appproxy.ProxyApplication
import com.example.appproxy.ui.home.HomeViewModel
import com.example.appproxy.ui.item.ItemEntryViewModel
import com.example.appproxy.ui.item.ItemEditViewModel
import com.example.appproxy.ui.settings.AppSelectionViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    private var mainActivity: MainActivity? = null

    fun setMainActivity(activity: MainActivity) {
        mainActivity = activity
    }

    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            ItemEditViewModel(
                this.createSavedStateHandle(),
                proxyApplication().container.proxyConfigsRepository
            )
        }
        // Initializer for ItemEntryViewModel
        initializer {
            ItemEntryViewModel(proxyApplication().container.proxyConfigsRepository)
        }

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(
                proxyApplication().container.proxyConfigsRepository,
                mainActivity ?: throw IllegalStateException("MainActivity not set")
            )
        }

        // Initializer for AppSelectionViewModel
        initializer {
            AppSelectionViewModel(
                proxyApplication().packageManager,
                proxyApplication().appSelectionRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [ProxyApplication].
 */
fun CreationExtras.proxyApplication(): ProxyApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ProxyApplication)

package com.example.appproxy.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppSelectionRepository(context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedApps(): List<String> {
        return sharedPreferences.getStringSet(KEY_SELECTED_APPS, emptySet())?.toList() ?: emptyList()
    }

    fun saveSelectedApps(apps: List<String>) {
        sharedPreferences.edit() {
            putStringSet(KEY_SELECTED_APPS, apps.toSet())
        }
    }

    companion object {
        private const val PREFS_NAME = "app_selection_prefs"
        private const val KEY_SELECTED_APPS = "selected_apps"
    }
} 
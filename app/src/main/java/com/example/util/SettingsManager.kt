package com.example.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    private val ARCHIVE_URI_KEY = stringPreferencesKey("archive_uri")

    val archiveUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ARCHIVE_URI_KEY]
    }

    suspend fun saveArchiveUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[ARCHIVE_URI_KEY] = uri
        }
    }
}

package com.oltrysifp.chessclock.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oltrysifp.chessclock.models.UserData
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("prefs")

class DataStoreManager(private val context: Context) {
    suspend fun saveUserData(userData: UserData) {
        context.dataStore.edit { pref ->
            pref[stringPreferencesKey("userData")] = Json.encodeToString(userData)
        }
    }

    fun getUserData() = context.dataStore.data.map { pref ->
        val userData = pref[stringPreferencesKey("userData")]

        if (userData != null) {
            try {
                return@map Json.decodeFromString<UserData>(userData)
            } catch (e: Exception) {
                return@map Constants.userDataDefault()
            }
        } else {
            return@map Constants.userDataDefault()
        }
    }
}
package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.cycleDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "solstice_cycle_prefs"
)

class CyclePreferencesStore(private val context: Context) {

    private object Keys {
        val CYCLE_START_DAY = intPreferencesKey("financial_cycle_start_day")
    }

    val cycleStartDayFlow: Flow<Int> = context.cycleDataStore.data.map { prefs ->
        (prefs[Keys.CYCLE_START_DAY] ?: 1).coerceIn(1, 28)
    }

    suspend fun setCycleStartDay(day: Int) {
        context.cycleDataStore.edit { prefs ->
            prefs[Keys.CYCLE_START_DAY] = day.coerceIn(1, 28)
        }
    }
}

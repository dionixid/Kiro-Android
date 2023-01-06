package com.codedillo.tinydb

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class TinyDB private constructor(
    private val store: DataStore<Preferences>
) {

    suspend fun getInt(key: String?, defaultValue: Int = 0): Int {
        key ?: return defaultValue
        return store.data.firstOrNull()?.get(intPreferencesKey(key)) ?: defaultValue
    }

    suspend fun getLong(key: String?, defaultValue: Long = 0): Long {
        key ?: return defaultValue
        return store.data.firstOrNull()?.get(longPreferencesKey(key)) ?: defaultValue
    }

    suspend fun getFloat(key: String?, defaultValue: Float = 0f): Float {
        key ?: return defaultValue
        return store.data.firstOrNull()?.get(floatPreferencesKey(key)) ?: defaultValue
    }

    suspend fun getDouble(key: String?, defaultValue: Double = 0.0): Double {
        key ?: return defaultValue
        return store.data.firstOrNull()?.get(doublePreferencesKey(key)) ?: defaultValue
    }

    suspend fun getBoolean(key: String?, defaultValue: Boolean = false): Boolean {
        key ?: return defaultValue
        return store.data.firstOrNull()?.get(booleanPreferencesKey(key)) ?: defaultValue
    }

    suspend fun getString(key: String?, defaultValue: String = ""): String {
        key ?: return defaultValue
        return store.data.firstOrNull()?.get(stringPreferencesKey(key)) ?: defaultValue
    }

    suspend fun <T> get(key: String?, classOfT: Class<T>?): T? {
        return try {
            Gson().fromJson(getString(key), classOfT)
        }catch (_: Exception) {
            null
        }
    }

    suspend fun getListInt(key: String?): List<Int> {
        return getListString(key).mapNotNull { it.toIntOrNull() }
    }

    suspend fun getListDouble(key: String?): List<Double> {
        return getListString(key).mapNotNull { it.toDoubleOrNull() }
    }

    suspend fun getListLong(key: String?): List<Long> {
        return getListString(key).mapNotNull { it.toLongOrNull() }
    }

    suspend fun getListBoolean(key: String?): List<Boolean> {
        return getListString(key).map { it.toBoolean() }
    }

    suspend fun getListString(key: String?): List<String> {
        key ?: return listOf()
        return store.data.first()[stringPreferencesKey(key)]?.split(DELIMITER) ?: listOf()
    }

    suspend fun <T> getList(key: String?, mClass: Class<T>?): List<T> {
        val gson = Gson()
        return try {
            getListString(key).mapNotNull { gson.fromJson(it, mClass) }
        } catch (_: Exception) {
            listOf()
        }
    }

    // Put methods
    suspend fun putInt(key: String?, value: Int) {
        key ?: return
        store.edit { it[intPreferencesKey(key)] = value }
    }

    suspend fun putLong(key: String?, value: Long) {
        key ?: return
        store.edit { it[longPreferencesKey(key)] = value }
    }

    suspend fun putFloat(key: String?, value: Float) {
        key ?: return
        store.edit { it[floatPreferencesKey(key)] = value }
    }

    suspend fun putDouble(key: String?, value: Double) {
        key ?: return
        store.edit { it[doublePreferencesKey(key)] = value }
    }

    suspend fun putBoolean(key: String?, value: Boolean) {
        key ?: return
        store.edit { it[booleanPreferencesKey(key)] = value }
    }

    suspend fun putString(key: String?, value: String?) {
        key ?: return
        value ?: return
        store.edit { it[stringPreferencesKey(key)] = value }
    }

    suspend fun <T> put(key: String?, obj: T?) {
        key ?: return
        obj ?: return
        putString(key, Gson().toJson(obj))
    }

    suspend fun putListInt(key: String?, list: List<Int>) {
        key ?: return
        putString(key, list.joinToString(DELIMITER))
    }

    suspend fun putListLong(key: String?, list: List<Long>) {
        key ?: return
        putString(key, list.joinToString(DELIMITER))
    }

    suspend fun putListDouble(key: String?, list: List<Double>) {
        key ?: return
        putString(key, list.joinToString(DELIMITER))
    }

    suspend fun putListString(key: String?, list: List<String>) {
        key ?: return
        putString(key, list.joinToString(DELIMITER))
    }

    suspend fun putListBoolean(key: String?, list: List<Boolean>) {
        key ?: return
        putString(key, list.joinToString(DELIMITER))
    }

    suspend fun <T> putListObject(key: String?, list: List<T?>) {
        key ?: return
        val gson = Gson()
        putListString(key, list.map { gson.toJson(it) })
    }

    suspend fun <T> remove(key: Preferences.Key<T>) {
        store.edit {
            it.remove(key)
        }
    }

    suspend fun clear() {
        store.edit {
            it.clear()
        }
    }

    companion object {
        private const val DELIMITER = "‚‗‚"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("TinyDB")
        private lateinit var tinyDB: TinyDB

        fun getStore(context: Context): DataStore<Preferences> = context.dataStore

        fun initialize(context: Context) {
            if (!this::tinyDB.isInitialized) {
                tinyDB = TinyDB(context.dataStore)
            }
        }

        fun getInstance(): TinyDB {
            if (!this::tinyDB.isInitialized) {
                throw IllegalStateException("Attempt to access TinyDB before it was initialized.")
            }
            return tinyDB
        }
    }

}
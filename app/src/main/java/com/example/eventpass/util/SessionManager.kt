package com.example.eventpass.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor seguro de sesión para almacenar y recuperar el token JWT
 * utilizando EncryptedSharedPreferences (AndroidX Security Crypto).
 */
class SessionManager private constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Guarda el token JWT de forma encriptada.
     */
    fun saveAuthToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    /**
     * Recupera el token JWT almacenado, o null si no existe.
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Guarda el ID y nombre del evento activo seleccionado para el check-in.
     */
    fun saveSelectedEvent(eventoId: String, eventoNombre: String = "") {
        sharedPreferences.edit()
            .putString(KEY_SELECTED_EVENT_ID, eventoId)
            .putString(KEY_SELECTED_EVENT_NAME, eventoNombre)
            .apply()
    }

    /**
     * Recupera el ID del evento seleccionado, o null si aún no se ha elegido.
     */
    fun getSelectedEventId(): String? {
        return sharedPreferences.getString(KEY_SELECTED_EVENT_ID, null)
    }

    /**
     * Recupera el nombre del evento seleccionado.
     */
    fun getSelectedEventName(): String? {
        return sharedPreferences.getString(KEY_SELECTED_EVENT_NAME, null)
    }

    /**
     * Elimina el token y limpia la sesión activa.
     */
    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_SELECTED_EVENT_ID)
            .remove(KEY_SELECTED_EVENT_NAME)
            .apply()
    }

    /**
     * Comprueba si el usuario tiene una sesión activa con token no vacío.
     */
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }

    companion object {
        private const val PREFS_NAME = "eventpass_secure_prefs"
        private const val KEY_AUTH_TOKEN = "jwt_token"
        private const val KEY_SELECTED_EVENT_ID = "selected_event_id"
        private const val KEY_SELECTED_EVENT_NAME = "selected_event_name"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

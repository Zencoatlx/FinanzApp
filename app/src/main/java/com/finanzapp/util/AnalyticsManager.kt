package com.finanzapp.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Clase que gestiona el seguimiento de eventos de analítica en la aplicación.
 * En un entorno de producción, esto podría integrarse con Firebase Analytics,
 * Mixpanel, o cualquier otra plataforma de analítica.
 */
class AnalyticsManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(ANALYTICS_PREFS, Context.MODE_PRIVATE)
    private val appContext = context.applicationContext

    // Estadísticas de uso que podemos consultar en la app
    private val _sessionCount = MutableLiveData<Int>()
    val sessionCount: LiveData<Int> = _sessionCount

    private val _totalTransactions = MutableLiveData<Int>()
    val totalTransactions: LiveData<Int> = _totalTransactions

    private val _featureUsage = MutableLiveData<Map<String, Int>>()
    val featureUsage: LiveData<Map<String, Int>> = _featureUsage

    companion object {
        private const val ANALYTICS_PREFS = "analytics_prefs"
        private const val USER_ID_KEY = "user_id"
        private const val FIRST_OPEN_DATE_KEY = "first_open_date"
        private const val SESSION_COUNT_KEY = "session_count"
        private const val TOTAL_TRANSACTIONS_KEY = "total_transactions"
        private const val FEATURE_USAGE_PREFIX = "feature_usage_"

        // Features que rastreamos
        const val FEATURE_ADD_TRANSACTION = "add_transaction"
        const val FEATURE_VIEW_STATS = "view_stats"
        const val FEATURE_CREATE_BUDGET = "create_budget"
        const val FEATURE_CREATE_SAVING_GOAL = "create_saving_goal"
        const val FEATURE_PREMIUM_VIEW = "premium_view"
        const val FEATURE_PREMIUM_PURCHASE = "premium_purchase"

        @Volatile
        private var instance: AnalyticsManager? = null

        fun getInstance(context: Context): AnalyticsManager {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsManager(context).also { instance = it }
            }
        }
    }

    init {
        initUserIfNeeded()
        incrementSessionCount()
        loadStatistics()
    }

    /**
     * Inicializa el ID de usuario si es la primera vez que se abre la app
     */
    private fun initUserIfNeeded() {
        if (!prefs.contains(USER_ID_KEY)) {
            val userId = UUID.randomUUID().toString()
            prefs.edit().putString(USER_ID_KEY, userId).apply()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            prefs.edit().putString(FIRST_OPEN_DATE_KEY, dateFormat.format(Date())).apply()
        }
    }

    /**
     * Incrementa el contador de sesiones
     */
    private fun incrementSessionCount() {
        val currentCount = prefs.getInt(SESSION_COUNT_KEY, 0)
        prefs.edit().putInt(SESSION_COUNT_KEY, currentCount + 1).apply()
        _sessionCount.value = currentCount + 1
    }

    /**
     * Carga las estadísticas guardadas
     */
    private fun loadStatistics() {
        _totalTransactions.value = prefs.getInt(TOTAL_TRANSACTIONS_KEY, 0)

        // Cargar uso de features
        val featureMap = mutableMapOf<String, Int>()
        featureMap[FEATURE_ADD_TRANSACTION] = prefs.getInt(FEATURE_USAGE_PREFIX + FEATURE_ADD_TRANSACTION, 0)
        featureMap[FEATURE_VIEW_STATS] = prefs.getInt(FEATURE_USAGE_PREFIX + FEATURE_VIEW_STATS, 0)
        featureMap[FEATURE_CREATE_BUDGET] = prefs.getInt(FEATURE_USAGE_PREFIX + FEATURE_CREATE_BUDGET, 0)
        featureMap[FEATURE_CREATE_SAVING_GOAL] = prefs.getInt(FEATURE_USAGE_PREFIX + FEATURE_CREATE_SAVING_GOAL, 0)
        featureMap[FEATURE_PREMIUM_VIEW] = prefs.getInt(FEATURE_USAGE_PREFIX + FEATURE_PREMIUM_VIEW, 0)
        featureMap[FEATURE_PREMIUM_PURCHASE] = prefs.getInt(FEATURE_USAGE_PREFIX + FEATURE_PREMIUM_PURCHASE, 0)

        _featureUsage.value = featureMap
    }

    /**
     * Registra un evento de uso de una característica
     */
    fun trackFeatureUsage(featureKey: String) {
        val currentCount = prefs.getInt(FEATURE_USAGE_PREFIX + featureKey, 0)
        prefs.edit().putInt(FEATURE_USAGE_PREFIX + featureKey, currentCount + 1).apply()

        // Actualizar map local
        val currentMap = _featureUsage.value?.toMutableMap() ?: mutableMapOf()
        currentMap[featureKey] = (currentMap[featureKey] ?: 0) + 1
        _featureUsage.value = currentMap
    }

    /**
     * Registra una nueva transacción creada
     */
    fun trackTransactionAdded() {
        val currentCount = prefs.getInt(TOTAL_TRANSACTIONS_KEY, 0)
        prefs.edit().putInt(TOTAL_TRANSACTIONS_KEY, currentCount + 1).apply()
        _totalTransactions.value = currentCount + 1

        // También aumentar contador de feature
        trackFeatureUsage(FEATURE_ADD_TRANSACTION)
    }

    /**
     * Obtiene información del dispositivo (para diagnóstico)
     */
    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "device" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "android_version" to Build.VERSION.RELEASE,
            "app_version" to getAppVersion()
        )
    }

    /**
     * Obtiene la versión de la aplicación
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "desconocida"
        }
    }

    /**
     * Obtiene el ID único de usuario
     */
    fun getUserId(): String {
        return prefs.getString(USER_ID_KEY, "") ?: ""
    }

    /**
     * Obtiene la fecha de primera apertura
     */
    fun getFirstOpenDate(): String {
        return prefs.getString(FIRST_OPEN_DATE_KEY, "") ?: ""
    }
}
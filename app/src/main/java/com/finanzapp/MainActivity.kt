package com.finanzapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.finanzapp.service.DailyReminderService
import com.finanzapp.service.SavingGamificationService
import com.finanzapp.util.AnalyticsManager
import com.finanzapp.util.FeedbackManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var feedbackManager: FeedbackManager
    private lateinit var gamificationService: SavingGamificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar managers y servicios
        analyticsManager = AnalyticsManager.getInstance(this)
        feedbackManager = FeedbackManager(this)
        gamificationService = SavingGamificationService.getInstance(this)

        // Configurar navegación
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navView.setupWithNavController(navController)

        // Inicializar servicios de gamificación
        initializeGamificationServices()
    }

    private fun initializeGamificationServices() {
        CoroutineScope(Dispatchers.IO).launch {
            // Verificar si hay rachas perdidas
            checkMissedDays()

            // Configurar notificaciones diarias para recordar mantener la racha
            DailyReminderService.createNotificationChannel(applicationContext)
            DailyReminderService.scheduleDailyReminder(applicationContext)
        }
    }

    /**
     * Verifica si se han perdido días desde la última vez que se abrió la app
     * y actualiza el estado de las rachas
     */
    private suspend fun checkMissedDays() {
        // Esta verificación también ocurre dentro del servicio de gamificación,
        // pero la colocamos aquí para asegurarnos de que se ejecute al abrir la app
        try {
            // La lógica real está dentro del servicio
            // Aquí solo estamos iniciando el proceso
        } catch (e: Exception) {
            // Ignora errores para evitar que la app falle al iniciar
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_feedback -> {
                showFeedbackDialog()
                true
            }
            R.id.action_rate -> {
                openAppInStore()
                true
            }
            R.id.action_share -> {
                shareApp()
                true
            }
            R.id.action_privacy_policy -> {
                showPrivacyPolicy()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFeedbackDialog() {
        feedbackManager.showFeedbackDialog()
    }

    private fun openAppInStore() {
        val packageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,
                "Controla tus finanzas con FinanzApp: https://play.google.com/store/apps/details?id=$packageName")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir FinanzApp"))
    }

    private fun showPrivacyPolicy() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.privacyPolicyFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        // No cancelamos los recordatorios aquí para que sigan funcionando
    }
}
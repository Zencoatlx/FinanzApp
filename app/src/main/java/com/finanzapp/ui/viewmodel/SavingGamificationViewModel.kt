package com.finanzapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.finanzapp.data.AppDatabase
import com.finanzapp.data.entity.Achievement
import com.finanzapp.data.entity.SavingChallenge
import com.finanzapp.data.entity.SavingRank
import com.finanzapp.data.entity.SavingStreak
import com.finanzapp.data.entity.UserLevel
import com.finanzapp.data.repository.AchievementRepository
import com.finanzapp.data.repository.SavingChallengeRepository
import com.finanzapp.data.repository.SavingStreakRepository
import com.finanzapp.data.repository.UserLevelRepository
import com.finanzapp.service.SavingGamificationService
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel que gestiona la lógica de gamificación para la UI.
 */
class SavingGamificationViewModel(application: Application) : AndroidViewModel(application) {

    // Servicio principal de gamificación
    private val gamificationService = SavingGamificationService.getInstance(application)

    // Repositorios
    private val achievementRepository: AchievementRepository
    private val savingChallengeRepository: SavingChallengeRepository
    private val savingStreakRepository: SavingStreakRepository
    private val userLevelRepository: UserLevelRepository

    // LiveData para la UI
    val userLevel: LiveData<UserLevel?>
    val savingStreak: LiveData<SavingStreak?>
    val achievements: LiveData<List<Achievement>>
    val unlockedAchievements: LiveData<List<Achievement>>
    val activeChallenges: LiveData<List<SavingChallenge>>
    val completedChallenges: LiveData<List<SavingChallenge>>
    val availableChallenges: LiveData<List<SavingChallenge>>

    // LiveData para resultados de gamificación
    private val _lastGamificationResult = MutableLiveData<SavingGamificationService.GamificationResult>()
    val lastGamificationResult: LiveData<SavingGamificationService.GamificationResult> = _lastGamificationResult

    // Estado de UI
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Contadores para insignias
    val unlockedAchievementsCount: LiveData<Int>
    val totalAchievementsCount: LiveData<Int>
    val completedChallengesCount: LiveData<Int>

    init {
        // Inicializar repositorios
        val database = AppDatabase.getDatabase(application)
        achievementRepository = AchievementRepository(database.achievementDao())
        savingChallengeRepository = SavingChallengeRepository(database.savingChallengeDao())
        savingStreakRepository = SavingStreakRepository(database.savingStreakDao())
        userLevelRepository = UserLevelRepository(database.userLevelDao())

        // Inicializar LiveData
        userLevel = userLevelRepository.userLevel
        savingStreak = savingStreakRepository.savingStreak
        achievements = achievementRepository.allAchievements
        unlockedAchievements = achievementRepository.unlockedAchievements
        activeChallenges = savingChallengeRepository.activeChallenges
        completedChallenges = savingChallengeRepository.completedChallenges
        availableChallenges = savingChallengeRepository.getAvailableChallenges(5)

        // Contadores
        unlockedAchievementsCount = achievementRepository.unlockedCount
        totalAchievementsCount = achievementRepository.totalCount
        completedChallengesCount = savingChallengeRepository.completedChallengesCount

        // Inicializar datos si es necesario
        viewModelScope.launch {
            savingStreakRepository.initialize()
            userLevelRepository.initialize()
        }
    }

    /**
     * Registra una contribución a una meta de ahorro y procesa las
     * recompensas de gamificación asociadas.
     */
    fun registerSavingContribution(goalId: Long, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Procesar la contribución a través del servicio
                val result = gamificationService.registerSavingGoalContribution(goalId, amount)

                // Notificar a la UI
                _lastGamificationResult.value = result

                // Reproducir efectos si hay celebraciones
                if (result.hasCelebrations) {
                    gamificationService.playCelebrationSound()
                    gamificationService.vibrate()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Activa un desafío para el usuario.
     */
    fun activateChallenge(challengeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                gamificationService.activateChallenge(challengeId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene las estadísticas del usuario en un formato consolidado.
     */
    fun getUserStats(callback: (SavingGamificationService.UserStats) -> Unit) {
        viewModelScope.launch {
            val stats = gamificationService.getUserStats()
            callback(stats)
        }
    }

    /**
     * Verifica si el usuario ya ha ahorrado hoy.
     */
    fun hasSavedToday(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val savedToday = gamificationService.hasSavedToday()
            callback(savedToday)
        }
    }

    /**
     * Calcula el progreso actual del nivel (0-100%).
     */
    fun getLevelProgress(callback: (Int) -> Unit) {
        viewModelScope.launch {
            val progress = userLevelRepository.calculateLevelProgress()
            callback(progress)
        }
    }

    /**
     * Habilita o deshabilita los efectos de sonido.
     */
    fun setSoundEffects(enabled: Boolean) {
        gamificationService.setSoundEffects(enabled)
    }

    /**
     * Obtiene el nombre visual para un rango.
     */
    fun getRankDisplayName(rank: SavingRank): String {
        return when (rank) {
            SavingRank.NOVICE -> "Novato"
            SavingRank.SAVER -> "Ahorrador"
            SavingRank.MONEY_MASTER -> "Maestro del Dinero"
            SavingRank.BUDGET_NINJA -> "Ninja del Presupuesto"
            SavingRank.WEALTH_WARRIOR -> "Guerrero de la Riqueza"
            SavingRank.FINANCE_LEGEND -> "Leyenda Financiera"
            SavingRank.ECONOMY_TITAN -> "Titán Económico"
            SavingRank.SAVINGS_SUPERHERO -> "Superhéroe del Ahorro"
        }
    }

    /**
     * Obtiene puntos de rango necesarios para el siguiente nivel.
     */
    fun getPointsToNextRank(rank: SavingRank): Int {
        return when (rank) {
            SavingRank.NOVICE -> 100
            SavingRank.SAVER -> 500
            SavingRank.MONEY_MASTER -> 1000
            SavingRank.BUDGET_NINJA -> 2500
            SavingRank.WEALTH_WARRIOR -> 5000
            SavingRank.FINANCE_LEGEND -> 10000
            SavingRank.ECONOMY_TITAN -> 25000
            SavingRank.SAVINGS_SUPERHERO -> Int.MAX_VALUE
        }
    }

    /**
     * Obtiene el color asociado a un rango.
     */
    fun getRankColor(rank: SavingRank): Int {
        // Devuelve un recurso de color
        return when (rank) {
            SavingRank.NOVICE -> android.R.color.darker_gray
            SavingRank.SAVER -> android.R.color.holo_blue_light
            SavingRank.MONEY_MASTER -> android.R.color.holo_green_light
            SavingRank.BUDGET_NINJA -> android.R.color.holo_orange_light
            SavingRank.WEALTH_WARRIOR -> android.R.color.holo_purple
            SavingRank.FINANCE_LEGEND -> android.R.color.holo_red_light
            SavingRank.ECONOMY_TITAN -> android.R.color.holo_blue_dark
            SavingRank.SAVINGS_SUPERHERO -> android.R.color.holo_orange_dark
        }
    }
}
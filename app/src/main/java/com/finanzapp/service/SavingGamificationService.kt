package com.finanzapp.service

import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import android.widget.Toast
import com.finanzapp.R
import com.finanzapp.data.AppDatabase
import com.finanzapp.data.entity.*
import com.finanzapp.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

/**
 * Servicio centralizado que coordina todas las funcionalidades de gamificación.
 * Este servicio actúa como el cerebro del sistema "Finanz Hero".
 */
class SavingGamificationService private constructor(private val context: Context) {

    // Repositorios
    private val savingGoalRepository: SavingGoalRepository
    private val savingChallengeRepository: SavingChallengeRepository
    private val achievementRepository: AchievementRepository
    private val savingStreakRepository: SavingStreakRepository
    private val userLevelRepository: UserLevelRepository

    // Para efectos
    private var vibrator: Vibrator? = null
    private var soundEffects: Boolean = true

    init {
        // Inicializar repositorios
        val database = AppDatabase.getDatabase(context)
        savingGoalRepository = SavingGoalRepository(database.savingGoalDao())
        savingChallengeRepository = SavingChallengeRepository(database.savingChallengeDao())
        achievementRepository = AchievementRepository(database.achievementDao())
        savingStreakRepository = SavingStreakRepository(database.savingStreakDao())
        userLevelRepository = UserLevelRepository(database.userLevelDao())

        // Inicializar vibrador
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

        // Asegurar que los datos base estén inicializados
        CoroutineScope(Dispatchers.IO).launch {
            savingStreakRepository.initialize()
            userLevelRepository.initialize()

            // Verificar por rachas perdidas (run once a day, ideally)
            checkMissedDays()
        }
    }

    companion object {
        @Volatile
        private var instance: SavingGamificationService? = null

        fun getInstance(context: Context): SavingGamificationService {
            return instance ?: synchronized(this) {
                instance ?: SavingGamificationService(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Registra una nueva contribución a una meta de ahorro y activa el sistema de gamificación.
     * @param goalId ID de la meta de ahorro
     * @param amount Cantidad ahorrada
     */
    suspend fun registerSavingGoalContribution(goalId: Long, amount: Double): GamificationResult {
        if (amount <= 0) return GamificationResult()

        // 1. Obtener la meta de ahorro
        val savingGoal = savingGoalRepository.getSavingGoalById(goalId) ?: return GamificationResult()

        // 2. Actualizar racha de ahorro
        val updatedStreak = savingStreakRepository.registerSavingActivity(amount)

        // Calcular bonus por racha
        val streakBonus = savingStreakRepository.calculateStreakBonus()
        userLevelRepository.updateStreakBonus(streakBonus)

        // 3. Añadir XP base por la contribución (10 XP por cada $100)
        val baseXP = (amount / 10).toInt().coerceAtLeast(1)
        val leveledUp = userLevelRepository.addExperience(baseXP, streakBonus)

        // 4. Verificar desafíos activos relacionados con ahorro
        val completedChallenges = mutableListOf<SavingChallenge>()
        val activeChallenges = savingChallengeRepository.activeChallenges.value ?: emptyList()

        for (challenge in activeChallenges) {
            // Actualizar progreso del desafío
            savingChallengeRepository.updateChallengeProgress(challenge.id, amount)

            // Verificar si se completó
            val updatedChallenge = savingChallengeRepository.getChallengeById(challenge.id)
            if (updatedChallenge != null && updatedChallenge.isCompleted) {
                completedChallenges.add(updatedChallenge)

                // Recompensar al usuario
                userLevelRepository.addExperience(updatedChallenge.rewardPoints)
                userLevelRepository.addRankPoints(updatedChallenge.rewardPoints / 10)
            }
        }

        // 5. Verificar logros
        val unlockedAchievements = mutableListOf<Achievement>()

        // Verificar logros de ahorro
        achievementRepository.checkAndUpdateCategoryAchievements(
            AchievementCategory.SAVINGS,
            amount.toInt()
        )

        // Verificar logros de racha
        if (updatedStreak.currentStreak > 0) {
            achievementRepository.checkAndUpdateCategoryAchievements(
                AchievementCategory.STREAKS,
                updatedStreak.currentStreak
            )
        }

        // Obtener logros desbloqueados recientemente
        val achievements = achievementRepository.allAchievements.value ?: emptyList()
        for (achievement in achievements) {
            if (achievement.isUnlocked && achievement.unlockedDate != null) {
                // Verificar si se desbloqueó recientemente (últimos 5 segundos)
                val fiveSecondsAgo = Date(System.currentTimeMillis() - 5000)
                if (achievement.unlockedDate.after(fiveSecondsAgo)) {
                    unlockedAchievements.add(achievement)

                    // Recompensar al usuario
                    userLevelRepository.addExperience(achievement.pointsReward)
                    userLevelRepository.addRankPoints(achievement.pointsReward / 5)
                }
            }
        }

        // 6. Crear resultado de gamificación
        return GamificationResult(
            xpEarned = baseXP,
            streakDays = updatedStreak.currentStreak,
            leveledUp = leveledUp,
            currentLevel = userLevelRepository.getCurrentLevel(),
            completedChallenges = completedChallenges,
            unlockedAchievements = unlockedAchievements,
            streakBonus = streakBonus
        )
    }

    /**
     * Activa un desafío para el usuario.
     */
    suspend fun activateChallenge(challengeId: Long): SavingChallenge? {
        val challenge = savingChallengeRepository.getChallengeById(challengeId) ?: return null

        // Verificar límite de desafíos activos (máximo 3 simultáneos)
        val activeCount = savingChallengeRepository.getActiveChallengesTotalCount()
        if (activeCount >= 3) {
            return null
        }

        // Activar el desafío
        return savingChallengeRepository.activateChallenge(challenge, challenge.duration)
    }

    /**
     * Verifica si se han perdido días en la racha de ahorro (por ejemplo, si la app no se abrió).
     */
    private suspend fun checkMissedDays() {
        savingStreakRepository.checkAndResetStreak()
    }

    /**
     * Reproduce un efecto de sonido para celebraciones.
     */
    fun playCelebrationSound() {
        if (!soundEffects) return

        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.achievement_unlocked)
            mediaPlayer?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            // Ignorar errores de sonido
        }
    }

    /**
     * Crea una vibración para efectos táctiles.
     */
    fun vibrate(pattern: LongArray = longArrayOf(0, 100, 50, 100)) {
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, -1)
            }
        }
    }

    /**
     * Obtiene nuevos desafíos disponibles para el usuario.
     */
    fun getAvailableChallenges(limit: Int = 5) = savingChallengeRepository.getAvailableChallenges(limit)

    /**
     * Clase que encapsula los resultados de una acción de gamificación.
     */
    data class GamificationResult(
        val xpEarned: Int = 0,
        val streakDays: Int = 0,
        val leveledUp: Boolean = false,
        val currentLevel: Int = 1,
        val completedChallenges: List<SavingChallenge> = emptyList(),
        val unlockedAchievements: List<Achievement> = emptyList(),
        val streakBonus: Double = 1.0
    ) {
        val hasCelebrations: Boolean
            get() = leveledUp || completedChallenges.isNotEmpty() || unlockedAchievements.isNotEmpty()
    }

    /**
     * Habilitar o deshabilitar efectos de sonido.
     */
    fun setSoundEffects(enabled: Boolean) {
        soundEffects = enabled
    }

    /**
     * Verifica y devuelve si el usuario ya ha ahorrado hoy.
     */
    suspend fun hasSavedToday(): Boolean {
        return savingStreakRepository.isActiveToday()
    }

    /**
     * Obtiene estadísticas consolidadas del usuario para mostrar en paneles o perfiles.
     */
    suspend fun getUserStats(): UserStats {
        val level = userLevelRepository.userLevel.value ?: UserLevel()
        val streak = savingStreakRepository.savingStreak.value ?: SavingStreak()

        return UserStats(
            level = level.level,
            currentXP = level.currentXP,
            xpToNextLevel = level.xpToNextLevel,
            savingRank = level.savingRank,
            currentStreak = streak.currentStreak,
            longestStreak = streak.longestStreak,
            totalSavingDays = streak.totalSavingDays,
            completedChallenges = savingChallengeRepository.completedChallengesCount.value ?: 0,
            unlockedAchievements = achievementRepository.unlockedCount.value ?: 0,
            totalAchievements = achievementRepository.totalCount.value ?: 0
        )
    }

    /**
     * Clase que encapsula las estadísticas del usuario.
     */
    data class UserStats(
        val level: Int,
        val currentXP: Int,
        val xpToNextLevel: Int,
        val savingRank: SavingRank,
        val currentStreak: Int,
        val longestStreak: Int,
        val totalSavingDays: Int,
        val completedChallenges: Int,
        val unlockedAchievements: Int,
        val totalAchievements: Int,
    ) {
        /**
         * Calcula el progreso del nivel actual (0-100%).
         */
        val levelProgress: Int
            get() = if (xpToNextLevel > 0) (currentXP * 100 / xpToNextLevel) else 100
    }
}
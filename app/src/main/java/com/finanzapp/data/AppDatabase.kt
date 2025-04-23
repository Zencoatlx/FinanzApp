package com.finanzapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.finanzapp.data.converters.Converters
import com.finanzapp.data.dao.*
import com.finanzapp.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaction::class,
        SavingGoal::class,
        Category::class,
        Budget::class,
        SavingChallenge::class,
        Achievement::class,
        SavingStreak::class,
        UserLevel::class
    ],
    version = 2, // Incrementamos la versión de la BD
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    // Nuevos DAOs para la gamificación
    abstract fun savingChallengeDao(): SavingChallengeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun savingStreakDao(): SavingStreakDao
    abstract fun userLevelDao(): UserLevelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanzapp_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback para inicializar la base de datos con datos predeterminados
         * cuando se crea por primera vez.
         */
        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Inicializar datos cuando se crea la BD
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // Inicializar valores default para gamificación
                        initializeGamificationData(database)
                    }
                }
            }

            private suspend fun initializeGamificationData(database: AppDatabase) {
                // 1. Inicializar UserLevel (nivel 1)
                val userLevelDao = database.userLevelDao()
                userLevelDao.insert(UserLevel(id = 1))

                // 2. Inicializar SavingStreak
                val savingStreakDao = database.savingStreakDao()
                savingStreakDao.insert(SavingStreak(id = 1))

                // 3. Inicializar logros básicos
                val achievementDao = database.achievementDao()

                // Algunos logros básicos para empezar
                val basicAchievements = listOf(
                    Achievement(
                        title = "Primer Paso",
                        description = "Crea tu primera meta de ahorro",
                        category = AchievementCategory.SAVINGS,
                        tier = AchievementTier.BRONZE,
                        pointsReward = 10,
                        iconName = "ic_achievement_first_goal",
                        targetProgress = 1,
                        conditions = "Crear una meta de ahorro"
                    ),
                    Achievement(
                        title = "Comienza la Racha",
                        description = "Ahorra 3 días consecutivos",
                        category = AchievementCategory.STREAKS,
                        tier = AchievementTier.BRONZE,
                        pointsReward = 20,
                        iconName = "ic_achievement_streak",
                        targetProgress = 3,
                        conditions = "Mantener una racha de 3 días"
                    ),
                    Achievement(
                        title = "Ahorro Constante",
                        description = "Alcanza una racha de 7 días consecutivos",
                        category = AchievementCategory.STREAKS,
                        tier = AchievementTier.SILVER,
                        pointsReward = 50,
                        iconName = "ic_achievement_streak_7",
                        targetProgress = 7,
                        conditions = "Mantener una racha de 7 días"
                    ),
                    Achievement(
                        title = "Maestro del Presupuesto",
                        description = "Mantente dentro del presupuesto por un mes completo",
                        category = AchievementCategory.BUDGET,
                        tier = AchievementTier.GOLD,
                        pointsReward = 100,
                        iconName = "ic_achievement_budget_master",
                        targetProgress = 30,
                        conditions = "No exceder ningún presupuesto durante 30 días"
                    )
                )

                basicAchievements.forEach { achievement ->
                    achievementDao.insert(achievement)
                }

                // 4. Inicializar desafíos iniciales
                val challengeDao = database.savingChallengeDao()

                // Algunos desafíos predeterminados
                val initialChallenges = listOf(
                    SavingChallenge(
                        title = "Ahorro Diario",
                        description = "Ahorra una pequeña cantidad cada día durante una semana",
                        targetAmount = 50.0,
                        rewardPoints = 30,
                        difficulty = ChallengeDifficulty.BEGINNER,
                        type = ChallengeType.DAILY,
                        duration = 7,
                        iconName = "ic_challenge_daily"
                    ),
                    SavingChallenge(
                        title = "Sin Gastos Innecesarios",
                        description = "No gastes en categorías no esenciales durante 3 días",
                        targetAmount = 0.0,
                        rewardPoints = 40,
                        difficulty = ChallengeDifficulty.EASY,
                        type = ChallengeType.NO_SPEND,
                        duration = 3,
                        iconName = "ic_challenge_no_spend"
                    ),
                    SavingChallenge(
                        title = "Ahorro del 20%",
                        description = "Ahorra el 20% de tus ingresos esta semana",
                        targetAmount = 0.0, // Se calculará según los ingresos
                        rewardPoints = 75,
                        difficulty = ChallengeDifficulty.MEDIUM,
                        type = ChallengeType.PERCENTAGE,
                        duration = 7,
                        iconName = "ic_challenge_percentage"
                    )
                )

                initialChallenges.forEach { challenge ->
                    challengeDao.insert(challenge)
                }
            }
        }
    }
}
package com.finanzapp.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.data.entity.Achievement
import com.finanzapp.data.entity.SavingChallenge
import com.finanzapp.data.entity.SavingRank
import com.finanzapp.service.SavingGamificationService
import com.finanzapp.ui.adapters.AchievementAdapter
import com.finanzapp.ui.adapters.ActiveChallengeAdapter
import com.finanzapp.ui.adapters.AvailableChallengeAdapter
import com.finanzapp.ui.viewmodel.SavingGamificationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavingsChallengesFragment : Fragment() {

    private lateinit var viewModel: SavingGamificationViewModel

    // Vistas
    private lateinit var heroLevelText: TextView
    private lateinit var heroRankText: TextView
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var xpProgressText: TextView
    private lateinit var currentStreakText: TextView
    private lateinit var longestStreakText: TextView
    private lateinit var achievementsCountText: TextView
    private lateinit var addSavingGoalButton: Button
    private lateinit var viewAllAchievementsText: TextView

    // Adaptadores
    private lateinit var activeChallengesAdapter: ActiveChallengeAdapter
    private lateinit var availableChallengesAdapter: AvailableChallengeAdapter
    private lateinit var achievementsAdapter: AchievementAdapter

    // Texto "no hay datos"
    private lateinit var noActiveChallengesText: TextView
    private lateinit var noAvailableChallengesText: TextView
    private lateinit var noAchievementsText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_savings_challenges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(requireActivity())[SavingGamificationViewModel::class.java]

        // Inicializar vistas
        initializeViews(view)

        // Configurar RecyclerViews
        setupRecyclerViews(view)

        // Observar cambios en datos
        observeData()

        // Configurar clics
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        // Hero card
        heroLevelText = view.findViewById(R.id.textHeroLevel)
        heroRankText = view.findViewById(R.id.textHeroRank)
        xpProgressBar = view.findViewById(R.id.progressXP)
        xpProgressText = view.findViewById(R.id.textXPProgress)
        currentStreakText = view.findViewById(R.id.textCurrentStreak)
        longestStreakText = view.findViewById(R.id.textLongestStreak)
        achievementsCountText = view.findViewById(R.id.textAchievements)

        // Botones y textos de acción
        addSavingGoalButton = view.findViewById(R.id.buttonAddSavingGoal)
        viewAllAchievementsText = view.findViewById(R.id.textViewAllAchievements)

        // Textos "no hay datos"
        noActiveChallengesText = view.findViewById(R.id.textNoActiveChallenges)
        noAvailableChallengesText = view.findViewById(R.id.textNoAvailableChallenges)
        noAchievementsText = view.findViewById(R.id.textNoAchievements)
    }

    private fun setupRecyclerViews(view: View) {
        // Adaptador para desafíos activos
        val recyclerActiveChallenges = view.findViewById<RecyclerView>(R.id.recyclerActiveChallenges)
        activeChallengesAdapter = ActiveChallengeAdapter { challenge ->
            showAbandonChallengeDialog(challenge)
        }
        recyclerActiveChallenges.layoutManager = LinearLayoutManager(requireContext())
        recyclerActiveChallenges.adapter = activeChallengesAdapter

        // Adaptador para desafíos disponibles
        val recyclerAvailableChallenges = view.findViewById<RecyclerView>(R.id.recyclerAvailableChallenges)
        availableChallengesAdapter = AvailableChallengeAdapter { challenge ->
            activateChallenge(challenge)
        }
        recyclerAvailableChallenges.layoutManager = LinearLayoutManager(requireContext())
        recyclerAvailableChallenges.adapter = availableChallengesAdapter

        // Adaptador para logros
        val recyclerAchievements = view.findViewById<RecyclerView>(R.id.recyclerAchievements)
        achievementsAdapter = AchievementAdapter { achievement ->
            showAchievementDetails(achievement)
        }
        recyclerAchievements.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerAchievements.adapter = achievementsAdapter
    }

    private fun observeData() {
        // Observar nivel del usuario
        viewModel.userLevel.observe(viewLifecycleOwner) { userLevel ->
            if (userLevel != null) {
                // Actualizar nivel y XP
                heroLevelText.text = "Nivel ${userLevel.level}"

                // Actualizar texto de rango
                val rankName = viewModel.getRankDisplayName(userLevel.savingRank)
                heroRankText.text = rankName

                // Cambiar color del badge de rango según el rango
                val rankColor = viewModel.getRankColor(userLevel.savingRank)
                heroRankText.setBackgroundResource(R.drawable.rank_badge_background)
                heroRankText.background.setTint(resources.getColor(rankColor, null))

                // Actualizar barra de progreso XP
                xpProgressBar.max = userLevel.xpToNextLevel
                xpProgressBar.progress = userLevel.currentXP
                xpProgressText.text = "${userLevel.currentXP}/${userLevel.xpToNextLevel} XP"

                // Si hay un avatar hero, actualizarlo según el rango
                val heroAvatar = view?.findViewById<ImageView>(R.id.imageHeroAvatar)
                heroAvatar?.let {
                    when (userLevel.savingRank) {
                        SavingRank.NOVICE -> it.setImageResource(R.drawable.ic_savings)
                        SavingRank.SAVER -> it.setImageResource(R.drawable.ic_savings)
                        SavingRank.MONEY_MASTER -> it.setImageResource(R.drawable.ic_savings)
                        SavingRank.BUDGET_NINJA -> it.setImageResource(R.drawable.ic_savings)
                        SavingRank.WEALTH_WARRIOR -> it.setImageResource(R.drawable.ic_savings)
                        SavingRank.FINANCE_LEGEND -> it.setImageResource(R.drawable.ic_savings)
                        SavingRank.ECONOMY_TITAN -> it.setImageResource(R.drawable.ic_savings)
                        SavingRank.SAVINGS_SUPERHERO -> it.setImageResource(R.drawable.ic_savings)
                    }
                }
            }
        }

        // Observar rachas de ahorro
        viewModel.savingStreak.observe(viewLifecycleOwner) { streak ->
            if (streak != null) {
                currentStreakText.text = streak.currentStreak.toString()
                longestStreakText.text = streak.longestStreak.toString()
            }
        }

        // Observar desafíos activos
        viewModel.activeChallenges.observe(viewLifecycleOwner) { challenges ->
            activeChallengesAdapter.submitList(challenges)
            noActiveChallengesText.visibility = if (challenges.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        // Observar desafíos disponibles
        viewModel.availableChallenges.observe(viewLifecycleOwner) { challenges ->
            availableChallengesAdapter.submitList(challenges)
            noAvailableChallengesText.visibility = if (challenges.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        // Observar logros
        viewModel.unlockedAchievements.observe(viewLifecycleOwner) { achievements ->
            // Mostrar solo los primeros 5 logros desbloqueados para la vista horizontal
            val topAchievements = achievements.take(5)
            achievementsAdapter.submitList(topAchievements)
            noAchievementsText.visibility = if (achievements.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        // Observar contadores para logros
        viewModel.unlockedAchievementsCount.observe(viewLifecycleOwner) { unlockedCount ->
            viewModel.totalAchievementsCount.observe(viewLifecycleOwner) { totalCount ->
                achievementsCountText.text = "$unlockedCount/$totalCount"
            }
        }

        // Observar resultados de gamificación (para mostrar diálogos de celebración)
        viewModel.lastGamificationResult.observe(viewLifecycleOwner) { result ->
            if (result != null && result.hasCelebrations) {
                if (result.leveledUp) {
                    showLevelUpDialog(result.currentLevel)
                }

                result.completedChallenges.forEach { challenge ->
                    showCompletedChallengeDialog(challenge)
                }

                result.unlockedAchievements.forEach { achievement ->
                    showUnlockedAchievementDialog(achievement)
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Botón para crear nueva meta de ahorro
        addSavingGoalButton.setOnClickListener {
            findNavController().navigate(R.id.addSavingGoalFragment)
        }

        // Texto para ver todos los logros
        viewAllAchievementsText.setOnClickListener {
            // Navegar a la pantalla de todos los logros
            // (Se implementaría en otra iteración)
            Toast.makeText(requireContext(), "Ver todos los logros", Toast.LENGTH_SHORT).show()
        }
    }

    private fun activateChallenge(challenge: SavingChallenge) {
        CoroutineScope(Dispatchers.Main).launch {
            // Mostrar carga
            val loadingToast = Toast.makeText(requireContext(), "Activando desafío...", Toast.LENGTH_SHORT)
            loadingToast.show()

            // Intentar activar el desafío
            val activated = withContext(Dispatchers.IO) {
                viewModel.activateChallenge(challenge.id)
                true // Simplificado para el ejemplo
            }

            // Ocultar carga
            loadingToast.cancel()

            // Mostrar resultado
            if (activated) {
                Toast.makeText(
                    requireContext(),
                    "¡Desafío activado! ¡Buena suerte!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "No se pudo activar el desafío. Ya tienes el máximo de desafíos activos.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showAbandonChallengeDialog(challenge: SavingChallenge) {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Abandonar desafío?")
            .setMessage("¿Estás seguro de que quieres abandonar el desafío \"${challenge.title}\"? Perderás todo el progreso.")
            .setPositiveButton("Abandonar") { _, _ ->
                // Abandonar desafío
                CoroutineScope(Dispatchers.IO).launch {
                    // Lógica para abandonar desafío
                    // viewModel.abandonChallenge(challenge.id)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Desafío abandonado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAchievementDetails(achievement: Achievement) {
        AlertDialog.Builder(requireContext())
            .setTitle(achievement.title)
            .setMessage("""
                ${achievement.description}
                
                Recompensa: ${achievement.pointsReward} XP
                
                ${if (achievement.isUnlocked) "¡Logro desbloqueado!" else "Condición: ${achievement.conditions}"}
            """.trimIndent())
            .setPositiveButton("Genial", null)
            .show()
    }

    private fun showLevelUpDialog(newLevel: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_level_up, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Configurar vistas del diálogo
        val newLevelText = dialogView.findViewById<TextView>(R.id.textNewLevel)
        newLevelText.text = "¡Ahora eres nivel $newLevel!"

        // Configurar botón para cerrar
        val continueButton = dialogView.findViewById<Button>(R.id.buttonContinue)
        continueButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCompletedChallengeDialog(challenge: SavingChallenge) {
        AlertDialog.Builder(requireContext())
            .setTitle("¡Desafío completado!")
            .setMessage("""
                Has completado el desafío "${challenge.title}"
                
                Recompensa obtenida: ${challenge.rewardPoints} XP
                
                ¡Sigue así para desbloquear más logros y subir de nivel!
            """.trimIndent())
            .setPositiveButton("¡Genial!", null)
            .show()
    }

    private fun showUnlockedAchievementDialog(achievement: Achievement) {
        AlertDialog.Builder(requireContext())
            .setTitle("¡Nuevo logro desbloqueado!")
            .setMessage("""
                "${achievement.title}"
                
                ${achievement.description}
                
                Recompensa: ${achievement.pointsReward} XP
            """.trimIndent())
            .setPositiveButton("¡Genial!", null)
            .show()
    }
}
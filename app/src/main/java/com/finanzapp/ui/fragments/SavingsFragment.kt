package com.finanzapp.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.service.SavingGamificationService
import com.finanzapp.ui.adapters.SavingGoalFullAdapter
import com.finanzapp.ui.viewmodel.SavingGamificationViewModel
import com.finanzapp.ui.viewmodel.SavingGoalViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class SavingsFragment : Fragment() {

    private lateinit var viewModel: SavingGoalViewModel
    private lateinit var gamificationViewModel: SavingGamificationViewModel
    private lateinit var adapter: SavingGoalFullAdapter

    // Vistas para la gamificación
    private lateinit var finanzHeroButton: MaterialButton
    private lateinit var cardStreak: CardView
    private lateinit var textStreakDays: TextView
    private lateinit var buttonSaveToday: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usar el nuevo layout actualizado
        return inflater.inflate(R.layout.fragment_savings_updated, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModels
        viewModel = ViewModelProvider(requireActivity())[SavingGoalViewModel::class.java]
        gamificationViewModel = ViewModelProvider(requireActivity())[SavingGamificationViewModel::class.java]

        // Inicializar elementos de gamificación
        finanzHeroButton = view.findViewById(R.id.buttonFinanzHero)
        cardStreak = view.findViewById(R.id.cardStreak)
        textStreakDays = view.findViewById(R.id.textStreakDays)
        buttonSaveToday = view.findViewById(R.id.buttonSaveToday)

        // Configurar RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerSavingGoals)
        adapter = SavingGoalFullAdapter { goalId ->
            showAddContributionDialog(goalId)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observar los datos
        viewModel.allSavingGoals.observe(viewLifecycleOwner) { goals ->
            adapter.submitList(goals)
        }

        // Configurar FAB
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddSavingGoal)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_savingsFragment_to_addSavingGoalFragment)
        }

        // Configurar botón de Finanz Hero
        finanzHeroButton.setOnClickListener {
            findNavController().navigate(R.id.action_savingsFragment_to_savingsChallengesFragment)
        }

        // Configurar botón de ahorrar hoy
        buttonSaveToday.setOnClickListener {
            showQuickSavingDialog()
        }

        // Verificar si el usuario ha ahorrado hoy para mostrar/ocultar banner de racha
        checkStreak()
    }

    override fun onResume() {
        super.onResume()
        // Verificar nuevamente la racha cada vez que se muestra el fragmento
        checkStreak()
    }

    /**
     * Verifica el estado actual de la racha y actualiza la UI acorde
     */
    private fun checkStreak() {
        CoroutineScope(Dispatchers.Main).launch {
            // Verificar si ya ahorró hoy
            gamificationViewModel.hasSavedToday { savedToday ->
                // Si ya ahorró hoy, ocultar el botón de "Ahorrar hoy"
                buttonSaveToday.visibility = if (savedToday) View.GONE else View.VISIBLE
            }

            // Obtener datos de racha actual
            val streak = gamificationViewModel.savingStreak.value

            if (streak != null && streak.currentStreak > 0) {
                // Mostrar banner de racha
                cardStreak.visibility = View.VISIBLE

                // Actualizar texto con días de racha
                val streakText = when (streak.currentStreak) {
                    1 -> "¡Comenzaste tu racha! Continúa ahorrando mañana."
                    else -> "Llevas ${streak.currentStreak} días ahorrando consecutivamente"
                }
                textStreakDays.text = streakText
            } else {
                // Ocultar banner si no hay racha activa
                cardStreak.visibility = View.GONE
            }
        }
    }

    /**
     * Muestra un diálogo para agregar una contribución a una meta de ahorro
     */
    private fun showAddContributionDialog(goalId: Long) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_contribution, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Añadir Ahorro")
            .setView(dialogView)
            .setPositiveButton("Guardar", null) // Se sobrescribirá después
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            // Obtener vistas del diálogo
            val amountInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextAmount)
            val dateInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextDate)

            // Establecer fecha actual formateada
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateInput.setText(dateFormat.format(Date()))

            // Configurar botón de guardar para evitar que se cierre automáticamente en caso de error
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val amountText = amountInput.text.toString()

                if (amountText.isBlank()) {
                    amountInput.error = "Ingresa una cantidad"
                    return@setOnClickListener
                }

                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    amountInput.error = "Ingresa una cantidad válida"
                    return@setOnClickListener
                }

                // Agregar contribución
                addContribution(goalId, amount)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    /**
     * Muestra un diálogo rápido para ahorrar una cantidad pequeña y mantener la racha
     */
    private fun showQuickSavingDialog() {
        // Obtener todas las metas de ahorro
        val goals = viewModel.allSavingGoals.value

        if (goals.isNullOrEmpty()) {
            // Si no hay metas, mostrar mensaje y sugerir crear una
            AlertDialog.Builder(requireContext())
                .setTitle("No hay metas de ahorro")
                .setMessage("Necesitas crear al menos una meta de ahorro para poder registrar ahorros.")
                .setPositiveButton("Crear meta") { _, _ ->
                    findNavController().navigate(R.id.action_savingsFragment_to_addSavingGoalFragment)
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }

        // Crear lista de opciones con las metas
        val goalNames = goals.map { it.name }.toTypedArray()
        val goalIds = goals.map { it.id }.toLongArray()

        // Mostrar diálogo para seleccionar meta
        AlertDialog.Builder(requireContext())
            .setTitle("¿Dónde quieres ahorrar hoy?")
            .setItems(goalNames) { _, which ->
                // Al seleccionar una meta, mostrar diálogo para ingresar cantidad
                showQuickAmountDialog(goalIds[which])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra un diálogo para ingresar rápidamente una cantidad para ahorrar
     */
    private fun showQuickAmountDialog(goalId: Long) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_quick_save, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Ahorro rápido")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            // Configurar botones de cantidad rápida
            val buttonSmall = dialogView.findViewById<Button>(R.id.buttonSmallAmount)
            val buttonMedium = dialogView.findViewById<Button>(R.id.buttonMediumAmount)
            val buttonLarge = dialogView.findViewById<Button>(R.id.buttonLargeAmount)
            val buttonCustom = dialogView.findViewById<Button>(R.id.buttonCustomAmount)

            // Generar cantidades aleatorias para hacer más interesante la experiencia
            val random = Random()
            val smallAmount = (random.nextInt(15) + 5) * 10.0 // 50-200
            val mediumAmount = (random.nextInt(10) + 10) * 25.0 // 250-500
            val largeAmount = (random.nextInt(10) + 20) * 50.0 // 1000-1500

            // Establecer textos en los botones
            buttonSmall.text = "$${smallAmount.toInt()}"
            buttonMedium.text = "$${mediumAmount.toInt()}"
            buttonLarge.text = "$${largeAmount.toInt()}"

            // Configurar clics en botones
            buttonSmall.setOnClickListener {
                addContribution(goalId, smallAmount)
                dialog.dismiss()
            }

            buttonMedium.setOnClickListener {
                addContribution(goalId, mediumAmount)
                dialog.dismiss()
            }

            buttonLarge.setOnClickListener {
                addContribution(goalId, largeAmount)
                dialog.dismiss()
            }

            buttonCustom.setOnClickListener {
                dialog.dismiss()
                showAddContributionDialog(goalId)
            }
        }

        dialog.show()
    }

    /**
     * Agrega una contribución a una meta de ahorro y procesa la gamificación
     */
    private fun addContribution(goalId: Long, amount: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1. Actualizar la meta de ahorro con la nueva contribución
                val goal = withContext(Dispatchers.IO) {
                    viewModel.getSavingGoalById(goalId)
                }

                if (goal != null) {
                    val updatedGoal = goal.copy(
                        currentAmount = goal.currentAmount + amount
                    )

                    withContext(Dispatchers.IO) {
                        viewModel.update(updatedGoal)
                    }

                    // 2. Procesar la gamificación
                    gamificationViewModel.registerSavingContribution(goalId, amount)

                    // 3. Mostrar mensaje de éxito
                    val message = "¡Has añadido $${amount.toInt()} a tu meta \"${goal.name}\"!"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    // 4. Actualizar estado de racha
                    checkStreak()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al guardar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
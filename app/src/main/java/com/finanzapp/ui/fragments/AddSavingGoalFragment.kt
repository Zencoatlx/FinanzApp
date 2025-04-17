package com.finanzapp.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.finanzapp.R
import com.finanzapp.data.entity.SavingGoal
import com.finanzapp.ui.viewmodel.SavingGoalViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddSavingGoalFragment : Fragment() {

    private lateinit var viewModel: SavingGoalViewModel
    private lateinit var editTextName: EditText
    private lateinit var editTextAmount: EditText
    private lateinit var editTextInitialAmount: EditText
    private lateinit var editTextDeadline: EditText
    private lateinit var buttonSave: Button

    private var deadlineDate: Date? = null
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_saving_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SavingGoalViewModel::class.java]

        // Inicializar vistas
        editTextName = view.findViewById(R.id.editTextName)
        editTextAmount = view.findViewById(R.id.editTextAmount)
        editTextInitialAmount = view.findViewById(R.id.editTextInitialAmount)
        editTextDeadline = view.findViewById(R.id.editTextDeadline)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Configurar selector de fecha
        editTextDeadline.setOnClickListener {
            showDatePicker()
        }

        // Configurar botón de guardar
        buttonSave.setOnClickListener {
            saveSavingGoal()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                deadlineDate = calendar.time
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
        editTextDeadline.setText(dateFormat.format(calendar.time))
    }

    private fun saveSavingGoal() {
        val name = editTextName.text.toString()
        val amountText = editTextAmount.text.toString()
        val initialAmountText = editTextInitialAmount.text.toString()

        // Validar campos
        if (name.isBlank() || amountText.isBlank()) {
            // TODO: Mostrar error
            return
        }

        val targetAmount = amountText.toDoubleOrNull() ?: return
        val initialAmount = initialAmountText.toDoubleOrNull() ?: 0.0

        val savingGoal = SavingGoal(
            name = name,
            targetAmount = targetAmount,
            currentAmount = initialAmount,
            deadline = deadlineDate,
            createdAt = Date()
        )

        viewModel.insert(savingGoal)
        findNavController().popBackStack()
    }
}
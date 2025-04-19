package com.finanzapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.finanzapp.R
import com.finanzapp.data.entity.Budget
import com.finanzapp.data.entity.Category
import com.finanzapp.data.entity.TransactionType
import com.finanzapp.ui.viewmodel.BudgetViewModel
import com.finanzapp.ui.viewmodel.CategoryViewModel
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddBudgetFragment : Fragment() {

    private lateinit var viewModel: BudgetViewModel
    private lateinit var categoryViewModel: CategoryViewModel

    private lateinit var editTextAmount: EditText
    private lateinit var dropdownCategory: AutoCompleteTextView
    private lateinit var sliderMonths: Slider
    private lateinit var textMonthInfo: TextView
    private lateinit var buttonSave: Button

    // Usar Safe Args para recibir argumentos
    private val args: AddBudgetFragmentArgs by navArgs()

    private var categories: List<Category> = emptyList()
    private var selectedMonth = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModels
        viewModel = ViewModelProvider(requireActivity())[BudgetViewModel::class.java]
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]

        // Inicializar vistas
        editTextAmount = view.findViewById(R.id.editTextAmount)
        dropdownCategory = view.findViewById(R.id.dropdownCategory)
        sliderMonths = view.findViewById(R.id.sliderMonths)
        textMonthInfo = view.findViewById(R.id.textMonthInfo)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Cargar categorías de gastos
        categoryViewModel.getCategoriesByType(TransactionType.EXPENSE).observe(viewLifecycleOwner) { expenseCategories ->
            categories = expenseCategories
            val categoryNames = expenseCategories.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
            dropdownCategory.setAdapter(adapter)

            // Si hay categorías, seleccionar la primera por defecto
            if (categoryNames.isNotEmpty()) {
                dropdownCategory.setText(categoryNames[0], false)
            }
        }

        // Configurar slider de meses
        sliderMonths.addOnChangeListener { _, value, _ ->
            val months = value.toInt()
            updateMonthText(months)
        }

        // Valor inicial del slider
        sliderMonths.value = 1f
        updateMonthText(1)

        // Si es edición, cargar datos existentes
        if (args.budgetId != -1L) {
            lifecycleScope.launch {
                val budget = viewModel.getBudgetById(args.budgetId)
                if (budget != null) {
                    // Rellenar campos con datos existentes
                    editTextAmount.setText(budget.amount.toString())
                    dropdownCategory.setText(budget.category, false)

                    // Establecer mes seleccionado
                    val cal = Calendar.getInstance()
                    cal.time = budget.date
                    selectedMonth = cal

                    // Establecer duración en meses
                    sliderMonths.value = budget.months.toFloat()
                    updateMonthText(budget.months)
                }
            }
        }

        // Configurar botón guardar
        buttonSave.setOnClickListener {
            saveAndReturn()
        }
    }

    private fun updateMonthText(months: Int) {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
        val calendar = Calendar.getInstance()
        calendar.time = selectedMonth.time

        val endCalendar = Calendar.getInstance()
        endCalendar.time = selectedMonth.time
        endCalendar.add(Calendar.MONTH, months - 1)

        val text = if (months > 1) {
            "${dateFormat.format(calendar.time)} - ${dateFormat.format(endCalendar.time)}"
        } else {
            dateFormat.format(calendar.time)
        }

        textMonthInfo.text = text
    }

    private fun saveAndReturn() {
        val amount = editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
        val category = dropdownCategory.text.toString()
        val months = sliderMonths.value.toInt()

        if (amount <= 0) {
            editTextAmount.error = "Ingrese un monto válido"
            return
        }

        if (category.isBlank()) {
            dropdownCategory.error = "Seleccione una categoría"
            return
        }

        val budget = if (args.budgetId != -1L) {
            // Actualizar presupuesto existente
            lifecycleScope.launch {
                val existingBudget = viewModel.getBudgetById(args.budgetId)
                if (existingBudget != null) {
                    val updatedBudget = existingBudget.copy(
                        amount = amount,
                        category = category,
                        date = selectedMonth.time,
                        months = months
                    )
                    viewModel.update(updatedBudget)
                    findNavController().popBackStack()
                }
            }
            return
        } else {
            // Crear nuevo presupuesto
            Budget(
                amount = amount,
                category = category,
                date = selectedMonth.time,
                months = months,
                createdAt = Date()
            )
        }

        viewModel.insert(budget)
        findNavController().popBackStack()
    }
}
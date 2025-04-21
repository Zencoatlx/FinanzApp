package com.finanzapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.finanzapp.R
import com.finanzapp.data.entity.Budget
import com.finanzapp.ui.viewmodel.BudgetViewModel
import java.util.Date

class AddBudgetFragment : Fragment() {

    private lateinit var viewModel: BudgetViewModel
    private lateinit var editTextName: EditText
    private lateinit var editTextAmount: EditText
    private lateinit var dropdownCategory: AutoCompleteTextView
    private lateinit var buttonSave: Button

    // En lugar de usar navArgs, accedemos directamente a los argumentos
    private var budgetId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[BudgetViewModel::class.java]

        // Obtenemos el ID del presupuesto de los argumentos
        arguments?.let {
            budgetId = it.getLong("budgetId", -1L)
        }

        // Inicializar vistas
        editTextName = view.findViewById(R.id.editTextName)
        editTextAmount = view.findViewById(R.id.editTextAmount)
        dropdownCategory = view.findViewById(R.id.dropdownCategory)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Configurar categorías
        setupCategoryDropdown()

        // Si estamos editando un presupuesto existente
        if (budgetId != -1L) {
            loadBudget()
        }

        // Configurar botón guardar
        buttonSave.setOnClickListener {
            saveBudget()
        }
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf(
            "Comida", "Transporte", "Vivienda", "Entretenimiento",
            "Salud", "Educación", "Ropa", "Servicios", "Otro"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dropdownCategory.setAdapter(adapter)
        dropdownCategory.setText(categories[0], false)
    }

    private fun loadBudget() {
        viewModel.getBudgetById(budgetId).observe(viewLifecycleOwner) { budget ->
            if (budget != null) {
                editTextName.setText(budget.name)
                editTextAmount.setText(budget.amount.toString())
                dropdownCategory.setText(budget.category, false)

                // Cambiar el texto del botón
                buttonSave.text = "Actualizar"
            }
        }
    }

    private fun saveBudget() {
        val name = editTextName.text.toString()
        val amountText = editTextAmount.text.toString()
        val category = dropdownCategory.text.toString()

        // Validar campos
        if (name.isBlank() || amountText.isBlank() || category.isBlank()) {
            Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Por favor ingrese un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear o actualizar presupuesto
        if (budgetId != -1L) {
            // Actualizar presupuesto existente
            viewModel.getBudgetById(budgetId).observe(viewLifecycleOwner) { existingBudget ->
                if (existingBudget != null) {
                    val updatedBudget = Budget(
                        id = existingBudget.id,
                        name = name,
                        amount = amount,
                        category = category,
                        spent = existingBudget.spent,
                        createdAt = existingBudget.createdAt
                    )
                    viewModel.update(updatedBudget)
                    findNavController().popBackStack()
                }
            }
        } else {
            // Crear nuevo presupuesto
            val budget = Budget(
                name = name,
                amount = amount,
                category = category,
                spent = 0.0,
                createdAt = Date()
            )
            viewModel.insert(budget)
            findNavController().popBackStack()
        }
    }
}
package com.finanzapp.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.finanzapp.R
import com.finanzapp.data.entity.Transaction
import com.finanzapp.ui.viewmodel.TransactionViewModel
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionFragment : Fragment() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var editTextAmount: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var dropdownCategory: AutoCompleteTextView
    private lateinit var editTextDate: EditText
    private lateinit var checkBoxRecurring: CheckBox
    private lateinit var buttonSave: Button

    private var selectedDate = Calendar.getInstance()
    private var isIncome = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        // Inicializar vistas
        tabLayout = view.findViewById(R.id.tabLayout)
        editTextAmount = view.findViewById(R.id.editTextAmount)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        dropdownCategory = view.findViewById(R.id.dropdownCategory)
        editTextDate = view.findViewById(R.id.editTextDate)
        checkBoxRecurring = view.findViewById(R.id.checkBoxRecurring)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Configurar tabs para tipo de transacción
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isIncome = tab?.position == 1
                updateCategoryDropdown()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Configurar selector de fecha
        updateDateDisplay()
        editTextDate.setOnClickListener {
            showDatePicker()
        }

        // Inicializar categorías
        updateCategoryDropdown()

        // Configurar botón guardar
        buttonSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateCategoryDropdown() {
        val categories = if (isIncome) {
            arrayOf("Salario", "Inversión", "Regalo", "Venta", "Reembolso", "Otro")
        } else {
            arrayOf("Comida", "Transporte", "Vivienda", "Entretenimiento", "Salud", "Educación", "Ropa", "Servicios", "Otro")
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dropdownCategory.setAdapter(adapter)
        dropdownCategory.setText(categories[0], false)
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
        editTextDate.setText(dateFormat.format(selectedDate.time))
    }

    private fun saveTransaction() {
        val amountText = editTextAmount.text.toString()
        val description = editTextDescription.text.toString()
        val category = dropdownCategory.text.toString()

        // Validar campos
        if (amountText.isBlank() || description.isBlank() || category.isBlank()) {
            // TODO: Mostrar error
            return
        }

        val amount = amountText.toDoubleOrNull() ?: return

        val transaction = Transaction(
            amount = amount,
            description = description,
            category = category,
            isIncome = isIncome,
            date = selectedDate.time,
            isRecurring = checkBoxRecurring.isChecked
        )

        viewModel.insert(transaction)
        findNavController().popBackStack()
    }
}
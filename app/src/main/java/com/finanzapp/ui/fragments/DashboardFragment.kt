package com.finanzapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.ui.adapters.RecentTransactionAdapter
import com.finanzapp.ui.adapters.SavingGoalAdapter
import com.finanzapp.ui.viewmodel.TransactionViewModel
import com.finanzapp.ui.viewmodel.SavingGoalViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var savingGoalViewModel: SavingGoalViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModels
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        savingGoalViewModel = ViewModelProvider(requireActivity())[SavingGoalViewModel::class.java]

        // Referencias a vistas
        val txtCurrentBalance = view.findViewById<TextView>(R.id.textCurrentBalance)
        val txtIncome = view.findViewById<TextView>(R.id.textIncome)
        val txtExpenses = view.findViewById<TextView>(R.id.textExpenses)
        val recyclerTransactions = view.findViewById<RecyclerView>(R.id.recyclerRecentTransactions)
        val recyclerSavingGoals = view.findViewById<RecyclerView>(R.id.recyclerSavingGoals)
        val fabAddTransaction = view.findViewById<FloatingActionButton>(R.id.fabAddTransaction)
        val txtViewAllTransactions = view.findViewById<TextView>(R.id.textViewAllTransactions)
        val txtViewAllSavingGoals = view.findViewById<TextView>(R.id.textViewAllSavingGoals)

        // Configurar adaptadores
        val transactionAdapter = RecentTransactionAdapter()
        recyclerTransactions.layoutManager = LinearLayoutManager(requireContext())
        recyclerTransactions.adapter = transactionAdapter

        val savingGoalAdapter = SavingGoalAdapter()
        recyclerSavingGoals.layoutManager = LinearLayoutManager(requireContext())
        recyclerSavingGoals.adapter = savingGoalAdapter

        // Observar datos
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            // Mostrar solo las últimas 5 transacciones
            transactionAdapter.submitList(transactions.take(5))

            // Calcular balance, ingresos y gastos
            var totalIncome = 0.0
            var totalExpenses = 0.0

            transactions.forEach { transaction ->
                if (transaction.isIncome) {
                    totalIncome += transaction.amount
                } else {
                    totalExpenses += transaction.amount
                }
            }

            val balance = totalIncome - totalExpenses

            // Formatear valores como moneda
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            txtCurrentBalance.text = format.format(balance)
            txtIncome.text = format.format(totalIncome)
            txtExpenses.text = format.format(totalExpenses)
        }

        savingGoalViewModel.allSavingGoals.observe(viewLifecycleOwner) { goals ->
            // Mostrar solo las primeras 3 metas
            savingGoalAdapter.submitList(goals.take(3))
        }

        // Configurar clics
        fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addTransactionFragment)
        }

        txtViewAllTransactions.setOnClickListener {
            findNavController().navigate(R.id.transactionsFragment)
        }

        txtViewAllSavingGoals.setOnClickListener {
            findNavController().navigate(R.id.savingsFragment)
        }
    }
}
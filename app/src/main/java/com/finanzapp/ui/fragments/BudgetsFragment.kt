package com.finanzapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.ui.adapters.BudgetAdapter
import com.finanzapp.ui.viewmodel.BudgetViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BudgetsFragment : Fragment() {

    private lateinit var viewModel: BudgetViewModel
    private lateinit var adapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budgets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[BudgetViewModel::class.java]

        // Configurar RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerBudgets)
        adapter = BudgetAdapter { budget ->
            // Al hacer clic en un presupuesto, navegamos a la pantalla de edición
            val bundle = Bundle().apply {
                putLong("budgetId", budget.id)
            }
            findNavController().navigate(R.id.addBudgetFragment, bundle)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observar los datos
        viewModel.allBudgets.observe(viewLifecycleOwner) { budgets ->
            adapter.submitList(budgets)
        }

        // Configurar FAB
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddBudget)
        fab.setOnClickListener {
            findNavController().navigate(R.id.addBudgetFragment)
        }
    }
}
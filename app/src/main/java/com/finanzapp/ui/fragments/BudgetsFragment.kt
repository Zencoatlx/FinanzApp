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
import com.finanzapp.util.PremiumManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BudgetsFragment : Fragment() {

    private lateinit var viewModel: BudgetViewModel
    private lateinit var adapter: BudgetAdapter
    private lateinit var premiumManager: PremiumManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budgets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModels
        viewModel = ViewModelProvider(requireActivity())[BudgetViewModel::class.java]

        // Inicializar PremiumManager
        premiumManager = PremiumManager(requireContext())

        // Configurar RecyclerView
        val recyclerBudgets = view.findViewById<RecyclerView>(R.id.recyclerBudgets)
        adapter = BudgetAdapter { budget ->
            // Acción al hacer clic en un presupuesto
            val action =
                BudgetsFragmentDirections.actionBudgetsFragmentToAddBudgetFragment(budget.id)
            findNavController().navigate(action)
        }

        recyclerBudgets.layoutManager = LinearLayoutManager(requireContext())
        recyclerBudgets.adapter = adapter

        // Observar datos
        viewModel.allBudgets.observe(viewLifecycleOwner) { budgets ->
            adapter.submitList(budgets)
        }

        // Configurar FAB
        val fabAddBudget = view.findViewById<FloatingActionButton>(R.id.fabAddBudget)
        fabAddBudget.setOnClickListener {
            // Verificar si el usuario es premium para añadir más de 3 presupuestos
            premiumManager.isPremium.observe(viewLifecycleOwner) { isPremium ->
                if (viewModel.allBudgets.value?.size ?: 0 >= 3 && !isPremium) {
                    showPremiumDialog()
                } else {
                    val action =
                        BudgetsFragmentDirections.actionBudgetsFragmentToAddBudgetFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun showPremiumDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Límite de presupuestos alcanzado")
            .setMessage("La versión gratuita permite hasta 3 presupuestos. Actualiza a Premium para crear presupuestos ilimitados.")
            .setPositiveButton("Actualizar a Premium") { _, _ ->
                findNavController().navigate(R.id.action_budgetsFragment_to_premiumFragment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
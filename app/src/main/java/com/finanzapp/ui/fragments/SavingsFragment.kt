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
import com.finanzapp.ui.adapters.SavingGoalFullAdapter
import com.finanzapp.ui.viewmodel.SavingGoalViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SavingsFragment : Fragment() {

    private lateinit var viewModel: SavingGoalViewModel
    private lateinit var adapter: SavingGoalFullAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_savings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SavingGoalViewModel::class.java]

        // Configurar RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerSavingGoals)
        adapter = SavingGoalFullAdapter { goalId ->
            // TODO: Navegar a la pantalla de detalles de la meta
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
            findNavController().navigate(R.id.addSavingGoalFragment)
        }
    }
}
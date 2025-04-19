package com.finanzapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.data.entity.Budget
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BudgetAdapter(private val onItemClick: (Budget) -> Unit) :
    ListAdapter<Budget, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = getItem(position)
        holder.bind(budget)
    }

    class BudgetViewHolder(itemView: View, private val onItemClick: (Budget) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val textBudgetName: TextView = itemView.findViewById(R.id.textBudgetName)
        private val textBudgetPeriod: TextView = itemView.findViewById(R.id.textBudgetPeriod)
        private val progressBudget: ProgressBar = itemView.findViewById(R.id.progressBudget)
        private val textBudgetProgress: TextView = itemView.findViewById(R.id.textBudgetProgress)
        private val textBudgetAmount: TextView = itemView.findViewById(R.id.textBudgetAmount)

        fun bind(budget: Budget) {
            textBudgetName.text = budget.category

            // Formatear fecha
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
            textBudgetPeriod.text = dateFormat.format(budget.date)

            // Calcular porcentaje
            val spent = budget.spent ?: 0.0
            val progress = if (budget.amount > 0) {
                (spent / budget.amount * 100).toInt()
            } else {
                0
            }

            progressBudget.progress = progress

            // Formatear montos
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            textBudgetProgress.text = "${format.format(spent)} / ${format.format(budget.amount)}"

            // Aquí cambiamos la referencia al color a R.color que es lo correcto
            textBudgetAmount.text = "$progress%"
            textBudgetAmount.setTextColor(itemView.context.getColor(R.color.design_default_color_primary))

            // Configurar click
            itemView.setOnClickListener { onItemClick(budget) }
        }
    }

    class BudgetDiffCallback : DiffUtil.ItemCallback<Budget>() {
        override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem == newItem
        }
    }
}
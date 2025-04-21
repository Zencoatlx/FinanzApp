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
import java.util.Locale

class BudgetAdapter(private val onItemClick: (Budget) -> Unit) :
    ListAdapter<Budget, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = getItem(position)
        holder.bind(budget)
        holder.itemView.setOnClickListener { onItemClick(budget) }
    }

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.textBudgetName)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBudget)
        private val progressText: TextView = itemView.findViewById(R.id.textBudgetProgress)
        private val percentage: TextView = itemView.findViewById(R.id.textBudgetPercentage)

        fun bind(budget: Budget) {
            name.text = budget.name

            // Calcular porcentaje
            val progress = if (budget.amount > 0) {
                (budget.spent / budget.amount * 100).toInt()
            } else {
                0
            }

            progressBar.progress = progress
            percentage.text = "$progress%"

            // Formatear montos
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            progressText.text = "${format.format(budget.spent)} / ${format.format(budget.amount)}"

            // Cambiar color del porcentaje si se está acercando al límite
            if (progress >= 90) {
                percentage.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            } else if (progress >= 75) {
                percentage.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            } else {
                percentage.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            }
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
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
import com.finanzapp.data.entity.SavingGoal
import java.text.NumberFormat
import java.util.Locale

class SavingGoalAdapter : ListAdapter<SavingGoal, SavingGoalAdapter.SavingGoalViewHolder>(SavingGoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingGoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saving_goal, parent, false)
        return SavingGoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavingGoalViewHolder, position: Int) {
        val savingGoal = getItem(position)
        holder.bind(savingGoal)
    }

    class SavingGoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.textSavingGoalName)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressSavingGoal)
        private val progressText: TextView = itemView.findViewById(R.id.textSavingGoalProgress)
        private val percentage: TextView = itemView.findViewById(R.id.textSavingGoalAmount)

        fun bind(savingGoal: SavingGoal) {
            name.text = savingGoal.name

            // Calcular porcentaje
            val progress = if (savingGoal.targetAmount > 0) {
                (savingGoal.currentAmount / savingGoal.targetAmount * 100).toInt()
            } else {
                0
            }

            progressBar.progress = progress
            percentage.text = "$progress%"

            // Formatear montos
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            progressText.text = "${format.format(savingGoal.currentAmount)} / ${format.format(savingGoal.targetAmount)}"
        }
    }

    class SavingGoalDiffCallback : DiffUtil.ItemCallback<SavingGoal>() {
        override fun areItemsTheSame(oldItem: SavingGoal, newItem: SavingGoal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavingGoal, newItem: SavingGoal): Boolean {
            return oldItem == newItem
        }
    }
}
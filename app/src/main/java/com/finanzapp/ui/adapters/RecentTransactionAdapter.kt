package com.finanzapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.data.entity.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RecentTransactionAdapter : ListAdapter<Transaction, RecentTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val category: TextView = itemView.findViewById(R.id.textTransactionCategory)
        private val description: TextView = itemView.findViewById(R.id.textTransactionDescription)
        private val date: TextView = itemView.findViewById(R.id.textTransactionDate)
        private val amount: TextView = itemView.findViewById(R.id.textTransactionAmount)

        fun bind(transaction: Transaction) {
            category.text = transaction.category
            description.text = transaction.description

            // Formatear fecha
            val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale("es", "MX"))
            date.text = dateFormat.format(transaction.date)

            // Formatear monto
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            val prefix = if (transaction.isIncome) "+ " else "- "
            amount.text = prefix + format.format(transaction.amount)

            // Establecer color
            amount.setTextColor(itemView.context.getColor(
                if (transaction.isIncome) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            ))
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
package com.finanzapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.data.entity.SavingChallenge
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class ActiveChallengeAdapter(
    private val onAbandonClick: (SavingChallenge) -> Unit
) : ListAdapter<SavingChallenge, ActiveChallengeAdapter.ViewHolder>(ChallengeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_challenge, parent, false)
        return ViewHolder(view, onAbandonClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onAbandonClick: (SavingChallenge) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.imageChallengeIcon)
        private val titleView: TextView = itemView.findViewById(R.id.textChallengeTitle)
        private val descriptionView: TextView = itemView.findViewById(R.id.textChallengeDescription)
        private val rewardView: TextView = itemView.findViewById(R.id.textChallengeReward)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressChallenge)
        private val progressText: TextView = itemView.findViewById(R.id.textChallengeProgress)
        private val timeLeftView: TextView = itemView.findViewById(R.id.textChallengeTimeLeft)
        private val abandonButton: Button = itemView.findViewById(R.id.buttonAbandonChallenge)

        fun bind(challenge: SavingChallenge) {
            titleView.text = challenge.title
            descriptionView.text = challenge.description
            rewardView.text = "+${challenge.rewardPoints} XP"

            // Establecer ícono según el tipo de desafío
            val iconResource = when (challenge.type) {
                com.finanzapp.data.entity.ChallengeType.DAILY -> R.drawable.ic_daily
                com.finanzapp.data.entity.ChallengeType.WEEKLY -> R.drawable.ic_weekly
                com.finanzapp.data.entity.ChallengeType.NO_SPEND -> R.drawable.ic_no_spend
                com.finanzapp.data.entity.ChallengeType.PERCENTAGE -> R.drawable.ic_percentage
                com.finanzapp.data.entity.ChallengeType.ONE_TIME -> R.drawable.ic_one_time
                com.finanzapp.data.entity.ChallengeType.STREAK -> R.drawable.ic_streak
            }

            // Si no existen los iconos, usar uno predeterminado
            try {
                iconView.setImageResource(iconResource)
            } catch (e: Exception) {
                iconView.setImageResource(R.drawable.ic_savings)
            }

            // Calcular progreso
            val progress = if (challenge.targetAmount > 0) {
                ((challenge.progress / challenge.targetAmount) * 100).roundToInt()
            } else {
                0
            }
            progressBar.progress = progress
            progressText.text = "$progress%"

            // Calcular días restantes
            val daysLeft = calculateDaysLeft(challenge.endDate)
            timeLeftView.text = when {
                daysLeft < 0 -> "¡Vencido!"
                daysLeft == 0 -> "¡Último día!"
                daysLeft == 1 -> "1 día restante"
                else -> "$daysLeft días restantes"
            }

            // Cambiar color del tiempo restante según urgencia
            val context = itemView.context
            timeLeftView.setTextColor(
                when {
                    daysLeft < 0 -> context.getColor(android.R.color.holo_red_dark)
                    daysLeft <= 1 -> context.getColor(android.R.color.holo_red_light)
                    daysLeft <= 3 -> context.getColor(android.R.color.holo_orange_dark)
                    else -> context.getColor(android.R.color.holo_green_dark)
                }
            )

            // Configurar botón de abandonar
            abandonButton.setOnClickListener {
                onAbandonClick(challenge)
            }
        }

        private fun calculateDaysLeft(endDate: Date?): Int {
            if (endDate == null) return Int.MAX_VALUE

            val now = Calendar.getInstance().time
            val diff = endDate.time - now.time
            return TimeUnit.MILLISECONDS.toDays(diff).toInt()
        }
    }

    class ChallengeDiffCallback : DiffUtil.ItemCallback<SavingChallenge>() {
        override fun areItemsTheSame(oldItem: SavingChallenge, newItem: SavingChallenge): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavingChallenge, newItem: SavingChallenge): Boolean {
            return oldItem == newItem
        }
    }
}
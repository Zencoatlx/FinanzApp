package com.finanzapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.data.entity.ChallengeDifficulty
import com.finanzapp.data.entity.SavingChallenge

class AvailableChallengeAdapter(
    private val onAcceptClick: (SavingChallenge) -> Unit
) : ListAdapter<SavingChallenge, AvailableChallengeAdapter.ViewHolder>(ChallengeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_available_challenge, parent, false)
        return ViewHolder(view, onAcceptClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onAcceptClick: (SavingChallenge) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.imageChallengeIcon)
        private val titleView: TextView = itemView.findViewById(R.id.textChallengeTitle)
        private val descriptionView: TextView = itemView.findViewById(R.id.textChallengeDescription)
        private val difficultyView: TextView = itemView.findViewById(R.id.textChallengeDifficulty)
        private val rewardView: TextView = itemView.findViewById(R.id.textChallengeReward)
        private val durationView: TextView = itemView.findViewById(R.id.textChallengeDuration)
        private val acceptButton: Button = itemView.findViewById(R.id.buttonAcceptChallenge)

        fun bind(challenge: SavingChallenge) {
            titleView.text = challenge.title
            descriptionView.text = challenge.description
            rewardView.text = "${challenge.rewardPoints} XP"

            // Formatear duración
            val durationText = when {
                challenge.duration <= 1 -> "1 día"
                challenge.duration <= 7 -> "${challenge.duration} días"
                challenge.duration % 7 == 0 -> "${challenge.duration / 7} semanas"
                else -> "${challenge.duration} días"
            }
            durationView.text = durationText

            // Establecer texto y color de dificultad
            difficultyView.text = getDifficultyText(challenge.difficulty)
            difficultyView.background.setTint(getDifficultyColor(challenge.difficulty))

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

            // Configurar botón de aceptar
            acceptButton.setOnClickListener {
                onAcceptClick(challenge)
            }
        }

        private fun getDifficultyText(difficulty: ChallengeDifficulty): String {
            return when (difficulty) {
                ChallengeDifficulty.BEGINNER -> "PRINCIPIANTE"
                ChallengeDifficulty.EASY -> "FÁCIL"
                ChallengeDifficulty.MEDIUM -> "MEDIO"
                ChallengeDifficulty.HARD -> "DIFÍCIL"
                ChallengeDifficulty.EXPERT -> "EXPERTO"
            }
        }

        private fun getDifficultyColor(difficulty: ChallengeDifficulty): Int {
            val context = itemView.context
            return context.getColor(
                when (difficulty) {
                    ChallengeDifficulty.BEGINNER -> android.R.color.holo_blue_light
                    ChallengeDifficulty.EASY -> android.R.color.holo_green_light
                    ChallengeDifficulty.MEDIUM -> android.R.color.holo_orange_light
                    ChallengeDifficulty.HARD -> android.R.color.holo_orange_dark
                    ChallengeDifficulty.EXPERT -> android.R.color.holo_red_light
                }
            )
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
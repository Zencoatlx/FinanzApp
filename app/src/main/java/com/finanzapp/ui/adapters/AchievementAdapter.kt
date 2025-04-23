package com.finanzapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finanzapp.R
import com.finanzapp.data.entity.Achievement
import com.finanzapp.data.entity.AchievementTier

class AchievementAdapter(
    private val onItemClick: (Achievement) -> Unit
) : ListAdapter<Achievement, AchievementAdapter.ViewHolder>(AchievementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Achievement) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.imageAchievementIcon)
        private val tierBadgeView: ImageView = itemView.findViewById(R.id.imageTierBadge)
        private val lockedView: View = itemView.findViewById(R.id.viewLocked)
        private val lockIconView: ImageView = itemView.findViewById(R.id.imageLock)
        private val titleView: TextView = itemView.findViewById(R.id.textAchievementTitle)
        private val descriptionView: TextView = itemView.findViewById(R.id.textAchievementDescription)
        private val rewardView: TextView = itemView.findViewById(R.id.textAchievementReward)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressAchievement)

        fun bind(achievement: Achievement) {
            titleView.text = achievement.title
            descriptionView.text = achievement.description
            rewardView.text = "+${achievement.pointsReward} XP"

            // Configurar visibilidad del estado bloqueado/desbloqueado
            val isUnlocked = achievement.isUnlocked
            lockedView.visibility = if (isUnlocked) View.GONE else View.VISIBLE
            lockIconView.visibility = if (isUnlocked) View.GONE else View.VISIBLE

            // Establecer ícono
            try {
                if (achievement.iconName.isNotEmpty()) {
                    val resourceId = itemView.context.resources.getIdentifier(
                        achievement.iconName, "drawable", itemView.context.packageName
                    )
                    if (resourceId != 0) {
                        iconView.setImageResource(resourceId)
                    } else {
                        // Ícono por defecto
                        iconView.setImageResource(R.drawable.ic_check)
                    }
                } else {
                    // Ícono por defecto
                    iconView.setImageResource(R.drawable.ic_check)
                }
            } catch (e: Exception) {
                // Ícono por defecto si hay error
                iconView.setImageResource(R.drawable.ic_check)
            }

            // Establecer ícono de nivel
            tierBadgeView.setImageResource(getTierBadgeResource(achievement.tier))

            // Establecer progreso
            val progress = if (achievement.targetProgress > 0) {
                (achievement.progress * 100 / achievement.targetProgress)
            } else 100

            progressBar.progress = progress

            // Si está desbloqueado, ocultar la barra de progreso
            progressBar.visibility = if (isUnlocked) View.GONE else View.VISIBLE

            // Configurar clic
            itemView.setOnClickListener {
                onItemClick(achievement)
            }
        }

        private fun getTierBadgeResource(tier: AchievementTier): Int {
            return when (tier) {
                AchievementTier.BRONZE -> R.drawable.ic_achievement_bronze
                AchievementTier.SILVER -> R.drawable.ic_achievement_silver
                AchievementTier.GOLD -> R.drawable.ic_achievement_gold
                AchievementTier.PLATINUM -> R.drawable.ic_achievement_platinum
                AchievementTier.DIAMOND -> R.drawable.ic_achievement_diamond
            }
        }
    }

    class AchievementDiffCallback : DiffUtil.ItemCallback<Achievement>() {
        override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem == newItem && oldItem.progress == newItem.progress && oldItem.isUnlocked == newItem.isUnlocked
        }
    }
}
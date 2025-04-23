package com.finanzapp.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.finanzapp.MainActivity
import com.finanzapp.R
import com.finanzapp.data.AppDatabase
import com.finanzapp.data.entity.SavingStreak
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Servicio para gestionar recordatorios diarios de racha de ahorro.
 * Este servicio se encarga de:
 * 1. Programar notificaciones diarias
 * 2. Verificar rachas perdidas
 * 3. Motivar al usuario a mantener su racha
 */
class DailyReminderService {

    companion object {
        private const val CHANNEL_ID = "saving_streak_channel"
        private const val NOTIFICATION_ID = 1001
        private const val DAILY_REMINDER_REQUEST_CODE = 2001

        /**
         * Configura el canal de notificaciones para Android 8.0+
         */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Rachas de Ahorro"
                val descriptionText = "Recordatorios para mantener tu racha de ahorro"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        /**
         * Programa el recordatorio diario para mantener la racha
         */
        fun scheduleDailyReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, StreakReminderReceiver::class.java)

            // Configurar PendingIntent inmutable
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Establecer hora para la notificación (8:00 PM)
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)

                // Si ya pasó la hora hoy, programar para mañana
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // Establecer alarma repetitiva diaria
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        /**
         * Cancela el recordatorio diario
         */
        fun cancelDailyReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, StreakReminderReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
        }

        /**
         * Verifica si la racha está activa y si el usuario ya ha ahorrado hoy
         */
        fun checkStreakStatus(context: Context, callback: (Boolean, Int) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getDatabase(context)
                val savingStreakDao = database.savingStreakDao()

                val streak = savingStreakDao.getSavingStreakDirect() ?: SavingStreak(id = 1)
                val activeToday = streak.isActiveToday
                val currentStreak = streak.currentStreak

                // Devuelve: (¿Ya ahorró hoy?, Días de racha actual)
                CoroutineScope(Dispatchers.Main).launch {
                    callback(activeToday, currentStreak)
                }
            }
        }
    }

    /**
     * BroadcastReceiver para recibir y mostrar recordatorios de racha
     */
    class StreakReminderReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Verificar estado de la racha
            checkStreakStatus(context) { savedToday, currentStreak ->
                // Solo mostrar notificación si no ha ahorrado hoy y tiene una racha activa
                if (!savedToday && currentStreak > 0) {
                    showStreakReminder(context, currentStreak)
                }
            }
        }

        /**
         * Muestra una notificación de recordatorio para mantener la racha
         */
        private fun showStreakReminder(context: Context, streakDays: Int) {
            // Crear intent para abrir la app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )

            // Personalizar mensaje según la longitud de la racha
            val title = "¡No pierdas tu racha de ahorro!"
            val message = when {
                streakDays >= 30 -> "¡Impresionante! Llevas $streakDays días seguidos ahorrando. ¡No pierdas tu racha hoy!"
                streakDays >= 14 -> "¡Gran trabajo! Tu racha de $streakDays días está creciendo. Mantén el impulso."
                streakDays >= 7 -> "Llevas $streakDays días seguidos ahorrando. ¡Sigue así!"
                streakDays > 1 -> "Has ahorrado $streakDays días consecutivos. ¡No rompas la cadena!"
                else -> "Has empezado una racha de ahorro. ¡Ahorra hoy para mantenerla!"
            }

            // Construir notificación
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_streak)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))

            // Mostrar notificación
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }
}
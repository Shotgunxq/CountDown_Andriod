package com.example.countdown

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CountDownWidgetProvider : AppWidgetProvider() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val PREFS_NAME = "com.example.countdown.widget"
        private const val PREF_PREFIX_KEY = "targetDate"

        @RequiresApi(Build.VERSION_CODES.O)
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val targetDateMillis = prefs.getLong(PREF_PREFIX_KEY, LocalDate.now().toEpochDay())
            val targetDate = LocalDate.ofEpochDay(targetDateMillis)
            val daysRemaining = daysRemaining(targetDate)

            val views = RemoteViews(context.packageName, R.layout.widget_countdown)
            views.setTextViewText(R.id.widget_days_remaining, "Days remaining: $daysRemaining")

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_days_remaining, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun daysRemaining(targetDate: LocalDate): Int {
            val today = LocalDate.now()
            return ChronoUnit.DAYS.between(today, targetDate).toInt()
        }
    }
}

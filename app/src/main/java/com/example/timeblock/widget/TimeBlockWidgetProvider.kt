package com.example.timeblock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.timeblock.MainActivity
import com.example.timeblock.R
import com.example.timeblock.data.AppDatabase
import com.example.timeblock.data.Repository
import com.example.timeblock.ui.MainViewModel
import kotlinx.coroutines.runBlocking

class TimeBlockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_INCREMENT_PROTEIN = "com.example.timeblock.action.INCREMENT_PROTEIN"
        const val ACTION_INCREMENT_VEGGIES = "com.example.timeblock.action.INCREMENT_VEGGIES"
        const val ACTION_INCREMENT_STEPS = "com.example.timeblock.action.INCREMENT_STEPS"
        const val EXTRA_EDIT_MODE = "com.example.timeblock.extra.EDIT_MODE"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, TimeBlockWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val db = AppDatabase.getDatabase(context)
            val repository = Repository(db.userDao(), db.entryDao())
            val entry = runBlocking { repository.getOrCreateTodayEntry() }

            val views = RemoteViews(context.packageName, R.layout.timeblock_widget)
            views.setTextViewText(R.id.widget_protein_value, "${entry.proteinGrams}g")
            views.setTextViewText(R.id.widget_veggies_value, "${entry.vegetableServings}")
            views.setTextViewText(R.id.widget_steps_value, "${entry.steps}")

            views.setOnClickPendingIntent(
                R.id.widget_protein_add,
                getBroadcastPendingIntent(context, ACTION_INCREMENT_PROTEIN)
            )
            views.setOnClickPendingIntent(
                R.id.widget_veggies_add,
                getBroadcastPendingIntent(context, ACTION_INCREMENT_VEGGIES)
            )
            views.setOnClickPendingIntent(
                R.id.widget_steps_add,
                getBroadcastPendingIntent(context, ACTION_INCREMENT_STEPS)
            )

            views.setOnClickPendingIntent(
                R.id.widget_protein_value,
                getActivityPendingIntent(context, MainViewModel.EditMode.PROTEIN)
            )
            views.setOnClickPendingIntent(
                R.id.widget_veggies_value,
                getActivityPendingIntent(context, MainViewModel.EditMode.VEGETABLES)
            )
            views.setOnClickPendingIntent(
                R.id.widget_steps_value,
                getActivityPendingIntent(context, MainViewModel.EditMode.STEPS)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getBroadcastPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, WidgetReceiver::class.java).apply { this.action = action }
            return PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun getActivityPendingIntent(context: Context, mode: MainViewModel.EditMode): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_EDIT_MODE, mode.name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                mode.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

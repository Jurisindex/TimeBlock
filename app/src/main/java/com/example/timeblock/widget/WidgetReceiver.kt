package com.example.timeblock.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.timeblock.data.AppDatabase
import com.example.timeblock.data.Repository
import kotlinx.coroutines.runBlocking

class WidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TimeBlockWidgetProvider.ACTION_INCREMENT_PROTEIN -> {
                updateEntry(context) { repo -> runBlocking { repo.updateProtein(1, true) } }
            }
            TimeBlockWidgetProvider.ACTION_INCREMENT_VEGGIES -> {
                updateEntry(context) { repo -> runBlocking { repo.updateVegetables(1, true) } }
            }
            TimeBlockWidgetProvider.ACTION_INCREMENT_STEPS -> {
                updateEntry(context) { repo -> runBlocking { repo.updateSteps(100, true) } }
            }
        }
        super.onReceive(context, intent)
    }

    private fun updateEntry(context: Context, block: (Repository) -> Unit) {
        val db = AppDatabase.getDatabase(context)
        val repository = Repository(db.userDao(), db.entryDao())
        block(repository)
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, TimeBlockWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        for (id in ids) {
            TimeBlockWidgetProvider.updateAppWidget(context, manager, id)
        }
    }
}

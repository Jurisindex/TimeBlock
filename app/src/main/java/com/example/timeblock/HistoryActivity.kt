package com.example.timeblock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.timeblock.data.AppDatabase
import com.example.timeblock.data.Repository
import com.example.timeblock.ui.HistoryViewModel
import com.example.timeblock.ui.screens.HistoryApp
import com.example.timeblock.ui.theme.TimeBlockTheme

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(database.userDao(), database.entryDao())
        val viewModelFactory = HistoryViewModel.HistoryViewModelFactory(repository)

        setContent {
            TimeBlockTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HistoryApp(viewModelFactory)
                }
            }
        }
    }
}

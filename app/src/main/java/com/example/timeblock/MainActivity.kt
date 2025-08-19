package com.example.timeblock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.timeblock.data.AppDatabase
import com.example.timeblock.data.Repository
import com.example.timeblock.ui.MainViewModel
import com.example.timeblock.ui.screens.HomeScreen
import com.example.timeblock.ui.screens.LoadingScreen
import com.example.timeblock.ui.screens.UserSetupScreen
import com.example.timeblock.ui.screens.HistoryScreen
import com.example.timeblock.ui.screens.SettingsScreen
import com.example.timeblock.ui.HistoryViewModel
import com.example.timeblock.ui.screens.LineGraphScreen
import com.example.timeblock.ui.theme.TimeBlockTheme
import com.example.timeblock.util.GarminHealthConnectStepsProvider
import com.example.timeblock.util.StepCountProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var stepCountProvider: StepCountProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(database.userDao(), database.entryDao())
        val viewModelFactory = MainViewModel.MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        val historyFactory = HistoryViewModel.HistoryViewModelFactory(repository)
        stepCountProvider = GarminHealthConnectStepsProvider(this)

        setContent {
            TimeBlockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimeBlockApp(viewModelFactory, historyFactory)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshForDateChange()
        lifecycleScope.launch {
            val steps = stepCountProvider.getTodaySteps().toInt()
            if (steps > 0) {
                viewModel.updateValue(steps, isAddition = false)
            }
        }
    }
}

@Composable
fun TimeBlockApp(
    viewModelFactory: MainViewModel.MainViewModelFactory,
    historyFactory: HistoryViewModel.HistoryViewModelFactory
) {
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
    val historyViewModel: HistoryViewModel = viewModel(factory = historyFactory)
    val uiState by viewModel.uiState.collectAsState()
    val isHistory by viewModel.isHistory.collectAsState()
    val isSettings by viewModel.isSettings.collectAsState()
    val isLineGraph by viewModel.isLineGraph.collectAsState()

    when (uiState) {
        is MainViewModel.UiState.Loading -> {
            LoadingScreen()
        }
        is MainViewModel.UiState.NeedsUser -> {
            UserSetupScreen(onUserCreated = { displayName, weight ->
                viewModel.createUser(displayName, weight)
            })
        }
        is MainViewModel.UiState.Ready -> {
            val user = (uiState as MainViewModel.UiState.Ready).user
            val trackingData by viewModel.trackingData.collectAsState()
            val editMode by viewModel.currentEditMode.collectAsState()

            if (isSettings) {
                SettingsScreen(user = user,
                    onSave = { name, weight -> viewModel.updateUser(user, name, weight); viewModel.closeSettings() },
                    onBack = { viewModel.closeSettings() })
            } else if (isLineGraph) {
                LineGraphScreen(viewModel = historyViewModel, onBack = { viewModel.exitLineGraph() })
            } else if (isHistory) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    weight = user.weight,
                    onBack = { viewModel.exitHistory() },
                    onShowGraphs = { viewModel.showLineGraph() }
                )
            } else {
                HomeScreen(
                    user = user,
                    trackingData = trackingData,
                    currentEditMode = editMode,
                    onEditModeSelected = { mode -> viewModel.showEditDialog(mode) },
                    onDismissDialog = { viewModel.dismissEditDialog() },
                    onUpdateValue = { value, isAddition -> viewModel.updateValue(value, isAddition) },
                    onViewHistory = { viewModel.viewHistory() },
                    onOpenSettings = { viewModel.openSettings() },
                    showWeightPrompt = user.weight == "0",
                    onWeightSet = { weight -> viewModel.updateUser(user, user.displayName, weight) }
                )
            }
        }
    }
}
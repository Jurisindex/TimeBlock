package com.example.timeblock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timeblock.data.AppDatabase
import com.example.timeblock.data.Repository
import com.example.timeblock.data.SettingsDataStore
import com.example.timeblock.data.ThemeMode
import com.example.timeblock.ui.HistoryViewModel
import com.example.timeblock.ui.MainViewModel
import com.example.timeblock.ui.screens.HistoryScreen
import com.example.timeblock.ui.screens.HomeScreen
import com.example.timeblock.ui.screens.LoadingScreen
import com.example.timeblock.ui.screens.SettingsScreen
import com.example.timeblock.ui.screens.UserSetupScreen
import com.example.timeblock.ui.theme.TimeBlockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(database.userDao(), database.entryDao())
        val settingsStore = SettingsDataStore(applicationContext)
        val viewModelFactory = MainViewModel.MainViewModelFactory(repository, settingsStore)
        val historyFactory = HistoryViewModel.HistoryViewModelFactory(repository)

        setContent {
            TimeBlockApp(viewModelFactory, historyFactory)
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
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    val darkTheme = when (settings.theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    TimeBlockTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (uiState) {
                is MainViewModel.UiState.Loading -> LoadingScreen()
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
                        SettingsScreen(
                            user = user,
                            theme = settings.theme,
                            onThemeChange = { viewModel.setTheme(it) },
                            garminEnabled = settings.garminEnabled,
                            onGarminChange = { viewModel.setGarminEnabled(it) },
                            wordleShare = settings.wordleShare,
                            onWordleChange = { viewModel.setWordleShare(it) },
                            onExportDb = { viewModel.exportDatabase(context) },
                            onImportDb = { viewModel.importDatabase(context) },
                            onSave = { name, weight ->
                                viewModel.updateUser(user, name, weight)
                                viewModel.closeSettings()
                            },
                            onBack = { viewModel.closeSettings() }
                        )
                    } else if (isHistory) {
                        HistoryScreen(viewModel = historyViewModel, weight = user.weight, onBack = { viewModel.exitHistory() })
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
    }
}

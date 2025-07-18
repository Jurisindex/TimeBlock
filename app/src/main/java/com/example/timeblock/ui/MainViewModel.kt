package com.example.timeblock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import com.example.timeblock.data.Repository
import com.example.timeblock.data.entity.Entry
import com.example.timeblock.data.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MainViewModel(private val repository: Repository) : ViewModel() {

    private val loadMutex = Mutex()
    private var lastLoadedDay: LocalDate? = null

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _trackingData = MutableStateFlow<Entry?>(null)
    val trackingData: StateFlow<Entry?> = _trackingData.asStateFlow()

    private val _currentEditMode = MutableStateFlow<EditMode?>(null)
    val currentEditMode: StateFlow<EditMode?> = _currentEditMode.asStateFlow()

    private val _isHistory = MutableStateFlow(false)
    val isHistory: StateFlow<Boolean> = _isHistory.asStateFlow()

    private val _isSettings = MutableStateFlow(false)
    val isSettings: StateFlow<Boolean> = _isSettings.asStateFlow()

    private val _isLineGraph = MutableStateFlow(false)
    val isLineGraph: StateFlow<Boolean> = _isLineGraph.asStateFlow()

    private val _allEntries = MutableStateFlow<List<Entry>>(emptyList())
    val allEntries: StateFlow<List<Entry>> = _allEntries.asStateFlow()

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            val user = repository.getUser()
            if (user == null) {
                _uiState.value = UiState.NeedsUser
            } else {
                _uiState.value = UiState.Ready(user)
                loadTodayEntry()
            }
        }
    }

    private fun loadTodayEntry() {
        viewModelScope.launch {
            loadMutex.withLock {
                _trackingData.value = repository.getOrCreateTodayEntry()
                lastLoadedDay = LocalDate.now()
            }
        }
    }

    fun refreshForDateChange() {
        val today = LocalDate.now()
        if (lastLoadedDay != today) {
            loadTodayEntry()
        }
    }

    fun createUser(displayName: String, weight: String) {
        viewModelScope.launch {
            val user = repository.insertUser(displayName, weight)
            _uiState.value = UiState.Ready(user)
            loadTodayEntry()
        }
    }

    fun showEditDialog(mode: EditMode) {
        _currentEditMode.value = mode
    }

    fun dismissEditDialog() {
        _currentEditMode.value = null
    }

    fun updateValue(value: Int, isAddition: Boolean) {
        viewModelScope.launch {
            val updatedEntry = when (_currentEditMode.value) {
                EditMode.PROTEIN -> repository.updateProtein(value, isAddition)
                EditMode.VEGETABLES -> repository.updateVegetables(value, isAddition)
                EditMode.STEPS -> repository.updateSteps(value, isAddition)
                null -> return@launch
            }
            _trackingData.value = updatedEntry
            _currentEditMode.value = null
        }
    }

    fun viewHistory() {
        viewModelScope.launch {
            _allEntries.value = repository.getAllEntries()
            _isHistory.value = true
        }
    }

    fun exitHistory() {
        _isHistory.value = false
    }

    fun showLineGraph() {
        viewModelScope.launch {
            _allEntries.value = repository.getAllEntries()
            _isLineGraph.value = true
        }
    }

    fun exitLineGraph() {
        _isLineGraph.value = false
    }

    fun openSettings() {
        _isSettings.value = true
    }

    fun closeSettings() {
        _isSettings.value = false
    }

    fun updateUser(user: User, displayName: String, weight: String) {
        viewModelScope.launch {
            val updated = repository.updateUser(user, displayName, weight)
            _uiState.value = UiState.Ready(updated)
        }
    }

    sealed class UiState {
        object Loading : UiState()
        object NeedsUser : UiState()
        data class Ready(val user: User) : UiState()
    }

    enum class EditMode {
        PROTEIN, VEGETABLES, STEPS
    }

    class MainViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

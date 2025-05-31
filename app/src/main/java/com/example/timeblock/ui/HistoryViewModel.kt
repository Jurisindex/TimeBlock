package com.example.timeblock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timeblock.data.Repository
import com.example.timeblock.data.entity.Entry
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HistoryRange { MAX, DAYS_30, DAYS_5 }

class HistoryViewModel(private val repository: Repository) : ViewModel() {

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private val _currentRange = MutableStateFlow(HistoryRange.DAYS_5)
    val currentRange: StateFlow<HistoryRange> = _currentRange.asStateFlow()

    init {
        loadEntries(_currentRange.value)
    }

    fun loadEntries(
        range: HistoryRange = _currentRange.value,
        updateCurrentRange: Boolean = true
    ) {
        viewModelScope.launch {
            if (updateCurrentRange) {
                _currentRange.value = range
            }
            val list = when (range) {
                HistoryRange.MAX -> repository.getAllEntries()
                HistoryRange.DAYS_30 -> repository.getEntriesSince(30)
                HistoryRange.DAYS_5 -> repository.getEntriesSince(5)
            }
            _entries.value = list.sortedByDescending { it.timeCreated }
        }
    }

    fun addEntry(date: LocalDate) {
        viewModelScope.launch {
            repository.insertEntryOn(date)
            loadEntries(_currentRange.value)
        }
    }

    fun updateEntry(entry: Entry, protein: Int, vegetables: Int, steps: Int) {
        viewModelScope.launch {
            repository.updateEntry(entry, protein, vegetables, steps)
            loadEntries(_currentRange.value)
        }
    }

    fun deleteEntry(entry: Entry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            loadEntries(_currentRange.value)
        }
    }

    class HistoryViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
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

    private var currentRange: HistoryRange = HistoryRange.DAYS_5

    init {
        loadEntries()
    }

    fun loadEntries(range: HistoryRange = currentRange) {
        viewModelScope.launch {
            currentRange = range
            _entries.value = when (range) {
                HistoryRange.MAX -> repository.getAllEntries()
                HistoryRange.DAYS_30 -> repository.getEntriesSince(30)
                HistoryRange.DAYS_5 -> repository.getEntriesSince(5)
            }
        }
    }

    fun addEntry(date: LocalDate) {
        viewModelScope.launch {
            repository.insertEntryOn(date)
            loadEntries(currentRange)
        }
    }

    fun updateEntry(entry: Entry, protein: Int, vegetables: Int, steps: Int) {
        viewModelScope.launch {
            repository.updateEntry(entry, protein, vegetables, steps)
            loadEntries(currentRange)
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
package ru.mrlargha.thenightingale.ui.records

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.mrlargha.thenightingale.data.repos.MusicRepository
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    val musicRepository: MusicRepository
) : ViewModel()
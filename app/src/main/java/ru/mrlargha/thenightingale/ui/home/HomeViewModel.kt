package ru.mrlargha.thenightingale.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.scopes.FragmentScoped
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import ru.mrlargha.thenightingale.data.repos.MusicRepository
import javax.inject.Inject

@FragmentScoped
class HomeViewModel @Inject constructor(private val musicRepository: MusicRepository) :
    ViewModel() {
    val musicFilesLiveData = MutableLiveData(musicRepository.getMusic())
}
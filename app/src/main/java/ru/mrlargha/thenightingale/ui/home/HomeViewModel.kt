package ru.mrlargha.thenightingale.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import ru.mrlargha.thenightingale.data.repos.MusicRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val musicRepository: MusicRepository) :
    ViewModel() {
    val musicFilesLiveData = MutableLiveData<List<MusicFileInfo>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            musicFilesLiveData.postValue(musicRepository.getMusic())
        }
    }

}
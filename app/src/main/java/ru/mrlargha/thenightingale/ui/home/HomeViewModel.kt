package ru.mrlargha.thenightingale.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import ru.mrlargha.thenightingale.data.repos.MusicRepository
import javax.inject.Inject

class HomeViewModel @ViewModelInject constructor(private val musicRepository: MusicRepository) :
    ViewModel() {
    val musicFilesLiveData = MutableLiveData<List<MusicFileInfo>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            musicFilesLiveData.postValue(musicRepository.getMusic())
        }
    }

}
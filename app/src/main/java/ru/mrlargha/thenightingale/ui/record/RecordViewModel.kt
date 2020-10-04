package ru.mrlargha.thenightingale.ui.record

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mrlargha.thenightingale.data.models.MusicFileInfo

@FragmentScoped
class RecordViewModel @ViewModelInject constructor(
    @ActivityContext val context: Context
) :
    ViewModel() {

    enum class RecordState {
        RECORDING,
        STOPPED
    }

    var fileUri: Uri? = null
        set(value) {
            field = value
            viewModelScope.launch(Dispatchers.IO) {
                trackInfoLiveData.postValue(MusicFileInfo.createByUri(value ?: Uri.EMPTY, context))
                setupMediaPlayer()
            }
        }

    val trackInfoLiveData: MutableLiveData<MusicFileInfo> = MutableLiveData()
    val currentProgressLiveData: MutableLiveData<Int> = MutableLiveData()
    val playerReadyLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    val playerStatusLiveData: MutableLiveData<RecordState> = MutableLiveData(RecordState.STOPPED)
    val maxPosLiveData: MutableLiveData<Int> = MutableLiveData(100)

    private var mediaPlayer: MediaPlayer? = null

    private fun setupMediaPlayer() =
        fileUri?.let { uri ->
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                prepareAsync()
                setOnPreparedListener {
                    maxPosLiveData.postValue(it.duration)
                    playerReadyLiveData.postValue(true)
                }
            }
        }

    override fun onCleared() {
        mediaPlayer?.stop()
    }

    fun invertStatus() {
        when (playerStatusLiveData.value) {
            RecordState.RECORDING -> {
                mediaPlayer?.stop()
                playerReadyLiveData.postValue(false)
                setupMediaPlayer()
                playerStatusLiveData.postValue(RecordState.STOPPED)
            }
            else -> {
                mediaPlayer?.let {
                    it.start()
                    playerStatusLiveData.postValue(RecordState.RECORDING)
                    viewModelScope.launch {
                        while (it.isPlaying) {
                            currentProgressLiveData.postValue(it.currentPosition)
                            delay(100)
                        }
                    }
                }
            }
        }
    }

}
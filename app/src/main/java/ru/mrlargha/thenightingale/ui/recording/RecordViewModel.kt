package ru.mrlargha.thenightingale.ui.recording

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mrlargha.thenightingale.data.database.MusicDatabase
import ru.mrlargha.thenightingale.data.models.IntensityRecord
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext val context: Context
) :
    ViewModel() {


    enum class RecordState {
        RECORDING,
        STOPPED
    }

    var currentIntensity: AtomicInteger = AtomicInteger(0)
    private var collectDataJob: Job? = null

    var fileUri: Uri? = null
        set(value) {
            field = value
            viewModelScope.launch(Dispatchers.IO) {
                musicFileInfoLiveData.postValue(
                    MusicFileInfo.createByUri(
                        value ?: Uri.EMPTY,
                        context
                    )
                )
                setupMediaPlayer()
            }
        }

    val musicFileInfoLiveData: MutableLiveData<MusicFileInfo> = MutableLiveData()
    val currentProgressLiveData: MutableLiveData<Int> = MutableLiveData()
    val playerReadyLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    val playerStatusLiveData: MutableLiveData<RecordState> = MutableLiveData(RecordState.STOPPED)
    val maxPosLiveData: MutableLiveData<Int> = MutableLiveData(100)

    private var mediaPlayer: MediaPlayer? = null

    val recordData: MutableList<Pair<Int, Int>> = mutableListOf()

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

    private fun startRecording() {
        mediaPlayer?.setOnCompletionListener { finishRecording() }
        mediaPlayer?.let {
            it.start()
            recordData.clear()
            playerStatusLiveData.postValue(RecordState.RECORDING)
            viewModelScope.launch {
                while (it.isPlaying) {
                    currentProgressLiveData.postValue(it.currentPosition)
                    delay(100)
                }
            }
            collectDataJob = viewModelScope.launch {
                while (true) {
                    recordData.add(Pair(mediaPlayer?.currentPosition ?: 0, currentIntensity.get()))
                    delay(50)
                }
            }
        }
    }

    private fun finishRecording() {
        mediaPlayer?.stop()
        playerReadyLiveData.postValue(false)
        setupMediaPlayer()
        playerStatusLiveData.postValue(RecordState.STOPPED)
        collectDataJob?.cancel()
        val rec = musicFileInfoLiveData.value?.let {
            IntensityRecord(
                it,
                Uri.decode(context.filesDir.path + "/rec_" + musicFileInfoLiveData.value?.name + ".ir")
            )
        }
        rec?.let {
            viewModelScope.launch(Dispatchers.IO) {
                MusicDatabase.getInstance(context).intensityRecordDao().insertRecord(it)
                it.saveData()
            }
        }
    }

    fun invertStatus() {
        when (playerStatusLiveData.value) {
            RecordState.STOPPED -> startRecording()
            else -> finishRecording()
        }
    }
}
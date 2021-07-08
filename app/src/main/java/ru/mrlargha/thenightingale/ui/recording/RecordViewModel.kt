package ru.mrlargha.thenightingale.ui.recording

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mrlargha.thenightingale.data.database.MusicDatabase
import ru.mrlargha.thenightingale.data.models.IntensityRecord
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import ru.mrlargha.thenightingale.data.repos.BLEPlayer
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val player: BLEPlayer,
) :
    ViewModel() {

    enum class RecordState {
        PLAYING,
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

    val inPulse: AtomicBoolean = AtomicBoolean(false)
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
                    delay(20)
                }
            }
            collectDataJob = viewModelScope.launch {
                while (true) {
                    if(!inPulse.get()) {
                        var intensity = currentIntensity.get()

                        if (abs(intensity) < 20) {
                            intensity = 0
                        }

                        if (abs(intensity) % 20 != 0) {
                            if (intensity > 0)
                                intensity += (20 - abs(intensity) % 20)
                            else
                                intensity -= (20 - abs(intensity) % 20)
                        }

                        if (abs(intensity) > 200) {
                            intensity = if (intensity > 0) {
                                255
                            } else {
                                -255
                            }
                        }

                        if (recordData.isEmpty() || recordData.last().second != intensity) {
                            recordData.add(
                                Pair(
                                    mediaPlayer?.currentPosition ?: 0,
                                    intensity
                                )
                            )
                            viewModelScope.launch(Dispatchers.IO) {
                                player.setSpeed(intensity)
                            }
                        }
                    }
                    delay(150)
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
            ).also { it.data = recordData }
        }
        rec?.let {
            viewModelScope.launch(Dispatchers.IO) {
                MusicDatabase.getInstance(context).intensityRecordDao().insertRecord(it)
                it.saveData()
            }
        }
    }

    fun playRecordedTrack(trackId: Int) {
        playerStatusLiveData.postValue(RecordState.PLAYING)
        viewModelScope.launch(Dispatchers.IO) {
            MusicDatabase.getInstance(context).intensityRecordDao().getRecordsSync()
                .find { it.id == trackId }?.let {
                    it.loadData()
                    mediaPlayer?.start()
                    delay(it.data.first().first.toLong())
                    player.setSpeed(it.data.first().second)
                    var last = it.data.first().second.toLong()
                    for (entry in it.data.subList(1, it.data.lastIndex)) {
                        delay(entry.first - last)
                        last = entry.first.toLong()
                        player.setSpeed(entry.second)
                    }
                }
        }
    }

    fun pulseDown() {
        inPulse.set(false)
        viewModelScope.launch(Dispatchers.IO){
            player.setSpeed(currentIntensity.get())
            recordData.add(Pair(mediaPlayer?.currentPosition ?: 0, currentIntensity.get()))
        }
    }

    fun pulseUp() {
        if (playerStatusLiveData.value == RecordState.RECORDING) {
            val intensity = currentIntensity.get()
            val pulseDir =
                when {
                    intensity > 0 -> -255
                    intensity < 0 -> 255
                    else -> 255
                }
            viewModelScope.launch(Dispatchers.IO){
                inPulse.set(true)
                recordData.add(Pair(mediaPlayer?.currentPosition ?: 0, pulseDir))
                player.setSpeed(pulseDir)
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
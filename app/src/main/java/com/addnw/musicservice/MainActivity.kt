package com.addnw.musicservice

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import com.addnw.musicservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val LOG_KEY = "Music"
        val songs = listOf<String>(
                "01-Jak.mp3",
                "02-Narodziny-swiata.mp3",
                "03-Czarny-blues-o-czwartej-nad-ranem.mp3",
                "04-Opadły-mgły,-wstaje-nowy-dzień.mp3",
                "05-Pieśń-na-wyjście.mp3"
        )
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var handler: Handler
    private lateinit var binding: ActivityMainBinding

    private var currentSongIndex = 0
    private var isReady = true

    private val updateSeekBarTask = object : Runnable {
        override fun run() {
            Log.d(LOG_KEY, "run")
            if (mediaPlayer.isPlaying) {
                Log.d(LOG_KEY, "set to: $mediaPlayer.currentPosition")
                binding.seekBar.progress = mediaPlayer.currentPosition
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        mediaPlayer = MediaPlayer()
        handler = Handler(Looper.getMainLooper())
        setContentView(binding.root)

        setControlling()

        mediaPlayer.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())
        mediaPlayer.setOnCompletionListener {
            next(true)
        }

        loadSong()
        mediaPlayer.prepare()
        binding.apply {
            songName.text = songs[currentSongIndex]
            seekBar.max = mediaPlayer.duration
            seekBar.progress = 0
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateSeekBarTask)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateSeekBarTask)
    }

    private fun setControlling() {
        binding.apply {
            next.setOnClickListener { next() }
            prev.setOnClickListener { previous() }

            play.setOnClickListener { play() }
            pause.setOnClickListener { pause() }
            pause.isEnabled = false
            stop.setOnClickListener { stop() }
            stop.isEnabled = false

            add10.setOnClickListener { jumpByTime(10000) }
            sub10.setOnClickListener { jumpByTime(-10000) }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        Log.d(LOG_KEY, "moved to: $progress")
                        seekBar?.let { mediaPlayer.seekTo(it.progress) }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun play() {
        if (!isReady) {
            mediaPlayer.prepare()
            isReady = true
        }
        mediaPlayer.start()
        binding.apply {
            pause.isEnabled = true
            play.isEnabled = false
            stop.isEnabled = true
            seekBar.isEnabled = true
        }
    }

    private fun stop() {
        mediaPlayer.stop()
        isReady = false
        binding.apply {
            pause.isEnabled = false
            play.isEnabled = true
            stop.isEnabled = false
            seekBar.isEnabled = false
            seekBar.progress = 0
        }
    }

    private fun pause() {
        mediaPlayer.pause()
        binding.apply {
            pause.isEnabled = false
            play.isEnabled = true
            stop.isEnabled = true
        }
    }

    private fun next(forceStart: Boolean = false) {
        currentSongIndex = if (currentSongIndex + 1 == songs.size) 0 else currentSongIndex + 1
        changeSong(forceStart)
    }

    private fun previous() {
        currentSongIndex = if (currentSongIndex == 0) songs.size - 1 else currentSongIndex - 1
        changeSong()
    }

    private fun jumpByTime(jumpDist: Int) {
        mediaPlayer.seekTo(mediaPlayer.currentPosition + jumpDist)
    }

    private fun changeSong(forceStart: Boolean = false) {
        val isPlaying = mediaPlayer.isPlaying
        mediaPlayer.reset()
        loadSong()
        mediaPlayer.prepare()
        binding.apply {
            songName.text = songs[currentSongIndex]
            seekBar.max = mediaPlayer.duration
            seekBar.progress = 0
        }
        if (forceStart || isPlaying) { mediaPlayer.start() }
    }

    private fun loadSong() {
        val afd = assets.openFd(songs[currentSongIndex])
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
    }
}
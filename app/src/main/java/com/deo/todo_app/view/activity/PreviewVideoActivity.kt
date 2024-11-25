package com.deo.todo_app.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.deo.todo_app.R
import com.deo.todo_app.databinding.ActivityPreviewVideoBinding

class PreviewVideoActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityPreviewVideoBinding
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerView: PlayerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPreviewVideoBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        val videoUrl = intent.getStringExtra("videoUrl")
        val videoPath = intent.getStringExtra("videoPath")

        _binding.closeButton.setOnClickListener {
            finish()
        }

        if (videoUrl != null) {
            initializePlayerWithFallback(videoUrl, videoPath)
        }else if (videoPath != null) {
            initializePlayer(videoPath)
        }


    }

    private fun initializePlayerWithFallback(videoUrl: String, videoPath: String?) {
        try {
            initializePlayer(videoUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            if (videoPath != null) {
                initializePlayer(videoPath)
            } else {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializePlayer(videoUrl: String) {
        exoPlayer = ExoPlayer.Builder(this).build()

        playerView = _binding.exoPlayerView
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
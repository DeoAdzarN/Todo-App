package com.deo.todo_app.view.activity

import android.content.ContentUris
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.deo.todo_app.R
import com.deo.todo_app.databinding.ActivityPreviewVideoBinding
import com.deo.todo_app.utils.Connectivity.isInternetAvailable

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

        Log.e("videoView", "onCreate: $videoUrl , $videoPath ", )

        if (videoUrl != null) {
            initializePlayerWithFallback(videoUrl, videoPath)
        }else if (videoPath != null) {
            val mediaItem = MediaItem.fromUri(getMediaUri(videoPath))
            initializePlayer(mediaItem)
        }


    }
    fun getMediaUri(filePath: String): Uri {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = "${MediaStore.Files.FileColumns.DATA} = ?"
        val selectionArgs = arrayOf(filePath)

        val cursor = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                return ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            }
        }
        return filePath.toUri()
    }
    private fun initializePlayerWithFallback(videoUrl: String, videoPath: String?) {
        if (isInternetAvailable(this)) {
            try {
                val mediaItem = MediaItem.fromUri(videoUrl)
                initializePlayer(mediaItem)
            } catch (e: Exception) {
                e.printStackTrace()
                fallbackToVideoPath(videoPath)
            }
        } else {
            fallbackToVideoPath(videoPath)
        }
    }
    private fun fallbackToVideoPath(videoPath: String?) {
        if (videoPath != null) {
            try {
                val mediaItem = MediaItem.fromUri(getMediaUri(videoPath))
                initializePlayer(mediaItem)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Failed to play video", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, "Video path is unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializePlayer(mediaItem: MediaItem) {
        exoPlayer = ExoPlayer.Builder(this).build()

        playerView = _binding.exoPlayerView
        playerView.player = exoPlayer

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

}
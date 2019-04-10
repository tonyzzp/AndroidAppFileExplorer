package me.izzp.androidappfileexplorer

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.MediaController
import kotlinx.android.synthetic.main.afe_activity_video_viewer.*

internal class VideoViewerActivity : AppCompatActivity() {

    private val controller by lazy { MediaController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_video_viewer)
        volumeControlStream = AudioManager.STREAM_MUSIC

        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val file = uri.toFile()
        supportActionBar?.title = file.name

        afe_videoView.setVideoURI(uri)
        afe_videoView.setMediaController(controller)
        afe_videoView.setOnErrorListener { mediaPlayer, i, j ->
            Log.d(this::class.java.simpleName, "play error:+$i,$j")
            false
        }
        afe_videoView.start()


        val fragment = FileInfoFragment.create(file)
        supportFragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
        fragment.mime = "video/*"
    }

    override fun onResume() {
        super.onResume()
        controller.show()
    }
}
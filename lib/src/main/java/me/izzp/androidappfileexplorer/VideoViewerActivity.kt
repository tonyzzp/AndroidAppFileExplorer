package me.izzp.androidappfileexplorer

import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.MediaController
import kotlinx.android.synthetic.main.afe_activity_video_viewer.*

internal class VideoViewerActivity : AppCompatActivity() {

    private val controller by lazy { MediaController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_video_viewer)
        volumeControlStream = AudioManager.STREAM_MUSIC

        val file = intent.data.toFile()
        supportActionBar?.title = file.name

        afe_videoView.setVideoURI(intent.data)
        afe_videoView.start()

        afe_videoView.setMediaController(controller)

        val fragment = FileInfoFragment.create(file)
        supportFragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
        fragment.mime = "video/*"
    }

    override fun onResume() {
        super.onResume()
        controller.show()
    }
}
package me.izzp.androidappfileexplorer

import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.MediaController
import kotlinx.android.synthetic.main.afe_activity_video_viewer.*

class VideoViewerActivity : AppCompatActivity() {

    private val controller by lazy { MediaController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_video_viewer)
        volumeControlStream = AudioManager.STREAM_MUSIC

        videoView.setVideoURI(intent.data)
        videoView.start()

        videoView.setMediaController(controller)

        val fragment = FileInfoFragment.create(intent.data.toFile())
        supportFragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
    }

    override fun onResume() {
        super.onResume()
        controller.show()
    }
}
package me.izzp.androidappfileexplorer

import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_video_viewer.*

class VideoViewerActivity : AppCompatActivity() {

    private val controller by lazy { MediaController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.afe_activity_video_viewer)
        volumeControlStream = AudioManager.STREAM_MUSIC

        videoView.setVideoURI(intent.data)
        videoView.start()

        videoView.setMediaController(controller)
    }

    override fun onResume() {
        super.onResume()
        controller.show()
    }
}
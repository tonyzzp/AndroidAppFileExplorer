package me.izzp.androidappfileexplorer

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.afe_activity_audio_viewer.*

internal class AudioViewerActivity : Activity() {

    var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_audio_viewer)
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val file = uri.toFile()
        actionBar?.title = file.name
        volumeControlStream = AudioManager.STREAM_MUSIC
        player = MediaPlayer.create(this, uri)
        if (player != null) {
            val player = player!!
            player.start()
            afe_btn.setImageResource(R.drawable.afe_ic_btn_pause)
            player.setOnCompletionListener {
                afe_btn.setImageResource(R.drawable.afe_ic_btn_play)
            }
            afe_btn.setOnClickListener {
                if (player.isPlaying) {
                    player.pause()
                    afe_btn.setImageResource(R.drawable.afe_ic_btn_play)
                } else {
                    player.start()
                    afe_btn.setImageResource(R.drawable.afe_ic_btn_pause)
                }
            }
            afe_tv.text = intent.dataString
        } else {
            afe_tv.text = "读取音乐失败"
            afe_btn.gone()
        }
        val fragment = FileInfoFragment.create(file)
        fragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
        afe_btn.setImageResource(R.drawable.afe_ic_btn_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
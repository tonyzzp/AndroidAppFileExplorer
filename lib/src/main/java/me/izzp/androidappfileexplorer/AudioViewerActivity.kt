package me.izzp.androidappfileexplorer

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_audio_viewer.*

class AudioViewerActivity : AppCompatActivity() {

    var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_viewer)
        volumeControlStream = AudioManager.STREAM_MUSIC
        player = MediaPlayer.create(this, intent.data)
        if (player != null) {
            val player = player!!
            player.start()
            btn.setImageResource(R.drawable.afe_ic_btn_pause)
            player.setOnCompletionListener {
                btn.setImageResource(R.drawable.afe_ic_btn_play)
            }
            btn.setOnClickListener {
                if (player.isPlaying) {
                    player.pause()
                    btn.setImageResource(R.drawable.afe_ic_btn_play)
                } else {
                    player.start()
                    btn.setImageResource(R.drawable.afe_ic_btn_pause)
                }
            }
            tv.text = intent.dataString
        } else {
            tv.text = "读取音乐失败"
            btn.gone()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player!!.pause()
            btn.setImageResource(R.drawable.afe_ic_btn_play)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
package me.izzp.androidappfileexplorer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_text_viewer.*

class TextViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_text_viewer)

        progress.show()
        asyncFuture {
            intent.data.toFile().readText()
        }.ui {
            progress.gone()
            tv.text = it
        }
    }
}

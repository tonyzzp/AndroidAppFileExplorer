package me.izzp.androidappfileexplorer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_text_viewer.*
import java.io.File

class TextViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_viewer)

        progress.show()
        asyncFuture {
            val data = intent.dataString
            val file = File(data.substringAfter("file://"))
            file.readText()
        }.ui {
            progress.gone()
            tv.text = it
        }
    }
}

package me.izzp.androidappfileexplorer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.afe_activity_text_viewer.*

class TextViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_text_viewer)

        progress.show()
        val f = intent.data.toFile()
        asyncFuture {
            f.readText()
        }.ui {
            progress.gone()
            tv.text = it
            val fragment = FileInfoFragment.create(f)
            supportFragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
        }
    }
}
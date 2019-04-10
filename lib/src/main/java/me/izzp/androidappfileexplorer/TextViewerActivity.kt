package me.izzp.androidappfileexplorer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.afe_activity_text_viewer.*

internal class TextViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_text_viewer)
        val f = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM).toFile()
        supportActionBar?.title = f.name

        afe_progress.show()
        asyncFuture {
            f.readText()
        }.ui {
            afe_progress.gone()
            afe_tv.text = it
            val fragment = FileInfoFragment.create(f)
            supportFragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
            fragment.mime = "text/*"
        }
    }
}
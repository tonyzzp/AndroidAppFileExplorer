package me.izzp.androidappfileexplorer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.afe_activity_text_viewer.*

internal class TextViewerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_text_viewer)
        val f = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM).toFile()
        actionBar?.title = f.name

        afe_progress.show()
        asyncFuture {
            f.readText()
        }.ui {
            afe_progress.gone()
            afe_tv.text = it
            val fragment = FileInfoFragment.create(f)
            fragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
            fragmentManager
            fragment.mime = "text/*"
        }
    }
}
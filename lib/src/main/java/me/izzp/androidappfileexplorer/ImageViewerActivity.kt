package me.izzp.androidappfileexplorer

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.afe_activity_image_view.*

internal class ImageViewerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_image_view)

        val file = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM).toFile()
        actionBar?.title = file.name

        val inStream = contentResolver.openInputStream(intent.getParcelableExtra(Intent.EXTRA_STREAM))
        val bmp = BitmapFactory.decodeStream(inStream)
        inStream.close()
        img.setImageBitmap(bmp)
        val fragment = FileInfoFragment.create(file)
        fragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
        if (bmp != null) {
            fragment.mime = "image/*"
        }
    }
}
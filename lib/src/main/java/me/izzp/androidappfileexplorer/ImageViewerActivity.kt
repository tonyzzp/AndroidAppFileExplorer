package me.izzp.androidappfileexplorer

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.afe_activity_image_view.*

internal class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_image_view)

        val file = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM).toFile()
        supportActionBar?.title = file.name

        val inStream = contentResolver.openInputStream(intent.getParcelableExtra(Intent.EXTRA_STREAM))
        val bmp = BitmapFactory.decodeStream(inStream)
        inStream.close()
        img.setImageBitmap(bmp)
        val fragment = FileInfoFragment.create(file)
        supportFragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
        if (bmp != null) {
            fragment.mime = "image/*"
        }
    }
}
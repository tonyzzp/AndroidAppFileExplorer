package me.izzp.androidappfileexplorer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.afe_activity_image_view.*

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_image_view)

        val file = intent.data.toFile()
        supportActionBar!!.title = file.name

        val inStream = contentResolver.openInputStream(intent.data)
        val bmp = BitmapFactory.decodeStream(inStream)
        inStream.close()
        img.setImageBitmap(bmp)
        val fragment = FileInfoFragment.create(file)
        supportFragmentManager.beginTransaction().add(fragment, "fileinfo").commit()
    }
}
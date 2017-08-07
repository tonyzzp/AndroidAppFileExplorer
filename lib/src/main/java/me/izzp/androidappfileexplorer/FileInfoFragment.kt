package me.izzp.androidappfileexplorer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import java.io.File

/**
 * Created by zzp on 2017-08-07.
 */

class FileInfoFragment : Fragment() {

    companion object {
        fun create(file: File): FileInfoFragment {
            val fragment = FileInfoFragment()
            val args = Bundle()
            args.putString("file", file.absolutePath)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity.menuInflater.inflate(R.menu.afe_fileinfo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var rtn = false
        when (item.itemId) {
            R.id.mi_fileinfo -> {
                showInfoDialog()
                rtn = true
            }
            R.id.mi_open -> {
                rtn = true
            }
        }
        return rtn
    }

    private fun showInfoDialog() {
        val f = File(arguments.getString("file"))
        val sb = StringBuilder()
        sb.append("路径:${f.absolutePath}\n\n")
        sb.append("大小:${formatFileSize(f.length())}\n\n")
        if (f.isImageFile()) {
            val opts = BitmapFactory.Options()
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeFile(f.absolutePath, opts)
            sb.append("分辨率:${opts.outWidth} * ${opts.outHeight}")
        }
        activity.alert(null, sb.toString())
    }
}

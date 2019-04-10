package me.izzp.androidappfileexplorer

import android.app.Fragment
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import java.io.File

/**
 * Created by zzp on 2017-08-07.
 */

internal class FileInfoFragment : Fragment() {

    companion object {
        fun create(file: File): FileInfoFragment {
            val fragment = FileInfoFragment()
            val args = Bundle()
            args.putString("file", file.absolutePath)
            fragment.arguments = args
            return fragment
        }
    }

    var mime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.afe_fileinfo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var rtn = false
        when (item.itemId) {
            R.id.afe_mi_fileinfo -> {
                showInfoDialog()
                rtn = true
            }
            R.id.afe_mi_open -> {
                if (shouldShowConfirm()) {
                    val message = "文件将被复制到sd卡上，并使用外部程序打开\n文件会保存到${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/afe目录"
                    activity!!.confirmDialog(null, message, "打开", {
                        PreferenceManager.getDefaultSharedPreferences(activity)
                                .edit().putBoolean("extenal_open_confirm", false).apply()
                        open()
                    }, "取消", null)
                } else {
                    open()
                }
                rtn = true
            }
        }
        return rtn
    }

    private fun showInfoDialog() {
        val f = File(arguments!!.getString("file"))
        val sb = StringBuilder()
        sb.append("路径:${f.absolutePath}\n\n")
        sb.append("大小:${formatFileSize(f.length())}\n\n")
        if (f.isImageFile()) {
            val opts = BitmapFactory.Options()
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeFile(f.absolutePath, opts)
            sb.append("分辨率:${opts.outWidth} * ${opts.outHeight}")
        }
        activity!!.alertDialog(null, sb.toString())
    }

    private fun shouldShowConfirm(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("extenal_open_confirm", true)
    }

    private fun open() {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("正在复制文件")
        progressDialog.show()
        activity!!.asyncFuture {
            var flag = false
            val src = File(arguments!!.getString("file"))
            val dst = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "afe/${src.name}")
            try {
                src.copyTo(dst, true)
                flag = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            flag to dst
        }.ui(100) {
            progressDialog.dismiss()
            if (it.first) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().penaltyDeathOnFileUriExposure().build())
                }
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.fromFile(it.second), mime)
                startActivity(Intent.createChooser(intent, null))
            } else {
                activity!!.toastLong("复制文件失败")
            }
        }
    }
}

package me.izzp.androidappfileexplorer.demo

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Created by zzp on 2017-08-02.
 */


internal fun clearFiles(dir: File) {
    dir.walk(FileWalkDirection.BOTTOM_UP).forEach {
        if (it != dir) {
            it.delete()
        }
    }
}

internal fun makeFiles(context: Context, dir: File) {
    clearFiles(dir)

    val zipin = ZipInputStream(context.assets.open("files.zip"))
    var entry: ZipEntry? = zipin.nextEntry
    while (entry != null) {
        val f = File(dir, entry.name)
        if (entry.isDirectory) {
            f.mkdirs()
        } else {
            if (!f.exists()) {
                val parent = f.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }
                f.createNewFile()
            }
            val stream = FileOutputStream(f)
            zipin.copyTo(stream)
            stream.close()
            zipin.closeEntry()
        }
        entry = zipin.nextEntry
    }
}
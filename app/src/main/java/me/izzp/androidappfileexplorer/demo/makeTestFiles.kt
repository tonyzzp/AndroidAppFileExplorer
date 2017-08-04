package me.izzp.androidappfileexplorer.demo

import android.content.Context
import java.io.File
import java.util.*

/**
 * Created by zzp on 2017-08-02.
 */


internal fun clearFiles(dir: File) {
    dir.walk(FileWalkDirection.BOTTOM_UP).forEach {
        it.delete()
    }
}

internal fun makeFiles(context: Context, dir: File) {
    clearFiles(dir)
    val s = "abcdefghijklmnopqrstuvwxyz"
    val r = Random()
    fun randomName(): String {
        var name = ""
        for (i in 1..5) {
            name += s[r.nextInt(s.length)]
        }
        return name + System.currentTimeMillis()
    }

    for (i in 1..5) {
        val name = randomName()
        val f = File(dir, name)
        f.mkdirs()

        for (i in 1..(1 + r.nextInt(3))) {
            val name = randomName() + ".txt"
            val f = File(f, name)
            f.createNewFile()
            f.writeText("${f.absolutePath} : ${System.currentTimeMillis()}")
        }
    }

    for (i in 1..(1 + r.nextInt(3))) {
        val name = randomName() + ".txt"
        val f = File(dir, name)
        f.createNewFile()
        f.writeText("${f.absolutePath} : ${System.currentTimeMillis()}")
    }

    val names = arrayOf(
            "files/Goldberg_Variations.mp3",
            "files/Hit_Da_Stylin.mp3",
            "files/img.png",
            "files/makeTestFiles.txt"
    )

    names.forEach {
        val inStream = context.assets.open(it)
        val f = File(dir, it)
        if (!f.exists()) {
            f.createNewFile()
        }
        val outStream = f.outputStream()
        inStream.copyTo(outStream)
        outStream.close()
        inStream.close()
    }

    clearFiles(dir)
}
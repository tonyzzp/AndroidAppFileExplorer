package me.izzp.androidappfileexplorer.demo

import java.io.File
import java.util.*

/**
 * Created by zzp on 2017-08-02.
 */

internal fun makeFiles(dir: File) {
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
}
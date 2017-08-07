package me.izzp.androidappfileexplorer

import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Created by zzp on 2017-08-07.
 */
object DbUtil {
    fun tables(f: File): List<String> {
        val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val c = db.query("sqlite_master", arrayOf("name"), "type=?", arrayOf("table"), null, null, null)
        val list = ArrayList<String>()
        while (c.moveToNext()) {
            list += c.getString(0)
        }
        c.close()
        return list
    }

    fun getData(f: File, table: String): ArrayList<ArrayList<String>> {
        val db = SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val c = db.query(table, null, null, null, null, null, null)
        val count = c.columnCount
        val data = ArrayList<ArrayList<String>>()
        val title = ArrayList<String>(count)
        for (i in 1..count) {
            title += c.getColumnName(i - 1)
        }
        data += title
        while (c.moveToNext()) {
            val list = ArrayList<String>(count)
            for (i in 1..count) {
                list += c.getString(i - 1)
            }
            data += list
        }
        c.close()
        return data
    }
}
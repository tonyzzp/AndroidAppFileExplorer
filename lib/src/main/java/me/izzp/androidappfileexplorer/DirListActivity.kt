package me.izzp.androidappfileexplorer

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

internal class DirListActivity : Activity() {


    private class Holder(val itemView: View) {
        var position = 0
        val title = itemView.findViewById(android.R.id.text1) as TextView
        val desc = itemView.findViewById(android.R.id.text2) as TextView
    }

    val listView: ListView by lazy {
        ListView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(listView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val grant = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (grant != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            }
        }
        refresh()
    }

    private fun refresh() {
        val dirs = LinkedHashMap<String, String>()
        dirs["Internal Data Dir"] = filesDir.parent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getExternalFilesDirs("afe")?.forEachIndexed { index, file ->
                dirs["External Data Dir $index"] = file.parentFile.parent
            }
        } else {
            dirs["External Data Dir"] = getExternalFilesDir("afe").parentFile.parent
        }
        AppFileExplorer.customDirs.forEachIndexed { index, s ->
            dirs["Dir $index"] = s
        }
        val list = dirs.toList()
        println(list)
        listView.adapter = object : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = if (convertView != null) {
                    convertView
                } else {
                    val v = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                    val holder = Holder(v)
                    holder.itemView.setBackgroundResource(R.drawable.afe_background_selector)
                    holder.itemView.setOnClickListener {
                        val item = list[holder.position]
                        val intent = FileExplorerActivity.create(this@DirListActivity, item.second)
                        startActivity(intent)
                    }
                    v.tag = holder
                    v
                }
                val item = getItem(position)
                val holder = view.tag as Holder
                holder.position = position
                holder.title.text = item.first
                holder.desc.text = item.second
                return view
            }

            override fun getItem(p0: Int) = list[p0]

            override fun getItemId(p0: Int): Long = p0.toLong()

            override fun getCount(): Int = list.size
        }
    }
}
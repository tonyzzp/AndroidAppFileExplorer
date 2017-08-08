package me.izzp.androidappfileexplorer

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class DirListActivity : AppCompatActivity() {


    private class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById(android.R.id.text1) as TextView
        val desc = itemView.findViewById(android.R.id.text2) as TextView
    }

    val recyclerView: RecyclerView by lazy {
        RecyclerView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

        val dirs = LinkedHashMap<String, String>()
        dirs["Internal Data Dir"] = filesDir.parent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getExternalFilesDirs("afe")?.forEachIndexed { index, file ->
                dirs["External Data Dir $index"] = file.parentFile.parent
            }
        } else {
            dirs["External Data Dir"] = getExternalFilesDir("afe").parentFile.parent
        }
        val list = dirs.toList()
        println(list)
        recyclerView.adapter = object : RecyclerView.Adapter<Holder>() {
            override fun getItemCount(): Int = list.size

            override fun onBindViewHolder(holder: Holder, position: Int) {
                val item = list[holder.adapterPosition]
                holder.title.text = item.first
                holder.desc.text = item.second
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder {
                val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                view.setBackgroundResource(R.drawable.afe_background_selector)
                val holder = Holder(view)
                holder.itemView.setOnClickListener {
                    val item = list[holder.adapterPosition]
                    val intent = FileExplorerActivity.create(this@DirListActivity, item.second)
                    startActivity(intent)
                }
                return holder
            }
        }
    }
}
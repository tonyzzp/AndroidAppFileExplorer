package me.izzp.androidappfileexplorer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_dbviewer.*
import me.izzp.androidappfileexplorer.view.LockTableView

class DBViewerActivity : AppCompatActivity() {

    private inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv = itemView.findViewById(android.R.id.text1) as TextView
    }

    private inner class Adapter(val list: List<String>) : RecyclerView.Adapter<Holder>() {

        override fun getItemCount() = list.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            val holder = Holder(view)
            view.setOnClickListener {
                loadTableData(list[holder.adapterPosition])
            }
            return holder
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.tv.text = list[position]
        }
    }

    private var tables: List<String>? = null
    private var task: AsyncFuture<*>? = null
    private var contentShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dbviewer)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

        loadTableList()
    }

    private fun loadTableList() {
        if (tables == null) {
            progress.show()
            recyclerView.gone()
            content.gone()
            task = asyncFuture {
                DbUtil.tables(intent.data.toFile())
            }.ui(100) {
                contentShown = false
                task = null
                tables = it
                progress.gone()
                recyclerView.show()
                recyclerView.adapter = Adapter(it)
            }
        } else {
            progress.gone()
            content.gone()
            recyclerView.show()
            contentShown = false
        }
    }

    private fun loadTableData(table: String) {
        progress.show()
        recyclerView.gone()
        task = asyncFuture {
            DbUtil.getData(intent.data.toFile(), table)
        }.ui(100) {
            task = null
            contentShown = true
            progress.gone()
            content.show()
            val grid = LockTableView(this, content, it)
            grid.setLockFristColumn(true)
            grid.setLockFristRow(true)
            grid.setTableViewListener(object : LockTableView.OnTableViewListener {
                override fun onTableViewScrollChange(x: Int, y: Int) {
                }

                override fun onItemClick(tv: TextView) {
                    alert(null, tv.text.toString())
                }
            })
            grid.show()
        }
    }

    override fun onBackPressed() {
        if (contentShown) {
            task?.cancel()
            loadTableList()
        } else {
            if (task != null && tables != null) {
                task?.cancel()
                loadTableList()
            } else {
                super.onBackPressed()
            }
        }
    }
}
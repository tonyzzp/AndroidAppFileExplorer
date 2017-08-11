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
import kotlinx.android.synthetic.main.afe_activity_dbviewer.*
import me.izzp.androidappfileexplorer.locktableview.LockTableView
import java.io.File

internal class DBViewerActivity : AppCompatActivity() {

    private inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv = itemView.findViewById(android.R.id.text1) as TextView
    }

    private inner class Adapter(val list: List<String>) : RecyclerView.Adapter<Holder>() {

        override fun getItemCount() = list.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            view.setBackgroundResource(R.drawable.afe_background_selector)
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
    private val file: File by lazy {
        intent.data.toFile()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_dbviewer)

        supportActionBar?.title = file.name

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

        loadTableList()
    }

    private fun loadTableList() {

        fun showList(list: List<String>) {
            progress.gone()
            content.gone()
            recyclerView.show()
            recyclerView.adapter = Adapter(list)
            contentShown = false
            supportActionBar?.title = file.name
        }

        if (tables == null) {
            progress.show()
            recyclerView.gone()
            content.gone()
            task = asyncFuture {
                Thread.sleep(5000)
                DbUtil.tables(file)
            }.ui(100) {
                task = null
                tables = it
                showList(it!!)
            }
        } else {
            showList(tables!!)
        }
    }

    private fun loadTableData(table: String) {
        progress.show()
        recyclerView.gone()
        task = asyncFuture {
            DbUtil.getData(file, table)
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
                    alertDialog(null, tv.text.toString())
                }
            })
            grid.show()
            supportActionBar?.title = "${file.name} - $table (${DbUtil.count(file, table)} 条数据)"
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
package me.izzp.androidappfileexplorer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.afe_activity_dbviewer.*
import me.izzp.androidappfileexplorer.locktableview.LockTableView
import java.io.File

internal class DBViewerActivity : Activity() {

    private inner class Holder(val itemView: View) {
        var position = 0
        val tv = itemView.findViewById(android.R.id.text1) as TextView
    }

    private inner class Adapter(val list: List<String>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = if (convertView != null) {
                convertView
            } else {
                val v = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                val holder = Holder(v)
                holder.itemView.setOnClickListener {
                    table = list[holder.position]
                    index = 0
                    tableCount = DbUtil.count(file, table)
                    loadTableData()
                }
                v.tag = holder
                v
            }
            val holder = view.tag as Holder
            holder.position = position
            holder.tv.text = list[position]
            return view
        }

        override fun getItem(p0: Int) = list[p0]

        override fun getItemId(p0: Int) = p0.toLong()

        override fun getCount() = list.size
    }

    private var tables: List<String>? = null
    private var task: AsyncFuture<*>? = null
    private var contentShown = false
    private val file: File by lazy { intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM).toFile() }
    private var table: String = ""
    private var index = 0
    private var tableCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_dbviewer)

        actionBar?.title = file.name

        afe_btn_pre.setOnClickListener {
            index -= 50
            loadTableData()
        }
        afe_btn_next.setOnClickListener {
            index += 50
            loadTableData()
        }
        loadTableList()
    }

    private fun refreshButtonStatus() {
        afe_btn_pre.isEnabled = index != 0
        afe_btn_next.isEnabled = (tableCount - index) > 50
        val start = index + 1
        val total = tableCount
        val count = Math.min(50, total - start + 1)
        afe_tv_page.text = "当前页:$start-${start + count - 1}/$total"
    }

    private fun loadTableList() {
        fun showList(list: List<String>) {
            afe_progress.gone()
            contentPanel.gone()
            afe_recyclerView.show()
            afe_recyclerView.adapter = Adapter(list)
            contentShown = false
            actionBar?.title = file.name
        }

        if (tables == null) {
            afe_progress.show()
            afe_recyclerView.gone()
            contentPanel.gone()
            task = asyncFuture {
                var rtn: List<String>? = null
                try {
                    rtn = DbUtil.tables(file)
                } catch (e: Exception) {
                }
                rtn
            }.ui(100) {
                task = null
                if (it == null) {
                    alertDialog(null, "打开数据库失败") {
                        finish()
                    }
                } else {
                    tables = it
                    showList(it!!)
                }
            }
        } else {
            showList(tables!!)
        }
    }

    private fun loadTableData() {
        afe_progress.show()
        afe_recyclerView.gone()
        contentPanel.gone()
        task = asyncFuture {
            DbUtil.getData(file, table, index, 50)
        }.ui(100) {
            task = null
            contentShown = true
            afe_progress.gone()
            contentPanel.show()
            refreshButtonStatus()
            val grid = LockTableView(this, afe_table_container, it)
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
            actionBar?.title = "${file.name} - $table"
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
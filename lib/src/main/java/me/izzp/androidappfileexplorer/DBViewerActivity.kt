package me.izzp.androidappfileexplorer

import android.content.Intent
import android.net.Uri
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            view.setBackgroundResource(R.drawable.afe_background_selector)
            val holder = Holder(view)
            view.setOnClickListener {
                table = list[holder.adapterPosition]
                index = 0
                count = DbUtil.count(file, table)
                loadTableData()
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
    private val file: File by lazy { intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM).toFile() }
    private var table: String = ""
    private var index = 0
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_dbviewer)

        supportActionBar?.title = file.name

        afe_recyclerView.layoutManager = LinearLayoutManager(this)
        afe_recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

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
        afe_btn_next.isEnabled = (count - index) > 50
        val start = index + 1
        val total = count
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
            supportActionBar?.title = file.name
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
            supportActionBar?.title = "${file.name} - $table"
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
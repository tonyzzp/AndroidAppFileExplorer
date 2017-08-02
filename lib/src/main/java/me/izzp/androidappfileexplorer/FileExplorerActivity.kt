package me.izzp.androidappfileexplorer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_file_explorer.*
import java.io.File

class FileExplorerActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context, dir: String): Intent {
            val intent = Intent(context, FileExplorerActivity::class.java)
            intent.putExtra("dir", dir)
            return intent
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById(R.id.iv_icon) as ImageView
        val name = itemView.findViewById(R.id.tv_name) as TextView
    }

    inner class Adapter(val list: List<File>) : RecyclerView.Adapter<Holder>() {
        override fun onBindViewHolder(holder: Holder, position: Int) {
            val f = itemAt(position)
            holder.name.text = f.name
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = layoutInflater.inflate(R.layout.file_explorer_list_item, parent, false)
            val holder = Holder(view)
            holder.itemView.setOnClickListener {
                toastShort("点击了:${itemAt(holder.adapterPosition).name}")
            }
            return holder
        }

        override fun getItemCount(): Int = list.size

        private fun itemAt(position: Int) = list[position]
    }

    val rootDir: File by lazy {
        File(intent.getStringExtra("dir"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        if (rootDir.isFile) {
            showError("$rootDir 是个文件")
        } else if (!rootDir.exists()) {
            showError("$rootDir 不存在")
        } else {
            showDir(rootDir)
        }
    }

    private fun showError(error: String) {
        tv_error.text = error
        tv_error.show()
        recyclerView.gone()
    }

    private fun showDir(dir: File) {
        val list = dir.listFiles()
        if (list == null) {
            showError("访问 $dir 出错")
        } else if (list.isEmpty()) {
            showError("$dir 目录为空")
        } else {
            showList(list.toList())
        }
    }

    private fun showList(list: List<File>) {
        recyclerView.adapter = Adapter(list)
        recyclerView.show()
        tv_error.gone()
    }
}

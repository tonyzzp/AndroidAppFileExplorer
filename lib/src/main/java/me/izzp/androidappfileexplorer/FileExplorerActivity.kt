package me.izzp.androidappfileexplorer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_file_explorer.*
import java.io.File
import kotlin.properties.Delegates

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
            var img = R.drawable.afe_ic_fileicon_file_light
            if (f.isDirectory) {
                img = R.drawable.afe_ic_fileicon_folder_light
            } else if (f.isImageFile()) {
                img = R.drawable.afe_ic_fileicon_image_light
            } else if (f.isVideoFile()) {
                img = R.drawable.afe_ic_fileicon_video_light
            } else if (f.isAudioFile()) {
                img = R.drawable.afe_ic_fileicon_audio_light
            } else if (f.isTextFile()) {
                img = R.drawable.afe_ic_fileicon_text_light
            }
            holder.icon.setImageResource(img)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = layoutInflater.inflate(R.layout.file_explorer_list_item, parent, false)
            val holder = Holder(view)
            holder.itemView.setOnClickListener {
                val f = itemAt(holder.adapterPosition)
                if (f.isDirectory) {
                    showDir(f)
                } else {
                    openFile(f, holder)
                }
            }
            return holder
        }

        override fun getItemCount(): Int = list.size

        private fun itemAt(position: Int) = list[position]
    }

    val rootDir: File by lazy {
        File(intent.getStringExtra("dir"))
    }
    val actionBar: ActionBar by lazy {
        supportActionBar!!
    }

    var currentDir: File by Delegates.notNull<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        currentDir = rootDir
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
        actionBar.title = dir.name
        currentDir = dir
        val list = dir.listFiles().sortByName()
        if (list == null) {
            showError("访问 $dir 出错")
        } else if (list.isEmpty()) {
            showError("$dir 目录为空")
        } else {
            showList(list.toList())
        }
    }

    private fun openFile(f: File, holder: Holder) {
        var type: String = when {
            f.isTextFile() -> "text/*"
            f.isImageFile() -> "image/*"
            f.isAudioFile() -> "audio/*"
            f.isVideoFile() -> "video/*"
            else -> ""
        }

        fun open() {
            if (type != "") {
                val intent = Intent(ACTION_VIEW)
                intent.setDataAndType(Uri.fromFile(f), type)
                val opts = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        holder.icon,
                        ViewCompat.getTransitionName(holder.icon)
                )
                val bundle = opts.toBundle()
                ActivityCompat.startActivity(this, intent, bundle)
            }
        }

        if (type == "") {
            val strs = arrayOf("文本", "图片", "音频", "视频")
            showItemsDialog(strs) { dialog, which ->
                type = when (which) {
                    0 -> "text/*"
                    1 -> "image/*"
                    2 -> "audio/*"
                    3 -> "video/*"
                    else -> ""
                }
                open()
            }
        } else {
            open()
        }
    }

    private fun showList(list: List<File>) {
        recyclerView.adapter = Adapter(list)
        recyclerView.show()
        tv_error.gone()
    }

    override fun onBackPressed() {
        if (currentDir != rootDir) {
            showDir(currentDir.parentFile)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
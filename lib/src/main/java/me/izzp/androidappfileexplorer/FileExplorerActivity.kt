package me.izzp.androidappfileexplorer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import java.io.File
import kotlin.properties.Delegates

internal class FileExplorerActivity : Activity(), ActionMode.Callback {

    companion object {
        fun create(context: Context, dir: String): Intent {
            val intent = Intent(context, FileExplorerActivity::class.java)
            intent.putExtra("dir", dir)
            return intent
        }
    }

    private class Item(val type: Int, val file: File) {
        companion object {
            const val TYPE_FILE = 2
            const val TYPE_RETURN = 3
        }

        val desc: String by lazy {
            var result = ""
            if (file.isFile) {
                val time = file.lastModified()
                val size = formatFileSize(file.length())
                result = "修改时间:${dateTime(time)}  大小:$size"
            }
            result
        }
    }

    inner class Holder(val itemView: View) {
        var position: Int = 0
        val icon = itemView.findViewById<ImageView>(R.id.afe_iv_icon)
        val name = itemView.findViewById<TextView>(R.id.afe_tv_name)
        val path = itemView.findViewById<TextView>(R.id.afe_tv_path)
        val checkbox = itemView.findViewById<CheckBox>(R.id.afe_checkbox)
    }

    private inner class Adapter(val list: List<Item>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = if (convertView != null) {
                convertView
            } else {
                val v = layoutInflater.inflate(R.layout.afe_file_explorer_list_item, parent, false)
                val holder = Holder(v)
                holder.itemView.setOnClickListener {
                    val item = getItem(holder.position)
                    if (isActionMode) {
                        if (item.type != Item.TYPE_RETURN) {
                            val file = item.file
                            if (selected.contains(file)) {
                                selected.remove(file)
                            } else {
                                selected.add(file)
                            }
                            (listView.adapter as BaseAdapter?)?.notifyDataSetChanged()
                        }
                    } else {
                        val type = item.type
                        val f = item.file
                        if (type == Item.TYPE_FILE) {
                            if (f.isDirectory) {
                                showDir(f)
                            } else {
                                openFile(f, holder)
                            }
                        } else if (type == Item.TYPE_RETURN) {
                            showDir(f)
                        }
                    }
                }
                holder.itemView.setOnLongClickListener {
                    val item = getItem(holder.position)
                    if (item.type != Item.TYPE_RETURN) {
                        startActionMode(this@FileExplorerActivity)
                        selected.add(item.file)
                        (listView.adapter as BaseAdapter?)?.notifyDataSetChanged()
                    }
                    true
                }
                holder.checkbox.setOnClickListener {
                    holder.itemView.performClick()
                }
                v.tag = holder
                v
            }
            val holder = view.tag as Holder
            holder.position = position
            val item = getItem(position)
            val f = item.file
            val type = item.type
            if (type == Item.TYPE_FILE) {
                holder.name.text = f.name
                val desc = item.desc
                if (desc.isEmpty()) {
                    holder.path.gone()
                } else {
                    holder.path.text = desc
                }
                val img = when {
                    f.isDirectory -> R.drawable.afe_ic_fileicon_folder_light
                    f.isImageFile() -> R.drawable.afe_ic_fileicon_image_light
                    f.isVideoFile() -> R.drawable.afe_ic_fileicon_video_light
                    f.isAudioFile() -> R.drawable.afe_ic_fileicon_audio_light
                    f.isTextFile() -> R.drawable.afe_ic_fileicon_text_light
                    else -> R.drawable.afe_ic_fileicon_file_light
                }
                holder.icon.setImageResource(img)
                holder.checkbox.isChecked = selected.contains(f)
            } else if (type == Item.TYPE_RETURN) {
                holder.icon.setImageResource(R.drawable.afe_ic_fileicon_return_light)
                holder.name.text = f.name
                holder.path.text = f.absolutePath
                holder.path.show()
            }
            holder.checkbox.visibility = if (isActionMode) View.VISIBLE else View.GONE
            if (type == Item.TYPE_RETURN) {
                holder.checkbox.visibility = View.GONE
            }
            return view
        }

        override fun getItem(p0: Int) = list[p0]

        override fun getItemId(p0: Int) = p0.toLong()

        override fun getCount() = list.size
    }

    val rootDir: File by lazy { File(intent.getStringExtra("dir")) }
    var currentDir: File by Delegates.notNull()
    private val listView by lazy { findViewById<ListView>(R.id.afe_recyclerView) }
    private val tv_error by lazy { findViewById<TextView>(R.id.afe_tv_error) }
    private val selected = mutableListOf<File>()
    private var isActionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.afe_activity_file_explorer)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        currentDir = rootDir
        refresh()
    }

    private fun refresh() {
        if (currentDir.isFile) {
            showError("$currentDir 是个文件")
        } else if (!currentDir.exists()) {
            showError("$currentDir 不存在")
        } else {
            showDir(currentDir)
        }
    }

    private fun showError(error: String) {
        tv_error.text = error
        tv_error.show()
        listView.gone()
    }

    private fun showDir(dir: File) {
        actionBar?.title = dir.name
        currentDir = dir
        val list = dir.listFiles()?.sortByName()
        if (list == null) {
            showError("访问 $dir 出错")
        } else {
            val data = ArrayList<Item>()
            if (dir != rootDir) {
                data.add(Item(Item.TYPE_RETURN, dir.parentFile))
            }
            list.forEach {
                data.add(Item(Item.TYPE_FILE, it))
            }
            showList(data)
        }
    }

    private fun openFile(f: File, holder: Holder) {
        var type: String = when {
            f.isTextFile() -> "text/*"
            f.isImageFile() -> "image/*"
            f.isAudioFile() -> "audio/*"
            f.isVideoFile() -> "video/*"
            f.isDatabase() -> "db/*"
            else -> ""
        }

        fun open() {
            if (type != "") {
                val intent = Intent(ACTION_VIEW)
                intent.`package` = packageName
                intent.type = type
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f))
                startActivity(intent)
            }
        }

        if (type == "") {
            val strs = arrayOf("文本", "图片", "音频", "视频", "数据库")
            showItemsDialog(strs) { _, which ->
                type = when (which) {
                    0 -> "text/*"
                    1 -> "image/*"
                    2 -> "audio/*"
                    3 -> "video/*"
                    4 -> "db/*"
                    else -> ""
                }
                open()
            }
        } else {
            open()
        }
    }

    private fun showList(list: List<Item>) {
        listView.adapter = Adapter(list)
        listView.show()
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

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.afe_mi_delete -> {
                selected.forEach {
                    it.deleteRecursively()
                }
            }
        }
        mode.finish()
        refresh()
        return true
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        menuInflater.inflate(R.menu.afe_explorer_actionmode, menu)
        isActionMode = true
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = true

    override fun onDestroyActionMode(mode: ActionMode) {
        selected.clear()
        isActionMode = false
        val adapter = listView.adapter
        if (adapter is BaseAdapter) {
            adapter.notifyDataSetChanged()
        }
    }
}
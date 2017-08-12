package me.izzp.androidappfileexplorer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import java.io.DataInputStream
import java.io.File
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.util.*
import kotlin.jvm.internal.Ref

/**
 * Created by zzp on 2017-08-02.
 */

internal fun View?.show() {
    this?.visibility = View.VISIBLE
}

internal fun View?.hide() {
    this?.visibility = View.INVISIBLE
}

internal fun View?.gone() {
    this?.visibility = View.GONE
}

internal fun Context.toastShort(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

internal fun Context.toastLong(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}

internal fun Activity.alertDialog(title: String?, message: String?, positiveListener: (() -> Unit)? = null) {
    AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(positiveListener == null)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                positiveListener?.invoke()
            }
            .show()
}

internal fun Activity.confirmDialog(
        title: String?,
        message: String?,
        positiveText: String,
        positiveListener: (() -> Unit)?,
        negativeText: String,
        negativeListener: (() -> Unit)?) {
    AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText, { _, _ ->
                positiveListener?.invoke()
            })
            .setNegativeButton(negativeText, { _, _ ->
                negativeListener?.invoke()
            })
            .show()
}

internal fun formatFileSize(size: Long): String {
    if (size < 1024) {
        return "$size B"
    } else if (size < 1024 * 1024) {
        return String.format("%.2fKB", size / 1024f)
    } else {
        val mb = size / 1024f / 1024f
        return String.format("%.2fMB", mb)
    }
}

internal val File.extension: String
    get() = name.substringAfterLast(".", "")

internal fun Collection<String>.containsIgnoreCase(s: String): Boolean {
    val s = s.toLowerCase()
    forEach {
        if (s == it.toLowerCase()) {
            return true
        }
    }
    return false
}

internal fun File.isTextFile(): Boolean = listOf("txt", "xml", "md").containsIgnoreCase(extension)

internal fun File.isImageFile(): Boolean = listOf("jpg", "png", "webp").containsIgnoreCase(extension)

internal fun File.isAudioFile(): Boolean = listOf("mp3", "ogg", "wav", "wma", "flac").containsIgnoreCase(extension)

internal fun File.isVideoFile(): Boolean = listOf("mp4", "avi", "3gp", "wmv").containsIgnoreCase(extension)

internal fun File.getMIME(): String? = when {
    isTextFile() -> "text/*"
    isImageFile() -> "image/*"
    isAudioFile() -> "audio/*"
    isVideoFile() -> "video/*"
    else -> null
}

internal fun File.isDatabase(): Boolean {
    if (!isFile) {
        return false
    }
    val parent = parentFile.name
    if (parent != "databases") {
        return false
    }
    val inStream = DataInputStream(inputStream())
    val magic = inStream.readInt()
    inStream.close()
    return magic == 0x53514c69
}

internal const val ACTION_VIEW = "me.izzp.androidappfileexplorer.ACTION_VIEW"

internal fun Activity.showItemsDialog(items: Array<String>, listener: (dialog: DialogInterface, witch: Int) -> Unit): AlertDialog {
    return AlertDialog.Builder(this).setItems(items, listener).show()
}

internal fun Uri.toFile(): File = File(URLDecoder.decode(toString().substringAfter("file://"), "UTF-8"))

/**
 * 对File按名字排序，文件夹排前面
 *
 * @return this
 */
internal fun Array<File>?.sortByName(): Array<File>? {
    Arrays.sort(this) { a, b ->
        val rtn: Int
        if (a.isDirectory and b.isFile) {
            rtn = -1
        } else if (a.isFile and b.isDirectory) {
            rtn = 1
        } else {
            val a = a.name
            val b = b.name
            rtn = a.compareTo(b)
        }
        rtn
    }
    return this
}

internal fun File.mkParentDirs() {
    if (!exists()) {
        val parent = parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }
    }
}

private val handler: Handler by lazy {
    Handler(Looper.getMainLooper())
}

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
internal open class ActivityLifeCycleAdapter : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }
}

internal class AsyncFuture<T>(act: Activity, val ref: Ref.ObjectRef<T>) {

    private var m: ((t: T) -> Unit)? = null
    private var delay = 0L
    private val actref = WeakReference(act)
    private val app: Application = act.application
    private var cb: ActivityLifeCycleAdapter? = null
    private var cancel = false

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            cb = object : ActivityLifeCycleAdapter() {
                @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                override fun onActivityDestroyed(activity: Activity) {
                    super.onActivityDestroyed(activity)
                    val act = actref.get()
                    if (activity == act) {
                        release()
                    }
                }
            }
            app.registerActivityLifecycleCallbacks(cb)
        }
    }

    fun ui(delay: Long = 0L, block: (t: T) -> Unit): AsyncFuture<T> {
        this.delay = delay
        m = block
        return this
    }

    fun cancel() {
        cancel = true
    }

    private fun release() {
        if (cb != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            app.unregisterActivityLifecycleCallbacks(cb)
        }
        cb = null
        m = null
    }

    internal fun post() {
        fun valid(): Boolean {
            val a = actref.get()
            return !cancel && m != null && a != null && !a.isFinishing
        }
        if (valid()) {
            handler.postDelayed({
                if (valid()) {
                    m!!.invoke(ref.element)
                }
                release()
            }, delay)
        } else {
            release()
        }
    }
}

internal fun <T> Activity.asyncFuture(block: () -> T): AsyncFuture<T> {
    val ref = Ref.ObjectRef<T>()
    val future = AsyncFuture(this, ref)
    Thread {
        val t = block()
        ref.element = t
        future.post()
    }.start()
    return future
}
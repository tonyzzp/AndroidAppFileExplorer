package me.izzp.androidappfileexplorer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import java.io.File
import java.lang.ref.WeakReference
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

internal val File.extension: String
    get() = name.substringAfter(".", "")

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

internal const val ACTION_VIEW = "me.izzp.androidappfileexplorer.ACTION_VIEW"

internal fun Activity.showItemsDialog(items: Array<String>, listener: (dialog: DialogInterface, witch: Int) -> Unit): AlertDialog {
    return AlertDialog.Builder(this).setItems(items, listener).show()
}

internal fun Uri.toFile(): File = File(toString().substringAfter("file://"))

/**
 * 对File按名字排序，文件夹排前面
 *
 * @return this
 */
internal fun Array<File>?.sortByName(): Array<File>? {
    Arrays.sort(this) { a, b ->
        var rtn: Int
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

private val handler: Handler by lazy {
    Handler(Looper.getMainLooper())
}

internal open class ActivityLifeCycleAdapter : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
    }

}

internal class AsyncFuture<T>(act: Activity, val ref: Ref.ObjectRef<T>) {


    private var m: ((t: T) -> Unit)? = null
    private val actref = WeakReference(act)
    private val app: Application = act.application
    private var cb: ActivityLifeCycleAdapter? = null

    init {
        cb = object : ActivityLifeCycleAdapter() {
            override fun onActivityDestroyed(activity: Activity?) {
                super.onActivityDestroyed(activity)
                release()
            }
        }
        app.registerActivityLifecycleCallbacks(cb)
    }

    fun ui(block: (t: T) -> Unit) {
        m = block
    }

    private fun release() {
        if (cb != null) {
            app.unregisterActivityLifecycleCallbacks(cb)
        }
        cb = null
        m = null
    }

    internal fun post() {
        fun valid(): Boolean {
            val a = actref.get()
            return m != null && a != null && !a.isFinishing
        }
        if (valid()) {
            handler.post {
                if (valid()) {
                    m!!.invoke(ref.element)
                }
                release()
            }
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
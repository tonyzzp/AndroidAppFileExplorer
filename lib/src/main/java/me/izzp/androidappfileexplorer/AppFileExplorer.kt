package me.izzp.androidappfileexplorer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat

/**
 * Created by zzp on 2017-08-08.
 */
object AppFileExplorer {

    const val CHANNEL_ID = "AppFileExplorer"

    val customDirs = ArrayList<String>()

    /**
     * 增加一些需要显示的目录
     */
    @JvmStatic
    fun addDirs(dirs: List<String>) {
        customDirs += dirs
    }

    /**
     * 打开文件浏览器
     */
    @JvmStatic
    fun open(context: Context) {
        val intent = Intent(context, DirListActivity::class.java)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * 在通知栏显示入口
     */
    @JvmStatic
    fun showNotification(context: Context) {
        val context = context.applicationContext
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = mgr.getNotificationChannel(CHANNEL_ID)
            if (channel == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.enableLights(false)
                channel.enableVibration(false)
                mgr.createNotificationChannel(channel)
            }
        }
        val intent = Intent(context, DirListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val noti = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("点击打开FileExplorer")
                .setDefaults(0)
                .setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .build()
        mgr.notify(R.id.afe_noti, noti)
    }

    /**
     * 取消通知栏入口
     */
    @JvmStatic
    fun dismissNotification(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.cancel(R.id.afe_noti)
    }
}
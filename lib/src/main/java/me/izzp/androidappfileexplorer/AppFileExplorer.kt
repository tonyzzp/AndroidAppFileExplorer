package me.izzp.androidappfileexplorer

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat

/**
 * Created by zzp on 2017-08-08.
 */
object AppFileExplorer {

    @JvmStatic
    fun open(context: Context) {
        val intent = Intent(context, DirListActivity::class.java)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    @JvmStatic
    fun showNotification(context: Context) {
        val intent = Intent(context, DirListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val noti = NotificationCompat.Builder(context)
                .setContentTitle("点击打开FileExplorer")
                .setOngoing(true)
                .setDefaults(0)
                .setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .build()
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(R.id.noti, noti)
    }

    @JvmStatic
    fun dismissNotification(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.cancel(R.id.noti)
    }
}
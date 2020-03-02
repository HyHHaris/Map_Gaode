package com.a01tech.map_gaode

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder

class NotifService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = Notification.Builder(applicationContext)
        val nfIntent = Intent(this, MainActivity::class.java)

        builder.setContentIntent(
            PendingIntent.getActivity(this, 0, nfIntent, 0)
        ) // 设置PendingIntent
//    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_large)) // 设置下拉列表中的图标(大图标).setContentTitle("下拉列表中的Title") // 设置下拉列表里的标题
            .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
            .setContentText("要显示的内容") // 设置上下文内容
            .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        val notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        return super.onStartCommand(intent, flags, startId);
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }
}
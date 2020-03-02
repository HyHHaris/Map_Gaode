package com.a01tech.map_gaode.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import com.a01tech.map_mz.IMyAidlInterface;

/**
 * content:后台运行的服务
 * Actor:韩小呆
 * Time:2018/5/3
 */
public class RemoteService extends Service {
    MyConn conn;
    MyBinder binder;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        conn = new MyConn();
        binder = new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        this.bindService(new Intent(this, MainService.class), conn, Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return RemoteService.class.getSimpleName();
        }
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Intent intent = new Intent(RemoteService.this, MainService.class);
            //开启本地服务
            if (Build.VERSION.SDK_INT >= 26) {
//               \\适配8.0机制
                RemoteService.this.startForegroundService(intent);
            } else {
                RemoteService.this.startService(intent);
            }
            //绑定本地服务
            RemoteService.this.bindService(new Intent(RemoteService.this, MainService.class), conn, Context.BIND_IMPORTANT);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(RemoteService.this, MainService.class);
        //开启本地服务
        if (Build.VERSION.SDK_INT >= 26) {
//           \\适配8.0机制
            RemoteService.this.startForegroundService(intent);
        } else {
            RemoteService.this.startService(intent);
        }
        //绑定本地服务
        RemoteService.this.bindService(new Intent(RemoteService.this, MainService.class), conn, Context.BIND_IMPORTANT);

    }
}

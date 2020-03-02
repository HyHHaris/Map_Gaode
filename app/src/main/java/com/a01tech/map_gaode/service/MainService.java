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
public class MainService extends Service {
    MyBinder binder;
    MyConn conn;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MyBinder();
        conn = new MyConn();
    }


    class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return MainService.class.getSimpleName();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.bindService(new Intent(MainService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Intent intent = new Intent(MainService.this, RemoteService.class);
            if (Build.VERSION.SDK_INT >= 26) {
//                  \\适配8.0机制
                MainService.this.startForegroundService(intent);
            } else {
                MainService.this.startService(intent);
            }
            //绑定远程服务
            MainService.this.bindService(new Intent(MainService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(MainService.this, RemoteService.class);
//        \\适配8.0机制
        if (Build.VERSION.SDK_INT >= 26) {
            MainService.this.startForegroundService(intent);

        } else {
            MainService.this.startService(intent);
        }
        MainService.this.bindService(new Intent(MainService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);
    }
}

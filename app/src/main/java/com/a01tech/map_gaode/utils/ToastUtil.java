package com.a01tech.map_gaode.utils;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * @class describe:解决Toast重复弹出
 */
public class ToastUtil {


    private static String oldMsg;
    private static Toast toast   = null;
    private static long  oneTime = 0;
    private static long  twoTime = 0;

    public static void showToast(Context context, String s) {
        if (s != null) {
            if (toast == null) {
                toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
                try {
                    LinearLayout view = (LinearLayout) toast.getView();
                    view.setGravity(View.TEXT_ALIGNMENT_CENTER);
                } catch (Exception e) {
                    RelativeLayout view = (RelativeLayout) toast.getView();
                    view.setGravity(RelativeLayout.CENTER_IN_PARENT);
                }
                toast.show();
                oneTime = System.currentTimeMillis();
            } else {
                twoTime = System.currentTimeMillis();
                if (s.equals(oldMsg)) {
                    if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                        toast.show();
                    }
                } else {
                    oldMsg = s;
                    toast.setText(s);
                    toast.show();
                }
            }
            oneTime = twoTime;
        }
    }


    public static void showToast(Context context, int resId) {
        showToast(context, context.getString(resId));
    }


}

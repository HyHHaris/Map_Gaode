package com.a01tech.map_gaode

import android.app.Application
import android.content.Context
import android.content.Intent
import com.a01tech.map_gaode.utils.SPUtil
import com.a01tech.map_gaode.utils.ToastUtil
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.google.gson.Gson

class MapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        XLog.init(if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE)
        gson = Gson()

        mContext = this
    }

    companion object {
        lateinit var gson: Gson
        lateinit var user: User

        fun refreshUser(context: Context) {
            user = Gson().fromJson(SPUtil.get(context, "user", "") as String, User::class.java)
        }

        fun getUser(context: Context): User {
            val s = SPUtil.get(context, "user", "") as String
            if (s.isEmpty()) {
                return User()
            }
            user = Gson().fromJson(s, User::class.java)
            return user
        }

        private var mContext: Context? = null
        fun restart() {
            ToastUtil.showToast(mContext, "请重新登录")
            mContext?.startActivity(Intent(mContext, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }


}

/**
 * @param role 1 管理员  2 巡查员
 */
data class User(
    var username: String = "",
    var token: String = "",
    var role: Int = 2, var nickName: String = "", var password: String = "", var headImg: String = "",
    var cbtd: Int = 0,
    var yzsdrk: Int = 0,
    var zztd: Int = 0,
    var bytd1: Int = 0,
    var bytd2: Int = 0,
    var zdhx: Int = 0
) {
    override fun toString(): String {
        return "User(username='$username', token='$token', role=$role, nickName='$nickName')"
    }
}
package com.a01tech.map_gaode.fragments


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.a01tech.map_gaode.ArgeenmentActivity
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.SetPasswordActivity
import com.a01tech.map_gaode.SplashActivity
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.ToastUtil
import com.google.gson.annotations.SerializedName
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_setting.*


class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnExit.setOnClickListener {
            //            activity?.onBackPressed()
            startActivity(Intent(context, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        btnBmxy.setOnClickListener {
            startActivity(Intent(context, ArgeenmentActivity::class.java))
        }


        textVersion.text = "App版本号：${getVersionName(context!!)}"

        btnCheck.setOnClickListener {
            RetrofitUtil.getService()
                .checkVersion()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe({
                    if (it.code == 1) {
                        val version = it.getData("appVersion", AppVersion::class.java)
                        if (version != null) {
                            if (version.androidVersion == getVersionCode(context!!)) {
                                ToastUtil.showToast(context, "已经是最新版本")
                            } else {
                                QMUIDialog.MessageDialogBuilder(context)
                                    .setTitle("版本更新")
                                    .setMessage("检测到新版本，是否更新？")
                                    .addAction("取消") { a, b ->
                                        a.dismiss()
                                    }.addAction("更新") { a, b ->
                                        a.dismiss()
                                        try {
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(version.path/*"http://www.baidu.com"*/)
                                                )
                                            )
                                        } catch (e: Exception) {
                                            ToastUtil.showToast(context, "地址错误")
                                        }
                                    }.create()
                                    .show()
                            }
                        }
                    } else {
                        ToastUtil.showToast(context, it.msg)
                    }

                }, {
                    it.printStackTrace()
                    ToastUtil.showToast(context, "查询失败")
                })
        }

        btnResetPassword.setOnClickListener {
            startActivity(Intent(context, SetPasswordActivity::class.java))
        }
    }

    data class AppVersion(
        @SerializedName("androidVersion")
        var androidVersion: Int = 0, // 11
        @SerializedName("appUpdateContent")
        var appUpdateContent: String = "", // 11
        @SerializedName("appVersion")
        var appVersion: String = "", // 11
        @SerializedName("createTime")
        var createTime: Long = 0, // 1561456038000
        @SerializedName("id")
        var id: String = "", // 590276846447
        @SerializedName("path")
        var path: String = "" // 11
    )

    /**
     * 获取当前本地apk的版本
     *
     * @param mContext
     * @return
     */
    fun getVersionCode(mContext: Context): Int {
        var versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
//            e.printStackTrace();
        }
        return versionCode
    }

    fun getVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            ""
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            SettingFragment()
    }
}

package com.a01tech.map_gaode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.*
import com.a01tech.map_gaode.utils.DialogUtil.genLoadingDialog
import com.elvishew.xlog.XLog
import com.google.gson.annotations.SerializedName
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.concurrent.TimeUnit


class SplashActivity : AppCompatActivity() {

    lateinit var d: QMUITipDialog
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        /*  */
        webView.loadUrl("file:///android_asset/xy.htm")
        webView.webChromeClient = WebChromeClient()
/*
        btnStart.setOnClickListener {
            requestPermissions()
        }*/

        btnStart.setOnClickListener {
            MapApp.refreshUser(this)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

        QMUIDisplayHelper.setFullScreen(this)
        requestPermissions()

        val dialog = genLoadingDialog(this)

        d = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord("初始化...")
            .create()

        d.show()
        requestPermissions()
        btnXy.setOnClickListener {
            startActivity(Intent(this, ArgeenmentActivity::class.java))
        }
        val nowUser = MapApp.getUser(this)

        if (nowUser.username?.isNotEmpty()) {
            edtUsername.setText(nowUser.username)

            if (nowUser.password?.isNotEmpty()) {
                edtPassword.setText(nowUser.password)
                cbRemember.isChecked = true
            } else {
                cbRemember.isChecked = false
            }
        }
        val desUtil = DesUtil("zy2dUKlG", "3fTrP5xE")
        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                dialog.show()
                RetrofitUtil.getService()
                    .login(username, desUtil.encode(password).apply {
                        XLog.e(this)
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { JsonData(it) }
                    .subscribe({

                        if (it.code == 1) {
                            val map = it.obj.getAsJsonObject("map")
                            MapApp.user = User(
                                username, map.get("token").asString
                                , map.get("role").asInt
//                                , 1
                                , map.get("nickName").asString
                                , if (cbRemember.isChecked)
                                    password else ""
                                , map.get("headImg").asString
                                , map.get("cbtd").asInt
                                , map.get("yzsdrk").asInt
                                , map.get("zztd").asInt
                                , map.get("bytd1").asInt
                                , map.get("bytd2").asInt
                                ,map.get("zdhx").asInt
                            ).apply {
                                XLog.e(this.toString())
                                SPUtil.setParam(
                                    this@SplashActivity, "user", MapApp.gson.toJson(
                                        this
                                    )
                                )
                            }


                            val version = try {
                                map.get("time").asLong
                            } catch (e: Exception) {
                                0L
                            }
                            var currentVersion = SPUtil.get(this, "version", 0L) as Long
                            if (version > currentVersion) {
                                val dia = DialogUtil.genLoadingDialog(this, "加载地块数据...")
                                dia.show()
                                RetrofitUtil.getService()
                                    .getAllLand(MapApp.user.token)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .map { JsonData(it) }
                                    .subscribe({
                                        dia.dismiss()
                                        val get = it.obj.toString()
//                                        val desUtil = DesUtil("zy2dUKlG","3fTrP5xE")
                                        XLog.e(get)


                                        SPUtil.setParam(
                                            this, "land", get/*desUtil.encode(get).apply {
//                                            XLog.e(this)
                                        }*/
                                        )
//                                   resources.assets.open("data5.txt")
//                                    assets.

                                        SPUtil.setParam(this, "version", version)
                                        startMain()

                                    }, {
                                        it.printStackTrace()
                                        dia.dismiss()
                                    })
                            } else {
                                startMain()
                            }


                        }

                        ToastUtil.showToast(this, it.msg)

                        dialog.dismiss()
                        XLog.e(it)
                    }, {
                        ToastUtil.showToast(this, "ERROR")
                        dialog.dismiss()
                        it.printStackTrace()
                    })
            }
        }



        checkVersion()
    }

    @SuppressLint("CheckResult")
    private fun checkVersion() {
        val v = getVersionCode(this)
        val l = SPUtil.get(this, "IntroVersion", 0) as Int
//        val l = 0
        if (v > l) {
            RetrofitUtil.getService()
                .getGuide()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe({
                    if (it.code == 1) {
                        val guides = it.getDataList("guidePages", Guide::class.java)
                        val s = guides.toMutableList()
                            .sortedBy { it.imgOrder }
                            .map {
                                it.img
                            }.reduceIndexed { index, acc, s ->
                                if (index == 0) {
                                    acc + s
                                } else {
                                    ",$acc$s"
                                }
                            }

                        startActivity(Intent(this, IntroActivity::class.java).apply {
                            putExtra("path", s)
                        })
                        finish()
                    } else {
                        ToastUtil.showToast(this, it.msg)
                    }
                }, { ToastUtil.showToast(this, "获取引导数据失败") })
        }

    }

    data class Guide(
        @SerializedName("createTime")
        var createTime: Long = 0, // 1561565285000
        @SerializedName("id")
        var id: String = "", // 412587963254
        @SerializedName("img")
        var img: String = "", // http://219.132.130.103:8080/patrolimg/userfiles/fileupload/412.jpg
        @SerializedName("imgOrder")
        var imgOrder: Int = 0, // 1
        @SerializedName("name")
        var name: String = "" // 引导页1
    )

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.CAMERA
            ), 1
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            d.dismiss()

            if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                var s = ""
                grantResults.forEachIndexed { index, i ->
                    if (i == PackageManager.PERMISSION_DENIED) {
                        s += " ${permissions[index]} "
                    }
                }

                ToastUtil.showToast(this, "请授予应用运行所需权限:$s")

                Flowable.timer(1000, TimeUnit.MILLISECONDS, Schedulers.newThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe { finish() }


            } else {
//                startMain()
            }
        }
    }


    private var cd = CompositeDisposable()
    private fun startMain() {
//        AgreementFragment().show(supportFragmentManager, "")

        /*cd.add(Flowable.interval(0, 1000, TimeUnit.MILLISECONDS, Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it == 0L) {
                    containerAgreement.visibility = View.VISIBLE
//                    btnStart.isEnabled = false
                    btnStart.isClickable = false
                }
                btnStart.text = "${5 - it}秒"
                if (it == 5L) {
                    btnStart.isClickable = true
                    btnStart.text = "同意并使用"
                    cd.clear()
                }
            })*/
        containerAgreement.visibility = View.VISIBLE
        btnStart.isClickable = true
        btnStart.text = "同意并使用"
//        Flowable.interval()
        /*MapApp.refreshUser(this)
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()*/
    }

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
}

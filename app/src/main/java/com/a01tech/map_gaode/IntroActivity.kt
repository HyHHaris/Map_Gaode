package com.a01tech.map_gaode

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.a01tech.map_gaode.utils.SPUtil
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        setListener()
        processLogic()
    }


    private fun setListener() {
        /**
         * 设置进入按钮和跳过按钮控件资源 id 及其点击事件
         * 如果进入按钮和跳过按钮有一个不存在的话就传 0
         * 在 BGABanner 里已经帮开发者处理了防止重复点击事件
         * 在 BGABanner 里已经帮开发者处理了「跳过按钮」和「进入按钮」的显示与隐藏
         */
        banner.setEnterSkipViewIdAndDelegate(
            R.id.btn_guide_enter,
            R.id.tv_guide_skip
        ) {
            val versionCode = getVersionCode(this)
            SPUtil.setParam(this, "IntroVersion", versionCode)
            //                SPUtil.set(this, Constants.SP_LAST_VISION_CODE, versionCode)
            //                startActivity(Intent(this@IntroduceActivity, SplashActivity::class.java))
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

    }

    private fun processLogic() {
        // 设置数据源

        /*  banner.setData(
              null,
              null,
              R.mipmap.splash001,
              R.mipmap.splash002,
              R.mipmap.splash003,
              R.mipmap.splash004
          )*/

        banner.setAdapter { banner, itemView, model, position ->
            Glide.with(this).load(model as String).placeholder(R.mipmap.icon)
                .error(R.mipmap.icon)
                .centerCrop()
                .dontAnimate()
                .into(itemView as ImageView)
        }

        banner.setData(
            intent.getStringExtra("path").split(","), mutableListOf("", "", "")
        )

    }

    override fun onResume() {
        super.onResume()

        // 如果开发者的引导页主题是透明的，需要在界面可见时给背景 Banner 设置一个白色背景，避免滑动过程中两个 Banner 都设置透明度后能看到 Launcher
        banner.setBackgroundResource(android.R.color.white)
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

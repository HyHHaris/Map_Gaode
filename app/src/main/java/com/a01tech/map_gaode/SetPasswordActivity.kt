package com.a01tech.map_gaode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.DesUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.SPUtil
import com.a01tech.map_gaode.utils.ToastUtil
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_set_password.*

class SetPasswordActivity : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QMUIDisplayHelper.setFullScreen(this)
        setContentView(R.layout.activity_set_password)

        val user = MapApp.user
        val desUtil = DesUtil("zy2dUKlG", "3fTrP5xE")


        cb1.checkedChanges().subscribe {
            edtOldPassword.transformationMethod = if (it) HideReturnsTransformationMethod.getInstance() else
                PasswordTransformationMethod.getInstance()
        }

        cb2.checkedChanges().subscribe {
            edtPassword.transformationMethod = if (it) HideReturnsTransformationMethod.getInstance() else
                PasswordTransformationMethod.getInstance()
        }

        cb3.checkedChanges().subscribe {
            edtPassword2.transformationMethod = if (it) HideReturnsTransformationMethod.getInstance() else
                PasswordTransformationMethod.getInstance()
        }

        textBack.setOnClickListener { onBackPressed() }
        btnLogin.setOnClickListener {
            val psd = edtOldPassword.text.toString().trim()
            val psdNew = edtPassword.text.toString().trim()
            val psdConfirm = edtPassword2.text.toString().trim()

            if (psd.isEmpty()) {
                textError1.text = "请填写密码"
            } else {
                textError1.text = ""
            }

            if (psdNew.isEmpty()) {
                textError2.text = "请填写密码"
            } else {
                textError2.text = ""
            }

            when {
                psdConfirm.isEmpty() -> textError3.text = "请填写密码"
                psdConfirm != psdNew -> textError3.text = "两次输入的密码不一致"
                else -> textError3.text = ""
            }



            if (psd.isNotEmpty() && psdNew.isNotEmpty() && psdConfirm == psdNew)
                RetrofitUtil.getService()
                    .changePassword(user.token, user.username, desUtil.encode(psd), desUtil.encode(psdNew))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { JsonData(it) }
                    .subscribe({
                        ToastUtil.showToast(this, it.msg)
                        if (it.code == 1) {
                            val dialog = QMUIDialog.MessageDialogBuilder(this)
                                .setTitle("提示")
                                .setMessage("密码修改成功，请重新登录。")

                                .addAction("确定") { a, b ->
                                    a.dismiss()
                                    user.password = ""
                                    SPUtil.setParam(
                                        this, "user", MapApp.gson.toJson(
                                            user
                                        )
                                    )
                                    startActivity(Intent(this, SplashActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                }.create()

                            dialog.setCancelable(false)
                            dialog.show()
                        } else {
                            textError1.text = it.msg
                        }
                    }, {
                        it.printStackTrace()
                        ToastUtil.showToast(this, "Error")
                    })
        }

    }
}

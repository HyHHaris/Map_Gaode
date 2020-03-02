package com.a01tech.map_gaode.utils

import android.content.Context
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog

object DialogUtil {
    fun genSavedDialog(context: Context) =
        QMUITipDialog.CustomBuilder(context)
            .create()!!.apply { setCancelable(false) }


    fun genLoadingDialog(context: Context) =
        QMUITipDialog.Builder(context)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .create()!!.apply { setCancelable(false) }

    fun genLoadingDialog(context: Context, s: String) =
        QMUITipDialog.Builder(context)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord(s)
            .create()!!.apply { setCancelable(false) }

    fun genUploadingDialog(context: Context) =
        QMUITipDialog.Builder(context)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord("上传中")
            .create()!!.apply { setCancelable(false) }

    fun createMessageDialog(
        context: Context,
        title: String,
        msg: String,
        confirm: QMUIDialogAction
    ): QMUIDialog {
        val builder = QMUIDialog.MessageDialogBuilder(context)
        return builder.setTitle(title)
            .setMessage(msg)
            .addAction(confirm)
            .setCancelable(false)
            .create()
    }


    fun createMessageDialogOK(
        context: Context,
        title: String,
        msg: String, listener: QMUIDialogAction.ActionListener
    ): QMUIDialog {
        return createMessageDialog(
            context
            , title, msg, QMUIDialogAction(
                context, "OK",
                listener
            )
        )
    }

    fun createSimpleDialog(
        context: Context,
        msg: String, listener: QMUIDialogAction.ActionListener
    ): QMUIDialog {
        val builder = QMUIDialog.MessageDialogBuilder(context)
        return builder
            .setMessage(msg)
            .addAction(
                QMUIDialogAction(
                    context, "OK",
                    listener
                )
            )
            .setCancelable(false)
            .create()
    }

    fun createSimpleChooseDialog(
        context: Context,
        msg: String, listener: QMUIDialogAction.ActionListener
    ): QMUIDialog {
        val builder = QMUIDialog.MessageDialogBuilder(context)
        return builder
            .setMessage(msg)
            .addAction(
                QMUIDialogAction(
                    context, "Cancel", QMUIDialogAction.ActionListener { dialog, index ->
                        dialog.dismiss()
                    }
                )
            )
            .addAction(
                QMUIDialogAction(
                    context, "OK",
                    listener
                )
            )
            .setCancelable(false)
            .create()
    }


}
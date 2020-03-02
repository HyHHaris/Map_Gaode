package com.a01tech.map_gaode.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.fragment.app.DialogFragment
import com.a01tech.map_gaode.R
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import kotlinx.android.synthetic.main.layout_agreement.*

class AgreementFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_agreement, container, false)
    }


    override fun onStart() {
        super.onStart()
        getDialog().getWindow().setLayout(QMUIDisplayHelper.dpToPx(400), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView.loadUrl("file:///android_asset/xy.htm")
        webView.webChromeClient = WebChromeClient()
    }
}
package com.a01tech.map_gaode

import android.os.Bundle
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_argeenment.*

class ArgeenmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_argeenment)

        topBar.addLeftBackImageButton().setOnClickListener { onBackPressed() }
        topBar.setTitle("")
        webView.loadUrl("file:///android_asset/xy.htm")
        webView.webChromeClient = WebChromeClient()

    }
}

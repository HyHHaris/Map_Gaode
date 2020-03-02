package com.a01tech.map_gaode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import kotlinx.android.synthetic.main.activity_photo_activity.*

class PhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_activity)
        QMUIDisplayHelper.setFullScreen(this)

        Glide.with(this)
            .load(intent.getStringExtra("href"))
            .into(photoView)

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}

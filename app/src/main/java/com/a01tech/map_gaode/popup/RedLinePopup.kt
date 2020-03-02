package com.a01tech.map_gaode.popup

import android.content.Context
import android.view.View
import android.view.animation.Animation
import com.a01tech.map_gaode.R
import razerdp.basepopup.BasePopupWindow


class RedLinePopup : BasePopupWindow {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, delayInit: Boolean) : super(context, delayInit)
    constructor(context: Context?, width: Int, height: Int) : super(context, width, height)

    override fun onCreateContentView(): View {
        return createPopupById(R.layout.layout_popup_dikuaishuxing)
    }




    override fun onCreateShowAnimation(): Animation {
        return getDefaultScaleAnimation(true)
    }

    override fun onCreateDismissAnimation(): Animation {
        return getDefaultScaleAnimation(false)
    }


    companion object {
        fun newInstance(context: Context) = RedLinePopup(context)
            .apply {

            }
    }
}
package com.a01tech.map_gaode.glide

import android.content.Context
import com.elvishew.xlog.XLog
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item

class TileFilter : Filter() {
    override fun filter(context: Context?, item: Item?): IncapableCause? {
        XLog.e("Filter" + item)
        if (!needFiltering(context, item))
            return null

        return item?.contentUri?.path?.contains("map_data")?.let {
            return if (it) IncapableCause(IncapableCause.TOAST, "...")
            else null
        }
    }

    override fun constraintTypes(): MutableSet<MimeType> = MimeType.ofImage()
}
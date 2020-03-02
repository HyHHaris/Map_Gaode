package com.a01tech.map_gaode.entity

import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.RedLine
import com.google.gson.annotations.SerializedName

data class ZdjhBean(
    @SerializedName("bZ")
    var bZ: String = "", // 测试11
    @SerializedName("createTime")
    var createTime: Long = 0, // 1562572968000
    @SerializedName("id")
    var id: Long = 0, // 312658350069
    @SerializedName("jJ")
    var jJ: String = "", // 测试10
    @SerializedName("mJ")
    var mJ: String = "", // 7
    @SerializedName("ofid")
    var ofid: String = "", // 8
    @SerializedName("wZ")
    var wZ: String = "" // 测试8
) {


    @SerializedName("data")
    val dots: MutableList<RedLine> = mutableListOf()
    fun turns() = ZdjhBeanStr(
        "",
        bZ, createTime, id, jJ, mJ, ofid, wZ
    )
}


data class ZdjhBeanStr(
    var data: String = "",
    @SerializedName("bZ")
    var bZ: String = "", // 测试11
    @SerializedName("createTime")
    var createTime: Long = 0, // 1562572968000
    @SerializedName("id")
    var id: Long = 0, // 312658350069
    @SerializedName("jJ")
    var jJ: String = "", // 测试10
    @SerializedName("mJ")
    var mJ: String = "", // 7
    @SerializedName("ofid")
    var ofid: String = "", // 8
    @SerializedName("wZ")
    var wZ: String = "" // 测试8
) {

    fun turns() = ZdjhBean(
        bZ, createTime, id, jJ, mJ, ofid, wZ
    ).apply {
        dots.addAll(MapApp.gson.fromJson(data, Array<RedLine>::class.java))
    }
}
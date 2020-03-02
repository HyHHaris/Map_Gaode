package com.a01tech.map_gaode.entity

import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.RedLine
import com.google.gson.annotations.SerializedName


data class ZzsBean(

    @SerializedName("area")
    var area: String = "", // 18143
    @SerializedName("area2")
    var area2: String = "", // 27.2145
    @SerializedName("bz")
    var bz: String = "", // 上坪村2座独立栋，一直未列入优先征收范围，待领导指示后启动征收
    @SerializedName("createTime")
    var createTime: Long = 0, // 1560333388000
    @SerializedName("ghqk")
    var ghqk: String = "", // 二类居住
    @SerializedName("id")
    var id: Long = 0, // 307682236810
    @SerializedName("landNumber")
    var landNumber: String = "", // 西5
    @SerializedName("mapNumber")
    var mapNumber: String = "",
    @SerializedName("ofid")
    var ofid: String = "", // 0
    @SerializedName("projectName")
    var projectName: String = "", // 江南新城
    @SerializedName("tdzl")
    var tdzl: String = "", // 三角镇
    @SerializedName("unsignedAppendages")
    var unsignedAppendages: String = "", // 0
    @SerializedName("unsignedGrave")
    var unsignedGrave: String = "", // 0
    @SerializedName("unsignedHouse")
    var unsignedHouse: String = "", // 2
    @SerializedName("unsignedLand")
    var unsignedLand: String = "", // 0
    @SerializedName("yqsj")
    var yqsj: String = "", // 2019年6月
    @SerializedName("zsgg")
    var zsgg: String = "", // 梅区府[2012]41号
    @SerializedName("zspw")
    var zspw: String = ""
) {
    @SerializedName("data")
    val dots: MutableList<RedLine> = mutableListOf()

    fun turns() = ZzsBeanStr(
        "",
        area,
        area2,
        bz,
        createTime,
        ghqk,
        id,
        landNumber,
        mapNumber,
        ofid,
        projectName,
        tdzl,
        unsignedAppendages,
        unsignedGrave,
        unsignedHouse,
        unsignedLand,
        yqsj,
        zsgg,
        zspw
    )
}

data class ZzsBeanStr(
    var data: String = "",
    @SerializedName("area")
    var area: String = "", // 18143
    @SerializedName("area2")
    var area2: String = "", // 27.2145
    @SerializedName("bz")
    var bz: String = "", // 上坪村2座独立栋，一直未列入优先征收范围，待领导指示后启动征收
    @SerializedName("createTime")
    var createTime: Long = 0, // 1560333388000
    @SerializedName("ghqk")
    var ghqk: String = "", // 二类居住
    @SerializedName("id")
    var id: Long = 0, // 307682236810
    @SerializedName("landNumber")
    var landNumber: String = "", // 西5
    @SerializedName("mapNumber")
    var mapNumber: String = "",
    @SerializedName("ofid")
    var ofid: String = "", // 0
    @SerializedName("projectName")
    var projectName: String = "", // 江南新城
    @SerializedName("tdzl")
    var tdzl: String = "", // 三角镇
    @SerializedName("unsignedAppendages")
    var unsignedAppendages: String = "", // 0
    @SerializedName("unsignedGrave")
    var unsignedGrave: String = "", // 0
    @SerializedName("unsignedHouse")
    var unsignedHouse: String = "", // 2
    @SerializedName("unsignedLand")
    var unsignedLand: String = "", // 0
    @SerializedName("yqsj")
    var yqsj: String = "", // 2019年6月
    @SerializedName("zsgg")
    var zsgg: String = "", // 梅区府[2012]41号
    @SerializedName("zspw")
    var zspw: String = ""
) {
    fun turns() = ZzsBean(
        area,
        area2,
        bz,
        createTime,
        ghqk,
        id,
        landNumber,
        mapNumber,
        ofid,
        projectName,
        tdzl,
        unsignedAppendages,
        unsignedGrave,
        unsignedHouse,
        unsignedLand,
        yqsj,
        zsgg,
        zspw
    ).apply {
        dots.addAll(MapApp.gson.fromJson(data, Array<RedLine>::class.java))

    }
}
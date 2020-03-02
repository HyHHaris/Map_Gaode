package com.a01tech.map_gaode.entity

import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.RedLine
import com.google.gson.annotations.SerializedName


data class YzsBean(

    @SerializedName("area")
    var area: String = "", // 96530
    @SerializedName("area2")
    var area2: String = "", // 144.795
    @SerializedName("bz")
    var bz: String = "", // 待周边土地征收后一并入库
    @SerializedName("createTime")
    var createTime: Long = 0, // 1560333241000
    @SerializedName("czwt")
    var czwt: String = "",
    @SerializedName("ghqk")
    var ghqk: String = "", // 二类居住、教育
    @SerializedName("id")
    var id: Long = 0, // 270595615638
    @SerializedName("landNumber")
    var landNumber: String = "",
    @SerializedName("mapNumber")
    var mapNumber: String = "",
    @SerializedName("ofid")
    var ofid: String = "", // 0
    @SerializedName("projectName")
    var projectName: String = "", // 江北宜居城乡
    @SerializedName("rkjd")
    var rkjd: String = "", // 未报送资料
    @SerializedName("tdzl")
    var tdzl: String = "", // 金山办
    @SerializedName("ywlx")
    var ywlx: String = "" // 城区其他储备地
) {


    @SerializedName("data")
    val dots: MutableList<RedLine> = mutableListOf()
    /*  get() {
          if (dots.isEmpty()) {
              dots.addAll(MapApp.gson.fromJson(data, Array<RedLine>::class.java).toList())
          }
          return dots
      }*/


    fun turns() = YzsBeanStr("",area, area2, bz, createTime, czwt, ghqk, id, landNumber, mapNumber, ofid, projectName, rkjd, tdzl)
}


data class YzsBeanStr(
    var data: String = "",
    @SerializedName("area")
    var area: String = "", // 96530
    @SerializedName("area2")
    var area2: String = "", // 144.795
    @SerializedName("bz")
    var bz: String = "", // 待周边土地征收后一并入库
    @SerializedName("createTime")
    var createTime: Long = 0, // 1560333241000
    @SerializedName("czwt")
    var czwt: String = "",
    @SerializedName("ghqk")
    var ghqk: String = "", // 二类居住、教育
    @SerializedName("id")
    var id: Long = 0, // 270595615638
    @SerializedName("landNumber")
    var landNumber: String = "",
    @SerializedName("mapNumber")
    var mapNumber: String = "",
    @SerializedName("ofid")
    var ofid: String = "", // 0
    @SerializedName("projectName")
    var projectName: String = "", // 江北宜居城乡
    @SerializedName("rkjd")
    var rkjd: String = "", // 未报送资料
    @SerializedName("tdzl")
    var tdzl: String = "", // 金山办
    @SerializedName("ywlx")
    var ywlx: String = "" // 城区其他储备地
) {

    fun turns() = YzsBean(
        area, area2, bz, createTime, czwt, ghqk, id, landNumber, mapNumber, ofid, projectName, rkjd, tdzl, ywlx
    ).apply {
        dots.addAll(MapApp.gson.fromJson(data, Array<RedLine>::class.java))
    }
}


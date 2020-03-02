package com.a01tech.map_gaode.entity

import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.RedLine
import com.google.gson.annotations.SerializedName

data class YgtdBean(

    @SerializedName("bZ")
    var bZ: String = "",
    @SerializedName("cJDJ")
    var cJDJ: String = "", // 0
    @SerializedName("cJZJ")
    var cJZJ: String = "", // 10448
    @SerializedName("createTime")
    var createTime: Long = 0, // 1562570938000
    @SerializedName("gDFS")
    var gDFS: String = "", // 网上拍卖
    @SerializedName("gDPW")
    var gDPW: String = "", // [21016]110
    @SerializedName("id")
    var id: Long = 0, // 245655044903
    @SerializedName("jZMD")
    var jZMD: String = "", // 0
    @SerializedName("lMJ")
    var lMJ: String = "", // 0
    @SerializedName("mJ1")
    var mJ1: String = "", // 59451
    @SerializedName("mJ2")
    var mJ2: String = "", // 89.1765
    @SerializedName("ofid")
    var ofid: String = "", // 1
    @SerializedName("pWRQ")
    var pWRQ: String = "", // 2016.7.4
    @SerializedName("rJL")
    var rJL: String = "", // 0
    @SerializedName("tDSYQR")
    var tDSYQR: String = "", // 梅州市客天下碧桂园房地产开发有限公司
    @SerializedName("tH")
    var tH: String = "", // GZ16022-3
    @SerializedName("yT")
    var yT: String = "" // 商住
) {


    @SerializedName("data")
    val dots: MutableList<RedLine> = mutableListOf()
    /*  get() {
          if (dots.isEmpty()) {
              dots.addAll(MapApp.gson.fromJson(data, Array<RedLine>::class.java).toList())
          }
          return dots
      }*/


    fun turns() = YgtdBeanStr(
        "",
        bZ, cJDJ, cJZJ, createTime, gDFS, gDPW, id, jZMD, lMJ, mJ1, mJ2, ofid, pWRQ, rJL, tDSYQR, tH, yT
    )
}


data class YgtdBeanStr(
    var data: String = "",
    @SerializedName("bZ")
    var bZ: String = "",
    @SerializedName("cJDJ")
    var cJDJ: String = "", // 0
    @SerializedName("cJZJ")
    var cJZJ: String = "", // 10448
    @SerializedName("createTime")
    var createTime: Long = 0, // 1562570938000
    @SerializedName("gDFS")
    var gDFS: String = "", // 网上拍卖
    @SerializedName("gDPW")
    var gDPW: String = "", // [21016]110
    @SerializedName("id")
    var id: Long = 0, // 245655044903
    @SerializedName("jZMD")
    var jZMD: String = "", // 0
    @SerializedName("lMJ")
    var lMJ: String = "", // 0
    @SerializedName("mJ1")
    var mJ1: String = "", // 59451
    @SerializedName("mJ2")
    var mJ2: String = "", // 89.1765
    @SerializedName("ofid")
    var ofid: String = "", // 1
    @SerializedName("pWRQ")
    var pWRQ: String = "", // 2016.7.4
    @SerializedName("rJL")
    var rJL: String = "", // 0
    @SerializedName("tDSYQR")
    var tDSYQR: String = "", // 梅州市客天下碧桂园房地产开发有限公司
    @SerializedName("tH")
    var tH: String = "", // GZ16022-3
    @SerializedName("yT")
    var yT: String = "" // 商住
) {

    fun turns() = YgtdBean(
        bZ, cJDJ, cJZJ, createTime, gDFS, gDPW, id, jZMD, lMJ, mJ1, mJ2, ofid, pWRQ, rJL, tDSYQR, tH, yT
    ).apply {
        dots.addAll(MapApp.gson.fromJson(data, Array<RedLine>::class.java))
    }
}
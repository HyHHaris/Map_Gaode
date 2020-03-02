package com.a01tech.map_gaode.entity

import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.RedLine
import com.amap.api.col.stln3.bz
import com.amap.api.col.stln3.mj
import com.google.gson.annotations.SerializedName

/**
 * Map_Gaode   com.a01tech.map_gaode.entity
 * Created by haris 2019/11/11 0011.9:53
 */
data class ZdhxBean(
    @SerializedName("mapNumber")    //图号
    var mapNumber:String = "",
    @SerializedName("bZ")    //备注
    var bZ:String = "",
    @SerializedName("mJ")       //面积
    var mJ:String = "",
    @SerializedName("ofid")     //ofid
    var ofid:String = "",
    @SerializedName("createTime")
    var createTime: Long = 0,// 1562570938000
    @SerializedName("id")
    var id: Long = 0 // 270595615638
){
    @SerializedName("data")
    val dots: MutableList<RedLine> = mutableListOf()
    fun turns() = ZdhxBeanStr(
        "",
        mapNumber, bZ, mJ ,ofid,createTime,id
    )
}
data class ZdhxBeanStr(
    var data: String = "",
    @SerializedName("mapNumber")
    var mapNumber: String = "",
    @SerializedName("bZ")
    var bZ: String = "",
    @SerializedName("mJ")
    var mJ: String = "",
    @SerializedName("ofid")
    var ofid: String = "",
    @SerializedName("createTime")
    var createTime: Long=0,
    @SerializedName("id")
    var id: Long = 0 // 270595615638
){
    fun turns() = ZdhxBean(
        mapNumber,bZ,mJ ,ofid,createTime,id
    ).apply {
        dots.addAll(MapApp.gson.fromJson(data, Array<RedLine>::class.java))
    }
}





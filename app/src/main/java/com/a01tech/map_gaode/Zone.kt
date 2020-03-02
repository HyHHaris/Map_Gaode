package com.a01tech.map_gaode

import com.amap.api.maps.model.Polygon
import com.google.gson.annotations.SerializedName

class ZoneEntityString(
    var id: Long, var tdzl: String = "",
    var mapNumber: String = "",
    @SerializedName("ywlx") var tId: String = "",
    var tdId: String? = "",
    var fbk1: String = "",
    var area: String = "",
    var area2: String = "",
    var projectName: String = "",
    //beizhu
    var fbk: String? = "",
    var tdgh: String? = "",
    var bid: String? = "",
    var totalCost: String? = "",
    var compensateCost: String? = "",
    var developCost: String? = "",
    var otherCost: String? = "",
    var landNumber: String? = "",
    var cbbh: String? = ""

//    "area": "2614",
//"area2": "3.921",
//"projectName": "西郊寨中1",
) {


    var purpose1: String? = ""
    var sPurpose1: String? = ""
    var purpose2: String? = ""
    var sPurpose2: String? = ""
    var purpose3: String? = ""
    var sPurpose3: String? = ""
    var purpose4: String? = ""
    var sPurpose4: String? = ""

    fun turn(): Zone {
        return Zone(
            id, tdzl, mapNumber, tId, tdId,
            MapApp.gson.fromJson(fbk1, Array<RedLine>::class.java).toList()
            , area, area2, projectName, fbk, tdgh, bid, totalCost, compensateCost, developCost, otherCost
            , landNumber, cbbh
        ).let {
            it.purpose1 = this.purpose1
            it.sPurpose1 = this.sPurpose1
            it.purpose2 = this.purpose2
            it.sPurpose2 = this.sPurpose2
            it.purpose3 = this.purpose3
            it.sPurpose3 = this.sPurpose3
            it.purpose4 = this.purpose4
            it.sPurpose4 = this.sPurpose4
            it
        }
    }

}


class Zone(
    var id: Long,
    var tdzl: String = "",
    var mapNumber: String = "",
    @SerializedName("ywlx") var tId: String = "",
    var tdId: String? = "",
    @SerializedName("fbk1") var redLines: List<RedLine>,
    var area: String = "",
    var area2: String = "",
    var projectName: String = "",
    var fbk: String? = "",
    var tdgh: String? = "",
    var bid: String? = "",
    var totalCost: String? = "",
    var compensateCost: String? = "",
    var developCost: String? = "",
    var otherCost: String? = "",
    var landNumber: String? = "",
    var cbbh: String? = "",
    var checked: Boolean = false

) {
    var purpose1: String? = ""
    var sPurpose1: String? = ""
    var purpose2: String? = ""
    var sPurpose2: String? = ""
    var purpose3: String? = ""
    var sPurpose3: String? = ""
    var purpose4: String? = ""
    var sPurpose4: String? = ""

    fun turns() = ZoneEntityString(
        id, tdzl, mapNumber, tId, tdId, "", area, area2, projectName
        , fbk, tdgh, bid, totalCost, compensateCost, developCost, otherCost, landNumber, cbbh
    ).let {
        it.purpose1 = this.purpose1
        it.sPurpose1 = this.sPurpose1
        it.purpose2 = this.purpose2
        it.sPurpose2 = this.sPurpose2
        it.purpose3 = this.purpose3
        it.sPurpose3 = this.sPurpose3
        it.purpose4 = this.purpose4
        it.sPurpose4 = this.sPurpose4
        it
    }

    override fun toString(): String {
        return "Zone(id=$id, tdzl='$tdzl', mapNumber='$mapNumber', tId='$tId', tdId='$tdId', redLines=$redLines, area='$area', area2='$area2', projectName='$projectName')"
    }
}

data class RedLine(val fid: Long, val dot: List<Dot>, var polygon: Polygon? = null)

data class Dot(var x: Double, var y: Double)
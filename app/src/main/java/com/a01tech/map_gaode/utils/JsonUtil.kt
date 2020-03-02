package com.a01tech.map_gaode.utils

import com.a01tech.map_gaode.MapApp
import com.google.gson.JsonObject

class JsonData(var obj: JsonObject) {
    var code = 0
    var msg = ""

    init {
        try {
            code = obj.get("code").asInt
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            msg = obj.get("msg").asString
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T : Any> getField(fieldName: String, clazz: Class<T>): T? {
        return try {

            MapApp.gson.fromJson(obj.get(fieldName), clazz)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun <T : Any> getData(fieldName: String, clazz: Class<T>): T? {
        return try {

            MapApp.gson.fromJson(obj.get(fieldName), clazz)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun <T : Any> getDataList(fieldName: String, clazz: Class<T>): List<T> {
        val result = mutableListOf<T>()
        val gson = MapApp.gson
        try {
            val list = obj.get(fieldName).asJsonArray
            list?.let {
                list.forEach { el ->
                    result.add(gson.fromJson(el, clazz))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
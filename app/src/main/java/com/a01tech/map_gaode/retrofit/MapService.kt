package com.a01tech.map_gaode.retrofit

import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface MapService {

    companion object {
        //        219.132.130.103 梅州ip
//        const val domain = "wakaka.01solomo.com"
        const val domain = "mztdcbzx.01solomo.com:8080"
       //const val domain = "192.168.0.107:8080"
        //                        const val baseUrl = "http://192.168.0.107:8080/jfinal_demo/"
//        const val baseUrl = "http://wakaka.01solomo.com/patrol/"
//        const val baseUrl = "http://192.168.0.107:8080/patrol/"
        const val baseUrl = "http://$domain/patrol/"
//        http://192.168.0.107:8080/patrol/
    }

    /*  @GET("user/checkUser")
      fun login(
          @Query("userName") username: String,
          @Query("password") password: String
      ): Observable<JsonObject>*/
    @GET("user/checkUser")
    fun login(
        @Query("userName") username: String,
        @Query("password", encoded = true) password: String
    ): Observable<JsonObject>


    @POST("user/checkUser")
    @FormUrlEncoded
    fun loginPost(
        @Field("userName") username: String,
        @Field("password") password: String
    ): Observable<JsonObject>

    @GET("user/appVersion")
    fun checkVersion(
    ): Observable<JsonObject>


    @Multipart
    @POST("upload/uploadFile")
    fun uploadImg(
        @Part upload: MultipartBody.Part
    ): Observable<JsonObject>


    @GET("patrol/reportPatrol")
    fun tijiaoXuncha(
        @Query("token") token: String,
        @Query("lId") lpId: String,
        @Query("trajectory") trajectory: String,
        @Query("content") content: String,
        @Query("situation") situation: String,
        @Query("piIds") piIds: String
    ): Observable<JsonObject>

    @GET("patrol/editPatrol")
    fun xiugaiXuncha(
        @Query("token") token: String,
        @Query("id") id: String,
        @Query("content") content: String,
        @Query("situation") situation: String,
        @Query("piIds") piIds: String
    ): Observable<JsonObject>


    @GET("patrol/managePatrol")
    fun xunchaGuanliLiebiao(
        @Query("token") token: String
    ): Observable<JsonObject>


    @GET("patrol/patrolInfo")
    fun xunchaDetail(
        @Query("token") token: String,
        @Query("id") id: String
    ): Observable<JsonObject>

    @GET("patrolimg/pattolImgInfo")
    fun photoDetail(
        @Query("token") token: String,
        @Query("id") id: String
    ): Observable<JsonObject>


    @GET("patrol/patrolHistory")
    fun xunchaLishiLiebiao(
        @Query("token") token: String
    ): Observable<JsonObject>


    @GET("patrol/newCylindricalManage")
    fun newCylindricalManage(
        @Query("token") token: String
    ): Observable<JsonObject>


    @GET("{p1}/{p2}")
    fun getCount(
        @Path("p1") p1: String,
        @Path("p2") p2: String,
        @Query("token") token: String
    ): Observable<JsonObject>

    @GET
    fun getText(
        @Url url: String
    ): Observable<JsonObject>

    //    所有巡查人员
    @GET("patrol/patrolPersonnelManage")
    fun patrolPersonnelManage(
        @Query("token") token: String
    ): Observable<JsonObject>

    //    所有巡查人员
    @GET("user/getAllPatroler")
    fun getAllPatroler(
        @Query("token") token: String
    ): Observable<JsonObject>

    //    管理端历史巡查记录
    @GET("patrol/patrolHistoryManage")
    fun patrolHistoryManage(
        @Query("token") token: String,
        @Query("name") name: String,
        @Query("firDate") firDate: String,
        @Query("secDate") secDate: String
    ): Observable<JsonObject>


    /**
     * 地块巡查列表
     *
     *     "neckName": "张三",
     *     "id": "125560272276",
     *     "time": 1558059407000,
     *     "content": "没有"
     */
    @GET("patrol/patrolListManage")
    fun patrolListManage(
        @Query("token") token: String,
        @Query("lid") name: String
    ): Observable<JsonObject>


    /**
     * 巡查员地块巡查列表
     *  "id": "743247199802",
    "time": "2019-05-20 11:12:28.0",
    "content": "缝纫工个人"
     */
    @GET("patrol/patrolList")
    fun patrolList(
        @Query("token") token: String,
        @Query("lid") name: String
    ): Observable<JsonObject>


    @GET("land/getAllLand")
    fun getAllLand(
        @Query("token") token: String
    ): Observable<JsonObject>


    @GET("guidepage/getGuidePageList")
    fun getGuide(): Observable<JsonObject>


    @GET("user/updatePassword")
    fun changePassword(
        @Query("token") token: String,
        @Query("userName", encoded = true) username: String,
        @Query("oldPassword", encoded = true) password: String,
        @Query("newPassword", encoded = true) passwordNew: String
    ): Observable<JsonObject>
}

package com.a01tech.map_gaode

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.a01tech.map_gaode.dialog.PropertyFragment
import com.a01tech.map_gaode.entity.*
import com.a01tech.map_gaode.fragments.*
import com.a01tech.map_gaode.interfaces.EditXunChaListener
import com.a01tech.map_gaode.interfaces.MyAMapNaviListener
import com.a01tech.map_gaode.interfaces.MyNavInfoCallback
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.*
import com.amap.api.col.stln3.it
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.*
import com.amap.api.navi.*
import com.amap.api.track.AMapTrackClient
import com.amap.api.track.ErrorCode
import com.amap.api.track.OnTrackLifecycleListener
import com.amap.api.track.TrackParam
import com.amap.api.track.query.entity.DriveMode
import com.amap.api.track.query.entity.Point
import com.amap.api.track.query.entity.Track
import com.amap.api.track.query.model.*
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.elvishew.xlog.XLog
import com.google.gson.JsonObject
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIKeyboardHelper
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.popup.QMUIPopup
import com.suke.widget.SwitchButton
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), PropertyFragment.PropertyFragmentListener, EditXunChaListener {


    override fun back(i: Int) {
        setLock(true)
        when (i) {
            0 -> {
                viewPager.visibility = View.GONE
            }
            1 -> {
                viewPager.visibility = View.VISIBLE
                viewPager.setCurrentItem(0, false)
            }
            2 -> {
                viewPager.visibility = View.VISIBLE
                viewPager.setCurrentItem(1, false)
            }
        }
    }

    private fun setLock(b: Boolean) {
        rb0.isClickable = b
        rb1.isClickable = b
        rb2.isClickable = b
        rb3.isClickable = b
        rb4.isClickable = b
    }

    private val polylines = LinkedList<Polyline>()
    private val endMarkers = LinkedList<Marker>()

    var totalCount = 0
    val trackPoints = mutableListOf<Point>()
    private var reshowDialog: ProgressDialog? = null

    private fun reShowTracks(terminalId: Long, trackId: Long, time: Long, page: Int) {
        if (page == 1) {
//            reshowDialog = ProgressDialog.show(this, "正在获取轨迹数据", "")
            reshowDialog = ProgressDialog(this)
            reshowDialog?.setTitle("正在获取轨迹数据")
            reshowDialog?.setProgressStyle(1)
            reshowDialog?.setCancelable(false)
            reshowDialog?.create()
            reshowDialog?.show()
//           reshowDialog.progress = 1
        }

        trackClient.queryTerminalTrack(
            QueryTrackRequest(
                Constants.serviceId, terminalId, trackId, time - 60000 * 60 * 12
                , time + 60000, 0, 0, 0, DriveMode.DRIVING, 0, 5000, 1, page, 100
            ), object : MyTrackListener() {
                override fun onQueryTrackCallback(queryTrackResponse: QueryTrackResponse) {
                    if (queryTrackResponse.isSuccess) {
//                        val dataString = MapApp.gson.toJson(queryTrackResponse.tracks)
                        XLog.e(queryTrackResponse.data)
                        if (queryTrackResponse.tracks.isEmpty()) {
                            reshowDialog?.dismiss()
                            ToastUtil.showToast(this@MainActivity, "未找到轨迹")
                        } else {

                            if (page == 1) {
                                totalCount = queryTrackResponse.tracks.first().count
                                trackPoints.clear()
                                reshowDialog?.setProgressStyle(1)
                                reshowDialog?.max = totalCount / 100 + 1
                                reshowDialog?.progress = page
//                                reshowDialog?.setMessage("$page/${totalCount / 100 + 1}")
                                reshowDialog?.setCancelable(false)
                            } else {
                                XLog.e("Page$page")
//                                reshowDialog?.setProgressStyle()
                                reshowDialog?.progress = page
//                                reshowDialog?.setMessage("$page/${totalCount / 100 + 1}")
                            }

                            trackPoints.addAll(queryTrackResponse.tracks.first().points)
                            if (trackPoints.size < totalCount) {
                                reShowTracks(terminalId, trackId, time, page + 1)
                            } else {
                                reshowDialog?.dismiss()
                                reshowDialog = null
                                drawTrackOnMap(trackPoints)
                                btnExitRe.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        reshowDialog?.dismiss()
                        ToastUtil.showToast(this@MainActivity, "获取轨迹失败 ${queryTrackResponse.errorMsg}")
                        XLog.e(MapApp.gson.toJson(queryTrackResponse))
                    }
                }
            }
        )
    }
    @SuppressLint("CheckResult")
    private fun reShowTracks(s: String) {
        reshowDialog = ProgressDialog.show(this, "正在获取轨迹数据", "")
        reshowDialog?.setCancelable(false)

        RetrofitUtil.getService()
            .getText(s)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                //                it.asJsonArray

                try {
                    XLog.e(it.get("data").asJsonObject.get("tracks").toString())
                    val tracks = Track.createTracks(it.getAsJsonObject("data").getAsJsonArray("tracks").toString())
                    XLog.e(tracks.size)
                    XLog.e(tracks.toString())
                    reshowDialog?.dismiss()
                    drawTrackOnMap(
                        tracks.first().points.apply {
                            XLog.e(this.size)
                            this.forEachIndexed { i, d ->
                                XLog.e("index ${i} ${d.lat} ${d.lng}")
                            }
                        }
                    )
                } catch (e: Exception) {
                    reshowDialog?.dismiss()

                    ToastUtil.showToast(this, "轨迹数据错误")
                    e.printStackTrace()
                }
                btnExitRe.visibility = View.VISIBLE

            }, {
                reshowDialog?.dismiss()
                it.printStackTrace()
                ToastUtil.showToast(this, "轨迹数据错误")
            })
    }

    //退出回放
    private fun exitReShow() {
        btnExitRe.visibility = View.GONE
        clearTracksOnMap()
    }

    private fun drawTrackOnMap(points: List<Point>) {
        val boundsBuilder = LatLngBounds.Builder()
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLUE).width(20f)
        polylineOptions.zIndex(30f)
        if (points.isNotEmpty()) {

            // 起点
            val p = points[0]
            val latLng = LatLng(p.lat, p.lng)
            val markerOptions = MarkerOptions()
                .position(latLng)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start))
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng, 17f
                )
            )
            endMarkers.add(map.addMarker(markerOptions))
        }
        if (points.size > 1) {
            // 终点
            val p = points[points.size - 1]
            val latLng = LatLng(p.lat, p.lng)
            val markerOptions = MarkerOptions()
                .position(latLng)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_end))

            endMarkers.add(map.addMarker(markerOptions))

            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng, 17f
                )
            )
        }
        for (p in points) {
            val latLng = LatLng(p.lat, p.lng)
            polylineOptions.add(latLng)
            boundsBuilder.include(latLng)
        }

        val polyline = map.addPolyline(polylineOptions)
        polylines.add(polyline)
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 30))
    }

    private fun clearTracksOnMap() {
        for (polyline in polylines) {
            polyline.remove()
        }
        for (marker in endMarkers) {
            marker.remove()
        }
        endMarkers.clear()
        polylines.clear()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.eventId) {
            1 -> {
                viewPager.setCurrentItem(4, false)
                Flowable.timer(500, TimeUnit.MILLISECONDS, Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        shangBaoFragment.setData(1, event.msg)
                    }
            }

            2 -> {
//                巡查历史
                viewPager.setCurrentItem(4, false)
                Flowable.timer(500, TimeUnit.MILLISECONDS, Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        shangBaoFragment.setData(2, event.msg)
                    }
            }
            77 -> {
                if (event.msg.contains(",")) {
                    rb0.isChecked = true
                    viewPager.visibility = View.GONE
                    val split = event.msg.split(",")
                    try {
                        exitReShow()
                        reShowTracks(split[0].toLong(), split[1].toLong(), dateFormatter.parse(split[2]).time, 1)
                    } catch (e: Exception) {
                        ToastUtil.showToast(this, "数据错误")
                    }
                } else {
                    ToastUtil.showToast(this, "数据错误")
                }
            }
            78 -> {
                exitReShow()
                viewPager.visibility = View.GONE
                rb0.isChecked = true
                reShowTracks(event.msg)
            }
        }
    }

    private var isGatherRunning = false

    private var dateFormatter = SimpleDateFormat("yy/MM/dd-HH:mm")

    private val searchData = mutableListOf<SearchBean>()

    val onTrackLifecycleListener = object : OnTrackLifecycleListener {
        override fun onStartGatherCallback(status: Int, msg: String) {
            when (status) {
                ErrorCode.TrackListen.START_GATHER_SUCEE -> {
                    Toast.makeText(this@MainActivity, "定位采集开启成功", Toast.LENGTH_SHORT).show()
                    isGatherRunning = true
                }
                ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED -> {
                    Toast.makeText(this@MainActivity, "定位采集已经开启", Toast.LENGTH_SHORT).show()
                    isGatherRunning = true
                }
                else -> Toast.makeText(
                    this@MainActivity,
                    "error onStartGatherCallback, status: $status, msg: $msg",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun onStopTrackCallback(p0: Int, p1: String?) {
        }

        override fun onBindServiceCallback(p0: Int, p1: String?) {
        }

        override fun onStopGatherCallback(status: Int, msg: String) {

            if (status == ErrorCode.TrackListen.STOP_GATHER_SUCCE) {
                Toast.makeText(this@MainActivity, "定位采集停止成功", Toast.LENGTH_SHORT).show()
                isGatherRunning = false
                containerXuncha.visibility = View.GONE

                viewPager.visibility = View.VISIBLE
                viewPager.currentItem = 4

                Flowable.timer(500, TimeUnit.MILLISECONDS, Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        shangBaoFragment.setData(
                            currentXunchaId,
                            "${terminalId},${trackId},${dateFormatter.format(System.currentTimeMillis())}"
                        )
                    }


            } else {
                Toast.makeText(
                    this@MainActivity,
                    "error onStopGatherCallback, status: $status, msg: $msg",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun onStartTrackCallback(status: Int, msg: String) {
            if (status == ErrorCode.TrackListen.START_TRACK_SUCEE ||
                status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK ||
                status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED
            ) {
                // 服务启动成功，继续开启收集上报
                trackClient.setTrackId(trackId)
                trackClient.startGather(this)
            } else {
                Toast.makeText(this@MainActivity, "轨迹上报服务服务启动异常，" + msg, Toast.LENGTH_SHORT).show();
            }
        }
    }


    var trackId = 0L

    var currentXunchaId = 0L
    override fun startXuncha(id: Long) {
        XLog.d("startXuncha $id")
        trackClient.addTrack(AddTrackRequest(Constants.serviceId, terminalId), object : MyTrackListener() {
            override fun onAddTrackCallback(addTrackResponse: AddTrackResponse) {
                if (addTrackResponse.isSuccess) {
                    // trackId需要在启动服务后设置才能生效，因此这里不设置，而是在startGather之前设置了track id
                    trackId = addTrackResponse.trid
                    val trackParam = TrackParam(Constants.serviceId, terminalId)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        trackParam.notification = createNotification()
                    }
//                    trackParam.trackId = trackId
                    trackClient.startTrack(trackParam, onTrackLifecycleListener)
                    map?.isMyLocationEnabled = true
                    containerXuncha.visibility = View.VISIBLE
                    setLock(false)
                    val find = dataChubei.find { it.id == id }
                    currentXunchaId = id
                    textXunchaName.text = "${find!!.projectName} ${find.mapNumber}"
                    startCountTime()
                } else {
                    Toast.makeText(this@MainActivity, "网络请求失败，" + addTrackResponse.errorMsg, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private var timeCompositeDisposable = CompositeDisposable()

    private fun startCountTime() {
        timeCompositeDisposable.clear()
        Flowable.interval(1000, TimeUnit.MILLISECONDS, Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                textXunchaTime.text =
                    "巡查已用时 ${(it / 3600).toInt().to2d}:${(it % 3600 / 60).toInt().to2d}:${(it % 60).toInt().to2d}"
            }.apply {
                timeCompositeDisposable.add(this)
            }
    }

    private fun initXunchaView() {
        containerXuncha.visibility = View.GONE
        btnEndXuncha.setOnClickListener {
            map.isMyLocationEnabled
            timeCompositeDisposable.clear()
            trackClient.stopGather(onTrackLifecycleListener)
        }
    }


    private val comp = CompositeDisposable()


    private lateinit var map: AMap

    private var currentIndex = 0

    private var pressBackTime = 0L
    override fun onBackPressed() {
        if (containerSearch.visibility == View.VISIBLE) {
            QMUIKeyboardHelper.hideKeyboard(window.decorView)
            edtSearch.setText("")
            containerSearch.visibility = View.GONE
        } else {
            /* val t = System.currentTimeMillis()
             if (t - pressBackTime < 1000) {
                 super.onBackPressed()
             } else {
                 ToastUtil.showToast(this, "再次按下返回键退出应用")
                 pressBackTime = t
             }*/
        }
    }


    //    显示地块属性
    private fun showPropertyPopup(id: Long, type: Int) {
        //地块会有重复的地方。所以当点击到重复地块的时候会显示多个属性，
        //解决办法是在地块属性前进行判断，只会显示已选择的地块
        val gson = MapApp.gson
        val s = when (type) {
            0 -> gson.toJson(dataChubei.find { it.id == id }?.turns())
            1 -> gson.toJson(dataYzs.find { it.id == id }?.turns())
            2 -> gson.toJson(dataZzs.find { it.id == id }?.turns())
            3 -> gson.toJson(dataYgtd.find { it.id == id }?.turns())
            4 -> gson.toJson(dataZdjh.find { it.id == id }?.turns())
            5 -> gson.toJson(dataZdhx.find { it.id == id }?.turns())
            else -> ""
        }
        val newInstance = PropertyFragment.newInstance(s, type)
        newInstance.show(supportFragmentManager, "")
    }


    val dataChubei = mutableListOf<Zone>()
    val dataYzs = mutableListOf<YzsBean>()
    val dataZzs = mutableListOf<ZzsBean>()
    val dataYgtd = mutableListOf<YgtdBean>()
    val dataZdjh = mutableListOf<ZdjhBean>()
    val dataZdhx = mutableListOf<ZdhxBean>()

    lateinit var trackClient: AMapTrackClient //猎鹰轨迹
    lateinit var naviClient: AMapNavi

    var terminalId = 0L
    lateinit var wakeLock: PowerManager.WakeLock

    lateinit var user: User
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        startService(Intent(this, MainService::class.java))
//        startService(Intent(this, RemoteService::class.java))
        setContentView(R.layout.activity_main)

        val manager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "a:MyWakelockTag")
        wakeLock.acquire()

        user = MapApp.user
        try {
            Glide.with(this).load(user.headImg).into(imgHead)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        EventBus.getDefault().register(this)
        val desUtil = DesUtil("zy2dUKlG", "3fTrP5xE")
        val bean = JsonData(MapApp.gson.fromJson((SPUtil.get(this, "land", "") as String), JsonObject::class.java))

        val s1 = bean.getField("lands", String::class.java)
        val s2 = bean.getField("landYzs", String::class.java)
        val s3 = bean.getField("landZzs", String::class.java)
        val s4 = bean.getField("landYgs", String::class.java)
        val s5 = bean.getField("landZdjhqies", String::class.java)
        val s6 = bean.getField("landZdhxs", String::class.java)

        dataChubei.addAll(

            MapApp.gson.fromJson(desUtil.decode(s1).apply {
                //                XLog.e(this)
            }, Array<ZoneEntityString>::class.java).map {
                it.turn()
            }
        )
        dataYzs.addAll(
            MapApp.gson.fromJson(desUtil.decode(s2), Array<YzsBeanStr>::class.java).map {
                it.turns()
            }
        )
        dataZzs.addAll(
            MapApp.gson.fromJson(desUtil.decode(s3), Array<ZzsBeanStr>::class.java).map {
                it.turns()
            }
        )
        dataYgtd.addAll(
            MapApp.gson.fromJson(desUtil.decode(s4), Array<YgtdBeanStr>::class.java).map {
                it.turns()
            }
        )
        dataZdjh.addAll(
            MapApp.gson.fromJson(desUtil.decode(s5), Array<ZdjhBeanStr>::class.java).map {
                it.turns()
            }
        )
        dataZdhx.addAll(
            MapApp.gson.fromJson(desUtil.decode(s6), Array<ZdhxBeanStr>::class.java).map {
                it.turns()
            }
        )

        searchData.addAll(
            dataChubei.map {
                SearchBean(
                    it.id, "${it.projectName} ${it.mapNumber}"
                    , "${it.projectName} ${it.mapNumber} ${it.tdzl} ${it.landNumber}", 0
                )
            }
        )

        searchData.addAll(
            dataZzs.map {
                SearchBean(
                    it.id, "${it.projectName} ${it.mapNumber}"
                    , "${it.projectName} ${it.mapNumber} ${it.tdzl} ${it.landNumber}", 1
                )
            }
        )

        searchData.addAll(
            dataYzs.map {
                SearchBean(
                    it.id, "${it.projectName} ${it.mapNumber}"
                    , "${it.projectName} ${it.mapNumber} ${it.tdzl} ${it.landNumber}", 2
                )
            }
        )

        searchData.addAll(
            dataYgtd.map {
                SearchBean(
                    it.id, "${it.tDSYQR} ${it.gDPW}"
                    , "${it.tDSYQR} ${it.gDPW} ${it.gDFS} ${it.pWRQ}", 3
                )
            }
        )
        searchData.addAll(
            dataZdjh.map {
                SearchBean(
                    it.id, "${it.jJ} ${it.wZ}"
                    , "${it.jJ} ${it.wZ} ${it.bZ}", 4
                )
            }
        )
        searchData.addAll(
            dataZdhx.map {
                SearchBean(
                    it.id, "${it.mJ} ${it.bZ}"
                    , "${it.mapNumber} ${it.mJ} ${it.bZ}", 5
                )
            }
        )

        textUserType.text = when (MapApp.user.role) {
            1 -> "管理人员"
            2 -> "巡查人员"
            else -> "?"
        }
        textUsername.text = MapApp.user.nickName


//        todo fixme
        if (MapApp.user.role == 1) {
            rb1.visibility = View.GONE
            rb2.visibility = View.GONE
        } else if (MapApp.user.role == 2) {
            rb4.visibility = View.GONE
        }


        QMUIDisplayHelper.setFullScreen(this)
        mapView.onCreate(savedInstanceState)
        initView()

        initSearchView()
        initXunchaView()
        btnExitRe.visibility = View.GONE
        btnExitRe.setOnClickListener {
            exitReShow()
        }
        //我的位置从按钮变成了复选框
        btnLocation.setOnClickListener {
        //我的位置按钮打开和关闭
            if (btnLocation.isChecked ){
               map.isMyLocationEnabled = true
            }else{
                map.isMyLocationEnabled = false
            }

            //XLog.e("我的位置测试")
            val location = map.myLocation
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude)
                    , 17f
                )
            )
//            if (!cbNavi.isChecked)
//                map.isMyLocationEnabled = !map.isMyLocationEnabled
        }

        naviClient = AMapNavi.getInstance(this)
        naviClient.addAMapNaviListener(object : MyAMapNaviListener() {
            override fun onInitNaviSuccess() {
                super.onInitNaviSuccess()
            }
        })

        trackClient = AMapTrackClient(applicationContext)
        trackClient.setInterval(1, 10)
//        仅gps模式，定位有问题
//        trackClient.setLocationMode(LocationMode.DEVICE_SENSORS)

        trackClient.queryTerminal(
            QueryTerminalRequest(Constants.serviceId, MapApp.user.username),
            object : MyTrackListener() {

                override fun onQueryTerminalCallback(queryTerminalResponse: QueryTerminalResponse) {
                    if (queryTerminalResponse.isSuccess) {
                        if (queryTerminalResponse.tid <= 0) {
                            trackClient.addTerminal(AddTerminalRequest(MapApp.user.username, Constants.serviceId),
                                object : MyTrackListener() {
                                    override fun onCreateTerminalCallback(addTerminalResponse: AddTerminalResponse) {
                                        if (addTerminalResponse.isSuccess) {
                                            terminalId = addTerminalResponse.tid
                                        } else {
                                            ToastUtil.showToast(
                                                this@MainActivity,
                                                "请求失败，" + addTerminalResponse.errorMsg
                                            )
                                        }
                                    }
                                })
                        } else {
                            terminalId = queryTerminalResponse.tid
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "请求失败，" + queryTerminalResponse.errorMsg, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
    }

    val types = mutableListOf("储备土地", "已征待入库", "在征土地","已供土地","重点计划区域","征地红线")

    @SuppressLint("CheckResult")
    private fun initSearchView() {

        containerSearch.visibility = View.GONE

        val typesAdapter = ZoneTypeAdapter()

        rcvTypes.layoutManager = LinearLayoutManager(this)
        rcvTypes.adapter = typesAdapter

        val searchAdapter = SearchAdapter()
        rcvResults.layoutManager = LinearLayoutManager(this)
        rcvResults.adapter = searchAdapter


        btnSearchMore.setOnClickListener {
            if (typesAdapter.data.isNotEmpty()) {
                typesAdapter.data.clear()
                typesAdapter.notifyDataSetChanged()
                btnSearchMore.setImageResource(R.mipmap.icon_right)
            } else {
                btnSearchMore.setImageResource(R.mipmap.icon_down)
                typesAdapter.replaceData(types)
            }
        }

        textTypeDetail.text = types.first()

        var currentType = 0
        typesAdapter.setOnItemClickListener { _, _, position ->
            textTypeDetail.text = typesAdapter.data[position]
            currentType = position
            typesAdapter.data.clear()
            typesAdapter.notifyDataSetChanged()
            btnSearchMore.setImageResource(R.mipmap.icon_right)
            edtSearch.setText("")
        }


        btnCancelSearch.setOnClickListener {
            QMUIKeyboardHelper.hideKeyboard(it)
            edtSearch.setText("")
            containerSearch.visibility = View.GONE
        }

        edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                QMUIKeyboardHelper.hideKeyboard(v)
            }
            true
        }

        edtSearch.textChanges()
            .subscribe { c ->
                val chars = c.trim()
                if (chars.isNotEmpty()) {
                    searchData.filter {
                        it.searchString.contains(chars) /*&& it.type == currentType*/
                                && (
                                (it.type == 0 && user.cbtd == 1)
                                        || (it.type == 1 && user.zztd == 1)
                                        || (it.type == 2 && user.yzsdrk == 1)
                                        || (it.type == 3 && user.bytd1 == 1)
                                        || (it.type == 4 && user.bytd2 == 1)
                                        || (it.type == 5 && user.zdhx ==1)
                                )
                    }.apply {
                        searchAdapter.replaceData(this)
                        textResultCount.text = "查询结果(共${this.size}条记录)"
                    }
                } else {
                    searchAdapter.replaceData(mutableListOf())
                    textResultCount.text = "查询结果(共0条记录)"
                }
            }

        searchAdapter.setOnItemChildClickListener { adapter, view, position ->
            QMUIKeyboardHelper.hideKeyboard(view)
            containerSearch.visibility = View.GONE
            showZone(searchAdapter.data[position].id)
        }
    }

    private fun showZone(id: Long) {
        for (zone in dataChubei) {
            if (zone.id == id) {
                for (redLine in zone.redLines) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getMapColor(0))
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        zone.redLines.first().dot.first().trans(), 17f
                    )
                )

            } else {
                for (redLine in zone.redLines) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getColorTrans(0))
                }
            }
        }
        for (zone in dataYzs) {
            if (zone.id == id) {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getMapColor(1))
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        zone.dots.first().dot.first().trans(), 17f
                    )
                )

            } else {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getColorTrans(1))
                }
            }
        }

        for (zone in dataYgtd) {
            if (zone.id == id) {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getMapColor(4))
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        zone.dots.first().dot.first().trans(), 17f
                    )
                )

            } else {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getColorTrans(4))
                }
            }
        }
        for (zone in dataZdjh) {
            if (zone.id == id) {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getMapColor(3))
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        zone.dots.first().dot.first().trans(), 17f
                    )
                )

            } else {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getColorTrans(3))
                }
            }
        }
        for (zone in dataZdhx) {
            if (zone.id == id) {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getMapColor(2))
                }
                //XLog.e("测试zone是否取到数据 ${zone.id}")

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        zone.dots.first().dot.first().trans(), 17f
                    )
                )

            } else {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = ContextCompat.getColor(this,getColorTrans(4
                    ))
                }
            }
        }

        for (zone in dataZzs) {
            if (zone.id == id) {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = resources.getColor(getMapColor(2))
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        zone.dots.first().dot.first().trans(), 17f
                    )
                )

            } else {
                for (redLine in zone.dots) {
                    redLine.polygon?.fillColor = resources.getColor(getColorTrans(2))
                }
            }
        }
    }

    lateinit var cb0: CheckBox
    lateinit var cb1: CheckBox
    lateinit var cb2: CheckBox
    lateinit var cb3: CheckBox
    lateinit var cb4: CheckBox
    lateinit var cb5: CheckBox

    @SuppressLint("CheckResult")
    private fun initView() {
        viewPager.adapter = PagesAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = 5

        rb0.setBound()
        rb1.setBound()
        rb2.setBound()
        rb3.setBound()
        rb4.setBound()

        rgTabs.checkedChanges()
            .subscribe {
                when (it) {
                    rb0.id -> {
                        viewPager.visibility = View.GONE
                    }

                    rb1.id -> {
                        viewPager.visibility = View.VISIBLE
                        viewPager.setCurrentItem(0, false)
                    }

                    rb2.id -> {
                        viewPager.visibility = View.VISIBLE
                        viewPager.setCurrentItem(1, false)
                    }

                    rb3.id -> {
                        viewPager.visibility = View.VISIBLE
                        viewPager.setCurrentItem(3, false)
                    }

                    rb4.id -> {
                        viewPager.visibility = View.VISIBLE
                        viewPager.setCurrentItem(2, false)
                    }
                }
            }


        tabs.reset()

        tabs.setHasIndicator(true)
        tabs.setDefaultSelectedColor(ActivityCompat.getColor(this, R.color.text_gray))
        tabs.setDefaultSelectedColor(
            ActivityCompat.getColor(
                this, R.color.bg_blue
            )
        )
        tabs.setIndicatorWidthAdjustContent(false)
        tabs.setTabTextSize(QMUIDisplayHelper.dpToPx(14))

        tabs.addTab(QMUITabSegment.Tab("基础地图"))
        tabs.addTab(QMUITabSegment.Tab("影像地图"))
        tabs.invalidate()
        tabs.selectTab(0)

        map = mapView.map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(24.28, 116.12), 15f))
        converter = CoordinateConverter(this)
        converter.from(CoordinateConverter.CoordType.GPS)

        val lats = mutableListOf<LatLng>()
        var latsPolygon: Polygon? = null
        //地图点击事件
        map.setOnMapTouchListener {

            if (cbArea.isChecked) {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        map.uiSettings.apply {
                            isScrollGesturesEnabled = false
                        }
                        lats.clear()
                        lats.add(map.projection.fromScreenLocation(android.graphics.Point().apply {
                            x = it.x.toInt()
                            y = it.y.toInt()
                        }))
                        latsPolygon?.remove()
                        latsPolygon = map.addPolygon(
                            PolygonOptions().addAll(lats)
                                .strokeColor(ContextCompat.getColor(this,getMapColor(3)))
                                .fillColor(ContextCompat.getColor(this,getColorTrans(3)))
                                .strokeWidth(2f)
                                .zIndex(20f)
                        )
                    }
                    MotionEvent.ACTION_UP -> {
                        map.uiSettings.apply {
                            isScrollGesturesEnabled = true
                        }
                        val result = AMapUtils.calculateArea(lats)
                        if (result > 10) {


                            if (false) {

                                val points = latsPolygon!!.points
                                val minLat = points.minBy {
                                    it.latitude
                                }!!.latitude.apply {
                                    //                                    XLog.e("minLat $this")
                                }
                                val maxLat = points.maxBy {
                                    it.latitude
                                }!!.latitude.apply {
                                    //                                    XLog.e("maxLat $this")
                                }
                                val minLon = points.minBy {
                                    it.longitude
                                }!!.longitude.apply {
                                    //                                    XLog.e("minLon $this")
                                }
                                val maxLon = points.maxBy {
                                    it.longitude
                                }!!.longitude.apply {
                                    //                                    XLog.e("maxLon $this")
                                }

                                var zoneCount = 0
                                val t = System.currentTimeMillis()
                                var c = 0

                                val filtered = dataChubei.filter {
                                    val transed = it.redLines.flatMap {
                                        it.dot
                                    }.map {
                                        it.trans()
                                    }
                                    transed.any {
                                        it.latitude in minLat..maxLat
                                                && it.longitude in minLon..maxLon
                                    }
                                }
                                val time1 = System.currentTimeMillis() - t
                                for (zone in filtered) {
                                    zone.checked = false
                                    zone.redLines.flatMap {
                                        it.dot
                                    }.map { it.trans() }
                                        .any {
                                            latsPolygon!!.contains(it).apply {
                                                if (this) zone.checked = true
                                            }
                                        }
                                }

                                val time2 = System.currentTimeMillis() - t

                                for (zone in dataChubei) {
                                    if (zone.checked) {
                                        zoneCount += 1
                                        for (redLine in zone.redLines) {
                                            redLine.polygon?.fillColor =
                                                Color.BLACK
                                        }
                                    }
                                }


                                QMUIDialog.MessageDialogBuilder(this)
                                    .setTitle("区域统计")
                                    .setMessage("选中地块数量为：$zoneCount \n用时1 $time1 \n用时2 $time2")
                                    .addAction("确定") { a, b -> a.dismiss() }
                                    .create()
                                    .show()


                            } else {
                                latsPolygon?.fillColor = resources.getColor(getMapColor(3))
                                QMUIDialog.MessageDialogBuilder(this)
                                    .setTitle("测量面积")
                                    .setMessage("选中区域的面积为：$result ㎡")
                                    .addAction("确定") { a, b -> a.dismiss() }
                                    .create()
                                    .show()
                            }

                        } else {
                            latsPolygon?.remove()
                        }
//                        ToastUtil.showToast(this, result.toString())
//                        latsPolygon?.
                    }
                    MotionEvent.ACTION_MOVE -> {
                        lats.add(map.projection.fromScreenLocation(android.graphics.Point().apply {
                            x = it.x.toInt()
                            y = it.y.toInt()
                        }))
                        latsPolygon?.remove()
                        latsPolygon = map.addPolygon(
                            PolygonOptions().addAll(lats)
                                .strokeColor(resources.getColor(getMapColor(3)))
                                .fillColor(resources.getColor(getColorTrans(3)))
                                .strokeWidth(2f)
                                .zIndex(20f)
                        )
                    }

                }
            }
        }

        var startMarker: Marker? = null
        var endMarker: Marker? = null
        var naviMarker: Marker? = null

        map.setOnMapClickListener { lat ->
            if (cbNavi.isChecked) {
                if (naviMarker != null) {
                    naviMarker?.remove()
                    naviMarker = null
                }

                val markerOptions = MarkerOptions()
                    .position(lat)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_end))
                naviMarker = map.addMarker(markerOptions)
//                    btnChooseTarget.isEnabled = true
                btnStartNavi.isEnabled = true
            } else if (cbDistance.isChecked) {
                if (startMarker == null) {
                    val markerOptions = MarkerOptions()
                        .position(lat)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start))
                    startMarker = map.addMarker(markerOptions)
                } else if (endMarker == null) {
                    val markerOptions = MarkerOptions()
                        .position(lat)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_end))
                    endMarker = map.addMarker(markerOptions)

                    val result = AMapUtils.calculateLineDistance(startMarker?.position, endMarker?.position)

                    val d = QMUIDialog.MessageDialogBuilder(this)
                        .setTitle("测量距离")
                        .setMessage("选中两点的距离为：$result m")
                        .addAction("确定") { a, b ->
                            a.dismiss()
                            startMarker?.remove()
                            startMarker = null
                            endMarker?.remove()
                            endMarker = null
                        }
                        .create()

                    d.setCancelable(false)
                    d.show()
                }
            } else {

                if (containerXuncha.visibility == View.VISIBLE) {
                    ToastUtil.showToast(this@MainActivity, "请先退出巡查")
                    return@setOnMapClickListener
                }
                if (btnExitRe.visibility == View.VISIBLE) {
                    ToastUtil.showToast(this@MainActivity, "请先退出回放")
                    return@setOnMapClickListener
                }

                for (zone in dataChubei) {
                    val contains = zone.redLines
                        .any {
                            it.polygon != null && it.polygon!!.contains(lat)
                        }

                    for (redLine in zone.redLines) {
                        redLine.polygon?.fillColor = if (contains) ContextCompat.getColor(this,getMapColor(0)) else
                            ContextCompat.getColor(this,getColorTrans(0))
                    }

                    if (contains && cb0.isChecked) {
                        //对地块属性进行判断，如果被选中就显示，否则不显示
                        showPropertyPopup(zone.id, 0)
                    }
                }


                for (zone in dataYzs) {
                    val contains = zone.dots
                        .any {
                            it.polygon != null && it.polygon!!.contains(lat)
                        }

                    for (redLine in zone.dots) {
                        redLine.polygon?.fillColor = if (contains) ContextCompat.getColor(this,getMapColor(1)) else
                            ContextCompat.getColor(this,getColorTrans(1))
                    }

                    if (contains && cb1.isChecked) {
                        //对地块属性进行判断，如果被选中就显示，否则不显示
                        showPropertyPopup(zone.id, 1)
                    }
                }

                for (zone in dataYgtd) {
                    val contains = zone.dots
                        .any {
                            it.polygon != null && it.polygon!!.contains(lat)
                        }

                    for (redLine in zone.dots) {
                        redLine.polygon?.fillColor = if (contains) ContextCompat.getColor(this,getMapColor(4)) else
                            ContextCompat.getColor(this,getColorTrans(4))
                    }

                    if (contains && cb3.isChecked) {
                        //对地块属性进行判断，如果被选中就显示，否则不显示
                        showPropertyPopup(zone.id, 3)
                    }
                }
                for (zone in dataZdjh) {
                    val contains = zone.dots
                        .any {
                            it.polygon != null && it.polygon!!.contains(lat)
                        }

                    for (redLine in zone.dots) {
                        redLine.polygon?.fillColor =
                            if (contains) ContextCompat.getColor(this,getMapColor(3)) else
                                ContextCompat.getColor(this,getColorTrans(3))
                    }

                    if (contains && cb4.isChecked) {
                        //对地块属性进行判断，如果被选中就显示，否则不显示
                        showPropertyPopup(zone.id, 4)
                    }
                }
                for (zone in dataZdhx) {
                    val contains = zone.dots
                        .any {
                            it.polygon != null && it.polygon!!.contains(lat)
                        }

                    for (redLine in zone.dots) {
                        redLine.polygon?.fillColor =
                            if (contains) ContextCompat.getColor(this,getMapColor(2)) else
                                ContextCompat.getColor(this,getColorTrans(4))
                    }

                    if (contains && cb5.isChecked) {
                        //对地块属性进行判断，如果被选中就显示，否则不显示
                        showPropertyPopup(zone.id, 5)
                    }
                }

                for (zone in dataZzs) {
                    val contains = zone.dots
                        .any {
                            it.polygon != null && it.polygon!!.contains(lat)
                        }

                    for (redLine in zone.dots) {
                        redLine.polygon?.fillColor = if (contains) ContextCompat.getColor(this,getMapColor(2)) else
                            ContextCompat.getColor(this,getColorTrans(2))
                    }

                    if (contains && cb2.isChecked) {
                        showPropertyPopup(zone.id, 2)
                    }
                }
            }
        }

        tabs.addOnTabSelectedListener(object : QMUITabSegment.OnTabSelectedListener {
            override fun onDoubleTap(index: Int) {

            }

            override fun onTabReselected(index: Int) {
            }

            override fun onTabUnselected(index: Int) {
            }

            override fun onTabSelected(index: Int) {
                currentIndex = index
//                setMapData()
                setTileData()
            }
        })


        val popup = QMUIPopup(this)
        val view = layoutInflater.inflate(R.layout.popup_select_line, null)
        popup.setContentView(view)


        val s = view.findViewById<SwitchButton>(R.id.switcher)
        cb0 = view.findViewById(R.id.cb0)
        cb1 = view.findViewById(R.id.cb1)
        cb2 = view.findViewById(R.id.cb2)
        cb3 = view.findViewById(R.id.cb3)
        cb4 = view.findViewById(R.id.cb4)
        cb5 = view.findViewById(R.id.cb5)
        val user = MapApp.user
        if (user.cbtd == 0) {
            cb0.isChecked = false
            cb0.visibility = View.GONE
        }
        if (user.yzsdrk == 0) {
            cb1.isChecked = false
            cb1.visibility = View.GONE
        }
        if (user.zztd == 0) {
            cb2.isChecked = false
            cb2.visibility = View.GONE
        }
        if (user.bytd1 == 0) {
            cb3.isChecked = false
            cb3.visibility = View.GONE
        }
        if (user.bytd2 == 0) {
            cb4.isChecked = false
            cb4.visibility = View.GONE
        }
        if (user.zdhx == 0) {
            cb5.isChecked = false
            cb5.visibility = View.GONE
        }

        s.isChecked = true
        cbRedline.isChecked = true


        cbRedline.setOnClickListener {
            cbRedline.isChecked = s.isChecked
            popup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
            popup.setPreferredDirection(QMUIPopup.DIRECTION_NONE)
            popup.setPositionOffsetX(-container_btns.width)
            popup.show(container_btns)
        }

        cbDistance.setOnClickListener {
            cbNavi.isChecked = false
            cbArea.isChecked = false
            if (latsPolygon != null) {
                latsPolygon?.remove()
                latsPolygon = null
            }

            if (!cbDistance.isChecked) {
                cbDistance.isChecked = false
                if (startMarker != null) {
                    startMarker?.remove()
                    startMarker = null
                }
                if (endMarker != null) {
                    endMarker?.remove()
                    endMarker = null
                }
            }

            cbNavi.isChecked = false
            containerNavi.visibility = View.GONE
            if (naviMarker != null) {
                naviMarker?.remove()
                naviMarker = null
//                btnChooseTarget.isEnabled = false
                btnStartNavi.isEnabled = false
            }
        }

        cbArea.setOnClickListener {
            if (!cbArea.isChecked) {
                if (latsPolygon != null) {
                    latsPolygon?.remove()
                    latsPolygon = null
                }
            }
            cbDistance.isChecked = false
            if (startMarker != null) {
                startMarker?.remove()
                startMarker = null
            }
            if (endMarker != null) {
                endMarker?.remove()
                endMarker = null
            }
            cbNavi.isChecked = false
            containerNavi.visibility = View.GONE
            if (naviMarker != null) {
                naviMarker?.remove()
                naviMarker = null
//                btnChooseTarget.isEnabled = false
                btnStartNavi.isEnabled = false
            }
        }

        cbNavi.setOnClickListener {
            cbDistance.isChecked = false
            if (startMarker != null) {
                startMarker?.remove()
                startMarker = null
            }
            if (endMarker != null) {
                endMarker?.remove()
                endMarker = null
            }

            cbArea.isChecked = false
            if (latsPolygon != null) {
                latsPolygon?.remove()
                latsPolygon = null
            }

            if (cbNavi.isChecked) {
//                map.isMyLocationEnabled = true
//                AmapNaviPage.getInstance().showRouteActivity(this, AmapNaviParams(null), MyNavInfoCallback())
                containerNavi.visibility = View.VISIBLE

            } else {
//               map.isMyLocationEnabled = false
                containerNavi.visibility = View.GONE
                if (naviMarker != null) {
                    naviMarker?.remove()
                    naviMarker = null
//                    btnChooseTarget.isEnabled = false
                    btnStartNavi.isEnabled = false
                }
            }
        }

        btnStartNavi.setOnClickListener {
            if (naviMarker != null) {
                val a = map.myLocation
                naviMarker!!.position

                AmapNaviPage.getInstance()
                    .showRouteActivity(
                        this,
                        AmapNaviParams(
                            Poi("", LatLng(a.latitude, a.longitude), ""),
                            null,
                            Poi("", naviMarker!!.position, ""),
                            AmapNaviType.DRIVER,
                            AmapPageType.NAVI
                        ),
                        MyNavInfoCallback()
                    )
            }
        }

        popup.setOnDismissListener {
            cbRedline.isChecked = s.isChecked
            setMapData()
        }


        btnSearch.setOnClickListener {
            containerSearch.visibility = View.VISIBLE
            QMUIKeyboardHelper.showKeyboard(edtSearch, 200)
        }

        textMapProgress.visibility = View.GONE

        setMapData()

    }

    private fun setTileData() {
        if (currentIndex == 1) {
            tileOverlay = map.addTileOverlay(
                TileOverlayOptions().tileProvider(LocalTileTileProvider("/storage/emulated/0/map_data/")).diskCacheEnabled(
                    true
                )
                    .diskCacheDir("/storage/emulated/0/amap/cache")
                    .diskCacheSize(2000000)
                    .memoryCacheEnabled(true)
                    .memCacheSize(1000000)
                    .zIndex(10f)
            )
        } else {
            tileOverlay?.remove()
        }
    }

    private var addAllCbs = false
    private var addAllYzs = false
    private var addAllZzs = false
    private var addAllYgtd = false
    private var addAllZdjh = false
    private var addAllZdhx = false

    private var tileOverlay: TileOverlay? = null

    //    地图数据
    private fun setMapData() {

        map.mapType = AMap.MAP_TYPE_NORMAL
        map.isTrafficEnabled = false
        map.uiSettings.apply {
            isCompassEnabled = false
            isRotateGesturesEnabled = false
            isIndoorSwitchEnabled = false
            isScaleControlsEnabled = false
            isMyLocationButtonEnabled = false
            isZoomControlsEnabled = false
            setLogoBottomMargin(-50)
            isTiltGesturesEnabled = false // 倾斜手势
        }


        val myLocationStyle = MyLocationStyle()
        myLocationStyle.interval(2000)
        //消除定位蓝点产生的淡蓝色圆圈
        myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        //map.setMyLocationStyle(myLocationStyle)
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)

        map.myLocationStyle = myLocationStyle
        //map.setMyLocationEnabled(true)
        map.isMyLocationEnabled = true


        if (cbRedline.isChecked) {
            comp.clear()

            if (cb0.isChecked) {
                if (addAllCbs) {
                    addYzs()
                } else {
                    dataChubei.forEach {
                        it.redLines.forEach {
                            it.polygon?.remove()
                        }
                    }
                    textMapProgress.visibility = View.VISIBLE
                    var count = 0
                    Flowable.fromIterable(dataChubei)
                        .subscribeOn(Schedulers.io())
                        .subscribe({ zone ->
                            count++
                            runOnUiThread {
                                textMapProgress.text = "正在加载储备土地数据: $count/${dataChubei.size}"
                            }

                            for (redLine in zone.redLines) {

                                if (redLine.dot.isNotEmpty()) {
                                }
                                map.addPolygon(
                                    PolygonOptions().addAll(redLine.dot
                                        .map { d -> d.trans() })
                                        .strokeColor(ContextCompat.getColor(this,getMapColor(0)))
                                        .fillColor(ContextCompat.getColor(this,getColorTrans(0)))
                                        .strokeWidth(2f)
                                        .zIndex(20f)
                                ).apply {
                                    redLine.polygon = this
                                }
                            }
//"储备土地", "已征待入库", "在征土地"

                        }, { it.printStackTrace() }, {
                            runOnUiThread {
                                textMapProgress.visibility = View.GONE
                            }
                            addAllCbs = true
                            addYzs()
                        }).apply { comp.add(this) }
                }
            } else {
                dataChubei.forEach {
                    it.redLines.forEach {
                        it.polygon?.remove()
                    }
                }
                addAllCbs = false
                addYzs()
            }

        } else {

            comp.clear()

            dataChubei.forEach {
                it.redLines.forEach {
                    it.polygon?.remove()
                }
            }
            addAllCbs = false
            dataZzs.forEach {
                it.dots.forEach {
                    it.polygon?.remove()
                }
            }
            addAllZzs = false
            dataYzs.forEach {
                it.dots.forEach {
                    it.polygon?.remove()
                }
            }
            addAllYzs = false
            dataYgtd.forEach {
                it.dots.forEach { it.polygon?.remove() }
            }
            addAllYgtd = false
            dataZdjh.forEach {
                it.dots.forEach { it.polygon?.remove() }
            }
            addAllZdjh = false
            dataZdhx.forEach {
                it.dots.forEach { it.polygon?.remove() }
            }
            addAllZdhx = false

            textMapProgress.visibility = View.GONE
        }
    }
    //加载在征土地数据
    private fun addZzs() {
        XLog.e("addZzs " + cb2.isChecked)

        if (cb2.isChecked) {
            if (!addAllZzs) {
                dataZzs.forEach {
                    it.dots.forEach {
                        it.polygon?.remove()
                    }
                }

                runOnUiThread { textMapProgress.visibility = View.VISIBLE }
                var count = 0

                Flowable.fromIterable(dataZzs)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ zone ->
                        count++
                        runOnUiThread {
                            textMapProgress.text = "正在加载在征土地数据: $count/${dataZzs.size}"
                        }
                        //                    XLog.e(zone)
                        for (redLine in zone.dots) {
                            if (redLine.dot.isNotEmpty())
                                map.addPolygon(
                                    PolygonOptions().addAll(redLine.dot
                                        .map { d -> d.trans() })
                                        .strokeColor(ContextCompat.getColor(this,getMapColor(2)))
                                        .fillColor(ContextCompat.getColor(this,getColorTrans(2)))
                                        .strokeWidth(2f)
                                        .zIndex(20f)
                                ).apply {
                                    redLine.polygon = this
                                }
                        }
                    }, { it.printStackTrace() }, {
                        runOnUiThread { textMapProgress.visibility = View.GONE }
                        addAllZzs = true
                        addYgtd()
                    }).apply { comp.add(this) }
            } else {
                addYgtd()
            }
        } else {
            dataZzs.forEach {
                it.dots.forEach {
                    it.polygon?.remove()
                }
            }
            addAllZzs = false
            addYgtd()
        }
    }

    private fun addYgtd() {
        XLog.e("addYgtd " + cb3.isChecked + " " + dataYgtd.size)

        if (cb3.isChecked) {
            if (!addAllYgtd) {
                dataYgtd.forEach {
                    it.dots.forEach {
                        it.polygon?.remove()
                    }
                }

                runOnUiThread { textMapProgress.visibility = View.VISIBLE }
                var count = 0

                Flowable.fromIterable(dataYgtd)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ zone ->
                        count++
                        runOnUiThread {
                            textMapProgress.text = "正在加载已供土地数据: $count/${dataYgtd.size}"
                        }
                        //                    XLog.e(zone)
                        for (redLine in zone.dots) {
                            if (redLine.dot.isNotEmpty())
                                map.addPolygon(
                                    PolygonOptions().addAll(redLine.dot
                                        .map { d -> d.trans() })
                                        .strokeColor(ContextCompat.getColor(this,getMapColor(4)))
                                        .fillColor(ContextCompat.getColor(this,getColorTrans(4)))
                                        .strokeWidth(2f)
                                        .zIndex(20f)
                                ).apply {
                                    redLine.polygon = this
                                }
                        }
                    }, { it.printStackTrace() }, {
                        runOnUiThread { textMapProgress.visibility = View.GONE }
                        addAllYgtd = true
                        addZdjh()
                    }).apply { comp.add(this) }
            } else {
                addZdjh()
            }
        } else {
            dataYgtd.forEach {
                it.dots.forEach {
                    it.polygon?.remove()
                }
            }
            addZdjh()
            addAllYgtd = false
        }
    }

    private fun addZdjh() {
        XLog.e("addZdjh " + cb4.isChecked + " " + dataZdjh.size)
        if (cb4.isChecked) {
            if (!addAllZdjh) {
                dataZdhx.forEach {
                    it.dots.forEach {
                        it.polygon?.remove()
                    }
                }
                runOnUiThread { textMapProgress.visibility = View.VISIBLE }
                var count = 0
                Flowable.fromIterable(dataZdjh)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ zone ->
                        XLog.e("addZdjh $count")
                        count++
                        runOnUiThread {
                            textMapProgress.text = "正在加载重点计划区域数据: $count/${dataZdjh.size}"
                        }
                        //XLog.e(zone)
                        for (redLine in zone.dots) {
                            if (redLine.dot.isNotEmpty())
                                map.addPolygon(
                                    PolygonOptions().addAll(redLine.dot
                                        .map { d -> d.trans() })
                                        .strokeColor(ContextCompat.getColor(this,getMapColor(3)))
                                        .fillColor(ContextCompat.getColor(this,getColorTrans(3)))
                                        .strokeWidth(2f)
                                        .zIndex(20f)
                                ).apply {
                                    redLine.polygon = this
                                }
                        }
                    }, { it.printStackTrace() }, {
                        runOnUiThread { textMapProgress.visibility = View.GONE }
                        addAllZdjh =true
                        addZdhx()
                    }).apply { comp.add(this) }
            }else {
                addZdhx()
            }
        } else {
            dataZdjh.forEach {
                it.dots.forEach {
                    it.polygon?.remove()
                }
            }
            addZdhx()
            addAllZdjh = false
        }
    }

    private fun addZdhx() {
        //XLog.e("addZdhx " + cb5.isChecked + " " + dataZdhx.size)
        if (cb5.isChecked) {
            if (!addAllZdhx) {
                dataZdhx.forEach {
                    it.dots.forEach {
                        it.polygon?.remove()
                    }
                }

                runOnUiThread { textMapProgress.visibility = View.VISIBLE }
                var count = 0

                Flowable.fromIterable(dataZdhx)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ zone ->
                        //XLog.e("addZdhx $count")
                        count++
                        runOnUiThread {
                            textMapProgress.text = "正在加载征地红线数据: $count/${dataZdhx.size}"
                        }
                        XLog.e(zone)
                        for (redLine in zone.dots) {
                            if (redLine.dot.isNotEmpty())
                                map.addPolygon(
                                    PolygonOptions().addAll(redLine.dot
                                        .map { d -> d.trans() })
                                        .strokeColor(ContextCompat.getColor(this,getMapColor(2)))
                                        .fillColor(ContextCompat.getColor(this,getColorTrans(4)))
                                        .strokeWidth(2f)
                                        .zIndex(19f)
                                ).apply {
                                    redLine.polygon = this
                                }
                        }
                        addAllZdhx = true
                    }, { it.printStackTrace() }, {
                        runOnUiThread { textMapProgress.visibility = View.GONE }
                    }).apply { comp.add(this) }
            }
        } else {
            dataZdhx.forEach {
                it.dots.forEach {
                    it.polygon?.remove()
                }
            }
            addAllZdhx = false
        }
    }

    private fun addYzs() {
        XLog.e("addYzs " + cb1.isChecked)
        if (cb1.isChecked)
            if (!addAllYzs) {
                dataYzs.forEach {
                    it.dots.forEach {
                        it.polygon?.remove()
                    }
                }
                runOnUiThread { textMapProgress.visibility = View.VISIBLE }
                var count = 0

                Flowable.fromIterable(dataYzs)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ zone ->

                        count++
                        runOnUiThread {
                            textMapProgress.text = "正在加载已征待入库数据: $count/${dataYzs.size}"
                        }

                        for (redLine in zone.dots) {

                            if (redLine.dot.isNotEmpty())
                                map.addPolygon(
                                    PolygonOptions().addAll(redLine.dot
                                        .map { d -> d.trans() })
                                        .strokeColor(ContextCompat.getColor(this,getMapColor(1)))
                                        .fillColor(ContextCompat.getColor(this,getColorTrans(1)))
                                        .strokeWidth(2f)
                                        .zIndex(20f)
                                ).apply {
                                    redLine.polygon = this
                                }
                        }
                    }, { it.printStackTrace() }, {
                        runOnUiThread { textMapProgress.visibility = View.GONE }
                        addAllYzs = true
                        addZzs()
                    }).apply { comp.add(this) }
            } else {
                addZzs()
            }
        else {
            dataYzs.forEach {
                it.dots.forEach {
                    it.polygon?.remove()
                }
            }
            addAllYzs = false
            addZzs()
        }
    }

    private fun getMapColor(type: Int): Int {
        return when (type) {
            0 -> R.color.blue
            1 -> R.color.yellow
            2 -> R.color.red
            3 -> R.color.purple
            4 -> R.color.white

            else -> R.color.blue
        }
    }


    private fun getColorTrans(type: Int): Int {
        return when (type) {
            0 -> R.color.blueTrans
            1 -> R.color.yellowTrans
            2 -> R.color.redTrans
            3 -> R.color.purpleTrans
            4 -> R.color.whiteTrans
            else -> R.color.blue
        }
    }


    private lateinit var converter: CoordinateConverter

    //    todo 偏移量
    private fun Dot.trans(): LatLng {
        return converter.coord(LatLng(y - 0.000018, x)).convert()
    }

    lateinit var shangBaoFragment: ShangbaoFragment

    inner class PagesAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    GuanliFragment.newInstance()
                }
                1 -> LishiFragment.newInstance()
                2 -> TongjiFragment.newInstance()
                3 -> SettingFragment.newInstance()
                4 -> ShangbaoFragment.newInstance().apply {
                    shangBaoFragment = this
                }

                else -> MapFragment.newInstance()
            }
        }

        override fun getCount(): Int = 5
    }


    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView?.onDestroy()
        wakeLock.release()
    }


    private val CHANNEL_ID_SERVICE_RUNNING = "CHANNEL_ID_SERVICE_RUNNING"

    private fun createNotification(): Notification? {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel =
                NotificationChannel(CHANNEL_ID_SERVICE_RUNNING, "app service", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
            Notification.Builder(applicationContext, CHANNEL_ID_SERVICE_RUNNING)
        } else {
            Notification.Builder(applicationContext)
        }

        val nfIntent = Intent(this@MainActivity, MainActivity::class.java)
        nfIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        builder.setContentIntent(PendingIntent.getActivity(this@MainActivity, 0, nfIntent, 0))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("猎鹰sdk运行中")
            .setContentText("猎鹰sdk运行中")
        return builder.build()
    }

    override fun showDetail(id: Long) {
        XLog.d("showDetail $id")

    }
}

private fun RadioButton.setBound() {
    val drs = compoundDrawables
    val r = Rect(0, 0, drs[1].minimumWidth * 2 / 3, drs[1].minimumHeight * 2 / 3)
    drs[1].bounds = r
    setCompoundDrawables(null, drs[1], null, null)
}


class ZoneTypeAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_zone_type) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.text, item)
    }
}


class SearchAdapter : BaseQuickAdapter<SearchBean, BaseViewHolder>(R.layout.item_zone_search) {
    override fun convert(helper: BaseViewHolder, item: SearchBean) {
        helper.setText(R.id.textNum, "${helper.adapterPosition + 1}")
            .setText(R.id.text, item.showText)
        helper.addOnClickListener(R.id.btnZoneDetail)
    }
}

class SearchBean(var id: Long, var showText: String, var searchString: String, var type: Int = 0)

val Int.to2d: String
    get() {
        return String.format("%02d", this)
    }

package com.a01tech.map_gaode.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.a01tech.map_gaode.Dot
import com.a01tech.map_gaode.R
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.TileOverlayOptions
import com.amap.api.maps.model.UrlTileProvider
import com.elvishew.xlog.XLog
import kotlinx.android.synthetic.main.fragment_map.*
import java.net.URL


class MapFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }


    private lateinit var map: AMap
    private lateinit var myLocationStyle:MyLocationStyle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        val url = "http://192.168.0.107/%d/%d/%d.png"


        setMapData()
    }

    @SuppressLint("CheckResult")
    private fun setMapData() {
        map.mapType = AMap.MAP_TYPE_NORMAL
        map.isTrafficEnabled = false
        map.uiSettings.apply {
            isCompassEnabled = false
            isMyLocationButtonEnabled = true
        }


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(24.28, 116.12), 15f))


        map.addTileOverlay(
            TileOverlayOptions().tileProvider(object : UrlTileProvider(256, 256) {
                override fun getTileUrl(x: Int, y: Int, z: Int): URL {
                    return URL("http://192.168.0.107/$z/$x/$y.jpg".apply { XLog.d(this) })
//                    return URL("http://www.google.cn/maps/vt?lyrs=s&gl=cn&x=$x&s=&y=$y&z=$z".apply { XLog.d(this) })
                }
            }).diskCacheEnabled(true)
                .diskCacheDir("/storage/emulated/0/amap/cache")
                .diskCacheSize(100000)
                .memoryCacheEnabled(true)
                .memCacheSize(100000)
        )


        converter = CoordinateConverter(context)
        converter.from(CoordinateConverter.CoordType.GPS)

        /*  Flowable.just("")
                  .subscribeOn(Schedulers.io())
                  .map {
                      MapApp.gson.fromJson(
                              BufferedReader(InputStreamReader(resources.assets.open("data2.json"))),
                              Array<Zone>::class.java
                      ).toMutableList()
                  }.observeOn(AndroidSchedulers.mainThread()).subscribe {
                      for (zone in it) {
                          map.addPolygon(
                                  PolygonOptions().addAll(zone.dot.map { d -> d.trans() })
                                          .strokeColor(Color.RED)
                                          .fillColor(Color.TRANSPARENT)
                                          .strokeWidth(1f)
                          )
                      }
                  }*/
    }

    private lateinit var converter: CoordinateConverter

    private fun Dot.trans() =
        converter.coord(LatLng(y, x)).convert()


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
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
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView?.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MapFragment()
    }
}

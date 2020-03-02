package com.a01tech.map_gaode.interfaces

import android.view.View
import com.amap.api.navi.INaviInfoCallback
import com.amap.api.navi.model.AMapNaviLocation

class MyNavInfoCallback : INaviInfoCallback {
    override fun getCustomNaviView(): View? {
        return null
    }

    override fun getCustomNaviBottomView(): View? {
        return null
    }

    override fun onGetNavigationText(p0: String?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCalculateRouteSuccess(p0: IntArray?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInitNaviFailure() {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStrategyChanged(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onReCalculateRoute(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }


    override fun onCalculateRouteFailure(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMapTypeChanged(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChange(p0: AMapNaviLocation?) {
        //To change body of created functions use File | Settings | File Templates.
    }


    override fun onArrivedWayPoint(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onArriveDestination(p0: Boolean) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartNavi(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStopSpeaking() {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onExitPage(p0: Int) {

    }
}
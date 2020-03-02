package com.a01tech.map_gaode

import com.amap.api.track.query.model.*

open class MyTrackListener : OnTrackListener {
    override fun onLatestPointCallback(p0: LatestPointResponse) {
    }

    override fun onCreateTerminalCallback(p0: AddTerminalResponse) {
    }

    override fun onQueryTrackCallback(p0: QueryTrackResponse) {
    }

    override fun onDistanceCallback(p0: DistanceResponse) {
    }

    override fun onQueryTerminalCallback(p0: QueryTerminalResponse) {
    }

    override fun onHistoryTrackCallback(p0: HistoryTrackResponse) {
    }

    override fun onParamErrorCallback(p0: ParamErrorResponse) {
    }

    override fun onAddTrackCallback(p0: AddTrackResponse) {
    }
}
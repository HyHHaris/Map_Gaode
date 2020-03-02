package com.a01tech.map_gaode.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.ZoneEntityString
import com.a01tech.map_gaode.entity.*
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.ToastUtil
import com.elvishew.xlog.XLog
import com.google.android.material.chip.Chip
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_popup_dikuaishuxing.*
import kotlinx.android.synthetic.main.popup_select_line.*

private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BlankFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PropertyFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var listener: PropertyFragmentListener? = null

    private var type = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            type = it.getInt("type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_popup_dikuaishuxing, container, false)
    }

    lateinit var zoneEntity: ZoneEntityString
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (type) {
            0 -> {
                //对地块进行判断，如果地块被选中就显示，否则不显示
//                if(cb0.isChecked){

                val d = MapApp.gson.fromJson(param1, ZoneEntityString::class.java)
                zoneEntity = d
                btnHistory.setOnClickListener {
                    getXunChaHistory(d.cbbh!!)
                }

                btnStart.setOnClickListener {
                    dismiss()
                    listener?.startXuncha(d.id)
                }

                addChip(getType())
                addChip(d.tId)
                if (d.area.trim().isNotEmpty()) {
                    addChip(d.area + "㎡")
                }
                addChip(d.projectName)
                addChip(d.tdzl)
                addChip(d.mapNumber)
                XLog.e(" 储备土地数据 ${d.mapNumber}")
                if (!d.purpose1.isNullOrEmpty() && d.purpose1 != " ") addChip("${d.purpose1} ${d.sPurpose1}㎡")
                if (!d.purpose2.isNullOrEmpty() && d.purpose2 != " ") addChip("${d.purpose2} ${d.sPurpose2}㎡")
                if (!d.purpose3.isNullOrEmpty() && d.purpose3 != " ") addChip("${d.purpose3} ${d.sPurpose3}㎡")
                if (!d.purpose4.isNullOrEmpty() && d.purpose4 != " ") addChip("${d.purpose4} ${d.sPurpose4}㎡")
//            }
            }
            1 -> {
                val d = MapApp.gson.fromJson(param1, YzsBeanStr::class.java)
                /*  btnHistory.setOnClickListener {
                      getXunChaHistory(d.cbbh!!)
                  }*/

                btnStart.setOnClickListener {
                    dismiss()
                    listener?.startXuncha(d.id)
                }
                addChip(d.ghqk)
                if (d.area.trim().isNotEmpty()) {
                    addChip(d.area + "㎡")
                }
                addChip(getType())
                addChip(d.projectName)
                addChip(d.tdzl)
                addChip(d.mapNumber)
                XLog.e(" 已征待入库数据 ${d.mapNumber}" )
            }

            2 -> {
                val d = MapApp.gson.fromJson(param1, ZzsBeanStr::class.java)
                /*  btnHistory.setOnClickListener {
                      getXunChaHistory(d.id)
                  }*/

                btnStart.setOnClickListener {
                    dismiss()
                    listener?.startXuncha(d.id)
                }
                addChip(d.ghqk)

                if (d.area.trim().isNotEmpty()) {
                    addChip(d.area + "㎡")
                }

                addChip(getType())
                addChip(d.projectName)
                addChip(d.tdzl)
                addChip(d.mapNumber)
                XLog.e(" 在征土地数据 ${d.mapNumber}" )
            }
            3 -> {
                val d = MapApp.gson.fromJson(param1, YgtdBeanStr::class.java)
                /*  btnHistory.setOnClickListener {
                      getXunChaHistory(d.id)
                  }*/

                btnStart.setOnClickListener {
                    dismiss()
                    listener?.startXuncha(d.id)
                }
                addChip(d.tDSYQR)

                if (d.mJ1.trim().isNotEmpty()) {
                    addChip(d.mJ1 + "㎡")
                }

                addChip(getType())
                addChip(d.yT)
                addChip(d.gDPW)
                addChip(d.gDFS)
                XLog.e(" 已供土地数据 ${d.yT}" )
            }
            4 -> {
                val d = MapApp.gson.fromJson(param1, ZdjhBeanStr::class.java)
                btnStart.setOnClickListener {
                    dismiss()
                    listener?.startXuncha(d.id)
                }
                addChip(d.bZ)
/*
                if (d.area.trim().isNotEmpty()) {
                    addChip(d.area + "㎡")
                }*/

                addChip(getType())
                addChip(d.jJ)
                addChip(d.wZ)
                addChip(d.mJ+ "㎡")
                XLog.e(" 重点计划数据 ${d.mJ}" )
            }
            5 -> {
                val d = MapApp.gson.fromJson(param1, ZdhxBeanStr::class.java)
                btnStart.setOnClickListener {
                    dismiss()
                    listener?.startXuncha(d.id)
                }

                addChip(getType())
                addChip(d.mapNumber)
                addChip(d.mJ+ "㎡")
                addChip(d.bZ)
                XLog.e(" 征地红线数据 ${d.mJ}" )
            }

        }


        btnStart.visibility = if (type == 0 && MapApp.user.role == 2) View.VISIBLE else View.GONE
        btnHistory.visibility = if (type == 0) View.VISIBLE else View.GONE

        btnDetail.setOnClickListener {
            PropertyDetailFragment.newInstance(param1!!, type)
                .show(fragmentManager, "")
        }

        textContent.text = ""

    }

    private fun getType(): String {
        return when (type) {
            0 -> "储备土地"
            1 -> "已征待入库"
            2 -> "在征土地"
            3 -> "已供土地"
            4 -> "重点计划区域"
            5 -> "征地红线"
            else -> ""
        }
    }

    @SuppressLint("CheckResult")
    private fun getXunChaHistory(id: String) {
        if (MapApp.user.role == 1) {
            RetrofitUtil.getService()
                .patrolListManage(MapApp.user.token, zoneEntity.cbbh!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe({
                    if (it.code == 1) {
                        val list = it.getDataList("list", XunChaHistoryBean::class.java)
                        if (list.isEmpty()) {
                            ToastUtil.showToast(context, it.msg)
                        } else {
                            XunChaHistoryFragment.newInstance(it.obj.toString())
                                .show(fragmentManager, "")
                            dismiss()
                        }
                    } else {
                        ToastUtil.showToast(context, it.msg)
                    }
                }, { it.printStackTrace() })
        } else {
            RetrofitUtil.getService()
                .patrolList(MapApp.user.token, zoneEntity.cbbh!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe({
                    if (it.code == 1) {
                        val list = it.getDataList("list", XunChaHistoryBean::class.java)
                        if (list.isEmpty()) {
                            ToastUtil.showToast(context, it.msg)
                        } else {
                            XunChaHistoryFragment.newInstance(it.obj.toString())
                                .show(fragmentManager, "")
                            dismiss()
                        }
                    } else {
                        ToastUtil.showToast(context, it.msg)
                    }
                }, { it.printStackTrace() })
        }
    }

    private fun addChip(s: String) {
        if (s.trim().isNotEmpty())
            chipGroup.addView(
                Chip(context).apply {
                    if (s != MapApp.user.nickName) {
                        isCloseIconVisible = false
                        text = s
                    }
                    isClickable = false
//                        setChipBackgroundColorResource(R.color.qmui_config_color_white)
                    setTextColor(ContextCompat.getColor(context,R.color.normal_blue))
                    setChipStrokeColorResource(R.color.normal_blue)
                }
            )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PropertyFragmentListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface PropertyFragmentListener {
        fun showDetail(id: Long)

        fun startXuncha(id: Long)
    }

    companion object {
        @JvmStatic
        fun newInstance(s: String, i: Int) =
            PropertyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, s)
                    putInt("type", i)
                }
            }
    }
}


/*  "neckName": "张三",
      "id": "743247199802",
      "time": 1558321948000,
      "content": "缝纫工个人"*/

data class XunChaHistoryBean(
    var id: String = "",
    var time: String = "",
    var nickName: String = "",
    var content: String = ""
)
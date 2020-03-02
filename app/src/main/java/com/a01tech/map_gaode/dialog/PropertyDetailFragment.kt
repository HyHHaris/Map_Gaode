package com.a01tech.map_gaode.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.ZoneEntityString
import com.a01tech.map_gaode.entity.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.layout_popup_detail.*

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
class PropertyDetailFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null

    var type: Int = 0
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
        return inflater.inflate(R.layout.layout_popup_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rcv.layoutManager = LinearLayoutManager(context)
        val adapter = MyAdapter()
        rcv.adapter = adapter

        when (type) {
            0 -> {
                val d = MapApp.gson.fromJson(param1, ZoneEntityString::class.java)

                adapter.replaceData(
                    mutableListOf(
                        InfoBean("业务类型", d.tId),
                        InfoBean("项目名称", d.projectName),
                        InfoBean("地块编号", d.landNumber),
                        InfoBean("土地座落", d.tdzl),
                        InfoBean("图号", d.mapNumber),
                        InfoBean("用地面积", d.area + "(㎡)"),
                        InfoBean("面积2", d.area2 + "(亩)"),
                        InfoBean("用途1", d.purpose1),
                        InfoBean("s用途1", d.sPurpose1 + "(㎡)"),
                        InfoBean("用途2", d.purpose2),
                        InfoBean("s用途2", d.sPurpose2 + "(㎡)"),
                        InfoBean("用途3", d.purpose3),
                        InfoBean("s用途3", d.sPurpose3 + "(㎡)"),
                        InfoBean("用途4", d.purpose4),
                        InfoBean("s用途4", d.sPurpose4 + "(㎡)"),
                        InfoBean("总成本", d.totalCost + "(万元)"),
                        InfoBean("补偿成本", d.compensateCost + "(万元)"),
                        InfoBean("开发成本", d.developCost + "(万元)"),
                        InfoBean("其它成本", d.otherCost + "(万元)"),
                        InfoBean("备注", d.fbk),
                        InfoBean("土地管护", d.tdgh)
                    )
                )
            }
            1 -> {
                val d = MapApp.gson.fromJson(param1, YzsBeanStr::class.java)
                adapter.replaceData(
                    mutableListOf(
                        InfoBean("业务类型", d.ywlx),
                        InfoBean("项目名称", d.projectName),
                        InfoBean("地块编号", d.landNumber),
                        InfoBean("土地座落", d.tdzl),
                        InfoBean("图号", d.mapNumber),
                        InfoBean("面积1", d.area + "(㎡)"),
                        InfoBean("面积2", d.area2 + "(亩)"),
                        InfoBean("入库进度", d.rkjd),
                        InfoBean("存在问题", d.czwt),
                        InfoBean("规划情况", d.ghqk),
                        InfoBean("备注", d.bz)
                    )
                )
            }
            2 -> {
                val d = MapApp.gson.fromJson(param1, ZzsBeanStr::class.java)
                adapter.replaceData(
                    mutableListOf(
                        InfoBean("项目名称", d.projectName),
                        InfoBean("地块编号", d.landNumber),
                        InfoBean("土地座落", d.tdzl),
                        InfoBean("图号", d.mapNumber),
                        InfoBean("面积1", d.area + "(㎡)"),
                        InfoBean("面积2", d.area2 + "(亩)"),
                        InfoBean("要求时间", d.yqsj),
                        InfoBean("未签房屋", d.unsignedHouse),
                        InfoBean("未签土地", d.unsignedLand),
                        InfoBean("未签附物", d.unsignedAppendages),
                        InfoBean("未签坟墓", d.unsignedGrave),
                        InfoBean("征收公告", d.zsgg),
                        InfoBean("征收批文", d.zspw),
                        InfoBean("规划情况", d.ghqk),
                        InfoBean("备注", d.bz)
//                        InfoBean("补偿成本", d.compensateCost),
//                        InfoBean("开发成本", d.developCost),
//                        InfoBean("其它成本", d.otherCost),
//                        InfoBean("备注", d.fbk),
//                        InfoBean("土地管护", d.tdgh)
                    )
                )
            }

            3 -> {
                val d = MapApp.gson.fromJson(param1, YgtdBeanStr::class.java)
                adapter.replaceData(
                    mutableListOf(
                        InfoBean("土地使用权人", d.tDSYQR),
                        InfoBean("图号", d.tH),
                        InfoBean("面积1", d.mJ1 + "(㎡)"),
                        InfoBean("面积2", d.mJ2 + "(亩)"),
                        InfoBean("用途", d.yT),
                        InfoBean("容积率", d.rJL),
                        InfoBean("建筑密度", d.jZMD + "%"),
                        InfoBean("供地方式", d.gDFS),
                        InfoBean("成交总价", d.cJZJ + "(万元)"),
                        InfoBean("成交单价", d.cJDJ + "(万元/亩)"),
                        InfoBean("楼面价", d.lMJ + "(元/㎡)"),
                        InfoBean("供地批文", d.gDPW),
                        InfoBean("批文日期", d.pWRQ),
                        InfoBean("备注", d.bZ)
                    )
                )
            }
            4 -> {
                val d = MapApp.gson.fromJson(param1, ZdjhBeanStr::class.java)
                adapter.replaceData(
                    mutableListOf(
                        InfoBean("位置", d.wZ),
                        InfoBean("面积", d.mJ+ "(㎡)"),
                        InfoBean("简介", d.jJ),
                        InfoBean("备注", d.bZ)
                    )
                )
            }
            5 -> {
                val d = MapApp.gson.fromJson(param1, ZdhxBeanStr::class.java)
                adapter.replaceData(
                    mutableListOf(
                        InfoBean("图号", d.mapNumber),
                        InfoBean("面积", d.mJ+ "(㎡)"),
                        InfoBean("备注", d.bZ)
                    )
                )
            }
        }

    }


    companion object {
        @JvmStatic
        fun newInstance(s: String, type: Int) =
            PropertyDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, s)
                    putInt("type", type)
                }
            }
    }
}

private class InfoBean(val title: String, val content: String?)

private class MyAdapter() : BaseQuickAdapter<InfoBean, BaseViewHolder>(R.layout.item_property_detail) {
    override fun convert(helper: BaseViewHolder, item: InfoBean) {
        helper.setText(R.id.textTitle, item.title)
            .setText(R.id.textContent, item.content)

        helper.layoutPosition
        helper.itemView.setBackgroundColor(
            ActivityCompat.getColor(
                mContext,
                if (helper.adapterPosition % 2 == 0) R.color.qmui_config_color_white else R.color.qmui_config_color_gray_7
            )
        )
    }
}
package com.a01tech.map_gaode.fragments


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.dialog.XunChaDetailFragment
import com.a01tech.map_gaode.entity.UserInfo
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.DialogUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.ToastUtil
import com.amap.api.col.stln3.it
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.elvishew.xlog.XLog
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_tongji.*
import java.text.SimpleDateFormat
import java.util.*


class TongjiFragment : Fragment() {

    private val cd = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tongji, container, false)
    }

    private val timeFormatter = SimpleDateFormat("yyyy-MM-dd")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChart()

        val calendarNew = Calendar.getInstance()
        val calendarOld = Calendar.getInstance()
        calendarOld.set(Calendar.MONTH, calendarNew.get(Calendar.MONTH) - 1)

        textStart.text = timeFormatter.format(calendarOld.time)
        textEnd.text = timeFormatter.format(calendarNew.time)

        textStart.setOnClickListener {
            DatePickerDialog(
                context!!,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    calendarOld.set(Calendar.YEAR, year)
                    calendarOld.set(Calendar.MONTH, month)
                    calendarOld.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    textStart.text = timeFormatter.format(calendarOld.time)
                },
                calendarOld.get(Calendar.YEAR),
                calendarOld.get(Calendar.MONTH),
                calendarOld.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        textEnd.setOnClickListener {
            DatePickerDialog(
                context!!,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    calendarNew.set(Calendar.YEAR, year)
                    calendarNew.set(Calendar.MONTH, month)
                    calendarNew.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    textEnd.text = timeFormatter.format(calendarNew.time)
                },
                calendarNew.get(Calendar.YEAR),
                calendarNew.get(Calendar.MONTH),
                calendarNew.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        loadNameData()
        adapter = TongjiAdapter()
        rcv.adapter = adapter
        rcv.layoutManager = LinearLayoutManager(context)

        adapter.setOnItemChildClickListener { _, view, position ->
            val dialog = DialogUtil.genLoadingDialog(context!!)
            dialog.show()
            RetrofitUtil.getService()
                .xunchaDetail(
                    MapApp.user.token,
                    adapter.data[position].id
                ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe({
                    dialog.dismiss()
                    if (it.code == 1) {
                        val d = it.getData("map", XunChaBean::class.java)
                        d?.id = adapter.data[position].id

                        XunChaDetailFragment.newInstance(MapApp.gson.toJson(d)).show(fragmentManager, "")
                    } else {
                        ToastUtil.showToast(context, it.msg)
                    }
                }, {
                    it.printStackTrace()
                    ToastUtil.showToast(context, "ERROR")
                    dialog.dismiss()
                })
        }

        textSearch.setOnClickListener {
            loadHistoryData()
        }
    }

    lateinit var adapter: TongjiAdapter

    @SuppressLint("CheckResult")
    private fun loadNameData() {
        RetrofitUtil.getService()
            .patrolPersonnelManage(MapApp.user.token)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { JsonData(it) }
            .subscribe({ it ->
                if (it.code == 1) {
                    val userInfos = it.getDataList("list", UserInfo::class.java)
                    val d = userInfos.map { user ->
                        user.nickName
                    }
                    if (d.isNotEmpty()) {
                        val simpleAdapter =
                            ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, d)
                        spinner.adapter = simpleAdapter

                        loadHistoryData()
                    } else {
                        textEmpty.visibility == View.VISIBLE
                    }

                } else ToastUtil.showToast(context, it.msg)


            }, { it.printStackTrace() })
    }

    @SuppressLint("CheckResult")
    private fun loadHistoryData() {
        val parse1 = timeFormatter.parse(textStart.text.toString())
        val parse2 = timeFormatter.parse(textEnd.text.toString())
        if (parse2.time <= parse1.time) {
            ToastUtil.showToast(context, "时间错误")
        } else if (
            spinner.selectedItem == null
        ) {
            textEmpty.visibility == View.VISIBLE
            ToastUtil.showToast(context, "没有数据")
        } else {
            RetrofitUtil.getService()
                .patrolHistoryManage(
                    MapApp.user.token, spinner.selectedItem as String, textStart.text.toString()
                    , textEnd.text.toString()
                ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe({
                    if (it.code == 1) {
                        val d = it.getDataList("list", TongjiBean::class.java)
                        adapter.replaceData(d)
                        textEmpty.visibility = if (d.isEmpty()) View.VISIBLE else View.GONE
                        ToastUtil.showToast(context, it.msg)
                    } else {
                        ToastUtil.showToast(context, it.msg)
                    }
                }, {
                    it.printStackTrace()
                    ToastUtil.showToast(context, "ERROR")
                })

        }
    }

    var t = 0L
    private fun initChart() {
        chart.description.isEnabled = false
        chart.setPinchZoom(false)
        chart.setDrawBarShadow(false)
        chart.setDrawGridBackground(false)

        val xFormater = SimpleDateFormat("MM/dd")

        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(true)
        l.yOffset = 0f
        l.xOffset = 10f
        l.yEntrySpace = 0f
        l.textSize = 8f

        val xAxis = chart.xAxis
        xAxis.granularity = 1f

        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 7
        /*设置X轴的位置（默认在上方）*/
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        /*设置X轴值的时间（月日）*/
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                //柱状图老是差一天，柱状图会显示最新的时间
                //return xFormater.format(t - (6 - value) * 24 * 60 * 60 * 1000)
                return xFormater.format(t - (6 - value) * 24 * 60 * 60 * 1000)
            }
        }

        val leftAxis = chart.axisLeft
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value % 1 == 0F) value.toInt().toString() else ""
            }
        }

        leftAxis.setDrawGridLines(true)
        leftAxis.spaceTop = 35f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        chart.axisRight.isEnabled = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            loadData()
        }
    }

    private fun loadData() {
        loadMpData()
        loadCountData(0, 0)
        loadCountData(0, 1)
        loadCountData(1, 2)
        loadCountData(1, 3)
    }

    private val strs = mutableListOf(
        "patrolAreaCountDayManage"
        , "patrolAreaCountWeekManage", "uploadCountDayManage", "uploadCountWeekManage"
    )
    private val pStrs = mutableListOf(
        "patrol"
        , "patrolimg"
    )

    private fun loadCountData(p1: Int, type: Int) {
        RetrofitUtil.getService()
            .getCount(pStrs[p1], strs[type], MapApp.user.token)
            .map { JsonData(it) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 1) {
                    when (type) {
                        0 -> num1
                        1 -> num2
                        2 -> num3
                        3 -> num4
                        else -> num4
                    }.text = it.obj.get("num").asInt.toString()
                }
            }, { it.printStackTrace() }).apply {
                cd.add(this)
            }
    }


    inner class DateNumBean(var date: String, var num: Int) {
        @SuppressLint("SimpleDateFormat")
        fun getTime() = SimpleDateFormat("yyyy-MM-dd").parse(date).time
    }

    override fun onDestroy() {
        super.onDestroy()
        cd.clear()
    }

    var set1: BarDataSet? = null
    var set2: BarDataSet? = null
    @SuppressLint("CheckResult")
    private fun loadMpData() {
        RetrofitUtil.getService()
            .newCylindricalManage(MapApp.user.token)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { JsonData(it) }
            .subscribe({
                if (it.code == 1) {
                    //加载
                    val list = it.getDataList("list", DateNumBean::class.java).toMutableList()
                    XLog.e("巡查次数 天数 ${list.size}")
                    XLog.e("${list[0].num} + ${list[0].date}")
                    list.removeAt(0)

//                    if (list.size > 14) {
//                        for (i in 1..(list.size - 14)) {
//                            list.removeAt(0)
//                        }
//                    }
//
//                    if (list.size < 14) {
//                        list.add(DateNumBean("", 0))
//                    }
                    XLog.e("巡查次数 天数 ${list.size}")
                    XLog.e("${list[0].num} + ${list[0].date}")
                    XLog.e("${list[7].num} + ${list[7].date}")
                    XLog.e("${list[1].num} + ${list[1].date}")
                    XLog.e("${list[8].num} + ${list[8].date}")
                    XLog.e("${list[2].num} + ${list[2].date}")
                    XLog.e("${list[9].num} + ${list[9].date}")
                    XLog.e("${list[3].num} + ${list[3].date}")
                    XLog.e("${list[10].num} + ${list[10].date}")
                    XLog.e("${list[4].num} + ${list[4].date}")
                    XLog.e("${list[11].num} + ${list[11].date}")
                    XLog.e("${list[5].num} + ${list[5].date}")
                    XLog.e("${list[12].num} + ${list[12].date}")
                    XLog.e("${list[6].num} + ${list[6].date}")
                    XLog.e("${list[13].num} + ${list[13].date}")

                    val values1 = ArrayList<BarEntry>()
                    val values2 = ArrayList<BarEntry>()
                    t = list.last().getTime()
                    XLog.e("${t} ")
                    for (i in 0..6 ) {
                        values1.add(
                            BarEntry(
                                i.toFloat(),
                                (list[list.size - 7 + i].num).toFloat()
                            )
                        )
                        values2.add(
                            BarEntry(
                                i.toFloat(),
                                (list[list.size - 14 + i].num).toFloat()
                            )
                        )
                    }


                    if (chart.data != null && chart.data.dataSetCount > 0) {
                        set1 = chart.data.getDataSetByIndex(0) as BarDataSet
                        set2 = chart.data.getDataSetByIndex(1) as BarDataSet
                        set1?.values = values2
                        set2?.values = values1
                        chart.data.notifyDataChanged()
                        chart.notifyDataSetChanged()
                    } else {
                        set1 = BarDataSet(values2, "上周")
                        set1?.color = Color.parseColor("#B5B5B5")
                        set2 = BarDataSet(values1, "本周")
                        set2?.color = Color.parseColor("#1875F0")

                        val data = BarData(set1, set2)
                        //设置图表显示的数据
                        data.setValueFormatter(LargeValueFormatter())
                        chart.data = data
                    }

                    // specify the width each bar should have
                    chart.barData.barWidth = 0.3f

                    // restrict the x-axis range
                    //x轴显示在底部
                    chart.xAxis.axisMinimum = 0f

                    // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
                    chart.xAxis.axisMaximum = 7f
                    chart.groupBars(0f, 0.4f, 0f)
                    chart.invalidate()
                }
            }, {
                it.printStackTrace()
            })
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            TongjiFragment()
    }
}


class TongjiBean(
    var name: String, var id: String, var land: String, var time: String
)


class TongjiAdapter : BaseQuickAdapter<TongjiBean, BaseViewHolder>(R.layout.item_tongji) {

    override fun convert(helper: BaseViewHolder, item: TongjiBean) {
        helper.setText(R.id.text1, item.time)
            .setText(R.id.text2, item.name)
            .setText(R.id.text3, item.land)
            .addOnClickListener(R.id.btn1)
    }
}

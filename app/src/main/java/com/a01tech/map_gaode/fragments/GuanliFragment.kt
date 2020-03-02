package com.a01tech.map_gaode.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.DialogUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.MessageEvent
import com.a01tech.map_gaode.utils.ToastUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.elvishew.xlog.XLog
import com.google.gson.annotations.SerializedName
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_guanli.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat


class GuanliFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guanli, container, false)
    }

    lateinit var adapter: GuanliAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rcv.layoutManager = LinearLayoutManager(context)
        adapter = GuanliAdapter()
        rcv.adapter = adapter

        adapter.setOnItemChildClickListener { _, view, position ->


            XLog.e("ClickBianji")
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
                        EventBus.getDefault().post(MessageEvent(1, MapApp.gson.toJson(d)))
                    } else {
                        ToastUtil.showToast(context, it.msg)
                    }
                }, {
                    it.printStackTrace()
                    ToastUtil.showToast(context, "ERROR")
                    dialog.dismiss()
                })

        }
    }

    @SuppressLint("CheckResult")
    private fun loadData() {
        RetrofitUtil.getService()
            .xunchaGuanliLiebiao(MapApp.user.token)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { JsonData(it) }
            .subscribe({
                if (it.code == 1) {
                    val data = it.getDataList("list", GuanLiBean::class.java)
                    adapter.replaceData(data)
                } else {
                    ToastUtil.showToast(context, it.msg)
                }
            }, {
                it.printStackTrace()
                ToastUtil.showToast(context, "ERROR")
            })

    }

    override fun onStart() {
        XLog.e("onStart")
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        XLog.e("onStop")
    }

    override fun onPause() {
        super.onPause()
        XLog.e("onPause")
    }

    override fun onResume() {
        super.onResume()
        XLog.e("onResume")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        XLog.e("isVisibleToUser $isVisibleToUser")
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            loadData()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GuanliFragment()
    }
}


/*         "createTime": 1556602762000,
            "tdzl": "深圳市南山区明亮科技园",
            "id": "874125214521",
            "content": "巡查测试"*/

/*"piIds": "736065983464,770754121431",
"tdzl": "深圳市南山区明亮科技园",
"trajectory": "轨迹数据20190429",
"content": "大王叫我来巡山",
"situation": "一切正常"*/
class XunChaBean(
    var id: String,
    var piIds: String,
    @SerializedName("projectName") var name: String,
    var trajectory: String,
    var content: String,
    var situation: String,
    var createTime: Long,
    var saveStatus: Int = 0,
    var savePath: String = ""
)

class GuanLiBean(
    var createTime: Long,
    @SerializedName("projectName") var name: String,
    var id: String,
    var content: String,
    var situation: String
)

class GuanliAdapter : BaseQuickAdapter<GuanLiBean, BaseViewHolder>(R.layout.item_guanli) {
    private val formatter = SimpleDateFormat("yyyy/M/dd HH:mm")

    override fun convert(helper: BaseViewHolder, item: GuanLiBean) {
        val t = System.currentTimeMillis() - item.createTime
        val s = 24 * 60 - t / 60000
        helper.setText(R.id.text1, "${s / 60}小时${s % 60}分钟")
            .setText(R.id.text2, item.name)
            .setText(R.id.text3, formatter.format(item.createTime))
            .setText(R.id.text4, item.situation)
            .addOnClickListener(R.id.btn1)
    }
}

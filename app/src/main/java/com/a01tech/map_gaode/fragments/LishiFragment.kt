package com.a01tech.map_gaode.fragments


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.dialog.XunChaDetailFragment
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.DialogUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.MessageEvent
import com.a01tech.map_gaode.utils.ToastUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.annotations.SerializedName
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lishi.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat


class LishiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lishi, container, false)
    }

    lateinit var adapter: LishiAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rcv.layoutManager = LinearLayoutManager(context)
        adapter = LishiAdapter()
        rcv.adapter = adapter

        adapter.setOnItemChildClickListener { _, view, position ->
            if (view.id == R.id.btn2 && adapter.data[position].edit == "N") {
                return@setOnItemChildClickListener
            }
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
                        if (view.id == R.id.btn2) {
                            EventBus.getDefault().post(MessageEvent(2, MapApp.gson.toJson(d)))
                        } else {
                            XunChaDetailFragment.newInstance(MapApp.gson.toJson(d)).show(fragmentManager, "")
                        }
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
            .xunchaLishiLiebiao(MapApp.user.token)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { JsonData(it) }
            .subscribe({
                if (it.code == 1) {
                    val data = it.getDataList("list", LishiBean::class.java)
                    adapter.replaceData(data)
                }
            }, {
                it.printStackTrace()
                ToastUtil.showToast(context, "ERROR")
            })

    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            loadData()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LishiFragment()
    }
}

/*  "edit": "N",

      "createTime": 1556446368000,

      "tdzl": "深圳市南山区明亮科技园",

      "id": "512478541256",

      "trajectory": "轨迹数据20190426_16:31",

      "content": "整个科技园进行巡查",

      "situation": "一切正常"*/
class LishiBean(
    var createTime: Long,
    @SerializedName("projectName") var name: String,
    var id: String,
    var content: String,
    var trajectory: String,
    var situation: String,
    var edit: String,
    var cbbh: String = ""
)

class LishiAdapter : BaseQuickAdapter<LishiBean, BaseViewHolder>(R.layout.item_lishi) {
    private val formatter = SimpleDateFormat("yyyy/M/dd HH:mm")

    override fun convert(helper: BaseViewHolder, item: LishiBean) {
        helper.setText(R.id.text1, /*item.id*/item.cbbh)
            .setText(R.id.text2, item.name)
            .setText(R.id.text3, formatter.format(item.createTime))
            .setText(R.id.text4, item.situation)
            .addOnClickListener(R.id.btn1)
            .addOnClickListener(R.id.btn2)

        helper.setBackgroundColor(
            R.id.btn2, if (item.edit == "Y") {
                Color.parseColor("#FFEE3F64")
            } else {
                Color.parseColor("#FFF499AC")
            }
        )
    }

}




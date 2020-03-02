package com.a01tech.map_gaode.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.PhotoActivity
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.fragments.XunChaBean
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.MessageEvent
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.material.chip.Chip
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_popup_xuncha.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat

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
class XunChaDetailFragment : DialogFragment() {

    private var param1: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_popup_xuncha, container, false)
    }

    override fun onStart() {
        super.onStart()
        getDialog().getWindow().setLayout(QMUIDisplayHelper.dpToPx(400), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val bean = MapApp.gson.fromJson(param1, XunChaBean::class.java)

        val adapter = UploadAdapter()
        rcv.layoutManager = GridLayoutManager(context, 3)
        rcv.adapter = adapter
        adapter.replaceData(bean.piIds.split(",").filter {
            it.isNotEmpty()
        })
        adapter.setOnItemClickListener { _, view, position ->

            RetrofitUtil.getService()
                .photoDetail(MapApp.user.token, adapter.data[position])
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe(
                    {
                        if (it.code == 1) {
                            val path = it.obj.get("patrolImg").asJsonObject.get("img").asString

                            startActivity(Intent(context, PhotoActivity::class.java).apply {
                                putExtra("href", path)
                            })
                        }
                    }, { it.printStackTrace() }
                )

        }
        /*"piIds": "736065983464,770754121431",
"tdzl": "深圳市南山区明亮科技园",
"trajectory": "轨迹数据20190429",
"content": "大王叫我来巡山",
"situation": "一切正常"*/
        val formatter = SimpleDateFormat("yyyyMMdd HH:mm")
        text1.text = formatter.format(bean.createTime)
        text2.text = bean.name
        text3.text = bean.trajectory
//        text4.text = bean.situation
        setChipData(bean.content)

        textContent.text = bean.situation


        btnBack.setOnClickListener { dismiss() }
        btn1.setOnClickListener {
            if (bean.saveStatus == 0) {
                EventBus.getDefault().post(MessageEvent(77, bean.trajectory))
            } else if (bean.saveStatus == 1) {
                EventBus.getDefault().post(MessageEvent(78, bean.savePath))
            }
            dismiss()
        }

    }

    private fun setChipData(s: String) {
        text4.removeAllViews()
        val list = s.split(",")

        for (s in list) {
            if (s.isNotEmpty()) {
                text4.addView(
                    Chip(context).apply {
                        isCloseIconVisible = false
                        text = s
                        isClickable = false
//                        setChipBackgroundColorResource(R.color.qmui_config_color_white)
                        setTextColor(resources.getColor(R.color.normal_blue))
                        setChipStrokeColorResource(R.color.normal_blue)
                    }
                )
            }
        }

    }


    companion object {
        @JvmStatic
        fun newInstance(s: String) =
            XunChaDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, s)
                }
            }
    }
}


class UploadAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_upload_img) {
    override fun convert(helper: BaseViewHolder, item: String) {
        RetrofitUtil.getService()
            .photoDetail(MapApp.user.token, item)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { JsonData(it) }
            .subscribe(
                {
                    if (it.code == 1) {
                        val path = it.obj.get("patrolImg").asJsonObject.get("img").asString
                        Glide.with(mContext).load(path/*.replaceX()*/).into(helper.getView(R.id.img))
                    }
                }, { it.printStackTrace() }
            )

        helper.setVisible(R.id.btnCancel, false)
            .setVisible(R.id.textStatus, false)
    }
}

